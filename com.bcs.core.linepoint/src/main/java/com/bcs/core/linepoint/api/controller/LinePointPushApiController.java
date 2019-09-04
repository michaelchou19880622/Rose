package com.bcs.core.linepoint.api.controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.linepoint.api.model.LinePointTaskModel;
import com.bcs.core.linepoint.api.service.LinePointApiService;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.service.LinePointDetailService;
import com.bcs.core.linepoint.db.service.LinePointMainService;
import com.bcs.core.linepoint.scheduler.service.LinePointSimpleSchedulerService;
import com.bcs.core.linepoint.akka.handler.LinePointPushApiActor;
import com.bcs.core.linepoint.akka.service.LinePointPushAkkaService;
import com.bcs.core.utils.CryptUtil;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.RestfulUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;

import com.bcs.core.db.repository.ShareUserRecordRepository;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;

@Controller
@RequestMapping("/api")
public class LinePointPushApiController {
	private static Logger logger = Logger.getLogger(LinePointPushApiController.class);
	
	@Autowired
	private LinePointPushAkkaService linePointPushAkkaService;
	@Autowired
	private LinePointDetailService linePointDetailService;
	@Autowired
	private LinePointMainService linePointMainService;
	@Autowired
	private LinePointSimpleSchedulerService linePointSimpleSchedulerService;
//	@Autowired
//	private LinePointApiService linePointApiService;
	
//	public static void main(String[] args) {
//		String secret = "taishinlinebuscs";
//		String iv = "taishinlinebuscs";
//		String originalToken = "ThisIsALinePointPushApi";
//		try {
//			String result = CryptUtil.Encrypt(CryptUtil.AES, originalToken, secret, iv);
//			System.out.println(result);
//		}catch(Exception e) {
//			System.out.println(e.toString());
//		}
//	}
	
//	public static void main(String[] args) {
//		try {
//			Date dt = new Date();
//			System.out.println(dt.toString());
//			Calendar c = Calendar.getInstance(); 
//			c.setTime(dt); 
//			c.add(Calendar.MINUTE, 12);
//			dt = c.getTime();
//			System.out.println(dt.toString());
//		}catch(Exception e) {
//			System.out.println(e.toString());
//		}
//	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/linePoint/pushLinePoint", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> pushLinePoint(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
			/*@RequestBody List<String> uids,*/ @RequestParam Long eventId) throws IOException {
		try {
				LinePointMain linePointMain = linePointMainService.findOne(eventId);	
			
				//List<String> uids = new JSONArray(){"U58ffae876d497a488111d38a70b5aea0", "U58ffae876d497a488111d38a70b5aea0"};
				
				JSONArray uids = new JSONArray();
				JSONArray amounts = new JSONArray();
				for(int i = 1; i <= 10; i++) {
					uids.put("U58ffae876d497a488111d38a70b5aea0");
					amounts.put(i);
				}
							
				LinePointPushModel linePointPushModel = new LinePointPushModel();
//				linePointPushModel.setAmount(amounts);
//				linePointPushModel.setUid(uids);
				linePointPushModel.setEventId(eventId);
				linePointPushModel.setSource(LinePointPushModel.SOURCE_TYPE_BCS);
				linePointPushModel.setSendTimeType(LinePointPushModel.SEND_TIMING_TYPE_IMMEDIATE);
				linePointPushModel.setTriggerTime(new Date());
				linePointPushAkkaService.tell(linePointPushModel);
				return new ResponseEntity<>("",HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if (e instanceof BcsNoticeException) 
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			else 
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/linePoint/task", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> linePointTask(HttpServletRequest request, HttpServletResponse response, @RequestBody LinePointTaskModel linePointTaskModel) {
		try {
			LinePointDetail linePointDetail = linePointTaskModel.getLinePointDetail();
			logger.info("-------------------- api linePoint task --------------------");
			
//			if(request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
//				return new ResponseEntity<>("{\"result\": 0, \"msg\": \"Missing 'Authorization' header.\"}", HttpStatus.BAD_REQUEST);
//			}else{
//				String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
//				
//				if(authorization.split("key=").length != 2) {
//					return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"Invalid 'Authorization' format.\"}", HttpStatus.UNAUTHORIZED);
//				}
//				
//				String token = authorization.split("key=")[1];
//				String secret = CoreConfigReader.getString(CONFIG_STR.AES_SECRET_KEY, true);
//				String iv = CoreConfigReader.getString(CONFIG_STR.AES_INITIALIZATION_VECTOR, true);
//				String originalToken = CoreConfigReader.getString(CONFIG_STR.API_ORIGINAL_TOKEN, true);
//				logger.info("token:"+token);
//				logger.info("secret:"+secret);
//				logger.info("iv:"+iv);
//				logger.info("originalToken:"+originalToken);
//				
//				String getToken = CryptUtil.Decrypt(CryptUtil.AES, token, secret, iv);
//				logger.info("decryptOriginalToken:"+getToken);
//				
//				if(!getToken.equals(originalToken)) {
//					return new ResponseEntity<>("{\"result\": 0, \"msg\": \"invalid token.\"}", HttpStatus.UNAUTHORIZED);
//				}
//			}
			
			logger.info("[LinePoint API] Request Body:" + linePointDetail);
			linePointDetail.setDetailType(LinePointDetail.DETAIL_TYPE_ISSUE_API);
			linePointDetail.setTriggerTime(new Date());
			
			// ----------- validation --------------
			try {
			    if(linePointDetail.getDepartment() == null) throw new IllegalArgumentException("Request Department is NULL");
			    if(linePointDetail.getServiceName() == null) throw new IllegalArgumentException("Request ServiceName is NULL");
			    if(linePointDetail.getPccCode() == null) throw new IllegalArgumentException("Request PccCode is NULL");
			    if(linePointDetail.getCampName() == null) throw new IllegalArgumentException("Request CampName is NULL");
			    if(linePointDetail.getUid() == null) throw new IllegalArgumentException("Request Uid is NULL");
			    if(linePointDetail.getOrderKey() == null) throw new IllegalArgumentException("Request OrderKey is NULL");
			    if(linePointDetail.getAmount() == null) throw new IllegalArgumentException("Request Amount is NULL");
			    
			    if(linePointDetail.getDepartment().length() > 50) { linePointDetail.setDepartment("NG"); throw new IllegalArgumentException("Request Department out of boundary");}
			    if(linePointDetail.getServiceName().length() > 50) { linePointDetail.setServiceName("NG"); throw new IllegalArgumentException("Request ServiceName out of boundary");}
			    if(linePointDetail.getPccCode().length() > 50) { linePointDetail.setPccCode("NG"); throw new IllegalArgumentException("Request PccCode out of boundary");}
			    if(linePointDetail.getCampName().length() > 50) { linePointDetail.setCampName("NG"); throw new IllegalArgumentException("Request CampName out of boundary");}
			    if(linePointDetail.getUid().length() > 33) { linePointDetail.setUid("NG"); throw new IllegalArgumentException("Request Uid out of boundary");}
			    if(linePointDetail.getCustid().length() > 10) { linePointDetail.setUid("NG"); throw new IllegalArgumentException("Request Custid out of boundary");}
			    if(linePointDetail.getOrderKey().length() > 50) { linePointDetail.setOrderKey("NG"); throw new IllegalArgumentException("Request OrderKey out of boundary");}
			    if(linePointDetail.getAmount() <= 0L || linePointDetail.getAmount() > 2147483647L) { 
			    	linePointDetail.setAmount(0L); throw new IllegalArgumentException("Request Amount out of boundary");
			    }
			}catch(Exception e) {
				linePointDetail.setMessage(e.toString());
				linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
				linePointDetail.setSendTime(new Date());
				linePointDetailService.save(linePointDetail);
				throw e;
			}
			
			String dateString = linePointTaskModel.getDate();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = sdf.parse(dateString);
			linePointSimpleSchedulerService.createLinePointTask(linePointDetail, date); // linePointTaskModel.getDate()
			return new ResponseEntity<>("{}", HttpStatus.OK);
		} catch(Exception e) {
			logger.info("e:"+e.toString());
			if(e instanceof IllegalArgumentException)
				return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
			else if(e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException)
				return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
			return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/linePoint/issue", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> pushLinePoint(HttpServletRequest request, HttpServletResponse response, @RequestBody LinePointDetail linePointDetail) {
		try {
			logger.info("-------------------- api linePoint issue --------------------");

			// ----------- authorization --------------
			if(request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
				return new ResponseEntity<>("{\"result\": 0, \"msg\": \"Missing 'Authorization' header.\"}", HttpStatus.BAD_REQUEST);
			}else{
				String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
				if(authorization.split("key=").length != 2) {
					return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"Invalid 'Authorization' format.\"}", HttpStatus.UNAUTHORIZED);
				}
				
				String token = authorization.split("key=")[1];
				String originalToken = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_ORIGINAL_TOKEN, true);
				
				if(!token.equals(originalToken)) {
					return new ResponseEntity<>("{\"result\": 0, \"msg\": \"invalid token.\"}", HttpStatus.UNAUTHORIZED);
				}
			}
			
//			if(request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
//				return new ResponseEntity<>("{\"result\": 0, \"msg\": \"Missing 'Authorization' header.\"}", HttpStatus.BAD_REQUEST);
//			}else{
//				String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
//				
//				if(authorization.split("key=").length != 2) {
//					return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"Invalid 'Authorization' format.\"}", HttpStatus.UNAUTHORIZED);
//				}
//				
//				String token = authorization.split("key=")[1];
//				String secret = CoreConfigReader.getString(CONFIG_STR.AES_SECRET_KEY, true);
//				String iv = CoreConfigReader.getString(CONFIG_STR.AES_INITIALIZATION_VECTOR, true);
//				String originalToken = CoreConfigReader.getString(CONFIG_STR.API_ORIGINAL_TOKEN, true);
//				logger.info("token:"+token);
//				logger.info("secret:"+secret);
//				logger.info("iv:"+iv);
//				logger.info("originalToken:"+originalToken);
//				
//				String getToken = CryptUtil.Decrypt(CryptUtil.AES, token, secret, iv);
//				logger.info("decryptOriginalToken:"+getToken);
//				
//				if(!getToken.equals(originalToken)) {
//					return new ResponseEntity<>("{\"result\": 0, \"msg\": \"invalid token.\"}", HttpStatus.UNAUTHORIZED);
//				}
//			}
			
			// ----- set main information -----
			String title = linePointDetail.getCampName();
			LinePointMain linePointMain = linePointMainService.findByTitle(title);
			if(linePointMain == null) {
				linePointMain = new LinePointMain();
				linePointMain.setSendType(LinePointMain.SEND_TYPE_API);
				linePointMain.setModifyTime(new Date());
				linePointMain.setTitle(title);
				linePointMain.setTotalCount(0L);
				linePointMain.setTotalAmount(0L);
				linePointMain.setSuccessfulAmount(0L);
				linePointMain.setSuccessfulCount(0L);
				linePointMain.setFailedCount(0L);
				
				linePointMain.setSerialId("API_" + title);
				linePointMain.setModifyUser("API");
				linePointMain.setSerialId("API無此參數");
				linePointMain.setDepartmentFullName(linePointDetail.getDepartment());
				linePointMainService.save(linePointMain);
			}
			logger.info("linePointMain:" + linePointMain);
			
			// ----- set detail information -----
			linePointDetail.setLinePointMainId(linePointMain.getId());
			linePointDetail.setDetailType(LinePointDetail.DETAIL_TYPE_ISSUE_API);
			linePointDetail.setTriggerTime(new Date());
			logger.info("linePointDetail:" + linePointDetail);
			
			// ----------- validation --------------
			try {
			    if(linePointDetail.getDepartment() == null) throw new IllegalArgumentException("Request Department is NULL");
			    if(linePointDetail.getServiceName() == null) throw new IllegalArgumentException("Request ServiceName is NULL");
			    if(linePointDetail.getPccCode() == null) throw new IllegalArgumentException("Request PccCode is NULL");
			    if(linePointDetail.getCampName() == null) throw new IllegalArgumentException("Request CampName is NULL");
			    if(linePointDetail.getUid() == null) throw new IllegalArgumentException("Request Uid is NULL");
			    if(linePointDetail.getOrderKey() == null) throw new IllegalArgumentException("Request OrderKey is NULL");
			    if(linePointDetail.getAmount() == null) throw new IllegalArgumentException("Request Amount is NULL");
			    
			    if(linePointDetail.getDepartment().length() > 50) { linePointDetail.setDepartment("NG"); throw new IllegalArgumentException("Request Department out of boundary");}
			    if(linePointDetail.getServiceName().length() > 50) { linePointDetail.setServiceName("NG"); throw new IllegalArgumentException("Request ServiceName out of boundary");}
			    if(linePointDetail.getPccCode().length() > 50) { linePointDetail.setPccCode("NG"); throw new IllegalArgumentException("Request PccCode out of boundary");}
			    if(linePointDetail.getCampName().length() > 50) { linePointDetail.setCampName("NG"); throw new IllegalArgumentException("Request CampName out of boundary");}
			    if(linePointDetail.getUid().length() > 33) { linePointDetail.setUid("NG"); throw new IllegalArgumentException("Request Uid out of boundary");}
			    if(linePointDetail.getCustid().length() > 10) { linePointDetail.setUid("NG"); throw new IllegalArgumentException("Request Custid out of boundary");}
			    if(linePointDetail.getOrderKey().length() > 50) { linePointDetail.setOrderKey("NG"); throw new IllegalArgumentException("Request OrderKey out of boundary");}
			    if(linePointDetail.getAmount() <= 0L || linePointDetail.getAmount() > 2147483647L) { 
			    	linePointDetail.setAmount(0L); throw new IllegalArgumentException("Request Amount out of boundary");
			    }
			}catch(Exception e) {
				linePointDetail.setMessage(e.toString());
				linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
				linePointDetail.setSendTime(new Date());
				linePointDetailService.save(linePointDetail);
				throw e;
			}
			
			
			// ----------- execute --------------
			// initialize request header
			HttpHeaders headers = new HttpHeaders();
			//String accessToken = linePointApiService.getLinePointChannelAccessToken();
			String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelToken.toString(), true);
			
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
			
			// initialize request body
			JSONObject requestBody = new JSONObject();
			String url = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_MESSAGE_PUSH_URL.toString(), true); // https://api.line.me/pointConnect/v1/issue
		    String clientId = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_CLIENT_ID.toString(), true); // 10052
		    requestBody.put("clientId", clientId);
			requestBody.put("amount", linePointDetail.getAmount());
			requestBody.put("memberId", linePointDetail.getUid());
			requestBody.put("orderKey", linePointDetail.getOrderKey());
			
		    // applicationTime
		    Long applicationTime = System.currentTimeMillis();
		    requestBody.put("applicationTime", applicationTime);
			linePointDetail.setApplicationTime(applicationTime);
			
			// HttpEntity by header and body
			HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
			RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
			JSONObject responseObject = null;
			try {
				responseObject = restfulUtil.execute();
				
				String Id = responseObject.getString("transactionId");
				Long Time = responseObject.getLong("transactionTime");
				String Type = responseObject.getString("transactionType");
				Integer Amount = responseObject.getInt("transactionAmount");
				Integer Balance = responseObject.getInt("balance");
				
				linePointDetail.setTranscationId(Id);
				linePointDetail.setTranscationTime(Time);
				linePointDetail.setTranscationType(Type);
				linePointDetail.setTransactionAmount(Amount);
				linePointDetail.setBalance(Balance);
				linePointDetail.setMessage("SUCCESS");
				linePointDetail.setStatus(LinePointDetail.STATUS_SUCCESS);
				linePointDetail.setSendTime(new Date());
				linePointDetailService.save(linePointDetail);
				
				linePointMain.setSuccessfulAmount(linePointMain.getSuccessfulAmount() + Amount);
				linePointMain.setSuccessfulCount(linePointMain.getSuccessfulCount() + 1L);
				linePointMainService.save(linePointMain);
			} catch (HttpClientErrorException e) {
				logger.info("[LinePointApi] Status code: " + e.getStatusCode());
				logger.info("[LinePointApi] Response body: " + e.getResponseBodyAsString());
				
				linePointDetail.setMessage(e.getResponseBodyAsString());
				linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
				linePointDetail.setSendTime(new Date());
				linePointDetailService.save(linePointDetail);
				
				linePointMain.setFailedCount(linePointMain.getFailedCount() + 1L);
				linePointMainService.save(linePointMain);
				return new ResponseEntity<>(e.getResponseBodyAsString(), HttpStatus.OK); // e.getStatusCode()

			}
			return new ResponseEntity<>(responseObject.toString(), HttpStatus.OK);
		} catch(Exception e) {
			logger.info("e:"+e.toString());
			if(e instanceof IllegalArgumentException)
				return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
			else if(e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException)
				return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
			return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/linePoint/cancel", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> cancelLinePoint(HttpServletRequest request, HttpServletResponse response, @RequestBody LinePointDetail linePointDetail) {
		try {
			logger.info("-------------------- api linePoint cancel --------------------");
			
			if(request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
				return new ResponseEntity<>("{\"result\": 0, \"msg\": \"Missing 'Authorization' header.\"}", HttpStatus.BAD_REQUEST);
			}else{
				String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
				if(authorization.split("key=").length != 2) {
					return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"Invalid 'Authorization' format.\"}", HttpStatus.UNAUTHORIZED);
				}
				
				String token = authorization.split("key=")[1];
				String originalToken = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_ORIGINAL_TOKEN, true);
				
				if(!token.equals(originalToken)) {
					return new ResponseEntity<>("{\"result\": 0, \"msg\": \"invalid token.\"}", HttpStatus.UNAUTHORIZED);
				}
			}
			
