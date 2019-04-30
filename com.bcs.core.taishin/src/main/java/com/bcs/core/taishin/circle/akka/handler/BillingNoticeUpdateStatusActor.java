package com.bcs.core.taishin.circle.akka.handler;

import java.util.ArrayList;
import java.util.List;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;

import akka.actor.UntypedActor;

/**
 *  更新發送結果
 * @author jessie
 *
 */
public class BillingNoticeUpdateStatusActor extends UntypedActor {
	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof BillingNoticeDetail) {
			BillingNoticeService billingNoticeService = ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeService.class);
			BillingNoticeDetail billingNoticeDetail = (BillingNoticeDetail) object;
			billingNoticeService.save(billingNoticeDetail);
			List<String> status = new ArrayList<>();
			status.add(BillingNoticeMain.NOTICE_STATUS_WAIT);
			status.add(BillingNoticeMain.NOTICE_STATUS_RETRY);
			status.add(BillingNoticeMain.NOTICE_STATUS_SENDING);
			status.add(BillingNoticeMain.NOTICE_STATUS_DRAFT);
			status.add(BillingNoticeMain.NOTICE_STATUS_SCHEDULED);
			// update BillingNoticeMain 
			// 若明細已無重試或等待發送的狀態資料，則更新主檔狀態為完成
			if (!status.contains(billingNoticeDetail.getStatus())) {
				billingNoticeService.updateBillingNoticeMainStatusComplete(billingNoticeDetail.getNoticeMainId());
			}
			return;
		}
	}
}