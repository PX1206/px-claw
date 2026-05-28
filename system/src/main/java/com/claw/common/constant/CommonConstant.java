package com.claw.common.constant;

/**
 * 公共常量
 */
public interface CommonConstant {

    /**
     * 默认页码为1
     */
    Long DEFAULT_PAGE_INDEX = 1L;

    /**
     * 默认页大小为20
     */
    Long DEFAULT_PAGE_SIZE = 20L;

    /**
     * 分页总行数名称
     */
    String PAGE_TOTAL_NAME = "total";

    /**
     * 分页数据列表名称
     */
    String PAGE_RECORDS_NAME = "records";

    /**
     * 分页当前页码名称
     */
    String PAGE_INDEX_NAME = "pageIndex";

    /**
     * 请求ID
     */
    String REQUEST_ID = "requestId";

    /**
     * 分页当前页大小名称
     */
    String PAGE_SIZE_NAME = "pageSize";

    /**
     * 登录token
     */
    String ACCESS_TOKEN = "Authorization";

    /**
     * 无法在 Header 携带 Authorization 时（如 GET /file/{code} 用于 img），通过 query 传递同一 token
     */
    String ACCESS_TOKEN_QUERY = "token";

    /**
     * Feigntoken
     */
    String FEIGN_TOKEN = "Feign-Token";

    /**
     * 图片
     */
    String IMAGE = "image";

    /**
     * JPEG
     */
    String JPEG = "JPEG";

    /**
     * base64前缀
     */
    String BASE64_PREFIX = "data:image/png;base64,";

    /**
     * ..
     */
    String SPOT_SPOT = "..";

    /**
     * ../
     */
    String SPOT_SPOT_BACKSLASH = "../";

    /**
     * SpringBootAdmin登录信息
     */
    String ADMIN_LOGIN_SESSION = "adminLoginSession";

    /**
     * 用户浏览器代理
     */
    String USER_AGENT = "User-Agent";

    /**
     * 本机地址IP
     */
    String LOCALHOST_IP = "127.0.0.1";
    /**
     * 本机地址名称
     */
    String LOCALHOST_IP_NAME = "本机地址";
    /**
     * 局域网IP
     */
    String LAN_IP = "192.168";
    /**
     * 局域网名称
     */
    String LAN_IP_NAME = "局域网";

    /**
     * Redis短信验证码key
     */
    String SMS_CODE = "sms_code_";

    /**
     * Redis短信验证码发送次数key
     */
    String SMS_SEND_NUM = "sms-send-num-";

    /**
     * 密码错误次数key
     */
    String PASSWORD_ERROR_NUM = "password-error-num-";

    /**
     * 登录防重放（RSA 明文：13 位时间戳 + 密码）
     */
    String LOGIN_REPLAY = "login-replay_";

    /**
     * Redis保存权限url前缀
     */
    String PERMISSION_URL = "permission_url_";

    /**
     * @description: 保存用户登录token集合
     */
    String USER_TOKEN_SET = "user_token_set_";

    /** 用户登录 token 在 Redis 中的有效期（秒），含桌面客户端与 Web；滑动续期见 LoginInterceptor */
    Long USER_TOKEN_VALIDITY = 2592000L;

    String PLATFORM_USER_TOKEN_SET = "platform_user_token_set_";

    /**
     * Redis保存登录用户商户号前缀
     */
    String MERCHANT_NO_TOKEN = "merchant_no_token_";

    String MERCHANT_ID_TOKEN = "merchant_id_token_";

    String AGENT_ID_TOKEN = "agent_id_token_";


    String AGENT_NO_TOKEN = "agent_no_token_";

    String USER_SHOP_NO_TOKEN = "user_shop_no_token_";

    String ORDER_ADD_CHECK = "order_add_check_";

    String ORDER_REFUND_CHECK = "order_refund_check_";

    String ORDER_QUERY_CHECK = "order_query_check_";

    /**
     * Redis保存设备token前缀
     */
    String MACHINE_TOKEN_SET = "machine_token_set_";

    String AGENT_WITHDRAWAL_APPLY = "agent_withdrawal_apply_";

    String AGENT_WITHDRAWAL_CHECK = "agent_withdrawal_check_";

    String SHOP_BINDING_APPLY = "shop_binding_apply_";

    String[] EXCLUDE_PATH = {
            "/open/**",
            "/user/register", "/user/login/*",
            "/user/updatePassword",
            "/area/getSubAreas/{parentId}",
            "/captcha/getPictureCode",
            "/sms/getCode", "/feign/**", "/file/{code}",
            "/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**", "/doc.html/**",
            "/favicon.ico", "/error"};
}
