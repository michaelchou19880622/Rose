package com.bcs.core.taishin.circle.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 更新發送結果
 *
 * @author jessie
 */
@Slf4j(topic = "BNRecorder")
public class BillingNoticeUpdateStatusActor extends UntypedActor {
    @Override
    public void onReceive(Object object) {
        Thread.currentThread().setName("Actor-BN-Update-" + Thread.currentThread().getId());
        log.info("Update Actor Receive!!");
        try {
            if (object instanceof BillingNoticeDetail) {
                updateProcess((BillingNoticeDetail) object);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
        log.info("Update Actor End!!");
    }

    private void updateProcess(BillingNoticeDetail object) {
        updateDetail(object);
//        updateMain(object);
    }

    private void updateDetail(BillingNoticeDetail object) {
        log.info("Before Update Detail: {}", DataUtils.toPrettyJsonUseJackson(object));
        getService().save(object);
    }

    /**
     * 若明細已無重試或等待發送的狀態資料, 則更新主檔狀態為完成
     */
    private void updateMain(BillingNoticeDetail object) {
        if (canUpdateSuccess(object.getStatus())) {
            log.info("Update Main To Complete!!");
            getService().updateBillingNoticeMainStatusComplete(object.getNoticeMainId());
        }
    }

    private BillingNoticeService getService() {
        return ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeService.class);
    }

    private boolean canUpdateSuccess(String status) {
        boolean canUpdate = !getStatusList().contains(status);
        log.info("Can Update Main Success: {}", canUpdate);
        return canUpdate;
    }

    private List<String> getStatusList() {
        List<String> statusList = new ArrayList<>();
        statusList.add(BillingNoticeMain.NOTICE_STATUS_WAIT);
        statusList.add(BillingNoticeMain.NOTICE_STATUS_RETRY);
        statusList.add(BillingNoticeMain.NOTICE_STATUS_SENDING);
        statusList.add(BillingNoticeMain.NOTICE_STATUS_DRAFT);
        statusList.add(BillingNoticeMain.NOTICE_STATUS_SCHEDULED);
        return statusList;
    }
}