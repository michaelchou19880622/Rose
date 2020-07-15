package com.bcs.web.ui.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ActionUserRewardCard;
import com.bcs.core.db.entity.ActionUserRewardCardPointDetail;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.db.service.ActionUserCouponService;
import com.bcs.core.db.service.ActionUserRewardCardPointDetailService;
import com.bcs.core.db.service.ActionUserRewardCardService;
import com.bcs.core.db.service.ContentRewardCardService;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.UserTraceLogUtil;
// import com.bcs.core.model.RewardCardModel;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.upload.ImportDataFromExcel;
// import com.mysql.fabric.xmlrpc.base.Array;

@Service
public class ActionUserRewardCardUIService {


	private static Logger logger = Logger.getLogger(ActionUserRewardCardUIService.class);

	@Autowired
	private ActionUserCouponService actionUserCouponService;
	@Autowired
	private ActionUserRewardCardService actionUserRewardCardService;

	@Autowired
	private ContentRewardCardService contentRewardCardService;
	@Autowired
	private ActionUserRewardCardPointDetailService actionUserRewardCardPointDetailService;
	@Autowired
	private ImportDataFromExcel importDataFromExcel;

	/**
	 * 新增領用(GET)集點卡記錄
	 * 
	 * @param mid
	 * @param rewardCardId
	 */
	public ActionUserRewardCard createFromUIForGet(String mid, String rewardCardId, Date sdate, Date edate) {
		return actionUserRewardCardService.createForGet(mid, rewardCardId, sdate, edate);
	}

