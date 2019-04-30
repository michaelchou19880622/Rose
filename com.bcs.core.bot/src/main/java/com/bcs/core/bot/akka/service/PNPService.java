package com.bcs.core.bot.akka.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.send.akka.handler.PNPMasterActor;
import com.bcs.core.utils.AkkaSystemFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Service
public class PNPService {
	/** Logger */
	private static Logger logger = Logger.getLogger(PNPService.class);
	
	private List<ActorSystem> actorSystemList = new ArrayList<ActorSystem>();
	private List<ActorRef> PNPMasterActorList = new ArrayList<ActorRef>();
	
	public PNPService() {
		new AkkaSystemFactory<PNPMasterActor>(actorSystemList, PNPMasterActorList, PNPMasterActor.class, "actorSystemList", "PNPMasterActorList");
	}
	
	public void tell(Object object) {
		ActorRef actor = getRandomActor(PNPMasterActorList);
		actor.tell(object, actor);
	}
	
	private ActorRef getRandomActor(List<ActorRef> actors){
        int index = new Random().nextInt(actors.size());
        return actors.get(index);
	}
	
	@PreDestroy
	public void shutdownNow(){
		logger.info("[DESTROY] Richart AkkaService shutdownNow cleaning up...");
		try {
			int count = 0;
			for(ActorSystem system : actorSystemList){
				system.stop(PNPMasterActorList.get(count));
				count++;
				
				system.shutdown();
				system = null;
			}
		} catch (Throwable e) {
		}
		
		System.gc();
		logger.info("[DESTROY] Richart AkkaService shutdownNow destroyed");
	}
}