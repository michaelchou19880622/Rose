package com.bcs.core.db.service;

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
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentGame;
import com.bcs.core.db.entity.ContentPrize;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.PrizeList;
import com.bcs.core.db.repository.ContentGameRepository;
import com.bcs.core.db.repository.ContentPrizeRepository;
import com.bcs.core.db.repository.MsgDetailRepository;
import com.bcs.core.db.repository.PrizeListRepository;
import com.bcs.core.exception.BcsNoticeException;
//import com.bcs.core.model.CouponModel;
import com.bcs.core.model.GameModel;
//import com.bcs.core.model.PrizeModel;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ObjectUtil;

@Service
public class ContentGameService {
	@Autowired
	private ContentGameRepository contentGameRepository;
	@Autowired
	private ContentPrizeRepository contentPrizeRepository;
	@Autowired
	private PrizeListRepository prizeListRepository;
	@Autowired
	private MsgDetailRepository msgDetailRepository;
	@Autowired
	private ContentCouponService contentCouponService;
	@Autowired
	private ActionUserCouponService actionUserCouponService;

	@PersistenceContext
	EntityManager entityManager;

	/** Logger */
	private static Logger logger = Logger.getLogger(ContentGameService.class);

	public ContentGameService() {
	}

	public ContentGame createGame(ContentGame contentGame) {
		return contentGameRepository.save(contentGame);
	}

	/**
	 * 取得之前的GameId
	 */
	public String getPreGameId(String gameId) {
		String preGameId;

		ContentGame contentGame = contentGameRepository.findOne(gameId);

		preGameId = contentGame.getGameId();
		contentGame.setStatus(ContentGame.STATUS_DELETE);
		contentGameRepository.save(contentGame);

		return preGameId;
	}

	/**
	 * 取得ContentGame
	 */
	public ContentGame findOne(String gameId) {
		ContentGame contentGame = contentGameRepository.findOne(gameId);

		return contentGame;
	}

	/**
	 * 取得之前的PrizeIds
	 */
	public List<String> getPrePrizeIds(String gameId) {
		List<String> list = new ArrayList<>();
		;

		List<ContentPrize> contentPrizes = contentPrizeRepository.findByGameId(gameId);

		for (ContentPrize contentPrize : contentPrizes) {
			list.add(contentPrize.getPrizeId());

			contentPrize.setStatus(ContentPrize.STATUS_DELETE);
			contentPrizeRepository.save(contentPrize);
		}

		return list;
	}

	/**
	 * 新增遊戲
	 * 
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class)
	public void createGame(ContentGame contentGame, List<ContentPrize> contentPrizes, List<MsgDetail> msgDetails) throws Exception {
		contentGameRepository.save(contentGame);

		PrizeList prizeList = null;

		ContentPrize contentPrize;
		MsgDetail msgDetail;

		for (int index = 0; index < contentPrizes.size(); index++) {
			contentPrize = contentPrizes.get(index);
			List<PrizeList> prizeLists = prizeListRepository.findByPrizeId(contentPrize.getPrizeId());

			if (!contentPrize.getIsConsolationPrize()) {
				if (contentPrize.getPrizeQuantity() < prizeLists.size()) {
					throw new BcsNoticeException("獎品數量不可低於原本數量！");
				}
				for (int j = 0; j < contentPrize.getPrizeQuantity(); j++) {
					if (j + 1 > prizeLists.size()) {
						prizeList = new PrizeList();
						prizeList.setPrizeId(contentPrize.getPrizeId());
						prizeList.setCreateTime(new Date());
						prizeList.setStatus(PrizeList.PRIZE_STATUS_NOT_WINNED);
						prizeList.setGameId(contentPrize.getGameId());

						prizeListRepository.save(prizeList);
					}
				}
			} else {
				prizeList = new PrizeList();
				prizeList.setPrizeId(contentPrize.getPrizeId());
				prizeList.setCreateTime(new Date());
				prizeList.setStatus(PrizeList.PRIZE_STATUS_NOT_WINNED);
				prizeList.setGameId(contentPrize.getGameId());
				prizeListRepository.save(prizeList);
			}

			msgDetail = msgDetails.get(index);
			msgDetail = msgDetailRepository.save(msgDetail);

			contentPrize.setDetailId(msgDetail.getDetailId());
			contentPrizeRepository.save(contentPrize);
		}
	}

	/**
	 * 取得所有遊戲清單
	 */
	@SuppressWarnings("unchecked")
	public List<GameModel> getAllContentGame() {
		String queryString = "SELECT BCS_CONTENT_GAME.GAME_ID, " + "BCS_CONTENT_GAME.GAME_NAME, "
				+ "BCS_CONTENT_GAME.GAME_CONTENT, " + "BCS_CONTENT_GAME.GAME_TYPE, " + "BCS_CONTENT_GAME.MODIFY_TIME, "
				+ "BCS_CONTENT_GAME.MODIFY_USER " + "FROM BCS_CONTENT_GAME "
				+ "WHERE BCS_CONTENT_GAME.STATUS <> 'DELETE' " + "ORDER BY BCS_CONTENT_GAME.MODIFY_TIME DESC";

		Query query = entityManager.createNativeQuery(queryString);
		List<Object[]> list = query.getResultList();

		List<GameModel> gameModels = new ArrayList<>();
		GameModel gameModel;

		for (Object[] o : list) {
			gameModel = new GameModel();

			gameModel.setGameId(o[0].toString());
			gameModel.setGameName(o[1].toString());
			gameModel.setGameContent(o[2].toString());
			gameModel.setGameType(o[3].toString());
			gameModel.setModifyTime(o[4].toString());
			gameModel.setModifyUserName(o[5].toString());

			gameModels.add(gameModel);
		}

		logger.debug(gameModels);

		return gameModels;
	}

