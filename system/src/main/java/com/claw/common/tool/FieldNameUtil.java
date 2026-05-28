package com.claw.common.tool;

import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * @author Sakura
 * @date 2025/10/30 10:27
 */
public class FieldNameUtil {

    /**
     * 获取字段显示名（优先取Excel注解、其次取ApiModelProperty）
     */
    public static String getDisplayName(Field field) {
        // 取 @ApiModelProperty(value)
        ApiModelProperty api = field.getAnnotation(ApiModelProperty.class);
        if (api != null && StringUtils.isNotBlank(api.value())) {
            return api.value();
        }

        // 默认返回字段名
        return field.getName();
    }
}
