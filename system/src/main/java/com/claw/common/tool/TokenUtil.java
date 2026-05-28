package com.claw.common.tool;

import com.claw.common.constant.CommonConstant;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Sakura
 * @date 2023/8/10 10:52
 */
public class TokenUtil {

    /**
     * 从请求头或者请求参数中
     *
     * @return
     */
    public static String getToken() {
        return getToken(getRequest());
    }

    /**
     * 从请求头或者请求参数中
     *
     * @param request
     * @return
     */
    public static String getToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        // 从请求头中获取 token（与普通接口一致）
        String token = request.getHeader(CommonConstant.ACCESS_TOKEN);
        if (StringUtil.isBlank(token)) {
            // 兼容无法在 Header 中带 Authorization 的请求（例如 img 加载 /file/{code}）
            token = request.getParameter(CommonConstant.ACCESS_TOKEN_QUERY);
        }
        return token;
    }

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        return servletRequestAttributes == null ? null:servletRequestAttributes.getRequest();
    }
}
