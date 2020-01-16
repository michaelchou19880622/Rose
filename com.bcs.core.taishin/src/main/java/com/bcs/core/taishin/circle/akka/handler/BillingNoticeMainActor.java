package com.bcs.core.taishin.circle.akka.handler;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;
import com.bcs.core.utils.AkkaRouterFactory;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author ???
 * @see com.bcs.core.taishin.circle.service.BillingNoticeAkkaService
 */
@Slf4j(topic = "BNRecorder")
public class BillingNoticeMainActor extends UntypedActor {
    private final ActorRef pushMessageRouterActor;
    private final ActorRef updateStatusRouterActor;
    private final ActorRef expireRouterActor;
    private final ActorRef curfewActor;

    public BillingNoticeMainActor() {
        pushMessageRouterActor = new AkkaRouterFactory<>(getContext(), BillingNoticePushMessageActor.class, true).routerActor;
        updateStatusRouterActor = new AkkaRouterFactory<>(getContext(), BillingNoticeUpdateStatusActor.class, true).routerActor;
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
            if (object instanceof BillingNoticeDetail) {
                toUpdateActor(object);
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
        int buffer = 100;
        List<BillingNoticeDetail> details = object.getDetails();
        List<BillingNoticeDetail> partition;
        int detailSize = details.size();
        int count = 0;

        log.info("Detail: {}", DataUtils.toPrettyJsonUseJackson(details));

        while (count < detailSize) {
            int counter = 0;
            partition = new ArrayList<>();
            for (; (counter < buffer) && (count < detailSize); counter++, count++) {
                partition.add(details.get(count));
            }
            log.info("To Akka Count: {}, Partition Size: {}", count, partition.size());

            BillingNoticeMain billingNoticeMainClone = ObjectUtils.clone(object);
            billingNoticeMainClone.setDetails(partition);
            toPushActor(billingNoticeMainClone);
        }
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

    private void toUpdateActor(Object object) {
        log.info("To UpdateActor!!");
        updateStatusRouterActor.tell(object, this.getSelf());
    }
}
