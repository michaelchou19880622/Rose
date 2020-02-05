package com.bcs.core.bot.receive.akka.handler;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.receive.model.ReceivedModelOriginal;
import com.bcs.core.utils.AkkaRouterFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @see com.bcs.core.bot.akka.service.AkkaBotService
 * @see com.bcs.core.taishin.circle.pnp.akka.CircleAkkaBotService
 */
@Slf4j
public class ReceivingMsgHandlerMaster extends UntypedActor {

    public final static AtomicLong taskCount = new AtomicLong(0L);
    public static Date updateDate;

    /**
     * Event Type(PNP Delivery)
     */
    private final ActorRef routerEventTypeActor;
    private final ActorRef routerMsgReceiveActor;

    /**
     * Line User Status Update
     */
    private final ActorRef routerMsgReceiveOpActor;

    public ReceivingMsgHandlerMaster() {
        routerEventTypeActor = new AkkaRouterFactory<>(getContext(), ReceivingMsgHandlerEventType.class, true).routerActor;
        routerMsgReceiveActor = new AkkaRouterFactory<>(getContext(), ReceivingMsgHandlerMsgReceive.class, true).routerActor;
        routerMsgReceiveOpActor = new AkkaRouterFactory<>(getContext(), ReceivingMsgHandlerMsgReceiveOp.class, true).routerActor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onReceive(Object message) throws Exception {
        try {
            Thread.currentThread().setName("Actor-Receive-Msg-Master-" + Thread.currentThread().getId());

            if (message instanceof ReceivedModelOriginal) {
                log.info("-------onReceive Step1-------");

                routerEventTypeActor.tell(message, getSelf());
            } else if (message instanceof Map) {
                log.info("-------onReceive Step2-------");

                Map<String, Object> map = (Map<String, Object>) message;
                String target = (String) map.get("Target");

                if (MsgBotReceive.EVENT_TYPE_MESSAGE.equals(target) || MsgBotReceive.EVENT_TYPE_POSTBACK.equals(target)) {
                    routerMsgReceiveActor.tell(message, getSelf());
                } else if (MsgBotReceive.EVENT_TYPE_FOLLOW.equals(target) || MsgBotReceive.EVENT_TYPE_UNFOLLOW.equals(target)) {
                    routerMsgReceiveOpActor.tell(message, getSelf());
                }
            }
        } catch (Exception e) {
            log.error("{}: {}", "Exception", e);
        }
    }
}
