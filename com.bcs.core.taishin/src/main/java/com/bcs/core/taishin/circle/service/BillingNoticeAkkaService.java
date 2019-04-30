package com.bcs.core.taishin.circle.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bcs.core.taishin.circle.akka.handler.BillingNoticeMainActor;
import com.bcs.core.utils.AkkaSystemFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Service
public class BillingNoticeAkkaService {
	/** Logger */
	private static Logger logger = Logger.getLogger(BillingNoticeAkkaService.class);
	private List<ActorSystem> actorSystemList = new ArrayList<ActorSystem>();
	private List<ActorRef> billingNoticeActorList = new ArrayList<ActorRef>();
	
	public BillingNoticeAkkaService() {
		new AkkaSystemFactory<BillingNoticeMainActor>(actorSystemList, billingNoticeActorList, BillingNoticeMainActor.class, "actorSystemList", "BillingNoticeActorList");
	}
	
	public void tell(Object object) {
		ActorRef actor = getRandomActor(billingNoticeActorList);
		actor.tell(object, actor);
	}
	
	private ActorRef getRandomActor(List<ActorRef> actors){
        int index = new Random().nextInt(actors.size());
        return actors.get(index);
	}
	
	@PreDestroy
	public void shutdownNow(){
		logger.info("[DESTROY] BillingNotice AkkaService shutdownNow cleaning up...");
		try {
			int count = 0;
			for(ActorSystem system : actorSystemList){
				system.stop(billingNoticeActorList.get(count));
				count++;
				
				system.shutdown();
				system = null;
			}
		} catch (Throwable e) {
		}
		
		System.gc();
		logger.info("[DESTROY] BillingNotice AkkaService shutdownNow destroyed");
	}
}
