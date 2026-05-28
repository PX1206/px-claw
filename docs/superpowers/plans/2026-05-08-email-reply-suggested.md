# 邮件 AI 建议回复 + 人工审核发送 — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `system`（Spring Boot）模块内实现 IMAP 收信 → 现有 RAG + `AiChatInferenceService` 生成建议回复 → 持久化待审核 → 管理端 REST 查看/编辑 → 人工确认后 SMTP 发出（带 `In-Reply-To` / `References`）。

**Architecture:** 数据落 MySQL（入站快照 + 建议回复 + IMAP UID 游标）；JavaMail 处理 IMAP/SMTP；`@Scheduled` 轮询拉信；编排层直接注入现有 `RagService` 与 `AiChatInferenceService`，prompt 构造与 `ChatController.buildPrompt` 语义对齐并增加「邮件客服」语气说明；管理 API 走现有登录拦截（**不**加入 `EXCLUDE_PATH`）。

**Tech Stack:** Java 8、Spring Boot 2.7.6、MyBatis-Plus 3.3.1、`spring-boot-starter-mail`（JavaMail）、MySQL、Knife4j。

**Spec 来源:** `docs/superpowers/specs/2026-05-08-email-reply-suggested-design.md`

---

## 文件结构（新增 / 修改一览）

| 路径 | 职责 |
|------|------|
| `system/pom.xml` | 增加 `spring-boot-starter-mail`。 |
| `system/src/main/resources/db/migration_mail_channel.sql` | 三张表：`mail_inbound`、`mail_suggested_reply`、`mail_imap_checkpoint`。 |
| `system/src/main/java/com/claw/system/entity/MailInbound.java` | 入站邮件实体。 |
| `system/src/main/java/com/claw/system/entity/MailSuggestedReply.java` | 建议回复与状态。 |
| `system/src/main/java/com/claw/system/entity/MailImapCheckpoint.java` | 每文件夹 IMAP `last_uid` 游标。 |
| `system/src/main/java/com/claw/system/enums/MailSuggestedReplyStatus.java` | `PENDING_REVIEW` / `SENT` / `DISCARDED` / `FAILED`。 |
| `system/src/main/java/com/claw/system/mapper/MailInboundMapper.java` | MyBatis-Plus `BaseMapper`。 |
| `system/src/main/java/com/claw/system/mapper/MailSuggestedReplyMapper.java` | 同上。 |
| `system/src/main/java/com/claw/system/mapper/MailImapCheckpointMapper.java` | 同上。 |
| `system/src/main/java/com/claw/system/config/MailChannelProperties.java` | `@ConfigurationProperties(prefix = "mail")`：imap/smtp 开关、连接、轮询、`maxBatch`、`enabled` 过滤等。 |
| `system/src/main/java/com/claw/system/config/MailChannelConfiguration.java` | 条件装配 `JavaMailSender`、`@EnableScheduling` 仅当需要时或集中在 `SystemApplication`（见任务说明）。 |
| `system/src/main/java/com/claw/system/service/MailEmailPromptBuilder.java` | 纯静态/Bean：`(context, question) -> prompt`，与 `ChatController.buildPrompt` 一致逻辑并追加邮件场景说明（便于单测）。 |
| `system/src/main/java/com/claw/system/service/MailMimeTextExtractor.java` | `text/plain` 优先，`text/html` 降级（可用简单正则去标签或 Hutool `HtmlUtil.cleanHtmlTag`）。 |
| `system/src/main/java/com/claw/system/service/MailThreadingHeaders.java` | 保存/解析 `Message-ID`、`References` 供回复 MIME 使用（可 JSON 存 `raw_headers_json`）。 |
| `system/src/main/java/com/claw/system/service/MailImapIngestService.java` | 连接 IMAP、按 UID 增量拉取、`MailMimeTextExtractor`、写 `mail_inbound`、调编排。 |
| `system/src/main/java/com/claw/system/service/MailSuggestedReplyOrchestrator.java` | 幂等：`message_id` 已存在则跳过；否则 `ragService.search` + `aiChatInferenceService.generate` + insert `mail_suggested_reply`。 |
| `system/src/main/java/com/claw/system/service/MailSendService.java` | `JavaMailSender` 构造 `MimeMessage`，设置 `In-Reply-To`、`References`、发件人、收件人（原 `From`）。 |
| `system/src/main/java/com/claw/system/service/MailSuggestedReplyAdminService.java` | 分页列表、详情、更新正文、discard、send（事务内改状态 + 调 `MailSendService`）。 |
| `system/src/main/java/com/claw/system/schedule/MailIngestScheduler.java` | `@Scheduled(fixedDelayString = "${mail.inbound.poll-interval-ms:60000}")` 调用 `MailImapIngestService`。 |
| `system/src/main/java/com/claw/system/controller/MailSuggestedReplyController.java` | `/mail/suggested-reply/**` REST，风格对齐 `RoleController`（`BaseController`、`@Module`、`ApiOperation`）。 |
| `system/src/main/java/com/claw/system/param/MailSuggestedReplyUpdateParam.java` | 更新正文。 |
| `system/src/main/java/com/claw/system/param/MailSuggestedReplyPageParam.java` | 分页查询（继承或对齐现有 `BasePageParam`）。 |
| `system/src/main/java/com/claw/system/vo/MailSuggestedReplyListVO.java` / `MailSuggestedReplyDetailVO.java` | 返回前端字段。 |
| `system/src/main/resources/application.yml` | **勿提交真实密码**；仅增加占位键与 `mail.inbound.enabled: false` 默认关闭。 |
| `system/src/test/java/com/claw/system/service/MailEmailPromptBuilderTest.java` | prompt 拼接单测。 |
| `system/src/test/java/com/claw/system/service/MailMimeTextExtractorTest.java` | plain/html 单测。 |
| `system/src/test/java/com/claw/system/service/MailSuggestedReplyOrchestratorTest.java` | Mock `RagService`、`AiChatInferenceService`、Mapper（或 `@MybatisPlusTest` 若引入）。 |
| `system/README.md` 或根 `README.md` | 增加「执行 `migration_mail_channel.sql`」与 `mail.*` 配置说明（若根 README 已描述迁移流程则只加一行链接）。 |

