"""
FAISS + 句向量检索服务，供 Java 端 HTTP 调用。
路径均相对本文件所在目录解析，便于整包复制到 U 盘或其它盘符运行。
需先 pip install -r requirements.txt；未放置本地模型时会尝试从 Hub 下载。
"""
import logging
import os
from contextlib import asynccontextmanager
from typing import Dict, List, Optional, Tuple

import faiss
import numpy as np
from fastapi import FastAPI
from pydantic import BaseModel, Field
from sentence_transformers import SentenceTransformer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 可移植根目录：始终为 main.py 所在目录（勿依赖当前工作目录 cwd，U 盘/快捷方式启动也正确）
APP_ROOT = os.path.normpath(os.path.dirname(os.path.abspath(__file__)))

def _default_faq_path() -> str:
    """与 Java 端 `user-faq-path` 默认一致：用户目录存在话术文件时优先使用，否则用包内 data/faq.txt。"""
    home_f = os.path.normpath(os.path.join(os.path.expanduser("~"), ".px-claw", "faq.txt"))
    if os.path.isfile(home_f):
        return home_f
    return os.path.join(APP_ROOT, "data", "faq.txt")


def get_active_faq_path() -> str:
    """每次加载前解析：未设置 FAQ_FILE 时，若用户目录已导入话术则自动切换到该文件。"""
    env = os.environ.get("FAQ_FILE", "").strip()
    if env:
        return os.path.normpath(env)
    return os.path.normpath(_default_faq_path())

# 默认本地模型目录：retrieval-service/models/paraphrase-multilingual-MiniLM-L12-v2/
_DEFAULT_MODEL_DIR = os.path.join(APP_ROOT, "models", "paraphrase-multilingual-MiniLM-L12-v2")


def resolve_embedding_model() -> str:
    """
    解析句向量模型路径：
    1) 环境变量 EMBEDDING_MODEL（绝对路径或相对 APP_ROOT 的相对路径）
    2) 若默认本地目录下存在 config.json，则使用该目录（离线、可 U 盘部署）
    3) 否则回退为 HuggingFace 模型名（需联网下载）
    """
    raw = os.environ.get("EMBEDDING_MODEL", "").strip()
    if raw:
        p = raw if os.path.isabs(raw) else os.path.normpath(os.path.join(APP_ROOT, raw))
        return p
    cfg = os.path.join(_DEFAULT_MODEL_DIR, "config.json")
    if os.path.isfile(cfg):
        return os.path.normpath(_DEFAULT_MODEL_DIR)
    return "paraphrase-multilingual-MiniLM-L12-v2"


MODEL_NAME = resolve_embedding_model()
HOST = os.environ.get("HOST", "127.0.0.1")
PORT = int(os.environ.get("PORT", "9170"))

# 检索池扩大倍数（再大也不会超过语料条数）；用于前缀加分重排时能覆盖更多候选
_FAISS_POOL_MULT = max(2, min(48, int(os.environ.get("FAISS_POOL_MULT", "8"))))
# 话术行若以「用户问题」为行首且与话术列空格分隔（Excel 导入形态），向量分上叠加权，压住「你是谁」≠「你是智障…」的纯向量趋近
_FAISS_PREFIX_BONUS = float(os.environ.get("FAISS_PREFIX_BONUS", "0.22"))


@asynccontextmanager
async def lifespan(_app: FastAPI):
    build_index()
    yield


app = FastAPI(title="FAISS Retrieval Service", version="1.0.0", lifespan=lifespan)

model: Optional[SentenceTransformer] = None
index: Optional[faiss.Index] = None
corpus: List[str] = []
# Java 多端隔离：faq_{owner_user_id}.txt -> corpus + 独立索引（mtime 失效则重建）
OWNER_INDEX_CACHE: Dict[int, Tuple[float, List[str], Optional[faiss.Index]]] = {}



class SearchBody(BaseModel):
    query: str = Field(..., description="用户问题")
    top_k: int = Field(3, ge=1, le=50)
    owner_user_id: Optional[int] = Field(
        default=None, description="话术归属用户 id，对应 ~/.px-claw/faq_{id}.txt；不传则兼容旧版单 faq.txt"
    )


class ReloadBody(BaseModel):
    owner_user_id: Optional[int] = Field(default=None, description="仅清除该用户缓存；不传则重建兼容单文件索引")


