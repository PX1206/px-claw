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
