package com.bcs.core.bot.receive.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.db.service.MsgBotReceiveService;
import com.bcs.core.bot.receive.utils.MsgBotReceiveParser;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.receive.model.ReceivedModelOriginal;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ???, Alan
 * @see ReceivingMsgHandlerMaster
 */
@Slf4j
public class ReceivingMsgHandlerEventType extends UntypedActor {

    @Override
    public void onReceive(Object message) throws Exception {
        Thread.currentThread().setName("Actor-Receive-Msg-Event-" + Thread.currentThread().getId());

        log.info("Receiving Msg Handler Event Type Actor onReceive!!");

        if (message instanceof ReceivedModelOriginal) {
            ReceivedModelOriginal original = (ReceivedModelOriginal) message;

            List<MsgBotReceive> list = MsgBotReceiveParser.parseMessage(original.getReceivingMsg(), original.getChannelId());

            if (!list.isEmpty()) {
                int count = 0;
                Date start = original.getStart();
                for (MsgBotReceive msg : list) {

                    ReceivingMsgHandlerMaster.taskCount.addAndGet(1L);

                    String eventType = msg.getEventType();
                    log.info("eventType:" + eventType);

                    String channelId = original.getChannelId();
                    String channelName = original.getChannelName();
                    String apiType = original.getApiType().toString();

                    String referenceId = "";


                    boolean channelNameIsEqConfig = channelName.equals(CoreConfigReader.getString(CONFIG_STR.MANUALREPLY_CHANNEL_NAME.toString(), true));
                    boolean eventIsMsgOrPostBack = MsgBotReceive.EVENT_TYPE_MESSAGE.equals(eventType) || MsgBotReceive.EVENT_TYPE_POSTBACK.equals(eventType);
                    boolean eventIsFollowOrUnFollow = MsgBotReceive.EVENT_TYPE_FOLLOW.equals(eventType) || MsgBotReceive.EVENT_TYPE_UNFOLLOW.equals(eventType);
                    boolean eventIsDelivery = MsgBotReceive.EVENT_TYPE_DELIVERY.equals(eventType);

                    if (channelNameIsEqConfig) {
                        methodA(msg, eventType, channelId, channelName, apiType);
                    } else if (eventIsMsgOrPostBack) {
                        referenceId = methodB(msg, eventType, channelId, channelName, apiType);
                    } else if (eventIsFollowOrUnFollow) {
                        referenceId = methodC(msg, eventType, channelId, apiType);
                    } else if (eventIsDelivery) {
                        updatePnpMainAndDetail(msg);
                    } else {
                        // Unknown eventType
                    }

                    SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_ReceivingMsgHandler, LOG_TARGET_ACTION_TYPE.ACTION_HandleMsgReceive, start, 200, msg.toString(), referenceId);
                    count++;
                    start = new Date();
                }
                SystemLogUtil.timeCheck(LOG_TARGET_ACTION_TYPE.TARGET_ReceivingMsgHandler, LOG_TARGET_ACTION_TYPE.ACTION_HandleMsgReceiveAll, original.getStart(), 200, 15000, "count:" + count + "-" + original.getReceivingMsg(), "200-" + count);
            }
        }
        log.debug("ReceivingMsgHandlerEventType End");
    }

    private void updatePnpMainAndDetail(MsgBotReceive msg) {
        //for pnp DELIVERY notification
        log.info("-------Get Line Return PNP Delivery Notification-------");
        ApplicationContextProvider.getApplicationContext().getBean(MsgBotReceiveService.class).bulkPersist(msg);
        //收到delivery時update PNP status為complete
        ApplicationContextProvider.getApplicationContext().getBean(MsgBotReceiveService.class).updatePnpStatus(msg.getDeliveryData());
    }

    /**
     * FIXME Alan 要變更為更適合的Method Name
     * @param msg
     * @param eventType
     * @param channelId
     * @param apiType
     * @return
     */
    private String methodC(MsgBotReceive msg, String eventType, String channelId, String apiType) {
        String referenceId;
        log.info("-------Get Operation-------");
        Long iMsgId = ReceivingMsgHandlerMsgReceiveOp.handleMsgReceiveOp(msg, channelId, apiType);

        referenceId = msg.getSourceId();

        Map<String, Object> map = new HashMap<>();
        map.put("Target", eventType);
        map.put("Content", msg);
        map.put("ChannelId", channelId);
        map.put("ApiType", apiType);
        map.put("iMsgId", iMsgId);
        getSender().tell(map, getSelf());
        return referenceId;
    }

    /**
     * FIXME Alan 要變更為更適合的Method Name
     * @param msg
     * @param eventType
     * @param channelId
     * @param channelName
     * @param apiType
     * @return
     */
    private String methodB(MsgBotReceive msg, String eventType, String channelId, String channelName, String apiType) {
        String referenceId;
        log.info("-------Get Message-------");
        Long iMsgId = ReceivingMsgHandlerMsgReceive.handleMsgReceive(msg, channelId, channelName, apiType);

        referenceId = msg.getMsgId();

        Map<String, Object> map = new HashMap<>();
        map.put("Target", eventType);
        map.put("Content", msg);
        map.put("ChannelId", channelId);
        map.put("ApiType", apiType);
        map.put("iMsgId", iMsgId);
        getSender().tell(map, getSelf());
        return referenceId;
    }

    /**
     * FIXME Alan 要變更為更適合的Method Name
     * @param msg
     * @param eventType
     * @param channelId
     * @param channelName
     * @param apiType
     * @throws Exception
     */
    private void methodA(MsgBotReceive msg, String eventType, String channelId, String channelName, String apiType) throws Exception {
        if (MsgBotReceive.EVENT_TYPE_MESSAGE.equals(eventType)) {
            /* 將訊息傳送至真人客服 */
            ReceivingMsgHandlerMsgReceive.trasmitToCustomerService(msg, channelId, channelName, apiType);
        } else {
            /* Other message type */
        }
    }
}
