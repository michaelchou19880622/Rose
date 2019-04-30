package com.bcs.web.ui.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bcs.core.web.ui.controller.BCSBaseController;

/**
 * CustomHandlerExceptionResolver 無法處理 404，所以仍舊在 web.xml 配置 404 轉向
 * 
 * @author Kevin
 * 
 */
@Controller
public class BCSErrorController extends BCSBaseController {

	@RequestMapping(method = RequestMethod.GET, value = "/404")
	public String go404Page(RedirectAttributes redirectAttributes,
			HttpServletRequest request, 
			HttpServletResponse response) {
		redirectAttributes.addFlashAttribute("error404", true);
		return "redirect:/m/index";
	}
}
