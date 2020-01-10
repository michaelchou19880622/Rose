package com.bcs.core.taishin.circle.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.bcs.core.taishin.circle.akka.handler.BillingNoticeMainActor;
import com.bcs.core.utils.AkkaSystemFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author ???
 */
@Slf4j
@Service
public class BillingNoticeAkkaService {
    private List<ActorSystem> actorSystemList = new ArrayList<>();
    private List<ActorRef> billingNoticeActorList = new ArrayList<>();

    public BillingNoticeAkkaService() {
        new AkkaSystemFactory<>(actorSystemList, billingNoticeActorList, BillingNoticeMainActor.class, "actorSystemList", "BillingNoticeActorList");
    }

    public void tell(Object object) {
        ActorRef actor = getRandomActor(billingNoticeActorList);
        actor.tell(object, actor);
    }

    private ActorRef getRandomActor(List<ActorRef> actors) {
        int index = new Random().nextInt(actors.size());
        return actors.get(index);
    }

    @PreDestroy
    public void shutdownNow() {
        log.info("[DESTROY] BillingNotice AkkaService shutdownNow cleaning up...");
        try {
            int count = 0;
            for (ActorSystem system : actorSystemList) {
                system.stop(billingNoticeActorList.get(count));
                count++;

                system.shutdown();
            }
        } catch (Throwable e) {
        }
        log.info("[DESTROY] BillingNotice AkkaService shutdownNow destroyed");
    }
}
