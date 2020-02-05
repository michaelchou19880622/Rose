package com.bcs.core.taishin.circle.pnp.service;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.api.msg.MsgGeneratorFactory;
import com.bcs.core.api.service.model.PostLineResponse;
import com.bcs.core.bot.api.model.SendToBotModel;
import com.bcs.core.bot.api.service.BcsApiClusterService;
import com.bcs.core.bot.api.service.LineAccessApiService;
import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.db.service.MsgBotReceiveService;
import com.bcs.core.bot.enums.SEND_TYPE;
import com.bcs.core.bot.record.service.CatchRecordReceive;
import com.bcs.core.bot.send.akka.model.AsyncSendingClusterModel;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.MsgApiSendRecord;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgInteractiveMain;
import com.bcs.core.db.entity.MsgSendMain;
import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.MsgApiSendRecordService;
import com.bcs.core.db.service.MsgDetailService;
import com.bcs.core.db.service.MsgInteractiveMainService;
import com.bcs.core.db.service.SerialSettingService;
import com.bcs.core.db.service.UserLiveChatService;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.interactive.service.InteractiveService;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.record.akke.model.MsgApiSendRecordModel;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.send.akka.model.AsyncSendingModel;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.pnp.akka.CircleAkkaBotService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.response.BotApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "PnpRecorder")
@Service
public class PnpSendingMsgService {

    @Autowired
    private MsgDetailService msgDetailService;
    @Autowired
    private MsgInteractiveMainService msgInteractiveMainService;
    @Autowired
    private CircleAkkaBotService akkaBotService;
    @Autowired
    private InteractiveService interactiveService;
    @Autowired
    private LineUserService lineUserService;
    @Autowired
    private SerialSettingService serialSettingService;
    @Autowired
    private MsgApiSendRecordService msgApiSendRecordService;
    @Autowired
    private UserTraceLogService userTraceLogService;
    @Autowired
    private UserLiveChatService userLiveChatService;

    public void sendToLineAsync(MsgGenerator msgGenerator, List<String> mids, API_TYPE apiType) throws Exception {
        List<MsgGenerator> msgGenerators = new ArrayList<MsgGenerator>();
        msgGenerators.add(msgGenerator);
        sendToLineAsync(CONFIG_STR.DEFAULT.toString(), msgGenerators, null, mids, apiType, null);
    }

    public void sendToLineAsync(MsgGenerator msgGenerator, List<MsgDetail> details, List<String> mids, API_TYPE apiType, Long updateMsgId) throws Exception {
        List<MsgGenerator> msgGenerators = new ArrayList<MsgGenerator>();
        msgGenerators.add(msgGenerator);
        sendToLineAsync(CONFIG_STR.DEFAULT.toString(), msgGenerators, details, mids, apiType, updateMsgId);
    }

    public void sendToLineAsync(List<MsgGenerator> msgGenerators, List<MsgDetail> details, List<String> mids, API_TYPE apiType, Long updateMsgId) throws Exception {
        sendToLineAsync(CONFIG_STR.DEFAULT.toString(), msgGenerators, details, mids, apiType, updateMsgId);
    }

