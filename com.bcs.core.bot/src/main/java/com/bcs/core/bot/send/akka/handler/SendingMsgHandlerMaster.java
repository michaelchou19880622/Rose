package com.bcs.core.bot.send.akka.handler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.bcs.core.db.entity.ContentEsnDetail;
import com.bcs.core.db.entity.MsgSendRecord;
import com.bcs.core.db.service.ContentEsnDetailService;
import com.bcs.core.db.service.MsgSendMainService;
import com.bcs.core.db.service.MsgSendRecordService;
import com.bcs.core.send.akka.model.AsyncEsnSendingModel;
import com.bcs.core.send.akka.model.AsyncEsnSendingModelError;
import com.bcs.core.send.akka.model.AsyncEsnSendingModelSuccess;
import com.bcs.core.send.akka.model.AsyncSendingModel;
import com.bcs.core.send.akka.model.AsyncSendingModelError;
import com.bcs.core.send.akka.model.AsyncSendingModelSuccess;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.AkkaRouterFactory;
import com.bcs.core.utils.ErrorRecord;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendingMsgHandlerMaster extends UntypedActor {

	public static final int pageSize = 100; //PageSize Limit

	public final static AtomicLong taskCount= new AtomicLong(0L);
	public static Date updateDate;

	/** log */
//	private static logger logger = log.getlog(SendingMsgHandlerMaster.class);

    private final ActorRef routerActor;

	public SendingMsgHandlerMaster(){
	    routerActor = new AkkaRouterFactory<SendingMsgHandlerSend>(getContext(), SendingMsgHandlerSend.class, true).routerActor;
	}

	@Override
	public void onReceive(Object message) throws Exception {
		log.info("SendingMsgHandlerMaster onReceive");

		if (message instanceof AsyncSendingModel) {
			log.info("message instanceof AsyncSendingModel");
			
			AsyncSendingModel msg = (AsyncSendingModel)message;
			log.info("msg = {}", msg);
			log.info("msg.getMidList() = {}", msg.getMidList());
			
			if(msg != null && msg.getMidList() != null){

				List<String> sendMids = new ArrayList<String>();
				for(String mid : msg.getMidList()){
					sendMids.add(mid);

					if (sendMids.size() % pageSize == 0) {
						log.info("sendMids.size() = {}", sendMids.size());

						// Handle : Sending
						AsyncSendingModel model = new AsyncSendingModel(sendMids, msg.getChannelId(), msg.getMsgGenerators(), msg.getApiType(), msg.getUpdateMsgId());
						log.info("1-1 model = {}", model);
						
						routerActor.tell(model, getSelf());
						taskCount.addAndGet(sendMids.size());
						sendMids = new ArrayList<String>();
					}
				}

				if (sendMids.size() > 0) {
					log.info("sendMids.size() = {}", sendMids.size());
					
					// Handle : Sending Else
					AsyncSendingModel model = new AsyncSendingModel(sendMids, msg.getChannelId(), msg.getMsgGenerators(), msg.getApiType(), msg.getUpdateMsgId());
					log.info("1-2 model = {}", model);
					
					routerActor.tell(model, getSelf());
					taskCount.addAndGet(sendMids.size());
				}
//				for(String mid :msg.getMids() ){
//					AsyncSendingModel model = new AsyncSendingModel(mid, msg.getChannelId(), msg.getMsgBody(), msg.getApiType(), msg.getUpdateMsgId());
//					taskCount.addAndGet(1L);
//					routerActor.tell(model, getSelf());
//				}
			}
		}
		else if (message instanceof AsyncSendingModelError) {
			log.info("message instanceof AsyncSendingModelError");
			
			AsyncSendingModelError msg = (AsyncSendingModelError)message;
			log.info("msg = {}", msg);
			log.info("msg.msg.retryTimeAdd() = {}", msg.retryTimeAdd());
			
			// Retry
			if(msg.retryTimeAdd() < 5){
				routerActor.tell(message, getSelf());
			}
			else{
				if(msg != null && msg.getUpdateMsgId() != null && msg.getMids().size() > 0){

					taskCount.addAndGet(-1 * msg.getMids().size());
					saveMsgSendLog(msg);
				}
			}
		}
		else if (message instanceof AsyncSendingModelSuccess) {
			log.info("message instanceof AsyncSendingModelSuccess");
			
			AsyncSendingModelSuccess success = (AsyncSendingModelSuccess) message;
			log.info("success = {}", success);
			log.info("success.getMids().size() = {}", success.getMids().size());

			if(success != null && success.getMids().size() > 0){
				// Setting Check Task Count
				taskCount.addAndGet(-1 * success.getMids().size());
				updateDate = Calendar.getInstance().getTime();

				if(success.getUpdateMsgId() != null){

					saveMsgSendLog(success);

					ApplicationContextProvider.getApplicationContext().getBean(MsgSendMainService.class).increaseSendCountByMsgSendId(success.getUpdateMsgId(), success.getMids().size());
//					ApplicationContextProvider.getApplicationContext().getBean(AkkaCoreService.class).recordMsgs(new MsgSendRecordModel(success.getUpdateMsgId(), success.getMids().size()));
				}
			}
		}
		else if(message instanceof AsyncEsnSendingModel) {
			log.info("message instanceof AsyncEsnSendingModel");
			
		    AsyncEsnSendingModel msg = (AsyncEsnSendingModel)message;
			log.info("msg = {}", msg);
			log.info("msg.getEsnDetails() = {}", msg.getEsnDetails());

		    List<ContentEsnDetail> esnDetails = new ArrayList<>();
		    
		    if(msg != null && msg.getEsnDetails() != null){

		        for(ContentEsnDetail esnDetail : msg.getEsnDetails()) {
		            esnDetails.add(esnDetail);

		            if(esnDetails.size() % pageSize == 0){
		                AsyncEsnSendingModel model = new AsyncEsnSendingModel(msg.getChannelId(), msg.getMessageList(), esnDetails, msg.getApiType());
		                routerActor.tell(model, getSelf());

		                esnDetails = new ArrayList<>();
		            }
		        }

		        if(esnDetails.size() > 0) {
		            AsyncEsnSendingModel model = new AsyncEsnSendingModel(msg.getChannelId(), msg.getMessageList(), esnDetails, msg.getApiType());
                    routerActor.tell(model, getSelf());
		        }
		    }
		}
		else if(message instanceof AsyncEsnSendingModelError) {
			log.info("message instanceof AsyncEsnSendingModelError");
			
		    AsyncEsnSendingModelError error = (AsyncEsnSendingModelError)message;
			log.info("error = {}", error);
			log.info("error.retryTimeAdd() = {}", error.retryTimeAdd());

		    if(error.retryTimeAdd() < 5){
                routerActor.tell(message, getSelf());
            }
            else{
                List<Long> errorDetailIds = new ArrayList<>();
                if(error.getEsnDetails() != null) {
                    for(ContentEsnDetail detail : error.getEsnDetails()) {
                        errorDetailIds.add(detail.getEsnDetailId());
                    }
                }

                if(errorDetailIds.size() > 0) {
                    ApplicationContextProvider.getApplicationContext().getBean(ContentEsnDetailService.class).updateStatusAndSendTimeByDetailIds(ContentEsnDetail.STATUS_FAIL, error.getDate(), errorDetailIds);
                }
            }
		}
		else if(message instanceof AsyncEsnSendingModelSuccess) {
			log.info("message instanceof AsyncEsnSendingModelSuccess");
			
		    AsyncEsnSendingModelSuccess success = (AsyncEsnSendingModelSuccess)message;
			log.info("success = {}", success);

		    ApplicationContextProvider.getApplicationContext().getBean(ContentEsnDetailService.class).updateStatusAndSendTimeByDetailIds(ContentEsnDetail.STATUS_FINISH, success.getDate(), success.getSuccessDetailIds());
		}
	}

	private void saveMsgSendLog(AsyncSendingModelError msg){
		log.info("---------- saveMsgSendLog ----------");
		
		log.info("AsyncSendingModelError msg = {}", msg);

		try{
			List<String> mids = msg.getMids();
			log.info("mids = {}", mids);
			
			for (String mid : mids) {
				MsgSendRecord msgSend = new MsgSendRecord();

				msgSend.setMid(mid);
				msgSend.setApiType(msg.getApiType().toString());
				msgSend.setChannelId(msg.getChannelId());
				msgSend.setMsgSendId(msg.getUpdateMsgId());
				msgSend.setSendTime(msg.getDate());
				msgSend.setSendRecord(msg.getErrorMsg());

				ApplicationContextProvider.getApplicationContext().getBean(MsgSendRecordService.class).bulkPersist(msgSend);
//				ApplicationContextProvider.getApplicationContext().getBean(AkkaCoreService.class).recordMsgs(msgSend);
			}

			log.info("---------- saveMsgSendLog completed ----------");
		}
		catch(Exception e){
			log.error(ErrorRecord.recordError(e));
		}
	}

	private void saveMsgSendLog(AsyncSendingModelSuccess success){
		log.info("---------- saveMsgSendLog ----------");
		
		log.info("AsyncSendingModelSuccess success = {}", success);

		try{
			List<String[]> mids = success.getMids();
			
			for(String[] mid : mids){
				MsgSendRecord msgSend = new MsgSendRecord();

				msgSend.setMid(mid[0]);
				msgSend.setApiType(success.getApiType().toString());
				msgSend.setChannelId(success.getChannelId());
				msgSend.setMsgSendId(success.getUpdateMsgId());
				msgSend.setSendTime(success.getDate());
				msgSend.setSendRecord(mid[1]);

				ApplicationContextProvider.getApplicationContext().getBean(MsgSendRecordService.class).bulkPersist(msgSend);
//				ApplicationContextProvider.getApplicationContext().getBean(AkkaCoreService.class).recordMsgs(msgSend);
			}

			log.info("---------- saveMsgSendLog completed ----------");
		}
		catch(Exception e){
			log.error(ErrorRecord.recordError(e));
		}
	}
}