	/**
	 * 修改集點卡狀態
	 * 
	 * @param mid
	 * @param rewardCardId
	 */
	public void modifyRewardCardActionType(String mid, Long rewardCardId, ActionUserRewardCard actionUserRewardCard) {

		actionUserRewardCard.setActionType(ActionUserRewardCard.ACTION_TYPE_USE);

		actionUserRewardCardService.save(actionUserRewardCard);

		UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_REWARD_CARD,
				LOG_TARGET_ACTION_TYPE.ACTION_RewardCardUse, mid, actionUserRewardCard, rewardCardId.toString());

	}

	/**
	 * 新增使用(USE)集點卡記錄 (取得點數紀錄)
	 * 
	 * @param mid
	 * @param actionUserRewardCard
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public void createFromUIForUse(String mid, ActionUserRewardCard actionUserRewardCard, int getAmount) {
		logger.info("createFromUIForUse mid : " + mid + ", actionUserRewardCard : " + actionUserRewardCard
				+ ", getAmount : " + getAmount);

		actionUserRewardCardPointDetailService.createForUse(mid, actionUserRewardCard, getAmount,
				String.valueOf(actionUserRewardCard.getRewardCardId()));
	}

	/**
	 * 修改actionUserRewardCard下次可以取得點數的時間(防止不當使用設定)
	 * 
	 * @param mid
	 * @param actionUserRewardCard
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public void modifyGetPointTime(String mid, ActionUserRewardCard actionUserRewardCard,
			ContentRewardCard contentRewardCard) {
		logger.info("createFromUIForUse  actionUserRewardCard : " + actionUserRewardCard + ", contentRewardCard : "
				+ contentRewardCard);

		actionUserRewardCard.setNextGetPointTime(new Date(actionUserRewardCard.getNextGetPointTime().getTime()
				+ (1000 * 60 * 60 * contentRewardCard.getLimitGetTime())));

		actionUserRewardCardService.save(actionUserRewardCard);

		UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_REWARD_CARD,
				LOG_TARGET_ACTION_TYPE.ACTION_CouponUse, mid, actionUserRewardCard,
				actionUserRewardCard.getRewardCardId().toString());
	}

	/**
	 * 集點卡是否在使用期間
	 * 
	 * @param contentRewardCard
	 * @return
	 */
	public boolean isInUsingTime(ContentRewardCard contentRewardCard) {
		Date today = new Date();
		return today.compareTo(contentRewardCard.getRewardCardStartUsingTime()) >= 0
				&& today.compareTo(contentRewardCard.getRewardCardEndUsingTime()) < 0;
	}

	/**
	 * 集點卡是否在使用期間
	 * 
	 * @param actionUserRewardCard
	 * @return
	 */
	public boolean isInUsingTime(ActionUserRewardCard actionUserRewardCard) {
		Date today = new Date();
		return today.compareTo(actionUserRewardCard.getRewardCardStartUsingTime()) >= 0
				&& today.compareTo(actionUserRewardCard.getRewardCardEndUsingTime()) < 0;
	}

	/**
	 * 優惠劵是否超過使用限制而不准使用
	 * 
	 * @param contentCoupon
	 * @return
	 */
	public boolean isDisabledByUsingLimit(String mid, ContentCoupon contentCoupon) {

		// 僅限一次
		if (ContentCoupon.COUPON_USING_LIMIT_ONCE.equals(contentCoupon.getCouponUsingLimit())) {
			return actionUserCouponService.existsByMidAndCouponIdAndActionType(mid, contentCoupon.getCouponId(),
					ActionUserCoupon.ACTION_TYPE_USE);
		}

		// 一天一次
		if (ContentCoupon.COUPON_USING_LIMIT_ONCE_A_DAY.equals(contentCoupon.getCouponUsingLimit())) {
			return actionUserCouponService.existsByMidAndCouponIdAndActionTypeAndActionTimeInToday(mid,
					contentCoupon.getCouponId(), ActionUserCoupon.ACTION_TYPE_USE);
		}

		// 有效期間內不限次數
		return false;
	}

	/**
	 * Find By Id
	 * 
	 * @param mid
	 * @param id
	 * @return
	 */
	public ActionUserRewardCard findOne(Long rewardCardId) {
		return actionUserRewardCardService.findOne(rewardCardId);
	}

	/**
	 * FindBy Mid And CouponId And ActionType ACTION_TYPE_GET
	 * 
	 * @param mid
	 * @param couponId
	 * @return
	 */
	public ActionUserRewardCard findByMidAndRewardCardIdAndActionType(String mid, String rewardCardId) {
		return actionUserRewardCardService.findByMidAndRewardCardIdAndActionType(mid, rewardCardId,
				ActionUserRewardCard.ACTION_TYPE_GET);
	}

	/**
	 * 集點卡是否已被使用者領用過
	 * 
	 * @param mid
	 * @param rewardCardId
	 * @return
	 */
	public boolean isGetRewardCard(String mid, String rewardCardId) {
		return actionUserRewardCardService.existsByMidAndRewardCardIdAndActionType(mid, rewardCardId,
				ActionUserRewardCard.ACTION_TYPE_GET);
	}

	/**
	 * 集點卡是否已集點成功
	 * 
	 * @param mid
	 * @param rewardCardId
	 * @return
	 */
	public boolean isUseRewardCard(String mid, String rewardCardId) {
		return actionUserRewardCardService.existsByMidAndRewardCardIdAndActionType(mid, rewardCardId,
				ActionUserRewardCard.ACTION_TYPE_USE);
	}

	/**
	 * 集點卡是否在領用期間
	 * 
	 * @param actionUserRewardCard
	 */
	public boolean isInGetTime(ContentRewardCard contentRewardCard) {
		Date rewardCardStartGetTime = contentRewardCard.getRewardCardStartGetTime();
		Date rewardCardEndGetTime = contentRewardCard.getRewardCardEndGetTime();

		if (rewardCardStartGetTime == null && rewardCardEndGetTime == null) {
			return isInUsingTime(contentRewardCard);
		}

		Date today = new Date();
		return today.compareTo(rewardCardStartGetTime) >= 0 && today.compareTo(rewardCardEndGetTime) < 0;
	}

	/**
	 * 優惠劵是否符合領用次數限制
	 * 
	 * @param contentCoupon
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean isGetNumberAdd1LessOrEqualsLimit(ContentCoupon contentCoupon) {
		Integer getLimit = contentCoupon.getCouponGetLimitNumber();
		int getNumber = contentCoupon.getCouponGetNumber();

		// 若領用次數限制為零表示不限制次數
		if (getLimit == null || getLimit == 0) {
			return true;
		}

		return (getNumber + 1) <= getLimit;
	}

	/**
	 * Setting RewardCard ImageId to Url
	 * 
	 * @param imageId
	 * @return
	 */
	public String settingRewardCardImage(String imageId) {

		if (StringUtils.isNotBlank(imageId)) {
			if (imageId.toLowerCase().startsWith("http")) {

			} else {
				imageId = UriHelper.getCdnResourceUri("IMAGE", imageId, true);
				//imageId = UriHelper.getResourceUri("IMAGE", imageId, true);
			}
		}

		return imageId;
	}

	/**
	 * Change RewardCard Description
	 * 
	 * @param contentRewardCard
	 */
	public void changeRewardCardDescription(ContentRewardCard contentRewardCard) {
		contentRewardCard.setRewardCardDescription(replaceBr(contentRewardCard.getRewardCardDescription()));
	}

	/**
	 * Replace <br>
	 * to \n
	 * 
	 * @param input
	 * @return
	 */
	public String replaceBr(String input) {

		if (StringUtils.isNotBlank(input)) {
			input = input.replaceAll("<br>", "\n");
			input = input.replaceAll("<Br>", "\n");
		}

		return input;
	}

	/**
	 * GenerateCouponSerialNumber
	 * 
	 * @param contentCoupon
	 * @param sessionMID
	 * @param couponId
	 * @return
	 */

	public String generateCouponSerialNumber(ContentCoupon contentCoupon, String sessionMID, String couponId){
		String couponSerialNumber = contentCoupon.getCouponSerialNumber();

		if (StringUtils.isNotBlank(couponSerialNumber)) {
			Long couponSIndex = actionUserCouponService.findCouponSIndexByMidAndCouponIdAndActionType(sessionMID,
					couponId, ActionUserCoupon.ACTION_TYPE_GET);
			String strCouponSIndex = (couponSIndex == null ? "" : String.valueOf(couponSIndex));
			StringBuffer sb = new StringBuffer();
			Matcher m = Pattern.compile("\\{(\\d+)\\}").matcher(couponSerialNumber);

			while (m.find()) {
				int padZeroSize = Integer.parseInt(m.group(1));
				m.appendReplacement(sb, StringUtils.leftPad(strCouponSIndex, padZeroSize, "0"));
			}

			m.appendTail(sb);
			couponSerialNumber = sb.toString();
		}

		return couponSerialNumber;
	}

	@Transactional(rollbackFor = Exception.class)
	public void createFromUIForUse(String mid, ActionUserRewardCard actionUserRewardCard, int getAmount,
			String referenceId) {
		logger.info("createFromUIForUse mid : " + mid + ", actionUserRewardCard : " + actionUserRewardCard
				+ ", getAmount : " + getAmount);

		actionUserRewardCardPointDetailService.createForUse(mid, actionUserRewardCard, getAmount, referenceId);
	}

	@SuppressWarnings("unused")
	public enum ACTION_USER_REWARDCARD_ENUM {
		UID("UID"), POINT("POINT");

		private String colum_en;

		private ACTION_USER_REWARDCARD_ENUM(String colum_en) {
			this.colum_en = colum_en;
		}
	}

	// 手動補點
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> manuallyCreateRewardCardPoint(String rewardCardId, MultipartFile filePart,
			Date modifyTime) throws IOException, Exception {
		String fileName = filePart.getOriginalFilename();
		
        logger.info("getOriginalFilename:" + fileName);
        
        String contentType = filePart.getContentType();
        
        logger.info("getContentType:" + contentType);
        logger.info("getSize:" + filePart.getSize());
        
        List<Map<String, String>> dataMap = null;
        if("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) || "application/vnd.ms-excel".equals(contentType) || "text/csv".equals(contentType)){
        	  dataMap = importDataFromExcel.importCSVDataKeyValueList(filePart.getInputStream());    
        }

        int count = 0;
        int overPointCount = 0;
        List<String> overPointUsers = new ArrayList<>();
        ContentRewardCard rewardCard = contentRewardCardService.findOne(rewardCardId);
        Integer maxPoint = Integer.parseInt(rewardCard.getRequirePoint().toString());
        
        if(dataMap != null && dataMap.size() > 0){	
        	//將資料匯入資料庫
        	for (Map<String,String> data : dataMap) { 
        			logger.info(data);
                	//判斷欄位是否正確
                	if(!data.containsKey(ACTION_USER_REWARDCARD_ENUM.UID.toString()) || !data.containsKey(ACTION_USER_REWARDCARD_ENUM.POINT.toString())){
                		throw new BcsNoticeException("欄位錯誤，\n應為 "+ACTION_USER_REWARDCARD_ENUM.UID.toString()+","+ACTION_USER_REWARDCARD_ENUM.POINT.toString());
                	}
        			String UID = data.get(ACTION_USER_REWARDCARD_ENUM.UID.toString());
        			Integer point = Integer.parseInt(data.get(ACTION_USER_REWARDCARD_ENUM.POINT.toString()));
        	
        			ActionUserRewardCard actionUserRewardCard =actionUserRewardCardService.findByMidAndRewardCardIdAndActionType(UID, rewardCardId, ActionUserRewardCard.ACTION_TYPE_GET);
        			
        			
        			if(actionUserRewardCard==null){//無領卡不能手動補點
    					throw new BcsNoticeException("\n第"+(count+overPointCount+1)+"筆資料錯誤，\n"+
						"錯誤資訊為：\n"+
						"此MID\""+UID+"\"之使用者未領取集點卡，請修正再上傳檔案\n");
        			}else{//手動補點
        				Integer havePoint = actionUserRewardCardPointDetailService.sumActionUserRewardCardGetPoint(actionUserRewardCard.getId());
        				Long userRewardCardId =actionUserRewardCard.getId();
        				ActionUserRewardCardPointDetail actionUserRewardCardPointDetail=new ActionUserRewardCardPointDetail();
        				actionUserRewardCardPointDetail.setUserRewardCardId(userRewardCardId);
        				actionUserRewardCardPointDetail.setReferenceId(rewardCardId.toString());
        				//超過集點卡點數就設定最大上限點數
        				logger.info((havePoint+point > maxPoint)? maxPoint-havePoint : point);
        				actionUserRewardCardPointDetail.setPointGetTime(modifyTime);
        				actionUserRewardCardPointDetail.setPointType(ActionUserRewardCardPointDetail.POINT_TYPE_MANUAL);
        				
        				if(havePoint+point > maxPoint){
        					actionUserRewardCardPointDetail.setPointGetAmount(maxPoint-havePoint);
        					if(maxPoint-havePoint == 0){
        						overPointCount++;
        						overPointUsers.add(UID);
        						continue;
        					}
        				}else{
        					actionUserRewardCardPointDetail.setPointGetAmount(point);
        				}
        				actionUserRewardCardPointDetailService.save(actionUserRewardCardPointDetail);
        				count++;
        				
        				
        			}	
        	}
        	
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("count", count);
            result.put("overPointCount", overPointCount);
            result.put("overPointUsers", overPointUsers);
            return result;
        }else
        	throw new BcsNoticeException("此檔案無資料");
	}
}
