package com.bcs.core.taishin.circle.PNP.akka.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpTaskService;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
//import com.bcs.core.taishin.circle.billingNotice.db.entity.BillingNoticeMain;
//import com.bcs.core.taishin.circle.billingNotice.scheduler.BillingNoticeTaskService;
//import com.bcs.core.taishin.circle.billingNotice.service.BillingNoticeService;

import akka.actor.UntypedActor;

/**
 * 發送訊息
 * @author jessie
 *
 */
public class PnpPushMessageActor extends UntypedActor {
	private static Logger logger = Logger.getLogger(PnpPushMessageActor.class);
	private DateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	@Override
	public void onReceive(Object object) throws Exception {
		logger.info("PnpPushMessageActor onReceive !!!");
		PnpMain pnpMain = (PnpMain) object;
		PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);
		if (pnpMain.getSendType().equals(AbstractPnpMainEntity.SEND_TYPE_IMMEDIATE)) {	// 立即發送
			pnpService.pushLineMessage(pnpMain, this.getSender(), this.getSelf());
		}else if(pnpMain.getSendType().equals(AbstractPnpMainEntity.SEND_TYPE_SCHEDULE_TIME_EXPIRED)) {//排程時間小於現在時間則視為立即發送
			pnpService.pushLineMessage(pnpMain, this.getSender(), this.getSelf());
		}else if (pnpMain.getSendType().equals(AbstractPnpMainEntity.SEND_TYPE_DELAY)) {	// 預約發送
		    Date scheduleTime = null;
		    try {
				scheduleTime = dataFormat.parse(pnpMain.getScheduleTime());
			} catch (ParseException e) {
				e.printStackTrace();
				logger.error("ScheduleTime format Error :" + pnpMain.getScheduleTime());
			}
		    if (scheduleTime != null && Calendar.getInstance().getTime().after(scheduleTime)) {
		    	// 排程時間因為已過現在時間， 立刻發送
		    	pnpService.pushLineMessage(pnpMain, this.getSender(), this.getSelf());
		    }else {
		    	PnpTaskService pnpTaskService = ApplicationContextProvider.getApplicationContext().getBean(PnpTaskService.class);
		    	pnpTaskService.startTask(pnpMain, scheduleTime);
		    }
		}else {
			logger.error("PnpPushMessageActor Type:" + pnpMain.getSendType() + " no Action");
		}
	}
}