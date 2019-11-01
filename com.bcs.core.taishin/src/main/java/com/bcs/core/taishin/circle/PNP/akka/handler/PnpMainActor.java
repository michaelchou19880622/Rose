package com.bcs.core.taishin.circle.PNP.akka.handler;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.utils.AkkaRouterFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Master Actor
 *
 * @author ???
 * @see com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService
 */
@Slf4j
public class PnpMainActor extends UntypedActor {
    /**
     * BC Actor
     */
    private final ActorRef pushMessageRouterActor;
    /**
     * PNP Actor
     */
    private final ActorRef pnpMessageRouterActor;
    /**
     * 更新訊息狀態Actor
     */
    private final ActorRef updateStatusRouterActor;

    private PnpMainActor() {
        pushMessageRouterActor = new AkkaRouterFactory<PnpPushMessageActor>(getContext(), PnpPushMessageActor.class, true).routerActor;
        pnpMessageRouterActor = new AkkaRouterFactory<PnpMessageActor>(getContext(), PnpMessageActor.class, true).routerActor;
        updateStatusRouterActor = new AkkaRouterFactory<PnpUpdateStatusActor>(getContext(), PnpUpdateStatusActor.class, true).routerActor;
    }

    @Override
    public void onReceive(Object object) throws Exception {
        try {
            Thread.currentThread().setName("Actor-PNP-Main-" + Thread.currentThread().getId());


            if (object instanceof PnpMain) {
                PnpMain pnpMain = (PnpMain) object;
                String stage = pnpMain.getProcStage();
                log.info("PnpMainActor onReceive object instanceof PnpMain!!! Stage: " + stage);
                switch (stage) {
                    case AbstractPnpMainEntity.STAGE_BC:
                        tellActor(pushMessageRouterActor, pnpMain);
                        break;
                    case AbstractPnpMainEntity.STAGE_PNP:
                        tellActor(pnpMessageRouterActor, pnpMain);
                        break;
                    case AbstractPnpMainEntity.STAGE_SMS:
                        //TODO SMS Process
                        break;
                    default:
                        break;
                }
            } else if (object instanceof PnpDetail) {
                log.info("Tell Update Actor do Update!!");
                updateStatusRouterActor.tell(object, this.getSelf());
            }
        } catch (Exception e) {
            log.error("{}", e);
        }
    }

    private void tellActor(ActorRef someActor, PnpMain tellSomething) throws CloneNotSupportedException {
        Integer buffer = 19;
        List<? super PnpDetail> details = tellSomething.getPnpDetails();
        List<? super PnpDetail> partition;
        log.info("PnpMainActor onReceive details.size :" + details.size());
        Integer arrayLength = details.size();
        Integer pointer = 0;
        while (pointer < arrayLength) {
            Integer counter = 0;
            partition = new ArrayList<>();
            for (; (counter < buffer) && (pointer < arrayLength); counter++, pointer++) {
                partition.add((PnpDetail) details.get(pointer));
            }
            PnpMain pnpMainClone = (PnpMain) tellSomething.clone();
            pnpMainClone.setPnpDetails(partition);
            someActor.tell(pnpMainClone, this.getSelf());
        }
    }
}
