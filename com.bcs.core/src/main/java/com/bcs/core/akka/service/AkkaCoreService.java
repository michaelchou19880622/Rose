package com.bcs.core.akka.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import com.bcs.core.record.akke.handler.RecordHandlerMaster;
import com.bcs.core.utils.AkkaSystemFactory;
import com.bcs.core.utils.ErrorRecord;

@Service
public class AkkaCoreService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(AkkaCoreService.class);

	private List<ActorSystem> systemRecordHandler = new ArrayList<ActorSystem>();
	private List<ActorRef> recordHandlerWork = new ArrayList<ActorRef>();
	
	private AkkaCoreService(){
		
		new AkkaSystemFactory<RecordHandlerMaster>(systemRecordHandler, recordHandlerWork, RecordHandlerMaster.class, "systemRecordHandler", "RecordHandlerMaster");
	}
	
	private ActorRef randomMaster(List<ActorRef> masters){
		logger.debug("randomMaster Size:" + masters.size());

        int index = new Random().nextInt(masters.size());
        return masters.get(index);
	}
	
	public void recordMsgs(Object record){
		try{
			ActorRef master = randomMaster(recordHandlerWork);
			master.tell(record, master);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
	}

	@PreDestroy
	public void shutdownNow(){
		logger.info("[DESTROY] AkkaCoreService shutdownNow cleaning up...");
		try{
			int count = 0;
			for(ActorSystem system : systemRecordHandler){
				system.stop(recordHandlerWork.get(count));
				count++;
				
				system.shutdown();
				system = null;
			}
		}
		catch(Throwable e){}
		
		System.gc();
		logger.info("[DESTROY] AkkaCoreService shutdownNow destroyed");
	}
}
