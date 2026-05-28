package com.claw.common.tool;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

public class SnowflakeUtil {

    private static Snowflake snowflake;

    static {
        snowflake = IdUtil.getSnowflake(1, 0);
    }

    public static String getNextId(){
        return String.valueOf(snowflake.nextId());
    }

}
