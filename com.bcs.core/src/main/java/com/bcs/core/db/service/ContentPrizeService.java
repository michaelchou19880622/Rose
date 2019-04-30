package com.bcs.core.db.service;

import java.math.BigDecimal;
//import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentPrize;
import com.bcs.core.db.entity.PrizeList;
import com.bcs.core.db.repository.ContentCouponRepository;
import com.bcs.core.db.repository.ContentPrizeRepository;
import com.bcs.core.db.repository.PrizeListRepository;

@Service
public class ContentPrizeService {
	// private static final String GET_PRIZE_FLAG = "GET_PRIZE_FLAG";

	// public static final String GET_PRIZE_FLAG = "GET_PRIZE_FLAG";
	@Autowired
	private ContentPrizeRepository contentPrizeRepository;
	@Autowired
	private PrizeListRepository prizeListRepository;
	@Autowired
	private ContentCouponRepository contentCouponRepository;
	@Autowired
	private ActionUserCouponService actionUserCouponService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentPrizeService.class);

	/**
	 * 隨機抽選一個獎品
	 * @throws Exception 
	 */
	public String getRandomPrize(String gameId, String mid) throws Exception {
		ContentCoupon drewCoupon = null;
		List<ContentCoupon> contentCouponList = contentCouponRepository.findByEventReferenceAndEventReferenceId(ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD, gameId);
		String drewCouponId = null;
		Random random = new Random();
		BigDecimal index = new BigDecimal(random.ints(1, 10000).findFirst().getAsInt());
		BigDecimal accumulation = new BigDecimal("0");

		int prizeCount = contentCouponList.size();

		for (int i = 0; i < prizeCount; i++) {
			accumulation = accumulation.add(contentCouponList.get(i).getProbability());

			/* 判斷此優惠券是否為中獎優惠券 */
			if (accumulation.multiply(new BigDecimal("100")).compareTo(index) == 1) {
				drewCoupon = contentCouponList.get(i);

				logger.info("◎ 抽中的優惠券：" + drewCoupon);

				/* 判斷此優惠券無數量限制或是還有剩餘的數量， */
				if ((drewCoupon.getCouponGetLimitNumber() == null || drewCoupon.getCouponGetLimitNumber() == 0)
						|| (drewCoupon.getCouponGetLimitNumber() - drewCoupon.getCouponGetNumber()) > 0) {
					Date today = new Date();
					
					/* 判斷此優惠券是否在可領取的期間，如果是的話，便將 drewCouponId 設為此優惠券 id */
					if(today.compareTo(drewCoupon.getCouponStartUsingTime()) >= 0 && today.compareTo(drewCoupon.getCouponEndUsingTime()) < 0) {
						drewCouponId = drewCoupon.getCouponId();
						Date startUsingDate = drewCoupon.getCouponStartUsingTime();
						Date endUsingDate = drewCoupon.getCouponEndUsingTime();

						actionUserCouponService.createActionUserCoupon(mid, drewCouponId, startUsingDate, endUsingDate);

						break;
					}
				}
			}
		}

		return drewCouponId;
	}

	// public Integer getRandomPrize(String gameId, String mid) {
	// Integer prizeListId = -1;
	// String prizeId = "";
	//
	// List<ContentPrize> prizes = contentPrizeRepository.findByGameId(gameId);
	//
	// Random random = new Random();
	// BigDecimal index = new BigDecimal(random.ints(1,
	// 10000).findFirst().getAsInt());
	// BigDecimal accumulation = new BigDecimal("0");
	//
	// int prizeCount = prizes.size();
	// PrizeList prize;
	//
	// for(int i = 0; i < prizeCount; i++){
	// accumulation = accumulation.add(prizes.get(i).getPrizeProbability());
	//
	// if(accumulation.multiply(new BigDecimal("100")).compareTo(index)==1){
	// prizeId = prizes.get(i).getPrizeId();
	//
	// synchronized (GET_PRIZE_FLAG) {
	// prize = prizeListRepository.findNotWinnedOneByPrizeId(prizeId);
	//
	// if(prize != null){
	// prizeListId = prize.getPrizeListId();
	// prize.setStatus(PrizeList.PRIZE_STATUS_NOT_ACCEPTED);
	// prize.setModifyTime(new Date());
	// prize.setMid(mid);
	// prizeListRepository.save(prize);
	// }
	// break;
	// }
	// }
	// }
	//
	// if(prizeListId == -1){
	// for(int i = 0; i < prizeCount; i++){
	// if(prizes.get(i).getIsConsolationPrize()){
	// prizeId = prizes.get(i).getPrizeId();
	// prizeListId =
	// prizeListRepository.findByPrizeId(prizeId).get(0).getPrizeListId();
	// }
	// }
	// }
	//
	// return prizeListId;
	// }

	/**
	 * 
	 */
	public ContentPrize getPrizeByPrizeListId(Integer prizeListId) {
		String prizeId = "";
		ContentPrize contentPrize = new ContentPrize();

		prizeId = prizeListRepository.findOne(prizeListId).getPrizeId();
		logger.info("@@@@@@@@@@@prizeId" + prizeId);
		contentPrize = contentPrizeRepository.findOne(prizeId);

		return contentPrize;
	}

	/**
	 * 
	 */
	public void retrievePrize(Integer prizeListId) {
		PrizeList prize = prizeListRepository.findOne(prizeListId);
		prize.setStatus(PrizeList.PRIZE_STATUS_NOT_WINNED);
		prizeListRepository.save(prize);
	}

	public List<PrizeList> findNotAcceptedByMid(String mid) {
		return prizeListRepository.findNotAcceptedByMid(mid);
	}
}
