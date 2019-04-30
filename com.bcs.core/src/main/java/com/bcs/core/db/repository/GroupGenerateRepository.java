package com.bcs.core.db.repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.bcs.core.db.entity.SendGroupDetail;

@Repository
public class GroupGenerateRepository{

	/** Logger */
	private static Logger logger = Logger.getLogger(GroupGenerateRepository.class);
	
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private SendGroupDetailRepository sendGroupDetailRepository;
    
	public static List<String> validQueryOp = Arrays.asList(new String[] { ">", ">=", "<", "<=", "=" });
    
    @PostConstruct
    public void init() {
    	
    }
    
	public BigInteger findMIDCountBySendGroupDetail(List<SendGroupDetail> sendGroupDetails) throws Exception {
		Query query = buildFindQuery(sendGroupDetails, "COUNT(DISTINCT MID)");
		Object result = query.getSingleResult();
		if(result instanceof  BigInteger){
			logger.debug("findMidCountBySendGroupDetail : BigInteger:" + result);
			return (BigInteger) result;
		}
		else if(result instanceof  Integer){
			logger.debug("findMidCountBySendGroupDetail : Integer:" + result);
			return BigInteger.valueOf(new Long((Integer)result));
		}
		else{
			return BigInteger.ZERO;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<String> findMIDBySendGroupDetailGroupId(Long groupId) throws Exception {
		List<SendGroupDetail> sendGroupDetails = sendGroupDetailRepository.findBySendGroupGroupId(groupId);
		Query query = buildFindQuery(sendGroupDetails, "DISTINCT MID");
		return query.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public String checkMIDBySendGroupDetailGroupId(Long groupId, String mid) throws Exception {
		List<SendGroupDetail> sendGroupDetails = sendGroupDetailRepository.findBySendGroupGroupId(groupId);
		Query query = buildFindQuery(sendGroupDetails, "MID", mid);
		List<String> result = query.getResultList();
		if(result != null && result.size() > 0){
			return result.get(0);
		}
		else{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> findMIDBySendGroupDetail(List<SendGroupDetail> sendGroupDetails) throws Exception {
		
		Query query = buildFindQuery(sendGroupDetails, "DISTINCT MID");
		return query.getResultList();
	}
	
	private Query buildFindQuery(List<SendGroupDetail> sendGroupDetails, String selectColumns) throws Exception {
		return this.buildFindQuery(sendGroupDetails, selectColumns, null);
	}
	
	/**
	 * 建立用來查詢 IK0105 的 Query
	 * 
	 * @param defaultSqlString
	 * @param sendGroupDetails
	 * @return
	 * @throws Exception 
	 */
	private Query buildFindQuery(List<SendGroupDetail> sendGroupDetails, String selectColumns, String mid) throws Exception {
		Validate.notEmpty(sendGroupDetails);
		Validate.notEmpty(selectColumns);
		
		List<SendGroupDetail> sendGroupSetting = new ArrayList<SendGroupDetail>();
		List<SendGroupDetail> uploadMidSetting = new ArrayList<SendGroupDetail>();
		
		// 分離 Upload Mid Detail Setting
		for(SendGroupDetail detail : sendGroupDetails){
			if("UploadMid".equals(detail.getQueryField())){
				uploadMidSetting.add(detail);
			}
			else{
				sendGroupSetting.add(detail);
			}
		}
		
		sendGroupDetails = sendGroupSetting;
		
		//  驗證 queryOp，避免SQL攻擊(SQL injection)
		checkSendGroupDetail(sendGroupDetails);

		String sqlString = "SELECT "
						+ selectColumns
				+ " FROM ";
		
		if(sendGroupDetails != null && sendGroupDetails.size() > 0){
			sqlString += generateMidFieldSettingFrom(sendGroupDetails, 1);
		}
		
		// Setting Upload Mid SQL
		if(uploadMidSetting != null && uploadMidSetting.size() > 0){
			if(sendGroupDetails != null && sendGroupDetails.size() > 0){
				sqlString += ", " + generateUploadMidSettingFrom(uploadMidSetting, sendGroupDetails.size()*2 + 1);
			}
			else{

				selectColumns = selectColumns.replace("MID", "SETMID");
				
				sqlString = 
						"SELECT " 
								+ selectColumns
						+ " FROM " + generateUploadMidSettingFrom(uploadMidSetting, 1);

				if(StringUtils.isNotBlank(mid)){
					sqlString += " WHERE SETMID = ?" + (uploadMidSetting.size() + 1) + " ";
				}
			}
		}

		// Setting Upload Mid SQL
		if(uploadMidSetting != null && uploadMidSetting.size() > 0){
			if(sendGroupDetails != null && sendGroupDetails.size() > 0){
				sqlString += " WHERE MID = EVENT_SET.SETMID ";

				if(StringUtils.isNotBlank(mid)){
					sqlString += " AND MID = ?" + (sendGroupDetails.size()*2 + 1 + uploadMidSetting.size()) + " ";
				}
			}
		}
		else{
			if(sendGroupDetails != null && sendGroupDetails.size() > 0){
				if(StringUtils.isNotBlank(mid)){
					sqlString += " WHERE MID = ?" + (sendGroupDetails.size()*2 + 1) + " ";
				}
			}
		}
		
		logger.info(sqlString);
		
		if(StringUtils.isBlank(sqlString)){
			throw new Exception("SQL Error : Blank");
		}
		
		Query query = entityManager.createNativeQuery(sqlString);
		query.setHint("javax.persistence.query.timeout", 30000);
		
		for (int i = 0; i < sendGroupDetails.size(); i++) {
			query.setParameter(2*i + 1, sendGroupDetails.get(i).getQueryField());
			query.setParameter(2*i + 2, sendGroupDetails.get(i).getQueryValue());
			logger.info("setParameter Field:" + (2*i + 1) + ", " + sendGroupDetails.get(i).getQueryField());
			logger.info("setParameter Value:" + (2*i + 2) + ", " + sendGroupDetails.get(i).getQueryValue());
		}

		// Setting Upload Mid Parameter
		if(uploadMidSetting != null && uploadMidSetting.size() > 0){

			for (int i = 0; i < uploadMidSetting.size(); i++) {
				String value = uploadMidSetting.get(i).getQueryValue();
				query.setParameter(sendGroupDetails.size()*2 + i + 1, value.split(":")[0]);
				logger.info("setParameter:" + (sendGroupDetails.size()*2 + i + 1) + ", " + value.split(":")[0]);
			}
		}

		if(StringUtils.isNotBlank(mid)){
			query.setParameter(sendGroupDetails.size()*2 + uploadMidSetting.size() + 1, mid);
			logger.info("setParameter:" + (sendGroupDetails.size()*2 + uploadMidSetting.size() + 1) + ", " + mid);
		}
		
		return query;
	}
	
	private String generateMidFieldSettingFrom(List<SendGroupDetail> sendGroupDetails, int params){

		if(sendGroupDetails != null && sendGroupDetails.size() > 0){
			String sqlString = 
					"( "
					+ " SELECT f.MID as MID"
					+ " FROM BCS_USER_FIELD_SET f ";
			
			sqlString += " INNER JOIN BCS_LINE_USER u ON u.MID = f.MID ";
			
			if(sendGroupDetails.size() > 1){
				for(int i = 1; i < sendGroupDetails.size(); i++){
					sqlString += " INNER JOIN BCS_USER_FIELD_SET f" + i + " ON f.MID = f" + i + ".MID ";
				}
			}
			
			SendGroupDetail detail = sendGroupDetails.get(0);
			sqlString += " WHERE f.KEY_DATA = ?" + params + " and f.VALUE " + detail.getQueryOp() + " ?" + (params+1) + " ";
			
			sqlString += " AND (u.STATUS = 'BINDED' OR u.STATUS = 'UNBIND') ";

			if(sendGroupDetails.size() > 1){
				for(int i = 1; i < sendGroupDetails.size(); i++){
					detail = sendGroupDetails.get(i);
					sqlString += " AND f" + i + ".KEY_DATA = ?" + (2*i+params) + " and f" + i + ".VALUE " + detail.getQueryOp() + " ?" + (2*i+params+1) + " ";
				}
			}
			
			sqlString+= " ) AS FIELD_SET ";
			
			return sqlString;
		}
		
		return null;
	}
	
	/**
	 * 產生 Upload Mid Setting From SQL
	 * @param sendGroupDetails
	 * @param params
	 * @return
	 */
	private String generateUploadMidSettingFrom(List<SendGroupDetail> sendGroupDetails, int params){

		if(sendGroupDetails != null && sendGroupDetails.size() > 0){
			String sqlString = 
					"( "
					+ " SELECT s.MID as SETMID"
					+ " FROM BCS_USER_EVENT_SET s ";
			
			sqlString += " INNER JOIN BCS_LINE_USER k ON k.MID = s.MID ";
			
			if(sendGroupDetails.size() > 1){
				for(int i = 1; i < sendGroupDetails.size(); i++){
					sqlString += " INNER JOIN BCS_USER_EVENT_SET s" + i + " ON s.MID = s" + i + ".MID ";
				}
			}
			
			sqlString += " WHERE s.REFERENCE_ID = ?" + params + " ";
			
			sqlString += " AND (k.STATUS = 'BINDED' OR k.STATUS = 'UNBIND') ";

			if(sendGroupDetails.size() > 1){
				for(int i = 1; i < sendGroupDetails.size(); i++){
					sqlString += " AND s" + i + ".REFERENCE_ID = ?" + (i+params) + " ";
				}
			}
			
			sqlString+= " ) AS EVENT_SET ";
			
			return sqlString;
		}
		
		return null;
	}
	
	/**
	 * 驗證 queryField、queryOp，避免SQL攻擊(SQL injection)
	 * 
	 * @param sendGroupDetails
	 */
	private void checkSendGroupDetail(List<SendGroupDetail> sendGroupDetails) {
		for (SendGroupDetail sendGroupDetail : sendGroupDetails) {
			String queryOp = sendGroupDetail.getQueryOp();
			
			if (!validQueryOp.contains(queryOp)) {
				throw new IllegalArgumentException(
						"queryOp is illegal! queryOp : " + queryOp);
			}
		}
	}
}
