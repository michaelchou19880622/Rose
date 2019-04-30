package com.bcs.core.bot.receive.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.api.service.LiveChatApiService;
import com.bcs.core.api.service.model.LiveChatResponse;
import com.bcs.core.api.service.model.LiveChatStartResponse;
import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.service.UserLiveChatService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LIVE_CHAT_WORDING;
import com.bcs.core.exception.LiveChatException;
import com.bcs.core.smartrobot.service.SwitchIconService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.LiveChatWordingUtil;
import com.bcs.core.bot.api.model.SendToBotModel;
import com.bcs.core.bot.api.service.LineAccessApiService;
import com.bcs.core.bot.enums.SEND_TYPE;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.Sender;
import com.linecorp.bot.model.message.TextMessage;

@Service
public class LiveChatProcessService {
	/** Logger */
	private static Logger logger = Logger.getLogger(LiveChatProcessService.class);

	@Autowired
	private LiveChatProcessService liveChatProcessService;
	@Autowired
	private LiveChatApiService liveChatApiService;
	@Autowired
	private UserLiveChatService userLiveChatService;
	@Autowired
	private MessageProcessService messageProcessService;
	@Autowired
	private SwitchIconService switchIconService;

	public void startProcess(String channelId, String replyToken, String UID, String category) throws Exception {
		try {
			List<Message> messageList = new ArrayList<Message>();
			Sender sender;
			UserLiveChat userLiveChat = userLiveChatService.findByUIDAndNotFinishAndNotDiscrad(UID);

			if (userLiveChat != null) {
				String text = null;
				String status = userLiveChat.getStatus();

				switch (status) {
				case UserLiveChat.BEGINNING:
					LiveChatStartResponse result = liveChatApiService.startChat(UID, category); // 為 User 開啟一個新的對話

					int resultStatus = result.getStatus();
					if (resultStatus == LiveChatStartResponse.WAITING) {
						userLiveChat.setChatId(result.getChatId());
						userLiveChat.setHash(result.getHash());
						userLiveChat.setStatus(UserLiveChat.WAITING);
						userLiveChat.setCategory(category);
						userLiveChat.setModifyTime(result.getTimestamp());

						userLiveChatService.save(userLiveChat);

						text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LIVE_CHAT_START.toString());
						sender = switchIconService.generateSenderModel(CONFIG_STR.AutoReply.toString());
						messageList.add(new TextMessage(text, sender));
					} else if (resultStatus == LiveChatStartResponse.NON_OFFICE_HOUR) { // 不在客服服務時間
						userLiveChat.setChatId(result.getChatId());
						userLiveChat.setHash(result.getHash());
						userLiveChat.setCategory(category);

						userLiveChatService.save(userLiveChat);
						
						liveChatApiService.resetChatFlow(userLiveChat.getUID());
						
						messageList.add(messageProcessService.generateNotInOfficeHourMessage());
					}
					break;

				case UserLiveChat.WAITING:// 等待中
					text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.USER_HAS_ALREADY_CHOOSE_CATEGORY.toString());
					
					sender = switchIconService.generateSenderModel(CONFIG_STR.AutoReply.toString());
					messageList.add(new TextMessage(text, sender));
					
					break;

				case UserLiveChat.IN_PROGRESS:// 已轉接客服
					text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.USER_IS_ALREADY_IN_PROGRESS.toString());
					
					sender = switchIconService.generateSenderModel(CONFIG_STR.AutoReply.toString());
					messageList.add(new TextMessage(text, sender));
					
					break;
				}
			} else {
				// 不可以串接客服
				String text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.USER_CANNOT_START.toString());
				sender = switchIconService.generateSenderModel(CONFIG_STR.AutoReply.toString());
				messageList.add(new TextMessage(text, sender));
			}
			
			messageProcessService.replyMessage(channelId, replyToken, messageList, CONFIG_STR.AutoReply.toString());
		} catch (Exception e) {
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
		}
	}

	public void sendChooseCategoryMsg(String channelId, String UID, String replyToken, boolean isLeaveMessage) {
		try {
			Boolean isInprogress = false;
			Message message = null;
			List<Message> sendMsgList = new ArrayList<Message>();
			Sender sender;
			UserLiveChat userLiveChat = userLiveChatService.findByUIDAndNotFinishAndNotDiscrad(UID);

			if (userLiveChat != null) {
				String status = userLiveChat.getStatus();

				switch (status) {
				case UserLiveChat.WAITING:
					sender = switchIconService.generateSenderModel(CONFIG_STR.AutoReply.toString());
					message = new TextMessage(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.USER_IS_ALREADY_WAITING.toString()), sender);
					
					break;
				case UserLiveChat.IN_PROGRESS:
					isInprogress = true;
					sender = switchIconService.generateSenderModel(CONFIG_STR.ManualReply.toString());
					message = new TextMessage(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.USER_IS_ALREADY_IN_PROGRESS.toString()), sender);
					
					break;
				default:
					message = messageProcessService.generateChooseCategoryMsg(isLeaveMessage);
					
					break;
				}
			} else {
				sender = switchIconService.generateSenderModel(CONFIG_STR.AutoReply.toString());
				message = new TextMessage(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.USER_CANNOT_START.toString()), sender);
			}

			sendMsgList.add(message);

			SendToBotModel sendToBotModel = new SendToBotModel();

			sendToBotModel.setChannelId(channelId);
			sendToBotModel.setSendType(SEND_TYPE.REPLY_MSG);
			String channelName = isInprogress == true ? CONFIG_STR.ManualReply.toString() : CONFIG_STR.AutoReply.toString();
			sendToBotModel.setChannelName(channelName);

			ReplyMessage replyMessage = new ReplyMessage(replyToken, sendMsgList);
			sendToBotModel.setReplyMessage(replyMessage);

			LineAccessApiService.sendToLine(sendToBotModel);
		} catch (Exception e) {
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
		}
	}

	public void handleSwitchAction(String type, String channelId, String UID, String replyToken) throws Exception {
		String text = null;
		List<String> textList = null;

		switch (type) {
			case "ManualReply":
				liveChatProcessService.sendChooseCategoryMsg(channelId, UID, replyToken, false);
				break;
	
			case "AutoReply":
				text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.GIVEUP_SWITCH.toString());
				textList = new ArrayList<String>();
	
				textList.add(text);
	
				this.giveUpSwitch(UID);
	
				messageProcessService.replyTextMessage(channelId, CONFIG_STR.AutoReply.toString(), textList, replyToken);
				break;
	
			case "LeaveMessage":
				UserLiveChat userLiveChat = userLiveChatService.findByUIDAndStatus(UID, UserLiveChat.BEGINNING);
				
				if(userLiveChat.getCategory() != null)
					liveChatProcessService.leaveMessage(channelId, replyToken, userLiveChat.getCategory(), UID);
				else
					liveChatProcessService.sendChooseCategoryMsg(channelId, UID, replyToken, true);
				
				break;
		}
	}

	public void handleWaitingAction(String waitingAction, String UID) throws Exception {
		switch (waitingAction) {
		case "keepWaiting":
			this.keepWaiting(UID);
			break;
		case "leaveMessage":
			this.leaveMessageEventHandler(UID, "Waiting", null);
			this.giveUpWaiting(UID);

			List<Message> messageList = new ArrayList<Message>();
			
			Sender sender = switchIconService.generateSenderModel(CONFIG_STR.AutoReply.toString());
			
			messageList.add(new TextMessage(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LEAVE_MESSAGE_INTRO.toString()), sender));
			messageList.add(new TextMessage(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LEAVE_MESSAGE_START.toString()), sender));
			
			messageProcessService.pushMessage(UID, messageList, CONFIG_STR.AutoReply.toString());
			break;
		case "giveUp":
			this.giveUpWaiting(UID);

			messageProcessService.pushTextMsgAsync(UID, LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.GIVEUP_MESSAGE.toString()), CONFIG_STR.AutoReply.toString());
			break;
		}
	}
	
	public void handleLeaveMessageAction(String leaveMessageAction, String channelId, String UID, String replyToken) throws Exception {
		List<String> textList = null;
		UserLiveChat userLiveChat = userLiveChatService.findLeaveMsgUserByUIDAndState(UID, UserLiveChat.CONFIRM);
		
		if(userLiveChat != null) {
			String text = null;
			textList = new ArrayList<String>();
			
			switch(leaveMessageAction) {
				case "confirm":
					LiveChatStartResponse result = liveChatApiService.leaveMessage(UID, userLiveChat);
					
					text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LEAVE_MESSAGE_COMPLETE.toString());
					
					textList.add(text);
					
					userLiveChat.setChatId(result.getChatId());
					userLiveChat.setHash(result.getHash());
					userLiveChat.setLeaveMsgState(UserLiveChat.COMPLETE);
					userLiveChat.setModifyTime(new Date());
					
					if(userLiveChat.getCategory() == null)
						userLiveChat.setCategory(UserLiveChat.CATEGORY_GENERAL);
					
					userLiveChatService.save(userLiveChat);
					
					liveChatApiService.resetChatFlow(userLiveChat.getUID());
					
					break;
				case "reset":
					text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LEAVE_MESSAGE_RESET.toString());
					
					textList.add(text);
					textList.add(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LEAVE_MESSAGE_START.toString()));
					
					userLiveChat.setLeaveMsgState(UserLiveChat.LEAVE_MESSAGE);
					userLiveChat.setMesssage(null);
					userLiveChat.setModifyTime(new Date());
					
					userLiveChatService.save(userLiveChat);
					break;
			}
			
			messageProcessService.replyTextMessage(channelId, CONFIG_STR.AutoReply.toString(), textList, replyToken);
		} else {
			throw new LiveChatException("[LiveChatProcessService] User do not have permission to leave messages.");
		}
	}
	
	public void leaveMessage(String channelId, String replyToken, String category, String UID) throws Exception {
		userLiveChatService.updateLeaveMessageState(UID, UserLiveChat.CONFIRM);	// 清理使用者未完成的留言，以免造成狀態混亂
		
		String text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LEAVE_MESSAGE_INTRO.toString());
		List<String> textList = new ArrayList<String>();

		textList.add(text);
		textList.add(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LEAVE_MESSAGE_START.toString()));

		this.leaveMessageEventHandler(UID, "Switch", category);
		this.giveUpSwitch(UID);

		messageProcessService.replyTextMessage(channelId, CONFIG_STR.AutoReply.toString(), textList, replyToken);
	}

	public void leaveMessage(String channelId, String replyToken, UserLiveChat userLiveChat, String message)
			throws Exception {
		userLiveChat.setMesssage(message);
		userLiveChat.setLeaveMsgState(UserLiveChat.CONFIRM);
		userLiveChat.setModifyTime(new Date());

		userLiveChatService.save(userLiveChat);
		
		List<Message> messageList = new ArrayList<Message>();

		messageList.add(messageProcessService.generateConfirmMessage(message));

		messageProcessService.replyMessage(channelId, replyToken, messageList, CONFIG_STR.AutoReply.toString());
	}

	private void keepWaiting(String UID) throws Exception {
		UserLiveChat userLiveChat = userLiveChatService.findByUIDAndStatus(UID, UserLiveChat.WAITING);

		if (userLiveChat != null) {
			LiveChatResponse result = liveChatApiService.giveUpChat(userLiveChat.getChatId(), userLiveChat.getHash(), 0);

			if (result.getError())
				throw new LiveChatException("[LiveChatProcessService] Encounter error when keep waiting!");
			else
				messageProcessService.pushTextMsgAsync(UID, LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.WAITING_MESSAGE.toString()), CONFIG_STR.AutoReply.toString());
		} else {
			throw new LiveChatException("[LiveChatProcessService] User is not in waiting process!");
		}
	}

	private void giveUpWaiting(String UID) throws Exception {
		UserLiveChat userLiveChat = userLiveChatService.findByUIDAndStatus(UID, UserLiveChat.WAITING);

		if (userLiveChat != null) {
			LiveChatResponse result = liveChatApiService.giveUpChat(userLiveChat.getChatId(), userLiveChat.getHash(), 2);

			if (result.getError())
				throw new LiveChatException("[LiveChatProcessService] Failed to give up waiting process!");
			else {
				this.setDiscardStatus(userLiveChat);
			}
		} else {
			throw new LiveChatException("[LiveChatProcessService] User is not in waiting process!");
		}
	}

	private void giveUpSwitch(String UID) throws Exception {
		UserLiveChat userLiveChat = userLiveChatService.findByUIDAndStatus(UID, UserLiveChat.BEGINNING);

		if (userLiveChat != null) {
			this.setDiscardStatus(userLiveChat);
		} else {
			throw new LiveChatException("[LiveChatProcessService] User did not trigger the switch process!");
		}
	}

	private void setDiscardStatus(UserLiveChat userLiveChat) throws Exception {
		liveChatApiService.resetChatFlow(userLiveChat.getUID()); // 通知碩網大腦，要 reset 錯誤回答的次數
		
		userLiveChat.setStatus(UserLiveChat.DISCARD);
		userLiveChat.setModifyTime(new Date());
			
		userLiveChatService.save(userLiveChat);
	}

	private void leaveMessageEventHandler(String UID, String type, String category) throws Exception {
		UserLiveChat userLiveChat = null;

		switch (type) {
			case "Switch":
				userLiveChat = userLiveChatService.findByUIDAndStatus(UID, UserLiveChat.BEGINNING);
	
				userLiveChat.setLeaveMsgState(UserLiveChat.LEAVE_MESSAGE);
				userLiveChat.setCategory(category);
	
				userLiveChatService.save(userLiveChat);
				break;
				
			case "Waiting":
				userLiveChat = userLiveChatService.findByUIDAndStatus(UID, UserLiveChat.WAITING);
	
				userLiveChat.setLeaveMsgState(UserLiveChat.LEAVE_MESSAGE);
	
				userLiveChatService.save(userLiveChat);
				break;
		}
	}
}
