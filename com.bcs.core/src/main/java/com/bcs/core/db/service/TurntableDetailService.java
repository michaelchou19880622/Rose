package com.bcs.core.db.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import com.bcs.core.db.entity.ContentGame;
import com.bcs.core.db.entity.ContentPrize;
import com.bcs.core.db.entity.TurntableDetail;
//import com.bcs.core.db.repository.ContentGameRepository;
import com.bcs.core.db.repository.ContentPrizeRepository;
import com.bcs.core.db.repository.TurntableDetailRepository;
import com.bcs.core.model.GameModel;
import com.bcs.core.model.PrizeModel;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class TurntableDetailService {
	public static final String TURNTABLE_SYNC = "TURNTABLE_SYNC";
	/* @Autowired
	private ContentGameRepository contentGameRepository; */
	
	@Autowired
	private ContentPrizeRepository contentPrizeRepository;
	
	@Autowired
	private TurntableDetailRepository turntableDetailRepository;
	
	@PersistenceContext
    EntityManager entityManager;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(TurntableDetailService.class);
	
	protected LoadingCache<String, GameModel> dataCache;

	private Timer flushTimer = new Timer();
	
	private class CustomTask extends TimerTask{
		
		@Override
		public void run() {

			try{
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(TURNTABLE_SYNC);
				if(isReSyncData){
					dataCache.invalidateAll();
					DataSyncUtil.syncDataFinish(TURNTABLE_SYNC);
				}
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}
	
	public TurntableDetailService(){

		flushTimer.schedule(new CustomTask(), 120000, 30000);

		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, GameModel>() {
					@Override
					public GameModel load(String key) throws Exception {
						return new GameModel();
					}
				});
	}
	
	/**
	 * 取得之前的TurntableDetailId
     */
	public String getPreTurntableDetailId(String gameId) {
		String preTurntableDetailId;
		
		TurntableDetail turntableDetail = turntableDetailRepository.findOneByGameId(gameId);
		
		preTurntableDetailId = turntableDetail.getTurntableDetailId();
		
    	return preTurntableDetailId;
    }
	
	/**
	 * refresh
     */
	public void refresh(String gameId){    	
    	dataCache.refresh(gameId);
		DataSyncUtil.settingReSync(TURNTABLE_SYNC);
	}
	
	/**
	 * 新增TurntableDetail
     */
    @Transactional(rollbackFor=Exception.class)
	public void createTurntableDetail(TurntableDetail turntableDetail){    	
    	turntableDetailRepository.save(turntableDetail);
    	
    	//dataCache.refresh(turntableDetail.getGameId());
		DataSyncUtil.settingReSync(TURNTABLE_SYNC);
	}
	
	private boolean notNull(GameModel result){
		if(result != null && result.getGameId() != null){
			return true;
		}
		return false;
	}
	
    /**
	 * 取得遊戲
     */
    @SuppressWarnings("unchecked")
	public GameModel getTurntable(Long gameId) {
		try {
			GameModel result = dataCache.get(gameId.toString());
			if(notNull(result)){
				return result;
			}
		} catch (Exception e) {}
		
		int gameSize = 12;
		
    	String queryString = 
    			"SELECT BCS_CONTENT_GAME.GAME_ID, "
    				+ "BCS_CONTENT_GAME.GAME_NAME,"
    				+ "BCS_CONTENT_GAME.GAME_CONTENT,"
    				+ "BCS_CONTENT_GAME.HEADER_IMAGE_ID,"
    				+ "BCS_CONTENT_GAME.FOOTER_IMAGE_ID,"
    				+ "BCS_TURNTABLE_DETAIL.TURNTABLE_IMAGE_ID,"
    				+ "BCS_TURNTABLE_DETAIL.TURNTABLE_BACKGROUND_IMAGE_ID,"
    				+ "BCS_TURNTABLE_DETAIL.POINTER_IMAGE_ID,"
    				+ "BCS_CONTENT_GAME.SHARE_IMAGE_ID,"
    				+ "BCS_CONTENT_GAME.SHARE_MSG,"
    				+ "BCS_CONTENT_GAME.GAME_PROCESS,"
    				+ "BCS_CONTENT_GAME.GAME_LIMIT_COUNT,"
    				+ "BCS_CONTENT_GAME.SHARE_SMALL_IMAGE_ID,"
    				
    				+ "BCS_CONTENT_PRIZE.PRIZE_NAME,"
    				+ "BCS_CONTENT_PRIZE.PRIZE_IMAGE_ID,"
    				+ "BCS_CONTENT_PRIZE.PRIZE_CONTENT,"
    				+ "BCS_CONTENT_PRIZE.PRIZE_QUANTITY,"
    				+ "BCS_CONTENT_PRIZE.PRIZE_PROBABILITY, "
    				+ "BCS_CONTENT_PRIZE.PRIZE_ID, "
    				+ "BCS_CONTENT_PRIZE.IS_CONSOLATION_PRIZE, "
    				
    				+ "BCS_MSG_DETAIL.TEXT "
    			+ "FROM BCS_CONTENT_GAME "
    				+ "LEFT JOIN BCS_CONTENT_PRIZE ON BCS_CONTENT_GAME.GAME_ID = BCS_CONTENT_PRIZE.GAME_ID "
    				+ "LEFT JOIN BCS_TURNTABLE_DETAIL ON BCS_CONTENT_GAME.GAME_ID = BCS_TURNTABLE_DETAIL.GAME_ID "
    				+ "LEFT JOIN BCS_MSG_DETAIL ON BCS_MSG_DETAIL.MSG_DETAIL_ID = BCS_CONTENT_PRIZE.MSG_DETAIL_ID "
    			+ "WHERE BCS_CONTENT_GAME.GAME_ID = ?1 AND BCS_CONTENT_GAME.STATUS <> 'DELETE' AND BCS_CONTENT_PRIZE.STATUS <> 'DELETE' "
    			+ "ORDER BY BCS_CONTENT_PRIZE.PRIZE_LETTER";
    	
    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, gameId);
		List<Object[]> list = query.getResultList();

		GameModel gameModel = new GameModel();
		List<PrizeModel> prizeModels = new ArrayList<>();
		Object[] o;
		
		for(int i = 0; i<list.size(); i++){
			o = list.get(i);
			
			if(i == 0){
				gameModel.setGameId(o[0].toString());
				gameModel.setGameName(o[1].toString());
				gameModel.setGameContent(o[2].toString());
				gameModel.setHeaderImageId(o[3].toString());
				gameModel.setFooterImageId(o[4].toString());
				gameModel.setTurntableImageId(o[5].toString());
				gameModel.setTurntableBackgroundImageId(o[6].toString());
				gameModel.setPointerImageId(o[7].toString());
				gameModel.setShareImageId(toString(o[8]));
				gameModel.setShareMsg(o[9].toString());
				gameModel.setGameProcess(toString(o[10]));
				if(StringUtils.isNotBlank(toString(o[11]))){
					gameModel.setGameLimitCount(Integer.parseInt(toString(o[11])));
				}
				gameModel.setShareSmallImageId(toString(o[12]));
			}
			PrizeModel prizeModel = new PrizeModel();
			prizeModel.setPrizeName(o[gameSize+1].toString());
			prizeModel.setPrizeImageId(o[gameSize+2].toString());
			prizeModel.setPrizeContent(o[gameSize+3].toString());
			prizeModel.setPrizeQuantity(Integer.parseInt(o[gameSize+4].toString()));
			prizeModel.setPrizeProbability(new BigDecimal(o[gameSize+5].toString()));
			prizeModel.setPrizeId(o[gameSize+6].toString());
			prizeModel.setIsConsolationPrize(Boolean.parseBoolean(o[gameSize+7].toString()));
			prizeModel.setMessageText(o[gameSize+8].toString());
				
			prizeModels.add(prizeModel);
		}
		
		gameModel.setPrizes(prizeModels);
		
    	logger.debug(gameModel);
		if(notNull(gameModel)){
			dataCache.put(gameId.toString(), gameModel);
		}
		
		return gameModel;
    }
    
    public String toString(Object o){
    	if(o != null){
    		return o.toString();
    	}
    	return "";
    }
    
	/**
	 *  檢查有無重覆使用到UUID
     */
    public Boolean checkDuplicateUUID(String queryType, String uuid) {
    	/*if(queryType == "1"){
    		ContentGame contentGame = contentGameRepository.findOne(uuid);
    		if (contentGame == null) return false;
    	} else */if (queryType == "2") {
    		ContentPrize contentPrize = contentPrizeRepository.findOne(uuid);
    		if (contentPrize == null) return false;
    	} else if (queryType == "3") {
    		TurntableDetail turntableDetail = turntableDetailRepository.findOne(uuid);
    		if (turntableDetail == null) return false;
    	}
    	
		return true;
    }
}
