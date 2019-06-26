package com.bcs.core.linepoint.akka.handler;

import org.json.JSONArray;

import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.AkkaRouterFactory;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class LinePointPushMasterActor extends UntypedActor {
	private final ActorRef pushMessageRouterActor;
//	private final ActorRef pushMessageRecordRouterActor;
//	private final ActorRef ftpTaskRouterActor;
	
	public LinePointPushMasterActor(){
	    pushMessageRouterActor = new AkkaRouterFactory<LinePointPushMessageActor>(getContext(), LinePointPushMessageActor.class, true).routerActor;
//	    pushMessageRecordRouterActor = new AkkaRouterFactory<LinePointPushMessageRecordActor>(getContext(), LinePointPushMessageRecordActor.class, true).routerActor;
//	    ftpTaskRouterActor = new AkkaRouterFactory<LinePointFtpTaskActor>(getContext(), LinePointFtpTaskActor.class, true).routerActor;
	}

	@Override
	public void onReceive(Object object) throws Exception {
		if(object instanceof LinePointPushModel) {
			LinePointPushModel pushApiModel = (LinePointPushModel) object;
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
				LinePointPushModel pushApiModel_clone = (LinePointPushModel) pushApiModel.clone();
				
				pushApiModel_clone.setUid(partition);
				
				pushMessageRouterActor.tell(pushApiModel_clone, this.getSelf());
			}
			
		} 
//		else if(object instanceof FtpTaskModel) {
//			FtpTaskModel ftpTaskModel = (FtpTaskModel) object;
//			
//			if(ftpTaskModel.getFileHead().getMessageSendType().equals(LinePointPushModel.SEND_TYPE_IMMEDIATE)) {	// 立即發送
//				ftpTaskRouterActor.tell(object, this.getSelf());
//			} else if(ftpTaskModel.getFileHead().getMessageSendType().equals(LinePointPushModel.SEND_TYPE_DELAY)) {	// 延遲發送
//				if(ftpTaskModel.getIsScheduled() != null && ftpTaskModel.getIsScheduled())
//					ftpTaskRouterActor.tell(object, this.getSelf());
//				else {
//					ApplicationContextProvider.getApplicationContext().getBean(LinePointPushMessageTaskService.class).startTaskFromFtp(ftpTaskModel);
//				}
//			}
//		} else if(object instanceof LinePointPushMessageRecord) {
//			pushMessageRecordRouterActor.tell(object, this.getSelf());
//		}
	}
}