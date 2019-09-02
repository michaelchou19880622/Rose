package com.bcs.core.taishin.circle.akka.handler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsg;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeService;
import com.bcs.core.utils.AkkaRouterFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class BillingNoticeMainActor extends UntypedActor {
	private final ActorRef pushMessageRouterActor;
	private final ActorRef updateStatusRouterActor;
	private final ActorRef expireRouterActor;
	private final ActorRef curfewActor;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BillingNoticeService.class);
	
	public BillingNoticeMainActor(){
	    pushMessageRouterActor = new AkkaRouterFactory<BillingNoticePushMessageActor>(getContext(), BillingNoticePushMessageActor.class, true).routerActor;
	    updateStatusRouterActor = new AkkaRouterFactory<BillingNoticeUpdateStatusActor>(getContext(), BillingNoticeUpdateStatusActor.class, true).routerActor;
	    expireRouterActor = new AkkaRouterFactory<BillingNoticeExpireActor>(getContext(), BillingNoticeExpireActor.class, true).routerActor;
	    curfewActor = new AkkaRouterFactory<BillingNoticeCurfewActor>(getContext(), BillingNoticeCurfewActor.class, true).routerActor; 
	}

	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof BillingNoticeMain) {
			
			BillingNoticeMain billingNoticeMain = (BillingNoticeMain) object;
			BillingNoticeService billingNoticeService = ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeService.class);
			BillingNoticeContentTemplateMsg template = billingNoticeMain.getTemplate();
			boolean iscurfew = billingNoticeService.isCurfew(template, Calendar.getInstance());
			logger.info("iscurfew1: " + iscurfew);
			
			if (iscurfew) {  //宵禁中
				curfewActor.tell(billingNoticeMain, this.getSelf());
			}else if (Calendar.getInstance().getTime().after(billingNoticeMain.getExpiryTime())) { // Expire
				 // update status 為不推送 並寄信
				expireRouterActor.tell(billingNoticeMain, this.getSelf());
			}else {
				Integer buffer = 100;
				//找出未發送以及重試BillingNoticeDetail
				List<BillingNoticeDetail> details = billingNoticeMain.getDetails();
				List<BillingNoticeDetail> partition = null;
				Integer arrayLength = details.size();
				Integer pointer = 0;
				
				logger.info("detail1" + details.toString());
				
				while(pointer < arrayLength) {
					Integer counter = 0;
					partition = new ArrayList<>();
					
					for(; (counter < buffer) && (pointer < arrayLength); counter++, pointer++) {
						partition.add(details.get(pointer));
					}
					BillingNoticeMain billingNoticeMain_clone = (BillingNoticeMain) billingNoticeMain.clone();
					
					billingNoticeMain_clone.setDetails(partition);
					
					pushMessageRouterActor.tell(billingNoticeMain_clone, this.getSelf());
				}
			}
			
		} else if(object instanceof BillingNoticeDetail) { // update status
			updateStatusRouterActor.tell(object, this.getSelf());
		}
	}
}
