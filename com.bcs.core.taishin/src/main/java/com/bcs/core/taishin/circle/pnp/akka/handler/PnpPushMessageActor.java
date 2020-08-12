package com.bcs.core.taishin.circle.pnp.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.pnp.code.PnpSendTypeEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStageEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMain;
import com.bcs.core.taishin.circle.pnp.scheduler.PnpTaskService;
import com.bcs.core.taishin.circle.pnp.service.PnpService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
                immediatePushMessage(pnpMain, PnpStageEnum.BC);
                break;
            case DELAY:
                checkScheduleTimeThenDoImmediateOrDelay(pnpMain, PnpStageEnum.BC);
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
    private void checkScheduleTimeThenDoImmediateOrDelay(PnpMain pnpMain, PnpStageEnum pnpStageEnum) throws SchedulerException {
        PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);
        Date scheduleTime = pnpService.getFormatScheduleTimeBySourceSystem(pnpMain.getSource(), pnpMain.getScheduleTime());
        if (DataUtils.isPast(scheduleTime)) {
            immediatePushMessage(pnpMain, pnpStageEnum);
        } else {
            delayPushMessage(pnpMain, scheduleTime);
        }
    }


    /**
     * 立即發送BC訊息
     *
     * @param pnpMain pnpMain
     */
    private void immediatePushMessage(PnpMain pnpMain, PnpStageEnum pnpStageEnum) {
        log.info("Received a BC Immediate Push Message");
        PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);
        PnpSendTypeEnum sendType = PnpSendTypeEnum.findEnumByName(pnpMain.getSendType());
        
        int expiredTime = CoreConfigReader.getInteger(CONFIG_STR.PNP_DELIVERY_EXPIRED_TIME);
        //Default : 預設 PNP_DELIVERY_EXPIRE_TIME的timeout的時間最少需30分鐘, 目前系統設定值為120分鐘.
        if (expiredTime < 30) {
        	expiredTime = 30;
        }
        Calendar expiredCalendar = new GregorianCalendar();
        
        switch (sendType) {
	        case IMMEDIATE:
	        	//發送前最後檢查..檢查是否在Create Time 起算的EpiredTime內發送
	        	expiredCalendar.setTime(pnpMain.getCreateTime());
	        	expiredCalendar.add(Calendar.MINUTE, expiredTime);            
	            if (DataUtils.inBetween(new Date(), pnpMain.getCreateTime(), expiredCalendar.getTime())) {
	                log.info("Send a BC Immediate Push Message");
	            	pnpService.pushLineMessage(pnpMain, this.getSender(), this.getSelf(), pnpStageEnum);        	
	            }
	            else {
	                /* 將 Main/Detail 訊息狀態更改為EXPIRED */
	                log.info("Received an expired BC Immediate Push Message, update status to [EXPIRED]. ExpiredTime is : {}",  expiredCalendar.getTime());	            	
	                pnpService.updateMainAndDetailStatus(pnpMain, PnpStatusEnum.EXPIRED.value);            	
	            }     	        	
	            break;
	        case DELAY:
	        case SCHEDULE_TIME_EXPIRED:
	        	//發送前最後檢查..檢查是否在Schedule Time 起算的EpiredTime內發送
	            Date scheduleTime = pnpService.getFormatScheduleTimeBySourceSystem(pnpMain.getSource(), pnpMain.getScheduleTime());            
	        	expiredCalendar.setTime(scheduleTime);
	        	expiredCalendar.add(Calendar.MINUTE, expiredTime);            
	            if (DataUtils.inBetween(new Date(), scheduleTime, expiredCalendar.getTime())) {
	                log.info("Send a BC Delay/ScheduleExpired Push Message");
	            	pnpService.pushLineMessage(pnpMain, this.getSender(), this.getSelf(), pnpStageEnum);
	            }
	            else {
	                /* 將 Main/Detail 訊息狀態更改為EXPIRED */
	                log.info("Received an expired BC Delay/ScheduleExpired Push Message, update status to [EXPIRED]. ExpiredTime is : {}", expiredCalendar.getTime());	            		            	
	                pnpService.updateMainAndDetailStatus(pnpMain, PnpStatusEnum.EXPIRED.value);            	            	
	            }
	            break;
	        default:
	            break;
	    }
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