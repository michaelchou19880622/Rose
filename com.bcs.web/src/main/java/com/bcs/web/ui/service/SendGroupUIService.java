package com.bcs.web.ui.service;

import java.io.InputStream;
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
	
	private static int TRANSACTION_TIMEOUT_RETRY_MAX_TIMES = 3;
	
	private List<String> existMids = new ArrayList<String>();
	private String referenceId;
	private String fileName;
	private Date modifyTime;
	private String modifyUser;
	private int curSaveIndex = 0;
	private int TransactionTimeoutRetry = 0;
	private List<String> list;
	
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
 
	@Transactional(rollbackFor=Exception.class, timeout = 300000)
	public Map<String, Object> uploadMidSendGroup(MultipartFile filePart, String modifyUser, Date modifyTime) throws Exception{
		long startTime = System.currentTimeMillis();
		long endTime = 0;
		
		logger.info("filePart.getSize():" + filePart.getSize());
		
		fileName = filePart.getOriginalFilename();
		logger.info("getOriginalFilename:" + fileName);
		
		String contentType = filePart.getContentType();
		logger.info("getContentType:" + contentType);
		
		this.modifyTime = modifyTime;
		this.modifyUser = modifyUser;
		

		Set<String> mids = null;
		if("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) || "application/vnd.ms-excel".equals(contentType)){
			mids = importMidFromExcel.importData(filePart.getInputStream());	
		}
		else if("text/plain".equals(contentType)){
			mids = importMidFromText.importData(filePart.getInputStream());	
		}
		
		if(mids != null && mids.size() > 0){
			list = new ArrayList<String>(mids);
			logger.info("list.size():" + list.size());
			
			try {
				
				List<String> check = new ArrayList<String>();
				for (int i = 1; i <= list.size(); i++) {

					check.add(list.get(i - 1));

					if (i % 1000 == 0) {
						List<String> midResult = lineUserService.findMidByMidInAndActive(check);
						if (midResult != null && midResult.size() > 0) {
							existMids.addAll(midResult);
						}
						check.clear();
					}
				}
				if (check.size() > 0) {
					List<String> midResult = lineUserService.findMidByMidInAndActive(check);
					if (midResult != null && midResult.size() > 0) {
						existMids.addAll(midResult);
					}
				}

				endTime = System.currentTimeMillis();

				logger.info("Fill existMids - START TIME : " + startTime);
				logger.info("Fill existMids - END TIME : " + endTime);
				logger.info("Fill existMids - ELAPSED : " + (endTime - startTime));

			} catch (Exception e) {

				endTime = System.currentTimeMillis();
				logger.info("findMidByMidInAndActive exception :" + e.getMessage());

				logger.info("Fill existMids - START TIME : " + startTime);
				logger.info("Fill existMids - END TIME : " + endTime);
				logger.info("Fill existMids - ELAPSED : " + (endTime - startTime));
			}
				
			if (existMids != null && existMids.size() > 0) {

				referenceId = UUID.randomUUID().toString().toLowerCase();
				
				/* 
				 * 增加Try-Catch，判斷Exception是否為Transaction Timeout Exception?
				 * 如是，則判斷是否已達Retry上限次數? 是的話拋出Execption{TimeOut}，否則拋Execption{RetrySaveUserEventSet}。
				*/
				try {
					curSaveIndex = 0;

					for (int i = 0; i < existMids.size(); i++) {
						String mid = existMids.get(i);

						UserEventSet userEventSet = new UserEventSet();
						userEventSet.setTarget(EVENT_TARGET_ACTION_TYPE.EVENT_SEND_GROUP.toString());
						userEventSet.setAction(EVENT_TARGET_ACTION_TYPE.ACTION_UPLOAD_MID.toString());
						userEventSet.setReferenceId(referenceId);
						userEventSet.setMid(mid);
						userEventSet.setContent(fileName);
						userEventSet.setSetTime(modifyTime);
						userEventSet.setModifyUser(modifyUser);

						logger.info("userEventSet1:" + userEventSet);

						userEventSetService.save(userEventSet);
					}

					endTime = System.currentTimeMillis();

					logger.info("Save [UserEventSet] - START TIME : " + startTime);
					logger.info("Save [UserEventSet] - END TIME : " + endTime);
					logger.info("Save [UserEventSet] - ELAPSED : " + (endTime - startTime));

				}catch(Exception e) {
					endTime = System.currentTimeMillis();

					logger.info("Save [UserEventSet] - START TIME : " + startTime);
					logger.info("Save [UserEventSet] - END TIME : " + endTime);
					logger.info("Save [UserEventSet] - ELAPSED : " + (endTime - startTime));
					
					if (e.getMessage().contains("transaction timeout expired")) {
						TransactionTimeoutRetry += 1;
						logger.info("Save [UserEventSet] retry : " + TransactionTimeoutRetry);

						if (TransactionTimeoutRetry > TRANSACTION_TIMEOUT_RETRY_MAX_TIMES) {
							throw new Exception("TimeOut");
						} else {
							throw new Exception("RetrySaveUserEventSet");
						}
					}
				}
				
				Map<String, Object> result = new HashMap<String, Object>();
		
				result.put("referenceId", referenceId);
				result.put("count", existMids.size());
				logger.info("result:"+result);
				
				existMids.clear();
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
	
	public Map<String, Object> RetrySaveUserEventSet()
	{
		try {
			return RetrySaveUserEventSet(existMids, referenceId, fileName, modifyTime, modifyUser, curSaveIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/* Retry to save UserEventSet */
	@Transactional(rollbackFor = Exception.class, timeout = -1)
	public Map<String, Object> RetrySaveUserEventSet(List<String> existMids, String referenceId, String fileName, Date modifyTime, String modifyUser, int curSaveIndex) throws Exception {
		long retryStartTime = System.currentTimeMillis();
		long retryEndTime = 0;
		
		this.existMids = existMids;
		this.referenceId = referenceId;
		this.fileName = fileName;
		this.modifyTime = modifyTime;
		this.modifyUser = modifyUser;
		this.curSaveIndex = curSaveIndex;
		

		try {
			for (int i = this.curSaveIndex; i < existMids.size(); i++) {
				String mid = existMids.get(i);

				UserEventSet userEventSet = new UserEventSet();

				userEventSet.setTarget(EVENT_TARGET_ACTION_TYPE.EVENT_SEND_GROUP.toString());
				userEventSet.setAction(EVENT_TARGET_ACTION_TYPE.ACTION_UPLOAD_MID.toString());

				userEventSet.setReferenceId(referenceId);

				userEventSet.setMid(mid);
				userEventSet.setContent(fileName);

				userEventSet.setSetTime(modifyTime);
				userEventSet.setModifyUser(modifyUser);

				logger.info("userEventSet1:" + userEventSet);

				userEventSetService.save(userEventSet);
			}

			retryEndTime = System.currentTimeMillis();

			logger.info("Save [UserEventSet] - START TIME : " + retryStartTime);
			logger.info("Save [UserEventSet] - END TIME : " + retryEndTime);
			logger.info("Save [UserEventSet] - ELAPSED : " + (retryEndTime - retryStartTime));
			
			Map<String, Object> result = new HashMap<String, Object>();
			
			result.put("referenceId", referenceId);
			result.put("count", existMids.size());
			logger.info("result:"+result);
			return result;

		} catch (Exception e) {
			retryEndTime = System.currentTimeMillis();

			logger.info("Save [UserEventSet] - START TIME : " + retryStartTime);
			logger.info("Save [UserEventSet] - END TIME : " + retryEndTime);
			logger.info("Save [UserEventSet] - ELAPSED : " + (retryEndTime - retryStartTime));
			
			// 增加retry機制，紀錄當前寫入UserEventSet table 的index， 如果 timeout，則重新從index繼續寫入UserEventSet table
			if (e.getMessage().contains("transaction timeout expired")) {
				TransactionTimeoutRetry += 1;
				logger.info("Save [UserEventSet] retry : " + TransactionTimeoutRetry);

				if (TransactionTimeoutRetry > TRANSACTION_TIMEOUT_RETRY_MAX_TIMES) {
					throw new Exception("TimeOut");
				} else {
					throw new Exception("RetrySaveUserEventSet");
				}
			}
			throw new BcsNoticeException("資料量過大導致超時，重試異常");
		}
	}
	
	@Transactional(rollbackFor=Exception.class ,timeout = 300000)
	public Map<String, Object> uploadMidSendGroup(InputStream inputStream, String modifyUser, Date modifyTime, String fileName) throws Exception{
		long startTime = System.currentTimeMillis();
		long endTime = 0;
		
		Set<String> mids = null;
		mids = importMidFromExcel.importData(inputStream);	
		
		if(mids != null && mids.size() > 0){
			List<String> list = new ArrayList<String>(mids);
			
			List<String> existMids = new ArrayList<String>();
			
			// Check MID Exist by Part
			List<String> check = new ArrayList<String>();
			for(int i = 1; i <= list.size(); i++){
				logger.info(" UID " + i + " : " +list.get(i-1));
				check.add(list.get(i-1));
				
				if(i % 1000 == 0){
					List<String> midResult = lineUserService.findMidByMidInAndActive(check);
					if(midResult != null && midResult.size() > 0){
						existMids.addAll(midResult);
					}
					check.clear();
				}
			}
			if(check.size() > 0){
				List<String> midResult = lineUserService.findMidByMidInAndActive(check);
				if(midResult != null && midResult.size() > 0){
					existMids.addAll(midResult);
				}
			}

			if(existMids != null && existMids.size() > 0){
				logger.debug("existMids:" + existMids);
				
				String referenceId = UUID.randomUUID().toString().toLowerCase();
				
				/* 
				 * 增加Try-Catch，判斷Exception是否為Transaction Timeout Exception?
				 * 如是，則判斷是否已達Retry上限次數? 是的話拋出Execption{TimeOut}，否則拋Execption{RetrySaveUserEventSet}。
				*/
				try {
					curSaveIndex = 0;

					for (int i = 0; i < existMids.size(); i++) {
						String mid = existMids.get(i);

						UserEventSet userEventSet = new UserEventSet();
						userEventSet.setTarget(EVENT_TARGET_ACTION_TYPE.EVENT_SEND_GROUP.toString());
						userEventSet.setAction(EVENT_TARGET_ACTION_TYPE.ACTION_UPLOAD_MID.toString());
						userEventSet.setReferenceId(referenceId);
						userEventSet.setMid(mid);
						userEventSet.setContent(fileName);
						userEventSet.setSetTime(modifyTime);
						userEventSet.setModifyUser(modifyUser);

						logger.info("userEventSet1:" + userEventSet);

						userEventSetService.save(userEventSet);
					}

					endTime = System.currentTimeMillis();

					logger.info("Save [UserEventSet] - START TIME : " + startTime);
					logger.info("Save [UserEventSet] - END TIME : " + endTime);
					logger.info("Save [UserEventSet] - ELAPSED : " + (endTime - startTime));

				}catch(Exception e) {
					endTime = System.currentTimeMillis();

					logger.info("Save [UserEventSet] - START TIME : " + startTime);
					logger.info("Save [UserEventSet] - END TIME : " + endTime);
					logger.info("Save [UserEventSet] - ELAPSED : " + (endTime - startTime));
					
					if (e.getMessage().contains("transaction timeout expired")) {
						TransactionTimeoutRetry += 1;
						logger.info("Save [UserEventSet] retry : " + TransactionTimeoutRetry);

						if (TransactionTimeoutRetry > TRANSACTION_TIMEOUT_RETRY_MAX_TIMES) {
							throw new Exception("TimeOut");
						} else {
							throw new Exception("RetrySaveUserEventSet");
						}
					}
				}

				Map<String, Object> result = new HashMap<String, Object>();
				logger.info("count : " + existMids.size());
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
