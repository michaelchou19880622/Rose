package com.bcs.core.bot.receive.akka.handler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import akka.actor.UntypedActor;

import com.bcs.core.api.service.LineUserStatusService;
import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.db.service.MsgBotReceiveService;
import com.bcs.core.bot.send.service.SendingMsgService;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.interactive.service.InteractiveService;
import com.bcs.core.record.service.CatchRecordOpAddReceive;
import com.bcs.core.record.service.CatchRecordOpBlockedReceive;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.ErrorRecord;

public class ReceivingMsgHandlerMsgReceiveOp extends UntypedActor {

	/** Logger */
	private static Logger logger = Logger.getLogger(ReceivingMsgHandlerMsgReceiveOp.class);

	@Override
	public void onReceive(Object message){
		logger.debug("-------Get Operation Save-------");

		if (message instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) message;
			MsgBotReceive receive = (MsgBotReceive) map.get("Content");
			Long iMsgId = (Long) map.get("iMsgId");
			
			receive.setReferenceId(iMsgId.toString());
			
			if(MsgBotReceive.EVENT_TYPE_FOLLOW.equals(receive.getEventType())){
				
				// incrementCount CatchRecord Op AddReceive
				ApplicationContextProvider.getApplicationContext().getBean(CatchRecordOpAddReceive.class).incrementCount();;
			}
			else if(MsgBotReceive.EVENT_TYPE_UNFOLLOW.equals(receive.getEventType())){
				
				// incrementCount CatchRecord Op Blocked Receive
				ApplicationContextProvider.getApplicationContext().getBean(CatchRecordOpBlockedReceive.class).incrementCount();;
			}
			
			String MID = receive.getSourceId();
			logger.debug("MID:" + MID);

			Date time = new Date();
			
			// 新增好友
			if(MsgBotReceive.EVENT_TYPE_FOLLOW.equals(receive.getEventType())){
				
				LineUserService lineUserService = ApplicationContextProvider.getApplicationContext().getBean(LineUserService.class);
				LineUser lineUser = lineUserService.findByMid(receive.getSourceId());
				if(lineUser != null){
					String isBinded =lineUser.getIsBinded();
					
					if(isBinded == null || isBinded.isEmpty() || isBinded.equals(LineUser.STATUS_UNBIND))
						isBinded = LineUser.STATUS_UNBIND;

					lineUser.setStatus(isBinded);
					lineUser.setModifyTime(time);
					if(lineUser.getCreateTime() == null){
						lineUser.setCreateTime(time);
					}
					lineUserService.save(lineUser);
					
					lineUserService.saveLog(lineUser, lineUser.getMid(), LOG_TARGET_ACTION_TYPE.ACTION_Unbind,  lineUser.getMid());
				}
				else{
					LineUser user = lineUserService.findByMidAndCreateUnbind(MID);
					
					lineUserService.saveLog(user, user.getMid(), LOG_TARGET_ACTION_TYPE.ACTION_Unbind,  user.getMid());
				}
			}
			// 封鎖好友
			else if(MsgBotReceive.EVENT_TYPE_UNFOLLOW.equals(receive.getEventType())){
			    LineUserStatusService lineUserStatusService = ApplicationContextProvider.getApplicationContext().getBean(LineUserStatusService.class);
				LineUserService lineUserService = ApplicationContextProvider.getApplicationContext().getBean(LineUserService.class);
				LineUser lineUser = lineUserService.findByMid(receive.getSourceId());
				if(lineUser != null){
					String oldStatus = lineUser.getStatus();
					lineUser.setStatus(LineUser.STATUS_BLOCK);
					lineUser.setModifyTime(time);
					if(lineUser.getCreateTime() == null){
						lineUser.setCreateTime(time);
					}
					lineUserService.save(lineUser);
					
					if(LineUser.STATUS_BINDED.equals(oldStatus)){
						// BINDED to BLOCK
						lineUserService.saveLog(lineUser, lineUser.getMid(), LOG_TARGET_ACTION_TYPE.ACTION_Binded2Block,  lineUser.getMid());
					}
					else{
						lineUserService.saveLog(lineUser, lineUser.getMid(), LOG_TARGET_ACTION_TYPE.ACTION_Block,  lineUser.getMid());
					}
				}
				else{
					
					LineUser user = new LineUser();
					user.setMid(MID);
					user.setSoureType(receive.getSourceType());
					user.setStatus(LineUser.STATUS_BLOCK);
					user.setModifyTime(time);
					user.setCreateTime(time);
					
					lineUserService.save(user);
					
					lineUserService.saveLog(user, user.getMid(), LOG_TARGET_ACTION_TYPE.ACTION_Block,  user.getMid());
				}
				//call ricart api通知user已封鎖
                lineUserStatusService.callLineUserStatusAPI(MID, LineUser.STATUS_BLOCK, time.getTime());
			}

			try{
		    	// Save Record
		    	ApplicationContextProvider.getApplicationContext().getBean(MsgBotReceiveService.class).bulkPersist(receive);
			} catch (Exception e) {
				logger.error(ErrorRecord.recordError(e));
			}
		}
		
		ReceivingMsgHandlerMaster.taskCount.addAndGet(-1L);
		ReceivingMsgHandlerMaster.updateDate = Calendar.getInstance().getTime();
		logger.debug("-------Get Operation Save End-------");
	}
	
	/**
	 * Handle Msg ReceiveOp
	 * 
	 * @param content
	 * @param ChannelId
	 * @param ApiType
	 * @return iMsgId
	 */
	public static Long handleMsgReceiveOp(MsgBotReceive content, String ChannelId, String ApiType){

		InteractiveService interactiveService = ApplicationContextProvider.getApplicationContext().getBean(InteractiveService.class);
		
		logger.debug("ChannelId:" + ChannelId);
		logger.debug("ApiType:" + ApiType);
		String MID = content.getSourceId();
		logger.debug("MID:" + MID);
		try {
			// 新增好友
			if(MsgBotReceive.EVENT_TYPE_FOLLOW.equals(content.getEventType())){

				// 取得 關鍵字回應 設定
				Long iMsgId = interactiveService.getWelcomeResponse();
				logger.debug("Get Welcome iMsgId:" + iMsgId);

				if(iMsgId != null){
					String replyToken = content.getReplyToken();
					// 傳送 關鍵字回應
					List<MsgDetail> details = interactiveService.getMsgDetails(iMsgId);
			    	ApplicationContextProvider.getApplicationContext().getBean(SendingMsgService.class).sendMatchMessage(replyToken, iMsgId, details, ChannelId, MID, ApiType, null);

			    	// 記錄自動回應 iMsgId
			    	return iMsgId;
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		return -1L;
	}
}
