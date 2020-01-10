package com.bcs.core.bot.send.akka.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.bot.api.model.SendToBotModel;
import com.bcs.core.bot.api.service.LineAccessApiService;
import com.bcs.core.bot.enums.SEND_TYPE;
import com.bcs.core.db.entity.ContentEsnDetail;
import com.bcs.core.db.entity.MsgSendMain;
import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.service.MsgSendMainService;
import com.bcs.core.db.service.SerialSettingService;
import com.bcs.core.db.service.UserLiveChatService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.send.akka.model.AsyncEsnSendingModel;
import com.bcs.core.send.akka.model.AsyncEsnSendingModelError;
import com.bcs.core.send.akka.model.AsyncEsnSendingModelSuccess;
import com.bcs.core.send.akka.model.AsyncSendingModel;
import com.bcs.core.send.akka.model.AsyncSendingModelError;
import com.bcs.core.send.akka.model.AsyncSendingModelSuccess;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.ErrorRecord;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;

import akka.actor.UntypedActor;
import retrofit2.Response;

public class SendingMsgHandlerSend extends UntypedActor {

	/** Logger */
	private static Logger logger = Logger.getLogger(SendingMsgHandlerSend.class);

	@Override
	public void onReceive(Object message) throws Exception {
		SerialSettingService serialSettingService = ApplicationContextProvider.getApplicationContext().getBean(SerialSettingService.class);
		UserLiveChatService userLiveChatService = ApplicationContextProvider.getApplicationContext().getBean(UserLiveChatService.class);

		Map<String, UserLiveChat> inprogressMids = userLiveChatService.findByStauts(UserLiveChat.IN_PROGRESS);

		if (message instanceof AsyncSendingModel) {
			AsyncSendingModel msgs = (AsyncSendingModel) message;
			logger.info("AsyncSendingModel onReceive:" + msgs);

			if(msgs.getMidList() != null){


				Long msgSendId = msgs.getUpdateMsgId();
				MsgSendMain msgSendMain = null;
				if(msgSendId != null){
					msgSendMain = ApplicationContextProvider.getApplicationContext().getBean(MsgSendMainService.class).findOne(msgSendId);
				}

				logger.info("Size:" + msgs.getMidList().size());
				List<String[]> successMid = new ArrayList<String[]>();

				List<MsgGenerator> msgGenerators = msgs.getMsgGenerators();

				SendToBotModel sendToBotModel = new SendToBotModel();

				sendToBotModel.setChannelId(msgs.getChannelId());
				sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);

				for(String mid : msgs.getMidList()){
					try {

						Map<String, String> replaceParam = null;

						if(msgSendMain != null){

							if(StringUtils.isNotBlank(msgSendMain.getSerialId())){
								replaceParam = serialSettingService.getSerialSettingReplaceParam(msgSendMain.getSerialId(), mid);
								if(replaceParam == null){
									continue;
								}
							}
						}

						List<Message> messageList = new ArrayList<Message>();
						for(MsgGenerator msgGenerator : msgGenerators){
							messageList.add(msgGenerator.getMessageBot(mid, replaceParam));
						}

						String recordStatus = "";
						PushMessage pushMessage = new PushMessage(mid, messageList);
						sendToBotModel.setPushMessage(pushMessage);

						String channelName = inprogressMids.get(mid) != null ? CONFIG_STR.InManualReplyButNotSendMsg.toString() : CONFIG_STR.AutoReply.toString();
						sendToBotModel.setChannelName(channelName);

						Response<BotApiResponse> response = LineAccessApiService.sendToLine(sendToBotModel);

						recordStatus = this.checkStatus(response, recordStatus, mid, msgs.getUpdateMsgId());

						//FIXME Usually status code is 400
						if(response.code() != 200){
							throw new Exception("PostLineResponse Status:" + response.code() );
						}
						String [] setting = new String[]{mid, recordStatus};
						successMid.add(setting);
					} catch (Exception e) {
						logger.error(ErrorRecord.recordError(e));
						logger.error("MID:" + mid);

						List<String> errorMid = new ArrayList<String>();
						errorMid.add(mid);
						AsyncSendingModelError error = new AsyncSendingModelError(msgs.getMsgGenerators(), msgs.getChannelId(), errorMid, msgs.getApiType(), e.getMessage(), msgs.getUpdateMsgId(), new Date());
						getSender().tell(error, getSelf());
					}
				}

				if(successMid != null && successMid.size() > 0){
					AsyncSendingModelSuccess success = new AsyncSendingModelSuccess(msgs.getChannelId(), successMid, msgs.getApiType(), msgs.getUpdateMsgId(), new Date());
					getSender().tell(success, getSelf());
				}
			}
			logger.debug("AsyncSendingModel End");
		}
		else if (message instanceof AsyncSendingModelError) {
			AsyncSendingModelError msgs = (AsyncSendingModelError) message;
			logger.debug("AsyncSendingModelError onReceive:" + msgs);

			if(msgs.getMids() != null){

				Long msgSendId = msgs.getUpdateMsgId();
				MsgSendMain msgSendMain = null;
				if(msgSendId != null){
					msgSendMain = ApplicationContextProvider.getApplicationContext().getBean(MsgSendMainService.class).findOne(msgSendId);
				}

				logger.debug("Size:" + msgs.getMids().size());

				List<MsgGenerator> msgGenerators = msgs.getMsgGenerators();

				SendToBotModel sendToBotModel = new SendToBotModel();

				sendToBotModel.setChannelId(CONFIG_STR.Default.toString());
				sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);

				List<String[]> successMid = new ArrayList<String[]>();
				for(String mid : msgs.getMids()){
					try {

						Map<String, String> replaceParam = null;

						if(msgSendMain != null){

							if(StringUtils.isNotBlank(msgSendMain.getSerialId())){
								replaceParam = serialSettingService.getSerialSettingReplaceParam(msgSendMain.getSerialId(), mid);
								if(replaceParam == null){
									continue;
								}
							}
						}

						List<Message> messageList = new ArrayList<Message>();
						for(MsgGenerator msgGenerator : msgGenerators){
							messageList.add(msgGenerator.getMessageBot(mid, replaceParam));
						}

						String recordStatus = "";
						PushMessage pushMessage = new PushMessage(mid, messageList);
						sendToBotModel.setPushMessage(pushMessage);

						String channelName = inprogressMids.get(mid) != null ? CONFIG_STR.InManualReplyButNotSendMsg.toString() : CONFIG_STR.AutoReply.toString();
						sendToBotModel.setChannelName(channelName);

						Response<BotApiResponse> response= LineAccessApiService.sendToLine(sendToBotModel);

						recordStatus = this.checkStatus(response, recordStatus, mid, msgs.getUpdateMsgId());

						//FIXME Usually status code is 400
						if(response.code() != 200){
							throw new Exception("PostLineResponse Status:" + response.code() );
						}
						String [] setting = new String[]{mid, recordStatus};
						successMid.add(setting);
					} catch (Exception e) {
						logger.error(ErrorRecord.recordError(e));
						logger.error("MID:" + mid);

						getSender().tell(msgs, getSelf());
					}
				}

				if(successMid != null && successMid.size() > 0){
					AsyncSendingModelSuccess success = new AsyncSendingModelSuccess(msgs.getChannelId(), successMid, msgs.getApiType(), msgs.getUpdateMsgId(), new Date());
					getSender().tell(success, getSelf());
				}
			}
			logger.debug("AsyncSendingModelError End");
		}
		else if(message instanceof AsyncEsnSendingModel) {
		    AsyncEsnSendingModel msgs = (AsyncEsnSendingModel)message;

		    if(msgs.getEsnDetails() != null) {

		        SendToBotModel sendToBotModel = new SendToBotModel();

                sendToBotModel.setChannelId(CONFIG_STR.Default.toString());
                sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);

                Date now = new Date();

                List<Long> successDetailIds = new ArrayList<>();

		        for(ContentEsnDetail esnDetail : msgs.getEsnDetails()) {

		            try {
		                List<Message> messageList = new ArrayList<>(msgs.getMessageList());
		                TextMessage esnMessage = new TextMessage(esnDetail.getEsn());
		                messageList.add(esnMessage);

		                String recordStatus = "";
                        PushMessage pushMessage = new PushMessage(esnDetail.getUid(), messageList);
                        sendToBotModel.setPushMessage(pushMessage);

                        String channelName = inprogressMids.get(esnDetail.getUid()) != null ? CONFIG_STR.InManualReplyButNotSendMsg.toString() : CONFIG_STR.AutoReply.toString();
						sendToBotModel.setChannelName(channelName);

                        Response<BotApiResponse> response = LineAccessApiService.sendToLine(sendToBotModel);

                        recordStatus = this.checkStatus(response, recordStatus, esnDetail.getUid(), esnDetail.getEsnDetailId());

                        if(response.code() != 200){
                            throw new Exception("Send ESN Msg PostLineResponse Status:" + response.code());
                        }

                        successDetailIds.add(esnDetail.getEsnDetailId());
		            }catch (Exception e) {
		                logger.error(ErrorRecord.recordError(e));

		                List<ContentEsnDetail> errorDetail = new ArrayList<>();
		                errorDetail.add(esnDetail);
		                AsyncEsnSendingModelError error = new AsyncEsnSendingModelError(msgs.getChannelId(), msgs.getMessageList(), errorDetail, msgs.getApiType(), now);

		                getSender().tell(error, getSelf());
		            }

		            if(successDetailIds != null && successDetailIds.size() > 0) {
		                AsyncEsnSendingModelSuccess success = new AsyncEsnSendingModelSuccess(successDetailIds, now);
		                getSender().tell(success, getSelf());
		            }
		        }
		    }
		}
		else if(message instanceof AsyncEsnSendingModelError) {
		    AsyncEsnSendingModelError msgs = (AsyncEsnSendingModelError)message;

		    if(msgs.getEsnDetails() != null) {

                SendToBotModel sendToBotModel = new SendToBotModel();

                sendToBotModel.setChannelId(CONFIG_STR.Default.toString());
                sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);

                Date now = new Date();

                List<Long> successDetailIds = new ArrayList<>();

                for(ContentEsnDetail esnDetail : msgs.getEsnDetails()) {

                    try {
                        List<Message> messageList = new ArrayList<>(msgs.getMessageList());
                        TextMessage esnMessage = new TextMessage(esnDetail.getEsn());
                        messageList.add(esnMessage);

                        String recordStatus = "";
                        PushMessage pushMessage = new PushMessage(esnDetail.getUid(), messageList);
                        sendToBotModel.setPushMessage(pushMessage);

                        String channelName = inprogressMids.get(esnDetail.getUid()) != null ? CONFIG_STR.InManualReplyButNotSendMsg.toString() : CONFIG_STR.AutoReply.toString();
						sendToBotModel.setChannelName(channelName);

                        Response<BotApiResponse> response = LineAccessApiService.sendToLine(sendToBotModel);

                        recordStatus = this.checkStatus(response, recordStatus, esnDetail.getUid(), esnDetail.getEsnDetailId());

                        if(response.code() != 200){
                            throw new Exception("Send ESN Msg PostLineResponse Status:" + response.code());
                        }

                        successDetailIds.add(esnDetail.getEsnDetailId());
                    }catch (Exception e) {
                        logger.error(ErrorRecord.recordError(e));

                        getSender().tell(msgs, getSelf());
                    }

                    if(successDetailIds != null && successDetailIds.size() > 0) {
                        AsyncEsnSendingModelSuccess success = new AsyncEsnSendingModelSuccess(successDetailIds, now);
                        getSender().tell(success, getSelf());
                    }
                }
            }
		}
	}

	private String checkStatus(Response<BotApiResponse> response, String recordStatus, String mid, Long msgId) throws Exception{

		logger.debug("status:" + response.code());
		recordStatus += response.code() + "-";

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
		}

		return recordStatus;
	}
}
