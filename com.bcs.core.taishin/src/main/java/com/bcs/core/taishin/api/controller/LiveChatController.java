package com.bcs.core.taishin.api.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bcs.core.aspect.annotation.WebServiceLog;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.bcs.core.api.service.LiveChatApiService;
import com.bcs.core.bot.db.service.BotReplyRecordService;
import com.bcs.core.bot.receive.service.MessageProcessService;
import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.service.UserLiveChatService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LIVE_CHAT_WORDING;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.exception.LiveChatException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.api.model.LiveChatReplyModel;
import com.bcs.core.taishin.api.model.LiveChatUserModel;
import com.bcs.core.taishin.service.LiveChatService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.LiveChatWordingUtil;
import com.linecorp.bot.model.message.Message;

@Controller
@RequestMapping("/api")
public class LiveChatController {
	private static Logger logger = Logger.getLogger(LiveChatController.class);

	// common
	private static String NOT_SUCCESS_USER_IN_NOT_IN_PROGRESS = "User is not in live chat progress.";
	// private static String DO_NOT_LEAVE_MESSAGE = "User did not leave any message before.";
	// ReplyMessage
	private static String SUCCESS_PUSH_MESSAGE = "Successfully push message.";
	// noticeChatAvailible
	private static String NOT_SUCCESS_USER_IS_NOT_WAITING = "User is not in waiting progress.";
	private static String NOT_SUCCESS_USER_IS_ALREADY_IN_PROGRESS = "User is already in progress.";
	// noticeChatClose
	private static String SUCCESS_CLOSE_CHAT = "Successfully close the chat";

	@Autowired
	private LiveChatService liveChatService;
	@Autowired
	private UserLiveChatService userLiveChatService;
	@Autowired
	private BotReplyRecordService botReplyRecordService;
	@Autowired
	private MessageProcessService messageProcessService;
	@Autowired
	private LiveChatApiService liveChatApiService;

