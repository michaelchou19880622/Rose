package com.bcs.core.taishin.circle.PNP.scheduler;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

/**
 * @see PnpTaskService#startTask
 * @author ???
 */
public class PnpTask implements Job {
    private static Logger logger = Logger.getLogger(PnpTask.class);
    private PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        try {
            PnpMain pnpMain = (PnpMain) context.getScheduler().getContext().get("PnpMain");

            /* 取得訊息發送方式 */
            String processStage = pnpMain.getProcStage();

            /* 依據排程傳進來的資訊判斷執行BC還是PNP */
            if (AbstractPnpMainEntity.STAGE_BC.equals(processStage)) {
                pnpService.pushLineMessage(pnpMain, null, null);
            } else if (AbstractPnpMainEntity.STAGE_PNP.equals(processStage)) {
                pnpService.pushPnpMessage(pnpMain, null, null);
            }
        } catch (SchedulerException e) {
            logger.error(e);
        }
    }
}