-- ============================================
-- 迁移脚本：若 file 表已存在但缺少同步相关列，请执行
-- 用于从旧版本升级
-- ============================================

USE `claw_db`;

-- 添加 sync_directory_id 列（若已存在会报错，可忽略）
ALTER TABLE `file` ADD COLUMN `sync_directory_id` bigint DEFAULT NULL COMMENT '所属同步目录ID' AFTER `del_flag`;

-- 添加 relative_path 列
ALTER TABLE `file` ADD COLUMN `relative_path` varchar(500) DEFAULT NULL COMMENT '在同步目录内的相对路径' AFTER `sync_directory_id`;

-- 添加索引
ALTER TABLE `file` ADD INDEX `idx_sync_directory_id` (`sync_directory_id`);
