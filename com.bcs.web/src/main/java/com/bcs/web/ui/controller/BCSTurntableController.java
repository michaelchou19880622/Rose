package com.bcs.web.ui.controller;

import java.io.IOException;
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

// import com.bcs.core.db.service.ContentGameService;
import com.bcs.core.db.service.TurntableDetailService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.model.GameModel;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.model.TurntableModel;
import com.bcs.web.ui.service.ContentGameUIService;

@Controller
@RequestMapping("/bcs")
public class BCSTurntableController {
	/* @Autowired
	private ContentGameService contentGameService; */
	@Autowired
	private TurntableDetailService turntableDetailService;
	@Autowired
	private ContentGameUIService contentGameUIService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSTurntableController.class);
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/gameCreatePage/turntable")
	public String TurntableCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("turntableCreatePage");
		return BcsPageEnum.TurntableCreatePage.toString();
	}
	
	/**
	 * 新增與更新轉盤遊戲
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/edit/createGame/turntable", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createGame(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestBody TurntableModel createTurntableModel, @RequestParam String actionType, @RequestParam String gameId) throws IOException {		
		try {
			return contentGameUIService.createGameDo(request, response, customUser, createTurntableModel, actionType, gameId);
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
	 * 取得轉盤遊戲
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getGame/turntable/{gameId}")
	@ResponseBody
	public ResponseEntity<?> getTurntable(HttpServletRequest request, HttpServletResponse response,
			@PathVariable Long gameId) throws IOException {
		logger.info("getTurntable");
				
		try{
			GameModel result = turntableDetailService.getTurntable(gameId);

			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
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
