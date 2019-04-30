package com.bcs.web.ui.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentGame;
import com.bcs.core.db.entity.ContentPrize;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.TurntableDetail;
import com.bcs.core.db.service.ContentGameService;
import com.bcs.core.db.service.TurntableDetailService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.web.security.CustomUser;
import com.bcs.web.ui.model.PrizeModel;
import com.bcs.web.ui.model.TurntableModel;

@Service
public class ContentGameUIService {

	@Autowired
	private ContentGameService contentGameService;
	@Autowired
	private TurntableDetailService turntableDetailService;

    @Transactional(rollbackFor=Exception.class)
	public ResponseEntity<?> createGameDo(HttpServletRequest request, HttpServletResponse response,
			CustomUser customUser,
			TurntableModel createTurntableModel, String actionType, String gameId) throws Exception {	
		if (!validateData(createTurntableModel)) {
			throw new BcsNoticeException("資料不合法！");
		}
		
		String adminUserAccount = customUser.getAccount();
		String turntableDetailId = "";
		
		List<PrizeModel> prizes = new ArrayList<>();
		List<String> prePrizeIds = new ArrayList<>();
		PrizeModel prizeModel;
		
		ContentGame contentGame = new ContentGame();
		TurntableDetail turntableDetail = new TurntableDetail();
		ContentPrize contentPrize;
		MsgDetail msgDetail;
		List<ContentPrize> contentPrizes = new ArrayList<>();
		List<MsgDetail> msgDetails = new ArrayList<>();
		
		if (actionType.equals("Edit")) { //變更
			gameId = contentGameService.getPreGameId(gameId);
			turntableDetailId = turntableDetailService.getPreTurntableDetailId(gameId);
		} else { //新增與複制
			//gameId = checkDuplicateUUID("1");
			turntableDetailId = checkDuplicateUUID("3");
		}
		
		contentGame.setGameId(gameId);
		contentGame.setGameName(createTurntableModel.getGameName());
		contentGame.setGameContent(createTurntableModel.getGameContent());
		contentGame.setModifyUser(adminUserAccount);
		contentGame.setModifyTime(new Date());
		contentGame.setStatus(ContentGame.STATUS_ACTIVE);
		contentGame.setGameType(createTurntableModel.getGameType());
		contentGame.setHeaderImageId(createTurntableModel.getHeaderImageId());
		contentGame.setFooterImageId(createTurntableModel.getFooterImageId());
//		contentGame.setShareMsg(createTurntableModel.getShareMsg());
//
//		contentGame.setShareImageId(createTurntableModel.getShareImageId());
//		contentGame.setShareSmallImageId(createTurntableModel.getShareSmallImageId());
//		contentGame.setGameProcess(createTurntableModel.getGameProcess());
//		contentGame.setGameLimitCount(createTurntableModel.getGameLimitCount());
		
		turntableDetail.setTurntableDetailId(turntableDetailId);
		turntableDetail.setGameId(gameId);
		turntableDetail.setTurntableBGImageId(createTurntableModel.getTurntableBGImageId());
		turntableDetail.setTurntableImageId(createTurntableModel.getTurntableImageId());
		turntableDetail.setPointerImageId(createTurntableModel.getPointerImageId());
		
		
		prizes = createTurntableModel.getPrizes();
		
		if (actionType.equals("Edit")) { //變更
			prePrizeIds = contentGameService.getPrePrizeIds(gameId);
		} else { //新增與複制
			for(int i = 0; i < prizes.size(); i++){
				prePrizeIds.add(checkDuplicateUUID("2"));
			}
		}
					
		for(int i = 0; i < prizes.size(); i++){
			prizeModel = prizes.get(i);
			
			if (i+1 > prePrizeIds.size()) { //新增獎品
				prePrizeIds.add(checkDuplicateUUID("2"));
			}
			
			contentPrize = new ContentPrize();
			contentPrize.setPrizeId(prePrizeIds.get(i));
			contentPrize.setGameId(gameId);
			contentPrize.setPrizeName(prizeModel.getPrizeName());
			contentPrize.setPrizeContent(prizeModel.getPrizeContent());
			contentPrize.setPrizeImageId(prizeModel.getPrizeImageId());
			contentPrize.setPrizeLetter(prizeModel.getPrizeLetter());
			contentPrize.setPrizeQuantity(prizeModel.getPrizeQuantity());
			contentPrize.setStatus(ContentPrize.STATUS_ACTIVE);
			contentPrize.setPrizeProbability(new BigDecimal(prizeModel.getPrizeProbability()));
			contentPrize.setIsConsolationPrize(prizeModel.getIsConsolationPrize());

			msgDetail = new MsgDetail();
			msgDetail.setMsgType("TEXT");
			msgDetail.setText(prizeModel.getMessage());
			
			contentPrizes.add(contentPrize);
			msgDetails.add(msgDetail);
		}
		
		contentGameService.createGame(contentGame, contentPrizes, msgDetails);
		turntableDetailService.createTurntableDetail(turntableDetail);
		
		return new ResponseEntity<>("save success", HttpStatus.OK);
	}
	
	/** 
	 * 檢查必填欄位不可為空
	 */
	public Boolean validateData(TurntableModel createTurntableModel) {
		if(createTurntableModel.getGameName().isEmpty()){
			return false;
		}
		if(createTurntableModel.getGameContent().isEmpty()){
			return false;
		}
		if(createTurntableModel.getTurntableBGImageId().isEmpty()){
			return false;
		}
		if(createTurntableModel.getTurntableImageId().isEmpty()){
			return false;
		}
		if(createTurntableModel.getPointerImageId().isEmpty()){
			return false;
		}
		if(createTurntableModel.getShareMsg().isEmpty()){
			return false;
		}
		
		List<PrizeModel> prizes = createTurntableModel.getPrizes();
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
			if(prizeModel.getPrizeQuantity() == null){
				return false;
			}
			if(prizeModel.getPrizeProbability().isEmpty()){
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
		Boolean duplicateUUID = turntableDetailService.checkDuplicateUUID(queryType, uuid);
		while (duplicateUUID) {
			uuid = UUID.randomUUID().toString().toLowerCase();
			duplicateUUID = turntableDetailService.checkDuplicateUUID(queryType, uuid);
		}
		
		return uuid;
	}
}
