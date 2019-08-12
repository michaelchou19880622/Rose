package com.bcs.core.linepoint.akka.handler;

import org.json.JSONArray;

import com.bcs.core.linepoint.akka.model.LinePointSendModel;
//import com.bcs.core.richart.api.model.LinePointPushModel;
//import com.bcs.core.richart.scheduler.service.LinePointPushMessageTaskService;
//import com.bcs.core.richart.db.entity.LinePointPushMessageRecord;
//import com.bcs.core.richart.record.akka.handler.LinePointPushMessageRecordActor;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.AkkaRouterFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class LinePointSendMasterActor extends UntypedActor {
	private final ActorRef pushMessageRouterActor;
	
	public LinePointSendMasterActor(){
	    pushMessageRouterActor = new AkkaRouterFactory<LinePointSendActor>(getContext(), LinePointSendActor.class, true).routerActor;
	}

	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof LinePointSendModel) {
			LinePointSendModel pushApiModel = (LinePointSendModel) object;
			Integer buffer = 100;
			JSONArray uids = pushApiModel.getUid();
			JSONArray partition = null;
			Integer arrayLength = uids.length();
			Integer pointer = 0;
			
			while(pointer < arrayLength) {
				Integer counter = 0;
				partition = new JSONArray();
				
				for(; (counter < buffer) && (pointer < arrayLength); counter++, pointer++) {
					partition.put(uids.get(pointer));
				}
				LinePointSendModel pushApiModel_clone = (LinePointSendModel) pushApiModel.clone();
				
				pushApiModel_clone.setUid(partition);
				
				pushMessageRouterActor.tell(pushApiModel_clone, this.getSelf());
			}
			
		}
	}
}