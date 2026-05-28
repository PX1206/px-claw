-- 已有库增量：安装包管理菜单 + 表 + 管理员角色菜单
-- 在 claw_db 下执行

CREATE TABLE IF NOT EXISTS `install_package` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `download_code` varchar(32) NOT NULL COMMENT '公开下载随机码',
  `file_name` varchar(255) NOT NULL COMMENT '展示文件名(含后缀)',
  `relative_path` varchar(64) NOT NULL COMMENT '存储子目录(yyyy-MM-dd)',
  `suffix` varchar(32) DEFAULT NULL COMMENT '文件后缀含点',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '字节',
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

INSERT INTO `menu` (`id`, `pid`, `title`, `type`, `permission`, `path`, `component`, `icon`, `name`, `sort`, `hidden`, `keep_alive`)
SELECT 115, 110, '安装包管理', 'menu', 'system:installPackage', '/system/install-package', 'system/install-package/index', 'download', 'InstallPackageManage', 5, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM `menu` WHERE `id` = 115);

INSERT INTO `role_menu` (`role_id`, `menu_id`)
SELECT 1, 115
WHERE NOT EXISTS (SELECT 1 FROM `role_menu` WHERE `role_id` = 1 AND `menu_id` = 115);