class SearchResponse(BaseModel):
    ok: bool = True
    passages: List[str] = Field(default_factory=list)
    scores: List[float] = Field(default_factory=list)
    message: Optional[str] = None


def _exact_imported_line(lines: List[str], q_raw: str) -> Optional[str]:
    """与 Java RagService.exactImportedLine 一致：整行形如「问题 话术 …」，先命中再问量。"""
    q = (q_raw or "").strip()
    if not q:
        return None
    for ln in lines:
        t = ln.strip()
        if t.startswith(q + " ") or t.startswith(q + "\t") or t == q:
            return ln
    return None


def _prefix_bonus_for_line(line: str, q_raw: str) -> float:
    """行首与用户问题完全一致（话术表「问题」列）时加权，语义相近但问题不同的行不得分。"""
    q = (q_raw or "").strip()
    if not q:
        return 0.0
    t = line.strip()
    if t.startswith(q + " ") or t.startswith(q + "\t") or t == q:
        return _FAISS_PREFIX_BONUS
    return 0.0


def _rerank_semantic_then_prefix(
    indices_row: np.ndarray,
    scores_row: np.ndarray,
    q_raw: str,
    top_out: int,
    all_lines: List[str],
) -> tuple[List[str], List[float]]:
    """向量分 + 行首前缀分 综合排序后再截断 top_out。"""
    scored: List[tuple[float, float, str]] = []
    for rank in range(int(indices_row.shape[0])):
        ti = int(indices_row[rank])
        if ti < 0:
            continue
        line = all_lines[ti]
        raw = float(scores_row[rank])
        eff = raw + _prefix_bonus_for_line(line, q_raw)
        scored.append((eff, raw, line))
    scored.sort(key=lambda x: (-x[0], -x[1]))
    seen: set = set()
    passages: List[str] = []
    scores: List[float] = []
    for eff, _raw, line in scored:
        if line in seen:
            continue
        seen.add(line)
        passages.append(line)
        scores.append(eff)
        if len(passages) >= top_out:
            break
    return passages, scores


def load_corpus() -> List[str]:
    path = get_active_faq_path()
    if not os.path.isfile(path):
        logger.warning("FAQ 文件不存在: %s", path)
        return []
    with open(path, encoding="utf-8") as f:
        return [
            ln.strip()
            for ln in f.readlines()
            if ln.strip() and not ln.strip().startswith("#")
        ]


def faq_path_for_owner(owner_id: int) -> str:
    """与 Java ChatScriptService.resolveFaqTxtPathForUser 约定一致。"""
    return os.path.normpath(
        os.path.join(os.path.expanduser("~"), ".px-claw", f"faq_{int(owner_id)}.txt")
    )


def lines_from_path(path: str) -> List[str]:
    if not os.path.isfile(path):
        return []
    with open(path, encoding="utf-8") as f:
        return [
            ln.strip()
            for ln in f.readlines()
            if ln.strip() and not ln.strip().startswith("#")
        ]


def _ensure_embedding_model() -> None:
    global model
    if model is not None:
        return
    if os.path.isdir(MODEL_NAME) and os.path.isfile(os.path.join(MODEL_NAME, "config.json")):
        logger.info("加载本地句向量模型: %s", MODEL_NAME)
        os.environ.setdefault("HF_HUB_OFFLINE", "1")
        os.environ.setdefault("TRANSFORMERS_OFFLINE", "1")
    else:
        logger.info("加载句向量模型（将尝试 Hub）: %s", MODEL_NAME)
    model = SentenceTransformer(MODEL_NAME)


def get_owner_corpus_and_index(owner_id: int) -> Tuple[List[str], Optional[faiss.Index]]:
    """按用户隔离的 faq 文件 + 缓存的 FAISS 索引。"""
    path = faq_path_for_owner(owner_id)
    mtime = os.path.getmtime(path) if os.path.isfile(path) else -1.0
    cached = OWNER_INDEX_CACHE.get(owner_id)
    if cached is not None and cached[0] == mtime:
        return cached[1], cached[2]

    lines = lines_from_path(path)
    if not lines:
        OWNER_INDEX_CACHE[owner_id] = (mtime, [], None)
        return [], None

    _ensure_embedding_model()
    assert model is not None
    vecs = model.encode(lines, normalize_embeddings=True, show_progress_bar=False)
    vecs = np.asarray(vecs, dtype="float32")
    dim = vecs.shape[1]
    idx = faiss.IndexFlatIP(dim)
    idx.add(vecs)
    OWNER_INDEX_CACHE[owner_id] = (mtime, lines, idx)
    logger.debug("owner=%s 索引就绪: %s 条", owner_id, len(lines))
    return lines, idx


