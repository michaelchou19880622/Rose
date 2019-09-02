package com.bcs.web.m.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentPrize;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.service.ContentGameService;
import com.bcs.core.db.service.ContentPrizeService;
import com.bcs.core.db.service.ScratchCardDetailService;
import com.bcs.core.db.service.WinnerListService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.model.CouponModel;
import com.bcs.core.model.GameModel;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.ui.page.enums.MobilePageEnum;
import com.bcs.web.m.service.MobileCouponService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Controller
@RequestMapping("/m")
public class MobileGameController {
	
	@Autowired
	private MobileUserController mobileUserController;
	@Autowired
	private ScratchCardDetailService scratchCardDetailService;
	@Autowired
	private ContentPrizeService contentPrizeService;
	@Autowired
	private WinnerListService winnerListService;
	@Autowired
	private ContentGameService contentGameService;
	@Autowired
	private ContentCouponService contentCouponService;
	@Autowired
	private MobileCouponService mobileCouponService;

	protected LoadingCache<String, ContentCoupon> dataCache;

	/** Logger */
	private static Logger logger = Logger.getLogger(MobileGameController.class);

	public MobileGameController() {

		dataCache = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterAccess(10, TimeUnit.MINUTES)
				.build(new CacheLoader<String, ContentCoupon>() {
					@Override
					public ContentCoupon load(String key) throws Exception {
						return new ContentCoupon();
					}
				});
	}

