package com.bcs.core.db.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

//import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.WinnerList;
import com.bcs.core.db.repository.WinnerListRepository;
import com.bcs.core.model.WinnedCouponModel;
import com.bcs.core.model.WinnerModel;

@Service
public class WinnerListService {
	private final static Integer pageSize = 1000;
	
	
	@Autowired
	private WinnerListRepository winnerListRepository;
	
	
	/** Logger */
	private static Logger logger = Logger.getLogger(WinnerListService.class);
		
	public WinnerListService(){
	}
	
	@PersistenceContext
    EntityManager entityManager;
	
	/**
	 * 
     */
	public void saveWinner(WinnerList winner){
		try {
    	winnerListRepository.save(winner);
    	} catch(DataAccessException e) {
    		logger.info("Spring Data Error: " + e);
    	}
	}
	
	/**
	 * 取得中獎名單
     */
    @SuppressWarnings("unchecked")
    public List<WinnedCouponModel> getWinnerList(String gameId,Integer pageIndex) throws ParseException{
    	List<WinnedCouponModel> winnerList = new ArrayList<WinnedCouponModel>();
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	String queryString = 
    		"SELECT "
    			+ "BCS_ACTION_USER_COUPON.ID, "
    			+ "BCS_ACTION_USER_COUPON.MID, "
    			+ "BCS_CONTENT_COUPON.COUPON_TITLE, "
    			+ "BCS_ACTION_USER_COUPON.ACTION_TIME, "
    			+ "BCS_CONTENT_COUPON.IS_FILL_IN, "
    			+ "BCS_WINNER_LIST.WINNER_LIST_ID, "
    			+ "BCS_WINNER_LIST.USER_NAME, "
    			+ "BCS_WINNER_LIST.USER_PHONENUMBER, "
    			+ "BCS_WINNER_LIST.USER_EMAIL, "
    			+ "BCS_WINNER_LIST.USER_ADDRESS "
    		+ "FROM "
    			+ "BCS_ACTION_USER_COUPON "
    			+ "LEFT JOIN BCS_CONTENT_COUPON ON BCS_ACTION_USER_COUPON.COUPON_ID = BCS_CONTENT_COUPON.COUPON_ID "
    			+ "LEFT JOIN BCS_WINNER_LIST ON BCS_ACTION_USER_COUPON.WINNER_LIST_ID = BCS_WINNER_LIST.WINNER_LIST_ID "
    		+ "WHERE "
    			+ "BCS_ACTION_USER_COUPON.ACTION_TYPE = 'GET' "
    			+ "AND BCS_CONTENT_COUPON.EVENT_REFERENCE_ID = ?1 "
    			+ "AND BCS_CONTENT_COUPON.EVENT_REFERENCE = 'SCRATCH_CARD' "
    			+ "AND BCS_CONTENT_COUPON.STATUS = 'ACTIVE'"
    		+ "ORDER BY BCS_ACTION_USER_COUPON.ACTION_TIME ASC";
    	
    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, gameId);
    	query.setFirstResult(((pageIndex-1)*pageSize));
    	query.setMaxResults(pageSize);
		List<Object[]> list = query.getResultList();
    	
