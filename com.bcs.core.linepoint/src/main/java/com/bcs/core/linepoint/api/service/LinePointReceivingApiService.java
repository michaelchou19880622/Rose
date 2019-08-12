package com.bcs.core.linepoint.api.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bcs.core.api.service.model.PostLineResponse;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.HttpClientUtil;
import com.bcs.core.utils.InputStreamUtil;

@Service
public class LinePointReceivingApiService {

	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointReceivingApiService.class);
//	@Autowired
//	private LinePointLineApiService lineRichMenuApiService;
	//@Autowired
	//private RichMenuContentRepository richMenuContentRepository;
	
	// validate
	public boolean richMenuMsgValidate(String receivingMsg) {
		try {
			logger.info("receivingMsg:"+receivingMsg);
			
			JSONObject recivingObject = new JSONObject(receivingMsg);
			logger.info("recivingObject:"+recivingObject.toString());
			
			JSONArray eventsArray = recivingObject.getJSONArray("events");
			logger.info("eventsArray:" + eventsArray.toString());
			
			Object firstEvent =  eventsArray.get(0);
			logger.info("firstEvent:" + firstEvent.toString());
			
			JSONObject firstEventObject = (JSONObject) firstEvent;
			logger.info("firstEventObject:" + firstEventObject.toString());
			
			// source-uid
			JSONObject sourceObject = firstEventObject.getJSONObject("source");
			logger.info("sourceObject:" + sourceObject.toString());
			
			String uid = sourceObject.getString("userId");
			logger.info("uid:" + uid);
			
			// postback-data
			JSONObject postbackObject = firstEventObject.getJSONObject("postback");	
			logger.info("postbackObject" + postbackObject);
			
			String richId = postbackObject.getString("data");
			logger.info("richId:" + richId);
			
			//RichMenuContent richMenuContent = richMenuContentRepository.findOne(richId);
			//logger.info("richMenuContent:" + richMenuContent);
			
			//String richMenuId = richMenuContent.getRichMenuId();
//			logger.info("richMenuId:" + richMenuId);
//			logger.debug("-------richMenuMsgValidate Success-------");
//			
//			callLinkRichMenuToUserAPI(richMenuId, uid);
			return true;
		}catch(Throwable e){
			logger.error(ErrorRecord.recordError(e));
			logger.debug("------- not a richMenuMsg -------");
			return false;
		}
	}
	
	// 設定指定UID的圖文選單API
	public void callLinkRichMenuToUserAPI(String richMenuId, String uid) throws BcsNoticeException  {
		logger.info(" ContentRichMenuUIService callLinkRichMenuToAllUserAPI");
		try{			
			//PostLineResponse result = lineRichMenuApiService.callLinkRichMenuToUserAPI(richMenuId, uid, 0);
//			if(result.getStatus() != 200){
//				throw new Exception(result.getResponseStr());
//			}
//			logger.debug("callLinkRichMenuToUserAPI result:" + result);			
		}catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			throw new BcsNoticeException(e.getMessage());
		}
	}
	
}
