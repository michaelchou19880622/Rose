package com.bcs.core.taishin.circle.PNP.akka.handler;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.utils.AkkaRouterFactory;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Master Actor
 *
 * @author ???
 * @see com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService
 */
public class PnpMainActor extends UntypedActor {
    private static Logger logger = Logger.getLogger(PnpMainActor.class);
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
        if (object instanceof PnpMain) {
            logger.info("PnpMainActor onReceive object instanceof PnpMain!!!");

            PnpMain pnpMain = (PnpMain) object;
            String stage = pnpMain.getProcStage();
            if (AbstractPnpMainEntity.STAGE_BC.equals(stage)) {
                tellActor(pushMessageRouterActor, pnpMain);
            } else if (AbstractPnpMainEntity.STAGE_PNP.equals(stage)) {
                tellActor(pnpMessageRouterActor, pnpMain);
            }
        } else if (object instanceof PnpDetail) {
            updateStatusRouterActor.tell(object, this.getSelf());
        }
    }

    private void tellActor(ActorRef someActor, PnpMain tellSomething) throws CloneNotSupportedException {
        logger.info("PnpMainActor onReceive doing Line PHONE NUMBER PUSH .");
        Integer buffer = 19;
        List<? super PnpDetail> details = tellSomething.getPnpDetails();
        List<? super PnpDetail> partition;
        logger.info("PnpMainActor onReceive details.size :" + details.size());
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
