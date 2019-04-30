package com.bcs.core.bot.send.akka.handler;

import org.json.JSONArray;

import com.bcs.core.api.service.model.PushApiModel;
import com.bcs.core.db.entity.PushMessageRecord;
import com.bcs.core.record.akke.handler.PushMessageRecordActor;
import com.bcs.core.utils.AkkaRouterFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class PNPMasterActor extends UntypedActor {
	private final ActorRef pushMessageRouterActor;
	private final ActorRef pushMessageRecordRouterActor;
	
	public PNPMasterActor(){
	    pushMessageRouterActor = new AkkaRouterFactory<PushMessageActor>(getContext(), PushMessageActor.class, true).routerActor;
	    pushMessageRecordRouterActor = new AkkaRouterFactory<PushMessageRecordActor>(getContext(), PushMessageRecordActor.class, true).routerActor;
	}

	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof PushApiModel) {
			PushApiModel pushApiModel = (PushApiModel) object;
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
				PushApiModel pushApiModel_clone = (PushApiModel) pushApiModel.clone();
				
				pushApiModel_clone.setUid(partition);
				
				pushMessageRouterActor.tell(pushApiModel_clone, this.getSelf());
			}
		} else if(object instanceof PushMessageRecord) {
			pushMessageRecordRouterActor.tell(object, this.getSelf());
		}
	}
}