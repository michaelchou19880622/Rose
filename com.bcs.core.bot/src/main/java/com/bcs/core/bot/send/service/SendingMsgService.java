package com.bcs.core.bot.send.service;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.api.msg.MsgGeneratorFactory;
import com.bcs.core.api.service.model.PostLineResponse;
import com.bcs.core.bot.akka.service.AkkaBotService;
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
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.response.BotApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author ???
 */
@Slf4j
@Service
public class SendingMsgService {

    private MsgDetailService msgDetailService;
    private MsgInteractiveMainService msgInteractiveMainService;
    private AkkaBotService akkaBotService;
    private InteractiveService interactiveService;
    private LineUserService lineUserService;
    private SerialSettingService serialSettingService;
    private MsgApiSendRecordService msgApiSendRecordService;
    private UserTraceLogService userTraceLogService;
    private UserLiveChatService userLiveChatService;

    @Autowired
    public SendingMsgService(MsgDetailService msgDetailService,
                             MsgInteractiveMainService msgInteractiveMainService,
                             AkkaBotService akkaBotService,
                             InteractiveService interactiveService,
                             LineUserService lineUserService,
                             SerialSettingService serialSettingService,
                             MsgApiSendRecordService msgApiSendRecordService,
                             UserTraceLogService userTraceLogService,
                             UserLiveChatService userLiveChatService) {
        this.msgDetailService = msgDetailService;
        this.msgInteractiveMainService = msgInteractiveMainService;
        this.akkaBotService = akkaBotService;
        this.interactiveService = interactiveService;
        this.lineUserService = lineUserService;
        this.serialSettingService = serialSettingService;
        this.msgApiSendRecordService = msgApiSendRecordService;
        this.userTraceLogService = userTraceLogService;
        this.userLiveChatService = userLiveChatService;
    }

    public void sendToLineAsync(MsgGenerator msgGenerator, List<String> midList, API_TYPE apiType) throws Exception {
        List<MsgGenerator> msgGenerators = new ArrayList<>();
        msgGenerators.add(msgGenerator);
        sendToLineAsync(CONFIG_STR.DEFAULT.toString(), msgGenerators, null, midList, apiType, null);
    }

    public void sendToLineAsync(MsgGenerator msgGenerator, List<MsgDetail> details, List<String> midList, API_TYPE apiType, Long updateMsgId) throws Exception {
        List<MsgGenerator> msgGenerators = new ArrayList<>();
        msgGenerators.add(msgGenerator);
        sendToLineAsync(CONFIG_STR.DEFAULT.toString(), msgGenerators, details, midList, apiType, updateMsgId);
    }

    public void sendToLineAsync(List<MsgGenerator> msgGenerators, List<MsgDetail> details, List<String> midList, API_TYPE apiType, Long updateMsgId) throws Exception {
        sendToLineAsync(CONFIG_STR.DEFAULT.toString(), msgGenerators, details, midList, apiType, updateMsgId);
    }

