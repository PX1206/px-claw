-- ============================================
-- 云同步 (claw-system) 数据库初始化脚本
-- 数据库: claw_db
-- MySQL 8.0+
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `claw_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `claw_db`;

-- --------------------------------------------
-- 1. 用户表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_no` varchar(50) DEFAULT NULL COMMENT '用户编号',
  `username` varchar(100) NOT NULL COMMENT '账号',
  `nickname` varchar(100) DEFAULT NULL COMMENT '昵称',
  `mobile` varchar(20) DEFAULT NULL COMMENT '手机号',
  `head_img` varchar(255) DEFAULT NULL COMMENT '头像',
  `sex` tinyint DEFAULT NULL COMMENT '性别：1男 2女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `salt` varchar(50) DEFAULT NULL COMMENT '盐',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `role` varchar(20) DEFAULT NULL COMMENT '角色：admin/user',
  `status` tinyint DEFAULT 1 COMMENT '状态：0注销 1正常 2禁用 3冻结 4临时冻结',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `del_flag` tinyint(1) DEFAULT 0 COMMENT '0正常 1删除',
  `sync_quota_bytes` bigint DEFAULT NULL COMMENT '同步文件总配额（字节），NULL=默认 5GB',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- --------------------------------------------
-- 2. 角色表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '角色名称',
  `code` varchar(50) NOT NULL COMMENT '角色标识',
  `description` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT 1 COMMENT '0禁用 1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `del_flag` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- --------------------------------------------
