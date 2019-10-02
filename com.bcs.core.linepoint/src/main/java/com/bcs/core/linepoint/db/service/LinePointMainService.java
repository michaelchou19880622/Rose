package com.bcs.core.linepoint.db.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.repository.LinePointMainRepository;

@Service
public class LinePointMainService {
	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointMainService.class);
	@Autowired
	private LinePointMainRepository linePointMainRepository;

    @PersistenceContext
    EntityManager entityManager;
    
	public void delete(LinePointMain linePointMain){
		linePointMainRepository.delete(linePointMain);
//		LinePointMain main = linePointMainRepository.findOne(msgId);
//		main.setModifyTime(new Date());
//		main.setStatus(LinePointMain.MESSAGE_STATUS_DELETE);
//		this.save(main);
	}
    
	public void save(LinePointMain linePointMain){
		linePointMainRepository.save(linePointMain);
	}
    
	public LinePointMain findOne(Long msgId){
		return linePointMainRepository.findOne(msgId);
	}
	
	public List<LinePointMain> findAll(){
		return linePointMainRepository.findAll();
	}
 
	public List<LinePointMain> findAll(String searchText){
		return linePointMainRepository.findAll(searchText);
	}
	public List<LinePointMain> findBySendType(String sendType){
		return linePointMainRepository.findBySendType(sendType);
	}
	public List<LinePointMain> findBySendTypeAndDate(String sendType, Date startDate, Date endDate){
		return linePointMainRepository.findBySendTypeAndDate(sendType, startDate, endDate);
	}	
	public List<LinePointMain> findAllowableIdles(){
		return linePointMainRepository.findAllowableIdles();
	}
	
	public List<LinePointMain> findByTitleAndModifyUserAndDate(Date startDate, Date endDate, String modifyUser, String title){
		return linePointMainRepository.findByTitleAndModifyUserAndDate(title, modifyUser, startDate, endDate);
	}
	@SuppressWarnings("unchecked")
	public List<LinePointMain> getLinePointStatisticsReport(Date startDate, Date endDate, String modifyUser, String title, Integer page){
    	// get Row Index
		Integer rowStart, rowEnd;
    	if(page == null) {
    		rowStart = 1;
    		rowEnd = Integer.MAX_VALUE; // get all data
    	}else {
    		page--; // 1~199 => 0~198
    		rowStart = page * 10 ;
    		rowEnd = rowStart + 10; // 10 as Size
    	}
    	logger.info("rowStart:"+rowStart);
    	logger.info("rowEnd:"+rowEnd);
    	
    	Query query = entityManager.createNamedQuery("queryGetStatisticsReportPage").setParameter(1, title).setParameter(2, modifyUser)
    			.setParameter(3, startDate).setParameter(4, endDate);
    	query.setFirstResult(rowStart);
    	query.setMaxResults(rowEnd);
    	
		return query.getResultList();
	}
	
	public Long getLinePointStatisticsReportTotalPages(Date startDate, Date endDate, String modifyUser, String title){
		return linePointMainRepository.findTotalCountByTitleAndModifyUserAndStartDateAndEndDate(title, modifyUser, startDate, endDate);
	}
//	public List<LinePointMain> findManual(){
//		return linePointMainRepository.findBySendType(LinePointMain.SEND_TYPE_MANUAL);
//	}
//	
//	public List<LinePointMain> findManual(String searchText){
//		return linePointMainRepository.findBySendType(LinePointMain.SEND_TYPE_MANUAL, searchText);
//	}
	
//	public List<LinePointMain> findAuto(){
//		return linePointMainRepository.findBySendType(LinePointMain.SEND_TYPE_AUTO);
//	}
//	
//	public List<LinePointMain> findAuto(String searchText){
//		return linePointMainRepository.findBySendType(LinePointMain.SEND_TYPE_AUTO, searchText);
//	}
//	
//	public List<LinePointMain> findUndoneManual(){
//		return linePointMainRepository.findUndoneBySendType(LinePointMain.SEND_TYPE_MANUAL);
//	}
//
//	public List<LinePointMain> findUndoneAuto(){
//		return linePointMainRepository.findUndoneBySendType(LinePointMain.SEND_TYPE_AUTO);
//	}
	
	public LinePointMain findBySerialId(String serialId){
		return linePointMainRepository.findBySerialId(serialId);
	}
	
	public LinePointMain findByTitle(String title){
		return linePointMainRepository.findByTitle(title);
	}
	
	public List<LinePointMain> findByStatus(String status){
		return linePointMainRepository.findByStatus(status);
	}
	
	
//	@SuppressWarnings("unchecked")
//	public Map<LinePointSendMain, List<LinePointSend>> queryGetLinePointSendMainAll(){
//		Query query = entityManager.createNamedQuery("queryLinePointSendMainDetailAll");
//		List<Object[]> list = query.getResultList();
//		
//		Map<LinePointSendMain, List<LinePointSend>> map = parseListToMap(list);
//    	logger.debug(map);
//		
//		return map;
//	}
//	
//	private Map<LinePointSendMain, List<LinePointSend>> parseListToMap(List<Object[]> list){
//
//		Map<LinePointSendMain, List<LinePointSend>> map = new LinkedHashMap<LinePointSendMain, List<LinePointSend>>();
//
//	    for(Object[] o : list){
//	    	logger.debug("length:" + o.length);
//	    	logger.debug(o[0]);
//	    	if(o[0] !=null){
//	    		List<LinePointSend> details = map.get(o[0]);
//	    		if(details == null){
//	    			map.put((LinePointSendMain) o[0], new ArrayList<LinePointSend>());
//	    		}
//	    	}
//	    	logger.debug(o[1]);
//	    	if(o[1] != null){
//	    		List<LinePointSend> details = map.get(o[0]);
//	    		details.add((LinePointSend) o[1]);
//	    	}
//	    }
//	    
//	    return map;
//	}	
	
}
