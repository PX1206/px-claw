-- 安装包 SHA-512（Base64），供桌面端 electron-updater 的 latest.yml 使用
ALTER TABLE `install_package`
    ADD COLUMN `sha512` varchar(128) DEFAULT NULL COMMENT 'SHA-512 Base64' AFTER `file_size`;
