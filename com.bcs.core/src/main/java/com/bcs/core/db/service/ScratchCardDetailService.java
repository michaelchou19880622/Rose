package com.bcs.core.db.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentGame;
//import com.bcs.core.db.entity.ContentGame;
import com.bcs.core.db.entity.ContentPrize;
import com.bcs.core.db.entity.ScratchCardDetail;
import com.bcs.core.db.repository.ContentGameRepository;
//import com.bcs.core.db.repository.ContentGameRepository;
import com.bcs.core.db.repository.ContentPrizeRepository;
import com.bcs.core.db.repository.ScratchCardDetailRepository;
import com.bcs.core.model.CouponModel;
import com.bcs.core.model.GameModel;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ScratchCardDetailService {
	/* @Autowired
	private ContentGameRepository contentGameRepository; */
	
	@Autowired
	private ContentPrizeRepository contentPrizeRepository;
	
	@Autowired
	private ScratchCardDetailRepository sratchCardDetailRepository;
	
	@Autowired
	private ContentGameRepository contentGameRepository;

	@PersistenceContext
    EntityManager entityManager;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ScratchCardDetailService.class);
	
	protected LoadingCache<String, Map<String, List<String>>> dataCache;
	
	public ScratchCardDetailService(){

		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, Map<String, List<String>>>() {
					@Override
					public Map<String, List<String>> load(String key) throws Exception {
						return new HashMap<String, List<String>>();
					}
				});
	}
	
	/**
	 * 取得之前的ScratchCardDetail
     */
	public ScratchCardDetail getPreScratchCardDetailId(String gameId) {
		//String preScratchCardDetailId;
		
		ScratchCardDetail scratchCardDetail = sratchCardDetailRepository.findOneByGameId(gameId);
		
		//preScratchCardDetailId = scratchCardDetail.getScratchCardDetailId();
		
    	return scratchCardDetail;
    }
	
	/**
	 * refresh
     */
	public void refresh(String gameId){    	
    	dataCache.refresh(gameId);
	}
	
	/**
	 * 新增ScratchCardDetail
     */
    @Transactional(rollbackFor=Exception.class)
	public void createScratchCardDetail(ScratchCardDetail scratchCardDetail){    	
    	sratchCardDetailRepository.save(scratchCardDetail);
    	
    	//dataCache.refresh(scratchCardDetail.getGameId());
	}
    
    /**
	 * 取得遊戲
     */
    @SuppressWarnings("unchecked")
	public GameModel getScratchCard(String gameId) {
		try {
			/*Map<String, List<String>> result = dataCache.get(gameId);
			if(result != null && result.get(gameId) != null){
				return result;
			}*/
		} catch (Exception e) {}
		
    	String queryString = 
    			"SELECT BCS_CONTENT_GAME.GAME_ID, "
    				+ "BCS_CONTENT_GAME.GAME_NAME,"
    				+ "BCS_CONTENT_GAME.GAME_CONTENT,"
    				+ "BCS_CONTENT_GAME.HEADER_IMAGE_ID,"
    				+ "BCS_CONTENT_GAME.FOOTER_IMAGE_ID,"
    				+ "BCS_SCRATCHCARD_DETAIL.SCRATCHCARD_BACKGROUND_IMAGE_ID,"
    				+ "BCS_SCRATCHCARD_DETAIL.SCRATCHCARD_FRONT_IMAGE_ID, "
    				+ "BCS_SCRATCHCARD_DETAIL.SCRATCHCARD_START_BUTTON_IMAGE_ID "
    				/*+ "BCS_SCRATCHCARD_DETAIL.SCRATCHCARD_FRONT_IMAGE_ID,"
    				+ "BCS_CONTENT_PRIZE.PRIZE_NAME,"
    				+ "BCS_CONTENT_PRIZE.PRIZE_IMAGE_ID,"
    				+ "BCS_CONTENT_PRIZE.PRIZE_CONTENT,"
    				+ "BCS_CONTENT_PRIZE.PRIZE_QUANTITY,"
    				+ "BCS_CONTENT_PRIZE.PRIZE_PROBABILITY, "
    				+ "BCS_CONTENT_PRIZE.PRIZE_ID, "
    				+ "BCS_CONTENT_PRIZE.IS_CONSOLATION_PRIZE, "
    				+ "BCS_MSG_DETAIL.TEXT "*/
    			+ "FROM BCS_CONTENT_GAME "
    				//+ "LEFT JOIN BCS_CONTENT_PRIZE ON BCS_CONTENT_GAME.GAME_ID = BCS_CONTENT_PRIZE.GAME_ID "
    				+ "LEFT JOIN BCS_SCRATCHCARD_DETAIL ON BCS_CONTENT_GAME.GAME_ID = BCS_SCRATCHCARD_DETAIL.GAME_ID "
    				//+ "LEFT JOIN BCS_MSG_DETAIL ON BCS_MSG_DETAIL.MSG_DETAIL_ID = BCS_CONTENT_PRIZE.MSG_DETAIL_ID "
    			//+ "WHERE BCS_CONTENT_GAME.GAME_ID = ?1 AND BCS_CONTENT_GAME.STATUS <> 'DELETE' AND BCS_CONTENT_PRIZE.STATUS <> 'DELETE' "
    			+ "WHERE BCS_CONTENT_GAME.GAME_ID = ?1 AND BCS_CONTENT_GAME.STATUS <> 'DELETE' ";
    			//+ "ORDER BY BCS_CONTENT_PRIZE.PRIZE_LETTER";
    	
    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, gameId);
		List<Object[]> list = query.getResultList();
		GameModel gameModel = new GameModel();
		List<CouponModel> couponList = new ArrayList<CouponModel>();
		Object[] o;
		
		for(int i = 0; i<list.size(); i++){
			o = list.get(i);
			
			if(i == 0){
				gameModel.setGameId(o[0].toString());
				gameModel.setGameName(o[1].toString());
				gameModel.setGameContent(o[2].toString());
				gameModel.setHeaderImageId(o[3].toString());
				gameModel.setFooterImageId(o[4].toString());
				gameModel.setScratchcardBackgroundImageId(o[5].toString());
				gameModel.setScratchcardFrontImageId(o[6].toString());
				gameModel.setScratchcardStartButtonImageId((o[7].toString() != null) ? o[7].toString() : null);
				
				String findCouponQuery = "SELECT COUPON_ID, COUPON_TITLE, COUPON_LIST_IMAGE_ID, COUPON_DESCRIPTION, COUPON_USE_DESC, COUPON_RULE_DESC, PROBABILITY, IDENTITY_LETTER "
						+ "FROM BCS_CONTENT_COUPON "
						+ "WHERE EVENT_REFERENCE = ?1 AND EVENT_REFERENCE_ID = ?2 "
						+ "ORDER BY IDENTITY_LETTER ASC";
				
				query =  entityManager.createNativeQuery(findCouponQuery);
				query.setParameter(1, ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD);
				query.setParameter(2, gameId);
				
				List<Object[]> couponObjectList = query.getResultList();
				Object[] couponObject;
				for(int index = 0; index < couponObjectList.size(); index++) {
					CouponModel coupon = new CouponModel();
					couponObject = couponObjectList.get(index);
					
					coupon.setCouponId(couponObject[0].toString());
					coupon.setCouponTitle(couponObject[1].toString());
					coupon.setCouponListImageId(couponObject[2].toString());
					coupon.setCouponDescription(couponObject[3].toString());
					coupon.setCouponUseDescription(couponObject[4].toString());
					coupon.setCouponRuleDescription(couponObject[5].toString());
					coupon.setProbability(new BigDecimal(couponObject[6].toString()));
					coupon.setIdentityLetter(couponObject[7].toString());
					
					couponList.add(coupon);
				}
			}
		}
		
		gameModel.setCouponList(couponList);
		
    	logger.debug("◎ 刮刮卡物件：" + gameModel);
		/*if(map != null){
			dataCache.put(gameId, map);
		}*/
		
		return gameModel;
    }
    
    
    
    /**
	 *  檢查有無重覆使用到UUID
     */
    public Boolean checkDuplicateUUID(String queryType, String uuid) {
    	if(queryType == "1"){
    		ContentGame contentGame = contentGameRepository.findOne(uuid);
    		if (contentGame == null) return false;
    	} else if (queryType == "2") {
    		ContentPrize contentPrize = contentPrizeRepository.findOne(uuid);
    		if (contentPrize == null) return false;
    	} else if (queryType == "3") {
    		ScratchCardDetail scratchCardDetail = sratchCardDetailRepository.findOne(uuid);
    		if (scratchCardDetail == null) return false;
    	}
    	
		return true;
    }
}
