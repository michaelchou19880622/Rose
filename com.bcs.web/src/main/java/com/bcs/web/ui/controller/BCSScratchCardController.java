package com.bcs.web.ui.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

// import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentGame;
import com.bcs.core.db.entity.ScratchCardDetail;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.service.ContentGameService;
import com.bcs.core.db.service.ScratchCardDetailService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.model.GameModel;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.model.PrizeModel;
import com.bcs.web.ui.model.ScratchCardModel;

@Controller
@RequestMapping("/bcs")
public class BCSScratchCardController {
	@Autowired
	private ContentGameService contentGameService;
	
	@Autowired
	private ScratchCardDetailService scratchCardDetailService;
	
	@Autowired
	private ContentCouponService contentCouponService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSScratchCardController.class);
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/gameCreatePage/scratchCard")
	public String ScratchCardCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("scratchCardCreatePage");
		return BcsPageEnum.ScratchCardCreatePage.toString();
	}
	
	/**
	 * 新增與更新刮刮卡
	 */
	@ControllerLog(description="新增與更新刮刮卡")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/createGame/scratchCard", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createGame(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestBody ScratchCardModel createScratchCardModel, 
			@RequestParam String actionType, 
			@RequestParam String gameId) throws IOException {
		logger.info("Create scratch card.");
		try {
			/*if (!validateData(createScratchCardModel)) {
				throw new BcsNoticeException("資料不合法！");
			}*/			
			String adminUserAccount = customUser.getAccount();
			
			// Integer couponListCount = 0;
			ContentGame contentGame = null;
			ScratchCardDetail scratchCardDetail = null;
			
			if (actionType.equals("Edit")) { //變更
				// List<ContentCoupon> targetCouponList;
				
				contentGame = contentGameService.findOne(gameId);
				// contentGameService.deleteGame(gameId, adminUserAccount);
				scratchCardDetail = scratchCardDetailService.getPreScratchCardDetailId(gameId);
				// targetCouponList = contentCouponService.findByEventReferenceAndEventReferenceId(ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD, gameId);
				// contentCouponService.resetCouponEvent(targetCouponList);
			} else { //新增與複制
				gameId = checkDuplicateUUID("1");
				contentGame = new ContentGame();
				scratchCardDetail = new ScratchCardDetail();
				
				contentGame.setGameId(gameId);				
				scratchCardDetail.setScratchCardDetailId(checkDuplicateUUID("3"));
				
				logger.info(">> Generate game id: " + gameId);
			}
			
			contentGame.setGameName(createScratchCardModel.getGameName());
			contentGame.setGameContent(createScratchCardModel.getGameContent());
			contentGame.setModifyUser(adminUserAccount);
			contentGame.setModifyTime(new Date());
			contentGame.setStatus(ContentGame.STATUS_ACTIVE);
			contentGame.setGameType(createScratchCardModel.getGameType());
			contentGame.setHeaderImageId(createScratchCardModel.getHeaderImageId());
			contentGame.setFooterImageId(createScratchCardModel.getFooterImageId());
			
			String savedGameId = contentGameService.createGame(contentGame).getGameId();
			
			scratchCardDetail.setGameId(savedGameId);
			scratchCardDetail.setScratchCardBGImageId(createScratchCardModel.getScratchCardBGImageId());
			scratchCardDetail.setScratchCardFrontImageId(createScratchCardModel.getScratchCardFrontImageId());
			scratchCardDetail.setScratchCardStartButtonImageId(createScratchCardModel.getScratchCardStartButtonImageId());
			
			/* 設定此遊戲中所有優惠券的 EVENT_REFERENCE 跟 EVENT_REFERENCE_ID */
			List<Object> couponList = createScratchCardModel.getCouponList();
			for(Object coupon : couponList) {
				contentCouponService.setCouponEvent(coupon, savedGameId, "ScratchCard");
			}
			
			scratchCardDetailService.createScratchCardDetail(scratchCardDetail);
			
			return new ResponseEntity<>("save success", HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	/**
	 * 取得刮刮卡
	 */
	@ControllerLog(description="取得刮刮卡")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getGame/scratchCard/{gameId}")
	@ResponseBody
	public ResponseEntity<?> getScratchCard(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String gameId) throws IOException {
		logger.info("getScratchCard");
				
		try{
			GameModel result = scratchCardDetailService.getScratchCard(gameId);

			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/** 
	 * 檢查必填欄位不可為空
	 */
	public Boolean validateData(ScratchCardModel createScratchCardModel) {
		if(createScratchCardModel.getGameName().isEmpty()){
			return false;
		}
		if(createScratchCardModel.getGameContent().isEmpty()){
			return false;
		}
		if(createScratchCardModel.getScratchCardBGImageId().isEmpty()){
			return false;
		}
		if(createScratchCardModel.getScratchCardFrontImageId().isEmpty()){
			return false;
		}
		
		List<PrizeModel> prizes = createScratchCardModel.getPrizes();
		PrizeModel prizeModel;
		
		for(int i = 0; i < prizes.size(); i++){
			prizeModel = prizes.get(i);
			if(prizeModel.getPrizeName().isEmpty()){
				return false;
			}
			if(prizeModel.getPrizeContent().isEmpty()){
				return false;
			}
			if(prizeModel.getPrizeImageId().isEmpty()){
				return false;
			}
			if(prizeModel.getPrizeQuantity() == null && !prizeModel.getIsConsolationPrize()){
				return false;
			}
			if(prizeModel.getPrizeProbability().isEmpty()){
				return false;
			}
			if(prizeModel.getMessage().isEmpty()){
				return false;
			}
		}
		
		BigDecimal totalProbability = new BigDecimal("0");
		
		for(int i = 0; i < prizes.size(); i++){
			prizeModel = prizes.get(i);
			totalProbability = totalProbability.add(new BigDecimal(prizeModel.getPrizeProbability()));
		}
		
		if(totalProbability.compareTo(new BigDecimal("100.00")) != 0){
			return false;
		}
		
		return true;
	}
	
	/** 
	 * 回傳一個沒有重覆的uuid
	 */
	public String checkDuplicateUUID(String queryType) {
		String uuid = UUID.randomUUID().toString().toLowerCase();
		Boolean duplicateUUID = scratchCardDetailService.checkDuplicateUUID(queryType, uuid);
		while (duplicateUUID) {
			uuid = UUID.randomUUID().toString().toLowerCase();
			duplicateUUID = scratchCardDetailService.checkDuplicateUUID(queryType, uuid);
		}
		
		return uuid;
	}
}
