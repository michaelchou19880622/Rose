package com.bcs.core.utils;

import lombok.experimental.UtilityClass;

import javax.servlet.http.HttpServletRequest;

/**
 * Ip Utils
 *
 * @author ???
 */
@UtilityClass
public class IpUtil {

    private static final String UNKNOW = "unknown";

    /**
     * Get IP Address from HttpServletRequest
     *
     * @param request HttpServletRequest
     * @return IP Address
     */
    public static String getIpAddress(HttpServletRequest request) {

        if (request == null) {
            return null;
        }

        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOW.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
