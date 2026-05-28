package com.claw.common.constant;

/**
 * 用户同步文件（云同步目录）空间配额
 */
public final class SyncQuotaConstants {

    private SyncQuotaConstants() {}

    /** 默认 5 GiB（字节） */
    public static final long DEFAULT_QUOTA_BYTES = 5L * 1024 * 1024 * 1024;

    public static final int MIN_GB = 1;
    public static final int MAX_GB = 2048;

    public static long gbToBytes(int gb) {
        return (long) gb * 1024L * 1024L * 1024L;
    }
}
