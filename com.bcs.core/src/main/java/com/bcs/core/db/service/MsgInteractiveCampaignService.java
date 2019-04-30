package com.bcs.core.db.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Campaign;
import com.bcs.core.db.entity.Invoice;
import com.bcs.core.db.entity.MsgInteractiveCampaign;
import com.bcs.core.db.repository.ContentPrizeRepository;
import com.bcs.core.db.repository.MsgInteractiveCampaignRepository;
import com.bcs.core.db.repository.PrizeListRepository;
import com.bcs.core.invoice.service.InvoiceStatus;
import com.bcs.core.model.WinnedCouponModel;

@Service
public class MsgInteractiveCampaignService {
	@Autowired
	private MsgInteractiveCampaignRepository msgInteractiveCampaignRepository;
	@Autowired
	private CampaignService campaignService;
    @Autowired
    private LineUserService lineUserService;
	@Autowired
	private UserFieldSetService userFieldSetService;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private ContentAcceptPrizeService contentAcceptPrizeService;
    @Autowired
    private ContentPrizeRepository contentPrizeRepository;
    @Autowired
    private PrizeListRepository prizeListRepository;
    @Autowired
    private MsgInteractiveCampaignService msgInteractiveCampaignService;
    @Autowired
    private WinnerListService winnerListService;

    
    /** Logger */
    private static Logger logger = Logger.getLogger(CampaignService.class);

	public void save(MsgInteractiveCampaign msgInteractiveCampaign){
		msgInteractiveCampaignRepository.save(msgInteractiveCampaign);
	}

	public MsgInteractiveCampaign findByiMsgId(Long iMsgId){
		return msgInteractiveCampaignRepository.findByiMsgId(iMsgId);
	}
	
	public void deleteByiMsgId(Long iMsgId){
	    MsgInteractiveCampaign msgInteractiveCampaign = msgInteractiveCampaignRepository.findByiMsgId(iMsgId);
	    if(msgInteractiveCampaign != null){
	    	msgInteractiveCampaignRepository.delete(msgInteractiveCampaign);
	    }
	}

    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public List<Map<String, Object>> findCampaignUserList(Long iMsgId, String startDate, String endDate) {
        logger.debug("findCampaignUserList:" + iMsgId);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
//        MsgInteractiveCampaign iMsgCampaign = msgInteractiveCampaignService.findByiMsgId(iMsgId);
//        
//        String campaignId = iMsgCampaign.getCampaignId();
//        if (campaignId != null) {
//            Campaign campaign = campaignService.findOne(campaignId);
//            Long gameId = campaign.getGameId();
//            
//            
//            if (gameId != null) {
//                List<WinnedCouponModel> winnerList = null;
//                if (startDate == null || endDate == null) {
//                    winnerList = winnerListService.getWinnerList(gameId);
//                } else {
//                    try {
//                        winnerList = winnerListService.queryWinnerList(gameId,startDate,endDate);
//                    } catch (Exception e) {
//                        logger.error(e);
//                    }
//                }
//                
//                if (winnerList != null) {
//                    List<Invoice> invoiceList = invoiceService.findByCampaignId(campaignId);
//                    for (List<String> values : winnerList.values()) {
//                        
//                        Map<String, Object> data = new HashMap<String, Object>();
//                        data.put("userName", values.get(0));
//                        data.put("modifyTime",  values.get(4));
//                        data.put("mobile",  values.get(1));
//                        data.put("address",  values.get(2));
//                        data.put("prizeName",  values.get(13));
//                        data.put("UID",  values.get(12));
//                        
//                        Long prizeListId = (values.get(14) != null ? Long.valueOf(14) : -1L);
//                        
//                        for (Invoice invoice : invoiceList) {
//                            if (invoice.getPrizeListId() != null && 
//                                    invoice.getPrizeListId() == prizeListId) {
//                                data.put("invNum", invoice.getInvNum());
//                                data.put("invStatus", InvoiceStatus.toChinese(invoice.getStatus()));
//                                data.put("resourceId", invoice.getPicPath());
//                                break;
//                            }
//                        } 
//                        result.add(data);
//                    }
//                }
                
                
//                List<ContentPrize> contentPrizeList = contentPrizeRepository.findByGameId(gameId);
//                List<ContentAcceptedPrize> contentAcceptedPrizeList = contentAcceptPrizeService.findByGameId(gameId);
//                
//                
//                for (ContentAcceptedPrize cap : contentAcceptedPrizeList) {
//                    String MID = cap.getMid();
//                    if (MID != null) {
//                        Map<String, Object> data = new HashMap<String, Object>();
//                        data.put("userName", cap.getUserName());
//                        data.put("modifyTime", cap.getModifyTime());
//
//                        LineUser lineUser = lineUserService.findByMid(MID);
//                        if (lineUser != null) {
//                            data.put("mobile", lineUser.getMobile());
//                        }
//                        
//                        List<UserFieldSet> addressList = userFieldSetService.findByMidAndKeyData(MID, "Address");
//                        if (addressList != null) {
//                            Collections.sort(addressList, new Comparator<UserFieldSet>() {
//                                @Override
//                                public int compare(UserFieldSet o1, UserFieldSet o2) {
//                                    //時間排序由近至遠
//                                    return o1.getSetTime().compareTo(o2.getSetTime()) * -1;
//                                }
//                            });
//                            
//                            data.put("address", addressList.get(0).getValue());
//                        }
//                        
//                        
//                        PrizeList prizeList = prizeListRepository.findOne(cap.getPrizeListId());
//                        if (prizeList != null) {
//                            for (ContentPrize prize : contentPrizeList) {
//                                if (prizeList.getPrizeId().equals(prize.getPrizeId())) {
//                                    data.put("prizeName", prize.getPrizeName());
//                                    break;
//                                }
//                            } 
//                        }
//                        if (invoiceList != null && prizeList != null) {
//                            for (Invoice invoice : invoiceList) {
//                                if (invoice.getPrizeListId() != null && 
//                                        invoice.getPrizeListId().intValue() == prizeList.getPrizeListId()) {
//                                    data.put("invNum", invoice.getInvNum());
//                                    data.put("invStatus", InvoiceStatus.toChinese(invoice.getStatus()));
//                                    data.put("resourceId", invoice.getPicPath());
//                                    break;
//                                }
//                            } 
//                        }
//                        result.add(data);
//                    }
//                }
//            }
//        }
        
        return result;
    }
}
