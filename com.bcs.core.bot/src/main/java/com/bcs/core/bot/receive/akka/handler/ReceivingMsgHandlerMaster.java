package com.bcs.core.bot.receive.akka.handler;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.receive.model.ReceivedModelOriginal;
import com.bcs.core.utils.AkkaRouterFactory;

public class ReceivingMsgHandlerMaster extends UntypedActor {
	
	public final static AtomicLong taskCount= new AtomicLong(0L);
	public static Date updateDate;

	/** Logger */
	private static Logger logger = Logger.getLogger(ReceivingMsgHandlerMaster.class);

    private final ActorRef routerEventTypeActor;
    private final ActorRef routerMsgReceiveActor;
    private final ActorRef routerMsgReceiveOpActor;
    
	public ReceivingMsgHandlerMaster(){
	    routerEventTypeActor = new AkkaRouterFactory<ReceivingMsgHandlerEventType>(getContext(), ReceivingMsgHandlerEventType.class, true).routerActor;

	    routerMsgReceiveActor = new AkkaRouterFactory<ReceivingMsgHandlerMsgReceive>(getContext(), ReceivingMsgHandlerMsgReceive.class, true).routerActor;

	    routerMsgReceiveOpActor = new AkkaRouterFactory<ReceivingMsgHandlerMsgReceiveOp>(getContext(), ReceivingMsgHandlerMsgReceiveOp.class, true).routerActor;
	}
	
	@Override
	public void onReceive(Object message) throws Exception {

		if (message instanceof ReceivedModelOriginal) {
			logger.debug("-------onReceive Step1-------");
			
			routerEventTypeActor.tell(message, getSelf());
		}
		else if(message instanceof Map){
			logger.debug("-------onReceive Step2-------");
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) message;
			String target = (String) map.get("Target");

			if(MsgBotReceive.EVENT_TYPE_MESSAGE.equals(target)){
				routerMsgReceiveActor.tell(message, getSelf());
			}
			else if(MsgBotReceive.EVENT_TYPE_POSTBACK.equals(target)){
				routerMsgReceiveActor.tell(message, getSelf());
			}
			else if(MsgBotReceive.EVENT_TYPE_FOLLOW.equals(target)){
				routerMsgReceiveOpActor.tell(message, getSelf());
			}
			else if(MsgBotReceive.EVENT_TYPE_UNFOLLOW.equals(target)){
				routerMsgReceiveOpActor.tell(message, getSelf());
			}
		}
	}
}
