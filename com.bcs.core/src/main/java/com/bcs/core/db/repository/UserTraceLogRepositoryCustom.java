package com.bcs.core.db.repository;

import java.util.List;

import com.bcs.core.db.entity.UserTraceLog;

public interface UserTraceLogRepositoryCustom{
	
	public void bulkPersist(List<UserTraceLog> userTraceLogs);
	
	public void bulkPersist(UserTraceLog userTraceLog);
}
