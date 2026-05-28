package com.claw.common.tool;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 获取IP地址工具类
 */
public final class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final String IPV6_LOCAL = "::1";  // 推荐用压缩形式

    private IpUtil(){
        throw new AssertionError();
    }

    /**
     * 获取请求用户的IP地址
     * @return
     */
    public static String getRequestIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        return getRequestIp(request);
    }

    /**
     * 获取请求用户的IP地址
     * @param request
     * @return
     */
    public static String getRequestIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");

        // 处理多级代理，获取第一个非 unknown 的有效 IP
        if (ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip)) {
            ip = ip.split(",")[0].trim();  // 取第一个 IP
        } else {
            ip = request.getHeader("X-Real-IP");  // 常见于 Nginx 代理
        }

        // 备用方案：尝试从其他头部获取
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();  // 默认获取远程地址
        }

        // 如果是本地回环地址，则返回本地机器的 IP
        if (IPV6_LOCAL.equals(ip) || "127.0.0.1".equals(ip)) {
            ip = getLocalhostIp();  // 获取本地 IP
        }

        return ip;
    }

    private static String getLocalhostIp() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "127.0.0.1";  // 如果无法获取，则返回回环地址
        }
    }

}
