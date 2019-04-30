package com.bcs.web.test.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.service.ActionUserCouponService;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.web.ui.controller.BCSMsgSendController;

@Controller
@RequestMapping("/m")
public class mobileTestController {
	private static Logger logger = Logger.getLogger(mobileTestController.class);

	@Autowired
	private ContentCouponService contentCouponService;
	@Autowired
	private ActionUserCouponService actionUserCouponService;
	
	 @Test
	 @RequestMapping(method = RequestMethod.GET, value = "/testCouponCode")
	 public ResponseEntity<?> testCouponCode(
	   HttpServletRequest request, 
	   HttpServletResponse response,
	   @RequestParam String couponId) {
	  
	  try{
		   ContentCoupon contentCoupon = contentCouponService.findOne(couponId);
		   if(!contentCoupon.getCouponTitle().equals("測試優惠券")){
			   return new ResponseEntity<>("不符合測試資格", HttpStatus.INTERNAL_SERVER_ERROR);
		   }
		   String sessionMID = UUID.randomUUID().toString();
		   Date now = new Date();
		   actionUserCouponService.createActionUserCoupon(sessionMID,couponId, now, now);
		   return new ResponseEntity<>(sessionMID, HttpStatus.OK);
	  } catch (Exception e) {
		   logger.error(ErrorRecord.recordError(e));
		   return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	  }
	  
	 }
	 
	 @Test
	 @RequestMapping(method = RequestMethod.GET, value = "/getTestCouponCodeResult")
	 public ResponseEntity<?> getTestCouponCodeResult(
			   HttpServletRequest request, 
			   HttpServletResponse response,
			   @RequestParam String couponId) {
		try{
			Map returnResult = new HashMap<>();
			ContentCoupon contentCoupon = contentCouponService.findOne(couponId);
			if(!contentCoupon.getCouponTitle().equals("測試優惠券")){
				return new ResponseEntity<>("不符合測試資格", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			List<Integer> result = actionUserCouponService.getIsCouponDuplicateResult(couponId);
			for(int i=0;i<result.size();i++){
				if(result.get(i) != 1){
					returnResult.put("壓測結果", "壓測未通過");
					returnResult.put("結果", result);
					return new ResponseEntity<>(returnResult, HttpStatus.OK);
				}
			}
			returnResult.put("壓測結果", "壓測已通過");
			returnResult.put("結果", result);
			return new ResponseEntity<>(returnResult, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
			  
	}
}
