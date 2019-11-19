package com.bcs.core.taishin.circle.PNP.scheduler;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;

/**
 * @author ???
 * @see PnpTaskService#startTask
 */
@Slf4j
public class PnpTask implements Job {
    private PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Current Thread Name : {}", Thread.currentThread().getName());
        log.info("Current Thread ID   : {}", Thread.currentThread().getId());
        Thread.currentThread().setName("Pnp-QuartzScheduler-" + Thread.currentThread().getId());

        log.info("SCHEDULE TIME IS UP!! RUN PUSH TASK!!");
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
            log.error("Exception", e);
        }
    }
}