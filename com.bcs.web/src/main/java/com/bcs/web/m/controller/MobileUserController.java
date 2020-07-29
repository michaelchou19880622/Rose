package com.bcs.web.m.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bcs.core.aspect.annotation.WebServiceLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bcs.core.db.service.LineUserService;
import com.bcs.core.enums.BCS_PAGE_TYPE;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.taishin.service.TaishinValidateService;
import com.bcs.core.utils.UrlUtil;
import com.bcs.core.validate.service.UserValidateService;
import com.bcs.core.web.m.service.MobilePageService;
import com.bcs.core.web.ui.page.enums.MobilePageEnum;


@Controller
@RequestMapping("/m")
public class MobileUserController {

	@Autowired
	private UserValidateService userValidateService;
	@Autowired
	private LineUserService lineUserService;
	@Autowired
	private MobilePageService mobilePageService;
	@Autowired
	private TaishinValidateService pagValidateService;
	@Autowired
	private MobileGameController mobileGameController;

	/** Logger */
	private static Logger logger = Logger.getLogger(MobileUserController.class);

	@WebServiceLog
	@RequestMapping(method = RequestMethod.GET, value = "/goIndex")
	public void goIndex(HttpServletRequest request, HttpServletResponse response) throws IOException{
//		logger.info("goIndex");
		String MID = request.getParameter("MID");
//		logger.info("goIndex MID:" + MID);
		String toPage = request.getParameter("toPage");
		logger.info("goIndex toPage:" + toPage);
		String referenceId = request.getParameter("referenceId");
//		logger.info("goIndex referenceId:" + referenceId);
		String time = request.getParameter("time");
//		logger.info("goIndex time:" + time);
		String hash = request.getParameter("hash");
//		logger.info("goIndex hash:" + hash);

		if(StringUtils.isBlank(MID)){
			MID = (String) request.getSession().getAttribute("MID");
		}
		else{
			boolean validate = false;
//			logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
//			logger.info("@@@@@@@@BCS_PAGE_TYPE.TYPE_REWARD_CARD_LIST_PAGE:"+BCS_PAGE_TYPE.TYPE_REWARD_CARD_LIST_PAGE);
//			logger.info("@@@@@@@@@@@@@@@@@toPage:"+toPage);
			if(StringUtils.isNotBlank(referenceId)){
				validate = UrlUtil.validateHash(MID, referenceId, time, hash);
			}
			else if(StringUtils.isNotBlank(toPage)){
				validate = UrlUtil.validateHash(MID, toPage, time, hash);
			}
			else{
				validate = UrlUtil.validateHash(MID, null, time, hash);
			}

//			logger.info("goIndex validate:" + validate);
			if(!validate){
				String linkUrl = UriHelper.bcsMPage;
				response.sendRedirect(linkUrl);
				return;
			}

			request.getSession().setAttribute("MID", MID);
		}

//		if(BCS_PAGE_TYPE.TYPE_CHANGE_SHOP_PAGE.toString().equals(toPage)){
//			response.sendRedirect(UriHelper.getChangeShopUri());
//			return;
//		}
//		if(BCS_PAGE_TYPE.TYPE_READ_TERMS_PAGE.toString().equals(toPage)){
//			response.sendRedirect(UriHelper.getReadTermsOfBusinessUri());
//			return;
//		}
//		else if(BCS_PAGE_TYPE.TYPE_GET_CARD_PAGE.toString().equals(toPage)){
//			response.sendRedirect(UriHelper.getCardUri());
//			return;
//		}
//		if(BCS_PAGE_TYPE.TYPE_COUPON_LIST_PAGE.toString().equals(toPage)){
//			response.sendRedirect(UriHelper.getUserCouponListPageUri());
//			return;
//		}
//		else if(BCS_PAGE_TYPE.TYPE_COUPON_PAGE.toString().equals(toPage)){
//			response.sendRedirect(UriHelper.getUserCouponPageUri(referenceId));
//			return;
//		}
//		else if(BCS_PAGE_TYPE.TYPE_TURNTABLE_PAGE.toString().equals(toPage)){
//			logger.info("@@@@@@@@@TYPE_TURNTABLE_PAGE:" + BCS_PAGE_TYPE.TYPE_TURNTABLE_PAGE);
//			logger.info("@@@@@@@@@sendRedirect:" +UriHelper.getUserTurntablePageUri(referenceId, false));
//			response.sendRedirect(UriHelper.getUserTurntablePageUri(referenceId, false));
//			return;
//		}
//		else if(BCS_PAGE_TYPE.TYPE_SCRACTH_PAGE.toString().equals(toPage)){
//			logger.info("@@@@@@@@@TYPE_SCRACTH_PAGE:" + BCS_PAGE_TYPE.TYPE_SCRACTH_PAGE);
//			logger.info("@@@@@@@@@sendRedirect:" +UriHelper.getUserScratchCardPageUri(MID,referenceId, false));
//			response.sendRedirect(UriHelper.getUserScratchCardPageUri(MID,referenceId, false));
//			return;
//		}
//		else if(BCS_PAGE_TYPE.TYPE_REWARD_CARD_LIST_PAGE.toString().equals(toPage)){
//			logger.info("@@@@@TYPE_REWARD_CARD_LIST_PAGE:" + BCS_PAGE_TYPE.TYPE_REWARD_CARD_LIST_PAGE.toString());
//			response.sendRedirect(UriHelper.getUserRewardCardListPageUri(MID));
//			return;
//		}
//		else if(BCS_PAGE_TYPE.TYPE_REWARD_CARD_PAGE.toString().equals(toPage)){
//			response.sendRedirect(UriHelper.getUserRewardCardPageUri(referenceId));
//			return;
//		}
//		else if(BCS_PAGE_TYPE.TYPE_REWARD_CARD_ADD_POINT_PAGE.toString().equals(toPage)){
//			response.sendRedirect(UriHelper.getAddPointUserRewardCardPageUri(referenceId));
//			return;
//		}
//		else if(BCS_PAGE_TYPE.MGM_CAMPAIGN_PAGE.toString().equals(toPage)) {
//            response.sendRedirect(UriHelper.getGoMgmPage(referenceId));
//            return;
//	    }
//		else if(BCS_PAGE_TYPE.TYPE_TURNTABLE_PAGE_REFRESH.toString().equals(toPage)){
//			response.sendRedirect(UriHelper.getUserTurntablePageUri(referenceId, true));
//			return;
//		}
//		else if(BCS_PAGE_TYPE.TYPE_SHARE_CONTENT_PAGE.toString().equals(toPage)){
//			response.sendRedirect(UriHelper.getShareContentPagePageUri());
//			return;
//		}
//		else{
			response.sendRedirect(UriHelper.getIndexUri());
			return;
//		}
	}

