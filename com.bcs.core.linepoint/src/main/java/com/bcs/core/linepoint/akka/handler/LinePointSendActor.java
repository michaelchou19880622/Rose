package com.bcs.core.linepoint.akka.handler;

import java.security.MessageDigest;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.jcodec.common.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;

//import com.bcs.core.richart.api.model.LinePointPushModel;
//import com.bcs.core.richart.api.model.LinePointResponseModel;
//import com.bcs.core.richart.scheduler.service.LinePointPushMessageTaskService;
//import com.bcs.core.richart.db.entity.LinePointDetail;
//import com.bcs.core.richart.db.entity.LinePointMain;
//import com.bcs.core.richart.db.entity.LinePointPushMessageRecord;
//import com.bcs.core.richart.db.repository.LinePointMainRepository;
//import com.bcs.core.richart.db.service.LinePointDetailService;
//import com.bcs.core.richart.db.service.LinePointPushMessageRecordService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.linepoint.akka.model.LinePointSendModel;
import com.bcs.core.linepoint.web.ui.service.LinePointContentUIService;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.RestfulUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.actor.UntypedActor;

public class LinePointSendActor extends UntypedActor {
	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof LinePointSendModel) {
			LinePointContentUIService richMenuContentUIService = ApplicationContextProvider.getApplicationContext().getBean(LinePointContentUIService.class);
			
			// get push data
			LinePointSendModel pushApiModel = (LinePointSendModel) object;
			String richMenuId = pushApiModel.getRichMenuId();
			JSONArray uids = pushApiModel.getUid();
			
			try {
				// Modify
				for(Integer i = 0; i < uids.length(); i++) {
					String uid = uids.get(i).toString();
					//richMenuContentUIService.callLinkRichMenuToUserAPI(richMenuId, uid);			
				}
			}catch(Exception e){
				Logger.info("LinePointSendActor Error:" + e.getMessage());
				throw e;
			}
		}
	}
}