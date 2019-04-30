package com.bcs.core.taishin.circle.PNP.akka.handler;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;

import akka.actor.UntypedActor;

/**
 * 發送效期已過不推送訊息
 * @author jessie
 *
 */
public class PnpExpireActor extends UntypedActor {
	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof BillingNoticeMain) {
			BillingNoticeService billingNoticeService = ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeService.class);
			BillingNoticeMain billingNoticeMain = (BillingNoticeMain) object;
			// update status 為不推送 並寄信
			billingNoticeService.updateStatusFailAndEmail(billingNoticeMain);
			return;
		}
	}
}