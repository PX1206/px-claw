/*
 Navicat Premium Data Transfer

 Source Server         : qyzj_insurance
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : 127.0.0.1:3306
 Source Schema         : claw_db

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 25/03/2026 13:52:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for area
-- ----------------------------
DROP TABLE IF EXISTS `area`;
CREATE TABLE `area`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `parent_id` int NULL DEFAULT NULL,
  `initial` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `initials` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `pinyin` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `extra` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `suffix` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `area_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `area_order` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '地区表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of area
-- ----------------------------

-- ----------------------------
-- Table structure for file
-- ----------------------------
DROP TABLE IF EXISTS `file`;
CREATE TABLE `file`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件编码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称(不含后缀)',
  `type` int NULL DEFAULT NULL COMMENT '1图片 2文档 3视频 4音频 5其它',
  `domain` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储路径',
  `suffix` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件后缀',
  `source` int NULL DEFAULT NULL,
  `size` int NULL DEFAULT NULL COMMENT '大小KB',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人/所属用户',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint(1) NULL DEFAULT 0,
  `sync_directory_id` bigint NULL DEFAULT NULL COMMENT '所属同步目录ID',
  `relative_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '在同步目录内的相对路径',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code`) USING BTREE,
  INDEX `idx_create_by`(`create_by`) USING BTREE,
  INDEX `idx_sync_directory_id`(`sync_directory_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file
-- ----------------------------
INSERT INTO `file` VALUES (1, 'yU8E3UwgoNAu3LpzFTJ6zMFltxzFFpFk', '1e952ee2264fa56d2fcca9ecdf19bee6', 1, 'https://ytb.hlc-inno.com/api/', '20260324', '.jpg', NULL, 952454, 1, '2026-03-24 14:06:19', NULL, '2026-03-24 17:51:20', 0, 1, '1e952ee2264fa56d2fcca9ecdf19bee6.jpg');
INSERT INTO `file` VALUES (2, '4fMWFbxehBN1mebRbYeD6wrGssHWaKYU', '1a831a91ceb3839b4cf4d0b4c27524dc', 1, 'https://ytb.hlc-inno.com/api/', '20260324', '.jpg', NULL, 2942480, 1, '2026-03-24 14:55:28', NULL, '2026-03-24 17:51:21', 0, 1, '1a831a91ceb3839b4cf4d0b4c27524dc.jpg');
INSERT INTO `file` VALUES (3, 'nRICUjxLeOZ0VBTWrlHCpbTmHsNFwQeO', '测试文本', 2, 'https://ytb.hlc-inno.com/api/', '20260324', '.txt', NULL, 9, 1, '2026-03-24 14:58:26', NULL, '2026-03-24 17:51:22', 0, 2, '测试文本.txt');
INSERT INTO `file` VALUES (4, 'BqP41B4ymfphWwa2mHkDej6v0j82Cw1X', '854080DD9ACCAFA11EFB2E5E124EA2CF', 1, 'https://ytb.hlc-inno.com/api/', '20260324', '.jpg', NULL, 33795, 1, '2026-03-24 15:03:57', NULL, '2026-03-24 17:51:23', 0, 2, '854080DD9ACCAFA11EFB2E5E124EA2CF.jpg');
INSERT INTO `file` VALUES (5, 'sOAnNNpZ3htsdTvQ0r14GUhuM8pK6y9n', 'dist', 5, 'https://ytb.hlc-inno.com/api/', '20260324', '.zip', NULL, 589201, 1, '2026-03-24 15:04:49', NULL, '2026-03-24 17:51:24', 0, 1, 'dist.zip');
INSERT INTO `file` VALUES (6, '096T7mImj1cK9yHqbd38DpWr9Hdl3hMc', '1cbd12b031859da32e21dddbc483f8a8', 1, 'https://ytb.hlc-inno.com/api/', '20260324', '.jpg', NULL, 874149, 1, '2026-03-24 16:03:55', NULL, '2026-03-24 17:51:25', 0, NULL, NULL);
INSERT INTO `file` VALUES (7, '81pPmMoWDw6OT2JKvaSYzvldkntMPVHJ', '特殊密码文本', 2, 'https://ytb.hlc-inno.com/api/', '20260324', '.txt', NULL, 55, 4, '2026-03-24 16:11:41', NULL, '2026-03-24 17:51:26', 0, 3, '特殊密码文本.txt');
INSERT INTO `file` VALUES (8, 'b88LlzAdvt0a75zKiy4SUBjsJRns6bbq', 'dist', 5, 'https://ytb.hlc-inno.com/api/', '20260324', '.zip', NULL, 589201, 4, '2026-03-24 16:20:49', NULL, '2026-03-24 17:51:27', 0, 3, 'dist.zip');
INSERT INTO `file` VALUES (9, 'JROygqp61QofvytRfumErh09GAuwDjAw', '测试文本', 2, 'https://ytb.hlc-inno.com/api/', '20260324', '.txt', NULL, 0, 1, '2026-03-24 16:21:40', NULL, '2026-03-24 17:51:28', 0, 2, '测试子目录/测试文本.txt');
INSERT INTO `file` VALUES (10, 'Km9qTt0fpm9VjlhbP06h4tsYD8LPOEeB', '6b2e257c4b33d9348ca9f16956f215f7', 1, 'https://ytb.hlc-inno.com/api/', '20260324', '.jpg', NULL, 43147, 1, '2026-03-24 16:46:43', NULL, '2026-03-24 17:51:30', 0, NULL, NULL);
INSERT INTO `file` VALUES (11, 'q4cT9haOQPREAux8w4rvpcOcSPLqM0hi', '6b2e257c4b33d9348ca9f16956f215f7', 1, 'https://ytb.hlc-inno.com/api/', '20260324', '.jpg', NULL, 43147, 1, '2026-03-24 18:28:52', NULL, '2026-03-24 18:28:52', 0, NULL, NULL);
INSERT INTO `file` VALUES (12, '2hjytQDKWkSPKsUzC1tWKrWX0YVbZm2D', '张三的文件', 2, 'https://ytb.hlc-inno.com/api/', '20260324', '.txt', NULL, 27, 5, '2026-03-24 19:01:13', NULL, '2026-03-24 19:01:13', 0, 4, '张三的文件.txt');
INSERT INTO `file` VALUES (13, 'LxCZhTQYxeMPcwIPhvn4kBfaGKYqt2Jm', '新建文本文档', 2, 'https://ytb.hlc-inno.com/api/', '20260325', '.txt', NULL, 0, 1, '2026-03-25 09:35:20', NULL, '2026-03-25 09:35:20', 0, 1, '新建文本文档.txt');
INSERT INTO `file` VALUES (14, 'QDZLZfno55Sja8nwLoo7hQ4pi1cT4Yyp', '绝密文件', 2, 'https://ytb.hlc-inno.com/api/', '20260325', '.txt', NULL, 46, 1, '2026-03-25 09:35:25', NULL, '2026-03-25 09:35:36', 0, 1, '绝密文件.txt');

-- ----------------------------
-- Table structure for install_package
-- ----------------------------
DROP TABLE IF EXISTS `install_package`;
CREATE TABLE `install_package`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `download_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公开下载随机码',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '展示文件名(含后缀)',
  `relative_path` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储子目录(yyyy-MM-dd)',
  `suffix` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件后缀含点',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '字节',
  `sha512` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SHA-512 Base64',
  `version_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本说明',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `del_flag` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_download_code`(`download_code`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '安装包分发表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of install_package
-- ----------------------------
INSERT INTO `install_package` VALUES (1, 'mnSSG85qLISrcXqJrZHY0R61US1yPlxW', 'ytb_20260324_006.exe', '20260324', '.exe', 78429923, NULL, '1.1', '第一个版本', '2026-03-24 18:59:07', 1, '2026-03-25 11:07:17', 1, 1);
INSERT INTO `install_package` VALUES (2, 'GxIx8uGO4cvdlTVbLLck12AWbbuZJOYN', 'ytb_20260325_004.exe', '20260325', '.exe', 78669466, 'fFQWi21ACEFuHumlrLjPsSJNqhM6OMYfeNf3OBI2yWFs7EqhJnAtSpOHHDlRtcWhHDwxwLyNG7lLUAx/Soy1+g==', '1.0.2', '优化版本', '2026-03-25 10:47:36', 1, '2026-03-25 11:07:15', 1, 1);
INSERT INTO `install_package` VALUES (3, 'ir8SZXpgoqvIkGstd3tSMmFQHOUBN9IT', 'ytb_20260325_006.exe', '20260325', '.exe', 78669905, 'H0nEfGU0mxSyLjS0qCiO9wFxTdmatQEKu1nr+XTRPLS8M9GLdE9onV0l+go1fU+uFlo7LB3/8A69Bee8izsuGw==', '1.1.0', '优化安装包自动更新问题', '2026-03-25 11:00:28', 1, '2026-03-25 11:19:11', NULL, 1);
INSERT INTO `install_package` VALUES (4, 'mo7lQ9duu7Upik7DHHHILeb6KxSEUevz', 'ytb_20260325_008.exe', '20260325', '.exe', 78671041, '5PI+Rfr8LJsg3magnXhrhj30NAvrWEvk6z4M5/KyR2x6Onv1PAFGRSYs4uQ2Vh8lDU2tFdA0QnGDxSB5yjeOXg==', '1.2.0', '优化安装包自动更新', '2026-03-25 11:19:13', 1, '2026-03-25 11:28:05', NULL, 1);
INSERT INTO `install_package` VALUES (5, 'pmASqpnjYsGKPFiHxD3AxJSlq97Mwa7D', 'ytb_20260325_009.exe', '20260325', '.exe', 78670486, 'ha/Cu+AB5Zv6SOI8I/QeS9nwXpZB04bsu3aabTMQIY38+84eJPPxCIYPiTMbayudI3TcwvzKsJqED1/ma5NJtQ==', '1.5.0', '优化版本', '2026-03-25 11:28:05', 1, '2026-03-25 11:28:06', NULL, 0);

-- ----------------------------
-- Table structure for menu
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pid` bigint NULL DEFAULT 0 COMMENT '父ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单标题',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单类型',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限code',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端路由名',
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `hidden` tinyint(1) NULL DEFAULT 0,
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路径',
  `affix` tinyint(1) NULL DEFAULT 0,
  `keep_alive` tinyint(1) NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `del_flag` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 116 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of menu
-- ----------------------------
INSERT INTO `menu` VALUES (100, 0, '文件管理', 'menu', NULL, 'file/index', 'folder', 'Files', NULL, 10, 0, '/files', 0, 1, '2026-03-24 10:34:53', NULL, '2026-03-24 10:34:53', NULL, 0);
INSERT INTO `menu` VALUES (101, 100, '文件列表', 'menu', NULL, 'file/list', 'document', 'FileList', NULL, 1, 0, '/files/list', 0, 1, '2026-03-24 10:34:53', NULL, '2026-03-24 10:34:53', NULL, 0);
INSERT INTO `menu` VALUES (110, 0, '系统管理', 'menu', NULL, 'Layout', 'setting', 'System', NULL, 100, 0, '/system', 0, 1, '2026-03-24 10:34:53', NULL, '2026-03-24 10:34:53', NULL, 0);
INSERT INTO `menu` VALUES (111, 110, '菜单管理', 'menu', NULL, 'system/menu/index', 'menu', 'MenuManage', NULL, 1, 0, '/system/menu', 0, 1, '2026-03-24 10:34:53', NULL, '2026-03-24 10:34:53', NULL, 0);
INSERT INTO `menu` VALUES (112, 110, '角色管理', 'menu', NULL, 'system/role/index', 'peoples', 'RoleManage', NULL, 2, 0, '/system/role', 0, 1, '2026-03-24 10:34:53', NULL, '2026-03-24 10:34:53', NULL, 0);
INSERT INTO `menu` VALUES (113, 110, '用户管理', 'menu', NULL, 'system/user/index', 'user', 'UserManage', NULL, 3, 0, '/system/user', 0, 1, '2026-03-24 10:34:53', NULL, '2026-03-24 10:34:53', NULL, 0);
INSERT INTO `menu` VALUES (114, 110, '日志管理', 'menu', NULL, 'system/log/index', 'form', 'LogManage', NULL, 4, 0, '/system/log', 0, 1, '2026-03-24 10:34:53', NULL, '2026-03-24 10:34:53', NULL, 0);
INSERT INTO `menu` VALUES (115, 110, '安装包管理', 'menu', 'system:installPackage', 'system/install-package/index', 'download', 'InstallPackageManage', NULL, 5, 0, '/system/install-package', 0, 1, '2026-03-24 17:01:35', NULL, '2026-03-24 17:01:35', NULL, 0);

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色标识',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1 COMMENT '0禁用 1启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `del_flag` tinyint(1) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, '管理员', 'admin', '系统管理员', 1, '2026-03-24 10:34:53', NULL, '2026-03-24 10:34:53', NULL, 0);
INSERT INTO `role` VALUES (2, '普通用户', 'user', '普通用户', 1, '2026-03-24 10:34:53', NULL, '2026-03-24 10:34:53', NULL, 0);

-- ----------------------------
-- Table structure for role_menu
-- ----------------------------
DROP TABLE IF EXISTS `role_menu`;
CREATE TABLE `role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_menu_id`(`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_menu
-- ----------------------------
INSERT INTO `role_menu` VALUES (1, 1, 100);
INSERT INTO `role_menu` VALUES (2, 1, 101);
INSERT INTO `role_menu` VALUES (3, 1, 110);
INSERT INTO `role_menu` VALUES (4, 1, 111);
INSERT INTO `role_menu` VALUES (5, 1, 112);
INSERT INTO `role_menu` VALUES (6, 1, 113);
INSERT INTO `role_menu` VALUES (7, 1, 114);
INSERT INTO `role_menu` VALUES (8, 2, 100);
INSERT INTO `role_menu` VALUES (9, 2, 101);
INSERT INTO `role_menu` VALUES (10, 1, 115);

-- ----------------------------
-- Table structure for sync_directory
-- ----------------------------
DROP TABLE IF EXISTS `sync_directory`;
CREATE TABLE `sync_directory`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `local_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '本地绝对路径',
  `display_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '显示名称',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '0正常 1删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '同步目录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sync_directory
-- ----------------------------
INSERT INTO `sync_directory` VALUES (1, 1, 'E:\\同步测试目录', '', '2026-03-24 14:01:21', '2026-03-24 14:01:21', 0);
INSERT INTO `sync_directory` VALUES (2, 1, 'E:\\测试目录', '', '2026-03-24 14:01:33', '2026-03-24 14:01:33', 0);
INSERT INTO `sync_directory` VALUES (3, 4, 'E:\\sakura同步测试', '', '2026-03-24 16:11:42', '2026-03-24 16:11:42', 0);
INSERT INTO `sync_directory` VALUES (4, 5, 'E:\\张三同步目录', '', '2026-03-24 19:01:14', '2026-03-24 19:01:14', 0);

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `request_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `area` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `operator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `module` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `class_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `method_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `request_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `request_body` tinyint(1) NULL DEFAULT NULL,
  `param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `type` int NULL DEFAULT NULL COMMENT '0:其它 1:新增 2:修改 3:删除 等',
  `success` tinyint(1) NULL DEFAULT NULL,
  `code` int NULL DEFAULT NULL,
  `message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `exception_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `exception_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `browser_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `browser_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `engine_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `engine_version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `os_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `platform_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `mobile` tinyint(1) NULL DEFAULT NULL,
  `device_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `device_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------
INSERT INTO `sys_operation_log` VALUES (1, 'e262c4a4b6584508bb25e40ef0adb276', NULL, NULL, '用户注册', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/register', 'claw', 'com.claw.system.controller.UserController', 'register', 'POST', 'application/json', 1, '{\"username\":\"sakura\",\"password\":\"G6eK7WDnewOLvxEi1iMkcx3PIEkS4jrM3Uiv8+ZqGcoEX2dO7XwRoJyTEJSq//D8OdAfgipZb8F3QPSEqRF57+6j0SfCZKQd9Kh5k8s5umwVIqMMsYr7r8DYf1i08Wr8g/UV46/Uf3Cm++5n0EB6D0EgPiY4K+TtvbLSVrTWeIo=\",\"nickname\":\"sakura\",\"mobile\":\"13128830520\",\"smsCode\":\"666666\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 10:55:32', '2026-03-24 10:55:32');
INSERT INTO `sys_operation_log` VALUES (2, 'f7d1f6c658c144cc9061495a1a2c98bb', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"sakura\",\"password\":\"Ob7MCxEBKgLmWuVWHauiawOBpgGmJfnMbn6gYbrCVYFSzwuO0YEoCxNFGxXhmHjRWcddPTpFcqn2uIRtPZjVzVpaYZiGYnLEi03RViuYXPIC7TNJCjMGGJd0jvvl03uUezoqccb1h2848pUGklb2IjYFvPeFEYXY4wYZD+Jsz/Y=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 10:55:41', '2026-03-24 10:55:41');
INSERT INTO `sys_operation_log` VALUES (3, '0507df8e1ce74b38a8035a09f480025c', NULL, NULL, '用户登录（账号密码）', '192.168.31.73', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"faK98TnCD985ZhurpxiUXIBZZptmZNFUX3SUrV/HH1jgF9TPDsoFDtC0vGi7fWD8/AWFhXYhYjB0hNfppMPh1bx44DOaHfya/Dmo6Fa3GFNGkqh0qVrbBj0O2AijKJPXLeZK04lY4y12eOcCYgiRGxF7u3UpgV/wp/zn4MHlf+c=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 13:59:57', '2026-03-24 13:59:57');
INSERT INTO `sys_operation_log` VALUES (4, '762b17c9837540f6a04bd0bd2cf72e20', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"ckfEzf6U+jLUBqmuHiinVydyn8adZ4Gk9XDfkFBGA9K4N1qNqSWKrTwOQNIX0n6LeREZJUcuUCl7Zq2Omb5uUn4rX7l7zdrMYXxRNdjaS0LCOU7sNyw4gtKvzSI4LKUx9JJKLSO3tfffkFLgv25x6V2pZ7n0Kct+OkuLqLJ7abs=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 14:07:12', '2026-03-24 14:07:12');
INSERT INTO `sys_operation_log` VALUES (5, '49f267a5caec403ab9b6d1e23b455557', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"dCvJd1+H7yzPEHGCTZtmS8AjXm50HV78DCdVA7hxGgcDDxZvlv4jEaE7GrrE8gvq3/i9fBGHL3Xecqk7W1ecvaHMHyzX+L+OjsdL1BdGVyuDYkBFfnEAJGzgKawPYaF5a0lvAuqU5YLb2+QU0+q1AXDWgHg5p12kQ2HeoVGo1AE=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 14:11:44', '2026-03-24 14:11:44');
INSERT INTO `sys_operation_log` VALUES (6, '556a96eaa7ac4c37a9937967a713001a', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"FGZ0dSf8JHFgONFPHxeiE4TcM59TWw8aa7cwNavf0Jj+uoatHi+JGYFSddrkj/ljzZw433H9YukZNFXg9rODJR/fgdfbaDUKNolfrq6swPAjGihexr+k1AIcCUfNu5b++v+G3ElJAxkPB2rDVWf6O8W8ht8sfyjBlVA6V98OJ18=\"}', NULL, 1, 0, 500, NULL, 'com.claw.common.exception.BusinessException', '用户名或密码错误', 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 14:12:42', '2026-03-24 14:12:42');
INSERT INTO `sys_operation_log` VALUES (7, 'ec544453ca9f455d8ff6ab6a13133b4b', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"Lcj93eCDfANyfP5tEKQ99kPKGIKtVoLfoXkQqE3ycbA003xoz54WfeU141ouqvaMfiwrKu5HjFn8Dqj5zcL/uST2m332CfXf4lQ6mSnfHWzugMsq0KDK0iz4rf3gNaQ5qlx2r0vFxQA54rIlnhiT+8Ep7Mchj5MR+KrmMizY7g0=\"}', NULL, 1, 0, 500, NULL, 'com.claw.common.exception.BusinessException', '用户名或密码错误', 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 14:12:53', '2026-03-24 14:12:53');
INSERT INTO `sys_operation_log` VALUES (8, 'b63381c570504404a435efcfc80d6a97', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"CsZfVJ859+r06xDJLg2SoSjMOiXmoERpa6Bk2Ua8vtJhOR/cjfnmNIKcDK//B7g8gYBvwKU3Qs56F7H7hdW3DtOdFpvAs/p9nzCug0zA0IgvjLEBf9BrqJ89z3T6InJhPWGG9LU7ck5Pw+Z1n9tnEafhKeuKobd04qQYvuJR4wI=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 14:15:46', '2026-03-24 14:15:46');
INSERT INTO `sys_operation_log` VALUES (9, 'a7a8edbac37a4d6489ecad6258e70cd5', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"eIKl0wUWhyzQ59HFYMURB/f9EaR5RbDbsdsppEHQmNte00ynTWYZPOuVVcZ3nTgaVEgqId1yADE0dOU4hFudpxwriv5AtbDS+Xl4c+35ENgyFL6t5zdf7fF/Ml5yHPS2ST6kOuDooPkAq26VgMRTpvs/PiBbFvsChSsA/nWX15g=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 14:55:28', '2026-03-24 14:55:28');
INSERT INTO `sys_operation_log` VALUES (10, '13502109d1ea44fcb6d50cfd83933628', NULL, NULL, '重置密码（管理端）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/resetPassword', 'claw', 'com.claw.system.controller.UserController', 'resetPassword', 'POST', 'application/json', 1, '{\"userId\":1,\"password\":\"XzfWFVVBPo4dUhqburHepvYWPBHFiLD7x7Ad3oxXE3SNdeUSfJOCqp6rMZdg9tmHwTRPifz/kN3BArimau5wuUwg8JGUnY1XkMguFm0nPkVdyZIBS3tpi5l4THhmrf6vK3A+GMYR8WUJ5igD8/re5vdfBe5pQARBS3YKIcL9Ic4=\"}', '68900e1cfaeb957eb71640f37e0c2ddf', 0, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 15:45:59', '2026-03-24 15:45:59');
INSERT INTO `sys_operation_log` VALUES (11, 'a176dc8eadce4fa4a2ca76b93ca3ea7a', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"JogkSbRm8DUnDdEPMVFLuOZBPS3oaeqWhCibLChuEqTZ3pV5As4Q9CY3JR7scpAr40CqBnJHHqyLMcD1XwyVXiGKiAhdVmIVqKM0CMd2wEO/00XR92QTr6Enq7tERKalWH6weaZkZqBPa/Ic6HcIDV4z+uLrZoSG+vSevoBOwxU=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 15:48:41', '2026-03-24 15:48:41');
INSERT INTO `sys_operation_log` VALUES (12, '69634b73c072498b9887a197ec7a58ab', '1', 'admin', '禁用账号', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/disable/4', 'claw', 'com.claw.system.controller.UserController', 'disable', 'POST', NULL, 0, NULL, 'f059d1f025b6b6470c687867bedcd067', 3, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:01:14', '2026-03-24 16:01:14');
INSERT INTO `sys_operation_log` VALUES (13, 'da8d0d3d6dc64102bbc4b1a39c42c542', '1', 'admin', '恢复账号（账号被禁用或冻结）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/restore/4', 'claw', 'com.claw.system.controller.UserController', 'restore', 'POST', NULL, 0, NULL, 'f059d1f025b6b6470c687867bedcd067', 3, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:01:16', '2026-03-24 16:01:16');
INSERT INTO `sys_operation_log` VALUES (14, '0863173e07a04774a27f4edf04b0503c', '1', 'admin', '冻结账号', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/freeze/4', 'claw', 'com.claw.system.controller.UserController', 'freeze', 'POST', NULL, 0, NULL, 'f059d1f025b6b6470c687867bedcd067', 3, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:01:25', '2026-03-24 16:01:25');
INSERT INTO `sys_operation_log` VALUES (15, 'ac7b02d75e2b42338c79ce15e459cd69', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"sakura\",\"password\":\"W81AVibYWNNmQ8OhjyYlDFXosGvWA2Ym2RzhCoKVaI0BRFafY/gwJqYI4lasbNVib0uvT2Rkt2Mo/JCyM5leMGDUPsatLKttND0pSK6y6UEPG8QJ7Uzm2j7IIBWTAMcx3zbakd9knRPHCzMCvi/QLoaD6YUUdAkultdxiOvPDS8=\"}', NULL, 1, 0, 500, NULL, 'com.claw.common.exception.BusinessException', '账号已被冻结，请联系平台客服', 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:01:42', '2026-03-24 16:01:42');
INSERT INTO `sys_operation_log` VALUES (16, 'b1414d98ef384263a9272d8d444f8ab4', '1', 'admin', '恢复账号（账号被禁用或冻结）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/restore/4', 'claw', 'com.claw.system.controller.UserController', 'restore', 'POST', NULL, 0, NULL, 'f059d1f025b6b6470c687867bedcd067', 3, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:01:47', '2026-03-24 16:01:47');
INSERT INTO `sys_operation_log` VALUES (17, 'c7cde04fd8974114851daa9cc4da125a', '1', 'admin', '禁用账号', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/disable/4', 'claw', 'com.claw.system.controller.UserController', 'disable', 'POST', NULL, 0, NULL, 'f059d1f025b6b6470c687867bedcd067', 3, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:01:50', '2026-03-24 16:01:50');
INSERT INTO `sys_operation_log` VALUES (18, 'be0f6df723cf41408fde2347614a63d6', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"sakura\",\"password\":\"JpimSH19LRxhxxNYTqe9qYdc22CS+EZecqhAn9rGR1uBModaSEWW1iheLqoYwLVLT41KFBS/5pOsI2dxHYH19Gc995+OW53BnNuKNVoIom/SfFXE5vvqzvC+PZlBGSM0d3Jw7HaxGGnJekh7GGNcwF6TKo+eXiCg/MpDddfd5Ys=\"}', NULL, 1, 0, 500, NULL, 'com.claw.common.exception.BusinessException', '账号已被禁用，请联系平台客服', 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:01:52', '2026-03-24 16:01:52');
INSERT INTO `sys_operation_log` VALUES (19, '000a22d3fe414d17bb472a3dfe1df5aa', '1', 'admin', '恢复账号（账号被禁用或冻结）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/restore/4', 'claw', 'com.claw.system.controller.UserController', 'restore', 'POST', NULL, 0, NULL, 'f059d1f025b6b6470c687867bedcd067', 3, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:01:56', '2026-03-24 16:01:56');
INSERT INTO `sys_operation_log` VALUES (20, '899d5e0ef0f948419f7c9f45ad900aa5', '1', 'admin', '修改用户（当前用户）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/update', 'claw', 'com.claw.system.controller.UserController', 'update', 'POST', 'application/json', 1, '{\"nickname\":\"管理员\",\"headImg\":\"/api/file/096T7mImj1cK9yHqbd38DpWr9Hdl3hMc?Authorization=fc78b19d-2079-4d58-a008-75d90781d213\"}', 'f059d1f025b6b6470c687867bedcd067', 2, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:03:59', '2026-03-24 16:03:59');
INSERT INTO `sys_operation_log` VALUES (21, '968a81800cdf43f1802a097bfe2250d1', NULL, NULL, '修改密码（当前用户）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/updatePassword', 'claw', 'com.claw.system.controller.UserController', 'updatePassword', 'POST', 'application/json', 1, '{\"password\":\"QNT+h6qqN7uNlW1gXANnPjHRZN40YCd50vGbiqMishm2893EC44trMklUTvFo/a9ujL80W1n0rw2QE0uQKzvTNAETEW4hwqRcSD+VU3Cc+rFui/44FFCGh+shI3Y11lTYCsMprxqEO01uCGdeBc1JojqzTZI9JQbC8EbU9d8ou4=\",\"mobile\":\"13100000000\",\"smsCode\":\"666666\"}', 'f059d1f025b6b6470c687867bedcd067', 0, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:04:27', '2026-03-24 16:04:27');
INSERT INTO `sys_operation_log` VALUES (22, '8e23d884c55d4b7285d8b7fbb7ca1e1d', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"Pw5HrAsfGtAQwLYzowcztsjNlROL4CHR4D/JmTEkzrA/NqJMtYL3nCLAoLrLchH16nGk2hCxO3U5vseFXAbgopzGzV+YsjEDPQ3bMEnUNuQCEpWnsX7FpiQ9/hoFH/peu82o/wR0uxFn7yCpR7C8noab4XK0+zMYIG6sUDKUxnA=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:04:39', '2026-03-24 16:04:39');
INSERT INTO `sys_operation_log` VALUES (23, 'c4b215d36967427385260a36f3b36299', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"sakura\",\"password\":\"ONYXvJ+iZIfOvtwteluJsTfWL7LzjxcbyHBgGCUUiINWMuCBF0aPpPqfRsH4HqOX1dSIrKenzXLNoEydkUIMOOU8uaXfpmGYXmHlfcZXgUKsud2/LYfPYMIRXXb2ScZOM3aHi7zqOXUcn6RydkpxU+wxzF3sU5Q7zb3HDbyKnKM=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:04:51', '2026-03-24 16:04:51');
INSERT INTO `sys_operation_log` VALUES (24, '42d2e35ff903474b9b8daee418786dbe', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"sakura\",\"password\":\"Qj2Fe4hy2DuyQOmyxRtUKqWz4UIm4IPEJBgEUF61CU0OrkaPYLo1YD8/ieuGZu8EEA81DzIekUCYTRNvNcNsMRpq2S/6Q2OSL1C9TVsGVN6qAItxX3uhdurKtIW8RgPCZfKohVfE4oLKlthd5Rf4wgTTIEVhEOVN7a2AmTLf84E=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:15:39', '2026-03-24 16:15:39');
INSERT INTO `sys_operation_log` VALUES (25, '25f174a8247041d2ba46ff842ff75812', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"HRC95R4VVKr+bPW69fnOJ21OkpoaH60uYux4d7g69antAw5Bklik9HXKr/hq3QrrOqBo1tUDrQ+a0WtC55P/nonwxve3DKPzIIiiBdJCYd00AgDHNJQrh2glLQRl5XWtN3swSxT+HSfGxY0iAXHrYO1xWjVL8AuGzNhF+y7D42k=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:21:40', '2026-03-24 16:21:40');
INSERT INTO `sys_operation_log` VALUES (26, 'b1f645ce61e941d5b164431a23b12372', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"E1Gfrl51oSHesHdyfImJ3os7xt6h8F6lDsoDCceOqVyFnnELJo0pY5KnxYEp6Av+XoLF/d3VeS3aqIGFPpdZWG4VCOMsZsEM5N7nAHVnFNYGQtNmnwTm8o2JrAZQPUblPhVmieibsj9cicdKhXHzP8QTDJWUmdhmcNNovgkT4rk=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:26:09', '2026-03-24 16:26:09');
INSERT INTO `sys_operation_log` VALUES (27, 'a43ba64cf03440e3a7704663853f4b36', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"QGZMh8FUcjsVGADysugahcYFlBLzYsEa3DiNVyCmSE9KviVx5cgEWf+So+Jx6dOGWLsTcNToArfVaiL07XZksk7vVPjtebE7qlQClprWbtHmRuOgFDAJphXltWQefRjS9t3ecgsIAYEsq8fwBq2knnnvqX9icGscLgJryA3Pz5I=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:43:02', '2026-03-24 16:43:02');
INSERT INTO `sys_operation_log` VALUES (28, '902c2245e09d48bd9ffdb3fd9163cc9f', '1', 'admin', '修改用户（当前用户）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/update', 'claw', 'com.claw.system.controller.UserController', 'update', 'POST', 'application/json', 1, '{\"nickname\":\"管理员\",\"headImg\":\"/api/file/Km9qTt0fpm9VjlhbP06h4tsYD8LPOEeB?Authorization=50a63242-4126-4ca9-a24d-6e73734598ef\"}', '5efe2361eb627b36fe90ae95a5cbf3bf', 2, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:46:44', '2026-03-24 16:46:44');
INSERT INTO `sys_operation_log` VALUES (29, 'eeb2556ee3074cc2987b770ecdf36915', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"SBnlwynMScG6UOND/gkxm0k0XZR/zS/kfFiJYalFh0/MA9kkBDGui7d+wmAFFW8UdEPwwM8RVlYeNji9cA6ZW1bB1DQN0xQ3+7s7QY+UeSzdDMnG6Hjtka8Ti0Ii76rdeyAmqsZnH3Kf9FdODJfs+VkiO4VnDyYBb0FceMfG3m4=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 16:48:24', '2026-03-24 16:48:24');
INSERT INTO `sys_operation_log` VALUES (30, '8883606457e3491893510123400b025c', NULL, NULL, '用户登录（账号密码）', '0:0:0:0:0:0:0:1', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"sakura\",\"password\":\"W8+FSAEpNM0+vhbxjBEi7gIqu5CPL3WJIK/X2xVZXq0mVOsXzenuhH7YGAwFwyekx/rpGsstABMhuq2h/fC+Lhg5PdY6FbYqZp/I1gTaEF4tlprqLN2WSIgXpSuSmjaCtaZLeV98oLw69MQET4P/HPL4CV+OVAdm0iD+aZ39FYk=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 17:03:43', '2026-03-24 17:03:43');
INSERT INTO `sys_operation_log` VALUES (31, '27e19d8c00a74250b1edb9097fb0d427', NULL, NULL, '添加保险', '39.106.71.206', NULL, NULL, '/open/insurance/add', 'insurance', 'com.yatai.insurance.controller.OpenInsuranceController', 'addInsurance', 'POST', 'application/json', 1, '{\"userNo\":\"8820260209815809\",\"timestamp\":\"1774346881501\",\"sendNo\":\"1774346881501722789\",\"sign\":\"KfVd7qUypw38fB75PibhJxv88DDKEoaDffnowH+2kHY/l3zNkO7k3e7/Y3sx1lSMw1la4FUbkkXuILT+tTuRekPjeX1bQvGhn1gh6zg968wpppiD89Si8v4oGfctNDhRcVDiLZeIeM8Eq1SV6DPejxRuqS1ZMAGef7szHCqkTwM=\",\"signType\":\"RSA\",\"bizCode\":\"001\",\"bizData\":\"e9kFXXSZXU53aJYqrcDEFjlFIiLaIw8ESzmiPvWhHz8SkOb/NIzVls9vaOggN5s1kkJ/QNOKdZL4i4XQkdiKeQMjB/JTOfvIuDPu0uC22ZB+hj/HI9y1QKzm9LeFu/dF3nNKM3sgI81ZxEU2oUhdvLfXIEh6MUZcQ9ndWMqi/79uRzSSHasWQ/f4Ax0hhn1B03ei9KPJTyu6sggqciDLwSJGpb1nsqkQcIByR813DlBKYDHliYuZICzqZKiTwriwJvSudUQYhlKAk/3PwWxrdc6bAuD8e5Qu3qarrypn2PHqVdeLjt11fIALucrusgG6ljrhSR5oLLTnnivm0mj1iQ==\"}', NULL, 1, NULL, NULL, NULL, NULL, NULL, 'Unknown', NULL, 'Unknown', NULL, 'Unknown', 'Unknown', 0, NULL, NULL, '', '2026-03-24 18:08:04', '2026-03-24 18:08:04');
INSERT INTO `sys_operation_log` VALUES (32, '383b228cea3f4b52bd5d05af4df0573c', NULL, NULL, '添加保险', '39.107.66.162', NULL, NULL, '/open/insurance/add', 'insurance', 'com.yatai.insurance.controller.OpenInsuranceController', 'addInsurance', 'POST', 'application/json', 1, '{\"userNo\":\"8820260209815809\",\"timestamp\":\"1774347749344\",\"sendNo\":\"1774347749344141820\",\"sign\":\"GGZB0iNJeZe3Q3H0UN/I8sNO0koVjolm0P8lr3C6nrwQuiv1xoSApe1SidbUUqemAVKE4rYIj8GZgdHCRi+aA6mPernzbsbU2lE0t8hKjWWkJGbtc4JsMK53Eo5tS9zYMpQ8fAxCYAKNTRxxDyQ3FHwGTifxlafnFA8iIS6dfhs=\",\"signType\":\"RSA\",\"bizCode\":\"001\",\"bizData\":\"VHrhG8IAT01HDkX0eGCmY4OUv8SuFrBtaiEK7eZwPLeBIyvkKgjZSyI6ySSjdf+HYtmYUNigZaq51TeqaO5u2qXRDrB4oNpRJGV5TQFfTx4fQTV8fjMe5+rIms0eoxwzmre9FTY1lotNDXLLid+0KqJZFUDMitS42b0HYRJVxP+UfKicRYmcpvltN51G034NfjPk6bntTXqrpLVgPsYUSK63ez89tdrUdvMWB1lET2squlnHWpjgAq0idQ5VeXxzU3Cw3K9KP6yRFHEG8uCOB0Egkdp5FYbsU0flntUpioquGSvYnjPl5MyPkccd7S01oVYwrbEJgQmNXjLvU0vkFTDpnVwZiG00iRtZmFmKuPsnl8OncH8f1ziddUeXk/zm6ha3gb1DMTGwS8t2LjKQtb7crtwAzUDJrdigYVgNuRJd81hUdZBHUUsIAb5vXrwvMELdYnPVKyKmA/Woj4w3TI6qTS0Bi5/5g12/J7FuNF8nk4BNI1Ljl0MzrwFjbGsC\"}', NULL, 1, NULL, NULL, NULL, NULL, NULL, 'Unknown', NULL, 'Unknown', NULL, 'Unknown', 'Unknown', 0, NULL, NULL, '', '2026-03-24 18:22:29', '2026-03-24 18:22:29');
INSERT INTO `sys_operation_log` VALUES (33, '34c5c0a8962643b4b3e60969dcfb9105', '1', 'admin', '修改用户（当前用户）', '113.87.152.51', NULL, NULL, '/user/update', 'claw', 'com.claw.system.controller.UserController', 'update', 'POST', 'application/json', 1, '{\"nickname\":\"管理员\",\"headImg\":\"/api/file/q4cT9haOQPREAux8w4rvpcOcSPLqM0hi?Authorization=8d1fa43f-d311-4fbd-8fe5-7c8e69ba9d70\"}', '7f32ea939fff3e38189dfd5f4de2870b', 2, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 18:28:53', '2026-03-24 18:28:53');
INSERT INTO `sys_operation_log` VALUES (34, '30b9d0d3803644b9b79f09a098983dc7', NULL, NULL, '用户登录（账号密码）', '113.87.152.51', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"J8IST3AavYTZGHBfv64PGQvTA+fwIJTwQFebBnk1Ow13VTjITKxt54M+DCb1BxOrZr/GoZagKopfGZSpcljqrc0eS5qrIn4FN91PrF1mdwFREt5hjWT5iiXYo4DKJYw9tWMYqfoDV/SWvWuBnLMB5G3OYgIX8X3p1u5u9i0Lk20=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 18:46:57', '2026-03-24 18:46:57');
INSERT INTO `sys_operation_log` VALUES (35, '1db0aa34b662464da953a82c4d63698b', NULL, NULL, '用户登录（短信验证码）', '113.87.152.51', NULL, NULL, '/user/login/sms', 'claw', 'com.claw.system.controller.UserController', 'smsLogin', 'POST', 'application/json', 1, '{\"mobile\":\"13000000000\",\"smsCode\":\"666666\"}', NULL, 1, 0, 500, NULL, 'com.claw.common.exception.BusinessException', '当前手机号还未注册', 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 18:53:15', '2026-03-24 18:53:15');
INSERT INTO `sys_operation_log` VALUES (36, '0e592382f0ea465d960233da08f709fb', NULL, NULL, '用户登录（短信验证码）', '113.87.152.51', NULL, NULL, '/user/login/sms', 'claw', 'com.claw.system.controller.UserController', 'smsLogin', 'POST', 'application/json', 1, '{\"mobile\":\"13100000000\",\"smsCode\":\"666666\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 18:53:34', '2026-03-24 18:53:34');
INSERT INTO `sys_operation_log` VALUES (37, 'c5f541ffebff4cf895624a1a1b04f639', NULL, NULL, '用户登录（账号密码）', '113.87.152.51', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"N6bgR4SVyKgvpKBu1txyXvoDBtHwslXKZ/BojKXS07XDFZioxTqeMwWXqUhZLaGna8AHLX2k7LAPnj1OZneV2j8Kq9PDk9HOR9rxHkrgNMoDTKuNzo8qpY5ts9h6sJJgjvYVvN8TccegucC76o/rmZzwvJHZSeQxYr2F9LILkZU=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 18:56:23', '2026-03-24 18:56:23');
INSERT INTO `sys_operation_log` VALUES (38, 'f197fa8777d34625830f85a940e8f409', NULL, NULL, '用户登录（账号密码）', '113.87.152.51', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"fx4bYhlCpa47FJU/91EraqHzCH5g5wpQJjn6pwIJpg3dBzAOlalNUb9fvkmJWkYouANKygcen0uABAehSf4b4pRYdxOcEneNCQs6CIF/zc0vl/VGyeDSK1LYeQxcMuMYGttm7q+1EPm7UVFc15Na1m7yHS8FwduaqWaYIEBgMBg=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 18:58:25', '2026-03-24 18:58:25');
INSERT INTO `sys_operation_log` VALUES (39, '6ca8d4b80b954c5582469d49aeb79d97', NULL, NULL, '用户登录（账号密码）', '113.87.152.51', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"e/QcOO03MM7ODT6FM12r2X49Jne/tASipdcBDzGbAofu2kaYt8wxFkj0fF1V679iJPq6sqsJmB+fBlcvpG1YIuKE3DeSfS+asM/8WXLKa2O0k7TTIYEA7mcoczU/Tcq2EMksVmoF23W3JFXQa//6GhHem2q9cyqofISJDpF3uco=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-24 19:26:37', '2026-03-24 19:26:37');
INSERT INTO `sys_operation_log` VALUES (40, '801824e664054563975e0a122f45117a', NULL, NULL, '添加保险', '119.29.115.218', NULL, NULL, '/open/insurance/add', 'insurance', 'com.yatai.insurance.controller.OpenInsuranceController', 'addInsurance', 'POST', 'application/json', 1, '{\"userNo\":\"8820251210991944\",\"timestamp\":\"1774360071990\",\"sendNo\":\"20260324180813017758922002057902\",\"sign\":\"NSWEEUWNeJU0KqZ+QhFEKIgeGCuP6J1127kR71vzKAYBtKD54LFh7mdXknVFOIGcAqoLTCtdAJe2n+jqtBDTVPLceZAsCiXe3zkW7u1rxqWL5SOmjCe8VU6BL9mft3TfFqlaB+zKlho3yq4MSoB5WQtsT2FlWQ3AaYmkVY0XDlg=\",\"signType\":\"RSA\",\"bizCode\":\"002\",\"bizData\":\"AnyPyhTZrhtUdEGjO2o+aHHvpTWhkCYPf+EC55zl07FIefdUiGtEJlBSmuGWQ2Qi8g90Ch79UZWlAyaKs/l/a1b2eCY35OFGZroefkXKqeLiWE6Q2Aks/4IxMg6oAdnBb6ttIAx8fSZhTzQer86FJPt2PfS4Dh6WjG4ATg5rXR/J0Llm519H0AkBBJoRJt1VlXfxRPikVxW542Nn4XTjMi9lmA4KgYl0dUdBw5A76sfnmW0k3kfO+UMzqpyiYIZKgrbdQGBE09S30U43bwimtwyuHqD/tv0yZvtBPJiH4FteMh963fKUlh2nQjwTPkhqeq0bz5JwT4iBZnSHha2TJ1OatUW7+pHOYUUU6n8nmWwPxur4E6Skn5vySP0fQqs/mr3YqhNbzSdjAem0mTlW7YvUUT5t1mAvqHd3RBI+nBsmVTUk5pXm0/kOODp3f+OvcwBMFxJMR0EONvuheoy+5Kl6Z0W0bApMjN/dbaM2tOabxKssjnKcNUh1Son10LdU\"}', NULL, 1, NULL, NULL, NULL, NULL, NULL, 'Unknown', NULL, 'Unknown', NULL, 'Unknown', 'Java', 0, NULL, NULL, '', '2026-03-24 21:47:53', '2026-03-24 21:47:53');
INSERT INTO `sys_operation_log` VALUES (41, '97d51ab8fa574205b867052dbb608500', NULL, NULL, '添加保险', '39.106.71.206', NULL, NULL, '/open/insurance/add', 'insurance', 'com.yatai.insurance.controller.OpenInsuranceController', 'addInsurance', 'POST', 'application/json', 1, '{\"userNo\":\"8820260209815809\",\"timestamp\":\"1774387364083\",\"sendNo\":\"1774387364083156921\",\"sign\":\"KB/IoLotIh8Tq2X6zhHogDB2B1yhJ+beTQq1XVFym4PLbFRiYPMKzuYdAyritDHOS+jEVPO7zcMPQTT9L5ZRSGn68uwhmDazAwRBadKiWynYd1VbB9DYaAbcIQTRhwjxVQvcL5/MSBW+/OxtFAZztprTo0tBAKsKnDAwToCzk4w=\",\"signType\":\"RSA\",\"bizCode\":\"001\",\"bizData\":\"Ln680EDZJAQdt91QuLfHVYDiwiExWYJQq2OJTmI5n5bs8v/vRQV77mzGe668IIPk/7F864Kg+UAzYHZNJ1E+cs6l1NKx9/fpTjvYDJv7f8yXSx96biCRO1gjYBxbU2aWJUGWcmNSca/2cyMv1C9p+lhmx2DbIL7Rzzg2XMbJ6yIwDonFQ+jVVp6HsYhdCUscDuVrqHuETqfxPcnd1IpdZhiuacKuvT+tv0StVjI064trY89zuAg0qQxnFT6Ue/ss6Ufsph5ynnBpnmZ9A8v0aX5v+wr3ML6XB6SygKttEBdEPa3TjfEujfknniEwAPN3f+N2pZloBlNSCTtfzeXTTrXc1uHmcH2FIdEa6y/WACvBhca9YtujKc3m3tWVyihe3MeBpusTHm7kNyTN3ao+kZBd8pbqTrGom+7TDwWC+FGTdOPdtUX+v/5ZwDLTUUMC5CfKxDDtJ2wMyp2Ookjkg3ULMyXDBZfhZRV7LH8QMidRsr/fGosxVvk2+Vk2bRsZ\"}', NULL, 1, NULL, NULL, NULL, NULL, NULL, 'Unknown', NULL, 'Unknown', NULL, 'Unknown', 'Unknown', 0, NULL, NULL, '', '2026-03-25 05:22:46', '2026-03-25 05:22:46');
INSERT INTO `sys_operation_log` VALUES (42, '45010e7e11474075948f7d5d01232b95', NULL, NULL, '添加保险', '39.106.71.206', NULL, NULL, '/open/insurance/add', 'insurance', 'com.yatai.insurance.controller.OpenInsuranceController', 'addInsurance', 'POST', 'application/json', 1, '{\"userNo\":\"8820260209815809\",\"timestamp\":\"1774390375774\",\"sendNo\":\"1774390375774768721\",\"sign\":\"f69MGJOUtvQ5c7IA3Z8YBuk6jvJtr4cX7YaftPv6xhEE4BzMQTxZfoa5FGi1rsPZlRD7cDMVR/bjqc0t+DVB33mI2WlAk3/yhxgB7PviXqa/z3TVwVNzpBrYEJF1+BQu2Tx5dDE3FXJKWwN7YvKLDYfwf41lemyO1aVhhzWl5+M=\",\"signType\":\"RSA\",\"bizCode\":\"001\",\"bizData\":\"dVuj2xrKzDS8/ruA/3k0iEN07FDYvdTuobOFm+GPlNRXD5QJP7aenARnsq2gVEhPfVCzKES2JZVvkZPR+kTTdyi70e8Em+KgvEqEd5yJY2e/OiWhw21ireGe+CbsdW+wKBtVLvdoUZsyamoLIWDXjb8PjuUMpK6xY/VAGqQkLwmck/tqWXl7h6nfDYvsO16qupfIU4wUWtDoE+fpbqnjkWMVhxUTxP9T4crgQMBh+vwqkK5M7QlcRyi9wVdILT4XGdurhZMk0ZAw28diOjQUxAG00EMZyhYPh8Y+SwErF/LC4gD2XpuUQuh3X62jssJCjnGvONmc+iKxywP1rvmWQK8SzxI5o2QbQppz5030BnAKkBeJaPqQH99XRML+u8DhTZJG+QQg70/QErubqUXSUel2rTMttRrlLxzdw1AW1zfew2xN70J4tQuP4SpUTNbtI1YYtdWgKu7MEQH/yHjhPrE7hSsxV0ihCiGxieoztCy6hWlCsjU3DvSC5IJtq06Y\"}', NULL, 1, NULL, NULL, NULL, NULL, NULL, 'Unknown', NULL, 'Unknown', NULL, 'Unknown', 'Unknown', 0, NULL, NULL, '', '2026-03-25 06:12:56', '2026-03-25 06:12:56');
INSERT INTO `sys_operation_log` VALUES (43, '1136478f7e454c759761f2fad3cd9627', NULL, NULL, '添加保险', '39.106.71.206', NULL, NULL, '/open/insurance/add', 'insurance', 'com.yatai.insurance.controller.OpenInsuranceController', 'addInsurance', 'POST', 'application/json', 1, '{\"userNo\":\"8820260209815809\",\"timestamp\":\"1774390851724\",\"sendNo\":\"1774390851724502032\",\"sign\":\"ESVE0YaSWFaknltXVfIQaqeTRvGL2bEAkQNToTmplcjjJK2RMKkS8uejAoFwGMT2OZEdlQhoBiv+5s60LdcnLM7TzSbA2+Jq0KrqORkHSGM6wbVigi8UwGt8r2alOwwoXbtidPTAe6E5x7SdU2xKuu34TSfaI07SEwhXJkJr9K4=\",\"signType\":\"RSA\",\"bizCode\":\"001\",\"bizData\":\"FzY5akLkeQFKnO7Xa+4bALFUqscSWM7Mq0A2HJx8rtZzDRajzZOrUVi4jIyZe0wEQ+t124Qt3EkXI/HcoF8TxzbZrDu3DjSJXhyhrZKS9kAaZjLWCh4qEE9KHn6L9602F8XPEGUVoj8JN3V6+AkKzzilwTi5DQi8K9Psue38/a1ERWFHVNgWe7UDf6vkqvNm9lYWfhQPJwq6cnay7Wnm16ygSbeGUR5jkVC0Pnla9BJVCB9nUPbjLCphjfbEv1d0BIAf7Xk6B8XjQBtN5jC6KlG+b3E1aKl8Ir5aGQ/uK9NySHmHljd8C9+YQCz/tqS2Bducs9FI63H+FBlrCWVNqUAosNg7IcVZ8FOE1pKUdt5KICDvT6IzxbEELQkwx1uj2r4Mhtk9PdFVdgs4io2zVubh4LS0MAR8vgmA3XE0fFQTVBoTfHVw+K/HmxBzofkgavsis2JRIVJsdtq4GSP/i2aAOBpNssMbJlVlSa8phi2ydag5MyRBlx3gzuvsOgKC\"}', NULL, 1, NULL, NULL, NULL, NULL, NULL, 'Unknown', NULL, 'Unknown', NULL, 'Unknown', 'Unknown', 0, NULL, NULL, '', '2026-03-25 06:20:54', '2026-03-25 06:20:54');
INSERT INTO `sys_operation_log` VALUES (44, '103a53da99ac450bb36b6f002711698e', NULL, NULL, '用户登录（账号密码）', '113.87.152.51', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"c0qlnCKz2vcEZtVdcUOLsHmjIk8ukyzquNUWmmUu9kWMP6t55O9caF5ClB5pehAscVmieuGw+ZtDZZnIqKxpNGNMYkCJ2bLuohJFKJVH2wd2Yfi1tGR8Knxv8ARM0qT3x2RFQogJktP0QDSB2aMTMZUsmk/FpBrdhDVkEVEv8Yk=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '120.0.6099.291', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-25 09:30:20', '2026-03-25 09:30:20');
INSERT INTO `sys_operation_log` VALUES (45, '8f4df6b4cdc540b2b614fd4dffd5878c', NULL, NULL, '添加保险', '39.107.66.162', NULL, NULL, '/open/insurance/add', 'insurance', 'com.yatai.insurance.controller.OpenInsuranceController', 'addInsurance', 'POST', 'application/json', 1, '{\"userNo\":\"8820260209815809\",\"timestamp\":\"1774410551624\",\"sendNo\":\"1774410551624124762\",\"sign\":\"Pgku37IsrO+0Ufq2ZnBwL3oQ4OrCCt7ruE0/x5889KTbHN4oA4rRMmyKCyc3EX8iPWkB8YUjGlQKgmTeC7v1Wt3u9ODMIpFMNIjz7dV/9/Q5I6YlaGyVKqbxrfiBGyPtH12SsxlVusnJYpPYeItEsTuezttHdy1UbabYlbDWCIA=\",\"signType\":\"RSA\",\"bizCode\":\"001\",\"bizData\":\"ztGiXu39LzP96EEsAt78d0E7qFAdIoiEOMTOfXl+A9q5orx3O/IYh4GMm6LG2OYRHksM8CBQKGdGT9hQDiwIBJmEvEfpkRj1po28aCPcgCHwmIm7hsb0nbDwwp9kZxChyAA/XyHzTbI5SzYs0cE2vdmrOuyDM28myqyeFES4zvWw0UmngAMgzdnK/8+Bo6cElwMgr5yZczBh12N2jn/EzMfVLVG6SINwQRaSyFE1eeDFCZo5uXONuk2df2LIFb50qeghpThSA2nc1SEFtoX/QFoTT4FJd3jNsyd3hqxeJCGf7CL8g8uGsCqAN7dyIQYwE8LGDIfIior24fAqvE9jlQ==\"}', NULL, 1, NULL, NULL, NULL, NULL, NULL, 'Unknown', NULL, 'Unknown', NULL, 'Unknown', 'Unknown', 0, NULL, NULL, '', '2026-03-25 11:49:12', '2026-03-25 11:49:12');
INSERT INTO `sys_operation_log` VALUES (46, '4e43f50785874c959db71b5639446423', NULL, NULL, '用户登录（账号密码）', '113.87.152.51', NULL, NULL, '/user/login/password', 'claw', 'com.claw.system.controller.UserController', 'passwordLogin', 'POST', 'application/json', 1, '{\"username\":\"admin\",\"password\":\"F/5/dBDree9d/KYddRr3X9Vz40vnPlyjCrVVf7GaxYPYEiB16QNWaAf256px6HmTqb9F53UYfIxHuGjpbXl+ZMoscNwDQghCJEWN2nyJFubseR+f95/Hxcpmd4CQRzRXQAStFbbHMasKqcoIgRFIEhYBCqgmuR9H/Rtk7NbHhrk=\"}', NULL, 1, 1, 200, '操作成功', NULL, NULL, 'Chrome', '146.0.0.0', 'Webkit', '537.36', 'Windows 10 or Windows Server 2016', 'Windows', 0, NULL, 'Win64; x64', '', '2026-03-25 13:51:30', '2026-03-25 13:51:30');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户编号',
  `username` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `head_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `sex` tinyint NULL DEFAULT NULL COMMENT '性别：1男 2女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `salt` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '盐',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色：admin/user',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0注销 1正常 2禁用 3冻结 4临时冻结',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT NULL,
  `del_flag` tinyint(1) NULL DEFAULT 0 COMMENT '0正常 1删除',
  `sync_quota_bytes` bigint NULL DEFAULT NULL COMMENT '同步文件总配额（字节），NULL=默认 5GB',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'U001', 'admin', '管理员', '13100000000', '/api/file/q4cT9haOQPREAux8w4rvpcOcSPLqM0hi?Authorization=8d1fa43f-d311-4fbd-8fe5-7c8e69ba9d70', NULL, NULL, NULL, 'fee8824e1e48d707dffc5c30e8a05272c3524f0a49f306a38ba143aaf0edde5a', '2b6de32a9cd43b1bbde802fa759c0b2b364e26093c59639fe5811cd35f94a06e', '2026-03-25 13:51:30', 'admin', 1, '2026-03-24 10:34:53', NULL, '2026-03-24 18:28:54', 1, 0, NULL);
INSERT INTO `user` VALUES (4, '8820260324393288', 'sakura', 'sakura', '13128830520', NULL, NULL, NULL, NULL, 'cf99eac345e2689adac9718189af8f9a0e43960b22a42c9b881b9d7b68a63eaa', 'a29434bbe70b58595fc96d8af712e64622b64830fbdd5941bd6c0185dc16ae89', '2026-03-25 09:30:05', 'user', 1, '2026-03-24 10:55:32', NULL, '2026-03-24 16:01:57', 1, 0, NULL);
INSERT INTO `user` VALUES (5, '8820260324158500', 'zhangsan', '张三', '13636027922', NULL, NULL, NULL, NULL, '55be757260a635e6102bbadd43dfa88bbf288a1c4eedd45986f984d4a4a42538', 'c0262b60d50514e7bc87f8093a87384205c5f82ef2810736c840656f1798153c', '2026-03-25 11:44:43', 'user', 1, '2026-03-24 19:00:10', NULL, '2026-03-24 19:00:10', NULL, 0, NULL);

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1, 1, 1);
INSERT INTO `user_role` VALUES (4, 4, 2);
INSERT INTO `user_role` VALUES (5, 5, 2);

SET FOREIGN_KEY_CHECKS = 1;
