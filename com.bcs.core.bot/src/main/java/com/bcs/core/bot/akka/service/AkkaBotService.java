package com.bcs.core.bot.akka.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.bcs.core.bot.receive.akka.handler.ReceivingMsgHandlerMaster;
import com.bcs.core.bot.send.akka.handler.SendingMsgHandlerMaster;
import com.bcs.core.bot.send.akka.model.AsyncSendingClusterModel;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.receive.model.ReceivedModelOriginal;
import com.bcs.core.send.akka.model.AsyncEsnSendingModel;
import com.bcs.core.send.akka.model.AsyncSendingModel;
import com.bcs.core.utils.AkkaSystemFactory;
import com.bcs.core.utils.ErrorRecord;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @see com.bcs.web.receive.controller.LineBCApiClusterController
 */
@Service
public class AkkaBotService {

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(AkkaBotService.class);

    private List<ActorSystem> systemSending = new ArrayList<>();
    private List<ActorSystem> systemReceiving = new ArrayList<>();
    private List<ActorRef> sendingMaster = new ArrayList<>();
    private List<ActorRef> receivingMaster = new ArrayList<>();

    private AkkaBotService() {

        new AkkaSystemFactory<SendingMsgHandlerMaster>(systemSending, sendingMaster, SendingMsgHandlerMaster.class, "systemSending", "SendingMsgHandlerMaster");
        new AkkaSystemFactory<ReceivingMsgHandlerMaster>(systemReceiving, receivingMaster, ReceivingMsgHandlerMaster.class, this.getClass().getSimpleName(), ReceivingMsgHandlerMaster.class.getSimpleName());
    }

    /**
	 * 發送訊息
     * @param msgs
     * @see com.bcs.core.bot.send.service.SendingMsgService#sendToLineAsync(List, List, List, API_TYPE, Long)
     * @see com.bcs.web.receive.controller.LineBCApiClusterController#lineBCApiClusterSend(AsyncSendingClusterModel, String, HttpServletRequest, HttpServletResponse)
     */
    public void sendingMsgs(AsyncSendingModel msgs) {
        try {
            ActorRef master = randomMaster(sendingMaster);
            master.tell(msgs, master);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    private ActorRef randomMaster(List<ActorRef> masters) {
        logger.debug("randomMaster Size:" + masters.size());

        int index = new Random().nextInt(masters.size());
        return masters.get(index);
    }

    /**
	 * 接收訊息
     * @param msgs
     * @see com.bcs.web.receive.controller.LineBotApiController#lineBotApiReceiving
     */
    public void receivingMsgs(ReceivedModelOriginal msgs) {
        try {
            ActorRef master = randomMaster(receivingMaster);
            master.tell(msgs, master);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    @PreDestroy
    public void shutdownNow() {
        logger.info("[DESTROY] AkkaBotService shutdownNow cleaning up...");

        try {
            int count = 0;
            for (ActorSystem system : systemSending) {
                system.stop(sendingMaster.get(count));
                count++;

                system.shutdown();
                system = null;
            }
        } catch (Exception e) {
        }

        try {
            int count = 0;
            for (ActorSystem system : systemReceiving) {
                system.stop(receivingMaster.get(count));
                count++;

                system.shutdown();
                system = null;
            }
        } catch (Exception e) {
        }

        System.gc();
        logger.info("[DESTROY] AkkaBotService shutdownNow destroyed");
    }

    public void sendingMsgs(AsyncEsnSendingModel msgs) {
        try {
            ActorRef master = randomMaster(sendingMaster);
            master.tell(msgs, master);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }
}
