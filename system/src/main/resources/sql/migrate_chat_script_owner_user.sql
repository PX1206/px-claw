-- 已有库升级：为 chat_script 增加归属用户并回填（执行一次）
-- 回填规则：优先 import_user_id，否则置为 1（请按环境自行调整）
ALTER TABLE chat_script
  ADD COLUMN owner_user_id BIGINT NULL COMMENT '话术归属用户（隔离）' AFTER id;
UPDATE chat_script SET owner_user_id = COALESCE(import_user_id, 1) WHERE owner_user_id IS NULL;
ALTER TABLE chat_script
  MODIFY owner_user_id BIGINT NOT NULL COMMENT '话术归属用户（隔离）';
CREATE INDEX idx_chat_script_owner ON chat_script (owner_user_id);
