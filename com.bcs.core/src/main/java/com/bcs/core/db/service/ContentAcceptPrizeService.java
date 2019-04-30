package com.bcs.core.db.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentAcceptedPrize;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.PrizeList;
import com.bcs.core.db.entity.UserFieldSet;
import com.bcs.core.db.repository.ContentAcceptedPrizeRepository;
import com.bcs.core.db.repository.PrizeListRepository;

@Service
public class ContentAcceptPrizeService {
	@Autowired
	private ContentAcceptedPrizeRepository contenAcceptPrizeRepository;

	@Autowired
	private PrizeListRepository prizeListRepository;
	
    @Autowired
    private LineUserService lineUserService;
    
    @Autowired
    UserFieldSetService userFieldSetService;
	
	/**
	 * 新增樣板訊息
     */
    @Transactional(rollbackFor=Exception.class)
	public void saveAcceptPrize(ContentAcceptedPrize contentAcceptPrize){    	
    	contenAcceptPrizeRepository.save(contentAcceptPrize);
    	
    	PrizeList prize = prizeListRepository.findOne(contentAcceptPrize.getPrizeListId());
		prize.setStatus(PrizeList.PRIZE_STATUS_WINNED);
		prizeListRepository.save(prize);
	}
    
    @Transactional(rollbackFor=Exception.class)
    public void acceptPrize(String gameId, Integer prizeListId, String MID, String userName, String address, Integer numOfChildren, List<String> preferredProducts){       
        
        ContentAcceptedPrize contentAcceptPrize = new ContentAcceptedPrize();
        String acceptPrizeId = "";
        
        acceptPrizeId = checkDuplicateUUID("1");
        
        contentAcceptPrize.setAcceptedPrizeId(acceptPrizeId);
        contentAcceptPrize.setGameId(gameId);
        contentAcceptPrize.setPrizeListId(prizeListId);
        contentAcceptPrize.setUserName(userName);
        contentAcceptPrize.setModifyTime(new Date());
        contentAcceptPrize.setMid(MID);

        LineUser user = lineUserService.findByMid(MID);
        if (user != null) {
            contentAcceptPrize.setUserPhoneNumber(user.getMobile());
            contentAcceptPrize.setUserEMail(user.getEmail());
        }
        
        saveAcceptPrize(contentAcceptPrize);
        
        List<UserFieldSet> userFieldSetList = new ArrayList<UserFieldSet>();
        if (CollectionUtils.isNotEmpty(preferredProducts)) {
            for (String preferredProduct : preferredProducts) {
                UserFieldSet userFieldSet = new UserFieldSet();
                userFieldSet.setKeyData("PreferredProduct");
                userFieldSet.setName("關注產品");
                userFieldSet.setType("String");
                userFieldSet.setSetTime(new Date());
                userFieldSet.setValue(preferredProduct);
                userFieldSet.setMid(MID);
                userFieldSetList.add(userFieldSet);
            }
        }
        
        UserFieldSet nameFieldSet = new UserFieldSet();
        nameFieldSet.setKeyData("Name");
        nameFieldSet.setName("姓名");
        nameFieldSet.setType("String");
        nameFieldSet.setSetTime(new Date());
        nameFieldSet.setValue(userName);
        nameFieldSet.setMid(MID);
        userFieldSetList.add(nameFieldSet);
        
        UserFieldSet addressFieldSet = new UserFieldSet();
        addressFieldSet.setKeyData("Address");
        addressFieldSet.setName("地址");
        addressFieldSet.setType("String");
        addressFieldSet.setSetTime(new Date());
        addressFieldSet.setValue(address);
        addressFieldSet.setMid(MID);
        userFieldSetList.add(addressFieldSet);
        
        if (numOfChildren != null) {
            UserFieldSet numOfChildrenFieldSet = new UserFieldSet();
            numOfChildrenFieldSet.setKeyData("NumOfChildren");
            numOfChildrenFieldSet.setName("小孩數量");
            numOfChildrenFieldSet.setType("Integer");
            numOfChildrenFieldSet.setSetTime(new Date());
            numOfChildrenFieldSet.setValue(String.valueOf(numOfChildren));
            numOfChildrenFieldSet.setMid(MID);
            userFieldSetList.add(numOfChildrenFieldSet);
        }
        
        userFieldSetService.save(userFieldSetList);
    }
    
    public List<ContentAcceptedPrize> findByGameIdAndMid(String gameId, String mid){
        return contenAcceptPrizeRepository.findByGameIdAndMid(gameId, mid);
    }
    
    public List<ContentAcceptedPrize> findByGameId(String gameId){
        return contenAcceptPrizeRepository.findByGameId(gameId);
    }
    
    /**
	 *  檢查有無重覆使用到UUID
     */
    public Boolean checkDuplicateUUID(String queryType, String uuid) {
    	if(queryType == "1"){
    		ContentAcceptedPrize contentAcceptPrize = contenAcceptPrizeRepository.findOne(uuid);
    		if (contentAcceptPrize == null) return false;
    	}
    	
		return true;
    }
    
    /** 
     * 回傳一個沒有重覆的uuid
     */
    public String checkDuplicateUUID(String queryType) {
        String uuid = UUID.randomUUID().toString().toLowerCase();
        Boolean duplicateUUID = checkDuplicateUUID(queryType, uuid);
        while (duplicateUUID) {
            uuid = UUID.randomUUID().toString().toLowerCase();
            duplicateUUID = checkDuplicateUUID(queryType, uuid);
        }
        
        return uuid;
    }
}
