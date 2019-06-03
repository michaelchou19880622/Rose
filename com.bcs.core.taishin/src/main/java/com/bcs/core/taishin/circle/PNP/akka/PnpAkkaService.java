package com.bcs.core.taishin.circle.PNP.akka;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bcs.core.taishin.circle.PNP.akka.handler.PnpMainActor;
import com.bcs.core.utils.AkkaSystemFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Service
public class PnpAkkaService {
	/** Logger */
	private static Logger logger = Logger.getLogger(PnpAkkaService.class);
	private List<ActorSystem> actorSystemList = new ArrayList<ActorSystem>();
	private List<ActorRef> PnpActorList = new ArrayList<ActorRef>();
	
	public PnpAkkaService() {
		new AkkaSystemFactory<PnpMainActor>(actorSystemList, PnpActorList, PnpMainActor.class, "actorSystemList", "PnpActorList");
	}
	
	public void tell(Object object) {
		ActorRef actor = getRandomActor(PnpActorList);
		actor.tell(object, actor);
	}
	
	private ActorRef getRandomActor(List<ActorRef> actors){
        int index = new Random().nextInt(actors.size());
        return actors.get(index);
	}
	
	public String getProcApName() {
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}
			
		} catch (Exception e) {
			logger.error(" getHostName error:" + e.getMessage());
		}
		return procApName;
	}
	
	@PreDestroy
	public void shutdownNow(){
		logger.info("[DESTROY] Pnp AkkaService shutdownNow cleaning up...");
		try {
			int count = 0;
			for(ActorSystem system : actorSystemList){
				system.stop(PnpActorList.get(count));
				count++;
				
				system.shutdown();
				system = null;
			}
		} catch (Throwable e) {
		}
		
		System.gc();
		logger.info("[DESTROY] Pnp AkkaService shutdownNow destroyed");
	}
}
