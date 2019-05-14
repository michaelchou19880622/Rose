package com.bcs.core.taishin.circle.PNP.akka.handler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsg;
import com.bcs.core.utils.AkkaRouterFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class PnpMainActor extends UntypedActor {
	private static Logger logger = Logger.getLogger(PnpMainActor.class);
	private final ActorRef pushMessageRouterActor;
	private final ActorRef pnpMessageRouterActor;
	private final ActorRef updateStatusRouterActor;
	private final ActorRef expireRouterActor;
	private final ActorRef curfewActor;
	
	public PnpMainActor(){
	    pushMessageRouterActor = new AkkaRouterFactory<PnpPushMessageActor>(getContext(), PnpPushMessageActor.class, true).routerActor;
	    pnpMessageRouterActor = new AkkaRouterFactory<PnpPNPMessageActor>(getContext(), PnpPNPMessageActor.class, true).routerActor;
	    updateStatusRouterActor = new AkkaRouterFactory<PnpUpdateStatusActor>(getContext(), PnpUpdateStatusActor.class, true).routerActor;
	    expireRouterActor = new AkkaRouterFactory<PnpExpireActor>(getContext(), PnpExpireActor.class, true).routerActor;
	    curfewActor = new AkkaRouterFactory<PnpCurfewActor>(getContext(), PnpCurfewActor.class, true).routerActor; 
	}

	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof PnpMain) {
			logger.info("PnpMainActor onReceive object instanceof PnpMain!!!");
			
			PnpMain pnpMain = (PnpMain) object;
			PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);
			
			String tempId = pnpMain.getTempId();
			
			if(StringUtils.isNotBlank(tempId)) {
				//TODO 找樣版 套板
			}
			
			String stage = pnpMain.getProcStage();
			if(AbstractPnpMainEntity.STAGE_BC.equals(stage)){
				logger.info("PnpMainActor onReceive doing Line PUSH .");
				
				Integer buffer = 19;
				
				List<? super PnpDetail> details = pnpMain.getPnpDetails();
				
				List<? super PnpDetail> partition = null;
				logger.info("PnpMainActor onReceive  details.size :" + details.size());
				Integer arrayLength = details.size();
				Integer pointer = 0;
				while(pointer < arrayLength) {
					Integer counter = 0;
					partition = new ArrayList<>();
					
					for(; (counter < buffer) && (pointer < arrayLength); counter++, pointer++) {
						partition.add((PnpDetail) details.get(pointer));
					}
					PnpMain pnpMain_clone = (PnpMain) pnpMain.clone();
					
					pnpMain_clone.setPnpDetails(partition);

					pushMessageRouterActor.tell(pnpMain_clone, this.getSelf());
				}
			}else if(AbstractPnpMainEntity.STAGE_PNP.equals(stage)){
				logger.info("PnpMainActor onReceive doing Line PHONE NUMBER PUSH .");
				Integer buffer = 19;
				
				List<? super PnpDetail> details = pnpMain.getPnpDetails();
				
				List<? super PnpDetail> partition = null;
				logger.info("PnpMainActor onReceive details.size :" + details.size());
				Integer arrayLength = details.size();
				Integer pointer = 0;
				while(pointer < arrayLength) {
					Integer counter = 0;
					partition = new ArrayList<>();
					
					for(; (counter < buffer) && (pointer < arrayLength); counter++, pointer++) {
						partition.add((PnpDetail) details.get(pointer));
					}
					PnpMain pnpMain_clone = (PnpMain) pnpMain.clone();
					
					pnpMain_clone.setPnpDetails(partition);

					pnpMessageRouterActor.tell(pnpMain_clone, this.getSelf());
				}
			}
			
			
//			boolean iscurfew = billingNoticeService.isCurfew(template, Calendar.getInstance());
//			if (iscurfew) {  //宵禁中
//				curfewActor.tell(billingNoticeMain, this.getSelf());
//			}else if (Calendar.getInstance().getTime().after(billingNoticeMain.getExpiryTime())) { // Expire
//				 // update status 為不推送 並寄信
//				expireRouterActor.tell(billingNoticeMain, this.getSelf());
//			}else {
				
//			}
			
		} else if(object instanceof PnpDetail) { // update status
			updateStatusRouterActor.tell(object, this.getSelf());
		}
	}
}
