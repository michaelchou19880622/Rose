package com.bcs.core.utils;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class IpUtil {
	/** Logger */
	private static Logger logger = Logger.getLogger(IpUtil.class);
	
	public static String getIP(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (!checkIP(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!checkIP(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!checkIP(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
	
    private static boolean checkIP(String ip) {
        if (ip == null) {
            return false;
        }
        return true;
    }
}
