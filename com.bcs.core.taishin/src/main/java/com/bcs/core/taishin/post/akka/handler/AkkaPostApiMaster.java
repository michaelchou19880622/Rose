package com.bcs.core.taishin.post.akka.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.api.model.LogApiModel;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;

import akka.actor.UntypedActor;

public class AkkaPostApiMaster extends UntypedActor{
	private static Logger logger = Logger.getLogger(AkkaPostApiMaster.class);

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof LogApiModel) {
			LogApiModel logApiModel =(LogApiModel) message;
			
			logger.info("LogApiService.excute"+logApiModel);
			Date start = new Date();
			try {
				String apiKey = CoreConfigReader.getString(CONFIG_STR.TAISHIN_LOG_API_KEY.toString(), true);

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
				JSONObject requestBody = new JSONObject();

				Map<String, String> dataMap = new HashMap<String, String>();
				String dataString = ObjectUtil.objectToJsonStr(logApiModel.getData());
				if(dataString.length()>450){
					dataString="資料量過大不顯示";
				}
				dataMap.put("data", dataString );
				dataMap.put("queryString", logApiModel.getQueryString());
				String functionProcess = ObjectUtil.objectToJsonStr(dataMap);

				requestBody.put("SYS_CODE", logApiModel.getSystemCode());
				requestBody.put("USER_ID", logApiModel.getUserId());
				requestBody.put("CLIENT_IP", logApiModel.getClientIp());
				requestBody.put("FN_TYPE", logApiModel.getFunctionType());
				requestBody.put("FN_NAME", logApiModel.getFunctionName());
				requestBody.put("FN_STTS", logApiModel.getFunctionStatus());
				requestBody.put("FN_KEYVALUE", logApiModel.getSensitiveData());
				requestBody.put("FN_PROC", functionProcess);
				logger.info("requestBody:"+requestBody);
				HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
				RestTemplate restTemplate = new RestTemplate();

				String apiUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_LOG_API_URL.toString());
				apiUrl += "?apikey=" + apiKey;

				ResponseEntity<String> logApiResponse = restTemplate.exchange(apiUrl, HttpMethod.POST, httpEntity,String.class);

				String responseBody = logApiResponse.getBody();
				JSONObject responseObject = new JSONObject(responseBody);

				String status = responseObject.get("status").toString();

				 if (!status.equals("200")) {
					 logger.error(responseObject);
					 SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_RichartLogApi,LOG_TARGET_ACTION_TYPE.ACTION_RichartLogAPi_Error,"error", responseObject.toString());
					 SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichartLogApi,LOG_TARGET_ACTION_TYPE.ACTION_RichartLogAPi_Error, start, Integer.parseInt(status), "error", status + "");
				 } else {
					 SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichartLogApi,LOG_TARGET_ACTION_TYPE.ACTION_RichartLogAPi, start, Integer.parseInt(status),responseObject.toString(), status + "");
				 }

			} catch (HttpStatusCodeException exception) {
				String error = ErrorRecord.recordError(exception, false);
				int status = exception.getStatusCode().value();
				logger.error(error);
				SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_RichartLogApi,
						LOG_TARGET_ACTION_TYPE.ACTION_RichartLogAPi_Error, error, exception.getMessage());
				SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_RichartLogApi,
						LOG_TARGET_ACTION_TYPE.ACTION_RichartLogAPi_Error, start, status, error, status + "");
			}
        }
	}	

}
