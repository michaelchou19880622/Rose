package com.bcs.core.taishin.circle.PNP.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.bcs.core.taishin.circle.PNP.akka.handler.PnpMainActor;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpPNPMsgService;
import com.bcs.core.utils.AkkaSystemFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author ???
 * @see com.bcs.core.taishin.circle.PNP.service.PnpService#pushLineMessage
 */
@Slf4j
@Service
public class PnpAkkaService {
    private List<ActorSystem> actorSystemList = new ArrayList<>();
    private List<ActorRef> pnpActorList = new ArrayList<>();

    public PnpAkkaService() {
        new AkkaSystemFactory<>(actorSystemList, pnpActorList, PnpMainActor.class, "actorSystemList", "PnpActorList");
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
     * 隨機選取Actor
     *
     * @param actors actors
     * @return actorRef
     */
    private ActorRef getRandomActor(List<ActorRef> actors) {
        int index = new Random().nextInt(actors.size());
        return actors.get(index);
    }


    /**
     * 取得Host Name
     *
     * @return Host Name
     */
    public String getProcessApName() {
        try {
            if (InetAddress.getLocalHost() != null) {
                return InetAddress.getLocalHost().getHostName();
            }
            log.error("LocalHost is Null!!");
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    /**
     * Shutdown Akka All Actor
     */
    @PreDestroy
    public void shutdownNow() {
        log.info("[DESTROY] Pnp AkkaService shutdownNow cleaning up...");
        try {
            int count = 0;

            for (ActorSystem actorSystem : actorSystemList) {
                actorSystem.stop(pnpActorList.get(count));
                actorSystem.shutdown();
                log.info("Shutdown Actor[" + count + "]");
                count++;
            }
        } catch (Exception e) {
            log.error("", e);
        }
        log.info("[DESTROY] Pnp AkkaService Shutdown Destroyed");
    }
}
