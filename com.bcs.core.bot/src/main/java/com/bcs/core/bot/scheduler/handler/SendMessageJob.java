package com.bcs.core.bot.scheduler.handler;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.bcs.core.utils.ErrorRecord;

public class SendMessageJob extends QuartzJobBean {
	/** Logger */
	private static Logger logger = Logger.getLogger(SendMessageJob.class);
	
	private Long msgId;
	private ExecuteSendMsgTask sendMsgTask;

	public void setSendMsgTask(ExecuteSendMsgTask sendMsgTask) {
		this.sendMsgTask = sendMsgTask;
	}
	
	public void setMsgId(Long msgId) {
		this.msgId = msgId;
	}

	@Override
	protected void executeInternal(JobExecutionContext context)
			throws JobExecutionException {
		try {
			sendMsgTask.executeSendMsg(msgId);
		} catch (Throwable e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
}
