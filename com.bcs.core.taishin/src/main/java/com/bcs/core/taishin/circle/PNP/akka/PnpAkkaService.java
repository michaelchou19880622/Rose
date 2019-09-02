package com.bcs.core.taishin.circle.PNP.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.bcs.core.taishin.circle.PNP.akka.handler.PnpMainActor;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpPNPMsgService;
import com.bcs.core.utils.AkkaSystemFactory;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @see com.bcs.core.taishin.circle.PNP.service.PnpService#pushLineMessage
 * @author ???
 */
@Service
public class PnpAkkaService {
    private static Logger logger = Logger.getLogger(PnpAkkaService.class);
    private List<ActorSystem> actorSystemList = new ArrayList<>();
    private List<ActorRef> pnpActorList = new ArrayList<>();

    public PnpAkkaService() {
        new AkkaSystemFactory<PnpMainActor>(actorSystemList, pnpActorList, PnpMainActor.class, "actorSystemList", "PnpActorList");
    }

    /**
     * @param object pnpMain
     * @see PnpPNPMsgService#sendingPnpMain
     * @see com.bcs.core.taishin.circle.PNP.service.PnpService#pushPnpMessage
     */
    public void tell(Object object) {
        ActorRef actor = getRandomActor(pnpActorList);
        actor.tell(object, actor);
    }

    /**
     * FIXME Alan 隨機選取Actor?
     * @param actors actors
     * @return actorRef
     */
    private ActorRef getRandomActor(List<ActorRef> actors) {
        int index = new Random().nextInt(actors.size());
        return actors.get(index);
    }


    /**
     * FIXME Alan 是否需要搬移至工具類別？
     * 取得Host Name
     * @return Host Name
     */
    public String getProcessApName() {
        try {
            if (InetAddress.getLocalHost() != null) {
                return InetAddress.getLocalHost().getHostName();
            }
            logger.error(" getHostName error: LocalHost is Null!!");
        } catch (Exception e) {
            logger.error(" getHostName error:" + e.getMessage());
        }
        return null;
    }

    /**
     * Shutdown Akka All Actor
     */
    @PreDestroy
    public void shutdownNow() {
        logger.info("[DESTROY] Pnp AkkaService shutdownNow cleaning up...");
        try {
            int count = 0;

            for (ActorSystem actorSystem : actorSystemList) {
                actorSystem.stop(pnpActorList.get(count));
                actorSystem.shutdown();
                logger.info("Shutdown Actor[" + count + "]");
                count++;
            }
        } catch (Exception e) {
            logger.error(e);
        }
        logger.info("[DESTROY] Pnp AkkaService Shutdown Destroyed");
    }
}
