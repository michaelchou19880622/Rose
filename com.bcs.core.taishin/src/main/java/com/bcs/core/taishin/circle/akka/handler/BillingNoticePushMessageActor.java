package com.bcs.core.taishin.circle.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.scheduler.BillingNoticeTaskService;
import com.bcs.core.taishin.circle.service.BillingNoticeService;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;

import java.util.Date;

/**
 * 發送訊息
 *
 * @author jessie
 */
@Slf4j
public class BillingNoticePushMessageActor extends UntypedActor {

    @Override
    public void onReceive(Object object) {
        try {
            BillingNoticeMain billingNoticeMain = (BillingNoticeMain) object;
            switch (billingNoticeMain.getSendType()) {
                case BillingNoticeMain.SENDING_MSG_TYPE_IMMEDIATE:
                    immediate(billingNoticeMain);
                    break;
                case BillingNoticeMain.SENDING_MSG_TYPE_DELAY:
                    delay(billingNoticeMain);
                    break;
                default:
                    log.error("BillingNoticePushMessageActor Type:" + billingNoticeMain.getSendType() + " no Action");
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void delay(BillingNoticeMain billingNoticeMain) throws SchedulerException {
        Date scheduleTime = DataUtils.convStrToDate(billingNoticeMain.getScheduleTime(), "yyyy-MM-dd hh:mm:ss");
        if (DataUtils.isPast(scheduleTime)) {
            immediate(billingNoticeMain);
            return;
        }
        ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeTaskService.class).startTask(billingNoticeMain, scheduleTime);
    }

    private void immediate(BillingNoticeMain billingNoticeMain) {
        ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeService.class).pushLineMessage(billingNoticeMain, this.getSender(), this.getSelf());
    }
}