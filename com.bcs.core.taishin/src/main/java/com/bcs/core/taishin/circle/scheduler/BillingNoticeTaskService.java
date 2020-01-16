package com.bcs.core.taishin.circle.scheduler;

import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @author ???
 */
@Slf4j(topic = "BNRecorder")
@Service
public class BillingNoticeTaskService {

    private BillingNoticeService billingNoticeService;

    @Autowired
    public BillingNoticeTaskService(BillingNoticeService billingNoticeService) {
        this.billingNoticeService = billingNoticeService;
    }

    public void startTask(BillingNoticeMain billingNoticeMain, Date scheduleTime) throws SchedulerException {
        log.info("Task!!");
        if (scheduleTime == null) {
            return;
        }
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();

        JobDetail jobDetail = newJob(BillingNoticeTask.class).withIdentity("billingNoticeMain", "BillingNotice").build();
        Trigger trigger = newTrigger().withIdentity("billingNoticeMain", "BillingNotice").startAt(scheduleTime).build();

        scheduler.getContext().put("BillingNoticeMain", billingNoticeMain);

        scheduler.scheduleJob(jobDetail, trigger);
        billingNoticeService.updateMainAndDetailStatus(billingNoticeMain, BillingNoticeMain.SENDING_MSG_TYPE_SCHEDULE);
        scheduler.start();
    }
}
