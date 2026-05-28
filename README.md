# PX-Claw

可移植 AI 客服平台：桌面端对话与 AI 客服、知识库 RAG 检索、可选邮件辅助回复，以及用户 / 文件 / 安装包等后端能力。三端可整包复制到 U 盘或内网离线部署。

## 项目结构

| 模块 | 说明 | 文档 |
|------|------|------|
| [`system/`](system/) | Spring Boot 后端：用户权限、文件同步、AI 客服 API、安装包分发 | [system/README.md](system/README.md) |
| [`client/`](client/) | Electron 桌面端：对话、AI 客服、邮件、本地/云端模型 | [client/README.md](client/README.md) |
| [`retrieval-service/`](retrieval-service/) | Python FAISS 语义检索微服务 | [retrieval-service/README.md](retrieval-service/README.md) |
| [`docs/`](docs/) | 设计文档（如邮件 AI 回复方案） | — |

本仓库**无独立 Web 管理端**；管理类操作可通过 Knife4j API 文档或桌面端（管理员角色）完成。

## 架构

```
┌─────────────────────────────────────────────────────────┐
│  client (Electron)                                      │
│  · 对话 / AI 客服 / 邮件                                   │
│  · 本地 llama-server (:18768，可选)                        │
└───────────────────────────┬─────────────────────────────┘
                            │ HTTP :9168
                            ▼
┌─────────────────────────────────────────────────────────┐
│  system (Spring Boot)                                   │
│  · 用户 / 文件 / 话术 / 会话归档                           │
│  · /chat/rag-context → 检索                              │
└───────────────────────────┬─────────────────────────────┘
                            │ HTTP :9170
                            ▼
┌─────────────────────────────────────────────────────────┐
│  retrieval-service (FastAPI + FAISS)                    │
│  · 话术 FAQ 向量检索                                      │
└─────────────────────────────────────────────────────────┘

依赖：MySQL (claw_db) · Redis · RocketMQ（部分功能，可选）
```

**AI 客服数据流（桌面端推荐路径）：**

1. 用户在桌面端提问
2. `client` → `system` `POST /chat/rag-context` 获取检索上下文
3. `system` → `retrieval-service` `POST /search`
4. `client` 使用本地或云端 LLM 生成回复

## 技术栈概览

| 层级 | 技术 |
|------|------|
| 后端 | Java 8、Spring Boot 2.7、MyBatis-Plus、MySQL、Redis、RocketMQ |
| 桌面端 | Electron 30、原生 HTML/JS、llama.cpp |
| 检索 | Python 3、FastAPI、FAISS、sentence-transformers |
| 文档 | Knife4j（`http://localhost:9168/doc.html`） |

各模块详细版本与依赖见对应 README。

## 环境要求

| 组件 | 版本 / 说明 |
|------|-------------|
| JDK | 8+ |
| Maven | 3.6+ |
| Node.js | 18+（桌面端） |
| Python | 3.8+（检索服务） |
| MySQL | 8.0+，库名 `claw_db` |
| Redis | 必需（后端会话与缓存） |
| RocketMQ | 可选（操作日志等 MQ 功能） |

**运行时资源（通常不随 Git 提交，需自行准备）：**

- `client/bin/` — llama.cpp 可执行文件与 DLL
- `client/models/*.gguf` — 本地对话模型（如 Qwen2.5-3B）
- `retrieval-service/models/` — 句向量模型（或首次联网从 Hub 下载）
- `system/src/main/resources/application.yml` — 本地数据库、Redis 等配置

## 快速启动

按以下顺序启动（开发环境默认端口）：

### 1. 基础设施

```bash
# 初始化数据库（MySQL）
mysql -u root -p < system/src/main/resources/db/init.sql
```

默认管理员：**admin / Admin123**

按需启动 Redis、RocketMQ。

### 2. 检索服务（9170）

```bash
cd retrieval-service
pip install -r requirements.txt
python main.py
```

### 3. 后端（9168）

```bash
cd system
# 复制并编辑 application.yml（数据库、Redis 等）
mvn spring-boot:run
```

API 文档：`http://localhost:9168/doc.html`

### 4. 桌面端（可选）

```bash
cd client
npm install
npm start
```

打包 Windows 便携版：`npm run dist`

## 端口一览

| 服务 | 端口 | 配置位置 |
|------|------|----------|
| system | 9168 | `system/.../application.yml` |
| retrieval-service | 9170 | `retrieval-service/main.py` 或 `PORT` 环境变量 |
| client llama-server | 18768 | `client/main.js`（本地模型） |
| MySQL | 3306 | `application.yml` |
| Redis | 6379 | `application.yml` |
| RocketMQ NameServer | 9876 | `application.yml` |

## 数据库迁移

**全新环境：** 执行 `system/src/main/resources/db/init.sql`。

**已有库增量迁移**（按顺序，已执行可跳过）：

| 顺序 | 脚本 | 说明 |
|------|------|------|
| 1 | `db/migration_user_sync_quota.sql` | 用户同步配额 |
| 2 | `db/migration_file_sync_columns.sql` | 文件同步字段 |
| 3 | `db/migration_install_package.sql` | 安装包表 |
| 4 | `db/migration_install_package_sha512.sql` | 安装包 SHA512 |
| 5 | `db/migration_mail_channel.sql` | Java 邮件渠道（可选） |
| 6 | `sql/chat_script.sql` | AI 客服话术 |
| 7 | `sql/ai_cs_chat_session.sql` | AI 客服会话归档 |
| 8 | `sql/migrate_chat_script_owner_user.sql` | 话术按用户隔离 |

## 主要功能

- **多模型对话**：本机 GGUF、OpenAI 兼容 API、豆包等
- **AI 客服**：Excel 话术导入、FAISS 检索、本地/云端推理、会话归档
- **邮件辅助**（二选一）：桌面端 IMAP/SMTP，或后端 Java 邮件模块 + 人工审核发信
- **平台能力**：用户注册登录、RBAC、文件上传与同步目录、安装包分发与桌面自动更新
- **可移植部署**：检索服务与桌面端路径相对模块根目录，适合 U 盘 / 离线环境

## 配置与安全

- 含密钥的配置文件（`application.yml`、`client/config/session.json`、`mail.json`、`providers.json`）**勿提交到公共仓库**
- 生产环境使用 `spring.profiles.active=prod`，敏感项通过环境变量注入
- 邮件渠道：Java 端 `mail.inbound.enabled` / `mail.smtp.enabled` 默认 `false`；桌面端配置见 `client/config/mail.json`

## 模块文档

- [system/README.md](system/README.md) — 后端 API、数据库、邮件渠道、AI 客服接口
- [client/README.md](client/README.md) — Electron 启动、模型与配置文件、打包
- [retrieval-service/README.md](retrieval-service/README.md) — FAISS 检索 API、FAQ 路径、环境变量

## 许可证

请根据项目实际情况补充开源许可证（如 MIT、Apache-2.0 等）。
