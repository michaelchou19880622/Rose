package com.bcs.core.taishin.circle.pnp.akka.handler;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.pnp.code.PnpStageEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMain;
import com.bcs.core.utils.AkkaRouterFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Master Actor
 *
 * @author ???
 * @see com.bcs.core.taishin.circle.pnp.akka.PnpAkkaService
 */
@Slf4j(topic = "PnpRecorder")
public class PnpMainActor extends UntypedActor {
    /**
     * BC Actor
     */
    private final ActorRef pushMessageRouterActor;
    /**
     * PNP Actor
     */
    private final ActorRef pnpMessageRouterActor;

    private PnpMainActor() {
        pushMessageRouterActor = new AkkaRouterFactory<>(getContext(), PnpPushMessageActor.class, true).routerActor;
        pnpMessageRouterActor = new AkkaRouterFactory<>(getContext(), PnpMessageActor.class, true).routerActor;
    }

    @Override
    public void onReceive(Object object) {
        try {
            Thread.currentThread().setName("Actor-PNP-Main-" + Thread.currentThread().getId());

            if (object instanceof PnpMain) {
                final PnpMain main = (PnpMain) object;
                /* TO BC */
                pushRoute(main, PnpStageEnum.BC, pushMessageRouterActor);
                /* TO PNP */
                pushRoute(main, PnpStageEnum.PNP, pnpMessageRouterActor);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void pushRoute(PnpMain main, PnpStageEnum stage, ActorRef someActor) {
        if (main != null && main.getPnpDetails() != null && CollectionUtils.isNotEmpty(main.getPnpDetails())) {
            List<PnpDetail> detailList = main.getPnpDetails().stream()
                    .filter(detail -> {
                        if (detail.getProcStage() != null) {
                            return Objects.equals(stage.value, detail.getProcStage());
                        }
                        return false;
                    })
                    .sorted(Comparator.comparing(PnpDetail::getPnpDetailId))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(detailList)) {
                PnpMain m = ObjectUtils.clone(main);
                m.setPnpDetails(detailList);
                tellActor(someActor, m);
            }
        }
    }

    private void tellActor(ActorRef someActor, PnpMain tellSomething) {
        final List<PnpDetail> detailList = tellSomething.getPnpDetails();
        final int buffer = getBuffer(detailList.size(), getMaxActorCount());
        log.info("PnpMainActor onReceive details.size : {}", detailList.size());

        List<List<PnpDetail>> partitionList = ListUtils.partition(detailList, buffer);
        partitionList.forEach(list -> {
            PnpMain pnpMainClone = ObjectUtils.clone(tellSomething);
            pnpMainClone.setPnpDetails(list.stream().sorted(Comparator.comparing(PnpDetail::getPnpDetailId)).collect(Collectors.toList()));
            log.info("To Akka main {} detail list size is {}", pnpMainClone.getPnpMainId(), buffer);
            someActor.tell(pnpMainClone, this.getSelf());
        });
    }

    private int getBuffer(final int detailSize, final int maxActorCount) {
        if (detailSize <= maxActorCount) {
            return 1;
        }
        if (detailSize % maxActorCount == 0) {
            return detailSize / maxActorCount;
        }
        return detailSize / maxActorCount + 1;
    }

    private int getMaxActorCount() {
        int count = CoreConfigReader.getInteger("bn.push.detail.max.actor.count", true);
        log.info("getMaxActorCount from database : {}", count);
        if (count <= 0) {
            count = 50;
            log.warn("Properties [bn.push.detail.max.actor.count] does not found, use default value is {}!!", count);
        }
        return count;
    }
}
