package com.bcs.core.taishin.circle.PNP.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpTaskService;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * BC發送訊息
 *
 * @author jessie
 * STAGE = BC
 * @see PnpMainActor#PnpMainActor()
 */
@Slf4j
public class PnpPushMessageActor extends UntypedActor {

    @Override
    public void onReceive(Object object) throws Exception {
        try {
            Thread.currentThread().setName("Actor-PNP-BCPush-" + Thread.currentThread().getId());

            log.info("PnpPushMessageActor Receive!!");
            PnpMain pnpMain = (PnpMain) object;
            checkSendTypeThenDoSomething(pnpMain);
        } catch (Exception e) {
            log.error("{}", e);
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
        switch (pnpMain.getSendType()) {
            case AbstractPnpMainEntity.SEND_TYPE_IMMEDIATE:
            case AbstractPnpMainEntity.SEND_TYPE_SCHEDULE_TIME_EXPIRED:
                immediatePushMessage(pnpMain);
                break;
            case AbstractPnpMainEntity.SEND_TYPE_DELAY:
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
        Date scheduleTime = getFormatScheduleTime(pnpMain);
        if (scheduleTime != null && Calendar.getInstance().getTime().after(scheduleTime)) {
            immediatePushMessage(pnpMain);
        } else {
            delayPushMessage(pnpMain, scheduleTime);
        }
    }

    /**
     * 取得格式化後預約發送時間
     *
     * @param pnpMain pnpMain
     * @return 預約時間
     */
    private Date getFormatScheduleTime(PnpMain pnpMain) {
        try {
            log.info("pnpMain.getScheduleTime {}", pnpMain.getScheduleTime());
            return new SimpleDateFormat("yyyyMMddHHmmss").parse(pnpMain.getScheduleTime());
        } catch (ParseException e) {
            log.error("{}: {}", "Exception", e);
            log.error("ScheduleTime Format Error :" + pnpMain.getScheduleTime());
            return null;
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