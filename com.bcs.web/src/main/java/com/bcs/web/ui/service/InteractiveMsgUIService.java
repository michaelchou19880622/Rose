package com.bcs.web.ui.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgInteractiveCampaign;
import com.bcs.core.db.entity.MsgInteractiveDetail;
import com.bcs.core.db.entity.MsgInteractiveMain;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.db.service.ContentStickerService;
import com.bcs.core.db.service.MsgDetailService;
import com.bcs.core.db.service.MsgInteractiveCampaignService;
import com.bcs.core.db.service.MsgInteractiveDetailService;
import com.bcs.core.db.service.MsgInteractiveMainService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.interactive.service.InteractiveService;
import com.bcs.core.upload.ImportDataFromExcel;
import com.bcs.web.ui.model.InteractiveMsgModel;
import com.bcs.web.ui.model.SendMsgDetailModel;

@Service
public class InteractiveMsgUIService {
	@Autowired
	private MsgInteractiveMainService msgInteractiveMainService;
    @Autowired
    private MsgDetailService msgDetailService;
	@Autowired
	private SendMsgUIService sendMsgUIService;
	@Autowired
	private ContentStickerService contentStickerService;
	@Autowired
	private ContentLinkService contentLinkService;
	@Autowired
	private InteractiveService interactiveService;
    @Autowired
    private MsgInteractiveDetailService msgInteractiveDetailService;
    @Autowired
    private MsgInteractiveCampaignService msgInteractiveCampaignService;
    @Autowired
    private ImportDataFromExcel importDataFromExcel;
    
    
	/**
	 * Delete InteractiveMsg
	 * @param iMsgId
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void deleteMessageMain(Long iMsgId, String adminUserAccount){
		// 不刪除, 只改狀態
		MsgInteractiveMain main = msgInteractiveMainService.findOne(iMsgId);
		main.setInteractiveStatus(MsgInteractiveMain.INTERACTIVE_STATUS_DELETE);
		
		main.setModifyTime(new Date());
		main.setModifyUser(adminUserAccount);
		
		msgInteractiveMainService.save(main);
		
		interactiveService.loadInteractiveMap();
	}

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void switchMessageMainStatus(Long iMsgId, String adminUserAccount) throws BcsNoticeException{
		// 切換狀態
		MsgInteractiveMain main = msgInteractiveMainService.findOne(iMsgId);
		
		String status = main.getInteractiveStatus();
		if(MsgInteractiveMain.INTERACTIVE_STATUS_ACTIVE.equals(status)){
			status = MsgInteractiveMain.INTERACTIVE_STATUS_DISABLE;
		}
		else if(MsgInteractiveMain.INTERACTIVE_STATUS_DISABLE.equals(status)){
			status = MsgInteractiveMain.INTERACTIVE_STATUS_ACTIVE;
		}
		else{
			throw new BcsNoticeException("請選擇正確的訊息");
		}
		
		main.setInteractiveStatus(status);
		
		main.setModifyTime(new Date());
		main.setModifyUser(adminUserAccount);
		
		msgInteractiveMainService.save(main);
		
		interactiveService.loadInteractiveMap();
	}
	
	/**
	 * Validate Interactive Msg Model
	 * 
	 * @param interactiveMsgModel
	 * @throws BcsNoticeException
	 */
	private void validateInteractiveMsgModel(InteractiveMsgModel interactiveMsgModel) throws BcsNoticeException{

		// Validate Input Keyword
		String keywordInput = interactiveMsgModel.getKeywordInput();
		if(StringUtils.isBlank(keywordInput)){
			throw new BcsNoticeException("代表關鍵字不能是空白");
		}
		else{
			keywordInput = StringUtils.trim(keywordInput);
			interactiveMsgModel.setKeywordInput(keywordInput);
		}
		
		// Validate otherKeywords
		List<String> otherKeywords = interactiveMsgModel.getOtherKeywords();
		if(otherKeywords != null && otherKeywords.size() > 0){
			for(String keyword : otherKeywords){
				if(StringUtils.isBlank(keyword)){
					throw new BcsNoticeException("追加關鍵字不能是空白");
				}
			}
		}

		// Validate User Status
		String userStatus = interactiveMsgModel.getUserStatus();
		if(LineUser.STATUS_BINDED.equals(userStatus)){
			// Pass
		}
		else if(LineUser.STATUS_UNBIND.equals(userStatus)){
			// Pass
		}
		else if(MsgInteractiveMain.USER_STATUS_ALL.equals(userStatus)){
			// Pass
		}
		else{
			throw new BcsNoticeException("使用者狀態錯誤");
		}

		// Validate SendMsgDetailModel
		List<SendMsgDetailModel> list = interactiveMsgModel.getSendMsgDetails();
		if(list != null && list.size() > 0){
			// Pass
		}
		else if(MsgInteractiveMain.INTERACTIVE_TYPE_BLACK_KEYWORD.equals(interactiveMsgModel.getInteractiveType())){
			// Pass
		}
		else{
			throw new BcsNoticeException("請設定發送內容");
		}

		// Validate iMsgId
		Long iMsgId = interactiveMsgModel.getiMsgId();
		if(iMsgId != null && iMsgId > 0){

			MsgInteractiveMain msgMain = msgInteractiveMainService.findOne(iMsgId);
			if(msgMain != null){
				// Pass
			}
			else{
				throw new BcsNoticeException("請確定設定內容");
			}
		}
		
		// Validate interactiveStatus
		String interactiveStatus = interactiveMsgModel.getInteractiveStatus();
		if(MsgInteractiveMain.INTERACTIVE_STATUS_ACTIVE.equals(interactiveStatus)){
			// Pass
		}
		else if(MsgInteractiveMain.INTERACTIVE_STATUS_DISABLE.equals(interactiveStatus)){
			// Pass
		}
		else{
			throw new BcsNoticeException("設定狀態錯誤");
		}
		
		String interactiveTimeType = interactiveMsgModel.getInteractiveTimeType();
		if(MsgInteractiveMain.INTERACTIVE_TIME_TYPE_DAY.equals(interactiveTimeType)){
			try{
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				String interactiveStartTime = interactiveMsgModel.getInteractiveStartTime();
				Date start = sdf.parse(interactiveStartTime);
				
				String interactiveEndTime = interactiveMsgModel.getInteractiveEndTime();
				Date end = sdf.parse(interactiveEndTime);
				
				if(start.compareTo(end) >= 0){
					throw new BcsNoticeException("設定生效區間錯誤");
				}
			}
			catch(Exception e){
				throw new BcsNoticeException("設定生效區間錯誤");
			}
		}
		else if(MsgInteractiveMain.INTERACTIVE_TIME_TYPE_RANGE.equals(interactiveTimeType)){
			try{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String interactiveStartTime = interactiveMsgModel.getInteractiveStartTime();
				Date start = sdf.parse(interactiveStartTime);
				
				String interactiveEndTime = interactiveMsgModel.getInteractiveEndTime();
				Date end = sdf.parse(interactiveEndTime);
				
				if(start.compareTo(end) >= 0){
					throw new BcsNoticeException("設定生效區間錯誤");
				}
			}
			catch(Exception e){
				throw new BcsNoticeException("設定生效區間錯誤");
			}
		}
	}
	
