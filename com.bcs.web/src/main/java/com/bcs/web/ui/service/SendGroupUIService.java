package com.bcs.web.ui.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.SendGroup;
import com.bcs.core.db.entity.SendGroupDetail;
import com.bcs.core.db.entity.UserEventSet;
import com.bcs.core.db.repository.SendGroupDetailRepository;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.db.service.UserEventSetService;
import com.bcs.core.enums.EVENT_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.upload.ImportDataFromExcel;
import com.bcs.core.upload.ImportDataFromText;

@Service
public class SendGroupUIService {
	/** Logger */
	private static Logger logger = Logger.getLogger(SendGroupUIService.class);
	
	@Autowired
	private SendGroupService sendGroupService;
	@Autowired
	private SendGroupDetailRepository sendGroupDetailRepository;
	@Autowired
	private ImportDataFromExcel importMidFromExcel;
	@Autowired
	private ImportDataFromText importMidFromText;
	@Autowired
	private LineUserService lineUserService;
	@Autowired
	private UserEventSetService userEventSetService; 
	
	/**
	 * 新增或修改發送群組
	 * 
	 * @param sendGroup
	 * @param adminUserAccount
	 * @return
	 * @throws BcsNoticeException 
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public SendGroup saveFromUI(SendGroup sendGroup, String adminUserAccount) throws BcsNoticeException{
		logger.info("saveFromUI:" + sendGroup);

		Long groupId = sendGroup.getGroupId();
		if(groupId != null && groupId < 0){
			throw new BcsNoticeException("預設群組無法修改");
		}
		
		String action = (groupId == null ? "Create" : "Edit");
		
		// Set Modify Admin User
		sendGroup.setModifyUser(adminUserAccount);
		sendGroup.setModifyTime(new Date());
		
		List<SendGroupDetail> list = sendGroup.getSendGroupDetail();
		sendGroup.setSendGroupDetail(new ArrayList<SendGroupDetail>());
		
		// Save Send Group
		sendGroupService.save(sendGroup);
		
		if(list != null){
			for(SendGroupDetail detail : list){
				detail.setSendGroup(sendGroup);
				sendGroup.getSendGroupDetail().add(detail);
			}
		}
		sendGroupService.save(sendGroup);
		sendGroup = sendGroupService.findOne(sendGroup.getGroupId());
		createSystemLog(action, sendGroup, sendGroup.getModifyUser(), sendGroup.getModifyTime(), sendGroup.getGroupId().toString());
		return sendGroup;
	}
	
	/**
	 * 刪除發送群組
	 * 
	 * @param groupId
	 * @param adminUserAccount
	 * @throws BcsNoticeException 
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void deleteFromUI(Long groupId, String adminUserAccount) throws BcsNoticeException {
		logger.info("deleteFromUI:" + groupId);
		if(groupId < 0){
			throw new BcsNoticeException("預設群組無法刪除");
		}
		String groupTitle = sendGroupService.findGroupTitleByGroupId(groupId);
		sendGroupService.delete(groupId);
		createSystemLog("Delete", groupTitle, adminUserAccount, new Date(), groupId.toString());
	}
	
	/**
	 * 新增系統日誌
	 * 
	 * @param action
	 * @param content
	 * @param modifyUser
	 * @param modifyTime
	 */
	private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime, String referenceId) {
		SystemLogUtil.saveLogDebug("SendGroup", action, modifyUser, content, referenceId);
	}

	@Transactional(rollbackFor=Exception.class)
	public Map<String, Object> uploadMidSendGroup(MultipartFile filePart, String modifyUser, Date modifyTime) throws Exception{

		String fileName = filePart.getOriginalFilename();
		logger.info("getOriginalFilename:" + fileName);
		String contentType = filePart.getContentType();
		logger.info("getContentType:" + contentType);
		logger.info("getSize:" + filePart.getSize());

		Set<String> mids = null;
		if("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) || "application/vnd.ms-excel".equals(contentType)){
			mids = importMidFromExcel.importData(filePart.getInputStream());	
		}
		else if("text/plain".equals(contentType)){
			mids = importMidFromText.importData(filePart.getInputStream());	
		}
		
		if(mids != null && mids.size() > 0){
			List<String> list = new ArrayList<String>(mids);

			List<String> existMids = new ArrayList<String>();
			
			// Check MID Exist by Part
			List<String> check = new ArrayList<String>();
			for(int i = 1; i <= list.size(); i++){
				
				check.add(list.get(i-1));
				
				if(i % 1000 == 0){
//					logger.info("check.size():" + check.size());
					List<String> midResult = lineUserService.findMidByMidInAndActive(check);
					if(midResult != null && midResult.size() > 0){
						existMids.addAll(midResult);
					}
					check.clear();
				}
			}
//			logger.info("check.size():" + check.size());
			if(check.size() > 0){
				List<String> midResult = lineUserService.findMidByMidInAndActive(check);
				if(midResult != null && midResult.size() > 0){
					existMids.addAll(midResult);
				}
			}

			if(existMids != null && existMids.size() > 0){
				logger.debug("existMids:" + existMids);
				
				String referenceId = UUID.randomUUID().toString().toLowerCase();
				
				for(String mid : existMids){
					UserEventSet userEventSet = new UserEventSet();

					userEventSet.setTarget(EVENT_TARGET_ACTION_TYPE.EVENT_SEND_GROUP.toString());
					userEventSet.setAction(EVENT_TARGET_ACTION_TYPE.ACTION_UPLOAD_MID.toString());

					userEventSet.setReferenceId(referenceId);
					
					userEventSet.setMid(mid);
					userEventSet.setContent(fileName);
					
					userEventSet.setSetTime(modifyTime);
					userEventSet.setModifyUser(modifyUser);
					
					userEventSetService.save(userEventSet);
				}

				Map<String, Object> result = new HashMap<String, Object>();
		
				result.put("referenceId", referenceId);
				result.put("count", existMids.size());
				
				return result;
			}
			else{
				throw new BcsNoticeException("上傳沒有UID");
			}
		}
		else if(mids == null){
			throw new BcsNoticeException("上傳格式錯誤");
		}
		else{
			throw new BcsNoticeException("上傳沒有UID");
		}
	}
}
