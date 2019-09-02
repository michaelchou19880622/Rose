package com.bcs.core.taishin.circle.PNP.scheduler;

import com.bcs.core.taishin.circle.PNP.akka.handler.PnpMessageActor;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @see PnpMessageActor#onReceive
 */
@Service
public class PnpTaskService {
    @Autowired
    private PnpService pnpService;

    /**
     * BC & PNP 開始排程
     * 將DB中訊息狀態改為Delay
     *
     * @param pnpMain pnpMain
     * @param scheduleTime scheduleTime
     * @throws SchedulerException SchedulerException
     *
     * PNP Actor
     * @see PnpMessageActor#onReceive
     * BC Actor
     * @see com.bcs.core.taishin.circle.PNP.akka.handler.PnpPushMessageActor#onReceive
     */
    public void startTask(PnpMain pnpMain, Date scheduleTime) throws SchedulerException {
        if (scheduleTime == null) {
            return;
        }
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        /* Job */
        JobDetail jobDetail = newJob(PnpTask.class).withIdentity("PNPMain", "PNP").build();
        /* 執行週期 */
        Trigger trigger = newTrigger().withIdentity("PNPMain", "PNP").startAt(scheduleTime).build();

        scheduler.getContext().put("PnpMain", pnpMain);

        scheduler.scheduleJob(jobDetail, trigger);
        /* 批次開始將訊息狀態更改為Delay */
        pnpService.updateMainAndDetailStatus(pnpMain, AbstractPnpMainEntity.SEND_TYPE_DELAY);
        scheduler.start();
    }
}
