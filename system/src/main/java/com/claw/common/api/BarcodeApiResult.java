package com.claw.common.api;

import com.claw.common.tool.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * REST API 返回结果
 * </p>
 */
@Data
@Accessors(chain = true)
@Builder
@AllArgsConstructor
public class BarcodeApiResult<T> implements Serializable {
    private static final long serialVersionUID = 8004487252556526569L;

    /**
     * 响应码
     */
    private int status;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T result;

    public BarcodeApiResult() {
    }

    public BarcodeApiResult(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public static BarcodeApiResult<Boolean> result(boolean flag) {
        if (flag) {
            return ok();
        }
        return fail();
    }

    public static BarcodeApiResult<Boolean> result(ApiCode apiCode) {
        return result(apiCode, null);
    }

    public static <T> BarcodeApiResult<T> result(ApiCode apiCode, T data) {
        return result(apiCode, null, data);
    }

    public static <T> BarcodeApiResult<T> result(ApiCode apiCode, String message, T data) {
        boolean success = false;
        if (apiCode.getCode() == ApiCode.SUCCESS.getCode()) {
            success = true;
        }
        String apiMessage = apiCode.getMessage();
        if (StringUtil.isBlank(message) && StringUtil.isNotBlank(apiMessage)) {
            message = apiMessage;
        }
        return (BarcodeApiResult<T>) BarcodeApiResult.builder()
                .status(apiCode.getCode())
                .msg(message)
                .result(data)
                .build();
    }

    public static BarcodeApiResult<Boolean> ok() {
        return ok(null);
    }

    public static <T> BarcodeApiResult<T> ok(T data) {
        return result(ApiCode.SUCCESS, data);
    }

    public static <T> BarcodeApiResult<T> ok(T data, String message) {
        return result(ApiCode.SUCCESS, message, data);
    }

    public static BarcodeApiResult<Map<String, Object>> okMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>(1);
        map.put(key, value);
        return ok(map);
    }

    public static BarcodeApiResult<Boolean> fail(ApiCode apiCode) {
        return result(apiCode, null);
    }

    public static BarcodeApiResult<String> fail(String message) {
        return result(ApiCode.FAIL, message, null);

    }

    public static <T> BarcodeApiResult<T> fail(ApiCode apiCode, T data) {
        if (ApiCode.SUCCESS == apiCode) {
            throw new RuntimeException("失败结果状态码不能为" + ApiCode.SUCCESS.getCode());
        }
        return result(apiCode, data);

    }

    public static BarcodeApiResult<String> fail(Integer errorCode, String message) {
        return new BarcodeApiResult<String>()
                .setStatus(errorCode)
                .setMsg(message);
    }

    public static BarcodeApiResult<Map<String, Object>> fail(String key, Object value) {
        Map<String, Object> map = new HashMap<>(1);
        map.put(key, value);
        return result(ApiCode.FAIL, map);
    }

    public static BarcodeApiResult<Boolean> fail() {
        return fail(ApiCode.FAIL);
    }
}
