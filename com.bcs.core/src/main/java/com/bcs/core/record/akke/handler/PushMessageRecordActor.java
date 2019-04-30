package com.bcs.core.record.akke.handler;

import com.bcs.core.db.entity.PushMessageRecord;
import com.bcs.core.db.service.PushMessageRecordService;
import com.bcs.core.spring.ApplicationContextProvider;

import akka.actor.UntypedActor;

public class PushMessageRecordActor extends UntypedActor {
	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof PushMessageRecord) {
			PushMessageRecordService pushMessageRecordService = ApplicationContextProvider.getApplicationContext().getBean(PushMessageRecordService.class);
			PushMessageRecord pushMessageRecord = (PushMessageRecord) object;
			
			pushMessageRecordService.save(pushMessageRecord);
			
			return;
		}
	}
}