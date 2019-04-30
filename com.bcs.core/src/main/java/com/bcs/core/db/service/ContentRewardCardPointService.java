package com.bcs.core.db.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jcodec.common.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ActionUserRewardCard;
import com.bcs.core.db.entity.ActionUserRewardCardPointDetail;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.db.entity.ContentRewardCardPoint;
import com.bcs.core.db.repository.ContentRewardCardPointRepository;
import com.bcs.core.exception.BcsNoticeException;

@Service
public class ContentRewardCardPointService {
	@Autowired
	private ContentRewardCardPointRepository contentRewardCardPointRepository;
	@Autowired
	private ContentRewardCardService contentRewardCardService;
	@Autowired
	private ActionUserRewardCardService actionUserRewardCardService;
	@Autowired
	private ActionUserRewardCardPointDetailService actionUserRewardCardPointDetailService;
	
	@Transactional(rollbackFor=Exception.class, timeout = 180)
	public String getPoint(String mid, String rewardCardPointId) throws BcsNoticeException, ParseException {
		
		ContentRewardCardPoint crcp = contentRewardCardPointRepository.findByIdAndStatus(rewardCardPointId, ContentRewardCardPoint.STATUS_ACTIVE);
		if (crcp != null) {
			String contentRewardCardId = crcp.getRewardCardId();
			
			ActionUserRewardCard actionUserRewardCard = actionUserRewardCardService.findByMidAndRewardCardIdAndActionType(mid, contentRewardCardId, ActionUserRewardCard.ACTION_TYPE_GET);
			
			if (actionUserRewardCard != null) {
			    ContentRewardCard contentRewardCard = contentRewardCardService.findOne(contentRewardCardId);
			    
                if(contentRewardCard != null) {
			        
			        //驗證集點卡
			        String errorMsg = contentRewardCardService.checkContentRewardCard(contentRewardCard);
			        
			        if(errorMsg != null) {
			            return errorMsg;
			        }
			        
			        // 驗證符合領用次數限制
			        Long limitGetTime = contentRewardCard.getLimitGetTime();
			        boolean isLimitNumber = false;
			        if(limitGetTime < 24L && limitGetTime != 0L) {
			            isLimitNumber = actionUserRewardCardPointDetailService.existsByUserRewardCardIdAndLimitGetNumberAndLimitGetTime(contentRewardCardId, new Date(), ActionUserRewardCardPointDetail.POINT_TYPE_AUTOMATIC);     
			        }
			        else if(limitGetTime == 24L){
			            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			            Date now = new Date();
			            Calendar calendarEnd = Calendar.getInstance();
			            calendarEnd.setTime(now);
			            calendarEnd.add(Calendar.DATE, 1);
			            
			            Long UserRewardCardPointDetaiLength = actionUserRewardCardPointDetailService.countByUserRewardCardIdAndReferenceIdAndPointType(actionUserRewardCard.getId(), String.valueOf(contentRewardCardId), 
			                    ActionUserRewardCardPointDetail.POINT_TYPE_AUTOMATIC, sdf.format(now), sdf.format(calendarEnd.getTime()));
			            
			            if(UserRewardCardPointDetaiLength >= contentRewardCard.getLimitGetNumber()){
			                isLimitNumber = true;
			            }  
			        }
			        
			        Logger.debug("actionUserRewardCardPointDetail isLimitNumber=" + isLimitNumber);
			        
			        if(isLimitNumber) {
			            return "失敗:此集點卡在" + contentRewardCard.getLimitGetTime() + "小時內, 只能集" + contentRewardCard.getLimitGetNumber() + "點";
			        }
			        
			       
			        //驗證是否領過此QRCode點數
			        boolean exists = actionUserRewardCardPointDetailService.existsByUserRewardCardIdAndRewardCardPointId(actionUserRewardCard.getId(), rewardCardPointId);
			        Logger.debug("actionUserRewardCardPointDetail exists=" + exists);
			        
			        //是否超過設定點數
			        Integer havePoint = actionUserRewardCardPointDetailService.sumActionUserRewardCardGetPoint(actionUserRewardCard.getId());
			        Integer maxPoint = Integer.parseInt(contentRewardCard.getRequirePoint().toString());
			        Logger.info("maxPoint:"+maxPoint+",havePoint:"+havePoint);
			        
			        if (exists) {
			            return "失敗:您已集過此點數";
			        } else if(havePoint>=maxPoint){
			        	return "失敗:您的點數已滿，不能再蒐集點數";
			        }else {
			            actionUserRewardCardPointDetailService.createForUse(mid, actionUserRewardCard, 1, String.valueOf(contentRewardCardId), rewardCardPointId, ActionUserRewardCardPointDetail.POINT_TYPE_AUTOMATIC);
			            return "成功:您已集了1點";
			        }
			    }else {
			        return "noCard";
			    }
			}else {
			    return "NotYetGetCard";
			}
		}else {
		    return "noCard";
		}
	}
	
	public ContentRewardCardPoint findByIdAndStatus(String id, String status) {
	    return contentRewardCardPointRepository.findByIdAndStatus(id, status);
	}
	
	public void save(ContentRewardCardPoint contentRewardCardPoint) {
	    contentRewardCardPointRepository.save(contentRewardCardPoint);
	}
	
	public List<ContentRewardCardPoint> findByRewardCardIdAndStatus(String rewardCardId, String status){
	    return contentRewardCardPointRepository.findByRewardCardIdAndStatus(rewardCardId, status);
	}
	
	public void updateStatusByRewardCardIdAndStatus(String rewardCardId, String oldStatus, String newStatus) {
	    contentRewardCardPointRepository.updateStatusByRewardCardIdAndStatus(rewardCardId, oldStatus, newStatus);
	}
	
    public String generateRewardCardPointId() {
        String rewardCardPointId = UUID.randomUUID().toString().toLowerCase();
        
        while (contentRewardCardPointRepository.findOne(rewardCardPointId) != null) {
            rewardCardPointId = UUID.randomUUID().toString().toLowerCase();
        }
        return rewardCardPointId;
    }
    
    public ContentRewardCardPoint findOne(String id) {
        return contentRewardCardPointRepository.findOne(id);
    }
}
