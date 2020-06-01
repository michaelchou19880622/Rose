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
 * @author ???
 * @see com.bcs.core.bot.akka.service.AkkaBotService
 */
@Slf4j
public class ReceivingMsgHandlerMaster extends UntypedActor {
    public final static AtomicLong taskCount = new AtomicLong(0L);
    public static Date updateDate;
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
    public void onReceive(Object message) throws Exception {
        try {
            Thread.currentThread().setName("Actor-Receive-Msg-Master-" + Thread.currentThread().getId());
            if (message instanceof ReceivedModelOriginal) {
            	log.info("Received an Original message and telling the EventType router");
                routerEventTypeActor.tell(message, getSelf());
            } else if (message instanceof Map) {
            	@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) message;
                String target = (String) map.get("Target");

                if (MsgBotReceive.EVENT_TYPE_MESSAGE.equals(target) || MsgBotReceive.EVENT_TYPE_POSTBACK.equals(target)) {
                	log.info("Received an MESSAGE or POSTBACK message and telling the MsgReceive router");
                	routerMsgReceiveActor.tell(message, getSelf());
                } else if (MsgBotReceive.EVENT_TYPE_FOLLOW.equals(target) || MsgBotReceive.EVENT_TYPE_UNFOLLOW.equals(target)) {
                	log.info("Received an FOLLOW or UNFOLLOW message and telling the MsgReceiveOp router");
                    routerMsgReceiveOpActor.tell(message, getSelf());
                }
            }
        } catch (Exception e) {
        	log.info("An exception detected dering processing a message");
            log.error("Exception", e);
        }
    }
}
