package com.bcs.core.taishin.circle.PNP.akka.handler;

import org.apache.log4j.Logger;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;

import akka.actor.UntypedActor;

/**
 * 宵禁中 狀態改為retry
 * @author jessie
 *
 */
public class PnpCurfewActor extends UntypedActor {
	private static Logger logger = Logger.getLogger(PnpCurfewActor.class);
	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof BillingNoticeMain) {
			BillingNoticeService billingNoticeService = ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeService.class);
			BillingNoticeMain billingNoticeMain = (BillingNoticeMain) object;
			// 宵禁中 狀態改為retry
			for (BillingNoticeDetail detail : billingNoticeMain.getDetails()) {
				detail.setStatus(BillingNoticeMain.NOTICE_STATUS_RETRY);
				logger.info("Curfew NOTICE_STATUS_RETRY NoticeDetailId:" + detail.getNoticeDetailId());
				billingNoticeService.save(detail);
			}
			return;
		}
	}
}