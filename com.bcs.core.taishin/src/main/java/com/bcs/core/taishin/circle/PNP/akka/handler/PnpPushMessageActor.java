package com.bcs.core.taishin.circle.PNP.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.PNP.code.PnpSendTypeEnum;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpTaskService;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;

import java.util.Date;

/**
 * BC發送訊息
 *
 * @author jessie
 * STAGE = BC
 * @see PnpMainActor#PnpMainActor()
 */
@Slf4j(topic = "PnpRecorder")
public class PnpPushMessageActor extends UntypedActor {

    @Override
    public void onReceive(Object object) {
        try {
            Thread.currentThread().setName("Actor-PNP-BCPush-" + Thread.currentThread().getId());

            log.info("PnpPushMessageActor Receive!!");
            PnpMain pnpMain = (PnpMain) object;
            checkSendTypeThenDoSomething(pnpMain);
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    /**
     * 判斷訊息發送方式為何並進行相關動作
     *
     * @param pnpMain pnpMain
     * @throws SchedulerException SchedulerException
     */
    private void checkSendTypeThenDoSomething(PnpMain pnpMain) throws SchedulerException {
        log.info("Send Type : " + pnpMain.getSendType());
        PnpSendTypeEnum sendType = PnpSendTypeEnum.findEnumByName(pnpMain.getSendType());
        switch (sendType) {
            case IMMEDIATE:
            case SCHEDULE_TIME_EXPIRED:
                immediatePushMessage(pnpMain);
                break;
            case DELAY:
                checkScheduleTimeThenDoImmediateOrDelay(pnpMain);
                break;
            default:
                log.error("PnpPushMessageActor Type:" + pnpMain.getSendType() + " No Action");
                break;
        }
    }

    /**
     * 判斷預約時間是否過期則決定是否立即發送或啟動排程延後發送
     *
     * @param pnpMain pnpMain
     * @throws SchedulerException SchedulerException
     */
    private void checkScheduleTimeThenDoImmediateOrDelay(PnpMain pnpMain) throws SchedulerException {
        Date scheduleTime = DataUtils.convStrToDate(pnpMain.getScheduleTime(), "yyyyMMddHHmmss");
        if (DataUtils.isPast(scheduleTime)) {
            immediatePushMessage(pnpMain);
        } else {
            delayPushMessage(pnpMain, scheduleTime);
        }
    }


    /**
     * 立即發送BC訊息
     *
     * @param pnpMain pnpMain
     */
    private void immediatePushMessage(PnpMain pnpMain) {
        log.info("BC Immediate Push Message");
        PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);
        pnpService.pushLineMessage(pnpMain, this.getSender(), this.getSelf());
    }

    /**
     * 啟動排程延後發送
     *
     * @param pnpMain      pnpMain
     * @param scheduleTime scheduleTime
     * @throws SchedulerException SchedulerException
     */
    private void delayPushMessage(PnpMain pnpMain, Date scheduleTime) throws SchedulerException {
        log.info("BC Delay Push Message");
        PnpTaskService pnpTaskService = ApplicationContextProvider.getApplicationContext().getBean(PnpTaskService.class);
        pnpTaskService.startTask(pnpMain, scheduleTime);
    }
}