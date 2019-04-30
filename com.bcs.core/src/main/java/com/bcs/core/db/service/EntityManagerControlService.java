package com.bcs.core.db.service;

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.repository.EntityManagerControl;
import com.bcs.core.utils.ErrorRecord;

@Service
public class EntityManagerControlService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(EntityManagerControlService.class);

	private Timer flushTimer = new Timer();
	
	@Autowired
	private EntityManagerControl entityManagerControl;

	private class CustomTask extends TimerTask{
		
		@Override
		public void run() {

			try{
				fluchAll();
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}
	
	@PreDestroy
	public void preDestroy(){
		flushTimer.cancel();
		logger.info("[DESTROY] EntityManagerControlService flushTimer destroyed");
	}
	
	public EntityManagerControlService(){
		logger.info("EntityManagerControlService Constructor");

		flushTimer.schedule(new CustomTask(), 120000, 30000);
	}
	
	public void fluchAll(){
		logger.debug("EntityManagerControl flushTimer execute");
		try{
			entityManagerControl.fluchAll();
		}
		catch(Throwable e){
			logger.error(ErrorRecord.recordError(e));
		}
		logger.debug("EntityManagerControl flushTimer end");
	}
}