    public void sendToLineAsync(String channelId, List<MsgGenerator> msgGenerators, List<MsgDetail> detailList, List<String> midList, API_TYPE apiType, Long updateMsgId) {
        log.info("---------- sendToLineAsync ----------");
        log.info("[sendToLineAsync] Mid List Size: {}", midList.size());

        if (channelId == null) {
            channelId = CONFIG_STR.DEFAULT.toString();
        }

        log.info("[sendToLineAsync] channelId: {}", channelId);

        AsyncSendingModel asyncSendingModel;
        if (updateMsgId != null) {
            asyncSendingModel = new AsyncSendingModel(channelId, msgGenerators, midList, apiType, updateMsgId);
        } else {
            asyncSendingModel = new AsyncSendingModel(channelId, msgGenerators, midList, apiType);
        }
        log.info("[sendToLineAsync] asyncSendingModel: {}", asyncSendingModel);

        if (detailList == null) {
            log.info("[sendToLineAsync] Detail List Is Null!!");
            akkaBotService.sendingMsgs(asyncSendingModel);
            return;
        }

        if (CoreConfigReader.getBoolean(CONFIG_STR.BCS_API_CLUSTER_SEND_THIS.toString())) {
            log.info("[sendToLineAsync] [rest.api.cluster.send.this] is True!!");
            akkaBotService.sendingMsgs(asyncSendingModel);
            return;
        }

        if (StringUtils.isBlank(CoreConfigReader.getString(CONFIG_STR.BCS_API_CLUSTER_SEND.toString()))) {
            log.info("[sendToLineAsync] [rest.api.cluster.send] is Blank!!");
            akkaBotService.sendingMsgs(asyncSendingModel);
            return;
        }

        try {
            log.info("[sendToLineAsync] Last!!");
            AsyncSendingClusterModel model = new AsyncSendingClusterModel(channelId, detailList, midList, apiType.toString(), updateMsgId);
            PostLineResponse response = BcsApiClusterService.clusterApiSend(model);
            log.info("[sendToLineAsync] response: {}", response);
            
            if (HttpStatus.OK.value() != response.getStatus()) {
                throw new Exception(response.toString());
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
            akkaBotService.sendingMsgs(asyncSendingModel);
        }
    }

    public void sendMatchMessage(String replyToken, Long iMsgId, String channelId, String mid, String apiType, String targetId) throws Exception {
        this.sendMatchMessage(replyToken, iMsgId, channelId, mid, apiType, targetId, 0);
    }

    public void sendMatchMessage(String replyToken, Long iMsgId, List<MsgDetail> details, String channelId, String mid, String apiType, String targetId) throws Exception {
        this.sendMatchMessage(replyToken, iMsgId, details, channelId, mid, apiType, targetId, 0);
    }

    public void sendMatchMessage(String replyToken, Long iMsgId, String channelId, String mid, String apiType, String targetId, int retryCount) throws Exception {

        if (iMsgId != null) {
            // 關鍵字回應 內容
            List<MsgDetail> details = msgDetailService.findByMsgIdAndMsgParentType(iMsgId, MsgInteractiveMain.THIS_PARENT_TYPE);

            this.sendMatchMessage(replyToken, iMsgId, details, channelId, mid, apiType, targetId, retryCount);
        }
    }

    public void sendMatchMessage(String replyToken, Long iMsgId, List<MsgDetail> details, String channelId, String mid, String apiType, String targetId, int retryCount) throws Exception {

        log.info("replyToken = " + replyToken);
        log.info("iMsgId = " + iMsgId);
        log.info("details = " + details);
        log.info("channelId = " + channelId);
        log.info("mid = " + mid);
        log.info("apiType = " + apiType);
        log.info("targetId = " + targetId);
        log.info("retryCount = " + retryCount);

        if (details != null && !details.isEmpty()) {

            MsgInteractiveMain msgInteractiveMain = null;

            if (iMsgId != null) {
                msgInteractiveMain = msgInteractiveMainService.findOne(iMsgId);
                log.info("msgInteractiveMain = " + msgInteractiveMain);
            }

            Map<String, String> replaceParam = null;

            if (msgInteractiveMain != null) {

                if (StringUtils.isNotBlank(msgInteractiveMain.getSerialId())) {
                    replaceParam = serialSettingService.getSerialSettingReplaceParam(msgInteractiveMain.getSerialId(), mid);
                    if (replaceParam == null) {
                        return;
                    }
                }
            }

            List<Message> messageList = MsgGeneratorFactory.validateMessagesWichMessage(details, mid, replaceParam);

            SendToBotModel sendToBotModel = new SendToBotModel();

            sendToBotModel.setChannelId(channelId);
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
                checkStatus(response, mid, iMsgId);

                this.saveLog(mid, response, iMsgId, targetId, targetId, LOG_TARGET_ACTION_TYPE.TARGET_InteractiveMsg, LOG_TARGET_ACTION_TYPE.ACTION_SendMatchMessage);
            } catch (Exception e) {
                log.error(ErrorRecord.recordError(e));
                if (retryCount < 5 && StringUtils.isBlank(codeError)) {
                    this.sendMatchMessage(replyToken, iMsgId, details, channelId, mid, apiType, targetId, retryCount + 1);
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

    public void sendMatchMessage(Long iMsgId, String channelId, String mid, String apiType, String targetId) throws Exception {
        this.sendMatchMessage(iMsgId, channelId, mid, apiType, targetId, 0);
    }

    public void sendMatchMessage(Long iMsgId, List<MsgDetail> details, String channelId, String mid, String apiType, String targetId) throws Exception {
        this.sendMatchMessage(iMsgId, details, channelId, mid, apiType, targetId, 0);
    }

    public void sendMatchMessage(Long iMsgId, String channelId, String mid, String apiType, String targetId, int retryCount) throws Exception {

        if (iMsgId != null) {
            // 關鍵字回應 內容
            List<MsgDetail> details = msgDetailService.findByMsgIdAndMsgParentType(iMsgId, MsgInteractiveMain.THIS_PARENT_TYPE);

            this.sendMatchMessage(iMsgId, details, channelId, mid, apiType, targetId, retryCount);
        }
    }

    public void sendMatchMessage(Long iMsgId, List<MsgDetail> detailList, String channelId, String mid, String apiType, String targetId, int retryCount) throws Exception {

        if (detailList != null && !detailList.isEmpty()) {

            MsgInteractiveMain msgInteractiveMain = null;
            if (iMsgId != null) {
                msgInteractiveMain = msgInteractiveMainService.findOne(iMsgId);
            }

            Map<String, String> replaceParam = null;

            if (msgInteractiveMain != null) {

                if (StringUtils.isNotBlank(msgInteractiveMain.getSerialId())) {
                    replaceParam = serialSettingService.getSerialSettingReplaceParam(msgInteractiveMain.getSerialId(), mid);
                    if (replaceParam == null) {
                        return;
                    }
                }
            }

            List<Message> messageList = MsgGeneratorFactory.validateMessagesWichMessage(detailList, mid, replaceParam);

            SendToBotModel sendToBotModel = new SendToBotModel();

            sendToBotModel.setChannelId(channelId);
            sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);
            sendToBotModel.setChannelName(CONFIG_STR.AUTO_REPLY.toString());

            // 回覆 關鍵字回應內容
            try {
                PushMessage pushMessage = new PushMessage(mid, messageList);
                sendToBotModel.setPushMessage(pushMessage);

                Response<BotApiResponse> response = LineAccessApiService.sendToLine(sendToBotModel);
                log.debug("status:" + response.code());

                // Check Response Status
                checkStatus(response, mid, iMsgId);

                this.saveLog(mid, response, iMsgId, targetId, targetId, LOG_TARGET_ACTION_TYPE.TARGET_InteractiveMsg, LOG_TARGET_ACTION_TYPE.ACTION_SendMatchMessage);
            } catch (Exception e) {
                log.error(ErrorRecord.recordError(e));
                if (retryCount < 5) {
                    this.sendMatchMessage(iMsgId, detailList, channelId, mid, apiType, targetId, retryCount + 1);
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

    public void sendEventMessage(String mid, String sendEvent) {
        try {
            log.debug("sendEventMessage:" + sendEvent);
            if (StringUtils.isNotBlank(sendEvent)) {

                // Record Match Message
                LineUser lineUser = lineUserService.findByMidAndCreateUnbind(mid);

                // 取得 關鍵字回應 設定
                Long iMsgId = interactiveService.getEventWelcomeResponse(lineUser.getStatus());
                log.debug("Get Event Welcome iMsgId:" + iMsgId);

                if (iMsgId != null) {
                    // 傳送 關鍵字回應
                    List<MsgDetail> details = interactiveService.getMsgDetails(iMsgId);

                    ApplicationContextProvider.getApplicationContext().getBean(SendingMsgService.class).sendMatchMessage(iMsgId, details, CONFIG_STR.DEFAULT.toString(), mid, API_TYPE.BOT.toString(), sendEvent);

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
                        log.info("receive : " + receive);
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
            List<Object> content = new ArrayList<>();
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

            List<Object> content = new ArrayList<>();
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

    public void sendMsgToMids(List<String> midList, List<MsgGenerator> msgGenerators, List<MsgDetail> details, boolean async, Long updateMsgId, int retryCount, LOG_TARGET_ACTION_TYPE target, LOG_TARGET_ACTION_TYPE action, String referenceId) throws Exception {
        log.info("sendMsgToMid:" + midList);

        if (midList != null && !midList.isEmpty() && msgGenerators != null && !msgGenerators.isEmpty()) {

            if (async) {
                this.sendToLineAsync(msgGenerators, details, midList, API_TYPE.BOT, updateMsgId);
            } else {

                SendToBotModel sendToBotModel = new SendToBotModel();

                sendToBotModel.setChannelId(CONFIG_STR.DEFAULT.toString());
                sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);

                List<Message> sendStrList = new ArrayList<>();
                for (MsgGenerator msgGenerator : msgGenerators) {
                    sendStrList.add(msgGenerator.getMessageBot(""));
                }

                Map<String, UserLiveChat> inProgressMidMap = userLiveChatService.findByStauts(UserLiveChat.IN_PROGRESS);

                String sendStr = ObjectUtil.objectToJsonStr(sendStrList);
                for (String mid : midList) {
                    try {

                        List<Message> messageList = new ArrayList<>();
                        for (MsgGenerator msgGenerator : msgGenerators) {
                            messageList.add(msgGenerator.getMessageBot(mid));
                        }

                        PushMessage pushMessage = new PushMessage(mid, messageList);
                        sendToBotModel.setPushMessage(pushMessage);

                        String channelName = inProgressMidMap.get(mid) != null ? CONFIG_STR.IN_MANUAL_REPLY_BUT_NOT_SEND_MSG.toString() : CONFIG_STR.AUTO_REPLY.toString();
                        sendToBotModel.setChannelName(channelName);

                        Response<BotApiResponse> responseMulti = LineAccessApiService.sendToLine(sendToBotModel);

                        log.info("statusMulti:" + responseMulti.code());

                        // Check Response Status
                        this.checkStatus(responseMulti, mid, null);

                        this.saveLog(mid, responseMulti, -1L, sendStr, referenceId, target, action);
                    } catch (Exception e) {
                        log.error(ErrorRecord.recordError(e));
                        if (retryCount < 5) {
                            List<String> midListRetry = new ArrayList<>();
                            midListRetry.add(mid);
                            this.sendMsgToMids(midListRetry, msgGenerators, details, async, updateMsgId, retryCount + 1, target, action, referenceId);
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

        if (messageList != null && !messageList.isEmpty()) {
            Map<String, UserLiveChat> inProgressMidMap = userLiveChatService.findByStauts(UserLiveChat.IN_PROGRESS);

            SendToBotModel sendToBotModel = new SendToBotModel();

            sendToBotModel.setChannelId(ChannelId);
            sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);

            String channelName = inProgressMidMap.get(MID) != null ? CONFIG_STR.IN_MANUAL_REPLY_BUT_NOT_SEND_MSG.toString() : CONFIG_STR.AUTO_REPLY.toString();
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
