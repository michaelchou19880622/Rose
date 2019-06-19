package com.bcs.core.taishin.circle.PNP.scheduler;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.service.PnpService;

public class PnpTask implements Job {	
	PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);
	private static Logger logger = Logger.getLogger(PnpTask.class);
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		try {
			PnpMain pnpMain = (PnpMain) context.getScheduler().getContext().get("PnpMain");
			
			String procStage = pnpMain.getProcStage();
			
			if (AbstractPnpMainEntity.STAGE_BC.equals(procStage)) {
				pnpService.pushLineMessage(pnpMain, null, null);
			}else if(AbstractPnpMainEntity.STAGE_PNP.equals(procStage)){
				pnpService.pushPNPMessage(pnpMain, null, null);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	}
}