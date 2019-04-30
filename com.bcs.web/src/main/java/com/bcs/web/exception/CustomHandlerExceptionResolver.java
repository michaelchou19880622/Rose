package com.bcs.web.exception;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CustomUser;

public class CustomHandlerExceptionResolver implements HandlerExceptionResolver {

	private static Logger logger = Logger.getLogger(CustomHandlerExceptionResolver.class);
	
	private static final String AJAX_HEADER = "XMLHttpRequest";
	
	@Override
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception e) {
		
		// 發生錯誤的日期時間
		String exceptionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		
		CustomUser customUser = getUser();
		String account = (customUser != null ? customUser.getAccount() : "");
		logger.error("============== Exception ==============");
		logger.error("user account : " + account);
		logger.error("request contentType : " + request.getContentType());
		logger.error("request parameter : ");
		for (Map.Entry<String, String[]> entry : request.getParameterMap()
				.entrySet()) {
			logger.error(entry.getKey() + ": " + Arrays.toString(entry.getValue()));
		}
		logger.error("exception message and printStackTrace : ");
		logger.error(ErrorRecord.recordError(e));
		
		// AJAX 請求發生錯誤
		if (isAjaxRequest(request)) {
			logger.error("ajax request error!");
			try {
				response.getWriter().write("System error!");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (IOException e1) {
	    		logger.error(ErrorRecord.recordError(e1));
			}
			return new ModelAndView(); // 回傳空的 ModelAndView 才不會出錯

		// 一般請求發生錯誤
		} else {
			logger.error("normal request error!");
			FlashMap outputFlashMap = RequestContextUtils.getOutputFlashMap(request);
			outputFlashMap.put("exception", e);
			outputFlashMap.put("exceptionDate", exceptionDate);
			ModelAndView modelAndView = new ModelAndView(new RedirectView(request.getContextPath() + "/bcs/index"));
			return modelAndView;
		}
	}

	private CustomUser getUser() {
		Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();

		if (authentication != null) {
			Object principal = authentication.getPrincipal();
			if (principal != null) {
				if (principal instanceof CustomUser) {
					return ((CustomUser) principal);
				}
			}
		}
		return null;
	}

	private boolean isAjaxRequest(HttpServletRequest request) {
		return AJAX_HEADER.equals(request.getHeader("X-Requested-With"));
	}
}