		for (Object[] o : list) {
			WinnedCouponModel winnerInfo = new WinnedCouponModel();
			
			winnerInfo.setActionUserCouponId(Long.parseLong(o[0].toString()));
			winnerInfo.setUserMID((o[1] != null) ? o[1].toString() : null);
			winnerInfo.setCouponTitle(o[2].toString());
			winnerInfo.setActionTime(simpleDateFormat.parse(o[3].toString()));
			winnerInfo.setIsFillIn(Boolean.valueOf(o[4].toString()));
			
			if(Boolean.valueOf(o[4].toString())) {
				WinnerModel winnerDetail = new WinnerModel();
				
				winnerDetail.setWinnerListId((o[5] == null) ? null : o[5].toString());
				winnerDetail.setUserName((o[6] == null) ? null : o[6].toString());
				winnerDetail.setUserPhoneNumber((o[7] == null) ? null : o[7].toString());
				winnerDetail.setUserEMail((o[8] == null) ? null : o[8].toString());
				winnerDetail.setUserAddress((o[9] == null) ? null : o[9].toString());
				
				winnerInfo.setWinnerDetail(winnerDetail);
			} else {
				winnerInfo.setWinnerDetail(null);
			}
			
			winnerList.add(winnerInfo);
		}
		return winnerList;
    }
    
    /**
	 * 取得某獎品中獎名單
     */
    @SuppressWarnings("unchecked")
    public List<WinnedCouponModel> getWinnerListByPrizeId(String gameId, String couponPrizeId,Integer pageIndex) throws ParseException {
    	List<WinnedCouponModel> winnerList = new ArrayList<WinnedCouponModel>();
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	String queryString = 
    		"SELECT "
    			+ "BCS_ACTION_USER_COUPON.ID, "
    			+ "BCS_ACTION_USER_COUPON.MID, "
    			+ "BCS_CONTENT_COUPON.COUPON_TITLE, "
    			+ "BCS_ACTION_USER_COUPON.ACTION_TIME, "
    			+ "BCS_CONTENT_COUPON.IS_FILL_IN, "
    			+ "BCS_WINNER_LIST.WINNER_LIST_ID, "
    			+ "BCS_WINNER_LIST.USER_NAME, "
    			+ "BCS_WINNER_LIST.USER_PHONENUMBER, "
    			+ "BCS_WINNER_LIST.USER_EMAIL, "
    			+ "BCS_WINNER_LIST.USER_ADDRESS "
    		+ "FROM "
    			+ "BCS_ACTION_USER_COUPON "
    			+ "LEFT JOIN BCS_CONTENT_COUPON ON BCS_ACTION_USER_COUPON.COUPON_ID = BCS_CONTENT_COUPON.COUPON_ID "
    			+ "LEFT JOIN BCS_WINNER_LIST ON BCS_ACTION_USER_COUPON.WINNER_LIST_ID = BCS_WINNER_LIST.WINNER_LIST_ID "
    		+ "WHERE "
    			+ "BCS_ACTION_USER_COUPON.ACTION_TYPE = 'GET' "
    			+ "AND BCS_CONTENT_COUPON.EVENT_REFERENCE_ID = ?1 "
    			+ "AND BCS_CONTENT_COUPON.EVENT_REFERENCE = 'SCRATCH_CARD' "
    			+ "AND BCS_ACTION_USER_COUPON.COUPON_ID = ?2 "
    			+ "AND BCS_CONTENT_COUPON.STATUS = 'ACTIVE'"
    		+ "ORDER BY BCS_ACTION_USER_COUPON.ACTION_TIME ASC";
    	
    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, gameId).setParameter(2, couponPrizeId);
    	query.setFirstResult(((pageIndex-1)*pageSize));
    	query.setMaxResults(pageSize);
		List<Object[]> list = query.getResultList();
    	
		for (Object[] o : list) {
			WinnedCouponModel winnerInfo = new WinnedCouponModel();
			
			winnerInfo.setActionUserCouponId(Long.parseLong(o[0].toString()));
			winnerInfo.setUserMID((o[1] != null) ? o[1].toString() : null);
			winnerInfo.setCouponTitle(o[2].toString());
			winnerInfo.setActionTime(simpleDateFormat.parse(o[3].toString()));
			winnerInfo.setIsFillIn(Boolean.valueOf(o[4].toString()));
			
			if(Boolean.valueOf(o[4].toString())) {
				WinnerModel winnerDetail = new WinnerModel();
				
				winnerDetail.setWinnerListId((o[5] == null) ? null : o[5].toString());
				winnerDetail.setUserName((o[6] == null) ? null : o[6].toString());
				winnerDetail.setUserPhoneNumber((o[7] == null) ? null : o[7].toString());
				winnerDetail.setUserEMail((o[8] == null) ? null : o[8].toString());
				winnerDetail.setUserAddress((o[9] == null) ? null : o[9].toString());
				
				winnerInfo.setWinnerDetail(winnerDetail);
			} else {
				winnerInfo.setWinnerDetail(null);
			}
			
			winnerList.add(winnerInfo);
		}
		return winnerList;
    }
    /**
	 * 取得一段時間中獎名單
     * 
     */
    @SuppressWarnings("unchecked")
    public  List<WinnedCouponModel> queryWinnerList(String gameId, String startDate, String endDate,Optional<Integer> pageIndex) throws ParseException{
    	List<WinnedCouponModel> winnerList = new ArrayList<WinnedCouponModel>();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	String queryString = 
        		"SELECT "
        			+ "BCS_ACTION_USER_COUPON.ID, "
        			+ "BCS_ACTION_USER_COUPON.MID, "
        			+ "BCS_CONTENT_COUPON.COUPON_TITLE, "
        			+ "BCS_ACTION_USER_COUPON.ACTION_TIME, "
        			+ "BCS_CONTENT_COUPON.IS_FILL_IN, "
        			+ "BCS_WINNER_LIST.WINNER_LIST_ID, "
        			+ "BCS_WINNER_LIST.USER_NAME, "
        			+ "BCS_WINNER_LIST.USER_IDCARDNUMBER, "
        			+ "BCS_WINNER_LIST.USER_PHONENUMBER, "
        			+ "BCS_WINNER_LIST.USER_EMAIL, "
        			+ "BCS_WINNER_LIST.USER_ADDRESS "
        		+ "FROM "
        			+ "BCS_ACTION_USER_COUPON "
        			+ "LEFT JOIN BCS_CONTENT_COUPON ON BCS_ACTION_USER_COUPON.COUPON_ID = BCS_CONTENT_COUPON.COUPON_ID "
        			+ "LEFT JOIN BCS_WINNER_LIST ON BCS_ACTION_USER_COUPON.WINNER_LIST_ID = BCS_WINNER_LIST.WINNER_LIST_ID "
        		+ "WHERE "
        			+ "BCS_ACTION_USER_COUPON.ACTION_TYPE = 'GET' "
        			+ "AND BCS_CONTENT_COUPON.EVENT_REFERENCE_ID = ?1 "
        			+ "AND BCS_CONTENT_COUPON.EVENT_REFERENCE = 'SCRATCH_CARD' "
        			+ "AND BCS_CONTENT_COUPON.STATUS = 'ACTIVE' "
        			+ "AND (BCS_ACTION_USER_COUPON.ACTION_TIME BETWEEN ?2 AND ?3) "
        		+ "ORDER BY BCS_ACTION_USER_COUPON.ACTION_TIME ASC";
    	
    	Date startDateObj = sdf.parse(startDate);
		Date endDateObj = sdf.parse(endDate);
		Calendar c = Calendar.getInstance();
		c.setTime(endDateObj);
		c.add(Calendar.DATE, 1); //增加一天，因為轉換的date其分秒是0，因此查詢時，今天新增的發送報告有設定時與分時，可能會撈不到
		c.add(Calendar.SECOND, -1); //減一秒，因為可能今天新增的發送報告時間是隔天且無設定時與分，會與增加一天的時間重疊，導致可能撈到隔天的資料
		endDateObj = c.getTime();
		
    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, gameId).setParameter(2, startDateObj).setParameter(3, endDateObj);
		if(pageIndex.isPresent()){
	    	query.setFirstResult(((pageIndex.get()-1)*pageSize));
	    	query.setMaxResults(pageSize);
		}
    	List<Object[]> list = query.getResultList();
    	
		for (Object[] o : list) {
			WinnedCouponModel winnerInfo = new WinnedCouponModel();
			
			winnerInfo.setActionUserCouponId(Long.parseLong(o[0].toString()));
			winnerInfo.setUserMID((o[1] != null) ? o[1].toString() : null);
			winnerInfo.setCouponTitle(o[2].toString());
			winnerInfo.setActionTime(simpleDateFormat.parse(o[3].toString()));
			winnerInfo.setIsFillIn(Boolean.valueOf(o[4].toString()));
			
			if(Boolean.valueOf(o[4].toString())) {
				WinnerModel winnerDetail = new WinnerModel();
				
				winnerDetail.setWinnerListId((o[5] == null) ? null : o[5].toString());
				winnerDetail.setUserName((o[6] == null) ? null : o[6].toString());
				winnerDetail.setUserIdCardNumber((o[7] == null) ? null : o[7].toString());
				winnerDetail.setUserPhoneNumber((o[8] == null) ? null : o[8].toString());
				winnerDetail.setUserEMail((o[9] == null) ? null : o[9].toString());
				winnerDetail.setUserAddress((o[10] == null) ? null : o[10].toString());
				
				winnerInfo.setWinnerDetail(winnerDetail);
			} else {
				winnerInfo.setWinnerDetail(null);
			}
			
			winnerList.add(winnerInfo);
		}
		
		return winnerList;
    }
    
    /**
	 * 取得一段時間某獎品中獎名單
     *  
     */
    @SuppressWarnings("unchecked")
    public  List<WinnedCouponModel> queryWinnerListByPrizeId(String gameId, String couponPrizeId, String startDate, String endDate,Optional<Integer> pageIndex) throws ParseException{
    	List<WinnedCouponModel> winnerList = new ArrayList<WinnedCouponModel>();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	String queryString = 
        		"SELECT "
        			+ "BCS_ACTION_USER_COUPON.ID, "
        			+ "BCS_ACTION_USER_COUPON.MID, "
        			+ "BCS_CONTENT_COUPON.COUPON_TITLE, "
        			+ "BCS_ACTION_USER_COUPON.ACTION_TIME, "
        			+ "BCS_CONTENT_COUPON.IS_FILL_IN, "
        			+ "BCS_WINNER_LIST.WINNER_LIST_ID, "
        			+ "BCS_WINNER_LIST.USER_NAME, "
        			+ "BCS_WINNER_LIST.USER_IDCARDNUMBER, "
        			+ "BCS_WINNER_LIST.USER_PHONENUMBER, "
        			+ "BCS_WINNER_LIST.USER_EMAIL, "
        			+ "BCS_WINNER_LIST.USER_ADDRESS "
        		+ "FROM "
        			+ "BCS_ACTION_USER_COUPON "
        			+ "LEFT JOIN BCS_CONTENT_COUPON ON BCS_ACTION_USER_COUPON.COUPON_ID = BCS_CONTENT_COUPON.COUPON_ID "
        			+ "LEFT JOIN BCS_WINNER_LIST ON BCS_ACTION_USER_COUPON.WINNER_LIST_ID = BCS_WINNER_LIST.WINNER_LIST_ID "
        		+ "WHERE "
        			+ "BCS_ACTION_USER_COUPON.ACTION_TYPE = 'GET' "
        			+ "AND BCS_CONTENT_COUPON.EVENT_REFERENCE_ID = ?1 "
        			+ "AND BCS_CONTENT_COUPON.EVENT_REFERENCE = 'SCRATCH_CARD' "
        			+ "AND BCS_CONTENT_COUPON.STATUS = 'ACTIVE' "
        			+ "AND BCS_ACTION_USER_COUPON.COUPON_ID = ?2 "
        			+ "AND (BCS_ACTION_USER_COUPON.ACTION_TIME BETWEEN ?3 AND ?4) "
        		+ "ORDER BY BCS_ACTION_USER_COUPON.ACTION_TIME ASC";
    	
    	Date startDateObj = sdf.parse(startDate);
		Date endDateObj = sdf.parse(endDate);
		Calendar c = Calendar.getInstance();
		c.setTime(endDateObj);
		c.add(Calendar.DATE, 1); //增加一天，因為轉換的date其分秒是0，因此查詢時，今天新增的發送報告有設定時與分時，可能會撈不到
		c.add(Calendar.SECOND, -1); //減一秒，因為可能今天新增的發送報告時間是隔天且無設定時與分，會與增加一天的時間重疊，導致可能撈到隔天的資料
		endDateObj = c.getTime();
		
    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, gameId).setParameter(2, couponPrizeId).setParameter(3, startDateObj).setParameter(4, endDateObj);
    	if(pageIndex.isPresent()){
	    	query.setFirstResult(((pageIndex.get()-1)*pageSize));
	    	query.setMaxResults(pageSize);
		}
    	List<Object[]> list = query.getResultList();
    	
		for (Object[] o : list) {
			WinnedCouponModel winnerInfo = new WinnedCouponModel();
			
			winnerInfo.setActionUserCouponId(Long.parseLong(o[0].toString()));
			winnerInfo.setUserMID((o[1] != null) ? o[1].toString() : null);
			winnerInfo.setCouponTitle(o[2].toString());
			winnerInfo.setActionTime(simpleDateFormat.parse(o[3].toString()));
			winnerInfo.setIsFillIn(Boolean.valueOf(o[4].toString()));
			
			if(Boolean.valueOf(o[4].toString())) {
				WinnerModel winnerDetail = new WinnerModel();
				
				winnerDetail.setWinnerListId((o[5] == null) ? null : o[5].toString());
				winnerDetail.setUserName((o[6] == null) ? null : o[6].toString());
				winnerDetail.setUserIdCardNumber((o[7] == null) ? null : o[7].toString());
				winnerDetail.setUserPhoneNumber((o[8] == null) ? null : o[8].toString());
				winnerDetail.setUserEMail((o[9] == null) ? null : o[9].toString());
				winnerDetail.setUserAddress((o[10] == null) ? null : o[10].toString());
				
				winnerInfo.setWinnerDetail(winnerDetail);
			} else {
				winnerInfo.setWinnerDetail(null);
			}
			
			winnerList.add(winnerInfo);
		}
		
		return winnerList;
    }
    
    /**
	 * 取得中獎人資料
     */
    @SuppressWarnings("unchecked")
    public WinnerModel getWinnerDetail(String winnerListId){
    	WinnerModel winnerDetail = new WinnerModel();
    	
    	String queryString = 
    			"SELECT "
    				+ "WINNER_LIST_ID, "
    				+ "USER_NAME, "
    				+ "USER_IDCARDNUMBER, "
    				+ "USER_PHONENUMBER, "
    				+ "USER_EMAIL, "
    				+ "USER_ADDRESS "
    			+ "FROM "
    				+ "BCS_WINNER_LIST "
    			+ "WHERE "
    				+ "WINNER_LIST_ID = ?1";
    	
    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, winnerListId);
		List<Object[]> list = query.getResultList();
		
		for (Object[] o : list) {
			winnerDetail.setWinnerListId((o[0] == null) ? null : o[0].toString());
			winnerDetail.setUserName((o[1] == null) ? null :o[1].toString());
			winnerDetail.setUserIdCardNumber((o[2] == null) ? null : o[2].toString());
			winnerDetail.setUserPhoneNumber((o[3] == null) ? null : o[3].toString());
			winnerDetail.setUserEMail((o[4] == null) ? null : o[4].toString());
			winnerDetail.setUserAddress((o[5] == null) ? null : o[5].toString());
		}
		
    	return winnerDetail;
    }
    
    /**
	 * 取得優惠券使用者填寫資料
     */
    @SuppressWarnings("unchecked")
    public List<WinnedCouponModel> getWinnerListAndCouponCodeByCouponId(String couponId,Integer pageIndex) throws ParseException {
    	List<WinnedCouponModel> winnedCouponModelList = new ArrayList<WinnedCouponModel>();
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	String queryString = 
    		"SELECT "
    			+ "BCS_WINNER_LIST.MODIFY_TIME, "			//0 modifyTime
    			+ "BCS_WINNER_LIST.UID, "					//1 uid
    			+ "BCS_WINNER_LIST.USER_ADDRESS, "			//2 userAddress
    			+ "BCS_WINNER_LIST.USER_IDCARDNUMBER, "		//3 idCardNumber
    			+ "BCS_WINNER_LIST.USER_NAME, "				//4 userName
    			+ "BCS_WINNER_LIST.USER_PHONENUMBER, "		//5 userPhoneNumber
    			+ "BCS_CONTENT_COUPON_CODE.COUPON_CODE "	//6 couponCode
    		+ "FROM "
    			+ "BCS_ACTION_USER_COUPON "
    			+ "LEFT JOIN BCS_WINNER_LIST ON BCS_WINNER_LIST.WINNER_LIST_ID = BCS_ACTION_USER_COUPON.WINNER_LIST_ID "
    			+ "LEFT JOIN BCS_CONTENT_COUPON_CODE ON BCS_CONTENT_COUPON_CODE.ACTION_USER_COUPON_ID = BCS_ACTION_USER_COUPON.ID "
    		+ "WHERE "
    			+ "BCS_ACTION_USER_COUPON.ACTION_TYPE = 'GET' "
    			+ "AND "
    			+ "BCS_WINNER_LIST.COUPON_ID= ?1";
    	
    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, couponId);
    	query.setFirstResult(((pageIndex-1)*pageSize));
    	query.setMaxResults(pageSize);
		Date date = new Date();
		logger.info("start query time:"+date);
		List<Object[]> list = query.getResultList();
		date = new Date();
		logger.info("end query time:"+date);
		for (Object[] o : list) {
			WinnedCouponModel winnedCouponModel = new WinnedCouponModel();
			WinnerModel winnerDetail = new WinnerModel();
			
			winnerDetail.setModifyTime(simpleDateFormat.parse(o[0].toString()));
			winnerDetail.setUID((o[1] != null) ? o[1].toString() : null);
			winnerDetail.setUserAddress(o[2].toString());
			winnerDetail.setUserIdCardNumber(o[3].toString());
			winnerDetail.setUserName(o[4].toString());
			winnerDetail.setUserPhoneNumber(o[5].toString());	
			
	
			winnedCouponModel.setCouponCode(o[6] != null ? o[6].toString() : "");

			
			winnedCouponModel.setWinnerDetail(winnerDetail);
			
			winnedCouponModelList.add(winnedCouponModel);
		}
		return winnedCouponModelList;
    }
    
    /**
	 * 取得優惠券使用者填寫資料 By 時間
     */
    @SuppressWarnings("unchecked")
    public List<WinnedCouponModel> getWinnerListAndCouponCodeByCouponId(String couponId,String startDate,String endDate,Optional<Integer> pageIndex) throws ParseException {
    	List<WinnedCouponModel> winnedCouponModelList = new ArrayList<WinnedCouponModel>();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	
    	String queryString = 
        		"SELECT "
        			+ "BCS_WINNER_LIST.MODIFY_TIME, "			//0 modifyTime
        			+ "BCS_WINNER_LIST.UID, "					//1 uid
        			+ "BCS_WINNER_LIST.USER_ADDRESS, "			//2 userAddress
        			+ "BCS_WINNER_LIST.USER_IDCARDNUMBER, "		//3 idCardNumber
        			+ "BCS_WINNER_LIST.USER_NAME, "				//4 userName
        			+ "BCS_WINNER_LIST.USER_PHONENUMBER, "		//5 userPhoneNumber
        			+ "BCS_CONTENT_COUPON_CODE.COUPON_CODE "	//6 couponCode
        		+ "FROM "
        			+ "BCS_ACTION_USER_COUPON "
        			+ "LEFT JOIN BCS_WINNER_LIST ON BCS_WINNER_LIST.WINNER_LIST_ID = BCS_ACTION_USER_COUPON.WINNER_LIST_ID "
        			+ "LEFT JOIN BCS_CONTENT_COUPON_CODE ON BCS_CONTENT_COUPON_CODE.ACTION_USER_COUPON_ID = BCS_ACTION_USER_COUPON.ID "
        		+ "WHERE "
        			+ "BCS_ACTION_USER_COUPON.ACTION_TYPE = 'GET' "
        			+ "AND "
        			+ "BCS_WINNER_LIST.COUPON_ID= ?1 "
        			+ "AND "
        			+ "(BCS_WINNER_LIST.MODIFY_TIME BETWEEN ?2 AND ?3) ";
    	
    	Date startDateObj = sdf.parse(startDate);
		Date endDateObj = sdf.parse(endDate);
		Calendar c = Calendar.getInstance();
		c.setTime(endDateObj);
		c.add(Calendar.DATE, 1); //增加一天，因為轉換的date其分秒是0，因此查詢時，今天新增的發送報告有設定時與分時，可能會撈不到
		c.add(Calendar.SECOND, -1); //減一秒，因為可能今天新增的發送報告時間是隔天且無設定時與分，會與增加一天的時間重疊，導致可能撈到隔天的資料
		endDateObj = c.getTime();
		
    	Query query = entityManager.createNativeQuery(queryString)
    			.setParameter(1, couponId).setParameter(2, startDateObj).setParameter(3, endDateObj);
    	
    	if(pageIndex.isPresent()){
        	query.setFirstResult(((pageIndex.get()-1)*pageSize));
        	query.setMaxResults(pageSize);
    	}

		List<Object[]> list = query.getResultList();
    	
		for (Object[] o : list) {
			WinnedCouponModel winnedCouponModel = new WinnedCouponModel();
			logger.info("time:"+simpleDateFormat.parse(o[0].toString()));
			WinnerModel winnerDetail = new WinnerModel();
			
			winnerDetail.setModifyTime(simpleDateFormat.parse(o[0].toString()));			
			winnerDetail.setUID(o[1]!=null ? o[1].toString():"");
			winnerDetail.setUserAddress(o[2]!=null ? o[2].toString():"");
			winnerDetail.setUserIdCardNumber(o[3]!=null ? o[3].toString():"");
			winnerDetail.setUserName(o[4]!=null ? o[4].toString():"");
			winnerDetail.setUserPhoneNumber(o[5]!=null? o[5].toString():"");	
			winnedCouponModel.setCouponCode(o[6] != null ? o[6].toString() : "");
			
			winnedCouponModel.setWinnerDetail(winnerDetail);
			
			winnedCouponModelList.add(winnedCouponModel);
		}
		return winnedCouponModelList;
    }
    
    public Integer getWinnerPageByCouponId(String couponId){
    	Integer winnerNum =  winnerListRepository.countTotalWinnerByCouponId(couponId);
    	Integer page = winnerNum / pageSize;
		if(winnerNum % 1000 != 0){
			page++;
		}
		return page;
    }
	
    public WinnerList findOne(String winnerListId){
    	return winnerListRepository.findOne(winnerListId);
    }
    
    public Integer getGameWinnerMaxPageByGameIdAndCouponId(String gameId,Optional<String> couponId){
    	Integer gameWinnerNum = 0;
    	if(!couponId.isPresent()){
    		gameWinnerNum= winnerListRepository.countTotalWinnerByGameId(gameId);
    	}else{
    		gameWinnerNum= winnerListRepository.countTotalWinnerByGameIdAndCouponId(gameId,couponId.get());
    	}
    	
    	Integer page = gameWinnerNum / pageSize;
		if(gameWinnerNum % 1000 != 0){
			page++;
		}
		return page;
    } 
    
	/** 
	 * 回傳一個沒有重覆的uuid
	 */
	public String checkDuplicateUUID(String queryType) {
		String uuid = UUID.randomUUID().toString().toLowerCase();
		Boolean duplicateUUID = checkDuplicateUUID(queryType, uuid);
		while (duplicateUUID) {
			uuid = UUID.randomUUID().toString().toLowerCase();
			duplicateUUID = checkDuplicateUUID(queryType, uuid);
		}
		
		return uuid;
	}
    
    /**
	 *  檢查有無重覆使用到UUID
     */
    public Boolean checkDuplicateUUID(String queryType, String uuid) {
    	if(queryType == "1"){
    		WinnerList contenAcceptPrize = winnerListRepository.findOne(uuid);
    		if (contenAcceptPrize == null) return false;
    	}
    	
		return true;
    }
}
