-- 用户同步文件空间配额（字节），NULL 表示使用服务端默认 5GB
ALTER TABLE `user`
    ADD COLUMN `sync_quota_bytes` bigint DEFAULT NULL
        COMMENT '同步文件总配额（字节），NULL=默认 5GB'
        AFTER `del_flag`;
