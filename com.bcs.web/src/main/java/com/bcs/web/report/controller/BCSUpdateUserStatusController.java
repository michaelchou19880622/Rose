package com.bcs.web.report.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.ExportTextUIService;
import com.bcs.web.ui.service.UpdateUserStatusUIService;

@Controller
@RequestMapping("/bcs")
public class BCSUpdateUserStatusController extends BCSBaseController {
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSUpdateUserStatusController.class);
	@Autowired
	private UpdateUserStatusUIService updateUserStatusUIService;
	@Autowired
	private ExportTextUIService exportTextUIService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/updateBindedStatusPage")
	public String updateBindedStatusPage(
			@CurrentUser CustomUser customUser,  
			HttpServletRequest request, HttpServletResponse response) {
		logger.info("updateBindedStatusPage");
		
		return BcsPageEnum.UpdateBindedStatusPage.toString();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/admin/updateBindedStatus")
	@ResponseBody
	public ResponseEntity<?> updateBindedStatus(@RequestPart MultipartFile filePart,
			@CurrentUser CustomUser customUser,  
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("updateBindedStatus");

		try{
			if(filePart != null){
				
				String modifyUser = customUser.getAccount();
				logger.info("modifyUser:" + modifyUser);
				
				Map<String, Object> result = updateUserStatusUIService.uploadUserStatus(filePart, modifyUser, new Date());
				
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
			else{
				throw new Exception("Upload Mid SendGroup Null");
			}
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
