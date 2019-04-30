package com.bcs.core.taishin.circle.PNP.akka.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.PNP.akka.model.AsyncPnpSendModel;
import com.bcs.core.taishin.circle.PNP.akka.model.AsyncPnpSendModelError;
import com.bcs.core.taishin.circle.PNP.akka.model.AsyncPnpSendModelSuccess;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpSendRecord;
import com.bcs.core.taishin.circle.PNP.service.PnpSendRecordService;
import com.bcs.core.utils.AkkaRouterFactory;
import com.bcs.core.utils.ErrorRecord;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

public class SendingPnpHandlerMaster extends UntypedActor {
	
	public static final int pageSize = 100; //PageSize Limit

	/** Logger */
	private static Logger logger = Logger.getLogger(SendingPnpHandlerMaster.class);

    private final ActorRef routerActor;
    
	public SendingPnpHandlerMaster(){
	    routerActor = new AkkaRouterFactory<SendingPnpHandlerSend>(getContext(), SendingPnpHandlerSend.class, true).routerActor;
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		logger.debug("SendingPnpHandlerMaster onReceive");

		if (message instanceof AsyncPnpSendModel) {
		    AsyncPnpSendModel msg = (AsyncPnpSendModel)message;
			if(msg != null && msg.getPnpDetails() != null){
				
				List<PnpDetail> sendDetails = new ArrayList<>();
				for(PnpDetail detail : msg.getPnpDetails()){
				    sendDetails.add(detail);
					
					if(sendDetails.size() % pageSize == 0){

						// Handle : Sending
						AsyncPnpSendModel model = new AsyncPnpSendModel(msg.getChannelId(), msg.getApiType(), sendDetails);
						routerActor.tell(model, getSelf());
						sendDetails = new ArrayList<>();
					}
				}

				if(sendDetails.size() > 0){
					// Handle : Sending Else
				    AsyncPnpSendModel model = new AsyncPnpSendModel(msg.getChannelId(), msg.getApiType(), sendDetails);
					routerActor.tell(model, getSelf());
				}
			}
		}
		else if (message instanceof AsyncPnpSendModelError) {
		    AsyncPnpSendModelError msg = (AsyncPnpSendModelError)message;
			// Retry
			if(msg.retryTimeAdd() < 5){
				routerActor.tell(message, getSelf());
			}
			else{
				if(msg != null && msg.getPnpDetails().size() > 0){
					saveMsgSendLog(msg);
				}
			}
		}
		else if (message instanceof AsyncPnpSendModelSuccess) {
		    AsyncPnpSendModelSuccess success = (AsyncPnpSendModelSuccess) message;
			
			if(success != null && success.getPnpDetails().size() > 0){
	
				saveMsgSendLog(success);

//				ApplicationContextProvider.getApplicationContext().getBean(PnpMainService.class).increaseSendCountByPnpMainId(success.getPnpDetails().get(0).getPnpMainId(), success.getPnpDetails().size());
			}
		}
	}
	
	private void saveMsgSendLog(AsyncPnpSendModelError msg){

		try{
			List<PnpDetail> details = msg.getPnpDetails();
			for(PnpDetail detail : details){
				PnpSendRecord record = new PnpSendRecord();

				record.setPhone(detail.getPhone());
				record.setApiType(msg.getApiType().toString());
				record.setChannelId(msg.getChannelId());
				record.setPnpMainId(detail.getPnpMainId());
				record.setPnpDetailId(detail.getPnpDetailId());
				record.setSendTime(msg.getDate());
				record.setRecord(msg.getErrorMsg());

				ApplicationContextProvider.getApplicationContext().getBean(PnpSendRecordService.class).bulkPersist(record);
			}
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	private void saveMsgSendLog(AsyncPnpSendModelSuccess success){

		try{
			List<PnpDetail> details = success.getPnpDetails();
			for(PnpDetail detail : details){
			    PnpSendRecord record = new PnpSendRecord();

			    record.setPhone(detail.getPhone());
			    record.setApiType(success.getApiType().toString());
			    record.setChannelId(success.getChannelId());
			    record.setPnpMainId(detail.getPnpMainId());
			    record.setPnpDetailId(detail.getPnpDetailId());
			    record.setSendTime(success.getDate());
			    record.setRecord("200-");

				ApplicationContextProvider.getApplicationContext().getBean(PnpSendRecordService.class).bulkPersist(record);
			}
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
	}
}