	@RequestMapping(method = RequestMethod.GET, value = "/turntableIndexPage")
	public String turntableIndexPage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "gameId", required = true) String gameId,
			@RequestParam(value = "UID", required = true) String UID, Model model) {
		logger.info("turntableIndexPage");

		try {

			if (request.getSession().getAttribute("obtainedPrize_" + gameId) == null) {
				return MobilePageEnum.TurntableIndexPage.toString();
			} else if (request.getSession().getAttribute("prizeIsAccepted_" + gameId) == null) {
				return MobilePageEnum.AcceptPrizePage.toString();
			} else {
				return MobilePageEnum.PrizeAcceptedPage.toString();
			}

		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return mobileUserController.indexPage(request, response, model);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/scratchCardIndexPage")
	public String scratchCardIndexPage(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(value = "gameId", required = true) String gameId, 
			@RequestParam(value = "UID", required=false) String UID, Model model) {

		logger.info("scratchCardIndexPage");

		if (UID == null) {
			UID = request.getSession().getAttribute("UID").toString();
		} else {
			request.getSession().setAttribute("UID", UID);
		}

		try {
			String scratchedOffCouponId = contentGameService.getScratchedOffCouponId(gameId, UID);
			if (scratchedOffCouponId != null) {
				logger.info("【刮刮卡首頁】該使用者已玩過刮刮卡！");
				
				return mobileCouponService.couponContentPage(scratchedOffCouponId.toString(), model, request, response,
						MobilePageEnum.UserCouponContentPage, false, false);
			} else {
				logger.info("【刮刮卡首頁】該使用者尚未玩過刮刮卡！");

				return MobilePageEnum.ScratchCardIndexPage.toString();
			}
		} catch (Exception e) {
			logger.info("〔ScratchCardIndexPage Error〕：");
			logger.error(ErrorRecord.recordError(e));
			return mobileUserController.indexPage(request, response, model);
		}
	}

	/**
	 * 取得轉盤遊戲
	 */
	/*
	 * @RequestMapping(method = RequestMethod.GET, value =
	 * "/Game/turntable/{gameId}")
	 * 
	 * @ResponseBody public ResponseEntity<?> getTurntable(HttpServletRequest
	 * request, HttpServletResponse response,
	 * 
	 * @PathVariable Long gameId) throws IOException { logger.info("getTurntable");
	 * 
	 * try { GameModel result = turntableDetailService.getTurntable(gameId); Integer
	 * prizeListId = -1;
	 * 
	 * if (request.getSession().getAttribute("obtainedPrize_" + gameId) == null) {
	 * prizeListId = contentPrizeService.getRandomPrize(gameId);
	 * request.getSession().setAttribute("obtainedPrize_" + gameId, prizeListId);
	 * request.getSession().setMaxInactiveInterval(1800); } else { prizeListId =
	 * (Integer) request.getSession().getAttribute("obtainedPrize_" + gameId); }
	 * 
	 * String obtainedPrizeId =
	 * contentPrizeService.getPrizeByPrizeListId(prizeListId).getPrizeId();
	 * 
	 * for (PrizeModel prizeModel : result.getPrizes()) { if
	 * (prizeModel.getPrizeId().equals(obtainedPrizeId)) {
	 * prizeModel.setPrizeQuantity(-1); } }
	 * 
	 * return new ResponseEntity<>(result, HttpStatus.OK); } catch (Exception e) {
	 * logger.error(ErrorRecord.recordError(e));
	 * 
	 * return new ResponseEntity<>(e.getMessage(),
	 * HttpStatus.INTERNAL_SERVER_ERROR); } }
	 */

	/**
	 * 取得刮刮卡遊戲
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/Game/scratchCard/{gameId}")
	@ResponseBody
	public ResponseEntity<?> getScratchCard(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String gameId) throws IOException {
		logger.info("getScratchCard");

		try {
			GameModel result = scratchCardDetailService.getScratchCard(gameId);
			
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * 抽一張優惠券
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/Game/drawCoupon/{gameId}")
	@ResponseBody
	public ResponseEntity<?> drawCoupon(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String gameId) throws IOException {
		logger.info("drawCoupon");

		try {
			String sessionMID = (String) request.getSession().getAttribute("MID"); // 取得使用者的 LINE UID
			
			if(StringUtils.isBlank(sessionMID)){
				return new ResponseEntity<>("MID Error", HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				String drewCouponId = contentPrizeService.getRandomPrize(gameId, sessionMID); // 抽一個優惠券，並回傳抽到的優惠券 id
				ContentCoupon contentCoupon = null;
				CouponModel drewCoupon = new CouponModel();
				
				/* 如果有抽到優惠券，將優惠券內容塞至 drewCouponContent 中 */
				if (drewCouponId != null) {
					contentCoupon = contentCouponService.findOne(drewCouponId);
	
					drewCoupon.setCouponId(drewCouponId);
					drewCoupon.setCouponTitle(contentCoupon.getCouponTitle());
					drewCoupon.setCouponImageId(contentCoupon.getCouponImageId());
					drewCoupon.setCouponDescription(contentCoupon.getCouponDescription());
					drewCoupon.setCouponUseDescription(contentCoupon.getCouponUseDescription());
					drewCoupon.setCouponRuleDescription(contentCoupon.getCouponRuleDescription());
				} else {
					drewCoupon = null;
				}
	
				return new ResponseEntity<>(drewCoupon, HttpStatus.OK);
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(null, HttpStatus.OK);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		}
	}

	/**
	 * 取得獎品資訊
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/Game/getPrizeDetail/{gameId}")
	@ResponseBody
	public ResponseEntity<?> getPrizeDetail(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String gameId) throws IOException {
		logger.info("getPrizeDetail");

		try {
			Integer prizeListId = (Integer) request.getSession().getAttribute("obtainedPrize_" + gameId);
			ContentPrize contentPrize = contentPrizeService.getPrizeByPrizeListId(prizeListId);

			return new ResponseEntity<>(contentPrize, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/Game/goScratchCardByQRcode/{gameId}")
	public String goScratchCardByQRcode(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String gameId, Model model) throws IOException {
		logger.info("goScratchCardByQRcode");

		try {
			String startTracingUrl = "";

			String ChannelID = CoreConfigReader.getString(CONFIG_STR.Default.toString(),CONFIG_STR.ChannelID.toString(),true);
			
			startTracingUrl = CoreConfigReader.getString(CONFIG_STR.LINE_OAUTH_URL_V2_1);
			startTracingUrl = startTracingUrl.replace("{ChannelID}", ChannelID);
			startTracingUrl = startTracingUrl.replace("{RedirectUrl}", URLEncoder.encode(UriHelper.getScratchCardValidateUri(), "UTF-8"));

			startTracingUrl = startTracingUrl.replace("{TracingId}", gameId);

			logger.info("◎ URL: " + startTracingUrl);

			model.addAttribute("lineoauthLink", startTracingUrl);

			return MobilePageEnum.UserTracingStartPage.toString();
		} catch (Exception e) {
			logger.info(ErrorRecord.recordError(e));
			return mobileUserController.indexPage(request, response, model);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/Game/ScratchCard/validate")
	public void validateTracing(HttpServletRequest request, HttpServletResponse response, Model model)
			throws Exception {
		logger.info("ScratchCard validateTracing");

		HttpSession session = request.getSession();

		try {
			String code = request.getParameter("code");
			logger.info("validateTracing code:" + code);

			String state = request.getParameter("state");
			logger.info("validateTracing state:" + state);

			String errorCode = request.getParameter("error");
			logger.info("validateTracing error code:" + errorCode);

			String errorDescription = request.getParameter("error_description");
			logger.info("validateTracing error description:" + errorDescription);
			
			if (StringUtils.isBlank(state)) {
				throw new Exception("Scratch Card Id Error:" + state);
			}

			/* 設定向 LINE 取得 access token 的 request headers */
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
			
			HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = null;
			String proxyUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_PROXY_URL.toString(), true);    // Proxy Server 的位置

			if (StringUtils.isNotBlank(proxyUrl)) {
				logger.info("Use proxy and proxy url is: " + proxyUrl);
				clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().setProxy(new HttpHost(proxyUrl, 80, "http")).build());
			}

			/* 設定 request body */
			MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
			map.add("grant_type", "authorization_code");
			map.add("code", code);
			map.add("redirect_uri", UriHelper.getScratchCardValidateUri());
			
			String ChannelID = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelID.toString(), true);
		    String ChannelSecret = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelSecret.toString(), true);
		    
			map.add("client_id", ChannelID);
			map.add("client_secret", ChannelSecret);
			
			HttpEntity<MultiValueMap<String, String>> accessTokenEntity = new HttpEntity<MultiValueMap<String, String>>(map, headers);

			/* 以 Post 方式送出 request (如果有需要用 Proxy，便將上面設定好的 clientHttpRequestFactory 加進來) */
			RestTemplate restTemplate = (clientHttpRequestFactory == null) ? new RestTemplate() : new RestTemplate(clientHttpRequestFactory);
			ResponseEntity<String> accessTokenResponse = restTemplate.postForEntity(CoreConfigReader.getString(CONFIG_STR.LINE_OAUTH_URL_ACCESSTOKEN_V2_1), accessTokenEntity, String.class);
			
			String responseBody = accessTokenResponse.getBody(); // Response 的結果
			logger.info("responseBody:"+responseBody);
			
			JSONObject responseObj = new JSONObject(responseBody);
			String ID_Token = responseObj.get("id_token").toString(); // 將 id_token 從 response body 中拿出來
			
			String[] parsedJWT = ID_Token.split("[.]");	// 將 id_token 以逗點為基準切成 header、payload、signature 三個部分
			
			String header = parsedJWT[0], payload = parsedJWT[1], signature = parsedJWT[2];
			Base64.Decoder base64Decoder = Base64.getDecoder();
			
			/* 驗證接收到的 ID Token 是否為合法的 JWT */
			if(validateJWT(ChannelSecret, header, payload, signature, ChannelID, null)) {			
				JSONObject payloadObject = new JSONObject(new String(base64Decoder.decode(payload), "UTF-8"));	// 將 payload 用 base64 解碼後轉為 UTF-8 字串，再轉換成 JSON 物件
				
				String UID = payloadObject.get("sub").toString();	// 從解析出來的 JSON 物件中取得使用者的 UID
				
				session.setAttribute("UID", UID); // 將 UID 存入 session，以便後續的刮刮卡流程使用
				
				response.sendRedirect(contentGameService.tranferURI("BcsPage:ScratchCardPage:" + state, UID));
			} else {
				throw new Exception("Illegal JWT !");
			}			
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			String linkUrl = UriHelper.bcsMPage;
			response.sendRedirect(linkUrl);
			return;
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/Game/getAnnouncementUrl")
	public ResponseEntity<?> getAnnouncementUrl(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String accouncementUrl = CoreConfigReader.getString(CONFIG_STR.RICHART_ANNOUNCEMENT_URL.toString(), true);
		
		return new ResponseEntity<>(accouncementUrl, HttpStatus.OK);
	}
	
	/*
	 * 驗證 JWT 是否合法
	 */
	private boolean validateJWT(String secret, String header, String payload, String signature, String channelId, String nonce){
		logger.info("Validate the ID Token is legal or not.");
		try {
			Integer rem = signature.length() % 4;
			String message = header + "." + payload;
			Base64.Encoder base64UrlEncoder = Base64.getUrlEncoder();
			Base64.Decoder base64Decoder = Base64.getDecoder();
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");			
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
			
			sha256_HMAC.init(secret_key);
			
			String generated_signature = base64UrlEncoder.encodeToString(sha256_HMAC.doFinal(message.getBytes()));
			
			if(rem > 0) {
				for(Integer i = 0; i < (4 - rem); i++)
					signature += "=";
			}
			
			JSONObject payloadObject = new JSONObject(new String(base64Decoder.decode(payload), "UTF-8"));
			
			/* 檢查是否為合法的 issuer */
			if(!payloadObject.get("iss").toString().equals("https://access.line.me"))
				return false;
			
			/* 檢查是否為合法的 audience */
			if(!payloadObject.get("aud").toString().equals(channelId))
				return false;
			
			/* 檢查此 JWT 是否在有效期內 */
			Date expireTime = new Date(Long.valueOf(payloadObject.get("exp").toString()) * 1000);
			Date now = new Date();
			
			if(now.after(expireTime))
				return false;
			
			/* 如果有 nonce 的話，檢查 nonce 是否合法 */
			if(payloadObject.has("nonce")) {
				if(!payloadObject.get("nonce").toString().equals(nonce))
					return false;
			}
			
			/* 檢查 signature 是否合法 */
			return generated_signature.equals(signature);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(ErrorRecord.recordError(e));
			return false;
		}
	}
	
	/**
	 * 回傳一個沒有重覆的uuid
	 */
	public String checkDuplicateUUID(String queryType) {
		String uuid = UUID.randomUUID().toString().toLowerCase();
		Boolean duplicateUUID = winnerListService.checkDuplicateUUID(queryType, uuid);
		while (duplicateUUID) {
			uuid = UUID.randomUUID().toString().toLowerCase();
			duplicateUUID = winnerListService.checkDuplicateUUID(queryType, uuid);
		}

		return uuid;
	}
}
