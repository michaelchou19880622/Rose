package com.bcs.core.taishin.circle.PNP.db.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
//import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailRepository;
//import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainRepository;
import com.bcs.core.utils.ErrorRecord;

@Service
public class PnpMainService {
	private static final String INIT_FLAG = "INIT_FLAG";
	private static final String twFormatPre = "+886";
	
	/** Logger */
	private static Logger logger = Logger.getLogger(PnpMainService.class);
	
	
	
//	
//	@Autowired
//	private PnpMainRepository pnpMainRepository;
	
//	@Autowired
//    private PnpDetailRepository pnpDetailRepository;

//	private Timer flushTimer = new Timer();
//	
//	private ConcurrentMap<Long, AtomicLong> increaseMap = new ConcurrentHashMap<Long, AtomicLong>();
//
//    @PersistenceContext
//    EntityManager entityManager;
//    
//    public PnpMainService(){
//
//		flushTimer.schedule(new CustomTask(), 120000, 30000);
//    }
//
//	private class CustomTask extends TimerTask{
//		
//		@Override
//		public void run() {
//
//			try{
//				flushIncrease();
//			}
//			catch(Throwable e){
//				logger.error(ErrorRecord.recordError(e));
//			}
//		}
//	}
//	
//	@PreDestroy
//	public void preDestroy(){
//		flushTimer.cancel();
//		logger.info("[DESTROY] MsgSendMainService flushTimer destroyed");
//	}
//	
//	public PnpMain findOne(Long pnpMainId){
//		return pnpMainRepository.findOne(pnpMainId);
//	}
//	
//	public List<PnpMain> findAll(){
//	    Sort sort = new Sort(Sort.Direction.DESC, "pnpMainId");
//	    return pnpMainRepository.findAll(sort);
//	}
//
//	public void save(PnpMain main){
//		pnpMainRepository.save(main);
//	}
//
//	public void increaseSendCountByPnpMainId(Long pnpMainId){
//		synchronized (INIT_FLAG) {
//			if(increaseMap.get(pnpMainId) == null){
//				increaseMap.put(pnpMainId, new AtomicLong(1L));
//			}
//			else{
//				increaseMap.get(pnpMainId).addAndGet(1);
//			}
//		}
//	}
//	
//	private void increaseSendCountByPnpMainIdAndCheck(Long pnpMainId, Long increase){
//		pnpMainRepository.increaseSendCountByPnpMainIdAndCheck(pnpMainId, increase);
//	}
//
//	public void increaseSendCountByPnpMainId(Long pnpMainId, Long increase){
//		synchronized (INIT_FLAG) {
//			if(increaseMap.get(pnpMainId) == null){
//				increaseMap.put(pnpMainId, new AtomicLong(increase));
//			}
//			else{
//				increaseMap.get(pnpMainId).addAndGet(increase);
//			}
//		}
//	}
//
//	public void increaseSendCountByPnpMainId(Long pnpMainId, int increase){
//		synchronized (INIT_FLAG) {
//			if(increaseMap.get(pnpMainId) == null){
//				increaseMap.put(pnpMainId, new AtomicLong(increase));
//			}
//			else{
//				increaseMap.get(pnpMainId).addAndGet(increase);
//			}
//		}
//	}
//
//	public void flushIncrease(){
//		synchronized (INIT_FLAG) {
//			logger.debug("PnpMainService flushTimer execute");
//			for(Map.Entry<Long, AtomicLong> map : increaseMap.entrySet()){
//				if(map.getValue().longValue() != 0){
//					logger.debug("PnpMainService flushTimer execute:" + map.getKey() + "," + map.getValue().longValue());
//					this.increaseSendCountByPnpMainIdAndCheck(map.getKey(), map.getValue().longValue());
//					map.getValue().set(0);
//				}
//			}
//			logger.debug("PnpMainService flushTimer end");
//		}
//	}
//	
//	public PnpMain createData(List<String> datas, String fileName) throws Exception {
//	    
//	    PnpMain main = new PnpMain();
//	    try {
//	        for(int i = 0; i < datas.size(); i++) {
//	            
//	            if(i == 0) {
//	                String[] dataArr = datas.get(i).split("&");
//
//	                main.setTitle(fileName);
//	                main.setTotalCount((long)(datas.size()-1));
//	                main.setSendCount(0L);
//	                
//	                if(StringUtils.isNotBlank(dataArr[3])) {
//	                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
//
//	                    main.setSendType(AbstractPnpMainEntity.SEND_TYPE_DELAY);
//	                    main.setScheduleTime(sdf.parse(dataArr[3])); 
//	                }
//	                
//	                if(StringUtils.isBlank(main.getSendType())) {
//	                    main.setSendType(AbstractPnpMainEntity.SEND_TYPE_IMMEDIATE);
//	                }
//	                
//	                pnpMainRepository.save(main);
//	            }
//	            else {
//	                
//	                String[] dataArr = datas.get(i).split("&");
//	                
//	                String phone = toTwFormat(dataArr[2]);
//	                
//	                if(StringUtils.isNotBlank(phone)) {
//	                    PnpDetail detail = new PnpDetail();
//	                    detail.setPnpMainId(main.getPnpMainId());
//	                    detail.setPhone(phone);
//	                    detail.setPhoneHash(toSHA256(phone));
//	                    detail.setMsg(dataArr[3].replace("\u0006", "\r\n"));
//	                    
//	                    pnpDetailRepository.save(detail);
//	                }
//	            }
//	        }
//
//	        return main;
//	    }
//	    catch (Exception e) {
//	        main.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_FAIL);
//	        main.setStatusNotice("create data fail");
//	        main.setTotalCount(0L);
//	        pnpMainRepository.save(main);
//	        
//	        pnpDetailRepository.deleteByPnpMainId(main.getPnpMainId());
//	        
//	        throw e;
//        }
//	}
	
//	private String toTwFormat(String originalPhone) {
//	    
//	    if(originalPhone.startsWith(twFormatPre)) {
//            return originalPhone;
//        }
//        else if(originalPhone.startsWith("0")) {
//            return twFormatPre + originalPhone.substring(1);   
//        } 
//        else {
//            return null;
//        }
//	}
	
	public String toSHA256(String phone) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        phone = "+886"+phone.substring(1);//改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
        byte[] hash = digest.digest(phone.getBytes(StandardCharsets.UTF_8));

        StringBuffer hexString = new StringBuffer();

        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }
	
}
