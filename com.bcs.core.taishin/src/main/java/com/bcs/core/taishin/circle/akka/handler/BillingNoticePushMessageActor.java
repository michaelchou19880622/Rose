package com.bcs.core.taishin.circle.akka.handler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.scheduler.BillingNoticeTaskService;
import com.bcs.core.taishin.circle.service.BillingNoticeService;

import akka.actor.UntypedActor;

/**
 * 發送訊息
 * @author jessie
 *
 */
public class BillingNoticePushMessageActor extends UntypedActor {
	private static Logger logger = Logger.getLogger(BillingNoticePushMessageActor.class);
	private DateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	@Override
	public void onReceive(Object object) throws Exception {
		BillingNoticeMain billingNoticeMain = (BillingNoticeMain) object;
		BillingNoticeService billingNoticeService = ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeService.class);
		if (billingNoticeMain.getSendType().equals(BillingNoticeMain.SENDING_MSG_TYPE_IMMEDIATE)) {	// 立即發送
				billingNoticeService.pushLineMessage(billingNoticeMain, this.getSender(), this.getSelf());
		} else if (billingNoticeMain.getSendType().equals(BillingNoticeMain.SENDING_MSG_TYPE_DELAY)) {	// 預約發送
		    Date scheduleTime = null;
		    try {
				scheduleTime = dataFormat.parse(billingNoticeMain.getScheduleTime());
			} catch (ParseException e) {
				e.printStackTrace();
				logger.error("ScheduleTime format Error :" + billingNoticeMain.getScheduleTime());
			}
		    if (scheduleTime != null && Calendar.getInstance().getTime().after(scheduleTime)) {
		    	// 排程時間因為宵禁已過 立刻重發
		    	billingNoticeService.pushLineMessage(billingNoticeMain, this.getSender(), this.getSelf());
		    }else {
		    	BillingNoticeTaskService billingNoticeTaskService = ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeTaskService.class);
				billingNoticeTaskService.startTask(billingNoticeMain, scheduleTime);
		    }
		}else {
			logger.error("BillingNoticePushMessageActor Type:" + billingNoticeMain.getSendType() + " no Action");
		}
	}
}