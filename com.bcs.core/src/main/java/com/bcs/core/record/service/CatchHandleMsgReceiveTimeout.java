package com.bcs.core.record.service;

import org.springframework.stereotype.Service;

import com.bcs.core.resource.CoreConfigReader;

@Service
public class CatchHandleMsgReceiveTimeout extends CatchRecordAbstract {
	
	@Override
	public void loadInitData() {

		if(!CoreConfigReader.isSystemTypeProduction()){
			return;
		}
	}
}
