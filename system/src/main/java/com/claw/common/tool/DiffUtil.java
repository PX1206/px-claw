package com.claw.common.tool;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对象字段变更对比工具（Java 8）
 * 支持日期格式化、BigDecimal 精度比较、null 显示为空
 */
public class DiffUtil {

    /**
     * 对比两个对象，返回变化字段的 Map
     * key: 中文字段名
     * value: Map<"old", 旧值; "new", 新值>
     */
    public static Map<String, Map<String, Object>> compareFields(Object oldObj, Object newObj) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        if (oldObj == null || newObj == null) {
            return result;
        }

        Class<?> clazz = oldObj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object oldVal = field.get(oldObj);
                Object newVal = field.get(newObj);

                if (isChanged(oldVal, newVal)) {
                    String fieldName = FieldNameUtil.getDisplayName(field); // 获取中文注释

                    Map<String, Object> changeMap = new HashMap<>();
                    changeMap.put("old", oldVal);
                    changeMap.put("new", newVal);

                    result.put(fieldName, changeMap);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * 格式化 Map 变更信息为可读字符串
     */
    public static String formatChanges(Map<String, Map<String, Object>> diff) {
        if (diff.isEmpty()) return "无变化";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return diff.entrySet().stream().map(e -> {
            Object oldVal = e.getValue().get("old");
            Object newVal = e.getValue().get("new");

            String oldStr = formatValue(oldVal, sdf);
            String newStr = formatValue(newVal, sdf);

            return String.format("%s: %s → %s", e.getKey(), oldStr, newStr);
        }).collect(Collectors.joining("; "));
    }

    /**
     * 格式化单个值
     */
    private static String formatValue(Object val, SimpleDateFormat sdf) {
        if (val == null) return "空";
        if (val instanceof Date) return sdf.format((Date) val);
        if (val instanceof BigDecimal) return ((BigDecimal) val).stripTrailingZeros().toPlainString();
        return val.toString();
    }

    /**
     * 判断字段值是否变化
     */
    private static boolean isChanged(Object oldVal, Object newVal) {
        if (oldVal == null && newVal == null) return false;
        if (oldVal == null || newVal == null) return true;

        // BigDecimal 精度比较
        if (oldVal instanceof BigDecimal || newVal instanceof BigDecimal) {
            try {
                BigDecimal oldBD = new BigDecimal(oldVal.toString());
                BigDecimal newBD = new BigDecimal(newVal.toString());
                return oldBD.compareTo(newBD) != 0;
            } catch (NumberFormatException e) {
                return !Objects.equals(oldVal, newVal);
            }
        }

        return !Objects.equals(oldVal, newVal);
    }
}

