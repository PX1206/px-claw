# retrieval-service

PX-Claw 语义检索微服务。基于 FAISS + sentence-transformers，为 `system` 后端提供 HTTP 检索接口，支撑 AI 客服知识库 RAG。路径均相对 `main.py` 所在目录解析，便于整包复制到 U 盘或其它盘符离线运行。

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Python 3 |
| Web 框架 | FastAPI + Uvicorn |
| 向量检索 | FAISS（faiss-cpu） |
| 句向量 | sentence-transformers 2.x |
| 默认模型 | `paraphrase-multilingual-MiniLM-L12-v2`（可本地目录或 Hub 下载） |

## 主要功能

- 将 FAQ 话术文件按行建索引，支持语义相似度检索
- Excel 导入格式优化：行首「问题 + 空格 + 话术」精确匹配与前缀加分重排
- 按用户隔离：`~/.px-claw/faq_{owner_user_id}.txt` 独立索引与缓存
- 文件变更后通过 `/reload` 重建或清除缓存
- 离线部署：将完整模型目录放入 `models/paraphrase-multilingual-MiniLM-L12-v2/` 即可无网启动

## 环境要求

- Python 3.8+
- pip 可安装 `requirements.txt` 中的依赖
- 可选：本地 embedding 模型（见 `models/` 目录说明）

## 启动方式

```bash
cd retrieval-service
pip install -r requirements.txt
python main.py
```

或使用 uvicorn：

```bash
uvicorn main:app --host 127.0.0.1 --port 9170
```

默认监听：**127.0.0.1:9170**

启动时会自动加载 FAQ 并构建 FAISS 索引；日志会输出索引条数与模型路径。

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `HOST` | `127.0.0.1` | 绑定地址 |
| `PORT` | `9170` | 监听端口 |
| `FAQ_FILE` | 见下文 | 强制指定 FAQ 文件路径 |
| `EMBEDDING_MODEL` | 本地目录或 Hub 名 | 句向量模型路径或 HuggingFace 模型名 |
| `FAISS_POOL_MULT` | `8` | 检索候选池扩大倍数（用于重排） |
| `FAISS_PREFIX_BONUS` | `0.22` | 问题前缀匹配加分权重 |

## FAQ 文件解析顺序

1. 若设置了 `FAQ_FILE` 环境变量，使用该路径
2. 否则若存在 `~/.px-claw/faq.txt`，使用该文件（与 Java 端 `ai.chat.user-faq-path` 默认一致）
3. 否则使用包内 `data/faq.txt`

多用户隔离时，Java 端会写入 `~/.px-claw/faq_{owner_user_id}.txt`，检索请求携带 `owner_user_id` 时使用对应文件。

## HTTP 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/health` | 健康检查：索引条数、FAQ 路径、模型信息 |
| POST | `/search` | 语义检索 |
| POST | `/reload` | 重建索引或清除指定用户缓存 |

### POST /search 请求体

```json
{
  "query": "用户问题",
  "top_k": 3,
  "owner_user_id": 1
}
```

`owner_user_id` 可选；省略时使用全局 FAQ 文件索引。

### POST /reload 请求体

```json
{
  "owner_user_id": 1
}
```

省略 `owner_user_id` 时清空全部用户缓存并重建全局索引。话术导入或同步后，由 `system` 后端自动调用此接口。

## 模型目录

默认本地路径：

```
retrieval-service/models/paraphrase-multilingual-MiniLM-L12-v2/
```

目录内需包含 `config.json` 等 sentence-transformers 标准文件。若本地目录不存在，首次启动会尝试从 HuggingFace Hub 下载（需联网）。

`requirements.txt` 中 `sentence-transformers` 限定 `<3.0.0`，与仓库内离线快照兼容；若缺少 `1_Pooling/config.json` 等文件，请参考 `models` 目录说明补全。

## 与 system 的集成

`system` 的 `application.yml`：

```yaml
ai:
  chat:
    retrieval-base-url: http://127.0.0.1:9170
    user-faq-path: ${user.home}/.px-claw/faq.txt
```

Java 端通过 `FaissRetrievalClient` 调用 `/search`；话术变更后 POST `/reload`。

桌面端（`client`）不直连本服务，经 `system` 的 `/chat/rag-context` 间接使用。

## 典型启动顺序

1. MySQL / Redis
2. **retrieval-service**（本模块，9170）
3. **system**（9168）
4. **client**（可选）

详见 [`system/README.md`](../system/README.md)、[`client/README.md`](../client/README.md)。
