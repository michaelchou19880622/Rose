package com.bcs.core.taishin.akka.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bcs.core.taishin.api.model.LogApiModel;
import com.bcs.core.taishin.post.akka.handler.AkkaPostApiMaster;
import com.bcs.core.utils.AkkaSystemFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Service
public class TaishinAkkaService {

	/** Logger */
	private static Logger logger = Logger.getLogger(TaishinAkkaService.class);

	private List<ActorRef> akkaPostApiMaster = new ArrayList<ActorRef>();
	private List<ActorSystem> systemakkaPostApi = new ArrayList<ActorSystem>();

	public TaishinAkkaService() {
		new AkkaSystemFactory<AkkaPostApiMaster>(systemakkaPostApi, akkaPostApiMaster, AkkaPostApiMaster.class, "systemakkaPostApi", "akkaPostApiMaster");
	}

	public void excuteLogApi(LogApiModel logApiModel) {
		ActorRef master = randomMaster(akkaPostApiMaster);
		master.tell(logApiModel, master);
	}


	private ActorRef randomMaster(List<ActorRef> masters){
		logger.debug("randomMaster Size:" + masters.size());
        int index = new Random().nextInt(masters.size());
        return masters.get(index);
	}

	@PreDestroy
	public void shutdownNow(){
		logger.info("[DESTROY] Taishin AkkaService shutdownNow cleaning up...");
		try {
			int count = 0;
			for(ActorSystem system : systemakkaPostApi){
				system.stop(akkaPostApiMaster.get(count));
				count++;

				system.shutdown();
				system = null;
			}
		} catch (Exception e) {
		}

		System.gc();
		logger.info("[DESTROY] Taishin AkkaService shutdownNow destroyed");
	}
}
