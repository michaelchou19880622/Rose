package com.bcs.core.bot.receive.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.akka.service.AkkaGatewayService;
import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.api.service.ChatBotApiService;
import com.bcs.core.api.service.LiveChatApiService;
import com.bcs.core.api.service.model.LocationModel;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.service.UserLiveChatService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LIVE_CHAT_WORDING;
import com.bcs.core.smartrobot.service.SwitchIconService;
import com.bcs.core.utils.LiveChatWordingUtil;

@Service
public class MessageTransmitService {
	private static Logger logger = Logger.getLogger(MessageTransmitService.class);

	@Autowired
	private MessageProcessService messageProcessService;
	@Autowired
	private ChatBotApiService chatBotApiService;
	@Autowired
	private LiveChatApiService liveChatApiService;
	@Autowired
	private UserLiveChatService userLiveChatService;
	@Autowired
	private AkkaGatewayService akkaGatewayService;
	@Autowired
	private SwitchIconService switchIconService;

	public void transmitToBOT(String ChannelId, String UID, String replyToken, String question, String msgId, String msgType) throws Exception {
		JSONObject replyMessageObject = new JSONObject();
		JSONArray messageList = new JSONArray();

        /* 先判斷是否為文字訊息，否的話給予預設回覆 >>>20190128捨棄此判斷，將收到的訊息都拋給Pepper GW處理*/
//      if(question != null) {
			Date now = new Date();

			boolean isForwardLiveChat = false, isAttentionKeyword = false, isMarketingKeyword = false;

            logger.info(">>> transmitToBOT.msgType: " + msgType);
            question = StringUtils.isBlank(question)? "":question;
            logger.info(">>> transmitToBOT.question: " + question);

			JSONObject responseObject = chatBotApiService.sendMessage(UID, question, null, msgType);
			UserLiveChat userLiveChat = userLiveChatService.findByUIDAndNotFinishAndNotDiscrad(UID);

			if(!responseObject.has("line"))
				throw new Exception("[Transmit to BOT] Encounter some errors when comuniacting with BOT!");

			isAttentionKeyword = messageProcessService.isAttentionKeyword(responseObject);
			isForwardLiveChat = messageProcessService.isForward(responseObject);
            isMarketingKeyword = messageProcessService.isMarketingKeyword(responseObject);

			if(isAttentionKeyword || isForwardLiveChat || isMarketingKeyword) {	// 判斷是否需要推送轉客服專員的訊息
				Integer switchMessageType = MessageProcessService.SWITCH_MESSAGE_NORMAL;

				if(userLiveChat == null){
					userLiveChat = new UserLiveChat();
					userLiveChat.setUID(UID);
					userLiveChat.setStatus(UserLiveChat.BEGINNING);
					userLiveChat.setCreateTime(now);
					userLiveChat.setModifyTime(now);
					userLiveChatService.save(userLiveChat);
				}

				/* 為行銷詞 */
				if(isMarketingKeyword)
					switchMessageType = MessageProcessService.SWITCH_MESSAGE_MARKETING_KEYWORD;

				/*為敏感詞 */
				if(isAttentionKeyword)
					switchMessageType = MessageProcessService.SWITCH_MESSAGE_KEYWORD;

				if(userLiveChatService.findLeaveMsgUserByUIDAndState(UID, UserLiveChat.COMPLETE) != null)	// 查詢此使用者有沒有留過言
					messageList.put(messageProcessService.generateConfirmSwitchMsg(switchMessageType, true));
				else
					messageList.put(messageProcessService.generateConfirmSwitchMsg(switchMessageType, false));

				replyMessageObject.put("messages", messageList);

				switchIconService.appendSender(CONFIG_STR.AUTO_REPLY.toString(), replyMessageObject);

				replyMessageObject.put("replyToken", replyToken);

				logger.info(">>> Reply Message Object: " + replyMessageObject);

				akkaGatewayService.replyMessage(replyMessageObject);	// 以非同步的方式送出回覆訊息
			} else {
				if(userLiveChat != null && userLiveChat.getStatus().equals(UserLiveChat.BEGINNING)) {
					userLiveChat.setStatus(UserLiveChat.DISCARD);
					userLiveChat.setModifyTime(now);
					userLiveChatService.save(userLiveChat);
				}

				if(responseObject.getJSONObject("line").getJSONArray("messages").length() > 0)
					replyMessageObject.put("messages", responseObject.getJSONObject("line").getJSONArray("messages"));
				else {
					messageList.put(messageProcessService.defaultReplyMessage());
					replyMessageObject.put("messages", messageList);
				}

				replyMessageObject.put("replyToken", replyToken);
			}
//		} else {
//			messageList.put(messageProcessService.defaultReplyMessage());
//			replyMessageObject.put("messages", messageList);
//
//			switchIconService.appendSender(CONFIG_STR.AutoReply.toString(), replyMessageObject);
//
//			replyMessageObject.put("replyToken", replyToken);
//
//			logger.info(">>> Reply Message Object: " + replyMessageObject);
//
//			akkaGatewayService.replyMessage(replyMessageObject);	// 以非同步的方式送出回覆訊息
//		}
		messageProcessService.botReplyMessageRecorder(replyMessageObject, UID);  // 紀錄 BOT 回覆的訊息
	}

