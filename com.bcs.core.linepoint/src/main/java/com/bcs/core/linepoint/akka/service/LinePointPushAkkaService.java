package com.bcs.core.linepoint.akka.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.bcs.core.linepoint.akka.handler.LinePointPushMasterActor;
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
public class LinePointPushAkkaService {
    private List<ActorSystem> actorSystemList = new ArrayList<>();
    private List<ActorRef> linePointMasterActorList = new ArrayList<>();

    public LinePointPushAkkaService() {
        new AkkaSystemFactory<>(actorSystemList, linePointMasterActorList, LinePointPushMasterActor.class, "actorSystemList", "LinePointMasterActorList");
    }

    public void tell(Object object) {
        ActorRef actor = getRandomActor(linePointMasterActorList);
        actor.tell(object, actor);
    }

    private ActorRef getRandomActor(List<ActorRef> actors) {
        int index = new Random().nextInt(actors.size());
        return actors.get(index);
    }

    @PreDestroy
    public void shutdownNow() {
        log.info("[DESTROY] Line Point Push AkkaService shutdownNow cleaning up...");
        try {
            int count = 0;
            for (ActorSystem system : actorSystemList) {
                system.stop(linePointMasterActorList.get(count));
                count++;

                system.shutdown();
            }
        } catch (Exception e) {
        }
        log.info("[DESTROY] Line Point Push AkkaService shutdownNow destroyed");
    }
}