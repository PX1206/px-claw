# 邮件渠道：AI 建议回复 + 人工审核发送 — 设计说明

**日期：** 2026-05-08  
**状态：** 草案已定稿待评审  
**关联决策：** 方案 **A**（Java / Spring 与现有 `system` 模块内聚）；发送策略 **2**（仅生成建议回复，**人工确认后再通过 SMTP 发出**）。微信渠道明确 **不在本轮范围**。

---

## 1. 背景与目标

### 1.1 现状

- 桌面端与后端已具备 **AI 客服** 能力：知识库检索（RAG）、服务端或客户端配置的模型推理（`ChatController`、`RagService`、`AiChatInferenceService` 等）。
- **尚无**与电子邮件收发的集成。

### 1.2 本轮目标

1. 从指定邮箱通过 **IMAP**（或后续扩展：厂商 API）**拉取**新邮件。
2. 将邮件正文规范为查询文本，走**现有** RAG + 推理链路，生成 **建议回复**。
3. **不自动对外发信**：建议回复进入 **待审核队列**；运营/客服在管理界面 **编辑（可选）并确认** 后，由服务端通过 **SMTP** 发送。
4. 回复邮件需尽量 **挂到原线程**（`In-Reply-To`、`References`），避免打乱会话。

### 1.3 非目标（本轮不做）

- 个人微信 / 企业微信 / 公众号等任何微信侧集成。
- 全自动无人值守发信（策略 1）；若未来需要，可通过配置开关扩展，**本 spec 不展开**。
- 复杂工单系统（SLA、多级审批）；本设计仅 **单级「待审核 → 已发送」**。

---

## 2. 架构概览

在 **claw-service（Spring Boot）** 内新增「邮件连接器 + 审核队列」能力，与现有领域服务同进程部署。

```text
[IMAP 轮询/IDLE] → 解析 MIME → 幂等去重
       → 抽取 question → RagService + AiChatInferenceService
       → 持久化「建议回复」草稿（状态：PENDING_REVIEW）
[管理端 UI] → 列表/详情/编辑 → 用户点击「发送」
       → SMTP 发送 → 状态 SENT；失败可重试/标记 FAILED
```

**与方案 A 一致**：不引入独立 Worker 进程；若日后邮件量或隔离要求上升，可将连接器拆出为独立服务，**对外仍通过内部 API 或消息队列**，本 spec 中的数据模型与状态机可复用。

---

## 3. 核心组件

| 组件 | 职责 |
|------|------|
| **MailIngestScheduler** | 定时触发（可配置 cron/fixedDelay）；调用 IMAP 拉取新 UID / Message-ID。 |
| **MailMessageNormalizer** | 解析 `From`/`To`/`Subject`/正文；优先 `text/plain`，`text/html` 可降级为简单 strip 或依赖库转纯文本。 |
| **IngestionDeduper** | 以 **Message-ID**（无则稳定哈希）为幂等键；已处理则跳过。 |
| **SuggestedReplyOrchestrator** | 组装 RAG 上下文与 prompt（可含「邮件客服」语气约束），调用现有推理；写入审核表。 |
| **MailSendService** | 仅在审核通过后调用；构造 MIME、`In-Reply-To`、`References`、SMTP 发送。 |
| **Admin API** | 待审核列表、详情、更新建议正文、提交发送、（可选）丢弃。 |

**鉴权：** 管理接口必须走现有后台登录与权限（例如仅管理员或具备「邮件客服」角色）。具体注解与角色名在实现计划中落到代码常量。

---

## 4. 数据模型（逻辑）

以下为逻辑字段，实现时可映射为单表或少量表。

**`mail_inbound`（入站快照，可选与审核表合并）**

- `id`
- `message_id`（唯一）
- `imap_uid` / `folder`（便于调试）
- `from_addr`, `to_addr`, `subject`
- `body_text`（用于推理的纯文本）
- `received_at`
- `raw_headers`（JSON 或截断存储，用于构造回复线程）

**`mail_suggested_reply`（审核与发送）**

