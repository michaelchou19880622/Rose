package com.bcs.core.record.service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface CatchRecordInterface {
	
	public void incrementCount();
	
	public void incrementCount(Date date);

	/**
	 * 1 Minute
	 * @return Map<String, Long>
	 */
	public Map<String, AtomicLong> getAll() throws Exception;
	
	/**
	 * @param field Calendar Field
	 * @param zoom
	 * @return
	 * @throws Exception
	 */
	public Map<String, AtomicLong> getAll(int field, Integer zoom) throws Exception;
	
	public void loadInitData();
}