    public void sendToLineAsync(String ChannelId, List<MsgGenerator> msgGenerators, List<MsgDetail> details, List<String> mids, API_TYPE apiType, Long updateMsgId) throws Exception {
        log.debug("sendToLineAsync");
        if (ChannelId == null) {
            ChannelId = CONFIG_STR.DEFAULT.toString();
        }

        AsyncSendingModel msgs;
        if (updateMsgId != null) {
            msgs = new AsyncSendingModel(ChannelId, msgGenerators, mids, apiType, updateMsgId);
        } else {
            msgs = new AsyncSendingModel(ChannelId, msgGenerators, mids, apiType);
        }
        log.debug("sendToLineAsync:Mids:" + mids.size());

        // Share Sending Action to Different Server
        if (details != null) {
            log.info("sendToLineAsync start, time=" + new Date().getTime());
            boolean sendThis = CoreConfigReader.getBoolean(CONFIG_STR.BCS_API_CLUSTER_SEND_THIS.toString());
            String url = CoreConfigReader.getString(CONFIG_STR.BCS_API_CLUSTER_SEND.toString());

            if (sendThis) {
                log.info("sendToLineAsync:This Server:Mids:" + mids.size());
                akkaBotService.sendingMsgs(msgs);
            } else {
                if (StringUtils.isNotBlank(url)) {
                    try {// Call Different Server
                        log.info("sendToLineAsync:Different Server:Mids:" + mids.size());
                        AsyncSendingClusterModel model = new AsyncSendingClusterModel(ChannelId, details, mids, apiType.toString(), updateMsgId);
                        PostLineResponse response = BcsApiClusterService.clusterApiSend(model);
                        if (200 != response.getStatus()) {
                            throw new Exception(response.toString());
                        }
                    } catch (Exception e) {
                        log.error(ErrorRecord.recordError(e));
                        log.info("Cluster send message failed,Use this server sendToLineAsync:This Server:Mids:" + mids.size());
                        akkaBotService.sendingMsgs(msgs);
                    }
                } else {
                    log.info("Cluster url is empty,sendToLineAsync:This Server:Mids:" + mids.size());
                    akkaBotService.sendingMsgs(msgs);
                }
            }
            log.info("sendToLineAsync stop, time=" + new Date().getTime());
        } else {
            log.info("sendToLineAsync:This Server:Mids:" + mids.size());
            akkaBotService.sendingMsgs(msgs);
        }
    }

    public void sendMatchMessage(String replyToken, Long iMsgId, String ChannelId, String MID, String ApiType, String targetId) throws Exception {
        this.sendMatchMessage(replyToken, iMsgId, ChannelId, MID, ApiType, targetId, 0);
    }

    public void sendMatchMessage(String replyToken, Long iMsgId, List<MsgDetail> details, String ChannelId, String MID, String ApiType, String targetId) throws Exception {
        this.sendMatchMessage(replyToken, iMsgId, details, ChannelId, MID, ApiType, targetId, 0);
    }

    public void sendMatchMessage(String replyToken, Long iMsgId, String ChannelId, String MID, String ApiType, String targetId, int retryCount) throws Exception {

        if (iMsgId != null) {
            // 關鍵字回應 內容
            List<MsgDetail> details = msgDetailService.findByMsgIdAndMsgParentType(iMsgId, MsgInteractiveMain.THIS_PARENT_TYPE);

            this.sendMatchMessage(replyToken, iMsgId, details, ChannelId, MID, ApiType, targetId, retryCount);
        }
    }

    public void sendMatchMessage(String replyToken, Long iMsgId, List<MsgDetail> details, String ChannelId, String MID, String ApiType, String targetId, int retryCount) throws Exception {

        if (details != null && details.size() > 0) {

            MsgInteractiveMain msgInteractiveMain = null;
            if (iMsgId != null) {
                msgInteractiveMain = msgInteractiveMainService.findOne(iMsgId);
            }

            Map<String, String> replaceParam = null;

            if (msgInteractiveMain != null) {

                if (StringUtils.isNotBlank(msgInteractiveMain.getSerialId())) {
                    replaceParam = serialSettingService.getSerialSettingReplaceParam(msgInteractiveMain.getSerialId(), MID);
                    if (replaceParam == null) {
                        return;
                    }
                }
            }

            List<Message> messageList = MsgGeneratorFactory.validateMessagesWichMessage(details, MID, replaceParam);

            SendToBotModel sendToBotModel = new SendToBotModel();

            sendToBotModel.setChannelId(ChannelId);
            sendToBotModel.setSendType(SEND_TYPE.REPLY_MSG);
            sendToBotModel.setChannelName(CONFIG_STR.AUTO_REPLY.toString());

            String codeError = "";

            // 回覆 關鍵字回應內容
            try {

                ReplyMessage replyMessage = new ReplyMessage(replyToken, messageList);
                sendToBotModel.setReplyMessage(replyMessage);

                Response<BotApiResponse> response = LineAccessApiService.sendToLine(sendToBotModel);
                log.debug("status:" + response.code());

                if (response.code() == 400) {
                    codeError = "400Error";
                }

                // Check Response Status
                checkStatus(response, MID, iMsgId);

                this.saveLog(MID, response, iMsgId, targetId, targetId, LOG_TARGET_ACTION_TYPE.TARGET_InteractiveMsg, LOG_TARGET_ACTION_TYPE.ACTION_SendMatchMessage);
            } catch (Exception e) {
                log.error(ErrorRecord.recordError(e));
                if (retryCount < 5 && StringUtils.isBlank(codeError)) {
                    this.sendMatchMessage(replyToken, iMsgId, details, ChannelId, MID, ApiType, targetId, retryCount + 1);
                } else {
                    // Call Line Fail >= 5
                }
            }

            if (retryCount == 0) {
                // Update 關鍵字回應 記數
                msgInteractiveMainService.increaseSendCountByMsgInteractiveId(iMsgId);
//				ApplicationContextProvider.getApplicationContext().getBean(AkkaCoreService.class).recordMsgs(new MsgInteractiveRecord(iMsgId));
            }
        }
    }

