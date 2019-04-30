package com.bcs.web.ui.service;

import java.io.IOException;
// import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentCouponCode;
import com.bcs.core.db.entity.ContentFlag;
import com.bcs.core.db.entity.ContentGame;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.db.service.ContentCouponCodeService;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.service.ContentFlagService;
import com.bcs.core.db.service.ContentGameService;
import com.bcs.core.db.service.ContentRewardCardService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.upload.ImportDataFromExcel;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.web.ui.model.ContentCouponModel;

@Service
public class ContentCouponUIService {

	private static Logger logger = Logger.getLogger(ContentCouponUIService.class);

	@Autowired
	private ContentCouponService contentCouponService;
	@Autowired
	private ContentFlagService contentFlagService;
	@Autowired
	private ImportDataFromExcel importDataFromExcel;
	@Autowired
	private ContentCouponCodeService contentCouponCodeService;
	@Autowired
	private ContentRewardCardService contentRewardCardService;
	@Autowired
	private ContentGameService contentGameService;

	/**
	 * 新增或修改優惠劵
	 * 
	 * @param contentCouponModel
	 * @param adminUserAccount
	 * @return ContentCoupon
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class, timeout = 1800)
	public ContentCoupon saveFromUI(ContentCouponModel contentCouponModel, MultipartFile couponCodeListData, String adminUserAccount) throws Exception {
		return this.saveFromUI(contentCouponModel, couponCodeListData, adminUserAccount, true);
	}

	/**
	 * 新增或修改優惠劵
	 * 
	 * @param contentCouponModel
	 * @param adminUserAccount
	 * @param doSave
	 * @return ContentCoupon
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class, timeout = 30)
	public ContentCoupon saveFromUI(ContentCouponModel contentCouponModel, MultipartFile couponCodeListData, String adminUserAccount, boolean doSave) throws Exception {
		logger.info("saveFromUI:" + contentCouponModel);
		Date time = new Date();

		ContentCoupon contentCoupon = contentCouponModel.getContentCoupon();
		String action = (contentCoupon.getCouponId() == null ? "Create" : "Edit");

		// 若是修改
		if (contentCoupon.getCouponId() != null && doSave) {
			contentCoupon = mergeOldData(contentCoupon);
		}

		String status = contentCoupon.getStatus();
		if (StringUtils.isBlank(status)) {
			contentCoupon.setStatus(ContentCoupon.COUPON_STATUS_DISABLE);
		}

		contentCoupon.setCouponType(contentFlagService.concat(contentCouponModel.getFlagValueList(), 50));

		// Set Modify Admin User
		contentCoupon.setModifyUser(adminUserAccount);
		contentCoupon.setModifyTime(time);
		if (doSave) {
			contentCouponService.save(contentCoupon);
			contentCoupon = contentCouponService.findOne(contentCoupon.getCouponId());

			// 優惠券上傳序號
			if (contentCoupon.getIsCouponCode().equals(ContentCoupon.IS_COUPON_CODE_TRUE)) {
				if (couponCodeListData != null) {
					saveContentCodeCouponByFile(contentCoupon, couponCodeListData, adminUserAccount, time);
				}

				Integer couponGetLimitNumber = contentCouponCodeService.findCouponCodeListNumber(contentCoupon.getCouponId());
				contentCoupon.setCouponGetLimitNumber(couponGetLimitNumber);

			}
			// Save ContentFlag
			contentFlagService.save(String.valueOf(contentCoupon.getCouponId()), ContentFlag.CONTENT_TYPE_COUPON, contentCouponModel.getFlagValueList());

			createSystemLog(action, contentCoupon, contentCoupon.getModifyUser(), contentCoupon.getModifyTime(), contentCoupon.getCouponId().toString());
		}
		return contentCoupon;
	}

	public void checkContentCoupon(ContentCouponModel contentCouponModel) {
		ContentCoupon contentCoupon = contentCouponModel.getContentCoupon();
		Validate.notBlank(contentCoupon.getCouponTitle(), "Coupon Title Null");
		Validate.notBlank(contentCoupon.getCouponListImageId(), "Coupon List Image Null");
		Validate.notBlank(contentCoupon.getCouponImageId(), "Coupon Image Null");
		Validate.notNull(contentCoupon.getCouponStartUsingTime(), "Coupon Start Using Time Null");
		Validate.notNull(contentCoupon.getCouponEndUsingTime(), "Coupon End Using Time Null");
		Validate.notBlank(contentCoupon.getCouponUsingLimit(), "Coupon Using Limit Null");
	}

	/**
	 * 刪除優惠劵
	 * 
	 * @param couponId
	 * @param adminUserAccount
	 * @throws BcsNoticeException
	 */

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void deleteFromUI(String couponId, String adminUserAccount) throws BcsNoticeException {
		logger.info("deleteFromUI:" + couponId);
		String couponTitle = contentCouponService.findCouponTitleByCouponId(couponId);
		ContentCoupon coupon = contentCouponService.findOne(couponId);

		if (coupon.getEventReferenceId() != null) {
			switch (coupon.getEventReference()) {
			case ContentCoupon.EVENT_REFERENCE_REWARD_CARD:
				logger.info("ContentCoupon.EVENT_REFERENCE_REWARD_CARD:" + ContentCoupon.EVENT_REFERENCE_REWARD_CARD);
				String rewardCardId = coupon.getEventReferenceId();
				ContentRewardCard contentRewardCard = contentRewardCardService.findOne(rewardCardId);
				logger.info("contentRewardCard.getStatus():" + contentRewardCard.getStatus());
				logger.info("ContentRewardCard.REWARD_CARD_STATUS_ACTIVE:" + ContentRewardCard.REWARD_CARD_STATUS_ACTIVE);
				if (contentRewardCard.getStatus().equals(ContentRewardCard.REWARD_CARD_STATUS_ACTIVE)) {
					throw new BcsNoticeException("請先刪除集點卡:" + contentRewardCard.getRewardCardMainTitle() + ",再刪除優惠券");
				}
				break;
			case ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD:
				String scratchCardId = coupon.getEventReferenceId();
				logger.info("ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD:" + ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD);
				ContentGame scratchCard = contentGameService.findOne(scratchCardId);
				logger.info("scratchCard.getStatus():" + scratchCard.getStatus());
				if (scratchCard.getStatus().equals(ContentGame.STATUS_ACTIVE)) {
					throw new BcsNoticeException("請先刪除刮刮樂:" + scratchCard.getGameName() + ",再刪除優惠券");
				}
				break;
			}
		}

		coupon.setStatus(ContentCoupon.COUPON_STATUS_DELETE);
		coupon.setModifyTime(new Date());
		coupon.setModifyUser(adminUserAccount);
		contentCouponService.save(coupon);

		createSystemLog("Delete", couponTitle, adminUserAccount, new Date(), couponId.toString());
	}

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void switchContentCouponStatus(String couponId, String adminUserAccount) throws BcsNoticeException{
		// 切換狀態
		ContentCoupon coupon = contentCouponService.findOne(couponId);

		String status = coupon.getStatus();
		if (ContentCoupon.COUPON_STATUS_ACTIVE.equals(status)) {
			status = ContentCoupon.COUPON_STATUS_DISABLE;
		} else if (ContentCoupon.COUPON_STATUS_DISABLE.equals(status)) {
			status = ContentCoupon.COUPON_STATUS_ACTIVE;
		} else {
			throw new BcsNoticeException("請選擇正確的優惠券");
		}

		coupon.setStatus(status);
		coupon.setModifyTime(new Date());
		coupon.setModifyUser(adminUserAccount);
		contentCouponService.save(coupon);

		createSystemLog("SwitchStatus", coupon.getCouponTitle(), adminUserAccount, new Date(), couponId.toString());
	}

