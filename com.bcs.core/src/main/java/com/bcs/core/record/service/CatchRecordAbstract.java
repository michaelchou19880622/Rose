package com.bcs.core.record.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.log4j.Logger;

import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


public abstract class CatchRecordAbstract implements CatchRecordInterface {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(CatchRecordAbstract.class);

	protected LoadingCache<String, AtomicLong> record;
	
	public CatchRecordAbstract(){
		
		record = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(24, TimeUnit.HOURS)
				.build(new CacheLoader<String, AtomicLong>() {
					@Override
					public AtomicLong load(String key) throws Exception {
						return new AtomicLong(0);
					}
				});
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] CatchRecordAbstract cleaning up...");
		try{
			if(record != null){
				record.invalidateAll();
				record = null;
			}
		}
		catch(Throwable e){}
		
		System.gc();
		logger.info("[DESTROY] CatchRecordAbstract destroyed.");
	}
	
	public void incrementCount(){
		try {
			Date date = new Date();
			incrementCount(date);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}

	public void incrementCount(Date date){
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			
			String dateString = sdf.format(date);
			
			record.asMap().putIfAbsent(dateString, new AtomicLong(0));
			record.get(dateString).incrementAndGet();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	public Map<String, AtomicLong> getAll() throws Exception{
		return getAll(Calendar.MINUTE, 1);
	}

	/**
	 * @param field Calendar Field
	 * @param minute
	 * @return
	 * @throws Exception
	 */
	public Map<String, AtomicLong> getAll(int field, Integer zoom) throws Exception{

		Map<String, AtomicLong> map = record.asMap();
		
		/**
		 * Sort Result
		 */
		Map<String, AtomicLong> result = new LinkedMap<String, AtomicLong>();
		
		SortedSet<String> keys = new TreeSet<String>(map.keySet());
		for (String key : keys) { 

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date time = sdf.parse(key);
			
			SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			if(Calendar.HOUR_OF_DAY == field){
				out = new SimpleDateFormat("yyyy-MM-dd HH");
			}
			else if(Calendar.DATE == field){
				out = new SimpleDateFormat("yyyy-MM-dd");
			}
			
			String toKey = key;
			if(zoom > 1){
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(time);
				
				int fieldNumber = calendar.get(field);
				
				int diff = fieldNumber % zoom; 
				
				if(diff == 0){
					// pass
				}
				else{
					if(Calendar.DATE == field){
						diff--;
					}
					calendar.add(field, -diff);
				}
				
				time = calendar.getTime();
			}

			toKey = out.format(time);
			
			AtomicLong value = map.get(key);
			
			if(result.get(toKey) == null){
				result.put(toKey, new AtomicLong(0));
			}
			
			result.get(toKey).addAndGet(value.get());
		}
		return result;
	}
}
