package com.bcs.core.linepoint.akka.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import com.bcs.core.linepoint.akka.handler.LinePointSendMasterActor;
import com.bcs.core.utils.AkkaSystemFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Service
public class LinePointSendAkkaService {
	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointSendAkkaService.class);
	
	
	private List<ActorSystem> actorSystemList = new ArrayList<ActorSystem>();
	private List<ActorRef> RichMenuMasterActorList = new ArrayList<ActorRef>();
	
	public LinePointSendAkkaService() {
		new AkkaSystemFactory<LinePointSendMasterActor>(actorSystemList, RichMenuMasterActorList, LinePointSendMasterActor.class, "actorSystemList", "RichMenuMasterActorList");
	}
	public void tell(Object object) {
		ActorRef actor = getRandomActor(RichMenuMasterActorList);
		actor.tell(object, actor);
	}
	private ActorRef getRandomActor(List<ActorRef> actors){
        int index = new Random().nextInt(actors.size());
        return actors.get(index);
	}
	
	@PreDestroy
	public void shutdownNow(){
		logger.info("[DESTROY] Rich Menu Send AkkaService shutdownNow cleaning up...");
		try {
			int count = 0;
			for(ActorSystem system : actorSystemList){
				system.stop(RichMenuMasterActorList.get(count));
				count++;
				
				system.shutdown();
				system = null;
			}
		} catch (Throwable e) {
		}
		System.gc();
		logger.info("[DESTROY] Rich Menu Send AkkaService shutdownNow destroyed");
	}
}