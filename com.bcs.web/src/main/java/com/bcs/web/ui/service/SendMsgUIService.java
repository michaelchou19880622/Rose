package com.bcs.web.ui.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import retrofit2.Response;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.api.msg.MsgGeneratorExtend;
import com.bcs.core.api.msg.MsgGeneratorFactory;
import com.bcs.core.api.msg.MsgGeneratorSticker;
import com.bcs.core.api.msg.MsgGeneratorText;
import com.bcs.core.api.msg.plugins.MsgGeneratorBcsLink;
import com.bcs.core.bot.api.model.SendToBotModel;
import com.bcs.core.bot.api.service.LineAccessApiService;
import com.bcs.core.bot.enums.SEND_TYPE;
import com.bcs.core.bot.scheduler.handler.ExecuteSendMsgTask;
import com.bcs.core.bot.scheduler.service.SchedulerService;
import com.bcs.core.bot.send.service.SendingMsgService;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentFlag;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.db.entity.ContentRichMsg;
import com.bcs.core.db.entity.ContentSticker;
import com.bcs.core.db.entity.ContentTemplateMsg;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.entity.SendGroup;
import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.service.ContentFlagService;
import com.bcs.core.db.service.ContentLinkService;
//import com.bcs.core.db.service.ContentResourceService;
import com.bcs.core.db.service.ContentRewardCardService;
import com.bcs.core.db.service.ContentRichMsgService;
import com.bcs.core.db.service.ContentStickerService;
import com.bcs.core.db.service.ContentTemplateMsgService;
import com.bcs.core.db.service.GroupGenerateService;
//import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.MsgDetailService;
import com.bcs.core.db.service.MsgMainService;
import com.bcs.core.db.service.MsgSendMainService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.db.service.SerialSettingService;
import com.bcs.core.db.service.UserLiveChatService;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.web.ui.model.SendMsgDetailModel;
import com.bcs.web.ui.model.SendMsgModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.response.BotApiResponse;

@Service
public class SendMsgUIService {
	@Autowired
	private MsgMainService msgMainService;
	@Autowired
	private MsgSendMainService msgSendMainService;
	@Autowired
	private MsgDetailService msgDetailService;
	@Autowired
	private SendGroupService sendGroupService;
	@Autowired
	private ContentStickerService contentStickerService;
	@Autowired
	private GroupGenerateService groupGenerateService;
	@Autowired
	private SendingMsgService sendingMegService;
	@Autowired
	private SchedulerService schedulerService;
	@Autowired
	private ContentLinkService contentLinkService;
	@Autowired
	private ContentRichMsgService contentRichMsgService;
	@Autowired
	private ContentCouponService contentCouponService;
	@Autowired
	private ContentRewardCardService contentRewardCardService;
	@Autowired
	private ContentFlagService contentFlagService;
	@Autowired
	private SerialSettingService serialSettingService;
	@Autowired
	private ContentTemplateMsgService contentTemplateMsgService;
	@Autowired
	private UserLiveChatService userLiveChatService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(SendMsgUIService.class);
	
	public void sendMsgToMid(String mid, List<SendMsgDetailModel> sendMsgDetails, String account) throws Exception{
		logger.info("sendMsgToMid:" + mid);
		
		List<String> mids = new ArrayList<String>();
		mids.add(mid);
		sendMsgToMids(mids, sendMsgDetails, account, null);
	}
	
	public void sendMsgToMid(String mid, List<SendMsgDetailModel> sendMsgDetails, String account, SendMsgModel sendMsgModel) throws Exception{
		logger.info("sendMsgToMid:" + mid);
		
		List<String> mids = new ArrayList<String>();
		mids.add(mid);
		sendMsgToMids(mids, sendMsgDetails, account, sendMsgModel);
	}
	
