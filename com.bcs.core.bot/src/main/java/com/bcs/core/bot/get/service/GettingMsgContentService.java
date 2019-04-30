package com.bcs.core.bot.get.service;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import retrofit2.Response;

import com.bcs.core.bot.api.service.LineAccessApiService;
import com.bcs.core.bot.enums.SEND_TYPE;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.service.ContentResourceService;
import com.bcs.core.db.service.UserLiveChatService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.utils.ErrorRecord;

@Service
public class GettingMsgContentService {

	@Autowired
	private ContentResourceService contentResourceService;
	@Autowired
	private UserLiveChatService userLiveChatService;
	/** Logger */
	private static Logger logger = Logger.getLogger(GettingMsgContentService.class);
	
	public ContentResource getImageMessage(String ChannelId, String MID, String ApiType, String receiveMsgId) throws Exception{
		return getImageMessage(ChannelId, MID, ApiType, receiveMsgId, 0);
	}
	
	public ContentResource getImageMessage(String ChannelId, String MID, String ApiType, String receiveMsgId, int retryCount) throws Exception{
			
		// 儲存接收的圖片
		try{
			UserLiveChat userLiveChat = userLiveChatService.findByUID(MID);
			String channelName = userLiveChat!=null ? CONFIG_STR.ManualReply.toString() : CONFIG_STR.AutoReply.toString();
			logger.debug("sendType:" + SEND_TYPE.REPLY_MSG.toString());
			Response<ResponseBody> response = LineAccessApiService.getImageFromLine(ChannelId, channelName, receiveMsgId);

			ResponseBody content = response.body();
			// Check Response Status
			checkStatus(response, MID, receiveMsgId);
				
			//this.saveLog(MID, response, iMsgId, targetId, targetId, LOG_TARGET_ACTION_TYPE.TARGET_InteractiveMsg, LOG_TARGET_ACTION_TYPE.ACTION_SendMatchMessage);
		    ContentResource resource = contentResourceService.uploadFile(content.byteStream(), receiveMsgId, 0L, ContentResource.RESOURCE_TYPE_RECEIVEIMAGE, ContentResource.RESOURCE_TYPE_RECEIVEIMAGE, MID);
			
			return resource;
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			if(retryCount < 5){
				this.getImageMessage(ChannelId, MID, ApiType, receiveMsgId, retryCount + 1);
			}
			else{
				// Call Line Fail >= 5
			}
		}
			
		if(retryCount == 0){
			// Update 關鍵字回應 記數
			//ApplicationContextProvider.getApplicationContext().getBean(AkkaCoreService.class).recordMsgs(new MsgInteractiveRecord(iMsgId));
		}
		return null;
		
	}
	
	private void checkStatus(Response<ResponseBody> response, String mid, String receiveMsgId) throws Exception{

		logger.debug("status:" + response.code());
		
		if(response.code() != 200){
			List<Object> content = new ArrayList<Object>();
			content.add(mid);
			content.add(receiveMsgId);
			content.add(response.code());
			content.add(response.body());
			if(response.errorBody() != null){
				content.add(response.errorBody().string());
			}
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_GetFromLineApiStatus, content, mid);
			throw new BcsNoticeException("發送訊息錯誤請洽資訊人員:" + response.code());
		}
	}

}