-- 3. 菜单表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pid` bigint DEFAULT 0 COMMENT '父ID',
  `title` varchar(100) DEFAULT NULL COMMENT '菜单标题',
  `type` varchar(20) DEFAULT NULL COMMENT '菜单类型',
  `permission` varchar(100) DEFAULT NULL COMMENT '权限code',
  `component` varchar(255) DEFAULT NULL,
  `icon` varchar(100) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL COMMENT '前端路由名',
  `redirect` varchar(255) DEFAULT NULL,
  `sort` int DEFAULT 0 COMMENT '排序',
  `hidden` tinyint(1) DEFAULT 0,
  `path` varchar(255) DEFAULT NULL COMMENT '路径',
  `affix` tinyint(1) DEFAULT 0,
  `keep_alive` tinyint(1) DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `del_flag` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- --------------------------------------------
-- 4. 用户角色关联表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- --------------------------------------------
-- 5. 角色菜单关联表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联';

-- --------------------------------------------
-- 6. 地区表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `area` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `parent_id` int DEFAULT NULL,
  `initial` varchar(10) DEFAULT NULL,
  `initials` varchar(50) DEFAULT NULL,
  `pinyin` varchar(100) DEFAULT NULL,
  `extra` varchar(50) DEFAULT NULL,
  `suffix` varchar(50) DEFAULT NULL,
  `code` varchar(50) DEFAULT NULL,
  `area_code` varchar(50) DEFAULT NULL,
  `area_order` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区表';

-- --------------------------------------------
-- 7. 文件表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) NOT NULL COMMENT '文件编码',
  `name` varchar(255) DEFAULT NULL COMMENT '文件名称(不含后缀)',
  `type` int DEFAULT NULL COMMENT '1图片 2文档 3视频 4音频 5其它',
  `domain` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL COMMENT '存储路径',
  `suffix` varchar(20) DEFAULT NULL COMMENT '文件后缀',
  `source` int DEFAULT NULL,
  `size` int DEFAULT NULL COMMENT '大小KB',
  `create_by` bigint DEFAULT NULL COMMENT '创建人/所属用户',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint(1) DEFAULT 0,
  `sync_directory_id` bigint DEFAULT NULL COMMENT '所属同步目录ID',
  `relative_path` varchar(500) DEFAULT NULL COMMENT '在同步目录内的相对路径',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_create_by` (`create_by`),
  KEY `idx_sync_directory_id` (`sync_directory_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';

-- --------------------------------------------
-- 8. 同步目录表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `sync_directory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `local_path` varchar(500) NOT NULL COMMENT '本地绝对路径',
  `display_name` varchar(200) DEFAULT NULL COMMENT '显示名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint(1) DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步目录表';

-- --------------------------------------------
-- 9. 操作日志表
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `request_id` varchar(50) DEFAULT NULL,
  `user_id` varchar(50) DEFAULT NULL,
  `user_name` varchar(100) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `ip` varchar(50) DEFAULT NULL,
  `area` varchar(100) DEFAULT NULL,
  `operator` varchar(100) DEFAULT NULL,
  `path` varchar(500) DEFAULT NULL,
  `module` varchar(100) DEFAULT NULL,
  `class_name` varchar(255) DEFAULT NULL,
  `method_name` varchar(100) DEFAULT NULL,
  `request_method` varchar(20) DEFAULT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `request_body` tinyint(1) DEFAULT NULL,
  `param` text,
  `token` varchar(255) DEFAULT NULL,
  `type` int DEFAULT NULL COMMENT '0:其它 1:新增 2:修改 3:删除 等',
  `success` tinyint(1) DEFAULT NULL,
  `code` int DEFAULT NULL,
  `message` varchar(500) DEFAULT NULL,
  `exception_name` varchar(255) DEFAULT NULL,
  `exception_message` text,
  `browser_name` varchar(100) DEFAULT NULL,
  `browser_version` varchar(50) DEFAULT NULL,
  `engine_name` varchar(100) DEFAULT NULL,
  `engine_version` varchar(50) DEFAULT NULL,
  `os_name` varchar(100) DEFAULT NULL,
  `platform_name` varchar(100) DEFAULT NULL,
  `mobile` tinyint(1) DEFAULT NULL,
  `device_name` varchar(100) DEFAULT NULL,
  `device_model` varchar(100) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- --------------------------------------------
-- 10. 桌面安装包分发（管理端上传，公开链接下载）
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `install_package` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `download_code` varchar(32) NOT NULL COMMENT '公开下载随机码',
  `file_name` varchar(255) NOT NULL COMMENT '展示文件名(含后缀)',
  `relative_path` varchar(64) NOT NULL COMMENT '存储子目录(yyyy-MM-dd)',
  `suffix` varchar(32) DEFAULT NULL COMMENT '文件后缀含点',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '字节',
  `sha512` varchar(128) DEFAULT NULL COMMENT 'SHA-512 Base64，用于桌面自动更新',
  `version_label` varchar(100) DEFAULT NULL COMMENT '版本说明',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` bigint DEFAULT NULL,
  `del_flag` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_download_code` (`download_code`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安装包分发表';

-- ============================================
-- 初始数据
-- ============================================

-- 角色
INSERT INTO `role` (`id`, `name`, `code`, `description`, `status`) VALUES
(1, '管理员', 'admin', '系统管理员', 1),
(2, '普通用户', 'user', '普通用户', 1);

-- 管理员账号：admin / Admin123
-- 密码为 SHA256(Admin123 + salt)，salt 为 x7k9m2
INSERT INTO `user` (`id`, `user_no`, `username`, `nickname`, `salt`, `password`, `role`, `status`) VALUES
(1, 'U001', 'admin', '管理员', 'x7k9m2', LOWER(SHA2(CONCAT('Admin123', 'x7k9m2'), 256)), 'admin', 1);

-- 用户角色：admin 用户 -> 管理员角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 菜单数据
INSERT INTO `menu` (`id`, `pid`, `title`, `type`, `path`, `component`, `icon`, `name`, `sort`, `hidden`, `keep_alive`) VALUES
-- 文件管理
(100, 0, '文件管理', 'menu', '/files', 'file/index', 'folder', 'Files', 10, 0, 1),
(101, 100, '文件列表', 'menu', '/files/list', 'file/list', 'document', 'FileList', 1, 0, 1),
-- 系统管理
(110, 0, '系统管理', 'menu', '/system', 'Layout', 'setting', 'System', 100, 0, 1),
(111, 110, '菜单管理', 'menu', '/system/menu', 'system/menu/index', 'menu', 'MenuManage', 1, 0, 1),
(112, 110, '角色管理', 'menu', '/system/role', 'system/role/index', 'peoples', 'RoleManage', 2, 0, 1),
(113, 110, '用户管理', 'menu', '/system/user', 'system/user/index', 'user', 'UserManage', 3, 0, 1),
(114, 110, '日志管理', 'menu', '/system/log', 'system/log/index', 'form', 'LogManage', 4, 0, 1);

INSERT INTO `menu` (`id`, `pid`, `title`, `type`, `permission`, `path`, `component`, `icon`, `name`, `sort`, `hidden`, `keep_alive`) VALUES
(115, 110, '安装包管理', 'menu', 'system:installPackage', '/system/install-package', 'system/install-package/index', 'download', 'InstallPackageManage', 5, 0, 1);

-- 角色菜单：管理员拥有所有菜单，普通用户仅文件管理
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
(1, 100), (1, 101), (1, 110), (1, 111), (1, 112), (1, 113), (1, 114), (1, 115),
(2, 100), (2, 101);

-- ============================================
-- 说明
-- ============================================
-- 默认管理员：账号 admin，密码 Admin123
-- 若 file 表已存在且缺少 sync_directory_id、relative_path 列，
-- 请执行 migration_file_sync_columns.sql
-- 安装包管理：已有库请执行 migration_install_package.sql
