package com.bcs.core.bot.receive.akka.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.receive.utils.MsgBotReceiveParser;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.receive.model.ReceivedModelOriginal;
import com.bcs.core.resource.CoreConfigReader;

import akka.actor.UntypedActor;

public class ReceivingMsgHandlerEventType extends UntypedActor {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ReceivingMsgHandlerEventType.class);
    
	public ReceivingMsgHandlerEventType(){
		
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		logger.debug("ReceivingMsgHandlerEventType onReceive");

		if (message instanceof ReceivedModelOriginal) {
			ReceivedModelOriginal original = (ReceivedModelOriginal) message;

			List<MsgBotReceive> list = MsgBotReceiveParser.parseMessage(original.getReceivingMsg(), original.getChannelId());
			
			if (list != null && list.size() > 0) {
				int count = 0;
				Date start = original.getStart();
				for (MsgBotReceive msg : list) {
					
					ReceivingMsgHandlerMaster.taskCount.addAndGet(1L);
					
					String eventType = msg.getEventType();
					logger.debug("eventType:" + eventType);
					
					String channelId = original.getChannelId();
					String channelName = original.getChannelName();
					String apiType = original.getApiType().toString();
					
					String referenceId = "";
					
					if (channelName.equals(CoreConfigReader.getString(CONFIG_STR.MANUALREPLY_CHANNEL_NAME.toString(), true))) {
						if(MsgBotReceive.EVENT_TYPE_MESSAGE.equals(eventType)) {
							/* 將訊息傳送至真人客服 */
							ReceivingMsgHandlerMsgReceive.trasmitToCustomerService(msg, channelId, channelName, apiType);
							
							/*Map<String, Object> map = new HashMap<String, Object>();
							map.put("Target", eventType);
							map.put("Content", msg);
							map.put("ChannelId", channelId);
							map.put("ApiType", apiType);
							// map.put("iMsgId", iMsgId);
							getSender().tell(map, getSelf());*/
						} else {
							/* Other message type */
						}
					} else if (MsgBotReceive.EVENT_TYPE_MESSAGE.equals(eventType) || MsgBotReceive.EVENT_TYPE_POSTBACK.equals(eventType)) {
						logger.debug("-------Get Message-------");
						Long iMsgId = ReceivingMsgHandlerMsgReceive.handleMsgReceive(msg, channelId, channelName, apiType);
						
						referenceId = msg.getMsgId();
						
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("Target", eventType);
						map.put("Content", msg);
						map.put("ChannelId", channelId);
						map.put("ApiType", apiType);
						map.put("iMsgId", iMsgId);
						getSender().tell(map, getSelf());
					} else if (MsgBotReceive.EVENT_TYPE_FOLLOW.equals(eventType) || MsgBotReceive.EVENT_TYPE_UNFOLLOW.equals(eventType)){
						logger.debug("-------Get Operation-------");
						Long iMsgId = ReceivingMsgHandlerMsgReceiveOp.handleMsgReceiveOp(msg, channelId, apiType);
						
						referenceId = msg.getSourceId();
						
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("Target", eventType);
						map.put("Content", msg);
						map.put("ChannelId", channelId);
						map.put("ApiType", apiType);
						map.put("iMsgId", iMsgId);
						getSender().tell(map, getSelf());
					} else {
						// Unknown eventType
					}
					
					SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_ReceivingMsgHandler, LOG_TARGET_ACTION_TYPE.ACTION_HandleMsgReceive, start, 200, msg.toString(), referenceId);
					count++;
					start = new Date();
				}
				
				SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_ReceivingMsgHandler, LOG_TARGET_ACTION_TYPE.ACTION_HandleMsgReceiveAll, original.getStart(), 200, 15000, "count:" + count + "-" + original.getReceivingMsg(), "200-" + count);
			}
		}
		logger.debug("ReceivingMsgHandlerEventType End");
	}
}
