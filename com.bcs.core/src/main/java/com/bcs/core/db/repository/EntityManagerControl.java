package com.bcs.core.db.repository;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.ErrorRecord;

@Repository
public class EntityManagerControl {
	private static final String INIT_FLAG = "INIT_FLAG";
	
	/** Logger */
	private static Logger logger = Logger.getLogger(EntityManagerControl.class);
	
	@PersistenceContext
	private EntityManager entityManager;
	
	// 注意！此數值必須和 spring-base.xml 的 hibernate.jdbc.batch_size 值相同
	private int batchSize = 50;
	
	private static AtomicBoolean isRun = new AtomicBoolean(false);  
	
	private ConcurrentLinkedQueue<Object> queueAdd = new ConcurrentLinkedQueue<Object>();
	
	public EntityManagerControl(){
		
	}

	public void fluchAll(){
		if(isRun.compareAndSet(false, true)){
			try{
				ApplicationContextProvider.getApplicationContext().getBean(EntityManagerControl.class).persistFlush();
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
			isRun.set(false);
		}
	}

	public void persist(Object obj){
		queueAdd.add(obj);
		logger.debug("persistAdd:" + obj);
		if(queueAdd.size() >= batchSize){
			if(isRun.compareAndSet(false, true)){
				try{
						ApplicationContextProvider.getApplicationContext().getBean(EntityManagerControl.class).persistFlush();
				}
				catch(Throwable e){
					logger.error(ErrorRecord.recordError(e));
				}
				isRun.set(false);
			}
		}
	}

	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public void persistFlush(){
//		logger.info("EntityManagerControl persistFlush execute");

		try{
			synchronized (INIT_FLAG) {
				boolean isPersistAdd = false;
				logger.debug("EntityManagerControl persistFlush execute:" + queueAdd.size());

				int i = 0;
				while(!queueAdd.isEmpty()){
		
					try{
						Object obj = queueAdd.poll();
						if(obj != null){
							entityManager.persist(obj);
							isPersistAdd = true;
						}
					}
					catch(Exception e){
						logger.error(ErrorRecord.recordError(e, false));
						break;
					}
					
					i++;

					if(i % batchSize == 0){
						if(isPersistAdd){
							entityManager.flush();
							entityManager.clear();
						}
					}
				}
		
				if(isPersistAdd){
					entityManager.flush();
					entityManager.clear();
				}
			}
		}
		catch(Throwable e){
			logger.error(ErrorRecord.recordError(e));
		}
		
		isRun.set(false);
		logger.debug("EntityManagerControl persistFlush end");
	}

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void persistInsert(List<Object> list){

		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		
		int i = 0; 
		for(Object obj : list){
			entityManager.persist(obj);
			i++;
			if(i % batchSize == 0){

				entityManager.flush();
				entityManager.clear();
			}
		}
	}
}
