package com.adm.xcp.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class IpAddressUtil {

	private static final String[] HEADERS_TO_TRY = { 
		"X-Forwarded-For",
		"Proxy-Client-IP", 
		"WL-Proxy-Client-IP", 
		"HTTP_X_FORWARDED_FOR",
		"HTTP_X_FORWARDED", 
		"HTTP_X_CLUSTER_CLIENT_IP", 
		"HTTP_CLIENT_IP",
		"HTTP_FORWARDED_FOR", 
		"HTTP_FORWARDED", 
		"HTTP_VIA", 
		"REMOTE_ADDR" 
	};

	public static String getRequestIpAddress(HttpServletRequest request) {
		for (String header : HEADERS_TO_TRY) {
			String ip = request.getHeader(header);
			if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
				return ip;
			}
		}
		// Para testar os headers da requisição
		// Map<String, String> headers = getRequestHeadersInMap(request);
		return request.getRemoteAddr();
	}

	public static Map<String, String> getRequestHeadersInMap(HttpServletRequest request) {
		Map<String, String> result = new HashMap<>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String key = (String) headerNames.nextElement();
			String value = request.getHeader(key);
			result.put(key, value);
		}
		return result;
	}
}