- `id`
- `inbound_id`（FK）
- `suggested_body`（AI 生成，可人工改）
- `retrieved_context`（可选，便于审计，与 `ChatReplyVo` 对齐）
- `status`：`PENDING_REVIEW` | `SENT` | `DISCARDED` | `FAILED`
- `reviewed_by_user_id`, `reviewed_at`
- `sent_at`, `smtp_message_id`（若可取得）
- `last_error`（发送失败时）

**约束：** `message_id` 唯一；同一入站邮件只对应一条有效 `PENDING_REVIEW`/`SENT` 建议（实现可用唯一索引或状态机校验）。

---

## 5. 配置（application / 环境变量）

**建议前缀：** `mail.inbound.*`、`mail.smtp.*`（具体 key 在实现计划中列举）。

至少包括：

- IMAP：host、port、username、password（或 OAuth 占位，首轮可仅密码）、folder（默认 `INBOX`）、轮询间隔、`useSsl`。
- SMTP：host、port、username、password、`useSsl`、**发件人地址**（必须与账号或域策略一致）。
- 安全：**禁止**将密码写入仓库；生产使用环境变量或密钥管理服务。
- **过滤规则（可选首轮简化）：** 仅处理发往 `support@example.com` 的邮件，或仅处理主题包含某前缀；未匹配则不入队。

---

## 6. 错误处理与运维

- **IMAP 失败：** 记录日志，下次调度重试；不丢已见 UID 游标（持久化 `last_uid` 或类似）。
- **推理失败：** 可标记为 `FAILED` 子状态或单独 `INBOUND_ERROR`；不入「待审核」或入队但标明「生成失败」，由实现计划二选一（推荐：记录失败原因，管理端可见）。
- **SMTP 失败：** `FAILED` + `last_error`，支持人工在管理端 **重试发送**（仍用当前审核后的正文）。
- **限流：** 单次调度最多处理 N 封新邮件（可配置），防止异常风暴。

---

## 7. 测试策略

- **单元测试：** MIME 解析、线程头构造、`In-Reply-To`/`References` 拼接、幂等键冲突。
- **集成测试：** Testcontainers 或嵌入式 GreenMail（若引入）对 IMAP/SMTP 冒烟；或对 `MailSendService` 使用 mock transport。
- **契约：** 管理端 REST 的 JSON 结构与状态迁移。

---

## 8. 管理端 UI

**最小可用：**

- 列表：时间、发件人、主题、状态。
- 详情：原文摘要、建议回复（可编辑）、RAG 片段（只读，可选）、按钮「发送」「丢弃」。

**放置位置：** 现有管理后台（若已有 Vue/React 管理端则新增菜单）；若无统一后台，首轮可用 **Knife4j 调试 + 简易页面** 过渡——**实现计划中必须明确落地路径**，避免长期只有裸 API。

---

## 9. 风险与合规

- **误发风险：** 策略 2 要求人工最终点击发送，仍在 UI 层提示「请核对收件人与内容」。
- **垃圾邮件：** 建议回复应简短、专业；域名 SPF/DKIM/DMARC 由运维在邮件域侧配置，**超出本应用代码范围**。
- **隐私：** 邮件内容可能含 PII；数据库访问权限与日志脱敏遵循现有系统规范。

---

## 10. 与后续微信扩展的关系

微信不在本轮范围。日后若接入 **企业微信** 等官方渠道，可复用同一 **「建议回复 + 人工审核」** 状态机，仅替换「入站/出站」适配器，**不强制**在本 spec 中预留接口类型定义；实现时保持 `Orchestrator` 与「渠道」边界清晰即可。

---

## 11. Spec 自检记录

- **占位符：** 无 TBD；管理 UI 落地路径在 §8 标明「实现计划必须明确」。
- **一致性：** 方案 A + 人工发送与全文一致。
- **范围：** 单 spec 可支撑一个实现计划；微信已显式排除。
- **歧义：** 「推理失败是否入队」在 §6 给出推荐（可见失败），具体枚举在实现计划定一条默认策略。

---

## 12. 审批

- [ ] 产品/负责人已阅读并同意本文档
- [ ] 可进入 `writing-plans` 生成实施任务列表
