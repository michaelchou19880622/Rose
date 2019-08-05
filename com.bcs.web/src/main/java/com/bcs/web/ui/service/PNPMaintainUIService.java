package com.bcs.web.ui.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Campaign;
import com.bcs.core.db.repository.CampaignRepository;
import com.bcs.core.db.service.CampaignService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelCustom;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelRepository;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class PNPMaintainUIService {

	/** Logger */
	private static Logger logger = Logger.getLogger(PNPMaintainUIService.class);
	@Autowired
	private PNPMaintainAccountModelRepository pnpMaintainAccountModelRepository;    
	@Autowired
    private PNPMaintainAccountModelCustom PNPMaintainAccountModelCustom;
	@Autowired
	private OracleService oraclePnpService;
	@PersistenceContext
    EntityManager entityManager;
	
	public void save(PNPMaintainAccountModel pnpMaintainAccountModel) {
		pnpMaintainAccountModelRepository.save(pnpMaintainAccountModel);
	}

	public void delete(PNPMaintainAccountModel pnpMaintainAccountModel) {
		pnpMaintainAccountModelRepository.delete(pnpMaintainAccountModel);
	}
	
	public PNPMaintainAccountModel findOne(Long id) {
		return pnpMaintainAccountModelRepository.findOne(id);
	}
	
	public List<PNPMaintainAccountModel> findByDivisionNameAndDepartmentNameAndGroupNameAndPccCodeAndAccountAndEmployeeIdAndAccountType(
			String divisionName, String departmentName, String groupName, String pccCode, String account, String employeeId, String accountType){
		return pnpMaintainAccountModelRepository.findByDivisionNameAndDepartmentNameAndGroupNameAndPccCodeAndAccountAndEmployeeIdAndAccountType(
				divisionName, departmentName, groupName, pccCode, account, employeeId, accountType);
	}
	public List<PNPMaintainAccountModel> queryUsePageCoditions(
			String divisionName, String departmentName, String groupName, String pccCode, String account, String employeeId, String accountType, Boolean status){
		return PNPMaintainAccountModelCustom.queryUseConditions(divisionName, departmentName, groupName, pccCode, account, employeeId, accountType, status);
	}
	
	public List<PNPMaintainAccountModel> findByAccountAndSourceSystemAndPnpContent(String account, String sourceSystem, String pnpContent){
		return pnpMaintainAccountModelRepository.findByAccountAndSourceSystemAndPnpContent(account, sourceSystem, pnpContent);
	}
   
	public List<PNPMaintainAccountModel> findByAccountType(String accountType){
		return pnpMaintainAccountModelRepository.findByAccountType(accountType);
	}
	
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getPNPDetailReport(String startDate, String endDate, String account, String pccCode, String sourceSystem, Integer page, String empId){
    	Integer rowStart, rowEnd;
    	if(page == null) {
    		rowStart = 1;
    		rowEnd = Integer.MAX_VALUE; // get all data
    	}else {
    		page--; // 1~199 => 0~198
    		rowStart = page * 10 + 1;
    		rowEnd = rowStart + 10; // 10 as Size
    	}
    	
    	String queryString = 
			"SELECT * FROM ( "
			+"SELECT ORIG_FILE_NAME, PROC_FLOW, SOURCE, MSG, PHONE, PNP_DELIVERY_EXPIRE_TIME, DETAIL_SCHEDULE_TIME, CREAT_TIME, STATUS, PCC_CODE, ACCOUNT, SOURCE_SYSTEM, EMPLOYEE_ID, "
			+"ROW_NUMBER() OVER ( ORDER BY CREAT_TIME desc) AS RowNum "
			+"FROM ( "
			+"SELECT M.ORIG_FILE_NAME, D.PROC_FLOW, D.SOURCE, MSG, PHONE, D.PNP_DELIVERY_EXPIRE_TIME, D.DETAIL_SCHEDULE_TIME, D.CREAT_TIME, D.STATUS, A.PCC_CODE, A.ACCOUNT, A.SOURCE_SYSTEM, A.EMPLOYEE_ID "
			+"FROM BCS_PNP_DETAIL_MING AS D LEFT JOIN BCS_PNP_MAIN_MING AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID "
            +"LEFT JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID "
			+"UNION "
			+"SELECT M.ORIG_FILE_NAME, D.PROC_FLOW, D.SOURCE, MSG, PHONE, D.PNP_DELIVERY_EXPIRE_TIME, NULL, D.CREAT_TIME, D.STATUS, A.PCC_CODE, A.ACCOUNT, A.SOURCE_SYSTEM, A.EMPLOYEE_ID "
			+"FROM BCS_PNP_DETAIL_MITAKE AS D LEFT JOIN BCS_PNP_MAIN_MITAKE AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID "
            +"LEFT JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID "
            +"UNION "
			+"SELECT M.ORIG_FILE_NAME, D.PROC_FLOW, D.SOURCE, MSG, PHONE, D.PNP_DELIVERY_EXPIRE_TIME, NULL, D.CREAT_TIME, D.STATUS, A.PCC_CODE, A.ACCOUNT, A.SOURCE_SYSTEM, A.EMPLOYEE_ID "
			+"FROM BCS_PNP_DETAIL_UNICA AS D LEFT JOIN BCS_PNP_MAIN_UNICA AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID "
            +"LEFT JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID "
			+"UNION "
			+"SELECT M.ORIG_FILE_NAME, D.PROC_FLOW, D.SOURCE, MSG, PHONE, D.PNP_DELIVERY_EXPIRE_TIME, NULL, D.CREAT_TIME, D.STATUS, A.PCC_CODE, A.ACCOUNT, A.SOURCE_SYSTEM, A.EMPLOYEE_ID "
			+"FROM BCS_PNP_DETAIL_EVERY8D AS D LEFT JOIN BCS_PNP_MAIN_EVERY8D AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID "
            +"LEFT JOIN BCS_PNP_MAINTAIN_ACCOUNT AS A ON M.PNP_MAINTAIN_ACCOUNT_ID = A.ID "
			+") AS R1 "
			+"WHERE CREAT_TIME >= ?1 "
			+"AND CREAT_TIME <  DATEADD(DAY, 1, ?2) ";
    	
		if(StringUtils.isNotBlank(account)) queryString += "AND ACCOUNT = '" + account + "' ";
		if(StringUtils.isNotBlank(pccCode)) queryString += "AND PCC_CODE = '" + pccCode + "' ";
		if(StringUtils.isNotBlank(sourceSystem)) queryString += "AND SOURCE_SYSTEM = '" + sourceSystem + "' ";
		String empAva = oraclePnpService.getAvailableEmpIdsByEmpId(empId);
		if(StringUtils.isNotBlank(empAva)) queryString += empAva;
    	
    	queryString	+=
    		") AS R2 "
			+"WHERE RowNum >= ?3 AND RowNum < ?4 ";

    	logger.info("str1: " + queryString);
    	logger.info("rowStart:"+rowStart);
    	logger.info("rowEnd:"+rowEnd);
    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, startDate).setParameter(2, endDate)
    			.setParameter(3, rowStart).setParameter(4, rowEnd);
    	logger.info("query:"+query.toString());
    	
		List<Object[]> list = query.getResultList();
    	//logger.info("List1: " + list.toString());
    		
    	Map<String, List<String>> map = new LinkedHashMap<>();
    	
    	Integer count = 0;
		for (Object[] o : list) {
			count++;
			logger.info("c:" + count);
			List<String> dataList = new ArrayList<String>();
			map.put(count.toString(), dataList);
			for (int i=0, max=10; i<max; i++) {
				if (o[i] == null) {
					dataList.add("");
					//logger.info("i=" + i  + ", null");
				} else {
					dataList.add(o[i].toString());
					//logger.info("i=" + i  + ", " + o[i].toString());
				}
			}
		}
    	logger.info("map1: " + map.toString());
    	
		return map;
    }

    @SuppressWarnings("unchecked")
    public String getPNPDetailReportTotalPages(String startDate, String endDate, String account, String pccCode, String sourceSystem, String empId){
    	String queryString = 
    		"SELECT COUNT(*) FROM ( "
			+"SELECT ORIG_FILE_NAME, PROC_FLOW, SOURCE, MSG, PHONE, PNP_DELIVERY_EXPIRE_TIME, DETAIL_SCHEDULE_TIME, CREAT_TIME, STATUS, "
			+"ROW_NUMBER() OVER ( ORDER BY CREAT_TIME desc) AS RowNum "
			+"FROM ( "
			+"SELECT M.ORIG_FILE_NAME, D.PROC_FLOW, D.SOURCE, MSG, PHONE, D.PNP_DELIVERY_EXPIRE_TIME, D.DETAIL_SCHEDULE_TIME, D.CREAT_TIME, D.STATUS "
			+"FROM BCS_PNP_DETAIL_MING AS D LEFT JOIN BCS_PNP_MAIN_MING AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID "
			+"UNION "
			+"SELECT M.ORIG_FILE_NAME, D.PROC_FLOW, D.SOURCE, MSG, PHONE, D.PNP_DELIVERY_EXPIRE_TIME, NULL, D.CREAT_TIME, D.STATUS "
			+"FROM BCS_PNP_DETAIL_MITAKE AS D LEFT JOIN BCS_PNP_MAIN_MITAKE AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID "
			+"UNION "
			+"SELECT M.ORIG_FILE_NAME, D.PROC_FLOW, D.SOURCE, MSG, PHONE, D.PNP_DELIVERY_EXPIRE_TIME, NULL, D.CREAT_TIME, D.STATUS "
			+"FROM BCS_PNP_DETAIL_UNICA AS D LEFT JOIN BCS_PNP_MAIN_UNICA AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID "
			+"UNION "
			+"SELECT M.ORIG_FILE_NAME, D.PROC_FLOW, D.SOURCE, MSG, PHONE, D.PNP_DELIVERY_EXPIRE_TIME, NULL, D.CREAT_TIME, D.STATUS "
			+"FROM BCS_PNP_DETAIL_EVERY8D AS D LEFT JOIN BCS_PNP_MAIN_EVERY8D AS M ON D.PNP_MAIN_ID = M.PNP_MAIN_ID "
			+") AS R1 "
			+"WHERE CREAT_TIME >= ?1 "
			+"AND CREAT_TIME <  DATEADD(DAY, 1, ?2) ";
    	
		if(StringUtils.isNotBlank(account)) queryString += "AND ACCOUNT = '" + account + "' ";
		if(StringUtils.isNotBlank(pccCode)) queryString += "AND PCC_CODE = '" + pccCode + "' ";
		if(StringUtils.isNotBlank(sourceSystem)) queryString += "AND SOURCE_SYSTEM = '" + sourceSystem + "' ";
		String empAva = oraclePnpService.getAvailableEmpIdsByEmpId(empId);
		if(StringUtils.isNotBlank(empAva)) queryString += empAva;
		
    	queryString	+=
    		") AS R2 ";
    	
    	logger.info("str1: " + queryString);
    	
    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, startDate).setParameter(2, endDate);
		List<Object[]> list = query.getResultList();
		String listStr = list.toString();
    	logger.info("List1:" + list.toString());
		
		// Total = Empty set,  []  => 0
		if(listStr.length() <= 2) return "0"; 
		
		// Total < 10
		char c1 = listStr.charAt(listStr.length() - 2); // 個位數
		if(listStr.length() == 3) return (c1=='0') ? "0" : "1"; // [0] => 0 , [1] => 1
		
		// Total >= 10
		if(c1 == '0') return listStr.substring(1, listStr.length() - 2); // [430] => 43
		char c10 = listStr.charAt(listStr.length() - 3); // 十位數
    	return listStr.substring(1, listStr.length() - 3) + (++c10); // [431] => 44
    }
    
