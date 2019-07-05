package com.bcs.core.linepoint.api.controller;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.akka.service.LinePointPushAkkaService;
import com.bcs.core.utils.ErrorRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.bcs.core.db.repository.ShareUserRecordRepository;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;

@Controller
@RequestMapping("/api")
public class LinePointPushApiController {
	private static Logger logger = Logger.getLogger(LinePointPushApiController.class);
	
	
	@Autowired
	private LinePointPushAkkaService AkkaLinePointPushService;
	
	@RequestMapping(method = RequestMethod.POST, value = "/linePoint/issue", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> pushLinePoint(HttpServletRequest request, HttpServletResponse response, @RequestBody LinePointDetail linePointDetail) {
		try {
			logger.info("-------------------- api linePoint issue --------------------");
			logger.info("linePointDetail:" + linePointDetail);
			
			//linePointDetail.setSource(LinePointDetail.SOURCE_ISSUE_API);
			linePointDetail.setTriggerTime(new Date());
			AkkaLinePointPushService.tell(linePointDetail);
			
//			JSONObject requestBody = new JSONObject(requestBodyString);
//			String myUid = "U58ffae876d497a488111d38a70b5aea0";
//			Integer limit = requestBody.getInt("limit");
//			Integer pointPerPerson = requestBody.getInt("point");
//			
//			for(int i = 1; i <= limit; i++) {
//				LinePointPushModel pushLinePointModel = new LinePointPushModel();
//				String message = "你獲得：" + pointPerPerson + "個Line Point, 你是第" + i + "//" + limit + "個獲得的人!";
//				logger.info(message);
//				//PushLinePointApiController.validate(myUid, message, pushLinePointModel);
//				
//			}
//			LinePointPushModel pushLinePointModel = new LinePointPushModel();
//			pushLinePointModel.setAmount(10);
//			pushLinePointModel.setUid(uid);
//			AkkaLinePointPushService.tell(pushLinePointModel);
			return new ResponseEntity<>("{\"result\": 1, \"msg\": \"Success.\"}", HttpStatus.OK);
			
		} catch(Exception e) {
			if(e instanceof IllegalArgumentException)
				return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
			else if(e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException)
				return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
			
			return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/pushtest", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> pushtest( HttpServletRequest request, HttpServletResponse response ) {
		try {
			logger.info("-------------------- pushtest --------------------");
//			@CurrentUser CustomUser setUpUser ,
			String myUid = "Ud167f3b44952f8e940ab85d18084fa29";
			Integer limit = 10;
			Integer pointPerPerson = 7;
			for(int i = 1; i <= limit; i++) {
				LinePointPushModel pushLinePointModel = new LinePointPushModel();
				String message = "你獲得：" + pointPerPerson + "個Line Point, 你是第" + i + "//" + limit + "個獲得的人!";
				logger.info(message);
				//PushLinePointApiController.validate(myUid, message, pushLinePointModel);
				AkkaLinePointPushService.tell(pushLinePointModel);
			}
			
			//====================== 寫進資料庫=======================
			return new ResponseEntity<>("{\"result\": 1, \"msg\": \"Success.\"}", HttpStatus.OK);
			
		} catch(Exception e) {
			if(e instanceof IllegalArgumentException)
				return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
			else if(e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException)
				return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
			
			return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}}

	@RequestMapping(method = RequestMethod.POST, value = "/message/test", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public  ResponseEntity<?> pushMessage1(HttpServletRequest request, HttpServletResponse response) {
		logger.info("-------------------- test --------------------");
		return new ResponseEntity<>("{\"result\": 1, \"msg\": \"Success.\"}", HttpStatus.OK);

	}

//	  private static Logger logger = Logger.getLogger(LinePointApiController.class);
//	  @Autowired
//	  LinePointMainService linePointMainService;
//	  @Autowired
//	  LinePointSendService linePointSendService;
//	  @Autowired
//	  LinePointDetailService linePointDetailService;
//	  
//	@SuppressWarnings("unchecked")
//	@RequestMapping(method={org.springframework.web.bind.annotation.RequestMethod.POST}, value={"/lpc"}, consumes={"application/json; charset=UTF-8"})
//	  public ResponseEntity<?> sendMSLinePointMsg(HttpServletRequest request, HttpServletResponse responset, @RequestBody String data)
//	    throws Exception
//	  {
//	    logger.info("sendMSLinePointMsg data:" + data);
//	    try
//	    {
//	      JsonNode dataNode = new ObjectMapper().readTree(data);
//	      String msId = dataNode.get("msId").asText();
//	      String uid = dataNode.get("uid").asText();
//	      String t = dataNode.get("t").asText();
//	      String hashInput = dataNode.get("hash").asText();
//	      
//	      logger.info("sendMSLinePointMsg msId:" + msId);
//	      logger.info("sendMSLinePointMsg uid:" + uid);
//	      logger.info("sendMSLinePointMsg t:" + t);
//	      logger.info("sendMSLinePointMsg hashInput:" + hashInput);
//	     
//	      LinePointApiService linepoint = new LinePointApiService();
//	      
//	      // get ClientId & ChannelToken
//	      String clientId = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_CLIENT_ID);
//	      String channelId = CONFIG_STR.Default.toString();
//	      logger.info("sendMSLinePointMsg channelId:" + channelId);
//	      String channelToken = CoreConfigReader.getString(channelId, CONFIG_STR.ChannelToken.toString(), true);
//	      logger.info("sendMSLinePointMsg channelToken:" + channelToken);
//	      
//	      // get OrderKay
//	      String prefix = CoreConfigReader.getString(CONFIG_STR.HASH_PREFIX, true);
//	      String suffix = CoreConfigReader.getString(CONFIG_STR.HASH_SUFFIX, true);
//	      Date hashTime = new Date();
//	      String hashStr = prefix + uid + hashTime.getTime() + msId + suffix;
//	      String hash = DigestUtils.md5Hex(hashStr);
//	      
//	      MessageDigest salt = MessageDigest.getInstance("SHA-256");
//	      //salt.update(UUID.randomUUID().toString().getBytes("UTF-8"));
//	      salt.update(hash.toString().getBytes("UTF-8"));
//	      String orderKey = bytesToHex(salt.digest()).substring(0, 45);
//	      logger.info("sendMSLinePointMsg orderKey:" + orderKey);
//	      
//	      // get EventId
//	      LinePointMain linePointMain = this.linePointMainService.findBySerialId(msId.substring(0, 4));
//	      
//	      // get Amount
//	      Long amount = 0L;
//	      amount = linePointMain.getAmount();
//	      logger.info("sendMSLinePointMsg amount:" + amount);
//	      
//	      // get UIDs
//	      List<LinePointSend> uids = this.linePointSendService.findByMsgLpIdAndUidAndStatus(linePointMain.getId(), uid , "1");
//	      List<LinePointSend> sends = this.linePointSendService.findByMsgLpIdAndStatus(linePointMain.getId(), "1");
//	      List<LinePointDetail> linePointDetail = this.linePointDetailService.findBySerialIdAndEmptyUid(msId);
//	      
//	      
//	      logger.info("sendMSLinePointMsg Sends Count:" + sends.size());
//	      ObjectNode result = new ObjectMapper().createObjectNode();
//
//	      
//	      LinePointSend successSend = new LinePointSend();
//	      successSend.setMsgLpId(linePointMain.getId());
//	      successSend.setSendTime(new Date());
//	      successSend.setResponseCode(200);
//	      successSend.setUid(uid);
//	      successSend.setSource("API");
//	      
//	      if (linePointDetail.size() == 0)
//	      {
//	    	/*
//	        result.put("error", "C003");
//	        result.put("message", "VALIDATION_USED_SERIAL_ID");
//	        result.put("details", "序號已使用");
//	        successSend.setStatus("9");
//	        */
//	        result.put("error", "C004");
//	        result.put("message", "VALIDATION_REPEAT");
//	        result.put("details", "序號已使用");
//	        successSend.setStatus("9");    	  
//	      }
//	      else if (sends.size() >= linePointMain.getAmount())
//	      {
//	    	/*
//	        result.put("error", "C001");
//	        result.put("message", "VALIDATION_FULL");
//	        result.put("details", "人數已滿");
//	        successSend.setStatus("9");
//	        */
//	        result.put("error", "C001");
//	        result.put("message", "VALIDATION_FAILED");
//	        result.put("details", "人數已滿");
//	        successSend.setStatus("9");        
//	      }
//	      else if (uids.size() > 0)
//	      {
//	        result.put("error", "C002");
//	        result.put("message", "VALIDATION_REPEAT");
//	        result.put("details", "您已參加過本活動");
//	        successSend.setStatus("9");
//	      }
//	      else
//	      {
//	        result = linepoint.callLinePointAPI(clientId, uid, orderKey, amount, channelToken);
//	        if (result.has("transactionType"))
//	        {
//	          successSend.setStatus("1");
//	          this.linePointDetailService.updateUID(linePointMain.getId(), msId, uid);
//	        }
//	        else
//	        {
//	          successSend.setStatus("9");
//	        }
//	      }
//	      successSend.setDescription(result.toString());
//	      this.linePointSendService.save(successSend);
//	      logger.info("sendMSLinePointMsg result:" + result.toString());
//	      return new ResponseEntity(createResultObjectNode(Integer.valueOf(200), result), HttpStatus.OK);
//	    }
//	    catch (Exception e)
//	    {
//	      logger.error(ErrorRecord.recordError(e));
//	      return new ResponseEntity(createResultObjectNode(Integer.valueOf(500), e.getMessage()), HttpStatus.OK);
//	    }
//	  }
//	  
//	  private String bytesToHex(byte[] hash)
//	  {
//	    StringBuffer hexString = new StringBuffer();
//	    for (int i = 0; i < hash.length; i++)
//	    {
//	      String hex = Integer.toHexString(0xFF & hash[i]);
//	      if (hex.length() == 1) {
//	        hexString.append('0');
//	      }
//	      hexString.append(hex);
//	    }
//	    return hexString.toString();
//	  }
//	  
//	  private ObjectNode createResultObjectNode(Integer status, ObjectNode msg)
//	  {
//	    return msg;
//	  }
//	  
//	  private ObjectNode createResultObjectNode(Integer status, String errMsg)
//	  {
//	    ObjectNode result = new ObjectMapper().createObjectNode();
//	    result.put("error", "C003");
//	    //result.put("message", "系統異常`");
//	    result.put("message", "VALIDATION_FAILED`");
//	    result.put("details", errMsg);
//	    return result;
//	  }
//	  
//	  private Map<String, Object> createResult(Integer status, String msg)
//	  {
//	    Map<String, Object> result = new HashMap();
//	    
//	    result.put("status", status);
//	    result.put("msg", msg);
//	    
//	    return result;
//	  }
}
