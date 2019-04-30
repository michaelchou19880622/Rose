package com.bcs.web.ui.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class HttpRequestFilter implements Filter {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(HttpRequestFilter.class);

	FilterConfig filterConfig = null;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		logger.debug("HttpRequestFilter:doFilter");

		chain.doFilter(request, response);
		
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			
			String url = httpRequest.getRequestURI();
			String queryString =httpRequest.getQueryString();
			String method = httpRequest.getMethod();
			
			logger.debug("url:" + url);
			logger.debug("queryString:" + queryString);
			logger.debug("method:" + method);
		}
		
		if(response instanceof HttpServletResponse ){
			HttpServletResponse httpResponse = (HttpServletResponse) response;
	        
	        Collection<String> names = httpResponse.getHeaderNames();
	        if(names != null){
		        for(String name : names){
					logger.debug("Header-name-" + name + "-value-" + httpResponse.getHeaders(name));
		        }
	        }
		}
	}

	@Override
	public void destroy() {
	}

}