//			if(request.getHeader(HttpHeaders.AUTHORIZATION) == null) {
//				return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"Missing 'Authorization' header.\"}", HttpStatus.BAD_REQUEST);
//			} else {
//				String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
//				
//				if(authorization.split("key=").length != 2) {
//					return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"Invalid 'Authorization' format.\"}", HttpStatus.UNAUTHORIZED);
//				}
//				
//				String token = authorization.split("key=")[1];
//				String secret = CoreConfigReader.getString(CONFIG_STR.AES_SECRET_KEY, true);
//				String iv = CoreConfigReader.getString(CONFIG_STR.AES_INITIALIZATION_VECTOR, true);
//				String originalToken = CoreConfigReader.getString(CONFIG_STR.API_ORIGINAL_TOKEN, true);
//				logger.info("token:"+token);
//				logger.info("secret:"+secret);
//				logger.info("iv:"+iv);
//				logger.info("originalToken:"+originalToken);
//				
//				String getToken = CryptUtil.Decrypt(CryptUtil.AES, token, secret, iv);
//				logger.info("decryptOriginalToken:"+getToken);
//				
//				if(!getToken.equals(originalToken)) {
//					return new ResponseEntity<>("{\"result\": 0, \"msg\": \"invalid token.\"}", HttpStatus.UNAUTHORIZED);
//				}
//			}
			
			logger.info("[LinePoint API] Request Body:" + linePointDetail);
			linePointDetail.setDetailType(LinePointDetail.DETAIL_TYPE_CANCEL_API);
			linePointDetail.setTriggerTime(new Date());
			
			// ----------- validation --------------
			try {
			    if(linePointDetail.getDepartment() == null) throw new IllegalArgumentException("Request Department is NULL");
			    if(linePointDetail.getServiceName() == null) throw new IllegalArgumentException("Request ServiceName is NULL");
			    if(linePointDetail.getPccCode() == null) throw new IllegalArgumentException("Request PccCode is NULL");
			    if(linePointDetail.getCampName() == null) throw new IllegalArgumentException("Request CampName is NULL");
			    if(linePointDetail.getUid() == null) throw new IllegalArgumentException("Request Uid is NULL");
			    if(linePointDetail.getOrderKey() == null) throw new IllegalArgumentException("Request OrderKey is NULL");
			    if(linePointDetail.getCancelTranscationId() == null) throw new IllegalArgumentException("Request CancelTranscationId is NULL");
			    
			    if(linePointDetail.getDepartment().length() > 50) { linePointDetail.setDepartment("NG"); throw new IllegalArgumentException("Request Department out of boundary");}
			    if(linePointDetail.getServiceName().length() > 50) { linePointDetail.setServiceName("NG"); throw new IllegalArgumentException("Request ServiceName out of boundary");}
			    if(linePointDetail.getPccCode().length() > 50) { linePointDetail.setPccCode("NG"); throw new IllegalArgumentException("Request PccCode out of boundary");}
			    if(linePointDetail.getCampName().length() > 50) { linePointDetail.setCampName("NG"); throw new IllegalArgumentException("Request CampName out of boundary");}
			    if(linePointDetail.getUid().length() > 33) { linePointDetail.setUid("NG"); throw new IllegalArgumentException("Request Uid out of boundary");}
			    if(linePointDetail.getCustid().length() > 10) { linePointDetail.setUid("NG"); throw new IllegalArgumentException("Request Custid out of boundary");}
			    if(linePointDetail.getOrderKey().length() > 50) { linePointDetail.setOrderKey("NG"); throw new IllegalArgumentException("Request OrderKey out of boundary");}
			    if(linePointDetail.getCancelTranscationId().length() > 15) { linePointDetail.setCancelTranscationId("NG"); throw new IllegalArgumentException("Request CancelTranscationId out of boundary");}
			    
			    if(linePointDetail.getNote() != null) {
			    	if(linePointDetail.getNote().length() > 50) { linePointDetail.setNote("NG"); throw new IllegalArgumentException("Request Note out of boundary");}
			    }
			    if(linePointDetail.getAmount() != null) {
				    if(linePointDetail.getAmount() <= 0L || linePointDetail.getAmount() > 2147483647L) { 
				    	linePointDetail.setAmount(0L); throw new IllegalArgumentException("Request Amount out of boundary");
				    }
			    }
			    
			}catch(Exception e) {
				linePointDetail.setMessage(e.toString());
				linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
				linePointDetail.setSendTime(new Date());
				linePointDetailService.save(linePointDetail);
				throw e;
			}
			
			// ---------------------------------------
			// initialize request header
			HttpHeaders headers = new HttpHeaders();
			//String accessToken = linePointApiService.getLinePointChannelAccessToken();
			String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelToken.toString(), true);
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
			
			// initialize request body
			JSONObject requestBody = new JSONObject();
			String url = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_MESSAGE_CANCEL_URL.toString(), true); // https://api.line.me/pointConnect/v1/issue
			url = url.replace("{transactionId}", linePointDetail.getCancelTranscationId());
			String clientId = CoreConfigReader.getString(CONFIG_STR.LINE_POINT_API_CLIENT_ID.toString(), true); // 10052
		    requestBody.put("clientId", clientId);
		    requestBody.put("memberId", linePointDetail.getUid());
			requestBody.put("orderKey", linePointDetail.getOrderKey());
		    if(linePointDetail.getAmount() != null) {
		    	requestBody.put("amount", linePointDetail.getAmount());
		    }
		    if(linePointDetail.getNote() != null) {
		    	requestBody.put("note", linePointDetail.getNote());
		    }
		    
			// HttpEntity by header and body
			HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
			RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
			JSONObject responseObject = null;
			try {
				responseObject = restfulUtil.execute();
				
				String Id = responseObject.getString("transactionId");
				Long Time = responseObject.getLong("transactionTime");
				String Type = responseObject.getString("transactionType");
				Integer cancelledAmount = responseObject.getInt("cancelledAmount");					
				Integer remainingAmount = responseObject.getInt("remainingAmount");					
				Integer Balance = responseObject.getInt("balance");

				linePointDetail.setTranscationId(Id);
				linePointDetail.setTranscationTime(Time);
				linePointDetail.setTranscationType(Type);
				linePointDetail.setCancelledAmount(cancelledAmount);
				linePointDetail.setRemainingAmount(remainingAmount);
				linePointDetail.setAmount(new Long(remainingAmount)); // amount = remainingAmount
				linePointDetail.setBalance(Balance);
				linePointDetail.setMessage("SUCCESS");
				linePointDetail.setStatus(LinePointDetail.STATUS_SUCCESS);
				linePointDetail.setSendTime(new Date());
				linePointDetailService.save(linePointDetail);
				
//				linePointMain.setSuccessfulAmount(linePointMain.getSuccessfulAmount() + Amount);
//				linePointMain.setSuccessfulCount(linePointMain.getSuccessfulCount() - 1L);
//				linePointMainService.save(linePointMain);
			} catch (HttpClientErrorException e) {
				logger.info("[LinePointApi] Status code: " + e.getStatusCode());
				logger.info("[LinePointApi]  Response body: " + e.getResponseBodyAsString());
				
				linePointDetail.setMessage(e.getResponseBodyAsString());
				linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
				linePointDetail.setSendTime(new Date());
				linePointDetailService.save(linePointDetail);
				return new ResponseEntity<>(e.getResponseBodyAsString(), HttpStatus.OK); //e.getStatusCode()
			}
			return new ResponseEntity<>(responseObject.toString(), HttpStatus.OK);
		} catch(Exception e) {
			logger.info("e:"+e.toString());
			if(e instanceof IllegalArgumentException)
				return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
			else if(e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException)
				return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
			return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
//	private String bytesToHex(byte[] hash) {
//		  StringBuffer hexString = new StringBuffer();
//		  for (int i = 0; i < hash.length; i++) {
//		    String hex = Integer.toHexString(0xFF & hash[i]);
//		    if (hex.length() == 1) hexString.append('0');
//		    hexString.append(hex);
//		  }
//		  return hexString.toString();
//		}
	
//	@RequestMapping(method = RequestMethod.POST, value = "/pushtest", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//	public ResponseEntity<?> pushtest( HttpServletRequest request, HttpServletResponse response ) {
//		try {
//			logger.info("-------------------- pushtest --------------------");
////			@CurrentUser CustomUser setUpUser ,
//			String myUid = "Ud167f3b44952f8e940ab85d18084fa29";
//			Integer limit = 10;
//			Integer pointPerPerson = 7;
//			for(int i = 1; i <= limit; i++) {
//				LinePointPushModel pushLinePointModel = new LinePointPushModel();
//				String message = "你獲得：" + pointPerPerson + "個Line Point, 你是第" + i + "//" + limit + "個獲得的人!";
//				logger.info(message);
//				//PushLinePointApiController.validate(myUid, message, pushLinePointModel);
//				AkkaLinePointPushService.tell(pushLinePointModel);
//			}
//			
//			//====================== 寫進資料庫=======================
//			return new ResponseEntity<>("{\"result\": 1, \"msg\": \"Success.\"}", HttpStatus.OK);
//			
//		} catch(Exception e) {
//			if(e instanceof IllegalArgumentException)
//				return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.BAD_REQUEST);
//			else if(e instanceof BadPaddingException || e instanceof IllegalBlockSizeException || e instanceof IllegalArgumentException)
//				return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"invalid token\"}", HttpStatus.UNAUTHORIZED);
//			return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
//		}}
//
//	@RequestMapping(method = RequestMethod.POST, value = "/message/test", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//	public  ResponseEntity<?> pushMessage1(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("-------------------- test --------------------");
//		return new ResponseEntity<>("{\"result\": 1, \"msg\": \"Success.\"}", HttpStatus.OK);
//
//	}

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
