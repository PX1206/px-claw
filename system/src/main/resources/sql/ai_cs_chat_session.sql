-- AI 客服会话与消息归档（Electron 快照同步；供运营/排查分析）
-- 在业务库中手工执行一次即可。

CREATE TABLE IF NOT EXISTS ai_cs_chat_session (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键（服务端）',
  user_id BIGINT NOT NULL COMMENT '登录用户 ID',
  client_session_id VARCHAR(80) NOT NULL COMMENT '桌面端会话 id（如 c_xxx）',
  title VARCHAR(512) DEFAULT NULL COMMENT '会话标题',
  provider_id VARCHAR(128) DEFAULT NULL COMMENT '选用的模型 providerId',
  client_updated_at BIGINT DEFAULT NULL COMMENT '客户端 updatedAt(ms)',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_cs_sess_user_client (user_id, client_session_id),
  KEY idx_ai_cs_sess_user_ut (user_id, update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI客服会话（归档）';

CREATE TABLE IF NOT EXISTS ai_cs_chat_message (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  session_id BIGINT NOT NULL COMMENT 'ai_cs_chat_session.id',
  role VARCHAR(32) NOT NULL COMMENT 'user / assistant',
  content MEDIUMTEXT NOT NULL COMMENT '消息正文',
  rag_context MEDIUMTEXT DEFAULT NULL COMMENT '用户轮次检索到的知识片段（可为空）',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '同会话内序号',
  created_at_ms BIGINT DEFAULT NULL COMMENT '客户端 createdAt(ms)',
  create_time DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_ai_cs_msg_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI客服消息（归档）';
