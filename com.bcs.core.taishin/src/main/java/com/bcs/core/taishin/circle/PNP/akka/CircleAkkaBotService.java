package com.bcs.core.taishin.circle.PNP.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.bcs.core.bot.receive.akka.handler.ReceivingMsgHandlerMaster;
import com.bcs.core.receive.model.ReceivedModelOriginal;
import com.bcs.core.send.akka.model.AsyncEsnSendingModel;
import com.bcs.core.send.akka.model.AsyncSendingModel;
import com.bcs.core.taishin.circle.PNP.akka.handler.SendingMsgHandlerMaster;
import com.bcs.core.taishin.circle.PNP.akka.handler.SendingPnpHandlerMaster;
import com.bcs.core.taishin.circle.PNP.akka.model.AsyncPnpSendModel;
import com.bcs.core.utils.AkkaSystemFactory;
import com.bcs.core.utils.ErrorRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j(topic = "PnpRecorder")
@Service
public class CircleAkkaBotService {


    private List<ActorSystem> systemSending = new ArrayList<>();
    private List<ActorSystem> systemReceiving = new ArrayList<>();
    private List<ActorSystem> systemPnpSending = new ArrayList<>();
    private List<ActorRef> sendingMaster = new ArrayList<>();
    private List<ActorRef> receivingMaster = new ArrayList<>();
    private List<ActorRef> sendingPnpMaster = new ArrayList<>();

    private CircleAkkaBotService() {

        new AkkaSystemFactory<>(systemSending, sendingMaster, SendingMsgHandlerMaster.class, "systemSending", "SendingMsgHandlerMaster");
        new AkkaSystemFactory<>(systemReceiving, receivingMaster, ReceivingMsgHandlerMaster.class, this.getClass().getSimpleName(), ReceivingMsgHandlerMaster.class.getSimpleName());
        new AkkaSystemFactory<>(systemPnpSending, sendingPnpMaster, SendingPnpHandlerMaster.class, "systemPnpSending", "SendingPnpMsgHandlerMaster");
    }

    public void sendingMsgs(AsyncSendingModel msgs) {
        try {
            ActorRef master = randomMaster(sendingMaster);
            master.tell(msgs, master);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    private ActorRef randomMaster(List<ActorRef> masters) {
        log.debug("randomMaster Size:" + masters.size());

        int index = new Random().nextInt(masters.size());
        return masters.get(index);
    }

    public void receivingMsgs(ReceivedModelOriginal msgs) {
        try {
            ActorRef master = randomMaster(receivingMaster);
            master.tell(msgs, master);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    @PreDestroy
    public void shutdownNow() {
        log.info("[DESTROY] AkkaBotService shutdownNow cleaning up...");

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

        try {
            int count = 0;
            for (ActorSystem system : systemPnpSending) {
                system.stop(sendingPnpMaster.get(count));
                count++;

                system.shutdown();
                system = null;
            }
        } catch (Exception e) {
        }

        System.gc();
        log.info("[DESTROY] AkkaBotService shutdownNow destroyed");
    }

    public void sendingMsgs(AsyncEsnSendingModel msgs) {
        try {
            ActorRef master = randomMaster(sendingMaster);
            master.tell(msgs, master);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    public void sendingPnp(AsyncPnpSendModel msgs) {
        try {
            ActorRef master = randomMaster(sendingPnpMaster);
            master.tell(msgs, master);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }
}
