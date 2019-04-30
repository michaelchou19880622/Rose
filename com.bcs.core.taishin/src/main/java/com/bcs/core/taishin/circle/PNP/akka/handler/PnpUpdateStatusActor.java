package com.bcs.core.taishin.circle.PNP.akka.handler;

import java.util.ArrayList;
import java.util.List;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
//import com.bcs.core.taishin.circle.billingNotice.db.entity.BillingNoticeDetail;
//import com.bcs.core.taishin.circle.billingNotice.db.entity.BillingNoticeMain;
//import com.bcs.core.taishin.circle.billingNotice.service.BillingNoticeService;

import akka.actor.UntypedActor;

/**
 *  更新發送結果
 * @author jessie
 *
 */
public class PnpUpdateStatusActor extends UntypedActor {
	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof PnpDetail) {
			PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);
			PnpDetail pnpDetail = (PnpDetail) object;
			pnpService.save(pnpDetail);
			List<String> status = new ArrayList<>();
			status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
			status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING);
			status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
			status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
			status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_SCHEDULED);
			// update BillingNoticeMain 
			// 若明細已無重試或等待發送的狀態資料，則更新主檔狀態為完成
			if (!status.contains(pnpDetail.getStatus())) {
				pnpService.updatePnpMainStatusComplete(pnpDetail.getPnpMainId() , pnpDetail.getSource());
			}
			return;
		}
	}
}