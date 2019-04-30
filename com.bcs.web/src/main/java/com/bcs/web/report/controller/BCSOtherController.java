package com.bcs.web.report.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import com.bcs.web.ui.service.LoadFileUIService;
import com.bcs.web.ui.service.MidToUidUIService;

@Controller
@RequestMapping("/bcs")
public class BCSOtherController extends BCSBaseController {
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSOtherController.class);
	@Autowired
	private MidToUidUIService midToUidUIService;
	@Autowired
	private ExportTextUIService exportTextUIService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/otherPage")
	public String otherPage(
			@CurrentUser CustomUser customUser,  
			HttpServletRequest request, HttpServletResponse response) {
		logger.info("otherPage");
		
		return BcsPageEnum.OtherPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/convertingMidToUidPage")
	public String convertingMidToUidPage(
			@CurrentUser CustomUser customUser,  
			HttpServletRequest request, HttpServletResponse response) {
		logger.info("convertingMidToUidPage");
		
		return BcsPageEnum.ConvertingMidToUidPage.toString();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/admin/uploadMidToUidTemp")
	@ResponseBody
	public ResponseEntity<?> uploadMidToUidTemp(@RequestPart MultipartFile filePart,
			@CurrentUser CustomUser customUser,  
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("uploadMidToUidTemp");

		try{
			if(filePart != null){
				
				String modifyUser = customUser.getAccount();
				logger.info("modifyUser:" + modifyUser);
				
//				Map<String, Object> result = midToUidUIService.uploadMidToUid(filePart, modifyUser, new Date());
				Map<String, Object> result = midToUidUIService.uploadSaveMidToUid(filePart, modifyUser, new Date());
				
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/downloadMidToUid")
	@ResponseBody
	public void downloadMidToUid(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("downloadMidToUid");

		String tempIdStr = request.getParameter("tempId");
		String fromFile = request.getParameter("fromFile");
		
		List<String> midToUids = midToUidUIService.getMidToUidList(tempIdStr);
		
		if(midToUids == null || midToUids.size() == 0){

			throw new Exception("downloadMidToUid Error");
		}
		
		if("true".equals(fromFile)){
			String filePath = midToUids.get(0);
			String fileName = midToUids.get(1);

			LoadFileUIService.loadFileToResponse(filePath, fileName, response);
		}
		else{

			List<List<String>> data = new ArrayList<List<String>>();
			data.add(midToUids);
			
			exportTextUIService.exportResultToText(request, response, "MidToUid", null, data);
		}
	}
}
