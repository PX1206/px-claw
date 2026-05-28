-- AI 客服话术（Excel 导入 + 话术管理）
-- 在业务库 claw_db 中手工执行一次即可（或自行集成迁移工具）。
CREATE TABLE IF NOT EXISTS chat_script (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  owner_user_id BIGINT NOT NULL COMMENT '话术归属用户（隔离）',
  question VARCHAR(2048) NOT NULL DEFAULT '' COMMENT '问题列',
  script_text MEDIUMTEXT NOT NULL COMMENT '话术列',
  supplement VARCHAR(1024) DEFAULT NULL COMMENT '补充（可选）',
  import_time DATETIME NOT NULL COMMENT '导入时间（同批导入共用）',
  import_user_id BIGINT DEFAULT NULL COMMENT '导入用户ID',
  import_username VARCHAR(128) DEFAULT NULL COMMENT '导入用户展示名',
  import_batch_id VARCHAR(36) DEFAULT NULL COMMENT '同一Excel批次',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_chat_script_owner (owner_user_id),
  KEY idx_chat_script_import_batch (import_batch_id),
  KEY idx_chat_script_import_time (import_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI客服话术表';
