package com.bcs.core.linepoint.scheduler.handler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.bcs.core.db.entity.ShareCampaign;
import com.bcs.core.db.entity.ShareCampaignClickTracing;
import com.bcs.core.db.entity.ShareUserRecord;
import com.bcs.core.db.service.ShareCampaignClickTracingService;
import com.bcs.core.db.service.ShareCampaignService;
import com.bcs.core.db.service.ShareUserRecordService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.linepoint.api.service.LinePointApiService;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.service.LinePointDetailService;
import com.bcs.core.resource.CoreConfigReader;
//import com.bcs.core.richart.akka.service.LinePointPushAkkaService;
//import com.bcs.core.richart.api.model.LinePointPushModel;
//import com.bcs.core.richart.db.entity.LinePointMain;
//import com.bcs.core.richart.db.entity.LinePointScheduledDetail;
//import com.bcs.core.richart.db.service.LinePointMainService;
//import com.bcs.core.richart.db.service.LinePointScheduledDetailService;
//import com.bcs.core.richart.scheduler.service.MGMService;
import com.bcs.core.spring.ApplicationContextProvider;
//import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
//import com.bcs.core.taishin.circle.service.BillingNoticeService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.RestfulUtil;

public class LinePointTask implements Job {
//	ShareUserRecordService shareUserRecordService = ApplicationContextProvider.getApplicationContext().getBean(ShareUserRecordService.class);
//	ShareCampaignService shareCampaignService = ApplicationContextProvider.getApplicationContext().getBean(ShareCampaignService.class);
//	ShareCampaignClickTracingService shareCampaignClickTracingService = ApplicationContextProvider.getApplicationContext().getBean(ShareCampaignClickTracingService.class);
//	LinePointMainService linePointMainService = ApplicationContextProvider.getApplicationContext().getBean(LinePointMainService.class);
//	LinePointScheduledDetailService linePointScheduledDetailService = ApplicationContextProvider.getApplicationContext().getBean(LinePointScheduledDetailService.class);
	//LinePointPushAkkaService linePointPushAkkaService = ApplicationContextProvider.getApplicationContext().getBean(LinePointPushAkkaService.class);
	LinePointDetailService linePointDetailService = ApplicationContextProvider.getApplicationContext().getBean(LinePointDetailService.class);
	LinePointApiService linePointApiService = ApplicationContextProvider.getApplicationContext().getBean(LinePointApiService.class);
	
	private static Logger logger = Logger.getLogger(LinePointTask.class);
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			logger.info("[LinePointTask execute]");
			
			LinePointDetail linePointDetail = (LinePointDetail) context.getScheduler().getContext().get("LinePointDetail");
			
			// ---------------------------------------
			// initialize request header
			HttpHeaders headers = new HttpHeaders();
			String accessToken = linePointApiService.getLinePointChannelAccessToken();
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
				logger.info("responseObject:"+responseObject.toString());
				
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
			} catch (HttpClientErrorException e) {
				logger.info("[LinePointApi] Status code: " + e.getStatusCode());
				logger.info("[LinePointApi]  Response body: " + e.getResponseBodyAsString());
				
				linePointDetail.setMessage(e.getResponseBodyAsString());
				linePointDetail.setStatus(LinePointDetail.STATUS_FAIL);
				linePointDetail.setSendTime(new Date());
				linePointDetailService.save(linePointDetail);
			}
		} catch (Exception e) {
			String error = ErrorRecord.recordError(e, false);
			logger.error("MGMTask Error:" + error);
		}
	}
}