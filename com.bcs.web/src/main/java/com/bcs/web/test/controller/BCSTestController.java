package com.bcs.web.test.controller;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.akka.service.AkkaCoreService;
import com.bcs.core.bot.receive.akka.handler.ReceivingMsgHandlerMaster;
import com.bcs.core.bot.send.akka.handler.SendingMsgHandlerMaster;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.importdata.ImportDataService;
import com.bcs.core.record.akke.model.RouterTestModel;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.web.ui.controller.BCSMsgSendController;

@Controller
@RequestMapping("/bcs")
public class BCSTestController extends BCSBaseController {
	@Autowired
	private ImportDataService importDataService;
	@Autowired
	private AkkaCoreService akkaCoreService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSMsgSendController.class);
	
//	@RequestMapping(method = RequestMethod.GET, value = "/admin/importLineUserByFileAsync")
//	@ResponseBody
	public ResponseEntity<?> importLineUserByFileAsync(HttpServletRequest request,HttpServletResponse response){
		logger.info("importLineUserByFileAsync");
		
		if(CoreConfigReader.isSystemTypeProduction()){
			
			return new ResponseEntity<>("Fail", HttpStatus.NOT_IMPLEMENTED);
		}
		
		String result = "Success";
		try {
			File file = new File(CoreConfigReader.getString("file.path.initdate") + "/friend_list_1471708768_20160628125343.txt");

			importDataService.importLineUserByFileAsync(file);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}
			else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/testAkkaRouter")
	@ResponseBody
	public ResponseEntity<?> testAkkaRouter(HttpServletRequest request, HttpServletResponse response) {
		logger.info("testAkkaRouter");

		String sizeStr = request.getParameter("size");
		int size = Integer.parseInt(sizeStr);
		
		for(int i = 0; i < size; i++){
			akkaCoreService.recordMsgs(new RouterTestModel());
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}

		return new ResponseEntity<>(RouterTestModel.count, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/resetSendingMsgHandlerTaskCount")
	@ResponseBody
	public ResponseEntity<?> resetSendingMsgHandlerTaskCount(HttpServletRequest request, HttpServletResponse response) {
		logger.info("resetSendingMsgHandlerTaskCount");

		SendingMsgHandlerMaster.taskCount.lazySet(0L);
		
		return new ResponseEntity<>(SendingMsgHandlerMaster.taskCount + "-" + SendingMsgHandlerMaster.updateDate, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/checkSendingMsgHandlerTaskCount")
	@ResponseBody
	public ResponseEntity<?> checkSendingMsgHandlerTaskCount(HttpServletRequest request, HttpServletResponse response) {
		logger.info("checkSendingMsgHandlerTaskCount");

		return new ResponseEntity<>(SendingMsgHandlerMaster.taskCount + "-" + SendingMsgHandlerMaster.updateDate, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/resetReceivingMsgHandlerTaskCount")
	@ResponseBody
	public ResponseEntity<?> resetReceivingMsgHandlerTaskCount(HttpServletRequest request, HttpServletResponse response) {
		logger.info("resetReceivingMsgHandlerTaskCount");

		ReceivingMsgHandlerMaster.taskCount.lazySet(0L);
		
		return new ResponseEntity<>(ReceivingMsgHandlerMaster.taskCount + "-" + ReceivingMsgHandlerMaster.updateDate, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/checkReceivingMsgHandlerTaskCount")
	@ResponseBody
	public ResponseEntity<?> checkReceivingMsgHandlerTaskCount(HttpServletRequest request, HttpServletResponse response) {
		logger.info("checkReceivingMsgHandlerTaskCount");

		return new ResponseEntity<>(ReceivingMsgHandlerMaster.taskCount + "-" + ReceivingMsgHandlerMaster.updateDate, HttpStatus.OK);
	}
}