	@WebServiceLog
	@RequestMapping(method = RequestMethod.GET, value = "/index")
	public String indexPage(HttpServletRequest request,
			HttpServletResponse response,
			Model model){
		logger.info("indexPage");

		model.addAttribute("linkDefault", UriHelper.bcsMPage);

		return MobilePageEnum.Page404.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/redirect")
	public String redirectPage(HttpServletRequest request,
			HttpServletResponse response,
			Model model){
		String redirect = request.getParameter("redirect");
		logger.info("redirectPage:" + redirect);

		model.addAttribute("linkDefault", redirect);

		return MobilePageEnum.PageRedirect.toString();
	}

//	@RequestMapping(method = RequestMethod.GET, value = "/index")
//	public String indexPage(HttpServletRequest request, HttpServletResponse response,
//			Model model){
//		logger.info("indexPage");
//
//		try{
//			String sessionMID = (String) request.getSession().getAttribute("MID");
//			logger.info("sessionMID:" + sessionMID);
//
//			if(StringUtils.isNotBlank(sessionMID)){
//
//				// Validate MID is Binding
//				boolean isBinding = userValidateService.isBinding(sessionMID);
//				if(isBinding){
//					mobilePageService.visitPageLog(sessionMID, MobilePageEnum.TurntableIndexPage.getName(), "index");
//					return mobileGameController.turntableIndexPage(request, response, model);
//				}
//				else{
//					mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserReadTermsOfBusinessPage.getName(), "index");
//					return MobilePageEnum.UserReadTermsOfBusinessPage.toString();
//				}
//			}
//			else{
//				logger.info("MID Null");
//				mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserReadTermsOfBusinessPage.getName(), "index");
//				return MobilePageEnum.UserReadTermsOfBusinessPage.toString();
//			}
//		} catch (Exception e) {
//			logger.error(ErrorRecord.recordError(e));
//			return MobilePageEnum.UserReadTermsOfBusinessPage.toString();
//		}
//	}

//	@RequestMapping(method = RequestMethod.GET, value = "/index")
//	public String indexPage(HttpServletRequest request, HttpServletResponse response){
//		logger.info("indexPage");
//
//		try{
//			String sessionMID = (String) request.getSession().getAttribute("MID");
//			logger.info("sessionMID:" + sessionMID);
//
//			if(StringUtils.isNotBlank(sessionMID)){
//
//				// Validate MID is Binding
//				boolean isBinding = userValidateService.isBinding(sessionMID);
//				if(isBinding){
//					String event = request.getParameter("event");
//					logger.info("indexPage event:" + event);
//
//					boolean isInShareEvent = CoreConfigReader.getBoolean(CONFIG_STR.EVENT_SHARE.toString(), true);
//					if(isInShareEvent && "SHARE".equals(event)){
//						mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserShareIndexPage.getName(), "index");
//						return MobilePageEnum.UserShareIndexPage.toString();
//					}else if(isInShareEvent && "SHARE_CARD".equals(event)){
//						mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserShareBindedPage.getName(), "index");
//						return MobilePageEnum.UserShareBindedPage.toString();
//					}else{
//						mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserBindedPage.getName(), "index");
//						return MobilePageEnum.UserBindedPage.toString();
//					}
//				}
//				else{
//					mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserNotBindedPage.getName(), "index");
//					return MobilePageEnum.UserNotBindedPage.toString();
//				}
//			}
//			else{
//				logger.info("MID Null");
//				mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserNotBindedPage.getName(), "index");
//				return MobilePageEnum.UserNotBindedPage.toString();
//			}
//		} catch (Exception e) {
//			logger.error(ErrorRecord.recordError(e));
//			return MobilePageEnum.UserNotBindedPage.toString();
//		}
//	}

//	@RequestMapping(method = RequestMethod.GET, value = "/userBindingPage")
//	public String userBindingPage(
//			@RequestParam(value = "readTermsOfBusiness", required = false, defaultValue = "false") boolean readTermsOfBusiness,
//			HttpServletRequest request,
//			HttpServletResponse response,
//			Model model) {
//		logger.info("userBindingPage");
//
//		try{
//			String sessionMID = (String) request.getSession().getAttribute("MID");
//
//			// Validate MID is Binding
//			boolean isBinding = userValidateService.isBinding(sessionMID);
//
//			if(StringUtils.isNotBlank(sessionMID) && !isBinding){
//
//				// 若尚未在閱讀條款頁面勾選 "我已閱讀..."，就轉向閱讀條款頁面
//				if (!readTermsOfBusiness) {
//					mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserReadTermsOfBusinessPage.getName(), "userBindingPage");
//					return MobilePageEnum.UserReadTermsOfBusinessPage.toString();
//				} else {
//					mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserReadTermsOfBusinessPage.getName(), "userBindingPage");
//					return MobilePageEnum.UserReadTermsOfBusinessPage.toString();
//				}
//			}
//			else if(StringUtils.isNotBlank(sessionMID) && isBinding){
//
//				mobilePageService.visitPageLog(sessionMID, MobilePageEnum.TurntableIndexPage.getName(), "userBindingPage");
//				return mobileGameController.turntableIndexPage(request, response, model);
//			}
//			else{
//				mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserReadTermsOfBusinessPage.getName(), "userBindingPage");
//				return MobilePageEnum.UserReadTermsOfBusinessPage.toString();
//			}
//		} catch (Exception e) {
//			logger.error(ErrorRecord.recordError(e));
//			return MobilePageEnum.UserReadTermsOfBusinessPage.toString();
//		}
//	}
//
//	@RequestMapping(method = RequestMethod.POST, value = "/doBinding")
//	public void doBindingPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//
//		String sessionMID = (String) request.getSession().getAttribute("MID");
//		logger.info("doBindingPost:" + sessionMID);
//
//		// Validate MID is Binding
//		boolean isBinding = userValidateService.isBinding(sessionMID);
//
//		if(StringUtils.isNotBlank(sessionMID) && !isBinding){
//
//			try{
//				String PhoneNum = request.getParameter("PhoneNum");
//				String BirthdayYear = request.getParameter("BirthdayYear");
//				String BirthdayMonth = request.getParameter("BirthdayMonth");
//				String Gender = request.getParameter("Gender");
//				logger.info("PhoneNum:" + PhoneNum);
//				logger.info("Birthday:" + BirthdayYear + "-" + BirthdayMonth);
//				logger.info("Gender:" + Gender);
//				if(StringUtils.isBlank(PhoneNum)){
//					throw new Exception("Phone Num Null");
//				}
//				if(StringUtils.isBlank(BirthdayYear)){
//					throw new Exception("User Birthday Year Null");
//				}
//				if(StringUtils.isBlank(BirthdayMonth)){
//					throw new Exception("User Birthday Month Null");
//				}
//				if(StringUtils.isBlank(Gender)){
//					throw new Exception("User Gender Null");
//				}
//
//				String Birthday = BirthdayYear + BirthdayMonth;
//
//				pagValidateService.bindedLineUser(sessionMID, PhoneNum, Birthday, Gender);
//				response.sendRedirect(UriHelper.getIndexUri());
//				return;
//			}
//			catch(Exception e){
//				logger.error(ErrorRecord.recordError(e));
//			}
//
//			response.sendRedirect(UriHelper.getReadTermsOfBusinessUri());
//			return;
//		}
//		else if(StringUtils.isNotBlank(sessionMID) && isBinding){
//			response.sendRedirect(UriHelper.getIndexUri());
//			return;
//		}
//		else{
//			response.sendRedirect(UriHelper.getIndexUri());
//			return;
//		}
//	}

//	@RequestMapping(method = RequestMethod.GET, value = "/userDoBindingFailPage")
//	public String userDoBindingFailPage(HttpServletRequest request, HttpServletResponse response){
//		logger.info("userDoBindingFailPage");
//
//		String sessionMID = (String) request.getSession().getAttribute("MID");
//
//		mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserDoBindingFailPage.getName(), "userDoBindingFailPage");
//		return MobilePageEnum.UserDoBindingFailPage.toString();
//	}
//
//	@RequestMapping(method = RequestMethod.GET, value = "/doBinding")
//	public String doBindingGet(HttpServletRequest request, HttpServletResponse response){
//		logger.info("doBindingGet");
//
//		return indexPage(request, response);
//	}

//	@RequestMapping(method = RequestMethod.GET, value = "/userGetCardPage")
//	public String userGetCardPage(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("userGetCardPage");
//
//		try{
//			String sessionMID = (String) request.getSession().getAttribute("MID");
//
//			// Validate MID is Binding
//			boolean isBinding = userValidateService.isBinding(sessionMID);
//
//			if(StringUtils.isNotBlank(sessionMID) && isBinding){
//				// Birthday Record
//				String Birthday = (String) request.getSession().getAttribute("Birthday");
//				if(StringUtils.isNotBlank(Birthday)){
//					request.getSession().setAttribute("GetCardSuccess", true);
//
//					mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserCardPage.getName(), "QuickPass");
//					return MobilePageEnum.UserCardPage.toString();
//				}
//
//				mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserGetCardPage.getName(), "userGetCardPage");
//				return MobilePageEnum.UserGetCardPage.toString();
//			}
//			else{
//				mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserNotBindedPage.getName(), "userGetCardPage");
//				return MobilePageEnum.UserNotBindedPage.toString();
//			}
//		} catch (Exception e) {
//			logger.error(ErrorRecord.recordError(e));
//			return MobilePageEnum.UserNotBindedPage.toString();
//		}
//	}

//	@RequestMapping(method = RequestMethod.POST, value = "/doGetCard")
//	public void doGetCardPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//		logger.info("doGetCardPost");
//
//		String sessionMID = (String) request.getSession().getAttribute("MID");
//
//		// Validate MID is Binding
//		boolean isBinding = userValidateService.isBinding(sessionMID);
//
//		if(StringUtils.isNotBlank(sessionMID) && isBinding){
//
//			try{
//				String BirthdayYear = request.getParameter("BirthdayYear");
//				String BirthdayMonth = request.getParameter("BirthdayMonth");
//				String BirthdayDay = request.getParameter("BirthdayDay");
//				logger.info("Birthday:" + BirthdayYear + "-" + BirthdayMonth + "-" + BirthdayDay);
//				if(StringUtils.isBlank(BirthdayYear)){
//					throw new Exception("User Birthday Year Null");
//				}
//				if(StringUtils.isBlank(BirthdayMonth)){
//					throw new Exception("User Birthday Month Null");
//				}
//				if(StringUtils.isBlank(BirthdayDay)){
//					throw new Exception("User Birthday Day Null");
//				}
//
//				LineUser lineUser = lineUserService.findByMid(sessionMID);
//				if(lineUser == null || StringUtils.isBlank(lineUser.getUserId())){
//					throw new Exception("User Not Binded");
//				}
//
//				String Birthday = BirthdayYear + "-" + BirthdayMonth + "-" + BirthdayDay;
//
//				// Do Binding Call Binding API
//				ObjectNode validateResult = userValidateService.callValidateAPI(sessionMID, lineUser.getUserId(), Birthday, lineUser.getUserFavoriteShop());
//				if(validateResult != null){
//
//					if(validateResult.get("result").intValue() == 1){
//						// Birthday Record
//						request.getSession().setAttribute("Birthday", Birthday);
//
//						lineUserService.saveLog(lineUser, sessionMID, LOG_TARGET_ACTION_TYPE.ACTION_GetCard, sessionMID);
//
//						request.getSession().setAttribute("GetCardSuccess", true);
//
//						response.sendRedirect(UriHelper.getUserCardPageUri());
//						return;
//					}
//					else if(validateResult.get("result").intValue() == 0){
//						logger.info("validateResult:" + validateResult);
//						UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_LineUser, LOG_TARGET_ACTION_TYPE.ACTION_GetCardFail, sessionMID, validateResult, sessionMID);
//					}
//					else{
//						throw new Exception("validateResult:" + validateResult);
//					}
//				}
//				else{
//					throw new Exception("Validate Null Error");
//				}
//			}
//			catch(Exception e){
//				logger.error(ErrorRecord.recordError(e));
//			}
//
//			response.sendRedirect(UriHelper.getUserGetCardFailPageUri());
//			return;
//		}
//		else{
//			response.sendRedirect(UriHelper.getIndexUri());
//			return;
//		}
//	}

//	@RequestMapping(method = RequestMethod.GET, value = "/userCardPage")
//	public String userCardPage(HttpServletRequest request, HttpServletResponse response){
//		logger.info("userCardPage");
//
//		String sessionMID = (String) request.getSession().getAttribute("MID");
//
//		// Validate MID is Binding
//		boolean isBinding = userValidateService.isBinding(sessionMID);
//
//		if(StringUtils.isNotBlank(sessionMID) && isBinding){
//			mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserCardPage.getName(), "userCardPage");
//			return MobilePageEnum.UserCardPage.toString();
//		} else {
//			mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserNotBindedPage.getName(), "userCardPage");
//			return MobilePageEnum.UserNotBindedPage.toString();
//		}
//	}

//	@RequestMapping(method = RequestMethod.GET, value = "/userGetCardFailPage")
//	public String userGetCardFailPage(HttpServletRequest request, HttpServletResponse response){
//		logger.info("userGetCardFailPage");
//
//		String sessionMID = (String) request.getSession().getAttribute("MID");
//
//		// Validate MID is Binding
//		boolean isBinding = userValidateService.isBinding(sessionMID);
//
//		if(StringUtils.isNotBlank(sessionMID) && isBinding){
//			mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserGetCardFailPage.getName(), "userGetCardFailPage");
//			return MobilePageEnum.UserGetCardFailPage.toString();
//		} else {
//			mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserNotBindedPage.getName(), "userGetCardFailPage");
//			return MobilePageEnum.UserNotBindedPage.toString();
//		}
//	}

//	@RequestMapping(method = RequestMethod.GET, value = "/doGetCard")
//	public String doGetCardGet(HttpServletRequest request, HttpServletResponse response){
//		logger.info("doGetCardGet");
//
//		return indexPage(request, response);
//	}

//	@RequestMapping(method = RequestMethod.GET, value = "/mycardBarcode")
//	public void mycardBarcode(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
//		logger.info("mycardBarcode");
//
//		String sessionMID = (String) request.getSession().getAttribute("MID");
//
//		// Validate MID is Binding
//		boolean isBinding = userValidateService.isBinding(sessionMID);
//
//		if(StringUtils.isNotBlank(sessionMID) && isBinding){
//			boolean GetCardSuccess = (boolean) request.getSession().getAttribute("GetCardSuccess");
//
//			if(GetCardSuccess){
//				String inputStr = userValidateService.getMyId(sessionMID);
//
//				BarcodeGenerator.generateBarcode128(inputStr, response.getOutputStream());
//				return ;
//			}
//		}
//
//		throw new Exception("User Error");
//	}

//	@RequestMapping(method = RequestMethod.GET, value = "/myIdSub")
//	@ResponseBody
//	public ResponseEntity<?> myIdSub(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
//		logger.info("myIdSub");
//
//		String sessionMID = (String) request.getSession().getAttribute("MID");
//
//		// Validate MID is Binding
//		boolean isBinding = userValidateService.isBinding(sessionMID);
//
//		if(StringUtils.isNotBlank(sessionMID) && isBinding){
//			boolean GetCardSuccess = (boolean) request.getSession().getAttribute("GetCardSuccess");
//
//			if(GetCardSuccess){
//				String inputStr = userValidateService.getMyId(sessionMID);
//				String result= inputStr.substring(inputStr.length() -4, inputStr.length());
//
//				return new ResponseEntity<>(result, HttpStatus.OK);
//			}
//		}
//
//		throw new Exception("User Error");
//	}
}