	/**
	 * 取得獎品
	 */
	public List<ContentCoupon> getPrizeList(String gameId) {
		/*
		 * String queryString = "SELECT BCS_CONTENT_PRIZE.PRIZE_ID, " +
		 * "BCS_CONTENT_PRIZE.PRIZE_NAME, " + "BCS_CONTENT_PRIZE.PRIZE_QUANTITY, " +
		 * "BCS_CONTENT_PRIZE.IS_CONSOLATION_PRIZE " + "FROM BCS_CONTENT_PRIZE " +
		 * "WHERE BCS_CONTENT_PRIZE.GAME_ID = ?1 " +
		 * "ORDER BY BCS_CONTENT_PRIZE.PRIZE_LETTER";
		 * 
		 * Query query = entityManager.createNativeQuery(queryString).setParameter(1,
		 * gameId); List<Object[]> list = query.getResultList();
		 * 
		 * List<PrizeModel> prizeModels = new ArrayList<>(); PrizeModel prizeModel;
		 * 
		 * for (Object[] o : list) { prizeModel = new PrizeModel();
		 * 
		 * prizeModel.setPrizeId(o[0].toString());
		 * prizeModel.setPrizeName(o[1].toString()); prizeModel.setPrizeQuantity(o[2] ==
		 * null? null : Integer.parseInt(o[2].toString()));
		 * prizeModel.setIsConsolationPrize(Boolean.parseBoolean(o[3].toString()));
		 * 
		 * queryString = "SELECT BCS_WINNER_LIST.WINNER_LIST_ID " +
		 * "FROM BCS_WINNER_LIST " +
		 * "LEFT JOIN BCS_PRIZE_LIST ON BCS_PRIZE_LIST.PRIZE_LIST_ID = BCS_WINNER_LIST.PRIZE_LIST_ID "
		 * + "WHERE BCS_PRIZE_LIST.PRIZE_ID = ?1"; String prizeId = o[0].toString();
		 * query = entityManager.createNativeQuery(queryString).setParameter(1,
		 * prizeId); list = query.getResultList();
		 * prizeModel.setWinnedCount(list.size());
		 * 
		 * prizeModels.add(prizeModel); }
		 * 
		 * logger.debug(prizeModels);
		 * 
		 * return prizeModels;
		 */
		return contentCouponService.findByEventReferenceAndEventReferenceId(ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD,
				gameId);
	}

	/**
	 * 刪除遊戲
	 */
	@Transactional(rollbackFor = Exception.class)
	public void deleteGame(String gameId, String account) {
		// 只改變狀態
		ContentGame contentGame = contentGameRepository.findOne(gameId);

		contentGame.setStatus(ContentGame.STATUS_DELETE);
		contentGame.setModifyUser(account);
		contentGame.setModifyTime(new Date());
		contentGameRepository.save(contentGame);
	}
    
    public Map<String, String> findGameNameMap(){
        List<Object[]> games = contentGameRepository.findAllGameIdAndGameName();
        logger.debug("findGroupTitleMap:" + ObjectUtil.objectToJsonStr(games));
        Map<String, String> result = new LinkedHashMap<String, String>();
        
        for(Object[] game : games){
            String gameId = (String) game[0];
            String gameName = (String) game[1];
            result.put(gameId, gameName);
        }
        
        return result;
    }
    
    /**
     * 判斷該使用者是否刮完刮刮樂
     */
    public String getScratchedOffCouponId(String gameId, String UID) {
    	String scratchedOffCouponId = null;
    	List<ContentCoupon> contentCouponList = contentCouponService.findByEventReferenceAndEventReferenceId(ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD, gameId);
    	
    	for(ContentCoupon contentCoupon : contentCouponList) {
    		ActionUserCoupon actionUserCoupon = actionUserCouponService.findByMidAndCouponIdAndActionType(UID, contentCoupon.getCouponId(), ActionUserCoupon.ACTION_TYPE_GET);
    		if(actionUserCoupon != null)
    			scratchedOffCouponId = actionUserCoupon.getCouponId();
    	}
    	return scratchedOffCouponId;
    }
    
    /*
     * 將刮刮卡資訊轉譯成網址
     */
    public String tranferURI(String uri, String uid) {
		return UriHelper.parseBcsPage(uri, uid);
	}
}