	/**
	 * 新增系統日誌
	 * 
	 * @param action
	 * @param content
	 * @param modifyUser
	 * @param modifyTime
	 */
	private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime,
			String referenceId) {
		SystemLogUtil.saveLogDebug("ContentCoupon", action, modifyUser, content, referenceId);
	}

	/**
	 * 將前端修改的欄位合併到原 entity，避免有些不在頁面上的欄位被清空
	 * 
	 * @param newContentCoupon
	 * @return
	 * @throws Exception
	 */
	private ContentCoupon mergeOldData(ContentCoupon newContentCoupon) throws Exception {

		// 原資料
		ContentCoupon oldContentCoupon = contentCouponService.findOne(newContentCoupon.getCouponId());

		oldContentCoupon.setCouponTitle(newContentCoupon.getCouponTitle());
		oldContentCoupon.setCouponListImageId(newContentCoupon.getCouponListImageId());
		oldContentCoupon.setCouponImageId(newContentCoupon.getCouponImageId());
		oldContentCoupon.setCouponType(newContentCoupon.getCouponType());
		oldContentCoupon.setCouponStartUsingTime(newContentCoupon.getCouponStartUsingTime());
		oldContentCoupon.setCouponEndUsingTime(newContentCoupon.getCouponEndUsingTime());
		oldContentCoupon.setCouponSerialNumber(newContentCoupon.getCouponSerialNumber());
		oldContentCoupon.setCouponUsingLimit(newContentCoupon.getCouponUsingLimit());
		oldContentCoupon.setCouponDescription(newContentCoupon.getCouponDescription());
		oldContentCoupon.setCouponUseDescription(newContentCoupon.getCouponUseDescription());
		oldContentCoupon.setCouponRuleDescription(newContentCoupon.getCouponRuleDescription());
		oldContentCoupon.setCouponStartGetTime(newContentCoupon.getCouponStartGetTime());
		oldContentCoupon.setCouponEndGetTime(newContentCoupon.getCouponEndGetTime());
		oldContentCoupon.setCouponGetLimitNumber(newContentCoupon.getCouponGetLimitNumber());
		oldContentCoupon.setCouponFlag(newContentCoupon.getCouponFlag());
		oldContentCoupon.setIsCouponCode(newContentCoupon.getIsCouponCode());
		oldContentCoupon.setIsFillIn(newContentCoupon.getIsFillIn());
		oldContentCoupon.setCouponRemark(newContentCoupon.getCouponRemark());

		return oldContentCoupon;
	}

	private void saveContentCodeCouponByFile(ContentCoupon contentCoupon, MultipartFile couponCodeListData,
			String adminUserAccount, Date time) throws IOException, Exception {
		String fileName = couponCodeListData.getOriginalFilename();
		logger.info("getOriginalFilename:" + fileName);
		String contentType = couponCodeListData.getContentType();
		logger.info("getContentType:" + contentType);

		List<Map<String, String>> dataMap = null;

		if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) || "application/vnd.ms-excel".equals(contentType)) {
			dataMap = importDataFromExcel.importCSVDataKeyValueList(couponCodeListData.getInputStream());
		}

		String serialNumber = "SERIALNUMBER";
		if (dataMap != null && dataMap.size() > 0) {
			for (Map<String, String> data : dataMap) {
				if (!data.containsKey(serialNumber)) {
					throw new BcsNoticeException("欄位錯誤，應為 " + serialNumber);
				}
				ContentCouponCode contentCouponCode = new ContentCouponCode();
				contentCouponCode.setCouponCode(data.get(serialNumber));
				contentCouponCode.setCouponId(contentCoupon.getCouponId());
				contentCouponCode.setStatus(ContentCouponCode.COUPON_CODE_IS_NOT_USE);
				contentCouponCode.setModifyTime(time);
				contentCouponCode.setModifyUser(adminUserAccount);
				try {
					contentCouponCodeService.save(contentCouponCode);
				} catch (Exception e) {
					String error = ErrorRecord.recordError(e, false);
					logger.error(error);
					throw new BcsNoticeException("優惠券錯誤：錯誤的電子序號為" + data.get(serialNumber) + "，請重新上傳正確之檔案。");
				}
			}
		}
	}

	@SuppressWarnings("unused")
	public int countNumberForCouponCodeListFile(MultipartFile couponCodeListData) throws IOException, Exception {
		String fileName = couponCodeListData.getOriginalFilename();
		logger.info("getOriginalFilename:" + fileName);
		String contentType = couponCodeListData.getContentType();
		logger.info("getContentType:" + contentType);

		List<Map<String, String>> dataMapList = null;

		if ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) || "application/vnd.ms-excel".equals(contentType)) {
			dataMapList = importDataFromExcel.importCSVDataKeyValueList(couponCodeListData.getInputStream());
		}

		if (dataMapList != null && dataMapList.size() > 0) {
			if (dataMapList != null) {
				return dataMapList.size();
			} else {
				throw new BcsNoticeException("無電子序號");
			}
		} else
			return 0;
	}
}
