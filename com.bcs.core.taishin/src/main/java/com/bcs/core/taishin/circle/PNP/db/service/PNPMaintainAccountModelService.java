package com.bcs.core.taishin.circle.PNP.db.service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelCustom;
import com.bcs.core.taishin.circle.PNP.db.repository.PNPMaintainAccountModelRepository;
import com.bcs.core.taishin.circle.db.service.OracleService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PNPMaintainAccountModelService {

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(PNPMaintainAccountModelService.class);
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
            String divisionName, String departmentName, String groupName, String pccCode, String account, String employeeId, String accountType) {
        return pnpMaintainAccountModelRepository.findByDivisionNameAndDepartmentNameAndGroupNameAndPccCodeAndAccountAndEmployeeIdAndAccountType(
                divisionName, departmentName, groupName, pccCode, account, employeeId, accountType);
    }

    public List<PNPMaintainAccountModel> queryUsePageCoditions(
            String divisionName, String departmentName, String groupName, String pccCode, String account, String employeeId, String accountType, Boolean status) {
        return PNPMaintainAccountModelCustom.queryUseConditions(divisionName, departmentName, groupName, pccCode, account, employeeId, accountType, status);
    }

    public List<PNPMaintainAccountModel> findByAccountAndSourceSystemAndPnpContent(String account, String sourceSystem, String pnpContent) {
        return pnpMaintainAccountModelRepository.findByAccountAndSourceSystemAndPnpContent(account, sourceSystem, pnpContent);
    }

    public List<PNPMaintainAccountModel> findByAccountType(String accountType) {
        return pnpMaintainAccountModelRepository.findByAccountType(accountType);
    }

    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getPNPDetailReportExcelList(String startDate, String endDate, String account, String pccCode, String sourceSystem, String empId) {

        StringBuilder sb = new StringBuilder();
        sb.append("select * from " +
                " ( " +
                "        ( " +
                "                select " +
                "                        concat(d.pnp_main_id, '.', d.pnp_detail_id) as 'id', " +
                "                        a.source_system, " +
                "                        d.proc_flow, " +
                "                        isnull(d.proc_stage, '') + ';' + isnull(a.pathway, '') as 'proc', " +
                "                        a.account, " +
                "                        a.pcc_code, " +
                "                        m.pnp_main_id, " +
                "                        d.sn, " +
                "                        a.template, " +
                "                        d.msg, " +
                "                        1 as message_point, " +
                "                        null as campaign_id, " +
                "                        null as segment_id, " +
                "                        null as program_id, " +
                "                        null as pid, " +
                "                        d.phone, " +
                "                        d.uid, " +
                "                        d.detail_schedule_time as detail_schedule_time1, " +
                "                        d.detail_schedule_time as detail_schedule_time2, " +
                "                        d.pnp_delivery_expire_time as pnp_delivery_expire_time1, " +
                "                        d.pnp_delivery_expire_time as pnp_delivery_expire_time2, " +
                "                        d.status as status1, " +
                "                        d.status as status2, " +
                "                        d.status as status3, " +
                "                        null as is_international, " +
                "                        d.creat_time as creat_time1, " +
                "                        d.creat_time as creat_time2, " +
                "                        a.employee_id " +
                "                from bcs_pnp_detail_ming as d " +
                "                left join bcs_pnp_main_ming as m on d.pnp_main_id = m.pnp_main_id " +
                "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                "        ) " +
                "        union all " +
                "        ( " +
                "                select " +
                "                        concat(d.pnp_main_id, '.', d.pnp_detail_id) as 'id', " +
                "                        a.source_system, " +
                "                        d.proc_flow, " +
                "                        isnull(d.proc_stage, '') + ';' + isnull(a.pathway, '') as 'proc', " +
                "                        a.account, " +
                "                        a.pcc_code, " +
                "                        m.pnp_main_id, " +
                "                        d.dest_name, " +
                "                        a.template, " +
                "                        d.msg, " +
                "                        1 as message_point, " +
                "                        null as campaign_id, " +
                "                        null as segment_id, " +
                "                        null as program_id, " +
                "                        null as pid, " +
                "                        d.phone, " +
                "                        d.uid, " +
                "                        null as detail_schedule_time1, " +
                "                        null as detail_schedule_time2, " +
                "                        d.pnp_delivery_expire_time as pnp_delivery_expire_time1, " +
                "                        d.pnp_delivery_expire_time as pnp_delivery_expire_time2, " +
                "                        d.status as status1, " +
                "                        d.status as status2, " +
                "                        d.status as status3, " +
                "                        null as is_international, " +
                "                        d.creat_time as creat_time1, " +
                "                        d.creat_time as creat_time2, " +
                "                        a.employee_id " +
                "                from bcs_pnp_detail_mitake as d " +
                "                left join bcs_pnp_main_mitake as m on d.pnp_main_id = m.pnp_main_id " +
                "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                "        ) " +
                "        union all " +
                "        ( " +
                "                select " +
                "                        concat(d.pnp_main_id, '.', d.pnp_detail_id) as 'id', " +
                "                        a.source_system, " +
                "                        d.proc_flow, " +
                "                        isnull(d.proc_stage, '') + ';' + isnull(a.pathway, '') as 'proc', " +
                "                        a.account, " +
                "                        a.pcc_code, " +
                "                        m.pnp_main_id, " +
                "                        d.sn, " +
                "                        a.template, " +
                "                        d.msg, " +
                "                        1 as message_point, " +
                "                        d.campaign_id, " +
                "                        d.segment_id, " +
                "                        d.program_id, " +
                "                        d.pid, " +
                "                        d.phone, " +
                "                        d.uid, " +
                "                        null as detail_schedule_time1, " +
                "                        null as detail_schedule_time2, " +
                "                        d.pnp_delivery_expire_time as pnp_delivery_expire_time1, " +
                "                        d.pnp_delivery_expire_time as pnp_delivery_expire_time2, " +
                "                        d.status as status1, " +
                "                        d.status as status2, " +
                "                        d.status as status3, " +
                "                        null as is_international, " +
                "                        d.creat_time as creat_time1, " +
                "                        d.creat_time as creat_time2, " +
                "                        a.employee_id " +
                "                from bcs_pnp_detail_unica as d " +
                "                left join bcs_pnp_main_unica as m on d.pnp_main_id = m.pnp_main_id " +
                "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                "        ) " +
                "        union all " +
                "        ( " +
                "                select " +
                "                        concat(d.pnp_main_id, '.', d.pnp_detail_id) as 'id', " +
                "                        a.source_system, " +
                "                        d.proc_flow, " +
                "                        isnull(d.proc_stage, '') + ';' + isnull(a.pathway, '') as 'proc', " +
                "                        a.account, " +
                "                        a.pcc_code, " +
                "                        m.pnp_main_id, " +
                "                        d.sn, " +
                "                        a.template, " +
                "                        d.msg, " +
                "                        1 as message_point, " +
                "                        d.campaign_id, " +
                "                        d.segment_id, " +
                "                        d.program_id, " +
                "                        d.pid, " +
                "                        d.phone, " +
                "                        d.uid, " +
                "                        null as detail_schedule_time1, " +
                "                        null as detail_schedule_time2, " +
                "                        d.pnp_delivery_expire_time as pnp_delivery_expire_time1, " +
                "                        d.pnp_delivery_expire_time as pnp_delivery_expire_time2, " +
                "                        d.status as status1, " +
                "                        d.status as status2, " +
                "                        d.status as status3, " +
                "                        null as is_international, " +
                "                        d.creat_time as creat_time1, " +
                "                        d.creat_time as creat_time2, " +
                "                        a.employee_id " +
                "                from bcs_pnp_detail_every8d as d " +
                "                left join bcs_pnp_main_every8d as m on d.pnp_main_id = m.pnp_main_id " +
                "                left join bcs_pnp_maintain_account as a on m.pnp_maintain_account_id = a.id " +
                "        ) " +
                " ) as r1 " +
                " where creat_time1 >= ?1 " +
                " and creat_time1 <  dateadd(day, 1, ?2) ");


        if (StringUtils.isNotBlank(account)) {
            sb.append(String.format(" and account = '%s'", account));
        }
        if (StringUtils.isNotBlank(pccCode)) {
            sb.append(String.format(" and pcc_code = '%s' ", pccCode));
        }
        if (StringUtils.isNotBlank(sourceSystem)) {
            sb.append(String.format(" and source_system = '%s' ", sourceSystem));
        }

        boolean oracleUseDepartmentCheck = CoreConfigReader.getBoolean(CONFIG_STR.ORACLE_USE_DEPARTMENT_CHECK, true);
        logger.info("oracleUseDepartmentCheck:" + oracleUseDepartmentCheck);
        if (oracleUseDepartmentCheck) {
            String empAva = oraclePnpService.getAvailableEmpIdsByEmpId(empId);
            if (StringUtils.isNotBlank(empAva)) {
                sb.append(empAva);
            }
        }

        sb.append(" order by creat_time1 desc ");

        logger.info("str1: " + sb.toString());
        Query query = entityManager.createNativeQuery(sb.toString()).setParameter(1, startDate).setParameter(2, endDate);
        logger.info("query:" + query.toString());
        List<Object[]> list = query.getResultList();

        Map<String, List<String>> map = new LinkedHashMap<>();

        int count = 0;
        for (Object[] o : list) {
            count++;
            logger.info("c:" + count);
            List<String> dataList = new ArrayList<>();
            map.put(Integer.toString(count), dataList);
            for (int i = 0, max = 27; i < max; i++) {
                if (o[i] == null) {
                    dataList.add("");
                } else {
                    dataList.add(o[i].toString());
                }
            }
        }
        logger.info("map1: " + map.toString());

        return map;
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
