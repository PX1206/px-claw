# client

PX-Claw 桌面端，基于 Electron 的可移植 AI 客服客户端。提供多模型对话、AI 客服（知识库检索 + 本地/云端推理）、邮件辅助回复、平台账号登录等功能。

## 技术栈

| 类别 | 技术 |
|------|------|
| 桌面框架 | Electron 30.5.1 |
| 前端 | 原生 HTML / CSS / JavaScript（无 React / Vue） |
| 本地推理 | llama.cpp（`llama-server`、`llama-completion`，需自行放置于 `bin/`） |
| 邮件 | imapflow、mailparser、nodemailer |
| 打包 | electron-builder 26.8.1（Windows portable） |

## 主要功能

### 对话

- 多会话本地 / 云端 LLM 聊天
- 支持本机 GGUF（Qwen2.5 等）、自建 OpenAI 兼容服务、OpenAI、豆包等
- 附件选择、流式输出

### AI 客服

- 从后端 `POST /chat/rag-context` 获取知识库检索结果
- 推理走与「对话」相同的模型配置（本地 IPC 或 OpenAI 兼容 API）
- 话术管理：Excel 导入、CRUD、同步 FAQ 至检索服务
- 会话快照同步至后端归档

### 邮件

- 桌面端 IMAP 收信 → RAG + LLM 草稿 → 人工审核 → SMTP 发信
- 配置保存在 `config/mail.json`
- 与后端 Java 邮件模块二选一，无需同时启用

### 模型配置

- 管理多个 LLM 提供商（本地 gguf、vLLM / llama.cpp server、OpenAI、豆包等）
- 配置保存在 `config/providers.json`

### 平台账号

- 对接 `system` 后端：登录 / 注册、资料、头像、改密
- 会话保存在 `config/session.json`

## 环境要求

- Node.js 18+（建议 LTS）
- Windows（当前打包目标为 portable；开发可在 Windows 上 `npm start`）
- **system** 后端已启动（默认 `http://127.0.0.1:9168`）
- **retrieval-service** 已启动（AI 客服检索，默认 `http://127.0.0.1:9170`，由后端调用；桌面端间接依赖）
- 本地模型（可选）：`bin/` 下 llama.cpp 可执行文件与 DLL；`models/` 下 GGUF 权重

## 启动方式

```bash
cd client
npm install
npm start          # 开发：electron .
npm run dist       # 打包：Windows portable 可执行文件
```

打包产物应用 ID：`com.uclaw.app`，产品名：**PX-Claw**。

## 目录与配置

### 运行时资源（通常不在 Git 中）

| 路径 | 说明 |
|------|------|
| `bin/llama-server.exe` | 本地模型 HTTP 服务（对话 / AI 客服本地推理） |
| `bin/llama-completion.exe` | 一次性补全（需与同目录 `.dll` 配套） |
| `models/*.gguf` | 本地模型权重，默认 `qwen2.5-3b-instruct-q8_0.gguf` |

可通过环境变量 `UCLAW_MODEL` 指定默认 GGUF 文件名。

### 配置文件

| 文件 | 用途 |
|------|------|
| `config/session.json` | 后端地址、Token、当前用户信息 |
| `config/providers.json` | LLM 提供商列表与当前选中项 |
| `config/mail.json` | IMAP / SMTP（邮件功能） |

**`config/session.json` 示例：**

```json
{
  "baseUrl": "http://127.0.0.1:9168",
  "token": "",
  "user": null
}
```

**`config/providers.json` 示例：** 见仓库内 `config/providers.json`（含 `local`、`openai_compat` 等 kind）。

首次使用请在后端注册账号或使用初始化管理员 **admin / Admin123**（需已执行 `system` 的 `init.sql`）。

## 内部端口

| 服务 | 端口 | 说明 |
|------|------|------|
| llama-server | 127.0.0.1:18768 | 主进程自动拉起，供本地 OpenAI 兼容调用 |
| system 后端 | 9168 | `session.json` → `baseUrl` |
| retrieval-service | 9170 | 由后端代理检索，桌面端不直连 |

## 项目结构

| 路径 | 说明 |
|------|------|
| `main.js` | Electron 主进程入口 |
| `preload.js` | 预加载脚本，暴露 `window.uClaw` |
| `renderer/` | 界面 HTML / CSS / JS |
| `lib/` | 后端 API、邮件轮询、OpenAI 流式、配置读写等 |

## 常见问题

**找不到 llama-server / llama-completion**  
从与当前 llama.cpp 版本一致的构建产物中，将可执行文件及同目录 `.dll` 复制到 `bin/`。GPU 版需本机 CUDA；缺 DLL 时可安装 VC++ 2015–2022 x64 可再发行包。

**AI 客服无检索结果**  
确认 `retrieval-service` 与 `system` 均已启动，且已在 AI 客服页导入话术并触发 FAQ 同步。

**邮件功能**  
在「邮件」设置页填写 IMAP / SMTP；默认轮询间隔 120000 ms。勿与后端 `mail.inbound` 同时对同一邮箱重复拉信。

## 与其他模块的关系

```
client
  ├── HTTP → system (:9168)     登录、话术、RAG 上下文、会话归档
  └── 本地   llama-server (:18768)  可选，本地 GGUF 推理

system → retrieval-service (:9170)  FAISS 检索（桌面端经后端调用）
```

典型启动顺序：MySQL / Redis → `retrieval-service` → `system` → `client`。

详见 [`system/README.md`](../system/README.md)、[`retrieval-service/README.md`](../retrieval-service/README.md)。
