package com.bcs.core.db.repository;

import java.util.List;

import com.bcs.core.db.entity.SystemLog;

public interface SystemLogRepositoryCustom{
	
	public void bulkPersist(List<SystemLog> systemLogs);
	
	public void bulkPersist(SystemLog systemLog);
}