//    protected LoadingCache<String, Campaign> dataCache;
//    
//	private Timer flushTimer = new Timer();
//	
//	private class CustomTask extends TimerTask{
//		
//		@Override
//		public void run() {
//
//			try{
//				// Check Data Sync
//				Boolean isReSyncData = DataSyncUtil.isReSyncData(CAMPAIGN_SYNC);
//				if(isReSyncData){
//					dataCache.invalidateAll();
//					DataSyncUtil.syncDataFinish(CAMPAIGN_SYNC);
//				}
//			}
//			catch(Throwable e){
//				logger.error(ErrorRecord.recordError(e));
//			}
//		}
//	}
//    
//    public CampaignService(){
//
//		flushTimer.schedule(new CustomTask(), 120000, 30000);
//
//        dataCache = CacheBuilder.newBuilder()
//                .concurrencyLevel(1)
//                .expireAfterAccess(30, TimeUnit.MINUTES)
//                .build(new CacheLoader<String, Campaign>() {
//                    @Override
//                    public Campaign load(String key) throws Exception {
//                        return new Campaign();
//                    }
//                });
//    }
//    
//    @PreDestroy
//    public void cleanUp() {
//        logger.info("[DESTROY] CampaignService cleaning up...");
//        try{
//            if(dataCache != null){
//                dataCache.invalidateAll();
//                dataCache = null;
//            }
//        }
//        catch(Throwable e){}
//        
//        System.gc();
//        logger.info("[DESTROY] CampaignService destroyed.");
//    }
//    
//    private boolean notNull(Campaign result){
//        if(result != null && StringUtils.isNotBlank(result.getCampaignId())){
//            return true;
//        }
//        return false;
//    }
//    
//    public Campaign findByName(String name) {
//        Campaign result = campaignRepository.findByCampaignName(name);
//        if(result != null){
//            dataCache.put(result.getCampaignId(), result);
//        }
//        return result;
//    }
//    
//
//    public List<Campaign> findAll() {
//        return campaignRepository.findAll();
//    }
//
//    public List<Campaign> findByIsActive(Boolean isActive) {
//        return campaignRepository.findByIsActive(isActive);
//    }
//
//    public Long countAll() {
//        return campaignRepository.count();
//    }
//
//    public void save(Campaign campaign) {
//        campaignRepository.save(campaign);
//
//        if(campaign != null){
//            dataCache.put(campaign.getCampaignId(), campaign);
//			DataSyncUtil.settingReSync(CAMPAIGN_SYNC);
//        }
//    }
//    
//    @Transactional(rollbackFor=Exception.class, timeout = 30)
//    public void delete(String campaignId) throws BcsNoticeException{
//        logger.debug("delete:" + campaignId);
//        
//        Campaign campaign = campaignRepository.findOne(campaignId);
//        
//        campaignRepository.delete(campaign);
//        dataCache.invalidate(campaignId);
//		DataSyncUtil.settingReSync(CAMPAIGN_SYNC);
//    }
//    
//    public String findCampaignNameByCampaignId(String campaignId) throws BcsNoticeException{
//        try {
//            Campaign result = dataCache.get(campaignId);
//            if(notNull(result)){
//                return result.getCampaignName();
//            }
//        } catch (Exception e) {}
//        
//        return campaignRepository.findCampaignNameByCampaignId(campaignId);
//    }
//    
//    public Campaign findOne(String campaignId){
//        try {
//            Campaign result = dataCache.get(campaignId);
//            if(notNull(result)){
//                return result;
//            }
//        } catch (Exception e) {}
//        
//        Campaign result = campaignRepository.findOne(campaignId);
//        if(result != null){
//            dataCache.put(result.getCampaignId(), result);
//        }
//        return result;
//    }
//    
//    public String generateCampaignId() {
//        String campaignId = UUID.randomUUID().toString().toLowerCase();
//        
//        while (campaignRepository.findOne(campaignId) != null) {
//            campaignId = UUID.randomUUID().toString().toLowerCase();
//        }
//        return campaignId;
//    }
//    
//	@Transactional(rollbackFor=Exception.class, timeout = 30)
//    public void deleteFromUI(String campaignId, String account) throws BcsNoticeException {
//        logger.info("deleteFromUI:" + campaignId);
//        
//        String campaignName = campaignService.findCampaignNameByCampaignId(campaignId);
//        campaignService.delete(campaignId);
//        createSystemLog("Delete", campaignName, account, new Date(), campaignId.toString());
//    }
//	
//	private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime, String referenceId) {
//        SystemLogUtil.saveLogDebug("Campaign", action, modifyUser, content, referenceId);
//    }
//	
//	@Transactional(rollbackFor=Exception.class, timeout = 30)
//    public Campaign saveFromUI(Campaign campaign, String account) throws BcsNoticeException{
//        logger.info("saveFromUI:" + campaign);
//
//        String campaignId = campaign.getCampaignId();
//        
//        String action = "Edit";
//        if (campaignId == null) {
//            action = "Create";
//            
//            campaignId = campaignService.generateCampaignId();
//            campaign.setCampaignId(campaignId);
//        }
//            
//        // Set Modify Admin User
//        campaign.setModifyUser(account);
//        campaign.setModifyTime(new Date());
//        
//        // Save Campaign
//        campaignService.save(campaign);
//        
//        campaign = campaignService.findOne(campaign.getCampaignId());
//        createSystemLog(action, campaign, campaign.getModifyUser(), campaign.getModifyTime(), campaign.getCampaignId());
//        return campaign;
//    }
//
//    @Transactional(rollbackFor=Exception.class, timeout = 30)
//	public Campaign switchIsActive(String campaignId, String account) throws BcsNoticeException{
//        logger.info("switchIsActive:" + campaignId);
//        
//        Campaign campaign = campaignService.findOne(campaignId);
//        if (campaign != null) {
//            boolean switchValue = (campaign.getIsActive() == Boolean.TRUE) ? false : true;
//            campaign.setIsActive(switchValue);
//            
//            // Set Modify Admin User
//            campaign.setModifyUser(account);
//            campaign.setModifyTime(new Date());
//            // Save Campaign
//            campaignService.save(campaign);
//        }
//        
//        return campaign;
//	}
}
