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

        try {
            BillingNoticeMain billingNoticeMain = (BillingNoticeMain) context.getScheduler().getContext().get("BillingNoticeMain");
            if (DataUtils.isPast(billingNoticeMain.getExpiryTime())) {
                billingNoticeService.updateStatusFailAndEmail(billingNoticeMain);
            } else {
                billingNoticeService.pushLineMessage(billingNoticeMain, null, null);
            }
        } catch (SchedulerException e) {
            log.error(e.getMessage());
        }
    }
}