def build_index() -> None:
    global index, corpus
    corpus = load_corpus()
    if not corpus:
        index = None
        logger.warning("知识库为空，未建立索引")
        return
    _ensure_embedding_model()
    assert model is not None
    vecs = model.encode(corpus, normalize_embeddings=True, show_progress_bar=False)
    vecs = np.asarray(vecs, dtype="float32")
    dim = vecs.shape[1]
    idx = faiss.IndexFlatIP(dim)
    idx.add(vecs)
    index = idx
    logger.info("FAISS 索引就绪: %s 条, 维度=%s", len(corpus), dim)


@app.get("/health")
def health():
    return {
        "status": "ok",
        "indexed": len(corpus),
        "app_root": APP_ROOT,
        "faq_path": get_active_faq_path(),
        "embedding_model": MODEL_NAME,
        "embedding_local": bool(
            os.path.isdir(str(MODEL_NAME)) and os.path.isfile(os.path.join(str(MODEL_NAME), "config.json"))
        ),
        "faiss_prefix_bonus": _FAISS_PREFIX_BONUS,
        "faiss_pool_mult": _FAISS_POOL_MULT,
    }


@app.post("/reload")
def reload(body: Optional[ReloadBody] = None):
    """FAQ 更新后调用：带 owner_user_id 时仅丢该用户缓存；否则重建兼容单文件索引。"""
    if body is None:
        body = ReloadBody()
    if body.owner_user_id is not None:
        OWNER_INDEX_CACHE.pop(int(body.owner_user_id), None)
        logger.info("已清除 owner=%s 的 FAISS 缓存", body.owner_user_id)
        return {"ok": True, "cleared_owner": body.owner_user_id}
    OWNER_INDEX_CACHE.clear()
    build_index()
    return {"ok": True, "indexed": len(corpus)}


@app.post("/search", response_model=SearchResponse)
def search_endpoint(body: SearchBody):
    q = (body.query or "").strip()
    if not q:
        return SearchResponse(ok=False, message="empty query")

    oid = body.owner_user_id
    if oid is not None:
        corp, oid_index = get_owner_corpus_and_index(int(oid))
        if not corp or oid_index is None or model is None:
            return SearchResponse(ok=False, message="index not ready or empty corpus")

        exact = _exact_imported_line(corp, q)
        if exact is not None:
            return SearchResponse(ok=True, passages=[exact], scores=[1.0])

        top_k = min(int(body.top_k), len(corp))
        retrieve_k = min(len(corp), max(top_k, min(len(corp), top_k * _FAISS_POOL_MULT)))

        qv = model.encode([q], normalize_embeddings=True)
        qv = np.asarray(qv, dtype="float32")
        scores, ids = oid_index.search(qv, retrieve_k)
        passages, out_scores = _rerank_semantic_then_prefix(
            ids[0], scores[0], q, top_out=top_k, all_lines=corp
        )
        if not passages:
            return SearchResponse(ok=False, message="no passages after rerank")
        return SearchResponse(ok=True, passages=passages, scores=out_scores)

    if not corpus or index is None or model is None:
        return SearchResponse(ok=False, message="index not ready or empty corpus")

    exact = _exact_imported_line(corpus, q)
    if exact is not None:
        return SearchResponse(ok=True, passages=[exact], scores=[1.0])

    top_k = min(int(body.top_k), len(corpus))
    retrieve_k = min(len(corpus), max(top_k, min(len(corpus), top_k * _FAISS_POOL_MULT)))

    qv = model.encode([q], normalize_embeddings=True)
    qv = np.asarray(qv, dtype="float32")
    scores, ids = index.search(qv, retrieve_k)
    passages, out_scores = _rerank_semantic_then_prefix(
        ids[0], scores[0], q, top_out=top_k, all_lines=corpus
    )
    if not passages:
        return SearchResponse(ok=False, message="no passages after rerank")
    return SearchResponse(ok=True, passages=passages, scores=out_scores)


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host=HOST, port=PORT)
