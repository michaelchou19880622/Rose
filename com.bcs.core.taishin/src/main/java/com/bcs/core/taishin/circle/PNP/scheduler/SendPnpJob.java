package com.bcs.core.taishin.circle.PNP.scheduler;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.bcs.core.utils.ErrorRecord;

public class SendPnpJob extends QuartzJobBean {
	/** Logger */
	private static Logger logger = Logger.getLogger(SendPnpJob.class);
	
	private Long pnpMainId;
	private ExecuteSendPnpTask sendPnpTask;

	public void setSendPnpTask(ExecuteSendPnpTask sendPnpTask) {
		this.sendPnpTask = sendPnpTask;
	}
	
	public void setPnpMainId(Long pnpMainId) {
		this.pnpMainId = pnpMainId;
	}

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
//		try {
//		    sendPnpTask.executeSendPnp(pnpMainId);
//		} catch (Throwable e) {
//			logger.error(ErrorRecord.recordError(e));
//		}
	}
}