	public void transmitToBOT(String ChannelId, String UID, String replyToken, LocationModel location) throws Exception {
		JSONObject replyMessageObject = new JSONObject();
		JSONArray messageList = new JSONArray();
		JSONObject responseObject = chatBotApiService.sendMessage(UID, location);

		if(!responseObject.has("line"))
			throw new Exception("[Transmit to BOT] Encounter some errors when comuniacting with BOT!");

		if(responseObject.getJSONObject("line").getJSONArray("messages").length() > 0)
			replyMessageObject.put("messages", responseObject.getJSONObject("line").getJSONArray("messages"));
		else {
			messageList.put(messageProcessService.defaultReplyMessage());
			replyMessageObject.put("messages", messageList);
		}
		replyMessageObject.put("replyToken", replyToken);

		switchIconService.appendSender(CONFIG_STR.AUTO_REPLY.toString(), replyMessageObject);

		messageProcessService.botReplyMessageRecorder(replyMessageObject, UID);  // 紀錄 BOT 回覆的訊息
	}

	public Map<Long, List<MsgDetail>> transmitToLiveChat(MsgBotReceive msg) throws Exception {
		try{
			String UID = msg.getSourceId();
			//20190126 判斷文字以外的訊息，傳給文字客服人員訊息的類型。
			if(MsgBotReceive.MESSAGE_TYPE_TEXT.equals(msg.getMsgType())){
				liveChatApiService.addMessage(UID, msg.getText()); // 將 User 的訊息傳送給真人客服
			}else {
				String msgTypeChStr = "";
				switch (msg.getMsgType()) {
				case "image" :
					msgTypeChStr="[圖片]";
					break;
				case "video" :
					msgTypeChStr="[影片]";
					break;
				case "audio" :
					msgTypeChStr="[聲音]";
					break;
				case "location" :
					msgTypeChStr="[位置]";
					break;
				case "sticker" :
					msgTypeChStr="[表情貼圖]";
					break;
				case "file" :
					msgTypeChStr="[檔案]";
					break;
				default:
					msgTypeChStr="[其他]";
					break;
				}
				liveChatApiService.addMessage(UID, msgTypeChStr); // 將 User 的訊息類型傳給真人客服
			}
			return null;
		}catch (Exception e) {
			Map<Long, List<MsgDetail>> result = new HashMap<Long, List<MsgDetail>>();
			List<MsgDetail> messageList = new ArrayList<>();
			MsgDetail message = new MsgDetail();
			message.setMsgType(MsgGenerator.MSG_TYPE_TEXT);
			message.setText(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.SEND_MESSAGE_FAIL.toString()));
			messageList.add(message);
			result.put(-3L, messageList);
			return result;
		}
	}
}
