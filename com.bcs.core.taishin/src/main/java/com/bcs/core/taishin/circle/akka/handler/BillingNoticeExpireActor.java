package com.bcs.core.taishin.circle.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;
import lombok.extern.slf4j.Slf4j;

/**
 * 發送效期已過不推送訊息
 *
 * @author jessie
 */
@Slf4j(topic = "BNRecorder")
public class BillingNoticeExpireActor extends UntypedActor {

    @Override
    public void onReceive(Object object) {
        Thread.currentThread().setName("Actor-BN-Expired-" + Thread.currentThread().getId());
        log.info("Expired Actor Receive!!");
        if (object instanceof BillingNoticeMain) {
            methodA((BillingNoticeMain) object);
        }
    }

    private void methodA(BillingNoticeMain object) {
        BillingNoticeService billingNoticeService = ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeService.class);
        billingNoticeService.updateStatusFailAndEmail(object);
    }
}