package com.bcs.core.taishin.circle.pnp.scheduler;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.pnp.code.PnpStageEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMain;
import com.bcs.core.taishin.circle.pnp.service.PnpService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author ???
 * @see PnpTaskService#startTask
 */
@Slf4j(topic = "PnpRecorder")
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

            int expiredTime = CoreConfigReader.getInteger(CONFIG_STR.PNP_DELIVERY_EXPIRED_TIME);
            // Default : 預設 PNP_DELIVERY_EXPIRE_TIME的timeout的時間最少需30分鐘, 目前系統設定值為120分鐘.
            if (expiredTime < 30) {
            	expiredTime = 30;
            }            
            Calendar expiredCalendar = new GregorianCalendar();
            Date scheduleTime = pnpService.getFormatScheduleTimeBySourceSystem(pnpMain.getSource(), pnpMain.getScheduleTime());            
        	expiredCalendar.setTime(scheduleTime);
        	expiredCalendar.add(Calendar.MINUTE, expiredTime);            
            
            /* 取得訊息發送方式 */
            String processStage = pnpMain.getProcStage();

            /* 依據排程傳進來的資訊判斷執行BC還是PNP */
            PnpStageEnum stage = PnpStageEnum.findEnumByName(processStage);
            switch (stage) {
                case BC:
    	            if (DataUtils.inBetween(new Date(), scheduleTime, expiredCalendar.getTime())) {
    	                log.info("Send a BC Schedule Push Message");
    	            	pnpService.pushLineMessage(pnpMain, null, null, stage);
    	            }
    	            else {
    	                /* 將 Main/Detail 訊息狀態更改為EXPIRED */
    	                log.info("Scheduled an expired BC schedule Push Message, update status to [EXPIRED]. ExpiredTime is : {}", expiredCalendar.getTime());	            		            		            	    	            	    	            	
    	                pnpService.updateMainAndDetailStatus(pnpMain, PnpStatusEnum.EXPIRED.value);            	            	
    	            }     	        	
                    break;
                case PNP:
    	            if (DataUtils.inBetween(new Date(), scheduleTime, expiredCalendar.getTime())) {
    	                log.info("Send a PNP Schedule Push Message");    	            	
    	            	pnpService.pushPnpMessage(pnpMain, null, null, stage);
    	            }
    	            else {
    	                /* 將 Main/Detail 訊息狀態更改為EXPIRED */
    	                log.info("Scheduled an expired PNP schedule Push Message, update status to [EXPIRED]. ExpiredTime is : {}", expiredCalendar.getTime());	            		            		            	    	            	
    	                pnpService.updateMainAndDetailStatus(pnpMain, PnpStatusEnum.EXPIRED.value);            	            	    	            	
    	            }
                    break;
                default:
                    break;
            }
        } catch (SchedulerException e) {
            log.error("Exception", e);
        }
    }
}