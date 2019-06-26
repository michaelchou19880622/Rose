package com.bcs.core.linepoint.akka.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bcs.core.linepoint.akka.handler.LinePointPushMasterActor;
import com.bcs.core.utils.AkkaSystemFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Service
public class LinePointPushAkkaService {
	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointPushAkkaService.class);
	private List<ActorSystem> actorSystemList = new ArrayList<ActorSystem>();
	private List<ActorRef> LinePointMasterActorList = new ArrayList<ActorRef>();
	
	public LinePointPushAkkaService() {
		new AkkaSystemFactory<LinePointPushMasterActor>(actorSystemList, LinePointMasterActorList, LinePointPushMasterActor.class, "actorSystemList", "LinePointMasterActorList");
	}
	public void tell(Object object) {
		ActorRef actor = getRandomActor(LinePointMasterActorList);
		actor.tell(object, actor);
	}
	private ActorRef getRandomActor(List<ActorRef> actors){
        int index = new Random().nextInt(actors.size());
        return actors.get(index);
	}
	
	@PreDestroy
	public void shutdownNow(){
		logger.info("[DESTROY] Line Point Push AkkaService shutdownNow cleaning up...");
		try {
			int count = 0;
			for(ActorSystem system : actorSystemList){
				system.stop(LinePointMasterActorList.get(count));
				count++;
				
				system.shutdown();
				system = null;
			}
		} catch (Throwable e) {
		}
		System.gc();
		logger.info("[DESTROY] Line Point Push AkkaService shutdownNow destroyed");
	}
}