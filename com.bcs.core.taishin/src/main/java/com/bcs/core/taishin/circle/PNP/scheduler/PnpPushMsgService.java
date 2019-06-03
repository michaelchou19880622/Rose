package com.bcs.core.taishin.circle.PNP.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.service.LineUserService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFTPType;

/**
 * 循環執行以Line push發送訊息
 * 
 * push成功:更新狀態成功
 * push失敗:跟據使用者設定的發送通路參數更新狀態為轉寄PNP或轉寄SMS或更新狀態失敗
 * 
 * @author Kenneth
 *
 */
@Service
public class PnpPushMsgService {

	/** Logger */
	private static Logger logger = Logger.getLogger(PnpPushMsgService.class);
//	@Autowired
//	private PnpService pnpService;
	@Autowired
	private PnpAkkaService pnpAkkaService;
	@Autowired
	private PnpRepositoryCustom pnpRepositoryCustom;
	@Autowired
	private LineUserService lineUserService;
	
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> scheduledFuture = null;

	public PnpPushMsgService() {
	}

	/**
	 * Start Schedule
	 * 
	 * @throws SchedulerException
	 * @throws InterruptedException
	 */
	public void startCircle() throws SchedulerException, InterruptedException {

		String unit = CoreConfigReader.getString(CONFIG_STR.PNP_SCHEDULE_UNIT, true, false);
		int time = CoreConfigReader.getInteger(CONFIG_STR.PNP_SCHEDULE_TIME, true, false);
		if (time == -1 || TimeUnit.valueOf(unit) == null) {
			logger.error(" PNPSendMsgService TimeUnit error :" + time  + unit);
			return;
		}
		scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				// 排程工作
				logger.debug(" PnpSendMsgService startCircle....");
				
				//#.pnp.bigswitch = 0(停止排程) 1(停止排程，並轉發SMS) 其他(正常運行)
				int bigSwitch = CoreConfigReader.getInteger(CONFIG_STR.PNP_BIGSWITCH, true, false);
				if (1==bigSwitch || 0==bigSwitch) { //大流程關閉時不做
					logger.warn("PNP_BIGSWITCH : "+bigSwitch +"PnpPushMsgService stop sendding...");
					return;
				}
				
				sendingMain();
				
			}
		}, 0, time, TimeUnit.valueOf(unit));

	}
	
	/**
	 * 根據PNPFTPType 依序Push 
	 */
	public void sendingMain(){
		String procApName = pnpAkkaService.getProcApName();
		for (PNPFTPType type : PNPFTPType.values()) {
			try {
				Set<Long>  allMainIds = new  HashSet<Long>(); 
				List<? super PnpDetail>  details = pnpRepositoryCustom.updateStatusByStageBC(type, procApName, allMainIds);
				if (details.isEmpty()) {
					logger.info("details not data type:" + type.toString());
				}else {
					details = findDetailUid(details);
					Long[] mainIds = allMainIds.toArray(new Long[allMainIds.size()]);
					PnpMain pnpMain = pnpRepositoryCustom.findMainByMainId(type, mainIds[0]);
					logger.info("sending handle Main:" + pnpMain.getOrigFileName() + " type:" + type);
					pnpMain.setPnpDetails(details);
					pnpAkkaService.tell(pnpMain);
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(" pnpMain type :"+ type  +" sendingMain error:" + e.getMessage());
			}
			
		}
		
	}

	private List<? super PnpDetail> findDetailUid(List<? super PnpDetail> details) {
		if(CollectionUtils.isEmpty(details)) { 
			logger.error("PnpPushMsgService.findDetailUid details is empty!!!");
			return null;
		}
		
		//用電話查UID 此段可以考慮搬到parse data flow 現階段考慮parse效能故在發送前做
		List<String> mobilesList = new ArrayList<>();
		
		for(Object detail:details){ //e.164格式 及 09XX格式都查
			String phone = ((PnpDetail) detail).getPhone();
			mobilesList.add(phone);
	        if("0".equals(phone.substring(0, 1))){//phone有些可能已轉成e.164格式，0開頭的再做轉換
	        	phone = "+886"+phone.substring(1);//改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
	        	mobilesList.add(phone);
	        }else if("+".equals(phone.substring(0, 1))){
	        	phone = "0"+phone.substring(4);//改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
	        	mobilesList.add(phone);
	        }
		}
		
		//TODO 確認電話號碼格式
		if(CollectionUtils.isNotEmpty(mobilesList)){
			List<Object[]> midPhoneList = lineUserService.findMidsByMobileIn(mobilesList);
			
			Map<String,String> midPhoneMap = new HashMap<>();
			for(Object[] midPhone : midPhoneList){
				midPhoneMap.put((String) midPhone[0], (String) midPhone[1]);
			}
			
			for(int i = 0 ; i < details.size() ; i++){
				PnpDetail detail = (PnpDetail) details.get(i);
				
				String phone = detail.getPhone();
				String phoneFormatB ="";
				if("0".equals(phone.substring(0, 1))){//phone有些可能已轉成e.164格式，0開頭的再做轉換
					phoneFormatB = "+886"+phone.substring(1);//改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
		        }else if("+".equals(phone.substring(0, 1))){
		        	phoneFormatB = "0"+phone.substring(4);//改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
		        }
				
				if(midPhoneMap.containsKey(phoneFormatB)) {
					detail.setUid(midPhoneMap.get(phoneFormatB));
				}else {
					detail.setUid(midPhoneMap.get(phone));
				}
				
				details.set(i, detail);
			}
			
			
//			for(PnpDetail detail:details){ 
//				if(midPhoneMap.containsKey(detail.getPhone())){
//					detail.setUid(midPhoneMap.get(detail.getPhone()));
//				}
//			}
		}
		
		return details;
	}
	

	/**
	 * Stop Schedule : Wait for Executing Jobs to Finish
	 * 
	 * @throws SchedulerException
	 */
	@PreDestroy
	public void destroy() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(true);
			logger.info(" PnpPushMsgService cancel....");
		}

		if (scheduler != null && !scheduler.isShutdown()) {
			logger.info(" PnpPushMsgService shutdown....");
			scheduler.shutdown();
		}

	}

}
