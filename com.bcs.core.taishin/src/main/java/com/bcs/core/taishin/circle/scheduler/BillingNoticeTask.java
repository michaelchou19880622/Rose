package com.bcs.core.taishin.circle.scheduler;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;

public class BillingNoticeTask implements Job {	
	BillingNoticeService billingNoticeService = ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeService.class);
	private static Logger logger = Logger.getLogger(BillingNoticeTask.class);
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		try {
			BillingNoticeMain billingNoticeMain = (BillingNoticeMain) context.getScheduler().getContext().get("BillingNoticeMain");
			if (Calendar.getInstance().getTime().after(billingNoticeMain.getExpiryTime())) { // Expire
				 // update status 為不推送 並寄信
				billingNoticeService.updateStatusFailAndEmail(billingNoticeMain);
			}else {
				billingNoticeService.pushLineMessage(billingNoticeMain, null, null);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}
}