	public void sendMsgToMids(List<String> mids, List<SendMsgDetailModel> sendMsgDetails, String account, SendMsgModel sendMsgModel) throws Exception{
		logger.info("sendMsgToMid:" + mids);

		sendMsgToMids(mids, sendMsgDetails, false, null, account, sendMsgModel);
	}
	
	public void sendMsgToMids(List<String> mids, List<SendMsgDetailModel> sendMsgDetails, boolean async, Long updateMsgId, String account, SendMsgModel sendMsgModel) throws Exception{
		this.sendMsgToMids(mids, sendMsgDetails, async, updateMsgId, account, 0, sendMsgModel);
	}
	
	public void sendMsgToMids(List<String> mids, List<SendMsgDetailModel> sendMsgDetails, boolean async, Long updateMsgId, String account, int retryCount, SendMsgModel sendMsgModel) throws Exception{
		logger.info("sendMsgToMid:" + mids);
		
		if(mids != null && mids.size() > 0){
			
			if(async){				
				List<MsgGenerator> msgGenerators = validateMessages(sendMsgDetails);
				sendingMegService.sendToLineAsync(msgGenerators, null, mids, API_TYPE.BOT, updateMsgId);
			}
			else{
				
				SendToBotModel sendToBotModel = new SendToBotModel();

				sendToBotModel.setChannelId(CONFIG_STR.Default.toString());
				sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);
				
				// Validate Messages
				List<Message> sendStrList = validateMessagesWichMessage(sendMsgDetails, "");
				String sendStr = ObjectUtil.objectToJsonStr(sendStrList);
				
				Map<String,UserLiveChat> inprogressMids = userLiveChatService.findByStauts(UserLiveChat.IN_PROGRESS);
				
				for(String mid : mids){
					try{
						
						Map<String, String> replaceParam = null;
						
						if(sendMsgModel != null && StringUtils.isNotBlank(sendMsgModel.getSerialId())){
							replaceParam = serialSettingService.getSerialSettingReplaceParam(sendMsgModel.getSerialId(), mid);
							if(replaceParam == null){
								continue;
							}
						}
						
						// Validate Messages
						List<Message> messageList = validateMessagesWichMessage(sendMsgDetails, mid, replaceParam);
						
						PushMessage pushMessage = new PushMessage(mid, messageList);
						sendToBotModel.setPushMessage(pushMessage);
						
						String channelName = inprogressMids.get(mid)!=null ? CONFIG_STR.InManualReplyButNotSendMsg.toString() : CONFIG_STR.AutoReply.toString();
						sendToBotModel.setChannelName(channelName);
						
						Response<BotApiResponse> responseMulti = LineAccessApiService.sendToLine(sendToBotModel);
						
						logger.info("statusMulti:" + responseMulti.code());

						// Check Response Status
						this.checkStatus(responseMulti, mid, null);
						
						this.saveLog(account, mid, sendStr, responseMulti, mid);
					}
					catch(Exception e){
						logger.error(ErrorRecord.recordError(e));
						if(retryCount < 5){
							List<String> midsRetry = new ArrayList<String>();
							midsRetry.add(mid);
							this.sendMsgToMids(midsRetry, sendMsgDetails, async, updateMsgId, account, retryCount + 1, sendMsgModel);
							break;
						}
						else{
							throw e;
						}
					}
				}
			}
		}
	}
	
	private void saveLog(String account, String mid, String sendStr, Response<BotApiResponse> responseMulti, String referenceId){

		try{			
			List<Object> content = new ArrayList<Object>();
			content.add(sendStr);
			content.add(mid);
			content.add(responseMulti);
			
			SystemLogUtil.saveLogDebug("ActiveMsg", "SendTestMsg", account, content, referenceId);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	private void checkStatus(Response<BotApiResponse> response, String mid, Long msgId) throws Exception{

		logger.debug("status:" + response.code());

		if(response.code() != 200){
			List<Object> content = new ArrayList<Object>();
			content.add(mid);
			content.add(msgId);
			content.add(response.code());
			content.add(response.body());
			if(response.errorBody() != null){
				content.add(response.errorBody().string());
			}
			SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApiStatus, content, mid);
			throw new BcsNoticeException("發送訊息錯誤請洽資訊人員:" + response.code());
		}
	}
	
	public void saveDraftMessage(SendMsgModel sendMsgModel, String adminUserAccount) throws Exception{

		this.saveMessage(sendMsgModel, adminUserAccount, MsgMain.MESSAGE_STATUS_DRAFT);
	}

	/**
	 * Delete MsgMain
	 * @param msgId
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void deleteMessageMain(Long msgId){
		msgMainService.delete(msgId);
	}

	/**
	 * Delete MsgSendMain
	 * @param msgSendId
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void deleteMessageSendMain(Long msgSendId){
		msgSendMainService.delete(msgSendId);
	}
	
	/**
	 * Check Status equals
	 * @param msgId
	 * @param compareStatus
	 * @return boolean
	 */
	private boolean checkMsgMainStatus(Long msgId, String compareStatus){
		
		MsgMain msgMain = msgMainService.findOne(msgId);
		if(compareStatus.equals(msgMain.getStatus())){
			return true;
		}
		
		return false;
	}
	
	/**
	 * Save Message
	 * @param sendMsgModel
	 * @param adminUserAccount
	 * @param status
	 * @return msgId
	 * @throws Exception
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	private Long saveMessage(SendMsgModel sendMsgModel, String adminUserAccount, String status) throws Exception{
		logger.info("saveMessage");
		MsgMain msgMain = new MsgMain();
		
		if(sendMsgModel.getMsgId() != null && sendMsgModel.getMsgId() > 0){
			if(checkMsgMainStatus(sendMsgModel.getMsgId(), MsgMain.MESSAGE_STATUS_DRAFT)){
				msgMain.setMsgId(sendMsgModel.getMsgId());
				// Remove Old Setting Detail
				msgDetailService.deleteByMsgIdAndMsgParentType(sendMsgModel.getMsgId(), MsgMain.THIS_PARENT_TYPE);
			}
		}
		msgMain.setGroupId(sendMsgModel.getSendGroupId());
		msgMain.setSerialId(sendMsgModel.getSerialId());
		msgMain.setSendType(sendMsgModel.getSendingMsgType());
		msgMain.setScheduleTime(sendMsgModel.getSendingMsgTime());
		msgMain.setStatus(status);
		msgMain.setModifyTime(new Date());
		msgMain.setModifyUser(adminUserAccount);
		msgMain.setMsgTag(contentFlagService.concat(sendMsgModel.getMsgTagList(), 50));

		msgMainService.save(msgMain);
		
		// Save ContentFlag
		contentFlagService.save(
				String.valueOf(msgMain.getMsgId()), 
				ContentFlag.CONTENT_TYPE_MSG_SEND, 
				sendMsgModel.getMsgTagList());
		
		List<SendMsgDetailModel> list = sendMsgModel.getSendMsgDetails();
		this.createMsgDetail(msgMain.getMsgId(), list, adminUserAccount, MsgMain.THIS_PARENT_TYPE);
		
		return msgMain.getMsgId();
	}
	
	public void createMsgDetail(Long msgId, List<SendMsgDetailModel> list, String adminUserAccount, String parentType ) throws Exception{

		if(list != null && list.size() > 0){
			for(SendMsgDetailModel model : list){
				String detailType = model.getDetailType();
				ObjectNode node = createObjectNode(model.getDetailContent());
				
				if(MsgGenerator.MSG_TYPE_TEXT.equals(detailType)){
					MsgGeneratorText result = new MsgGeneratorText(node);
					
					MsgDetail detail = new MsgDetail();
					detail.setMsgId(msgId);
					detail.setMsgType(MsgGenerator.MSG_TYPE_TEXT);
					detail.setText(result.getText());
					detail.setMsgParentType(parentType);
					detail.setEventType(model.getEventType());
					
					msgDetailService.save(detail);
				}
				else if(MsgGenerator.MSG_TYPE_STICKER.equals(detailType)){
					MsgGeneratorSticker result = new MsgGeneratorSticker(node);
					
					ContentSticker sticker = new ContentSticker();
					sticker.setStickerStkid(result.getSTKID());
					sticker.setStickerStkpkgid(result.getSTKPKGID());
					sticker.setStickerStkver(result.getSTKVER());
					contentStickerService.save(sticker);
					
					MsgDetail detail = new MsgDetail();
					detail.setMsgId(msgId);
					detail.setMsgType(MsgGenerator.MSG_TYPE_STICKER);
					detail.setReferenceId(sticker.getStickerStkid());
					detail.setMsgParentType(parentType);
                    detail.setEventType(model.getEventType());
					
					msgDetailService.save(detail);
				}
				else if(MsgGenerator.MSG_TYPE_IMAGE.equals(detailType)){
					String resourceId = node.get("resourceId").textValue();
					
					MsgDetail detail = new MsgDetail();
					detail.setMsgId(msgId);
					detail.setMsgType(MsgGenerator.MSG_TYPE_IMAGE);
					detail.setReferenceId(resourceId);
					detail.setMsgParentType(parentType);
                    detail.setEventType(model.getEventType());
					
					msgDetailService.save(detail);
				}
				else if(MsgGenerator.MSG_TYPE_AUDIO.equals(detailType)){
					String resourceId = node.get("resourceId").textValue();

					MsgDetail detail = new MsgDetail();
					detail.setMsgId(msgId);
					detail.setMsgType(MsgGenerator.MSG_TYPE_AUDIO);
					detail.setReferenceId(resourceId);
					detail.setMsgParentType(parentType);
                    detail.setEventType(model.getEventType());
					
					msgDetailService.save(detail);
				}
				else if(MsgGenerator.MSG_TYPE_VIDEO.equals(detailType)){
					String resourceId = node.get("resourceId").textValue();

					MsgDetail detail = new MsgDetail();
					detail.setMsgId(msgId);
					detail.setMsgType(MsgGenerator.MSG_TYPE_VIDEO);
					detail.setReferenceId(resourceId);
					detail.setMsgParentType(parentType);
                    detail.setEventType(model.getEventType());
					
					msgDetailService.save(detail);
				}
				else if(MsgGeneratorExtend.MSG_TYPE_BCS_PAGE.equals(detailType)){

					MsgDetail detail = new MsgDetail();
					detail.setText("卡友頁面"); 
					detail.setMsgId(msgId);
					detail.setMsgType(MsgGeneratorExtend.MSG_TYPE_BCS_PAGE);
					detail.setMsgParentType(parentType);
                    detail.setEventType(model.getEventType());
					
					msgDetailService.save(detail);
				}
				else if(MsgGenerator.MSG_TYPE_LINK.equals(detailType)){
					MsgGeneratorBcsLink result = new MsgGeneratorBcsLink(node);

					String linkId = UUID.randomUUID().toString().toLowerCase();
					
					ContentLink contentLink = new ContentLink();
					contentLink.setLinkId(linkId);
					contentLink.setLinkUrl(result.getLinkUriParams());
					contentLink.setLinkTitle(result.getTextParams());
					contentLink.setLinkPreviewImage(result.getLinkImageParams());
					
					List<String> linkTagList = new ArrayList<>();
					
					for (JsonNode linkTag : node.get("linkTagList")) {
						linkTagList.add(linkTag.asText());
					}
					
					contentLink.setLinkTag(contentFlagService.concat(linkTagList, 50));
					contentLink.setModifyTime(new Date());
					contentLink.setModifyUser(adminUserAccount);
					contentLinkService.save(contentLink);
					
					// Save ContentFlag
					contentFlagService.save(
							linkId, 
							ContentFlag.CONTENT_TYPE_LINK, 
							linkTagList);
					
					MsgDetail detail = new MsgDetail();
					detail.setText(result.getTextParams());
					detail.setMsgId(msgId);
					detail.setMsgType(MsgGenerator.MSG_TYPE_LINK);
					detail.setReferenceId(contentLink.getLinkId());
					detail.setMsgParentType(parentType);
                    detail.setEventType(model.getEventType());
					
					msgDetailService.save(detail);
				}
				else if(MsgGenerator.MSG_TYPE_RICH_MSG.equals(detailType)){
					String richId = node.get("richId").textValue();
					
					MsgDetail detail = new MsgDetail();
					detail.setMsgId(msgId);
					detail.setMsgType(MsgGenerator.MSG_TYPE_RICH_MSG);
					detail.setReferenceId(richId);
					detail.setMsgParentType(parentType);
                    detail.setEventType(model.getEventType());
					
					msgDetailService.save(detail);
				}
				else if(MsgGeneratorExtend.MSG_TYPE_INTERACTIVE_LINK.equals(detailType)){
					MsgGeneratorBcsLink result = new MsgGeneratorBcsLink(node);

					String linkId = UUID.randomUUID().toString().toLowerCase();
					
					ContentLink contentLink = new ContentLink();
					contentLink.setLinkId(linkId);
					contentLink.setLinkUrl(result.getLinkUriParams());
					contentLink.setLinkTitle(result.getTextParams());
					contentLink.setLinkTag(node.get("linkTag").asText());
					contentLink.setModifyTime(new Date());
					contentLink.setModifyUser(adminUserAccount);
					contentLinkService.save(contentLink);
					
					MsgDetail detail = new MsgDetail();
					detail.setText(result.getTextParams());
					detail.setMsgId(msgId);
					detail.setMsgType(MsgGeneratorExtend.MSG_TYPE_INTERACTIVE_LINK);
					detail.setReferenceId(contentLink.getLinkId());
					detail.setMsgParentType(parentType);
                    detail.setEventType(model.getEventType());
					
					msgDetailService.save(detail);
				}
				else if(MsgGeneratorExtend.MSG_TYPE_COUPON.equals(detailType)){					
					String couponId = node.get("couponId").textValue();
					 ContentCoupon contentCoupon = contentCouponService.findOne(couponId);
					
					MsgDetail detail = new MsgDetail();
					detail.setMsgId(msgId);
					detail.setText(contentCoupon.getCouponTitle());
					detail.setMsgType(MsgGeneratorExtend.MSG_TYPE_COUPON);
					detail.setReferenceId(couponId);
					detail.setMsgParentType(parentType);
                    detail.setEventType(model.getEventType());
					
					msgDetailService.save(detail);
				}else if(MsgGeneratorExtend.MSG_TYPE_REWARDCARD.equals(detailType)){
					String rewardCardId = node.get("rewardCardId").textValue();
					ContentRewardCard rewardCard = contentRewardCardService.findOne(rewardCardId);
					
					MsgDetail detail = new MsgDetail();
					detail.setMsgId(msgId);
					detail.setText(rewardCard.getRewardCardMainTitle());
					detail.setMsgType(MsgGeneratorExtend.MSG_TYPE_REWARDCARD);
					detail.setReferenceId(rewardCardId);
					detail.setMsgParentType(parentType);
					detail.setEventType(model.getEventType());
					
					msgDetailService.save(detail);
				}
				else if(MsgGeneratorExtend.MSG_TYPE_TEMPLATE.equals(detailType)){
					String templateId = node.get("templateId").textValue();
					
					MsgDetail detail = new MsgDetail();
					detail.setMsgId(msgId);
					detail.setMsgType(MsgGenerator.MSG_TYPE_TEMPLATE);
					detail.setReferenceId(templateId);
					detail.setMsgParentType(parentType);
                    detail.setEventType(model.getEventType());
					
					msgDetailService.save(detail);
				}
			}
		}
	}

	private static class ExecuteSendMsgRunnable implements Runnable{

		public Long thisMsgId = null;
		
		@Override
		public void run() {
			try{
				if(thisMsgId != null){
					ExecuteSendMsgTask task = new ExecuteSendMsgTask();
					task.executeSendMsg(thisMsgId);
				}
			}
			catch(Exception e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}

	/**
	 * Send Message 
	 * 
	 * @param sendMsgModel
	 * @param adminUserAccount
	 * @throws Exception
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void sendMessage(SendMsgModel sendMsgModel, String adminUserAccount) throws Exception{

		/**
		 * Send to Group
		 */
		Long groupId = sendMsgModel.getSendGroupId();
		if(groupId == null){
			logger.error("SendGroupId Error");
			throw new BcsNoticeException("群組設定錯誤");
		}
		
		// Validate SendGroupId
		SendGroup sendGroup = sendGroupService.findOne(groupId);
		if(sendGroup == null){
			logger.error("SendGroupId Null");
			throw new BcsNoticeException("群組設定錯誤");
		}
		
		String sendingMsgType = sendMsgModel.getSendingMsgType();
		if(MsgMain.SENDING_MSG_TYPE_IMMEDIATE.equals(sendingMsgType)){
			
			// 行銷人員設定 群組
			if(sendGroup.getGroupId() > 0){
				try{
					List<String> mids =  groupGenerateService.findMIDBySendGroupDetailGroupId(sendGroup.getGroupId());
					if(mids != null && mids.size() >0){
						// 設定成功
					}
					else{
						throw new BcsNoticeException("群組設定錯誤:查不到發送目標");
					}
				}
				catch(Exception e){
					logger.error("SendGroup Send Error");
					throw new BcsNoticeException("群組設定錯誤:查不到發送目標");
				}
			}
			// 預設群祖
			else{
				Long result= sendGroupService.countDefaultGroupSize(sendGroup.getGroupId());
				if(result != null){
					if(result > 0){
						// 設定成功
					}
					else{
						throw new BcsNoticeException("群組設定錯誤:查不到發送目標");
					}
				}
				else{
					throw new Exception("SendGroup Send Error");
				}
			}
			
			// Handle : IMMEDIATE
			Long msgId = this.saveMessage(sendMsgModel, adminUserAccount, MsgMain.MESSAGE_STATUS_SCHEDULED);

			ExecuteSendMsgRunnable run = new ExecuteSendMsgRunnable();
			run.thisMsgId = msgId;
			
			Thread thread = new Thread(run);
			
			thread.start();
		}
		else if(MsgMain.SENDING_MSG_TYPE_DELAY.equals(sendingMsgType)){

			// Handle : DELAY
			// Validate ScheduleTime By Parse
			Date startTime = schedulerService.parseToDate(sendMsgModel.getSendingMsgTime());
			Date now = new Date();
			
			// Validate Time
			if(now.compareTo(startTime) >= 0){
				throw new BcsNoticeException("設定時間必須大於現在");
			}
			
			Long msgId = this.saveMessage(sendMsgModel, adminUserAccount, MsgMain.MESSAGE_STATUS_SCHEDULED);
			
			schedulerService.addMsgSendSchedule(msgId, startTime);
		}
		else if(MsgMain.SENDING_MSG_TYPE_SCHEDULE.equals(sendingMsgType)){

			// Handle : SCHEDULE
			// Validate ScheduleTime By Parse
			String cronExpression = schedulerService.parseToCronExpression(sendMsgModel.getSendingMsgTime());
			
			Long msgId = this.saveMessage(sendMsgModel, adminUserAccount, MsgMain.MESSAGE_STATUS_SCHEDULED);
			
			schedulerService.addMsgSendSchedule(msgId, cronExpression);
		}
		else{
			throw new Exception("UnKnow SendingMsgType : " + sendingMsgType);
		}
	}
	
	/**
	 * Redesign Message Set Status to DRAFT
	 * 
	 * @param sendMsgModel
	 * @param adminUserAccount
	 * @throws Exception
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void redesignMsg(SendMsgModel sendMsgModel, String adminUserAccount) throws Exception{ 

		MsgMain msgMain = msgMainService.findOne(sendMsgModel.getMsgId());
		
		if(msgMain != null){
			msgMain.setStatus(MsgMain.MESSAGE_STATUS_DRAFT);
			msgMain.setModifyUser(adminUserAccount);
			msgMain.setModifyTime(new Date());
	
			msgMainService.save(msgMain);
			
			// Remove Message from Scheduler
			schedulerService.deleteMsgSendSchedule(msgMain.getMsgId());
		}
		else{
			logger.error("Redesign Msg Not Found MsgId:" + sendMsgModel.getMsgId());
			throw new BcsNoticeException("請選擇正確的訊息");
		}
	}

	private List<Message> validateMessagesWichMessage(List<SendMsgDetailModel> sendMsgDetails, String mid) throws Exception{
		return validateMessagesWichMessage(sendMsgDetails, mid, null);
	}
	
	private List<Message> validateMessagesWichMessage(List<SendMsgDetailModel> sendMsgDetails, String mid, Map<String, String> replaceParam) throws Exception{

		List<Message> result = new ArrayList<Message>();
		for(SendMsgDetailModel sendMsgDetail : sendMsgDetails){
				
			MsgGenerator msg = MsgGeneratorFactory.createFromNode(sendMsgDetail.getDetailType(), createObjectNode(sendMsgDetail.getDetailContent()));
			if(msg != null){
				Message message = msg.getMessageBot(mid, replaceParam); 
				if(message != null){
					result.add(message);
				}
				else{
					logger.error("Message Generator Error");
					throw new BcsNoticeException("訊息產生錯誤");
				}
			}
			else{
				logger.error("Message Generator Error");
				throw new BcsNoticeException("訊息產生錯誤");
			}
		}
		
		return result;
	}
	
	/**
	 * Validate Message and Create MsgGenerator
	 * 
	 * @param sendMsgDetails
	 * @return List<MsgGenerator>
	 * @throws Exception
	 */
	private List<MsgGenerator> validateMessages(List<SendMsgDetailModel> sendMsgDetails) throws Exception{

		if(sendMsgDetails != null && sendMsgDetails.size() > 0){
			List<MsgGenerator> result = new ArrayList<MsgGenerator>();
			
			for(SendMsgDetailModel sendMsgDetail : sendMsgDetails){
					
				MsgGenerator msg = MsgGeneratorFactory.createFromNode(sendMsgDetail.getDetailType(), createObjectNode(sendMsgDetail.getDetailContent()));
				if(msg != null){
					result.add(msg);
				}
				else{
					logger.error("Message Generator Error");
					throw new BcsNoticeException("訊息產生錯誤");
				}
			}
			
			return result;
		}

		logger.error("SendMsgDetails Null");
		throw new BcsNoticeException("必須至少一個訊息");
	}
	
	/**
	 * Parse msgStr to ObjectNode
	 * 
	 * @param msgStr
	 * @return ObjectNode
	 * @throws Exception
	 */
	public ObjectNode createObjectNode(String msgStr) throws Exception{
		ObjectNode node = (ObjectNode)(new ObjectMapper()).readTree(msgStr);
		
		if(node.get("resourceType") != null && StringUtils.isNotBlank(node.get("resourceType").textValue())){
			String resourceType = node.get("resourceType").textValue();
			String resourceId = node.get("resourceId").textValue();
			if(ContentResource.RESOURCE_TYPE_IMAGE.equals(resourceType)){
				node.put("originalContentUrl",UriHelper.getResourceUri(resourceType, resourceId));
				node.put("previewImageUrl", UriHelper.getResourcePreviewUri(resourceType, "IMAGE", resourceId));
			}
			else if(ContentResource.RESOURCE_TYPE_VIDEO.equals(resourceType)){
				node.put("originalContentUrl",UriHelper.getResourceUri(resourceType, resourceId));
				node.put("previewImageUrl", UriHelper.getResourcePreviewUri(resourceType, "IMAGE", resourceId));
			}
			else if(ContentResource.RESOURCE_TYPE_AUDIO.equals(resourceType)){
				node.put("originalContentUrl",UriHelper.getResourceUri(resourceType, resourceId));
				node.put("AUDLEN", node.get("resourceLength").textValue());
			}
		}
		
		return node;
	}
	
	/**
	 * Set Detail Content
	 * 
	 * @param result
	 * @param details
	 */
	@SuppressWarnings("unchecked")
	public void setDetailContent(Map<String, Object> result,  List<MsgDetail> details){
		try{
			logger.debug("setDetailContent:" + details);
			Map<String, Object> resourceMap = (Map<String, Object>) result.get("ResourceMap");
			if(resourceMap == null){
				resourceMap = new HashMap<String, Object>();
			}
			
			if(details != null){
				for(MsgDetail detail : details){
					String referenceId = detail.getReferenceId();
					if(resourceMap.get(referenceId) == null){
						 if(MsgGenerator.MSG_TYPE_LINK.equals(detail.getMsgType())){
							 ContentLink contentLink = contentLinkService.findOne(referenceId);
							 resourceMap.put(referenceId, contentLink);
						 }
						 else if(MsgGenerator.MSG_TYPE_RICH_MSG.equals(detail.getMsgType())){
							 ContentRichMsg richMsg = contentRichMsgService.getSelectedContentRichMsg(referenceId);
							 resourceMap.put(referenceId, richMsg);
						 }
						 else if(MsgGeneratorExtend.MSG_TYPE_INTERACTIVE_LINK.equals(detail.getMsgType())){
							 ContentLink contentLink = contentLinkService.findOne(referenceId);
							 resourceMap.put(referenceId, contentLink);
						 }
						 else if(MsgGeneratorExtend.MSG_TYPE_COUPON.equals(detail.getMsgType())){
							 ContentCoupon contentCoupon = contentCouponService.findOne(referenceId);
							 resourceMap.put(referenceId, contentCoupon);
						 }else if(MsgGeneratorExtend.MSG_TYPE_REWARDCARD.equals(detail.getMsgType())){
							 ContentRewardCard contentRewardCard = contentRewardCardService.findOne(referenceId);
							 resourceMap.put(referenceId, contentRewardCard);
						 }
						 else if(MsgGeneratorExtend.MSG_TYPE_TEMPLATE.equals(detail.getMsgType())){
							 ContentTemplateMsg contentTemplateMsg = contentTemplateMsgService.getSelectedContentTemplateMsg(referenceId);
							 resourceMap.put(referenceId, contentTemplateMsg);
						 }
					}
				 }
			}
			
			result.put("ResourceMap", resourceMap);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	public void setResourceMap(Map<String, Object> result, String referenceId, Object obj){

		@SuppressWarnings("unchecked")
		Map<String, Object> resourceMap = (Map<String, Object>) result.get("ResourceMap");
		if(resourceMap == null){
			resourceMap = new HashMap<String, Object>();
			result.put("ResourceMap", resourceMap);
		}

		if(resourceMap.get(referenceId) == null){
			resourceMap.put(referenceId, obj);
		}
	}
	
	/**
	 * Set Groups Response
	 * 
	 * @param result
	 */
	public void setGroups(Map<String, Object> result){

		try{
			Map<Long, String> groups = sendGroupService.findGroupTitleMap();
			result.put("SendGroup", groups);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
	}
}
