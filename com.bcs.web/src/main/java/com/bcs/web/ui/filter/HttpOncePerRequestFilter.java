package com.bcs.web.ui.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

public class HttpOncePerRequestFilter extends OncePerRequestFilter{
	
	/** Logger */
	private static Logger logger = Logger.getLogger(HttpOncePerRequestFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		logger.debug("HttpOncePerRequestFilter:doFilterInternal");

		boolean isResource = false;
		
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			String url = httpRequest.getRequestURI();
			logger.debug("url:" + url);
			if(url.indexOf("getResource") > -1){
				isResource = true;
			}
			
			Cookie[] cookies = httpRequest.getCookies();
			if(cookies != null){
				for(Cookie cookie : cookies){
					logger.debug("cookie:" + cookie.getName() + "-" + cookie.getValue());
				}
			}
		}
		
		if(response instanceof HttpServletResponse ){
			logger.debug("HttpOncePerRequestFilter setHeader:Content-Security-Policy:default-src 'self'");
			HttpServletResponse httpResponse = (HttpServletResponse) response;
//	        httpResponse.addHeader("Content-Security-Policy", "default-src 'self' style-src 'self' 'unsafe-inline';");
			if(!isResource){
		        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
		        httpResponse.addHeader("X-XSS-Protection", "1; mode=block ");
		        httpResponse.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
		        httpResponse.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
		        httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
			}
	        
	        Collection<String> names = httpResponse.getHeaderNames();
	        for(String name : names){
				logger.debug("HttpOncePerRequestFilter Header-name-" + name + "-value-" + httpResponse.getHeaders(name));
	        }
		}

		filterChain.doFilter(request, response);
	}

}
