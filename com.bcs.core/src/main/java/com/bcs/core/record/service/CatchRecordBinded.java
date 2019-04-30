package com.bcs.core.record.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.service.UserTraceLogService;

@Service
public class CatchRecordBinded extends CatchRecordAbstractSystemLog {
	
	@Autowired
	private UserTraceLogService systemLogService;
	
	@Override
	public void loadInitData() {
		this.loadFromSystemLog(systemLogService, "LineUser", "Binded");
	}
}