    public void sendMatchMessage(Long iMsgId, String ChannelId, String MID, String ApiType, String targetId) throws Exception {
        this.sendMatchMessage(iMsgId, ChannelId, MID, ApiType, targetId, 0);
    }

    public void sendMatchMessage(Long iMsgId, List<MsgDetail> details, String ChannelId, String MID, String ApiType, String targetId) throws Exception {
        this.sendMatchMessage(iMsgId, details, ChannelId, MID, ApiType, targetId, 0);
    }

    public void sendMatchMessage(Long iMsgId, String ChannelId, String MID, String ApiType, String targetId, int retryCount) throws Exception {

        if (iMsgId != null) {
            // 關鍵字回應 內容
            List<MsgDetail> details = msgDetailService.findByMsgIdAndMsgParentType(iMsgId, MsgInteractiveMain.THIS_PARENT_TYPE);

            this.sendMatchMessage(iMsgId, details, ChannelId, MID, ApiType, targetId, retryCount);
        }
    }

    public void sendMatchMessage(Long iMsgId, List<MsgDetail> details, String ChannelId, String MID, String ApiType, String targetId, int retryCount) throws Exception {

        if (details != null && details.size() > 0) {

            MsgInteractiveMain msgInteractiveMain = null;
            if (iMsgId != null) {
                msgInteractiveMain = msgInteractiveMainService.findOne(iMsgId);
            }

            Map<String, String> replaceParam = null;

            if (msgInteractiveMain != null) {

                if (StringUtils.isNotBlank(msgInteractiveMain.getSerialId())) {
                    replaceParam = serialSettingService.getSerialSettingReplaceParam(msgInteractiveMain.getSerialId(), MID);
                    if (replaceParam == null) {
                        return;
                    }
                }
            }

            List<Message> messageList = MsgGeneratorFactory.validateMessagesWichMessage(details, MID, replaceParam);

            SendToBotModel sendToBotModel = new SendToBotModel();

            sendToBotModel.setChannelId(ChannelId);
            sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);
            sendToBotModel.setChannelName(CONFIG_STR.AUTO_REPLY.toString());

            // 回覆 關鍵字回應內容
            try {
                PushMessage pushMessage = new PushMessage(MID, messageList);
                sendToBotModel.setPushMessage(pushMessage);

                Response<BotApiResponse> response = LineAccessApiService.sendToLine(sendToBotModel);
                log.debug("status:" + response.code());

                // Check Response Status
                checkStatus(response, MID, iMsgId);

                this.saveLog(MID, response, iMsgId, targetId, targetId, LOG_TARGET_ACTION_TYPE.TARGET_InteractiveMsg, LOG_TARGET_ACTION_TYPE.ACTION_SendMatchMessage);
            } catch (Exception e) {
                log.error(ErrorRecord.recordError(e));
                if (retryCount < 5) {
                    this.sendMatchMessage(iMsgId, details, ChannelId, MID, ApiType, targetId, retryCount + 1);
                } else {
                    // Call Line Fail >= 5
                }
            }

            if (retryCount == 0) {
                // Update 關鍵字回應 記數
                msgInteractiveMainService.increaseSendCountByMsgInteractiveId(iMsgId);
//				ApplicationContextProvider.getApplicationContext().getBean(AkkaCoreService.class).recordMsgs(new MsgInteractiveRecord(iMsgId));
            }
        }
    }

    public void sendEventMessage(String mid, String SendEvent) {
        try {
            log.debug("sendEventMessage:" + SendEvent);
            if (StringUtils.isNotBlank(SendEvent)) {

                // Record Match Message
                LineUser lineUser = lineUserService.findByMidAndCreateUnbind(mid);

                // 取得 關鍵字回應 設定
                Long iMsgId = interactiveService.getEventWelcomeResponse(lineUser.getStatus());
                log.debug("Get Event Welcome iMsgId:" + iMsgId);

                if (iMsgId != null) {
                    // 傳送 關鍵字回應
                    List<MsgDetail> details = interactiveService.getMsgDetails(iMsgId);

                    ApplicationContextProvider.getApplicationContext().getBean(PnpSendingMsgService.class).sendMatchMessage(iMsgId, details, CONFIG_STR.DEFAULT.toString(), mid, API_TYPE.BOT.toString(), SendEvent);

                    // incrementCount CatchRecord Receive
                    ApplicationContextProvider.getApplicationContext().getBean(CatchRecordReceive.class).incrementCount();
                    ;

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date time = new Date();

                        MsgBotReceive receive = new MsgBotReceive();

                        receive.setChannel(CONFIG_STR.DEFAULT.toString());

                        receive.setEventType(MsgBotReceive.EVENT_TYPE_BCSEVENT);
                        receive.setMsgId("-");
                        receive.setMsgType(MsgBotReceive.EVENT_TYPE_BCSEVENT);
                        receive.setReceiveTime(time);
                        receive.setReceiveDay(sdf.format(time));
                        receive.setReferenceId(iMsgId.toString());
                        receive.setSourceId(mid);
                        receive.setSourceType(MsgBotReceive.SOURCE_TYPE_USER);
                        receive.setTimestamp(time.getTime());

                        // Set User Status
                        receive.setUserStatus(lineUser.getStatus());

                        // Save Record
                        ApplicationContextProvider.getApplicationContext().getBean(MsgBotReceiveService.class).bulkPersist(receive);
                    } catch (Exception e) {
                        log.error(ErrorRecord.recordError(e));
                    }
                }
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    private String checkStatus(Response<BotApiResponse> response, String mid, Long msgId) throws Exception {

        log.debug("status:" + response.code());
        String recordStatus = response.code() + "-";

        if (response.code() != 200) {
            List<Object> content = new ArrayList<Object>();
            content.add(mid);
            content.add(msgId);
            content.add(response.code());
            content.add(response.body());
            if (response.errorBody() != null) {
                content.add(response.errorBody().string());
            }
            SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendToLineApiStatus, content, mid);
            throw new BcsNoticeException("發送訊息錯誤請洽資訊人員:" + response.code());
        }

        return recordStatus;
    }

    private void saveLog(String mid, Response<BotApiResponse> responseMulti, Long iMsgId, String targetId, String referenceId, LOG_TARGET_ACTION_TYPE target, LOG_TARGET_ACTION_TYPE action) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date now = new Date();

            // SendMatchMessage Log
            UserTraceLog log = new UserTraceLog();
            log.setTarget(target);
            log.setAction(action);
            log.setModifyTime(now);
            log.setModifyUser(mid);
            log.setLevel(UserTraceLog.USER_TRACE_LOG_LEVEL_TRACE);
            log.setModifyDay(sdf.format(now));

            List<Object> content = new ArrayList<Object>();
            content.add(iMsgId);
            content.add(mid);
            content.add(targetId);
            content.add(responseMulti);
            log.setContent(content);
            log.setReferenceId(referenceId);
            userTraceLogService.bulkPersist(log);
//			ApplicationContextProvider.getApplicationContext().getBean(AkkaCoreService.class).recordMsgs(log);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    public void sendMsgToMids(List<String> mids, List<MsgGenerator> msgGenerators, List<MsgDetail> details, boolean async, Long updateMsgId, int retryCount, LOG_TARGET_ACTION_TYPE target, LOG_TARGET_ACTION_TYPE action, String referenceId) throws Exception {
        log.info("sendMsgToMid:" + mids);

        if (mids != null && mids.size() > 0 && msgGenerators != null && msgGenerators.size() > 0) {

            if (async) {
                this.sendToLineAsync(msgGenerators, details, mids, API_TYPE.BOT, updateMsgId);
            } else {

                SendToBotModel sendToBotModel = new SendToBotModel();

                sendToBotModel.setChannelId(CONFIG_STR.DEFAULT.toString());
                sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);

                List<Message> sendStrList = new ArrayList<Message>();
                for (MsgGenerator msgGenerator : msgGenerators) {
                    sendStrList.add(msgGenerator.getMessageBot(""));
                }

                Map<String, UserLiveChat> inprogressMids = userLiveChatService.findByStauts(UserLiveChat.IN_PROGRESS);

                String sendStr = ObjectUtil.objectToJsonStr(sendStrList);
                for (String mid : mids) {
                    try {

                        List<Message> messageList = new ArrayList<Message>();
                        for (MsgGenerator msgGenerator : msgGenerators) {
                            messageList.add(msgGenerator.getMessageBot(mid));
                        }

                        PushMessage pushMessage = new PushMessage(mid, messageList);
                        sendToBotModel.setPushMessage(pushMessage);

                        String channelName = inprogressMids.get(mid) != null ? CONFIG_STR.IN_MANUAL_REPLY_BUT_NOT_SEND_MSG.toString() : CONFIG_STR.AUTO_REPLY.toString();
                        sendToBotModel.setChannelName(channelName);

                        Response<BotApiResponse> responseMulti = LineAccessApiService.sendToLine(sendToBotModel);

                        log.info("statusMulti:" + responseMulti.code());

                        // Check Response Status
                        this.checkStatus(responseMulti, mid, null);

                        this.saveLog(mid, responseMulti, -1L, sendStr, referenceId, target, action);
                    } catch (Exception e) {
                        log.error(ErrorRecord.recordError(e));
                        if (retryCount < 5) {
                            List<String> midsRetry = new ArrayList<String>();
                            midsRetry.add(mid);
                            this.sendMsgToMids(midsRetry, msgGenerators, details, async, updateMsgId, retryCount + 1, target, action, referenceId);
                            break;
                        } else {
                            throw e;
                        }
                    }
                }
            }
        }
    }

    public void sendApiMessage(MsgApiSendRecordModel msgApiSendRecordModel, String ChannelId, String ApiType, int retryCount) throws Exception {

        List<Message> messageList = msgApiSendRecordModel.getMessage();
        String MID = msgApiSendRecordModel.getMsgApiSendRecord().getMid();

        if (messageList != null && messageList.size() > 0) {
            Map<String, UserLiveChat> inprogressMids = userLiveChatService.findByStauts(UserLiveChat.IN_PROGRESS);

            SendToBotModel sendToBotModel = new SendToBotModel();

            sendToBotModel.setChannelId(ChannelId);
            sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);

            String channelName = inprogressMids.get(MID) != null ? CONFIG_STR.IN_MANUAL_REPLY_BUT_NOT_SEND_MSG.toString() : CONFIG_STR.AUTO_REPLY.toString();
            sendToBotModel.setChannelName(channelName);

            // 回覆 關鍵字回應內容
            try {
                PushMessage pushMessage = new PushMessage(MID, messageList);
                sendToBotModel.setPushMessage(pushMessage);

                Response<BotApiResponse> response = LineAccessApiService.sendToLine(sendToBotModel);
                log.debug("status:" + response.code());

                // Check Response Status
                String recordStatus = checkStatus(response, MID, null);

                MsgApiSendRecord record = msgApiSendRecordModel.getMsgApiSendRecord();
                record.setApiType(ApiType);
                record.setChannelId(ChannelId);
                record.setStatus(MsgSendMain.MESSAGE_STATUS_FINISH);
                record.setSendRecord(recordStatus);

                this.recordMsgs(record);
            } catch (Exception e) {
                log.error(ErrorRecord.recordError(e));
                if (retryCount < 5) {
                    this.sendApiMessage(msgApiSendRecordModel, ChannelId, ApiType, retryCount + 1);
                } else {
                    // Call Line Fail >= 5
                    throw e;
                }
            }
        }
    }

    private void recordMsgs(MsgApiSendRecord msgApiSendRecord) {

        try {
//            akkaCoreService.recordMsgs(msgApiSendRecord);
            msgApiSendRecordService.bulkPersist(msgApiSendRecord);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

}
