package com.bcs.core.taishin.circle.PNP.scheduler;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;

@Service
public class PnpTaskService {
	@Autowired
	private PnpService pnpService;
	
	/**
	 * 排程通知
	 * @param PnpMain
	 *  @param scheduleTime
	 * @throws SchedulerException
	 */
	public void startTask(PnpMain pnpMain, Date scheduleTime) throws SchedulerException {
		if (scheduleTime == null) {
			return;
		}
//		SchedulerFactory schedulerFactory = new StdSchedulerFactory();
//	    Scheduler scheduler = schedulerFactory.getScheduler();
//	    
//	    JobDetail jobDetail = newJob(PnpTask.class).withIdentity("PNPMain", "PNP").build();
//	    Trigger trigger = newTrigger().withIdentity("PNPMain", "PNP").startAt(scheduleTime).build();
//	    
//	    scheduler.getContext().put("PnpMain", pnpMain);
//	    
//	    scheduler.scheduleJob(jobDetail, trigger);
//	    pnpService.updateMainAndDetailStatus(pnpMain, AbstractPnpMainEntity.SEND_TYPE_SCHEDULE);
//	    scheduler.start();
//	    
	}
}
