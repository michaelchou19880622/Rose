package com.bcs.core.taishin.circle.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;
import lombok.extern.slf4j.Slf4j;

/**
 * 宵禁中 狀態改為retry
 *
 * @author jessie
 */
@Slf4j(topic = "BNRecorder")
public class BillingNoticeCurfewActor extends UntypedActor {
    @Override
    public void onReceive(Object object) {
        try {
            Thread.currentThread().setName("Actor-BN-Curfew-" + Thread.currentThread().getId());
            log.info("Curfew Actor Receive!!");

            if (object instanceof BillingNoticeMain) {
                methodA((BillingNoticeMain) object);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void methodA(BillingNoticeMain object) {
        BillingNoticeService billingNoticeService = ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeService.class);
        for (BillingNoticeDetail detail : object.getDetails()) {
            detail.setStatus(BillingNoticeMain.NOTICE_STATUS_RETRY);
            log.info("Curfew NOTICE_STATUS_RETRY NoticeDetailId:" + detail.getNoticeDetailId());
            billingNoticeService.save(detail);
        }
    }
}