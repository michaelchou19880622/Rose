package com.bcs.core.bot.receive.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.bot.api.model.SendToBotModel;
import com.bcs.core.bot.api.service.LineAccessApiService;
import com.bcs.core.bot.db.entity.BotReplyRecord;
import com.bcs.core.bot.db.service.BotReplyRecordService;
import com.bcs.core.bot.enums.SEND_TYPE;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LIVE_CHAT_WORDING;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.smartrobot.service.SwitchIconService;
import com.bcs.core.utils.LiveChatWordingUtil;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.Sender;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;

@Service
public class MessageProcessService {
	private static Logger logger = Logger.getLogger(MessageProcessService.class);

	public static final Integer SWITCH_MESSAGE_NORMAL = 0;
	public static final Integer SWITCH_MESSAGE_KEYWORD = 1;
	public static final Integer SWITCH_MESSAGE_MARKETING_KEYWORD = 2;

	@Autowired
	private BotReplyRecordService botReplyRecordService;
	@Autowired
	private SwitchIconService switchIconService;

	public boolean isAttentionKeyword(JSONObject responseMsg) {
		if(responseMsg.has("attentionKeywords"))
			return true;

		return false;
	}

    public boolean isMarketingKeyword(JSONObject responseMsg) {
        if(responseMsg.has("isMarketing"))
            return responseMsg.getBoolean("isMarketing");

        return false;
    }

	public boolean isForward(JSONObject responseMsg) {
		if(responseMsg.has("choose_dep"))
			return responseMsg.getString("choose_dep").equals("success");

		return false;
	}

	public JSONObject generateConfirmSwitchMsg(Integer type, Boolean isLeaveMessage) {
		JSONObject comfirmSwitchMessage = new JSONObject();
		JSONObject templateObject = new JSONObject();
		JSONObject actionObject = new JSONObject();
		JSONArray actionList = new JSONArray();
		String key = LIVE_CHAT_WORDING.SWITCH_MESSAGE_NORMAL.toString();

		if(type == SWITCH_MESSAGE_KEYWORD) {
			key = LIVE_CHAT_WORDING.SWITCH_MESSAGE_KEYWORD.toString();
		} else if(type == SWITCH_MESSAGE_MARKETING_KEYWORD) {
			key = LIVE_CHAT_WORDING.SWITCH_MESSAGE_MARKETING.toString();
		}

		String text = LiveChatWordingUtil.getString(key);
		/*String thumbnailImageUrl = CoreConfigReader.getString(CONFIG_STR.BaseUrlHTTPS) + "BCS/images/rose_emoticon/rose_confused.png";

		if(key.equals(LIVE_CHAT_WORDING.SWITCH_MESSAGE_KEYWORD.toString())) {
			thumbnailImageUrl = CoreConfigReader.getString(CONFIG_STR.BaseUrlHTTPS) + "BCS/images/rose_emoticon/rose_happy.png";
		}*/

		comfirmSwitchMessage.put("type", "template");
		comfirmSwitchMessage.put("altText", text);

		templateObject.put("type", "buttons");
		// templateObject.put("title", LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.SWITCH_MESSAGE_NORMAL_TITLE.toString()));
		templateObject.put("text", text);
		// templateObject.put("thumbnailImageUrl", thumbnailImageUrl);

		if(!isLeaveMessage) {	// 沒留言過的使用者才可以有「轉接客服專員」之選項
			actionObject.put("type", "postback");
            actionObject.put("label", LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.SWITCH_BUTTON.toString()));
			actionObject.put("data", "action=ManualReply");

			actionList.put(actionObject);
		}

		actionObject = new JSONObject();