	@WebServiceLog
	@RequestMapping(method = RequestMethod.POST, value = "/replyMessage", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> ReplyMessage(HttpServletRequest request, HttpServletResponse response, @RequestBody LiveChatReplyModel receivedMessage) {
		try {
			logger.info("ReplyMessage");
			logger.info("receivedMessage: " + receivedMessage);
			
			receivedMessage.validateResponse();
			
			String UID = receivedMessage.getTo();
			Integer messageType = receivedMessage.getIsWaiting();
			
			if(messageType != LiveChatReplyModel.NORMAL_REPLY) {
				UserLiveChat userLiveChat = userLiveChatService.findByUIDAndStatus(UID, UserLiveChat.WAITING);	// 找出在「等待客服」的使用者
				
				if (userLiveChat == null) {
					throw new BcsNoticeException(NOT_SUCCESS_USER_IS_NOT_WAITING);
				} else {
					List<Message> messageList = new ArrayList<Message>();
					
					messageList.add(messageProcessService.generateWaitingMessage(messageType));
					
					messageProcessService.pushMessage(UID, messageList, CONFIG_STR.AutoReply.toString());
					
					return new ResponseEntity<>(SUCCESS_PUSH_MESSAGE, HttpStatus.OK);
				}
			} else {
				UserLiveChat userLiveChat = userLiveChatService.findWaitingAndInProgressByUID(UID);	// 找出在「客服對話中」或「等待中」的使用者
				
				if (userLiveChat == null) {
					throw new BcsNoticeException(NOT_SUCCESS_USER_IN_NOT_IN_PROGRESS);
				} else {
					if(userLiveChat.getStatus().equals(UserLiveChat.WAITING))
						liveChatService.sendMsgToUser(receivedMessage, userLiveChat, CONFIG_STR.AutoReply.toString());
					else
						liveChatService.sendMsgToUser(receivedMessage, userLiveChat);
					
					return new ResponseEntity<>(SUCCESS_PUSH_MESSAGE, HttpStatus.OK);
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if (e instanceof BcsNoticeException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			} else if (e instanceof IllegalArgumentException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			} else {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@WebServiceLog
	@RequestMapping(method = RequestMethod.POST, value = "/notice/chat/availible/{hour}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> noticeChatAvailible(HttpServletRequest request, HttpServletResponse response, @RequestBody LiveChatUserModel liveChatUser, @PathVariable Long hour) {
		try {
			logger.info("noticeChatAvailible");
			logger.info("liveChatUser: " + liveChatUser);

			liveChatUser.validateResponse();
			
			int maxSize = CoreConfigReader.getInteger(CONFIG_STR.CHATLOG_GET_MAX_HOUR.toString());
			
			if(hour < 1)
				throw new IllegalArgumentException("\"hour\" cannot be smaller than one!");
			else if(hour > maxSize)
				throw new IllegalArgumentException("\"hour\" cannot be larger than " + maxSize + ".");

			String UID = liveChatUser.getUID();
			UserLiveChat userLiveChat = userLiveChatService.findByUIDAndNotFinishAndNotDiscrad(UID);
			
			/* 找尋 */
			if (userLiveChat != null) {
				String status = userLiveChat.getStatus();
				
				switch (status) {
					case UserLiveChat.WAITING:
						liveChatService.switchToManualReplyChannel(userLiveChat, LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.SUCCESS_SWITCH.toString()));
						return new ResponseEntity<>(botReplyRecordService.getChatLog(userLiveChat.getUID(), hour.intValue()), HttpStatus.OK);

					case UserLiveChat.BEGINNING:
						userLiveChat = userLiveChatService.findLeaveMsgUserByUIDAndState(UID, UserLiveChat.COMPLETE);
						
						if(userLiveChat != null) {
							liveChatService.switchToManualReplyChannel(userLiveChat, LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.SUCCESS_SWITCH.toString()));
							
							userLiveChatService.updateLeaveMsgState(userLiveChat.getChatId(), UserLiveChat.REPLY);
							
							return new ResponseEntity<>(botReplyRecordService.getChatLog(userLiveChat.getUID(), hour.intValue()), HttpStatus.OK);
						}
						
						throw new LiveChatException(NOT_SUCCESS_USER_IS_NOT_WAITING);

					case UserLiveChat.IN_PROGRESS:
						throw new LiveChatException(NOT_SUCCESS_USER_IS_ALREADY_IN_PROGRESS);
				}
			} else {
				userLiveChat = userLiveChatService.findLeaveMsgUserByUIDAndState(UID, UserLiveChat.COMPLETE);
				
				if(userLiveChat != null) {
					liveChatService.switchToManualReplyChannel(userLiveChat, LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.SUCCESS_SWITCH.toString()));
					
					userLiveChatService.updateLeaveMsgState(userLiveChat.getChatId(), UserLiveChat.REPLY);
					
					return new ResponseEntity<>(botReplyRecordService.getChatLog(userLiveChat.getUID(), hour.intValue()), HttpStatus.OK);
				}
				
				throw new LiveChatException("[NoticeChatAvailible] Cannot connect to this user now!\nDetail: User \'" + UID + "\' didn't left any message before.");
			}

			throw new LiveChatException("[NoticeChatAvailible] Cannot connect to this user now!");
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if (e instanceof LiveChatException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			} else if (e instanceof IllegalArgumentException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			} else {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@WebServiceLog
	@RequestMapping(method = RequestMethod.POST, value = "/notice/chat/close", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<?> noticeChatClose(HttpServletRequest request, HttpServletResponse response, @RequestBody LiveChatUserModel liveChatUser) {
		try {
			logger.info("noticeChatClose");
			logger.info("liveChatUser: " + liveChatUser);
			logger.info("liveChatUser close type: " + liveChatUser.getCloseType());
			
			liveChatUser.validateResponse();
			
			String UID = liveChatUser.getUID();
			Integer closeType = liveChatUser.getCloseType();
			
			if(closeType == null)
				throw new IllegalArgumentException("Parameter \"closeType\" is required!");
			if(closeType > 2 || closeType < 0)
				throw new IllegalArgumentException("Invalid \"closeType\"");
			if(closeType == 0) {
				if(liveChatUser.getSurveyUrl() == null)
					throw new IllegalArgumentException("Parameter \"surveyUrl\" is required!");
				if(liveChatUser.getSurveyUrl().equals(""))
					throw new IllegalArgumentException("Parameter \"surveyUrl\" cannot be empty!");
			}
			
			if(closeType != LiveChatUserModel.DISCARD_WAITING) {	// 客服人員主動關閉或由客服系統被動關閉
				String message = null;
				UserLiveChat userLiveChat = userLiveChatService.findByUIDAndStatus(UID, UserLiveChat.IN_PROGRESS);
				
				if (userLiveChat == null) {
					throw new BcsNoticeException(NOT_SUCCESS_USER_IN_NOT_IN_PROGRESS);
				}
				
				if(closeType == LiveChatUserModel.ACTIVE_CLOSE) {
					List<Message> messageList = new ArrayList<Message>();
					
					liveChatService.switchToAutoReplyChannel(userLiveChat, message);	// 將使用者切換回智能客服的 Channel
					
					messageList.add(messageProcessService.generateSurveyMessage(liveChatUser.getSurveyUrl()));
					
					messageProcessService.pushMessage(UID, messageList, CONFIG_STR.AutoReply.toString());
				} else if(closeType == LiveChatUserModel.PASSIVE_CLOSE){
					message = LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LIVE_CHAT_CLOSE_PASSIVE.toString());
					
					liveChatService.switchToAutoReplyChannel(userLiveChat, message);	// 將使用者切換回智能客服的 Channel
				}
			} else {	// 使用者超過四次提醒未反應，強制剔除等待隊伍
				UserLiveChat userLiveChat = userLiveChatService.findByUIDAndStatus(UID, UserLiveChat.WAITING);	// 找出在「等待客服」的使用者
				
				if (userLiveChat == null) {	// 如該使用者不為「等待中」的狀態，拋出 Exception
					throw new BcsNoticeException(NOT_SUCCESS_USER_IS_NOT_WAITING);
				}
				
				userLiveChat.setStatus(UserLiveChat.DISCARD);
				userLiveChat.setModifyTime(new Date());
				
				userLiveChatService.save(userLiveChat);
				
				messageProcessService.pushTextMsgAsync(UID, LiveChatWordingUtil.getString(LIVE_CHAT_WORDING.LIVE_CHAT_CLOSE_PASSIVE.toString()), CONFIG_STR.AutoReply.name());
			}
			liveChatApiService.resetChatFlow(UID);	// 通知碩網大腦，要 reset 錯誤回答的次數
			
			return new ResponseEntity<>(SUCCESS_CLOSE_CHAT, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if (e instanceof BcsNoticeException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			} else if (e instanceof IllegalArgumentException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
			} else {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	/*
	 * 取得使用者與智能客服的對話紀錄
	 */
	@WebServiceLog
	@RequestMapping(method = RequestMethod.GET, value = "/getChatLogs")
	public ResponseEntity<?> getChatLog(HttpServletRequest request, HttpServletResponse response,	
			@RequestParam(value = "uid", required = true) String uid, 
			@RequestParam(value = "hour", required = true) Long hour) {
		logger.info("getChatLogs");
		
		try {
			return new ResponseEntity<>(botReplyRecordService.getChatLog(uid, hour.intValue()), HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			logger.info("Error: " + e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.info("Error: " + e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}