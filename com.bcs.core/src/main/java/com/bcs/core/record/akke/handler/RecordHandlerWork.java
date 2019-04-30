package com.bcs.core.record.akke.handler;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import akka.actor.UntypedActor;

import com.bcs.core.api.service.LineProfileService;
import com.bcs.core.api.service.LineWebLoginApiService;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.MsgApiSendRecord;
import com.bcs.core.db.entity.MsgSendRecord;
import com.bcs.core.db.entity.SystemLog;
import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.service.ContentReportService;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.MsgApiSendRecordService;
import com.bcs.core.db.service.MsgInteractiveMainService;
import com.bcs.core.db.service.MsgSendMainService;
import com.bcs.core.db.service.MsgSendRecordService;
import com.bcs.core.db.service.SystemLogService;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.log.util.UserTraceLogUtil;
import com.bcs.core.record.akke.model.ClickLinkModel;
import com.bcs.core.record.akke.model.MsgInteractiveRecord;
import com.bcs.core.record.akke.model.MsgSendRecordModel;
import com.bcs.core.record.akke.model.RecordMsg;
import com.bcs.core.record.akke.model.RouterTestModel;
import com.bcs.core.record.akke.model.WebLoginClickLinkModel;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.servlet.service.HttpSessionService;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.ErrorRecord;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RecordHandlerWork  extends UntypedActor {

	/** Logger */
	private static Logger logger = Logger.getLogger(RecordHandlerWork.class);

	@Override
	public void onReceive(Object recordMsgObj){
		logger.debug("RecordHandlerWork onReceive");

		try{
			if (recordMsgObj instanceof RecordMsg) {
				RecordMsg recordMsg = (RecordMsg) recordMsgObj;
				
				Object message =recordMsg.getMsg();
				
				// Click Link Record
				if (message instanceof ClickLinkModel) {
					ClickLinkModel msg = (ClickLinkModel) message;
		
					ApplicationContextProvider.getApplicationContext().getBean(ContentReportService.class).countClickNumber(msg.getLinkId(), msg.getClickTime());
				}
				// Msg Interactive Record
				else if(message instanceof MsgInteractiveRecord) {
					MsgInteractiveRecord msg = (MsgInteractiveRecord) message;
		
					ApplicationContextProvider.getApplicationContext().getBean(MsgInteractiveMainService.class).increaseSendCountByMsgInteractiveId(msg.getiMsgId());
				}
				// Msg Send Record Model
				else if(message instanceof MsgSendRecordModel) {
					MsgSendRecordModel msg = (MsgSendRecordModel) message;
		
					if(msg.getCount() == 1){
						ApplicationContextProvider.getApplicationContext().getBean(MsgSendMainService.class).increaseSendCountByMsgSendId(msg.getiMsgId());
					}
					else if(msg.getCount() > 1){
						ApplicationContextProvider.getApplicationContext().getBean(MsgSendMainService.class).increaseSendCountByMsgSendId(msg.getiMsgId(), msg.getCount());
					}
				}
				// Msg Send Record
				else if(message instanceof MsgSendRecord) {
					MsgSendRecord msg = (MsgSendRecord) message;
		
					ApplicationContextProvider.getApplicationContext().getBean(MsgSendRecordService.class).bulkPersist(msg);
				}
				// Msg Api Send Record
				else if(message instanceof MsgApiSendRecord) {
					MsgApiSendRecord msg = (MsgApiSendRecord) message;
		
					ApplicationContextProvider.getApplicationContext().getBean(MsgApiSendRecordService.class).bulkPersist(msg);
				}
				// Router Test Model
				else if(message instanceof RouterTestModel) {
					RouterTestModel.count.addAndGet(1L);
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {}
					logger.debug("RouterTestModel End");
				}
				// Record SystemLog
				else if (message instanceof SystemLog) {
					SystemLog msg = (SystemLog) message;

					ApplicationContextProvider.getApplicationContext().getBean(SystemLogService.class).bulkPersist(msg);
					
					// Send Error Msg To Line
					if(SystemLog.SYSTEM_LOG_LEVEL_ERROR.equals(msg.getLevel())){
						//SystemLog Error Notice
					}
				}
				// Record UserTraceLog
				else if (message instanceof UserTraceLog) {
					UserTraceLog msg = (UserTraceLog) message;

					ApplicationContextProvider.getApplicationContext().getBean(UserTraceLogService.class).bulkPersist(msg);
				}
				// WebLoginClickLinkModel Record
				else if(message instanceof WebLoginClickLinkModel){
					WebLoginClickLinkModel msg = (WebLoginClickLinkModel) message;

					String ChannelID = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelID.toString(), true);
					String ChannelSecret = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelSecret.toString(), true);

					LineWebLoginApiService lineWebLoginApiService = ApplicationContextProvider.getApplicationContext().getBean(LineWebLoginApiService.class);
					
					ObjectNode result = lineWebLoginApiService.callRetrievingAPI(ChannelID, ChannelSecret, msg.getCode(), UriHelper.getOauthUrl());

					boolean isSuccess = false;
					
					if(result != null && result.get("access_token") != null){
						String access_token = result.get("access_token").asText();
						if(StringUtils.isNotBlank(access_token)){

							ObjectNode getProfile = ApplicationContextProvider.getApplicationContext().getBean(LineProfileService.class).callGetProfileAPI(access_token);

							if(getProfile != null && getProfile.get("userId") != null && StringUtils.isNotBlank(getProfile.get("userId").asText())){
								String sessionMID = getProfile.get("userId").asText();
								
								ContentLink contentLink = msg.getContentLink();
								String linkId = contentLink.getLinkId();
								
								String linkUrl = UriHelper.getLinkUri(linkId, sessionMID);

								ApplicationContextProvider.getApplicationContext().getBean(LineUserService.class).findByMidAndCreateUnbind(sessionMID);
								
								try{
									HttpSession session = ApplicationContextProvider.getApplicationContext().getBean(HttpSessionService.class).getSession(msg.getSessionId());
									if(session != null){
										session.setAttribute("MID", sessionMID);
									}
								}
								catch(Exception e){
									logger.error(ErrorRecord.recordError(e));
								}
								
								UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_ContentLink, LOG_TARGET_ACTION_TYPE.ACTION_ClickLinkWebLogin, sessionMID, linkUrl + "--" + contentLink, linkId + ":WebLogin:" +msg.getState());
								UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_ContentLink, LOG_TARGET_ACTION_TYPE.ACTION_ClickLinkWebLogin_API, sessionMID, result, linkId + ":WebLogin:" +msg.getState());
								UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_ContentLink, LOG_TARGET_ACTION_TYPE.ACTION_ClickLink, sessionMID, contentLink.getLinkUrl() + "--" + contentLink, contentLink.getLinkId());
								
								isSuccess = true;
							}
							else{
								result.put("getProfile", getProfile.toString());
							}
						}
					}

					if(!isSuccess){
						result.put("code", msg.getCode());
						SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_ValidateLoginApi, "SYSTEM", result, msg.getState());
					}
				}
				
				recordMsg.setSuccess(true);
			}
		}
		catch( Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
		
		getSender().tell(recordMsgObj, getSelf());
	}
}