		actionObject.put("type", "postback");
        actionObject.put("label", LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LEAVE_MESSAGE_BUTTON.toString()));
		actionObject.put("data", "action=LeaveMessage");

		actionList.put(actionObject);

		/*if(type == SWITCH_MESSAGE_KEYWORD) {	// 有觸發到敏感詞事件才會有的選項
			actionObject = new JSONObject();

			actionObject.put("type", "postback");
            actionObject.put("label", LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.KEEP_CHATTING_BUTTON.toString()));
			actionObject.put("data", "action=AutoReply");

			actionList.put(actionObject);
		}*/

		templateObject.put("actions", actionList);

		comfirmSwitchMessage.put("template", templateObject);

		return comfirmSwitchMessage;
	}

	public Message generateChooseCategoryMsg(boolean isLeaveMessage) {
		List<Action> actions = new ArrayList<Action>();
		String text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.CHOOSE_CATAGORY.toString());

        String[] categories = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.CATEGORY_NAME_ZH_TW.toString()).split(",");
        String[] categories_en = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.CATEGORY_NAME_EN.toString()).split(",");

        for(Integer i = 0; i < categories.length; i++) {
            String label = categories[i];
            String data =  isLeaveMessage ?  "leaveMsgCategory=" + categories_en[i] : "category=" + categories_en[i];

            actions.add(new PostbackAction(label, data));
        }

		Sender sender = switchIconService.generateSenderModel(CONFIG_STR.AUTO_REPLY.toString());

		Message message = new TemplateMessage(text, new ButtonsTemplate(null, null, text, actions), sender);

		return message;
	}

	public Message generateWaitingMessage(Integer type) {
		String text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.CUSTOMER_SERVICE_BUSY.toString());
		String thumbnailInageUrl = CoreConfigReader.getString(CONFIG_STR.BASE_URL_HTTPS) + "BCS/images/rose_emoticon/rose_grateful.png";
		String title = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.CUSTOMER_SERVICE_BUSY_TITLE.toString());;
		List<Action> actions = new ArrayList<Action>();

        actions.add(new PostbackAction(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.KEEP_WAITING_BUTTON.toString()), "waitingAction=keepWaiting"));
        actions.add(new PostbackAction(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LEAVE_MESSAGE_BUTTON.toString()), "waitingAction=leaveMessage"));
        actions.add(new PostbackAction(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.GIVEUP_WAITING_BUTTON.toString()), "waitingAction=giveUp"));

		if(type == 2) {
			text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.CUSTOMER_SERVICE_STILL_BUSY.toString());
			thumbnailInageUrl = CoreConfigReader.getString(CONFIG_STR.BASE_URL_HTTPS) + "BCS/images/rose_emoticon/rose_happy.png";
			title = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.CUSTOMER_SERVICE_STILL_BUSY_TITLE.toString());
		}

		Sender sender = switchIconService.generateSenderModel(CONFIG_STR.AUTO_REPLY.toString());

		Message message = new TemplateMessage(text, new ButtonsTemplate(thumbnailInageUrl, title, text, actions), sender);

		return message;
	}

	public Message generateSurveyMessage(String surveyUrl) {
		String text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LIVE_CHAT_CLOSE.toString());
		List<Action> actions = new ArrayList<Action>();

        actions.add(new URIAction(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.CUSTOMER_SATISFACTION_DEGREE_BUTTON.toString()), surveyUrl));

		Sender sender = switchIconService.generateSenderModel(CONFIG_STR.AUTO_REPLY.toString());

		Message message = new TemplateMessage(text, new ButtonsTemplate(null, null, text, actions), sender);

		return message;
	}

	public Message generateConfirmMessage(String message) {
		String text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LEAVE_MESSAGE_CONFIRM.toString());
        Action leftButtonAction = new PostbackAction(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.CONFIRM_LEAVE_MESSAGE_BUTTON.toString()), "leaveMessageAction=confirm");
        Action rightButtonAction = new PostbackAction(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.RESET_LEAVE_MESSAGE_BUTTON.toString()), "leaveMessageAction=reset");

		text += "「" + message + "」\n" + LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LEAVE_MESSAGE_CHOOSE.toString());

		Sender sender = switchIconService.generateSenderModel(CONFIG_STR.AUTO_REPLY.toString());

		Message confirmMessage = new TemplateMessage(text, new ConfirmTemplate(text, leftButtonAction, rightButtonAction), sender);

		return confirmMessage;
	}

	public Message generateNotInOfficeHourMessage() {
		String text = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.NOT_IN_OFFICE_HOUR.toString());
		List<Action> actions = new ArrayList<Action>();

        actions.add(new PostbackAction(LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LEAVE_MESSAGE_BUTTON.toString()), "action=LeaveMessage"));

		Sender sender = switchIconService.generateSenderModel(CONFIG_STR.AUTO_REPLY.toString());

		Message confirmMessage = new TemplateMessage(
				text,
				new ButtonsTemplate(
						CoreConfigReader.getString(CONFIG_STR.BASE_URL_HTTPS) + "BCS/images/rose_emoticon/rose_joyful.png",
						LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.NOT_IN_OFFICE_HOUR_TITLE.toString()),
						text,
						actions
				),
				sender
		);

		return confirmMessage;
	}

	public void replyTextMessage(String channelId, String channelName, List<String> textList, String replyToken) throws Exception {
		List<Message> sendMsgList = new ArrayList<Message>();
		Message message = null;
		Sender sender = switchIconService.generateSenderModel(CONFIG_STR.AUTO_REPLY.toString());

		for(String text : textList) {
			message = new TextMessage(text, sender);

			sendMsgList.add(message);
		}

		SendToBotModel sendToBotModel = new SendToBotModel();

		sendToBotModel.setChannelId(channelId);
		sendToBotModel.setSendType(SEND_TYPE.REPLY_MSG);
		sendToBotModel.setChannelName(channelName);

		ReplyMessage replyMessage = new ReplyMessage(replyToken, sendMsgList);
		sendToBotModel.setReplyMessage(replyMessage);

		LineAccessApiService.sendToLine(sendToBotModel);
	}

	public void pushTextMsgAsync(String UID, String text,String channelName) throws Exception {
		logger.info("pushTextMsgAsync");

		MsgDetail sendMsgDetail = new MsgDetail();

		sendMsgDetail.setText(text);
		sendMsgDetail.setMsgType(MsgGenerator.MSG_TYPE_TEXT);

		Sender sender = switchIconService.generateSenderModel(channelName);

		TextMessage textMesage = new TextMessage(text, sender);
		PushMessage pushMessage = new PushMessage(UID, textMesage);

		SendToBotModel sendToBotModel = new SendToBotModel();
		sendToBotModel.setChannelId(CONFIG_STR.DEFAULT.toString());
		sendToBotModel.setChannelName(channelName);
		sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);
		sendToBotModel.setPushMessage(pushMessage);
		LineAccessApiService.sendToLine(sendToBotModel);
	}

	public void pushMessage(String UID, List<Message> messageList, String channelName) throws Exception {
		SendToBotModel sendToBotModel = new SendToBotModel();

		sendToBotModel.setChannelId(CONFIG_STR.DEFAULT.toString());
		sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);
		sendToBotModel.setChannelName(channelName);

		PushMessage pushMessage = new PushMessage(UID, messageList);
		sendToBotModel.setPushMessage(pushMessage);

		LineAccessApiService.sendToLine(sendToBotModel);
	}

	public void replyMessage(String channelId, String replyToken, List<Message> messageList, String channelName) throws Exception {
		SendToBotModel sendToBotModel = new SendToBotModel();

		sendToBotModel.setChannelId(channelId);
		sendToBotModel.setSendType(SEND_TYPE.REPLY_MSG);
		sendToBotModel.setChannelName(channelName);

		ReplyMessage replyMessage = new ReplyMessage(replyToken, messageList);
		sendToBotModel.setReplyMessage(replyMessage);

		LineAccessApiService.sendToLine(sendToBotModel);
	}

	public JSONObject defaultReplyMessage() {
		JSONObject defaultMessage = new JSONObject();

		defaultMessage.put("type", "text");
		defaultMessage.put("text", LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.DEFAULT_REPLY_MESSAGE.toString()));

		return defaultMessage;
	}

	public void botReplyMessageRecorder(JSONObject replyMessageObject, String UID) throws Exception {
		if(replyMessageObject.has("replyToken") && replyMessageObject.has("messages")) {
			JSONArray messageList = replyMessageObject.getJSONArray("messages");

			for(int index = 0; index < messageList.length(); index++) {
				BotReplyRecord record = new BotReplyRecord();
				JSONObject message = messageList.getJSONObject(index);
				String messageType = message.getString("type");

				//FIXME 可能會有長度爆掉的地方
				record.setMsgType(messageType);
				record.setUID(UID);
				record.setReplyToken(replyMessageObject.getString("replyToken"));
				record.setCreateTime(new Date());
				record.setModifyTime(new Date());

				switch(messageType) {
					case BotReplyRecord.MESSAGE_TYPE_TEXT:
						//FIXME 可能會有長度爆掉的地方
						record.setMsgText(message.getString("text"));
						break;
					case BotReplyRecord.MESSAGE_TYPE_STICKER:
						record.setMsgText("[貼圖]");
						break;
					case BotReplyRecord.MESSAGE_TYPE_IMAGE:
						record.setMsgText("[圖片]");
						//FIXME 可能會有長度爆掉的地方
						record.setReferenceLinkUrl(message.getString("originalContentUrl"));
						break;
					case BotReplyRecord.MESSAGE_TYPE_VIDEO:
						record.setMsgText("[影片]");
						//FIXME 可能會有長度爆掉的地方
						record.setReferenceLinkUrl(message.getString("originalContentUrl"));
						break;
					case BotReplyRecord.MESSAGE_TYPE_AUDIO:
						record.setMsgText("[音訊]");
						//FIXME 可能會有長度爆掉的地方
						record.setReferenceLinkUrl(message.getString("originalContentUrl"));
						break;
					case BotReplyRecord.MESSAGE_TYPE_LOCATION:
						record.setMsgText("[位置]");
						//FIXME 可能會有長度爆掉的地方
						record.setLocation(message.getString("latitude") + ", " + message.getString("longitude"));
						break;
					case BotReplyRecord.MESSAGE_TYPE_TEMPLATE:
						//FIXME 可能會有長度爆掉的地方
						if(!message.getJSONObject("template").isNull("title"))
							record.setMsgText(message.getJSONObject("template").getString("title") + " " + message.getJSONObject("template").getString("text"));
						else
							record.setMsgText(message.getJSONObject("template").getString("text"));
						break;
					case BotReplyRecord.MESSAGE_TYPE_IMAGEMAP:
						record.setMsgText("[圖文訊息]");
						//FIXME 可能會有長度爆掉的地方
						record.setReferenceLinkUrl(message.getString("baseUrl"));
						break;
				}

				botReplyRecordService.save(record);
			}
		} else {
			throw new IllegalArgumentException("[BOT Reply Message Recorder]: Invalid format!");
		}
	}
}