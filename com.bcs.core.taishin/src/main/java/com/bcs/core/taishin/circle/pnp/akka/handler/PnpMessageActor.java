package com.bcs.core.taishin.circle.pnp.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.pnp.code.PnpSendTypeEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStageEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMain;
import com.bcs.core.taishin.circle.pnp.scheduler.PnpTaskService;
import com.bcs.core.taishin.circle.pnp.service.PnpService;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;

import java.util.Date;

/**
 * PNP發送訊息
 *
 * @author kenneth
 * STAGE = PNP
 * @see PnpMainActor#onReceive
 */
@Slf4j(topic = "PnpRecorder")
public class PnpMessageActor extends UntypedActor {

    @Override
    public void onReceive(Object object) {
        try {
            Thread.currentThread().setName("Actor-PNP-PnpPush-" + Thread.currentThread().getId());

            log.debug("PnpMessageActor Receive!!");
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
                immediate(pnpMain);
                break;
            case DELAY:
                delayOrImmediate(pnpMain);
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
    private void delayOrImmediate(PnpMain pnpMain) throws SchedulerException {
        Date scheduleTime = getFormatScheduleTimeBySourceSystem(pnpMain.getSource(), pnpMain.getScheduleTime());
        if (DataUtils.isPast(scheduleTime)) {
            immediate(pnpMain);
        } else {
            delayPushMessage(pnpMain, scheduleTime);
        }
    }

    /**
     * 取得格式化後預約發送時間
     *
     * @param sourceSystem sourceSystem
     * @param scheduleTime scheduleTIme
     * @return 預約時間
     */
    private Date getFormatScheduleTimeBySourceSystem(String sourceSystem, String scheduleTime) {
        switch (sourceSystem) {
            case AbstractPnpMainEntity.SOURCE_MITAKE:
            case AbstractPnpMainEntity.SOURCE_UNICA:
            case AbstractPnpMainEntity.SOURCE_EVERY8D:
                return DataUtils.convStrToDate(scheduleTime, "yyyyMMddHHmmss");
            case AbstractPnpMainEntity.SOURCE_MING:
            default:
                return DataUtils.convStrToDate(scheduleTime, "yyyy-MM-dd hh:mm:ss");
        }
    }

    /**
     * 立即發送PNP訊息
     *
     * @param pnpMain pnpMain
     */
    private void immediate(PnpMain pnpMain) {
        log.debug("PNP Immediate Push Message");
        PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);
        pnpService.pushPnpMessage(pnpMain, this.getSender(), this.getSelf(), PnpStageEnum.PNP);
    }

    /**
     * 啟動排程延後發送
     *
     * @param pnpMain      pnpMain
     * @param scheduleTime scheduleTime
     * @throws SchedulerException SchedulerException
     */
    private void delayPushMessage(PnpMain pnpMain, Date scheduleTime) throws SchedulerException {
        log.debug("PNP Delay Push Message");
        PnpTaskService pnpTaskService = ApplicationContextProvider.getApplicationContext().getBean(PnpTaskService.class);
        pnpTaskService.startTask(pnpMain, scheduleTime);
    }
}