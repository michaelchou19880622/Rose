package com.bcs.core.taishin.circle.pnp.scheduler;

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.pnp.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMain;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpRepositoryCustom;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 循環執行以Line push發送訊息
 * <p>
 * push成功:更新狀態成功
 * push失敗:跟據使用者設定的發送通路參數更新狀態為轉寄PNP或轉寄SMS或更新狀態失敗
 *
 * @author Kenneth
 * @author Alan
 */
@Slf4j(topic = "PnpRecorder")
@Service
public class PnpPushMsgService {

    private PnpAkkaService pnpAkkaService;
    private PnpRepositoryCustom pnpRepositoryCustom;
    private LineUserService lineUserService;
    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("PNP-BC-Scheduled-%d")
                    .daemon(true).build()
    );

    @Autowired
    public PnpPushMsgService(PnpAkkaService pnpAkkaService, PnpRepositoryCustom pnpRepositoryCustom, LineUserService lineUserService) {
        this.pnpAkkaService = pnpAkkaService;
        this.pnpRepositoryCustom = pnpRepositoryCustom;
        this.lineUserService = lineUserService;
    }

    /**
     * Start Schedule
     */
    public void startCircle() {
        scheduler.scheduleWithFixedDelay(this::sendProcess, 0, getTimeValue(), getTimeUnit());
    }

    private int getTimeValue() {
        int time = CoreConfigReader.getInteger(CONFIG_STR.PNP_SEND_SCHEDULE_TIME, true, false);
        if (time <= 0) {
            log.warn("Properties [pnp.send.schedule.time] does not found, use default value 30s!!");
            time = 30;
        }
        return time;
    }

    private TimeUnit getTimeUnit() {
        try {
            return TimeUnit.valueOf(CoreConfigReader.getString(CONFIG_STR.PNP_SCHEDULE_UNIT, true, false));
        } catch (IllegalArgumentException e) {
            log.warn("Properties [pnp.schedule.unit] does not found or illegal value, use default value second!!");
            return TimeUnit.SECONDS;
        }
    }

    /**
     * 1. Find all main with status is wait.
     * 2. Find all detail by main id.
     * 3. Use phone number find
     * 4. Tell akka
     */
    private void sendProcess() {
        try {
	        /* pnp.big switch = 0(停止排程) 1(停止排程，並轉發SMS)  2(正常運行) , -1(系統異常)*/        	
	        int bigSwitch = Integer.parseInt(CoreConfigReader.getString(CONFIG_STR.PNP_BIG_SWITCH, true, false));
	        if (bigSwitch == 0 || bigSwitch == 1) {
	            log.warn("Stop sending!!, BigSwitch is : {}", bigSwitch);
	            return;
	        }
	        else if (bigSwitch == 2) {
	            String procApName = DataUtils.getProcApName();
	            for (PnpFtpSourceEnum type : PnpFtpSourceEnum.values()) {
	                log.info("ftp source is {}, ap_name: {}", type.english, procApName);
	                try {
	                    /* 1.Find all main */
	                    List<PnpMain> allMainList = pnpRepositoryCustom.findAllMain(procApName, type);
	                    if (allMainList.isEmpty()) {
	                        log.info("allmainList: {} is empty", type.english);
	                        continue;
	                    }
	                    log.info("detailTable: {} forEach operation", type.english);
	                    allMainList.forEach(main -> {
	                        /* 2.Find all detail by main id */
//                          int detailCount = pnpRepositoryCustom.getDetailCountByMainId(type, main.getPnpMainId());
	                        List<PnpDetail> allDetailList = pnpRepositoryCustom.findAllDetail(main.getPnpMainId(), type);
	                        int detailCount = allDetailList.size();
	                        log.info(String.format("detail count: %s, detail table: %s,  main id: %s", detailCount, type.detailTable,  main.getPnpMainId().toString()));
	                        
	                        if (detailCount > 0) {
	    	                    List<PnpDetail> allDetailListAndUpdate = pnpRepositoryCustom.findAllDetailAndUpdateStatus(main.getPnpMainId(), type);
	    	                    /* 3.Use phone find detail uid */
	    	                    List<PnpDetail> filterDetailList = usePhoneFindDetailUid(allDetailListAndUpdate).stream()
	    	                            .filter(detail -> Objects.equals((detail).getStatus(), PnpStatusEnum.SENDING.value))
	    	                            .sorted(Comparator.comparing(PnpDetail::getPnpDetailId))
	    	                            .collect(Collectors.toList());
	    	                    
	    	                    List<PnpDetail> filterDetailCompleteList = usePhoneFindDetailUid(allDetailList).stream()
	    	                            .filter(detail -> Objects.equals((detail).getStatus(), PnpStatusEnum.COMPLETE.value))
	                                    .sorted(Comparator.comparing(PnpDetail::getPnpDetailId))
	    	                            .collect(Collectors.toList());
	
	                            List<PnpDetail> filterDetailExpiredList = usePhoneFindDetailUid(allDetailList).stream()
	                                    .filter(detail -> Objects.equals((detail).getStatus(), PnpStatusEnum.EXPIRED.value))
	                                    .sorted(Comparator.comparing(PnpDetail::getPnpDetailId))
	                                    .collect(Collectors.toList());
	
	                            filterDetailCompleteList.addAll(filterDetailExpiredList);
	    	                    
//    	                    	for (PnpDetail detail : filterDetailList) {
//    		                    	log.info(String.format("Filter and Update Detail id: %s, detail status: %s, main id: %s",  detail.getPnpDetailId(), detail.getStatus(), main.getPnpMainId()));
//    	                    	}
//    	                    	for (PnpDetail detail : allDetailList) {
//    		                    	log.info(String.format("Filter Complete Detail id: %s, detail status: %s, main id: %s",  detail.getPnpDetailId(), detail.getStatus(), main.getPnpMainId()));
//    	                    	}
	    	                    allDetailList.clear();
	    	                    
	    	                    log.info("Main id and after filter detail list size, main id: {}, after filter detail size: {}", main.getPnpMainId(), filterDetailList.size());
	    	                    // log.info("After filter detail list: {}", DataUtils.toPrettyJsonUseJackson(filterDetailList));
	    	                    
	    	                    /* If detail list is empty not include any sending and detail from db is all sent  */
	    	                    if (detailCount == filterDetailCompleteList.size()) {
	    	                        /* all detail sent to bc and pnp is finish, Update main status to complete, but not include sms */
	    	                        log.info("Before update main id: {} to complete!!", main.getPnpMainId());
	    	                        pnpRepositoryCustom.updateMainToComplete(main.getPnpMainId(), type, PnpStatusEnum.COMPLETE);
	    	                    } 
	    	                    else if (filterDetailList.size() > 0){
	    	                        main.setPnpDetails(filterDetailList);
	    	                        /* 4.Tell akka */
	    	                        log.info("Tell Akka Send BC, main id: {}",  main.getPnpMainId());
	    	                        pnpAkkaService.tell(main);
	    	                    }
	    	                    else {
	    	                        log.info("Do Nothing, main id: {}", main.getPnpMainId());	                    	
	    	                    }
	                        }
	                        else {
	    	                    log.info("AllDetailList size is empty in main id : {}", main.getPnpMainId());
	                        }
	                        	
	                    });
	                } catch (Exception e) {
	                    log.error("PnpPushMsgService sendProcess error:" + e + ", errorMessage: " + e.getMessage());
	                }
	            }	            
	        }   
	        else if (bigSwitch == -1) {
	            log.warn("Can't Load PNP_BIG_SWITCH!");
	            return;
	        }
	        else  {
	            log.warn("BigSwitch is is not defined: {}", bigSwitch);  	
	            return;
	        }	            	        
        } catch (Exception e) {
            log.error("PnpPushMsgService sendProcess error:" + e + ", errorMessage: " + e.getMessage());
        }
    }

    /**
     * @param details details
     * @return XXXPnpDetail
     */
    private List<PnpDetail> usePhoneFindDetailUid(List<PnpDetail> details) {
        List<String> phoneNumberList = addAllFormatPhoneNumberToList(details);
        log.debug("phoneNumberList : {}", DataUtils.toPrettyJsonUseJackson(phoneNumberList));

        /* Return Original Object */
        if (CollectionUtils.isEmpty(phoneNumberList)) {
            return details;
        }

        /* 透過電話號碼清單查尋UID */
        List<LineUser> lineUserList = lineUserService.findByMobileIn(phoneNumberList);
        log.debug("LineUserList:{}", DataUtils.toPrettyJsonUseJackson(lineUserList));
        Map<String, String> uidPhoneNumberMap = generatePhoneNumberUidMapWithoutBlock(lineUserList);
        Map<String, String> phoneNumberStatusMap = generatePhoneNumberStatusMap(lineUserList);
        log.debug("uidPhoneNumberMap   :{}", DataUtils.toPrettyJsonUseJackson(uidPhoneNumberMap));
        log.debug("phoneNumberStatusMap:{}", DataUtils.toPrettyJsonUseJackson(phoneNumberStatusMap));
        for (int i = 0; i < details.size(); i++) {
            PnpDetail detail = details.get(i);
            log.debug("Phone Number : {}", detail.getPhone());
            if (detail.getUid() == null || detail.getUid().trim().isEmpty()) {
                detail.setUid(getUidByPhoneNumberMap(uidPhoneNumberMap, detail.getPhone()));
            }
            detail.setBindStatus(getBindStatusByPhoneNumber(phoneNumberStatusMap, detail.getPhone()));
            details.set(i, detail);
        }
        return ObjectUtils.clone(details);
    }

    private Map<String, String> generatePhoneNumberStatusMap(List<LineUser> lineUserList) {
        Map<String, String> map = new HashMap<>(lineUserList.size());
        for (LineUser lineUser : lineUserList) {
            map.put(lineUser.getMobile(), lineUser.getStatus());
        }
        return map;
    }

    /**
     * Set Uid By Phone Number Map
     *
     * @param phoneNumberStatusMap phoneNumberStatusMap
     * @param phoneNumber          phoneNumber
     * @return UID
     */
    private String getBindStatusByPhoneNumber(Map<String, String> phoneNumberStatusMap, String phoneNumber) {
        String phoneE164 = formatPhoneNumberToE164(phoneNumber);
        return phoneNumberStatusMap.containsKey(phoneE164) ? phoneNumberStatusMap.get(phoneE164) : phoneNumberStatusMap.get(phoneNumber);
    }

    /**
     * Set Uid By Phone Number Map
     *
     * @param uidPhoneNumberMap uidPhoneNumberMap
     * @param phoneNumber       phoneNumber
     * @return UID
     */
    private String getUidByPhoneNumberMap(Map<String, String> uidPhoneNumberMap, String phoneNumber) {
        String phoneE164 = formatPhoneNumberToE164(phoneNumber);
        return uidPhoneNumberMap.containsKey(phoneE164) ? uidPhoneNumberMap.get(phoneE164) : uidPhoneNumberMap.get(phoneNumber);
    }

    /**
     * 製作電話號碼UID對應表
     *
     * @param lineUserList line User List
     * @return Map Key: Phone Number, Value: Uid
     */
    private Map<String, String> generatePhoneNumberUidMapWithoutBlock(List<LineUser> lineUserList) {
        Map<String, String> uidPhoneMap = new HashMap<>(lineUserList.size());
        for (LineUser lineUser : lineUserList) {
            if (LineUser.STATUS_BLOCK.equals(lineUser.getStatus())) {
                continue;
            }
            uidPhoneMap.put(lineUser.getMobile(), lineUser.getMid());
        }
        return uidPhoneMap;
    }

    /**
     * 加入所有格式電話號碼
     *
     * @param details details
     * @return All Format Phone Number List
     */
    private List<String> addAllFormatPhoneNumberToList(List<? super PnpDetail> details) {
        List<String> phoneNumberList = new ArrayList<>();
        for (Object detail : details) {
            String phone = ((PnpDetail) detail).getPhone();
            phoneNumberList.add(phone);
            phone = formatPhoneNumberToE164(phone);
            phoneNumberList.add(phone);
        }
        return phoneNumberList;
    }

    /**
     * 格式化電話號碼為E.164國際電信組織制定的通信編碼格式
     * [國碼]+[區碼]+[電話號碼]
     *
     * @param phoneNumber phone number
     * @return newPhoneNumber
     */
    private String formatPhoneNumberToE164(String phoneNumber) {
        String newPhoneNumber = "";
        if ("0".equals(phoneNumber.substring(0, 1))) {
            newPhoneNumber = "+886" + phoneNumber.substring(1);
        } else if ("+".equals(phoneNumber.substring(0, 1))) {
            newPhoneNumber = "0" + phoneNumber.substring(4);
        }
        return newPhoneNumber;
    }


    /**
     * Stop Schedule : Wait for Executing Jobs to Finish
     */
    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            log.debug("Shutdown....");
            scheduler.shutdown();
        }
    }
}
