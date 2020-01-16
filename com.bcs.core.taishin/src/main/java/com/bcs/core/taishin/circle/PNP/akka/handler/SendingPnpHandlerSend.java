package com.bcs.core.taishin.circle.PNP.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.bot.api.model.SendToBotModel;
import com.bcs.core.bot.enums.SEND_TYPE;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.taishin.circle.PNP.akka.model.AsyncPnpSendModel;
import com.bcs.core.taishin.circle.PNP.akka.model.AsyncPnpSendModelError;
import com.bcs.core.taishin.circle.PNP.akka.model.AsyncPnpSendModelSuccess;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDeliveryRecord;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.service.CircleLineAccessApiService;
import com.bcs.core.utils.ErrorRecord;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j(topic = "PnpRecorder")
public class SendingPnpHandlerSend extends UntypedActor {

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof AsyncPnpSendModel) {
            AsyncPnpSendModel msgs = (AsyncPnpSendModel) message;
            log.debug("AsyncPnpSendModel onReceive:" + msgs);

            if (msgs.getPnpDetails() != null) {

                log.debug("Size:" + msgs.getPnpDetails().size());
                List<PnpDetail> successDetail = new ArrayList<>();

                SendToBotModel sendToBotModel = new SendToBotModel();

                sendToBotModel.setChannelId(msgs.getChannelId());
                sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);

                for (PnpDetail detail : msgs.getPnpDetails()) {
                    try {

                        if (StringUtils.isNotBlank(detail.getMsg())) {
                            List<Message> messageList = new ArrayList<Message>();
                            TextMessage text = new TextMessage(detail.getMsg());
                            messageList.add(text);

                            String recordStatus = "";
                            PushMessage pushMessage = new PushMessage(detail.getPhoneHash(), messageList);
                            sendToBotModel.setPushMessage(pushMessage);

                            // Note: 64 <= tag length <= 100
                            List<String> deliveryTags = new ArrayList<>();
                            deliveryTags.add(PnpDeliveryRecord.THIS_TYPE);
                            deliveryTags.add(detail.getPhone());
                            deliveryTags.add(detail.getPnpMainId().toString());
                            deliveryTags.add(detail.getPnpDetailId().toString());

                            Integer statusCode = CircleLineAccessApiService.sendPnpToLine(sendToBotModel, deliveryTags);

                            recordStatus = this.checkStatus(statusCode, recordStatus, detail.getPhone(), detail.getPnpDetailId());

                            if (!new Integer(200).equals(statusCode)) {
                                throw new Exception(statusCode + "-");
                            }

                            successDetail.add(detail);
                        } else {
                            throw new Exception("MSG_NULL");
                        }

                    } catch (Exception e) {
                        log.error(ErrorRecord.recordError(e));
                        log.error("phone:" + detail.getPhone());

                        List<PnpDetail> errorDetail = new ArrayList<>();
                        errorDetail.add(detail);
                        AsyncPnpSendModelError error = new AsyncPnpSendModelError(msgs.getChannelId(), msgs.getApiType(), e.getMessage(), errorDetail, new Date());
                        getSender().tell(error, getSelf());
                    }
                }

                if (successDetail != null && successDetail.size() > 0) {
                    AsyncPnpSendModelSuccess success = new AsyncPnpSendModelSuccess(msgs.getChannelId(), msgs.getApiType(), successDetail, new Date());
                    getSender().tell(success, getSelf());
                }
            }
            log.debug("AsyncPnpSendModel End");
        } else if (message instanceof AsyncPnpSendModelError) {
            AsyncPnpSendModelError msgs = (AsyncPnpSendModelError) message;
            log.debug("AsyncPnpSendModelError onReceive:" + msgs);

            if (msgs.getPnpDetails() != null) {

                log.debug("Size:" + msgs.getPnpDetails().size());
                List<PnpDetail> successDetail = new ArrayList<>();

                SendToBotModel sendToBotModel = new SendToBotModel();

                sendToBotModel.setChannelId(msgs.getChannelId());
                sendToBotModel.setSendType(SEND_TYPE.PUSH_MSG);

                for (PnpDetail detail : msgs.getPnpDetails()) {
                    try {

                        List<Message> messageList = new ArrayList<Message>();
                        TextMessage text = new TextMessage(detail.getMsg());
                        messageList.add(text);

                        String recordStatus = "";
                        PushMessage pushMessage = new PushMessage(detail.getPhoneHash(), messageList);
                        sendToBotModel.setPushMessage(pushMessage);

                        List<String> deliveryTags = new ArrayList<>();
                        deliveryTags.add(PnpDeliveryRecord.THIS_TYPE);
                        deliveryTags.add(detail.getPhone());
//                        deliveryTags.add(detail.getPhoneHash());
                        deliveryTags.add(detail.getPnpMainId().toString());
                        deliveryTags.add(detail.getPnpDetailId().toString());

                        Integer statusCode = CircleLineAccessApiService.sendPnpToLine(sendToBotModel, deliveryTags);

                        recordStatus = this.checkStatus(statusCode, recordStatus, detail.getPhone(), detail.getPnpDetailId());

                        if (!new Integer(200).equals(statusCode)) {
                            throw new Exception(statusCode + "-");
                        }

                        successDetail.add(detail);
                    } catch (Exception e) {
                        log.error(ErrorRecord.recordError(e));

                        getSender().tell(msgs, getSelf());
                    }
                }

                if (successDetail != null && successDetail.size() > 0) {
                    AsyncPnpSendModelSuccess success = new AsyncPnpSendModelSuccess(msgs.getChannelId(), msgs.getApiType(), successDetail, new Date());
                    getSender().tell(success, getSelf());
                }
            }
            log.debug("AsyncPnpSendingModelError End");
        }
    }

    private String checkStatus(Integer statusCode, String recordStatus, String phone, Long msgId) throws Exception {

        log.debug("status:" + statusCode);
        recordStatus += statusCode + "-";

        if (statusCode != 200) {
            List<Object> content = new ArrayList<Object>();
            content.add(phone);
            content.add(msgId);
            content.add(statusCode);

            SystemLogUtil.saveLogError(LOG_TARGET_ACTION_TYPE.TARGET_LineApi, LOG_TARGET_ACTION_TYPE.ACTION_SendPnpToLineApiStatus, content, phone);
        }

        return recordStatus;
    }
}