	/**
	 * @param interactiveMsgModel
	 * @param adminUserAccount
	 * @param interactiveType
	 * @return iMsgId
	 * @throws Exception
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public Long saveSettingMessage(InteractiveMsgModel interactiveMsgModel, String adminUserAccount) throws Exception{

		validateInteractiveMsgModel(interactiveMsgModel);
		
		// Save Interative Message
		MsgInteractiveMain msgMain = new MsgInteractiveMain();
		
		Long sendCount = 0L;
		
		if(interactiveMsgModel.getiMsgId() != null && interactiveMsgModel.getiMsgId() > 0){
			Long iMsgId = interactiveMsgModel.getiMsgId();
			MsgInteractiveMain dbData = msgInteractiveMainService.findOne(iMsgId);
			if(dbData != null){
				msgMain.setiMsgId(iMsgId);
				
				sendCount = dbData.getSendCount();
				
				// Remove Old Setting Detail
				msgDetailService.deleteByMsgIdAndMsgParentType(iMsgId, MsgInteractiveMain.THIS_PARENT_TYPE);

                // Remove Old Setting Detail
                msgInteractiveDetailService.deleteByiMsgId(iMsgId);

                // Remove Old Setting Detail
                msgInteractiveCampaignService.deleteByiMsgId(iMsgId);
			}
		}
		
		// Setting Data
		msgMain.setInteractiveType(interactiveMsgModel.getInteractiveType());
		msgMain.setMainKeyword(interactiveMsgModel.getKeywordInput());
		msgMain.setUserStatus(interactiveMsgModel.getUserStatus());
		msgMain.setInteractiveStatus(interactiveMsgModel.getInteractiveStatus());

		msgMain.setOtherRole(interactiveMsgModel.getOtherRole());
		msgMain.setSerialId(interactiveMsgModel.getSerialId());
		msgMain.setInteractiveIndex(interactiveMsgModel.getInteractiveIndex());

		// 設定生效區間
		String interactiveTimeType = interactiveMsgModel.getInteractiveTimeType();
		if(MsgInteractiveMain.INTERACTIVE_TIME_TYPE_DAY.equals(interactiveTimeType)){
			try{
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				String interactiveStartTime = interactiveMsgModel.getInteractiveStartTime();
				Date start = sdf.parse(interactiveStartTime);
				msgMain.setInteractiveStartTime(start);
				
				String interactiveEndTime = interactiveMsgModel.getInteractiveEndTime();
				Date end = sdf.parse(interactiveEndTime);
				msgMain.setInteractiveEndTime(end);
				
				msgMain.setInteractiveTimeType(interactiveTimeType);
			}
			catch(Exception e){
				throw new BcsNoticeException("設定生效區間錯誤");
			}
		}
		else if(MsgInteractiveMain.INTERACTIVE_TIME_TYPE_RANGE.equals(interactiveTimeType)){
			try{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String interactiveStartTime = interactiveMsgModel.getInteractiveStartTime();
				Date start = sdf.parse(interactiveStartTime);
				msgMain.setInteractiveStartTime(start);
				
				String interactiveEndTime = interactiveMsgModel.getInteractiveEndTime();
				Date end = sdf.parse(interactiveEndTime);
				msgMain.setInteractiveEndTime(end);
				
				msgMain.setInteractiveTimeType(interactiveTimeType);
			}
			catch(Exception e){
				throw new BcsNoticeException("設定生效區間錯誤");
			}
		}

		// Send Count 不歸零
		msgMain.setSendCount(sendCount);
		
		msgMain.setModifyTime(new Date());
		msgMain.setModifyUser(adminUserAccount);

		msgInteractiveMainService.save(msgMain);
		
		Long iMsgId = msgMain.getiMsgId();

		// Create Detail
		List<SendMsgDetailModel> list = interactiveMsgModel.getSendMsgDetails();
		sendMsgUIService.createMsgDetail(msgMain.getiMsgId(), list, adminUserAccount, MsgInteractiveMain.THIS_PARENT_TYPE);

		List<String> otherKeywords = interactiveMsgModel.getOtherKeywords();
		if(otherKeywords != null && otherKeywords.size() > 0){
			for(String keyword : otherKeywords){
				MsgInteractiveDetail detail = new MsgInteractiveDetail();
				detail.setiMsgId(iMsgId);
				detail.setOtherKeyword(keyword);
				
				msgInteractiveDetailService.save(detail);
			}
		}
		
		if (StringUtils.isNoneBlank(interactiveMsgModel.getCampaignId())) {
		    MsgInteractiveCampaign msgCampaign = new MsgInteractiveCampaign();
		    msgCampaign.setCampaignId(interactiveMsgModel.getCampaignId());
		    msgCampaign.setiMsgId(iMsgId);
		    msgCampaign.setErrorLimit(interactiveMsgModel.getErrorLimit());
		    msgCampaign.setTimeout(interactiveMsgModel.getTimeout());
		    
		    msgInteractiveCampaignService.save(msgCampaign);
		}
		
		
		interactiveService.loadInteractiveMap();
		
		return iMsgId;
	}
    
    public void setResourceMap(Map<String, Object> result, String referenceName, String referenceId, Object obj){

        @SuppressWarnings("unchecked")
        Map<String, Object> resourceMap = (Map<String, Object>) result.get(referenceName);
        if(resourceMap == null){
            resourceMap = new HashMap<String, Object>();
            result.put(referenceName, resourceMap);
        }

        if(resourceMap.get(referenceId) == null){
            resourceMap.put(referenceId, obj);
        }
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 300)
    public Map<String, List<String>> uploadMainKeywordList(MultipartFile filePart, String modifyUser, Date modifyTime) throws Exception {
        
        String contentType = filePart.getContentType();
        Map<String, List<String>> keywordsMap = null;
        
        if("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) || "application/vnd.ms-excel".equals(contentType)){
            keywordsMap = importDataFromExcel.importDataKeyValueList(filePart.getInputStream());    
        }
        
        if(keywordsMap != null && keywordsMap.size() > 0) {
            Set<String> mainKeywords = keywordsMap.keySet();
            
            for(String mainKeyword : mainKeywords) {
                
                //save msgInteractiveMain
                MsgInteractiveMain msgInteractiveMain = new MsgInteractiveMain();
                msgInteractiveMain.setInteractiveStatus(MsgInteractiveMain.INTERACTIVE_STATUS_DISABLE);
                msgInteractiveMain.setInteractiveType(MsgInteractiveMain.INTERACTIVE_TYPE_KEYWORD);
                msgInteractiveMain.setMainKeyword(mainKeyword);
                msgInteractiveMain.setModifyTime(modifyTime);
                msgInteractiveMain.setModifyUser(modifyUser);
                msgInteractiveMain.setSendCount(0L);
                msgInteractiveMain.setUserStatus(MsgInteractiveMain.USER_STATUS_ALL);
                msgInteractiveMainService.save(msgInteractiveMain);
                
                Long iMsgId = msgInteractiveMain.getiMsgId();
                
                //save msgDetail
                MsgDetail detail = new MsgDetail();
                detail.setMsgId(iMsgId);
                detail.setMsgType(MsgGenerator.MSG_TYPE_TEXT);
                detail.setText("預設");
                detail.setMsgParentType(MsgInteractiveMain.THIS_PARENT_TYPE);
                
                msgDetailService.save(detail);
                
                //save msgInteractiveDetail
                List<String> otherKeywords = keywordsMap.get(mainKeyword);
                for(int i = 0; i < otherKeywords.size(); i++) {
                    String otherKeyword = otherKeywords.get(i);
                    if(otherKeyword != null) {
                        MsgInteractiveDetail interactiveDetail = new MsgInteractiveDetail();
                        interactiveDetail.setiMsgId(iMsgId);
                        interactiveDetail.setOtherKeyword(otherKeyword);
                        
                        msgInteractiveDetailService.save(interactiveDetail); 
                    }else {
                        otherKeywords.remove(i);
                        i--;
                    }
                } 
            }
            return keywordsMap;
            
        }else if(keywordsMap == null){
            throw new BcsNoticeException("上傳格式錯誤");
        }
        else{
            throw new BcsNoticeException("沒有上傳檔案");
        }
    }
}
