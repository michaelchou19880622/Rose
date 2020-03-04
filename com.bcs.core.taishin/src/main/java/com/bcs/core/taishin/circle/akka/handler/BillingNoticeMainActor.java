package com.bcs.core.taishin.circle.akka.handler;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;
import com.bcs.core.utils.AkkaRouterFactory;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Calendar;
import java.util.List;

/**
 * @author ???
 * @see com.bcs.core.taishin.circle.service.BillingNoticeAkkaService
 */
@Slf4j(topic = "BNRecorder")
public class BillingNoticeMainActor extends UntypedActor {
    private final ActorRef pushMessageRouterActor;
    private final ActorRef expireRouterActor;
    private final ActorRef curfewActor;

    public BillingNoticeMainActor() {
        pushMessageRouterActor = new AkkaRouterFactory<>(getContext(), BillingNoticePushMessageActor.class, true).routerActor;
        expireRouterActor = new AkkaRouterFactory<>(getContext(), BillingNoticeExpireActor.class, true).routerActor;
        curfewActor = new AkkaRouterFactory<>(getContext(), BillingNoticeCurfewActor.class, true).routerActor;
    }

    @Override
    public void onReceive(final Object object) {
        Thread.currentThread().setName("Actor-BN-Main-" + Thread.currentThread().getId());
        log.info("Main Actor Receive!!");
        try {
            if (object instanceof BillingNoticeMain) {
                pushRoute((BillingNoticeMain) object);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void pushRoute(final BillingNoticeMain object) {
        BillingNoticeService billingNoticeService = ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeService.class);

        if (billingNoticeService.isCurfew(object.getTemplate(), Calendar.getInstance())) {
            toCurfewActor(object);
        } else if (DataUtils.isPast(object.getExpiryTime())) {
            toExpiredActor(object);
        } else {
            pushProcess(object);
        }
    }

    private void pushProcess(BillingNoticeMain object) {
        List<BillingNoticeDetail> details = object.getDetails();
        final int detailSize = details.size();
        final int maxActorCount = getMaxActorCount();
        final int buffer = getBuffer(detailSize, maxActorCount);
        log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(details));
        List<List<BillingNoticeDetail>> partitionList = ListUtils.partition(details, buffer);
        partitionList.forEach(list -> {
            log.info("To Akka Partition Size: {}", list.size());
            BillingNoticeMain billingNoticeMainClone = ObjectUtils.clone(object);
            billingNoticeMainClone.setDetails(list);
            toPushActor(billingNoticeMainClone);
        });
    }

    private int getMaxActorCount() {
        int count = CoreConfigReader.getInteger("bn.push.detail.max.actor.count");
        if (count <= 0){
            count = 100;
        }
        return count;
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

    private void toPushActor(BillingNoticeMain billingNoticeMainClone) {
        log.info("To PushActor!!");
        pushMessageRouterActor.tell(billingNoticeMainClone, this.getSelf());
    }

    private void toExpiredActor(BillingNoticeMain object) {
        log.info("To ExpireActor!!");
        expireRouterActor.tell(object, this.getSelf());
    }

    private void toCurfewActor(BillingNoticeMain object) {
        log.info("To CurfewActor!!");
        curfewActor.tell(object, this.getSelf());
    }
}