---

### Task 1: 依赖与默认配置占位

**Files:**
- Modify: `system/pom.xml`
- Modify: `system/src/main/resources/application.yml`

- [ ] **Step 1: 在 `pom.xml` 的 `<dependencies>` 内增加**

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
```

- [ ] **Step 2: 在 `application.yml` 增加（值保持默认关闭，密码用环境变量占位说明）**

```yaml
mail:
  inbound:
    enabled: false
    host: ${MAIL_IMAP_HOST:imap.example.com}
    port: ${MAIL_IMAP_PORT:993}
    username: ${MAIL_IMAP_USER:}
    password: ${MAIL_IMAP_PASSWORD:}
    folder: INBOX
    ssl: true
    poll-interval-ms: 60000
    max-batch-per-poll: 20
  smtp:
    host: ${MAIL_SMTP_HOST:smtp.example.com}
    port: ${MAIL_SMTP_PORT:587}
    username: ${MAIL_SMTP_USER:}
    password: ${MAIL_SMTP_PASSWORD:}
    ssl: false
    starttls: true
    from-address: ${MAIL_FROM:noreply@example.com}
```

- [ ] **Step 3: Commit**

```bash
git add system/pom.xml system/src/main/resources/application.yml
git commit -m "chore(mail): add spring-mail dependency and mail.* config placeholders"
```

---

### Task 2: 数据库迁移脚本

**Files:**
- Create: `system/src/main/resources/db/migration_mail_channel.sql`

- [ ] **Step 1: 写入完整 DDL（工程师按环境执行，勿自动 Flyway 除非项目已统一 Flyway）**

```sql
-- 邮件渠道：入站 + 建议回复 + IMAP 游标（与 docs/superpowers/specs 一致）
CREATE TABLE IF NOT EXISTS `mail_inbound` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `message_id` varchar(255) NOT NULL COMMENT 'RFC Message-ID，幂等键',
    `imap_uid` bigint DEFAULT NULL,
    `folder` varchar(128) DEFAULT 'INBOX',
    `from_addr` varchar(512) DEFAULT NULL,
    `to_addr` varchar(512) DEFAULT NULL,
    `subject` varchar(1024) DEFAULT NULL,
    `body_text` mediumtext COMMENT '用于 RAG/推理的纯文本',
    `received_at` datetime DEFAULT NULL,
    `raw_headers_json` text COMMENT 'JSON：Message-ID、References、In-Reply-To 等',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_mail_inbound_message_id` (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `mail_suggested_reply` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `inbound_id` bigint NOT NULL,
    `suggested_body` mediumtext,
    `retrieved_context` mediumtext,
    `status` varchar(32) NOT NULL COMMENT 'PENDING_REVIEW|SENT|DISCARDED|FAILED',
    `reviewed_by_user_id` bigint DEFAULT NULL,
    `reviewed_at` datetime DEFAULT NULL,
    `sent_at` datetime DEFAULT NULL,
    `smtp_message_id` varchar(512) DEFAULT NULL,
    `last_error` varchar(1024) DEFAULT NULL,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_mail_suggested_inbound` (`inbound_id`),
    KEY `idx_mail_suggested_status` (`status`),
    CONSTRAINT `fk_mail_suggested_inbound` FOREIGN KEY (`inbound_id`) REFERENCES `mail_inbound` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `mail_imap_checkpoint` (
    `id` int NOT NULL,
    `folder` varchar(128) NOT NULL,
    `last_uid` bigint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `mail_imap_checkpoint` (`id`, `folder`, `last_uid`)
VALUES (1, 'INBOX', 0)
ON DUPLICATE KEY UPDATE `folder` = VALUES(`folder`);
```

- [ ] **Step 2: Commit**

```bash
git add system/src/main/resources/db/migration_mail_channel.sql
git commit -m "db: add mail_inbound, mail_suggested_reply, mail_imap_checkpoint"
```

---

### Task 3: 枚举与实体、Mapper

**Files:**
- Create: `system/src/main/java/com/claw/system/enums/MailSuggestedReplyStatus.java`
- Create: `system/src/main/java/com/claw/system/entity/MailInbound.java`
- Create: `system/src/main/java/com/claw/system/entity/MailSuggestedReply.java`
- Create: `system/src/main/java/com/claw/system/entity/MailImapCheckpoint.java`
- Create: `system/src/main/java/com/claw/system/mapper/MailInboundMapper.java`
- Create: `system/src/main/java/com/claw/system/mapper/MailSuggestedReplyMapper.java`
- Create: `system/src/main/java/com/claw/system/mapper/MailImapCheckpointMapper.java`

- [ ] **Step 1: 枚举**

```java
package com.claw.system.enums;

public enum MailSuggestedReplyStatus {
    PENDING_REVIEW,
    SENT,
    DISCARDED,
    FAILED
}
```

- [ ] **Step 2: 实体字段与 `AiCsChatSession` 一致风格 — `MailInbound` 含 `@TableName("mail_inbound")`、`@TableId(type = IdType.AUTO)`，字段名与 DDL 驼峰映射（MyBatis-Plus 默认下划线）。`MailSuggestedReply` 中 `status` 用 `String` 存枚举名或加 `TypeHandler`（首轮可用 `String` + `getStatus()` 封装）。**

- [ ] **Step 3: 三个 `Mapper` 接口继承 `BaseMapper<Entity>` + `@Mapper`。**

- [ ] **Step 4: Commit**

```bash
git add system/src/main/java/com/claw/system/enums/MailSuggestedReplyStatus.java \
  system/src/main/java/com/claw/system/entity/MailInbound.java \
  system/src/main/java/com/claw/system/entity/MailSuggestedReply.java \
  system/src/main/java/com/claw/system/entity/MailImapCheckpoint.java \
  system/src/main/java/com/claw/system/mapper/MailInboundMapper.java \
  system/src/main/java/com/claw/system/mapper/MailSuggestedReplyMapper.java \
  system/src/main/java/com/claw/system/mapper/MailImapCheckpointMapper.java
git commit -m "feat(mail): add entities and mappers for mail channel"
```

---

### Task 4: `MailChannelProperties` 与条件 `JavaMailSender`

**Files:**
- Create: `system/src/main/java/com/claw/system/config/MailChannelProperties.java`
- Create: `system/src/main/java/com/claw/system/config/MailChannelConfiguration.java`

- [ ] **Step 1: `MailChannelProperties` 嵌套类 `Inbound`、`Smtp`，字段与 `application.yml` 对齐；主类 `@ConfigurationProperties(prefix = "mail")` + `@Validated` + `@Component`（或 `@EnableConfigurationProperties`）。**

- [ ] **Step 2: `MailChannelConfiguration` 中 `@Bean @ConditionalOnProperty(prefix = "mail.inbound", name = "enabled", havingValue = "true")` 定义 `JavaMailSender`（`JavaMailSenderImpl` 设置 host/port/username/password/properties：`mail.smtp.auth`、`mail.smtp.starttls.enable`）。**

- [ ] **Step 3: Commit**

```bash
git add system/src/main/java/com/claw/system/config/MailChannelProperties.java \
  system/src/main/java/com/claw/system/config/MailChannelConfiguration.java
git commit -m "feat(mail): configuration properties and conditional JavaMailSender"
```

---

### Task 5: `MailEmailPromptBuilder` + 单元测试（TDD）

**Files:**
- Create: `system/src/main/java/com/claw/system/service/MailEmailPromptBuilder.java`
- Create: `system/src/test/java/com/claw/system/service/MailEmailPromptBuilderTest.java`

- [ ] **Step 1: 写失败测试**

```java
package com.claw.system.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MailEmailPromptBuilderTest {

    @Test
    void buildsPromptContainingContextQuestionAndEmailTone() {
        MailEmailPromptBuilder b = new MailEmailPromptBuilder();
        String p = b.build("ctx-line", "用户问什么");
        assertTrue(p.contains("ctx-line"));
        assertTrue(p.contains("用户问什么"));
        assertTrue(p.contains("邮件") || p.contains("客服"));
    }
}
```

- [ ] **Step 2: 运行测试（应 FAIL：类不存在）**

Run: `mvn -pl system -am test -Dtest=MailEmailPromptBuilderTest`

Expected: 编译失败或 `ClassNotFoundException` / 找不到符号。

- [ ] **Step 3: 最小实现（与 `ChatController.buildPrompt` 对齐 + 邮件说明）**

```java
package com.claw.system.service;

import org.springframework.stereotype.Component;

@Component
public class MailEmailPromptBuilder {

    public String build(String context, String question) {
        return "你是一个客服助手，只能通过邮件回复客户；只能根据以下资料回答。\n"
                + "语气正式、简洁；若资料中没有相关信息，请说「暂时没有相关信息」。\n\n"
                + "【资料】\n" + context + "\n\n"
                + "【客户问题】\n" + question;
    }
}
```

- [ ] **Step 4: 运行测试 — 应 PASS**

Run: `mvn -pl system -am test -Dtest=MailEmailPromptBuilderTest`

Expected: `BUILD SUCCESS`，测试通过。

- [ ] **Step 5: Commit**

```bash
git add system/src/main/java/com/claw/system/service/MailEmailPromptBuilder.java \
  system/src/test/java/com/claw/system/service/MailEmailPromptBuilderTest.java
git commit -m "feat(mail): MailEmailPromptBuilder with unit test"
```

---

### Task 6: `MailMimeTextExtractor` + 单元测试

**Files:**
- Create: `system/src/main/java/com/claw/system/service/MailMimeTextExtractor.java`
- Create: `system/src/test/java/com/claw/system/service/MailMimeTextExtractorTest.java`

- [ ] **Step 1: 失败测试 — plain 优先**

```java
package com.claw.system.service;

import org.junit.jupiter.api.Test;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class MailMimeTextExtractorTest {

    @Test
    void prefersPlainText() throws Exception {
        Session s = Session.getDefaultInstance(new Properties());
        MimeMessage msg = new MimeMessage(s);
        MimeMultipart mp = new MimeMultipart();
        MimeBodyPart plain = new MimeBodyPart();
        plain.setText("hello plain", "UTF-8");
        MimeBodyPart html = new MimeBodyPart();
        html.setContent("<p>hello html</p>", "text/html; charset=UTF-8");
        mp.addBodyPart(plain);
        mp.addBodyPart(html);
        msg.setContent(mp);
        MailMimeTextExtractor ex = new MailMimeTextExtractor();
        assertEquals("hello plain", ex.extractBodyText(msg).trim());
    }
}
```

- [ ] **Step 2: 运行测试 — 应 FAIL**

Run: `mvn -pl system -am test -Dtest=MailMimeTextExtractorTest`

- [ ] **Step 3: 实现 `extractBodyText(MimeMessage)`：递归 `multipart/*`，收集 `text/plain`；若无则 `text/html` strip 标签（可用正则 `replaceAll("<[^>]+>", "")` 或 Hutool）。**

- [ ] **Step 4: 测试 PASS 后 Commit**

```bash
git add system/src/main/java/com/claw/system/service/MailMimeTextExtractor.java \
  system/src/test/java/com/claw/system/service/MailMimeTextExtractorTest.java
git commit -m "feat(mail): MIME body text extraction"
```

---

### Task 7: `MailSuggestedReplyOrchestrator` + Mock 单测

**Files:**
- Create: `system/src/main/java/com/claw/system/service/MailSuggestedReplyOrchestrator.java`
- Create: `system/src/test/java/com/claw/system/service/MailSuggestedReplyOrchestratorTest.java`

- [ ] **Step 1: 单测 — 当 `MailInboundMapper.selectOne` 已存在同 `message_id` 时，不调用 `RagService`**

使用 `@ExtendWith(MockitoExtension.class)`，`@Mock MailInboundMapper`、`@Mock MailSuggestedReplyMapper`、`@Mock RagService`、`@Mock AiChatInferenceService`。

- [ ] **Step 2: 实现 `orchestrateAfterPersistInbound(MailInbound inbound)`：**
  - 若已存在 `mail_suggested_reply` 关联同 `inbound_id` 且状态非 `DISCARDED`（或按 spec：同一 `message_id` 仅一条）则 return。
  - `context = ragService.search(inbound.getBodyText())`（或 subject+body 拼接，计划中用 `body_text`）。
  - `answer = aiChatInferenceService.generate(mailEmailPromptBuilder.build(context, question))`。
  - `insert` `MailSuggestedReply`：`PENDING_REVIEW`、`retrievedContext=context`。

- [ ] **Step 3: 运行 `mvn -pl system -am test -Dtest=MailSuggestedReplyOrchestratorTest` — PASS**

- [ ] **Step 4: Commit**

```bash
git add system/src/main/java/com/claw/system/service/MailSuggestedReplyOrchestrator.java \
  system/src/test/java/com/claw/system/service/MailSuggestedReplyOrchestratorTest.java
git commit -m "feat(mail): orchestrate RAG + inference into suggested reply"
```

---

### Task 8: `MailImapIngestService`（IMAP 拉取 + 入库 + 编排）

**Files:**
- Create: `system/src/main/java/com/claw/system/service/MailImapIngestService.java`

- [ ] **Step 1: 使用 `javax.mail.Store`：`properties` 设置 `mail.store.protocol=imaps`（若 ssl）、`mail.imaps.host` 等，与 `MailChannelProperties.Inbound` 一致。**

- [ ] **Step 2: 读取 `mail_imap_checkpoint` 行 `id=1`，`Folder.getMessagesByUID(lastUid+1, UIDNEXT-1)` 或按文件夹枚举新 UID（JavaMail API：`folder.getMessages(start, end)` 若未用 UID，**推荐** `IMAPFolder` + `UID`：Spring Boot 2.7 使用 `com.sun.mail.imap.IMAPFolder`）。

- [ ] **Step 3: 每封邮件：解析 `Message-ID`（无则 `hash(from+subject+received)` 合成稳定 id 前缀 `local-`），`MailMimeTextExtractor.extractBodyText`，insert `mail_inbound`，再调 `mailSuggestedReplyOrchestrator.orchestrateAfterPersistInbound`。**

- [ ] **Step 4: 更新 `last_uid`；整批不超过 `max-batch-per-poll`。**

- [ ] **Step 5: 可选集成测试：使用内嵌 GreenMail（若不想加依赖，首轮可仅手动联调 + 日志）；计划中最低要求为 **手动联调步骤** 记在 Task 12。**

- [ ] **Step 6: Commit**

```bash
git add system/src/main/java/com/claw/system/service/MailImapIngestService.java
git commit -m "feat(mail): IMAP ingest and persist inbound mail"
```

---

### Task 9: `MailSendService`（SMTP + 线程头）

**Files:**
- Create: `system/src/main/java/com/claw/system/service/MailSendService.java`

- [ ] **Step 1: 方法 `sendReply(MailInbound inbound, String editedBody, MailSuggestedReply reply)`：**
  - `MimeMessageHelper` 或 `mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(inbound.getFromAddr()))`。
  - `subject`：`Re: ` + 原 subject（若无 `Re:` 前缀则追加）。
  - 从 `raw_headers_json` 读原始 `Message-ID`，设置 `In-Reply-To`、`References`（RFC 5322 格式）。

- [ ] **Step 2: `javaMailSender.send(mimeMessage)`；捕获异常写入 `last_error` 由调用方处理状态 `FAILED`。**

- [ ] **Step 3: Commit**

```bash
git add system/src/main/java/com/claw/system/service/MailSendService.java
git commit -m "feat(mail): SMTP send with threading headers"
```

---

### Task 10: `MailSuggestedReplyAdminService` + `MailSuggestedReplyController`

**Files:**
- Create: `system/src/main/java/com/claw/system/service/MailSuggestedReplyAdminService.java`
- Create: `system/src/main/java/com/claw/system/controller/MailSuggestedReplyController.java`
- Create: `system/src/main/java/com/claw/system/param/MailSuggestedReplyUpdateParam.java`
- Create: `system/src/main/java/com/claw/system/param/MailSuggestedReplyPageParam.java`
- Create: `system/src/main/java/com/claw/system/vo/MailSuggestedReplyListVO.java`
- Create: `system/src/main/java/com/claw/system/vo/MailSuggestedReplyDetailVO.java`

- [ ] **Step 1: `MailSuggestedReplyPageParam` 继承 `com.claw.common.pagination.BasePageParam`（与项目一致）。**

- [ ] **Step 2: `AdminService` 方法：`pageList`、`getDetail(id)`、`updateBody(id, body)`、`discard(id)`、`send(id, userId)`：**
  - `send`：仅当 `PENDING_REVIEW` 或 `FAILED`（可配置是否允许重试）；`@Transactional` 内调用 `MailSendService`，成功则 `SENT` + `sent_at`；失败 `FAILED` + `last_error`。

- [ ] **Step 3: `MailSuggestedReplyController`：`@RequestMapping("/mail/suggested-reply")`，路径**不要**加入 `CommonConstant.EXCLUDE_PATH`。使用 `BaseController` 取当前登录用户 id（与 `UserController` 相同工具方法，若项目有 `LoginUtil` 则复用）。**

- [ ] **Step 4: Knife4j 注解补齐，便于管理端调试。**

- [ ] **Step 5: 编译**

Run: `mvn -pl system -am compile -q`

Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add system/src/main/java/com/claw/system/service/MailSuggestedReplyAdminService.java \
  system/src/main/java/com/claw/system/controller/MailSuggestedReplyController.java \
  system/src/main/java/com/claw/system/param/MailSuggestedReplyUpdateParam.java \
  system/src/main/java/com/claw/system/param/MailSuggestedReplyPageParam.java \
  system/src/main/java/com/claw/system/vo/MailSuggestedReplyListVO.java \
  system/src/main/java/com/claw/system/vo/MailSuggestedReplyDetailVO.java
git commit -m "feat(mail): admin REST for suggested reply review and send"
```

---

### Task 11: `MailIngestScheduler` + 启用调度

**Files:**
- Create: `system/src/main/java/com/claw/system/schedule/MailIngestScheduler.java`
- Modify: `system/src/main/java/com/claw/system/SystemApplication.java`

- [ ] **Step 1: `MailIngestScheduler` 类上 `@ConditionalOnProperty(prefix = "mail.inbound", name = "enabled", havingValue = "true")`，方法 `@Scheduled(fixedDelayString = "${mail.inbound.poll-interval-ms:60000}")`，调用 `mailImapIngestService.pollOnce()`。**

- [ ] **Step 2: 在 `SystemApplication` 增加 `@EnableScheduling`（全局启用；调度 Bean 仍由条件注解控制）。**

```java
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SystemApplication {
```

- [ ] **Step 3: Commit**

```bash
git add system/src/main/java/com/claw/system/schedule/MailIngestScheduler.java \
  system/src/main/java/com/claw/system/SystemApplication.java
git commit -m "feat(mail): scheduled IMAP poll when mail.inbound.enabled=true"
```

---

### Task 12: 文档与手动验收

**Files:**
- Modify: `system/README.md`（或仓库根 `README.md` 中迁移小节）

- [ ] **Step 1: 记录：执行 `migration_mail_channel.sql`；设置 `mail.inbound.enabled=true` 前需填 IMAP/SMTP；管理端通过 Knife4j `POST /mail/suggested-reply/...`（以最终实现路径为准）审核发信。**

- [ ] **Step 2: 手动验收清单**
  1. DB 迁移 applied。  
  2. 自建测试邮箱发送一封到收件箱 → 调度触发后 `mail_inbound` 有行、`mail_suggested_reply` 为 `PENDING_REVIEW`。  
  3. Knife4j 调发送接口 → 原发件人收到回复，邮件线程在 Thunderbird/Outlook 中正确嵌套。  
  4. 将 `enabled=false` → 确认不再轮询（日志无 IMAP 连接）。

- [ ] **Step 3: Commit**

```bash
git add system/README.md
git commit -m "docs(mail): migration and manual verification for mail channel"
```

---

## 计划自检（对照 Spec）

| Spec 章节 | 覆盖任务 |
|-----------|----------|
| IMAP 收信 + 轮询 | Task 8、11 |
| RAG + 推理 + 建议持久化 | Task 5、7、8 |
| 人工审核后 SMTP | Task 9、10 |
| 线程头 | Task 8（存 raw headers）、9 |
| 配置与安全（环境变量、默认关闭） | Task 1、4 |
| 管理 API + 登录 | Task 10 |
| 非目标微信 | 无任务（明确不实现） |

**占位符扫描：** 本计划不含 TBD/TODO 式步骤；具体 REST 路径前缀以 `/mail/suggested-reply` 为基准，若与现有路由冲突可改为 `/admin/mail/...`，但须在全任务中统一修改。

**类型一致性：** `MailSuggestedReplyStatus` 与 DB `varchar(32)`、实体 `status` 字段须在 Task 3 与 Task 7–10 中统一（枚举 `.name()` 存库）。

---

## 执行交接

**计划已保存到** `docs/superpowers/plans/2026-05-08-email-reply-suggested.md`。

**两种执行方式：**

1. **Subagent-Driven（推荐）** — 每个 Task 派生子 Agent，任务之间人工快速过目，迭代快。  
2. **Inline Execution** — 本会话用 executing-plans，按批次带检查点连续做。

**你想用哪一种？** 回复 `1` 或 `2` 即可；若自行开发，可直接按本文 checkbox 顺序实现，不必经过子 Agent。
