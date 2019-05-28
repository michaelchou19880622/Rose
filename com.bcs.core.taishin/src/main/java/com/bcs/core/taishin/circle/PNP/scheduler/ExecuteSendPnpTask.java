package com.bcs.core.taishin.circle.PNP.scheduler;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bcs.core.db.service.LineUserService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;


public class ExecuteSendPnpTask {

    private static final int pageSize = 9000;
    private static final int sendSize = 150;
    private static final int sleepCount = 10;
    
	/** Logger */
	private static Logger logger = Logger.getLogger(ExecuteSendPnpTask.class);

	@Autowired
	private LineUserService lineUserService;
	
	
	public void executeSendTask() throws Exception{
		/**
		 * #通路參數 : 寄BC 失敗直接結束 = 1
		 * #通路參數 : 寄BC 失敗轉發SMS後結束 =2
		 * #通路參數 : 寄BC 失敗後寄PNP失敗後寄SMS結束 =3
		 */
		
		String sendPath = CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_MING.toString(), true, false);
		switch (sendPath) {
		case "1"://1=BC>PNP>SMS
			
			break;
        case "2"://2=BC>>SMS
			
			break;
        case "3"://3=BC
	
        	break;

		default:
			break;
		}
		
		
	}
	
	public void executeSendPnp(Long pnpMainId) throws Exception{
//		logger.debug("executeSendPnp pnpMainId ============ :" + pnpMainId);
//
//		PnpMainService pnpMainService = ApplicationContextProvider.getApplicationContext().getBean(PnpMainService.class);
////		PnpDetailService pnpDetailService = ApplicationContextProvider.getApplicationContext().getBean(PnpDetailService.class);
//
//		PnpMain pnpMain = pnpMainService.findOne(pnpMainId);
//		if(pnpMain != null){
//		    pnpMain.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
//		    pnpMain.setSendTime(new Date());
//		    pnpMainService.save(pnpMain);
//		    
//			try{
//			    int page = 0;
//			    
//			    while(true) {
//			        List<PnpDetail> details = pnpDetailService.findByPnpMainId(pnpMainId, page, pageSize);
//			        
//			        if(details != null && details.size() > 0) {
//
//			            int count = 0;
//			            
//			            List<PnpDetail> sendDetails = new ArrayList<>();
//			            for(PnpDetail detail : details){
//			                sendDetails.add(detail);
//			                
//			                if(sendDetails.size() % sendSize == 0){
//			                    
//			                    // Handle : Sending
//			                    sendMsgToPhones(sendDetails);
//			                    sendDetails = new ArrayList<>();
//			                    
//			                    count++;
//			                    if(count % sleepCount == 0){
//			                        // delay 10 seconds
//			                        Thread.sleep(10*1000);
//			                    }
//			                }
//			            }
//			            
//			            if(sendDetails.size() > 0){
//			                // Handle : Sending Else
//			                sendMsgToPhones(sendDetails);
//			            }
//			        }
//			        else {
//			            if(page == 0) {
//			                throw new BcsNoticeException("查不到發送目標");  
//			            }
//			            
//			            break;
//			        }
//			        
//			        page++;
//			    }
//			    
//			}
//			catch(Exception e){
//				logger.error(ErrorRecord.recordError(e));
//				
//				pnpMain.setStatus(PnpMainEvery8d.MSG_SENDER_STATUS_FAIL);
//				pnpMain.setStatusNotice(e.getMessage());
//				pnpMainService.save(pnpMain);
//				
//				if(AbstractPnpMainEntity.SEND_TYPE_DELAY.equals(pnpMain.getSendType())) {
//				    CircleSchedulerService schedulerService = ApplicationContextProvider.getApplicationContext().getBean(CircleSchedulerService.class);
//		            boolean status = schedulerService.deletePnpSendSchedule(pnpMainId);
//		            logger.error("Schdeuler deletePnpSendSchedule:" + pnpMainId + " - status - " + status);
//				}
//			}
//		}
//		else{
//			logger.error("Schdeuler pnpMainId:" + pnpMainId + " Missing");
//			// Remove Scheduler
//			CircleSchedulerService schedulerService = ApplicationContextProvider.getApplicationContext().getBean(CircleSchedulerService.class);
//			boolean status = schedulerService.deletePnpSendSchedule(pnpMainId);
//			logger.error("Schdeuler deletePnpSendSchedule:" + pnpMainId + " - status - " + status);
//		}
	}
	
}
