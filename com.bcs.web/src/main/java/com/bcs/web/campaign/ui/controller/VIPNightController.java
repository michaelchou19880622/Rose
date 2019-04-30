package com.bcs.web.campaign.ui.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bcs.core.web.ui.page.enums.BcsPageEnum;

@Controller
@RequestMapping("/campaign")
public class VIPNightController {
	/** Logger */
	private static Logger logger = Logger.getLogger(VIPNightController.class);
	
	@RequestMapping(method = RequestMethod.GET, value = "/VIPNight")
	public String index(HttpServletRequest request, HttpServletResponse response) {
		logger.info("---------- 'VIP Night' index page ----------");
		return BcsPageEnum.CampaignVIPNightIndexPage.toString();
	}
}