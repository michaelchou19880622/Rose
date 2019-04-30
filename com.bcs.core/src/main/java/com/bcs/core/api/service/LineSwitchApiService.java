package com.bcs.core.api.service;

import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.RestfulUtil;

@Service
public class LineSwitchApiService {
	private static Logger logger = Logger.getLogger(LineSwitchApiService.class);

	public String executeSwitch(String destinationId, String UID, String note) throws Exception {
		Date start = new Date();
		try {			
			String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(), "ChannelToken", true);

			logger.info("========== Start switching to another webhook ==========");
			logger.info("-----> Access Token: " + accessToken);
			logger.info("-----> Destination Id: " + destinationId);
			logger.info("-----> UID: " + UID);
			logger.info("-----> Note: " + note);

			/* 設定 request headers */
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

			/* 設定 request body */
			JSONObject requestBody = new JSONObject();
			requestBody.put("userId", UID);
			requestBody.put("destinationId", destinationId);
			requestBody.put("note", note);

			HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
			
			RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, CoreConfigReader.getString(CONFIG_STR.LINE_SWITCH_API_SWITCHER_SWITCH.toString()), httpEntity);

			JSONObject responseObject = restfulUtil.execute();

			logger.info("switchAPI response：" + responseObject);
			SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SwitcherSwitchApi, start, 200, responseObject.toString(), 200 + "");
		} catch (Exception e) {
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SwitcherSwitchApi, error, e.getMessage());
			throw new BcsNoticeException(e.getMessage());
		}
		return "";
	}

}
