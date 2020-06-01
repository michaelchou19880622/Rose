package com.bcs.core.bot.receive.akka.handler;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
//import org.apache.log4j.log;

import com.bcs.core.api.service.model.LocationModel;
import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.db.service.MsgBotReceiveService;
import com.bcs.core.bot.get.service.GettingMsgContentService;
import com.bcs.core.bot.receive.service.LiveChatProcessService;
import com.bcs.core.bot.receive.service.MessageTransmitService;
import com.bcs.core.bot.record.service.CatchRecordReceive;
import com.bcs.core.bot.send.service.SendingMsgService;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgInteractiveMain;
import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.MsgInteractiveMainService;
import com.bcs.core.db.service.UserLiveChatService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.interactive.model.CampaignFlowData;
import com.bcs.core.interactive.service.InteractiveService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.ErrorRecord;

import akka.actor.UntypedActor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceivingMsgHandlerMsgReceive extends UntypedActor {
    @Override
    public void onReceive(Object message) {
        log.debug("-------Get Message Save-------");
        InteractiveService interactiveService = ApplicationContextProvider.getApplicationContext().getBean(InteractiveService.class);
        boolean recordText = CoreConfigReader.getBoolean(CONFIG_STR.RECORD_RECEIVE_AUTORESPONSE_TEXT, true);
        try {
            if (message instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) message;
                MsgBotReceive receive = (MsgBotReceive) map.get("Content");
                Long iMsgId = (Long) map.get("iMsgId");
                receive.setReferenceId(iMsgId.toString());
                LineUserService lineUserService = ApplicationContextProvider.getApplicationContext().getBean(LineUserService.class);
                MsgBotReceiveService msgBotReceiveService = ApplicationContextProvider.getApplicationContext().getBean(MsgBotReceiveService.class);
                LineUser lineUser = null;
                String mid = receive.getSourceId();
                log.debug("MID:" + mid);
                String eventType = receive.getEventType();
                log.debug("eventType:" + eventType);

                /* 
	    		 * 增加防呆機制 : 
		    	 * 在寫入BCS_LINE_USER之前，需要先檢查 BCS_MSG_BOT_RECEIVE 資料表，該 UID 在資料表中是否有紀錄? 且第一筆紀錄的 EVENT_TYPE = follow? 
			     * 如果沒有則 STATUS 狀態寫 SYSADD，有則寫 UNBIND。 */
                List<MsgBotReceive> listMsgBotReceives = msgBotReceiveService.findTopByEventTypeAndSourceIdOrderBySourceId(MsgBotReceive.EVENT_TYPE_FOLLOW, mid);
            
                if (listMsgBotReceives.size() != 0) {
            	    MsgBotReceive msgBotReceive = listMsgBotReceives.get(0);
            	
            	    if (MsgBotReceive.EVENT_TYPE_FOLLOW.equals(msgBotReceive.getEventType())) {
                        lineUser = lineUserService.findByMidAndCreateUnbind(mid);
            	    } else {
                        lineUser = lineUserService.findByMidAndCreateSysAdd(mid);
				    }
            	
                } else if (MsgBotReceive.EVENT_TYPE_MESSAGE.equals(eventType)) {
                    lineUser = lineUserService.findByMidAndCreateSysAdd(mid);
			    }

                //  incrementCount CatchRecord Receive
                ApplicationContextProvider.getApplicationContext().getBean(CatchRecordReceive.class).incrementCount();
                try {
                    String userStatus = lineUser.getStatus();
                    // Set User Status
                    receive.setUserStatus(userStatus);
                    if (!recordText) {
                        if (interactiveService.checkIsAutoResponse(iMsgId, userStatus)) {
                            receive.setText("-RemoveBySystemForSecurity-");
                        }
                    }
                    log.info("map : " + map);
                    log.info("receive : " + receive);
                    // Save Record
                    ApplicationContextProvider.getApplicationContext().getBean(MsgBotReceiveService.class).bulkPersist(receive);
                } catch (Exception e) {
                    log.error(ErrorRecord.recordError(e));
                }
            }
            ReceivingMsgHandlerMaster.taskCount.addAndGet(-1L);
            ReceivingMsgHandlerMaster.updateDate = Calendar.getInstance().getTime();
            log.debug("-------Get Message Save End-------");
        } catch (Exception e) {
        	log.info("An exception detected dering processing a message");
            log.error("Exception", e);
        }
    }

    /**
     * Handle Msg Receive
     *
     * @param content
     * @param ChannelId
     * @param ApiType
     * @return iMsgId
     */
    public static Long handleMsgReceive(MsgBotReceive content, String channelId, String channelName, String apiType) throws Exception {
        LineUserService lineUserService = ApplicationContextProvider.getApplicationContext().getBean(LineUserService.class);
        InteractiveService interactiveService = ApplicationContextProvider.getApplicationContext().getBean(InteractiveService.class);
        LiveChatProcessService liveChatService = ApplicationContextProvider.getApplicationContext().getBean(LiveChatProcessService.class);
        MsgBotReceiveService msgBotReceiveService = ApplicationContextProvider.getApplicationContext().getBean(MsgBotReceiveService.class);
        boolean recordText = CoreConfigReader.getBoolean(CONFIG_STR.RECORD_RECEIVE_AUTORESPONSE_TEXT, true);
        String mid = content.getSourceId();
        String text = content.getText();
        String replyToken = content.getReplyToken();
        String eventType = content.getEventType();
        log.info("Handling an Msg or Postback, UID=" + mid + " channelId=" + channelId + " apiType=" + apiType + " eventType=" + eventType);
        Map<Long, List<MsgDetail>> result;
        LineUser lineUser = null;
        try {
			/* 
			 * 增加防呆機制 : 
			 * 在寫入BCS_LINE_USER之前，需要先檢查 BCS_MSG_BOT_RECEIVE 資料表，該 UID 在資料表中是否有紀錄? 且第一筆紀錄的 EVENT_TYPE = follow? 
			 * 如果沒有則 STATUS 狀態寫 SYSADD，有則寫 UNBIND。 */
            List<MsgBotReceive> listMsgBotReceives = msgBotReceiveService.findTopByEventTypeAndSourceIdOrderBySourceId(MsgBotReceive.EVENT_TYPE_FOLLOW, mid);            
            if (listMsgBotReceives.size() != 0) {
            	MsgBotReceive msgBotReceive = listMsgBotReceives.get(0);
            	
            	if (MsgBotReceive.EVENT_TYPE_FOLLOW.equals(msgBotReceive.getEventType())) {
                    lineUser = lineUserService.findByMidAndCreateUnbind(mid);
            	} else {
                    lineUser = lineUserService.findByMidAndCreateSysAdd(mid);
				}
            	
            } else if (MsgBotReceive.EVENT_TYPE_MESSAGE.equals(eventType)) {
                lineUser = lineUserService.findByMidAndCreateSysAdd(mid);
			}

            /* For keyword process condition */
            String userStatus = LineUser.STATUS_UNBIND;
            if (lineUser != null) {
                userStatus = LineUser.STATUS_SYS_ADD.equals(lineUser.getStatus()) ? LineUser.STATUS_UNBIND : lineUser.getStatus();
            }
            log.info("userStatus=" + userStatus + " lineUser=" + lineUser);
            if (MsgBotReceive.EVENT_TYPE_POSTBACK.equals(eventType)) {
                text = content.getPostbackData();

                if (text.contains("action=")) {
                    String switchAction = text.split("action=")[1];
                    liveChatService.handleSwitchAction(switchAction, channelId, mid, replyToken);
                    return -3L;
                } else if (text.contains("category=")) {
                    String category = text.split("category=")[1];
                    liveChatService.startProcess(channelId, replyToken, mid, category);
                    return -3L;
                } else if (text.contains("waitingAction=")) {
                    String waitingAction = text.split("waitingAction=")[1];
                    liveChatService.handleWaitingAction(waitingAction, mid);
                    return -3L;
                } else if (text.contains("leaveMessageAction=")) {
                    String leaveMessageAction = text.split("leaveMessageAction=")[1];
                    log.info(">>> 使用者選擇：" + leaveMessageAction);
                    liveChatService.handleLeaveMessageAction(leaveMessageAction, channelId, mid, replyToken);
                    return -3L;
                } else if (text.contains("leaveMsgCategory=")) {
                    String leaveMsgCategory = text.split("leaveMsgCategory=")[1];
                    liveChatService.leaveMessage(channelId, replyToken, leaveMsgCategory, mid);
                    return -3L;
                } else {
                    log.info("Not match any!! Post back event data to Pepper gateway!!");
                }
            }

            // 問泰咪段邏輯保留
            if (MsgBotReceive.MESSAGE_TYPE_LOCATION.equals(content.getMsgType())) {
                // 地址
                String address = content.getLocationAddress();
                // 經度
                String longitude = content.getLocationLongitude();
                // 緯度
                String latitude = content.getLocationLatitude();
                ApplicationContextProvider.getApplicationContext().getBean(MessageTransmitService.class).transmitToBOT(channelId, mid, replyToken, new LocationModel(address, longitude, latitude));
                return -2L;
            }

            // 處理活動流程(如果先前有觸發過，否則一律回傳空值)
            result = handleCampaignFlow(mid, text, content.getReplyToken(), channelId, apiType, content.getMsgId(), content.getMsgType());
            log.info("Handled campaign flow, result=" + result + " text=" + text);
            if (StringUtils.isNotBlank(text) || (result.size() > 0)) {
                if (result.size() == 0) {
                    // 取得 關鍵字回應 設定
                    //FIXME Use user status for what?
                    result = interactiveService.getMatchKeyword(mid, userStatus, text);
                    log.info("Got match keyword, resultSize=" + (result == null ? 0 : result.size()) + " result=" + result);
                }

                if (result != null && result.size() == 1) {
                    for (Long iMsgId : result.keySet()) {
                        List<MsgDetail> details = result.get(iMsgId);

                        log.info("Sending a match message, replyToken=" + replyToken+ " iMsgId=" + iMsgId + " channelId=" + channelId + " UID=" + mid + " apiType=" + apiType + " contentMsgId=" + content.getMsgId() + " details=" + details);
                        // 傳送 關鍵字回應
                        ApplicationContextProvider.getApplicationContext().getBean(SendingMsgService.class).sendMatchMessage(replyToken, iMsgId, details, channelId, mid, apiType, content.getMsgId());
                        // 記錄自動回應 iMsgId
                        return iMsgId;
                    }
                    log.info("No match keyword sent!");
                } else {
                    // 紀錄 是否 黑名單選擇
                    //FIXME Use user status for what?
                    Long iMsgIdBlack = interactiveService.getMatchBlackKeywordMsgId(userStatus, text);
                    if (iMsgIdBlack != null) {
                        // Update 關鍵字回應 記數
                        ApplicationContextProvider.getApplicationContext().getBean(MsgInteractiveMainService.class).increaseSendCountByMsgInteractiveId(iMsgIdBlack);
                        log.debug("Match BlackKeyword:" + text + ", iMsgIdBlack:" + iMsgIdBlack);
                        return iMsgIdBlack;
                    } else {
                        // 未設定 預設回應
                        log.info("◎ 不符合任何關鍵字，把訊息丟給Pepper GW處理: " + text);
                        UserLiveChat userLiveChat = ApplicationContextProvider.getApplicationContext().getBean(UserLiveChatService.class).findLeaveMsgUserByUIDAndState(mid, UserLiveChat.LEAVE_MESSAGE);
                        if (userLiveChat == null) {
                            // 20190126 新增參數content.getMsgType()供Pepper GW判斷。
                            // 20190126機器人應答狀況下，如使用者傳圖片，須判斷type後在qa-ajax多帶一個參數給Pepper GW判斷
                            ApplicationContextProvider.getApplicationContext().getBean(MessageTransmitService.class).transmitToBOT(channelId, mid, replyToken, text, content.getMsgId(), content.getMsgType());
                            return -2L;
                        } else {
                            log.info("◎ 使用者 " + mid + " 留言，留言內容為： " + text);
                            liveChatService.leaveMessage(channelId, replyToken, userLiveChat, text);
                            return -2L;
                        }
                    }
                }
            } else {
                //FIXME Use user status for what?
                MsgInteractiveMain main = interactiveService.getAutoResponse(mid, userStatus);
                log.info("main = " + main);
                if (main != null) {
                    Long iMsgId = main.getiMsgId();
                    List<MsgDetail> details = interactiveService.getMsgDetails(iMsgId);
                    // 傳送 關鍵字回應
                    ApplicationContextProvider.getApplicationContext().getBean(SendingMsgService.class).sendMatchMessage(replyToken, iMsgId, details, channelId, mid, apiType, content.getMsgId());
                    // 記錄自動回應 iMsgId
                    return iMsgId;
                } else {
                    // 20190126 新增參數content.getMsgType()供Pepper GW判斷。
                    // 20190126機器人應答狀況下，如使用者傳圖片，須判斷type後在qa-ajax多帶一個參數給Pepper GW判斷
                    ApplicationContextProvider.getApplicationContext().getBean(MessageTransmitService.class).transmitToBOT(channelId, mid, replyToken, text, content.getMsgId(), content.getMsgType());
                    return -2L;
                }
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
        return -1L;
    }

    private static Map<Long, List<MsgDetail>> handleCampaignFlow(String MID, Object msg, String replyToken, String channelId, String apiType, String msgId, String msgType) throws Exception {
        log.debug(("MID=" + MID + ", msg=" + msg + ", replyToken=" + replyToken + ", channelId=" + channelId + ", apiType=" + apiType + ", msgId=" + msgId + ", msgType=" + msgType));
        Map<Long, List<MsgDetail>> iMsgIdAndMsgDetails = new HashMap<Long, List<MsgDetail>>();
        try {
            InteractiveService interactiveService = ApplicationContextProvider.getApplicationContext().getBean(InteractiveService.class);
            // 是否為Image Message
            if (MsgBotReceive.MESSAGE_TYPE_IMAGE.equals(msgType)) {
                ContentResource resource = ApplicationContextProvider.getApplicationContext().getBean(GettingMsgContentService.class).getImageMessage(channelId, MID, apiType, msgId);
                log.info("receiveImageId:" + resource.getResourceId());
                msg = resource;
            }

            CampaignFlowData flowResponse = interactiveService.handleCampaignFlow(MID, msg);
            if (flowResponse != null) {
                MsgInteractiveMain main = flowResponse.getMsgInteractiveMain();
                List<MsgDetail> currentResp = flowResponse.getCurrentResponse();

                if (CollectionUtils.isNotEmpty(currentResp)) {
                    iMsgIdAndMsgDetails.put(main.getiMsgId(), currentResp);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
        return iMsgIdAndMsgDetails;
    }

    public static void trasmitToCustomerService(MsgBotReceive msg, String ChannelId, String ChannelName, String ApiType) throws Exception {
        MessageTransmitService messageTransmitService = ApplicationContextProvider.getApplicationContext().getBean(MessageTransmitService.class);

        log.info(">>> 要傳送給真人客服的訊息：" + msg);
        Map<Long, List<MsgDetail>> errResult = messageTransmitService.transmitToLiveChat(msg);

        // error message response
        if (errResult != null) {
            String replyToken = msg.getReplyToken();
            String MID = msg.getSourceId();
            for (Long iMsgId : errResult.keySet()) {
                List<MsgDetail> details = errResult.get(iMsgId);
                // 傳送關鍵字回應
                ApplicationContextProvider.getApplicationContext().getBean(SendingMsgService.class).sendMatchMessage(replyToken, iMsgId, details, ChannelId, MID, ApiType, msg.getMsgId());
            }
        }
    }
}
