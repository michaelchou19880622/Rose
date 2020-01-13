package com.bcs.core.taishin.circle.scheduler;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;

/**
 * @author ???
 */
@Slf4j
public class BillingNoticeTask implements Job {
    BillingNoticeService billingNoticeService = ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeService.class);

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Task Execute!!");
        try {
            BillingNoticeMain billingNoticeMain = (BillingNoticeMain) context.getScheduler().getContext().get("BillingNoticeMain");
            if (DataUtils.isPast(billingNoticeMain.getExpiryTime())) {
                log.info("Update Status Fail And Email!!");
                billingNoticeService.updateStatusFailAndEmail(billingNoticeMain);
            } else {
                log.info("Push Line Message!!");
                billingNoticeService.pushLineMessage(billingNoticeMain, null, null);
            }
        } catch (SchedulerException se) {
            log.error(se.getMessage());
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }
}