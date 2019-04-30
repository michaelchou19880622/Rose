package com.bcs.web.ui.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.db.service.ContentFlagService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.web.aop.ControllerLog;

/**
 * ContentFlag Controller
 * 
 * @author Kevin
 *
 */
@Controller
@RequestMapping("/bcs")
public class BCSContentFlagController extends BCSBaseController {
	
	private static Logger logger = Logger.getLogger(BCSContentFlagController.class);
	
	@Autowired
	private ContentFlagService contentFlagService;
	
	/**
	 * 查詢自動完成標籤列表
	 * 
	 * @param contentType
	 * @param referenceId
	 * @param request
	 * @param response
	 * @return
	 */
	@ControllerLog(description="查詢自動完成標籤列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getAutocompleteContentFlagList")
	@ResponseBody
	public ResponseEntity<?> getAutocompleteContentFlagList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam(value = "contentType", required = false) String contentType
			) {
		logger.info("getAutocompleteContentFlagList");
		
		try {
			List<String> contentFlagList = contentFlagService.findDistinctFlagValueByContentTypeOrderByFlagValueAsc(contentType);
			return new ResponseEntity<>(contentFlagList, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}
			else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	/**
	 * 查詢指定的 referenceId、contentType 所屬的標籤
	 * 
	 * @param referenceId
	 * @param contentType
	 * @param request
	 * @param response
	 * @return
	 */
	@ControllerLog(description="查詢指定的 referenceId、contentType 所屬的標籤")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getContentFlagList")
	@ResponseBody
	public ResponseEntity<?> getContentFlagList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam(value = "referenceId", required = false) String referenceId,
			@RequestParam(value = "contentType", required = false) String contentType
			) {
		logger.info("getContentFlagList");
		
		try {
			List<String> contentFlagList = contentFlagService.findFlagValueByReferenceIdAndContentTypeOrderByFlagValueAsc(referenceId, contentType);
			return new ResponseEntity<>(contentFlagList, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}
			else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
}
