package com.bcs.web.ui.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.service.ContentGameService;
import com.bcs.core.db.service.WinnerListService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.model.GameModel;
import com.bcs.core.model.WinnedCouponModel;
import com.bcs.core.model.WinnerModel;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.utils.QrcodeGenerator;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.model.SendMsgModel;
import com.bcs.web.ui.service.SendMsgUIService;

@Controller
@RequestMapping("/bcs")
public class BCSGameController {
	@Autowired
	private ContentGameService contentGameService;
	@Autowired
	private WinnerListService winnerListService;
	@Autowired
	private SendMsgUIService sendMsgUIService;
	@Autowired
	private ContentCouponService contentCouponService;	
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSGameController.class);

	@RequestMapping(method = RequestMethod.GET, value = "/edit/gameCreatePage")
	public String GameCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("gameCreatePage");
		return BcsPageEnum.GameCreatePage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/gameListPage")
	public String GameListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("gameListPage");
		return BcsPageEnum.GameListPage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/winnerListPage")
	public String WinnerListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("winnerListPage");
		return BcsPageEnum.WinnerListPage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/prizeListPage")
	public String PrizeListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("prizeListPage");
		return BcsPageEnum.PrizeListPage.toString();
	}

	/**
	 * 取得遊戲列表
	 */
	@ControllerLog(description="取得遊戲列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getGameList")
	@ResponseBody
	public ResponseEntity<?> getGameList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser
			) throws IOException {
		logger.info("getTemplateMsgList");

		try {
			List<GameModel> result = contentGameService.getAllContentGame();

			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 取得獎品列表
	 */
	@ControllerLog(description="取得獎品列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getPrizeList/{gameId}")
	@ResponseBody
	public ResponseEntity<?> getPrizeList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String gameId) throws IOException {
		logger.info("getPrizeList");

		try {
			List<ContentCoupon> result = contentGameService.getPrizeList(gameId);

			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 取得中獎名單
	 */
	@ControllerLog(description="取得中獎名單")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getWinnerList/{gameId}/{prizeId}/{pageIndex}")
	@ResponseBody
	public ResponseEntity<?> getWinnerList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String gameId, 
			@PathVariable String prizeId,
			@PathVariable Integer pageIndex) throws IOException {
		logger.info("getWinnerList");

		try {
			List<WinnedCouponModel> result = null;
			if (prizeId.equals("null")) {
				result = winnerListService.getWinnerList(gameId, pageIndex);
			} else {
				result = winnerListService.getWinnerListByPrizeId(gameId, prizeId, pageIndex);
			}
			
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 取得一段時間中獎名單
	 */
	@ControllerLog(description="取得一段時間中獎名單")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getWinnerList/{gameId}/{prizeId}/{startDate}/{endDate}/{pageIndex}")
	@ResponseBody
	public ResponseEntity<?> getWinnerList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String gameId, 
			@PathVariable String prizeId, 
			@PathVariable String startDate,
			@PathVariable String endDate,
			@PathVariable Optional<Integer> pageIndex) throws IOException {
		logger.info("getWinnerList");

		try {
			if (prizeId.equals("null")) {
				List<WinnedCouponModel> result = winnerListService.queryWinnerList(gameId, startDate, endDate,pageIndex);
				return new ResponseEntity<>(result, HttpStatus.OK);
			} else {
				List<WinnedCouponModel> result = winnerListService.queryWinnerListByPrizeId(gameId, prizeId,
						startDate, endDate,pageIndex);
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 取得中獎人資訊
	 */
	@ControllerLog(description="取得中獎人資訊")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getWinnerDetail/{winnerId}")
	@ResponseBody
	public ResponseEntity<?> getWinnerDetail(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String winnerId) throws IOException {
		logger.info("getWinnerDetail");

		try {
			WinnerModel result = winnerListService.getWinnerDetail(winnerId);

			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 傳送得獎者
	 * 
	 * @param sendMsgModel
	 * @param customUser
	 * @return String
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/edit/sendToWinner")
	@ResponseBody
	public ResponseEntity<?> sendToWinner(HttpServletRequest request, HttpServletResponse response,
			@RequestBody SendMsgModel sendMsgModel, @CurrentUser CustomUser customUser, @RequestParam String UID)
			throws Exception {
		try {
			String account = customUser.getAccount();
	
			sendMsgUIService.sendMsgToMid(UID, sendMsgModel.getSendMsgDetails(), account);
	
			String result = "Sending Message To Winner Success";
	
			return new ResponseEntity<>(result, HttpStatus.OK);
		}catch (Exception e) {
			logger.info(e.getMessage());
			
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 刪除遊戲
	 */
	@ControllerLog(description="刪除遊戲")
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteGame/{gameId}")
	@ResponseBody
	public ResponseEntity<?> deleteGame(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,  
			@PathVariable String gameId) {
		logger.info("deleteGame");

		try {
			// Check Delete Right
			boolean isAdmin = customUser.isAdmin();
			if (isAdmin) {
				List<ContentCoupon> targetCouponList = null;

				targetCouponList = contentCouponService.findByEventReferenceAndEventReferenceId(ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD, gameId);

				contentCouponService.resetCouponEvent(targetCouponList);    // 將優惠券重新設為未使用的狀態
				contentGameService.deleteGame(gameId, customUser.getAccount());    // 刪除遊戲
				// turntableDetailService.refresh(gameId);
				return new ResponseEntity<>("Delete Success", HttpStatus.OK);
			} else {
				throw new BcsNoticeException("此帳號沒有刪除權限");
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			if (e instanceof BcsNoticeException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			} else {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@ControllerLog(description="getGameNameList")
	@RequestMapping(method = RequestMethod.GET, value = "/admin/getGameNameList")
	@ResponseBody
	public ResponseEntity<?> getGameNameList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser)
			throws IOException {
		logger.info("getGameNameList");
		Map<String, String> map = contentGameService.findGameNameMap();
		logger.debug("map:" + ObjectUtil.objectToJsonStr(map));
		return new ResponseEntity<>(map, HttpStatus.OK);
	}
	
	/*
	 * 刮刮卡 QRcode
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/game/createGameQRcode/{gameId}")
	@ResponseBody
	public void createGameQRcode(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String gameId) throws IOException {
		response.setContentType("image/png");
	    try {
            QrcodeGenerator.generateQrcode(UriHelper.goScratchCardUri() + "/" + gameId, response.getOutputStream());
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }
	}
	
	@ControllerLog(description="取得中獎名單最大頁面數")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getGameWinnerMaxPage")
	@ResponseBody
	public ResponseEntity<?> getCouponSerialMaxPage(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String gameId,
			@RequestParam(required=false) Optional<String> couponId) throws IOException {
		logger.info("getCouponSerialMaxPage");
		try{
			Integer MaxPage = winnerListService.getGameWinnerMaxPageByGameIdAndCouponId(gameId,couponId);
			return new ResponseEntity<>(MaxPage, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}