package com.bcs.web.ui.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
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
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.service.LineUserUIService;


@Controller
@RequestMapping("/bcs")
public class BCSLineUserController extends BCSBaseController {
	
	@Autowired
	private LineUserUIService lineUserUIService;
    
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSLineUserController.class);

	@RequestMapping(method = RequestMethod.GET, value = "/admin/lineUserUploadPage")
	public String lineUserUploadPage(
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		logger.info("lineUserUploadPage");
				
		return BcsPageEnum.LineUserUploadPage.toString();
	}
	
	@ControllerLog(description="uploadLineUserList")
	@RequestMapping(method = RequestMethod.POST, value = "/admin/uploadLineUserList")
    @ResponseBody
    public ResponseEntity<?> uploadLineUserList(
    		 HttpServletRequest request, 
    		 HttpServletResponse response,
    		 @CurrentUser CustomUser customUser,  
    		 @RequestPart MultipartFile filePart                    
           ) throws IOException {
        logger.info("uploadLineUserList");

        try{
            if(filePart != null){
                String currentUser = customUser.getAccount();
                
                Map<String, Object> result = lineUserUIService.uploadLineUserList(filePart, currentUser, new Date());
                
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            else{
                throw new Exception("Upload Data Null");
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
	@ControllerLog(description="uploadLineUserListCSVData")
	@RequestMapping(method = RequestMethod.POST, value = "/admin/uploadLineUserListCSVData")
    @ResponseBody
    public ResponseEntity<?> uploadLineUserListCSVData(
    		 HttpServletRequest request, 
             HttpServletResponse response,
             @CurrentUser CustomUser customUser,
             @RequestPart MultipartFile filePart
           ) throws IOException {
        logger.info("uploadLineUserList");

        try{
            if(filePart != null){
                String currentUser = customUser.getAccount();
                
                int userNum = lineUserUIService.uploadLineUserListCSVData(filePart, currentUser, new Date());
                Map<String,Object> result = new HashMap<String,Object>();
                result.put("上傳之使用者數量", userNum);
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            else{
                throw new Exception("Upload Data Null");
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
