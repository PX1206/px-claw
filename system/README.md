# system

PX-Claw 后端主服务，提供用户与权限、文件同步、安装包分发、AI 客服知识库与检索、邮件客服渠道等 REST API。桌面端（`client`）与 Python 检索服务（`retrieval-service`）均依赖本模块。

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 / 框架 | Java 8、Spring Boot 2.7.6 |
| 持久层 | MyBatis-Plus 3.3.1、MySQL 8、Druid 连接池 |
| 缓存 / 消息 | Redis、RocketMQ 2.1.1（按功能启用） |
| API 文档 | Knife4j 2.0.9（Swagger UI） |
| 工具库 | Hutool、Guava、Apache POI（话术 Excel 导入） |
| 其他 | Spring Mail（IMAP 收信 / SMTP 发信）、阿里云短信（可选） |

## 主要功能

### 平台基础

- **用户与认证**：注册、密码登录、短信登录、RSA 加密传输、个人资料与头像
- **权限管理**：角色、菜单、操作日志
- **文件管理**：上传、下载、权限过滤；桌面同步走 `/file/uploadForSync`
- **同步目录**：用户本地同步目录的云端记录
- **同步配额**：按用户限制同步文件总占用（`user.sync_quota_bytes`，默认 5 GiB）
- **安装包分发**：管理员上传；公开下载与 `GET /open/installPackage/latest.yml`（electron-updater）
- **辅助能力**：图形验证码、地区数据、短信验证码（阿里云，按配置启用）

### AI 客服（`/chat`）

- 话术 CRUD、Excel 导入 / 模板下载
- 调用 `retrieval-service`（默认 `http://127.0.0.1:9170`）做 FAISS 语义检索
- `POST /chat/rag-context`：仅检索上下文（桌面端本地推理时使用）
- `POST /chat`：检索 + 服务端推理（脚本调试 / 无桌面端场景）
- AI 客服会话快照归档（`/chat/ai-cs/session/snapshot`）
- 按用户隔离话术与 FAQ 文件：`~/.px-claw/faq_{userId}.txt`

### 邮件渠道（可选，默认关闭）

- IMAP 轮询收信 → AI 生成建议回复 → 人工审核 → SMTP 发出
- REST：`/mail/suggested-reply/**`
- 与桌面端「邮件」页二选一即可，无需同时启用 Java 与 Electron 两套邮件逻辑

## 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 8.0+（库名 `claw_db`）
- Redis
- RocketMQ（若使用依赖 MQ 的功能）
- **retrieval-service** 已启动（AI 客服检索，端口 9170）

## 启动方式

### 开发环境

```bash
cd system
mvn spring-boot:run
```

主类：`com.claw.system.SystemApplication`  
默认端口：**9168**（`application.yml` → `server.port`）  
默认 Profile：`dev`（`spring.profiles.active`）

### 生产环境

```bash
mvn package -DskipTests
java -jar target/system-1.0.0.jar --spring.profiles.active=prod
```

生产配置见 `application-prod.yml`（如 `local.host` 对外 URL）。敏感信息请通过环境变量或外部配置注入，**勿将生产密钥提交到 Git**。

### 测试与打包

```bash
mvn test
mvn package -DskipTests
# 产物：target/system-1.0.0.jar
```

## API 文档

启动后访问（需 Basic 认证，默认账号见 `application.yml` 中 `knife4j.basic`）：

- 文档 UI：`http://localhost:9168/doc.html`

## 配置说明

| 配置项 | 说明 | 默认值 / 位置 |
|--------|------|----------------|
| `server.port` | 服务端口 | 9168 |
| `spring.datasource` | MySQL 连接 | `application.yml` |
| `spring.redis` | Redis 连接 | `application.yml` |
| `rocketmq.name-server` | RocketMQ 地址 | `application.yml` |
| `knife4j.enable` | 是否开启 API 文档 | true |
| `local.host` | 对外服务根 URL（安装包下载链、`latest.yml` 内 URL） | 需按部署环境修改 |
| `ai.chat.retrieval-base-url` | Python FAISS 检索服务 | `http://127.0.0.1:9170` |
| `ai.chat.*` | 服务端推理（OpenAI 兼容 API 或本机 llama subprocess） | 见 `application.yml` |
| `mail.inbound.*` / `mail.smtp.*` | 邮件渠道 IMAP / SMTP | 默认 `enabled: false` |

## 数据库

### 全新环境

执行 `src/main/resources/db/init.sql`（含默认管理员 **admin / Admin123**）。

### 已有库增量迁移

按顺序执行（若某脚本已执行可跳过）：

1. `db/migration_user_sync_quota.sql`
2. `db/migration_file_sync_columns.sql`
3. `db/migration_install_package.sql`
4. `db/migration_install_package_sha512.sql`
5. `db/migration_mail_channel.sql`（启用 Java 邮件渠道时）
6. `sql/chat_script.sql`（AI 客服话术表）
7. `sql/ai_cs_chat_session.sql`（AI 客服会话归档）
8. `sql/migrate_chat_script_owner_user.sql`（话术按用户隔离）

## 主要 API 路由

### 公开接口（无需登录）

| 路径 | 说明 |
|------|------|
| `GET /open/rsaPublicKey` | 登录前 RSA 公钥 |
| `POST /user/login/*`、`POST /user/register` 等 | 认证与注册 |
| `GET /open/installPackage/download/{code}` | 安装包公开下载 |
| `GET /open/installPackage/latest.yml` | 桌面端自动更新描述 |

完整排除路径见 `CommonConstant.EXCLUDE_PATH`。

### 需登录接口

| 路径前缀 | 功能 |
|----------|------|
| `/captcha`、`/sms` | 验证码、短信 |
| `/area` | 地区 |
| `/user` | 用户 |
| `/role`、`/menu` | 角色、菜单 |
| `/file` | 文件 |
| `/syncDirectory` | 同步目录 |
| `/installPackage` | 安装包管理（管理员） |
| `/sysOperationLog` | 操作日志 |
| `/chat` | AI 客服、话术、检索 |
| `/mail/suggested-reply/**` | 邮件 AI 建议回复 |

## 邮件渠道启用步骤

1. 执行 `db/migration_mail_channel.sql`
2. **收信**：`mail.inbound.enabled=true`，配置 IMAP `host` / `port` / `username` / `password`；首次启动游标对齐当前最大 UID，不处理历史邮件
3. **发信**：`mail.smtp.enabled=true`，配置 SMTP 与 `from-address`
4. 在 Knife4j 中审核并调用 `/mail/suggested-reply/send` 前请确认建议正文

## 与其他模块的关系

```
client (Electron)  ──HTTP──►  system (:9168)
                                │
                                └──HTTP──►  retrieval-service (:9170)
```

典型启动顺序：MySQL / Redis → `retrieval-service` → `system` → `client`。

详见 [`client/README.md`](../client/README.md)、[`retrieval-service/README.md`](../retrieval-service/README.md)。
