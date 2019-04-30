package com.bcs.web.ui.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.SerialSetting;
import com.bcs.core.db.entity.UserEventSet;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.SerialSettingService;
import com.bcs.core.db.service.UserEventSetService;
import com.bcs.core.enums.EVENT_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.upload.ImportDataFromExcel;
import com.bcs.core.upload.ImportDataFromText;
import com.bcs.core.utils.LineIdUtil;

@Service
public class SerialSettingUIService {
	/** Logger */
	private static Logger logger = Logger.getLogger(SerialSettingUIService.class);

	@Autowired
	private ImportDataFromExcel importMidFromExcel;
	@Autowired
	private ImportDataFromText importMidFromText;
	@Autowired
	private LineUserService lineUserService;
	@Autowired
	private UserEventSetService userEventSetService;
	@Autowired
	private SerialSettingService serialSettingService;

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public Map<String, Object> uploadSerialSetting(MultipartFile filePart, String modifyUser, Date modifyTime, String SerialId) throws Exception{

		if(StringUtils.isBlank(SerialId)){
			throw new BcsNoticeException("上傳錯誤");
		}
		logger.info("SerialId:" + SerialId);
		
		String fileName = filePart.getOriginalFilename();
		logger.info("getOriginalFilename:" + fileName);
		String contentType = filePart.getContentType();
		logger.info("getContentType:" + contentType);
		logger.info("getSize:" + filePart.getSize());

		Map<String, String> midsMap = null;
		if("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) || "application/vnd.ms-excel".equals(contentType)){
			midsMap = importMidFromExcel.importDataKeyValue(filePart.getInputStream());	
		}
		else if("text/plain".equals(contentType)){
			midsMap = importMidFromText.importDataKeyValue(filePart.getInputStream());	
		}
		
		if(midsMap != null && midsMap.size() > 0){
			List<String> list = new ArrayList<String>(midsMap.keySet());

			List<String> existMids = new ArrayList<String>();
			
			// Check MID Exist by Part
			List<String> check = new ArrayList<String>();
			for(int i = 1; i <= list.size(); i++){

				String mid = list.get(i-1);
				if(LineIdUtil.isLineUID(mid)){
					check.add(mid);
					
					if(i % 1000 == 0){
						logger.info("check.size():" + check.size());
						List<String> midResult = lineUserService.findMidByMidInAndActive(check);
						if(midResult != null && midResult.size() > 0){
							existMids.addAll(midResult);
						}
						check.clear();
					}
				}
			}
			logger.info("check.size():" + check.size());
			if(check.size() > 0){
				List<String> midResult = lineUserService.findMidByMidInAndActive(check);
				if(midResult != null && midResult.size() > 0){
					existMids.addAll(midResult);
				}
			}

			if(existMids != null && existMids.size() > 0){
				logger.debug("existMids:" + existMids);

				String target = EVENT_TARGET_ACTION_TYPE.EVENT_SERIAL_SETTING.toString();
				String action = EVENT_TARGET_ACTION_TYPE.ACTION_UPLOAD_MID_SERIAL.toString();

				Long deleteCount = userEventSetService.deleteByTargetAndActionAndReferenceId(target, action, SerialId);
				
				for(String mid : existMids){
					UserEventSet userEventSet = new UserEventSet();

					userEventSet.setTarget(target);
					userEventSet.setAction(action);

					userEventSet.setReferenceId(SerialId);
					
					userEventSet.setMid(mid);
					userEventSet.setContent(midsMap.get(mid));
					
					userEventSet.setSetTime(modifyTime);
					userEventSet.setModifyUser(modifyUser);
					
					userEventSetService.save(userEventSet);
				}
				
				SerialSetting serialSetting = serialSettingService.findOne(SerialId);
				if(serialSetting == null){
					serialSetting = new SerialSetting();
					serialSetting.setSerialId(SerialId);
				}
				serialSetting.setSerialCount(existMids.size());
				serialSetting.setSerialTitle(fileName);
				serialSetting.setSerialLevel(SerialSetting.SERIAL_LEVEL_MAIN);
				serialSetting.setSerialTarget("ReplaceParam");
				
				serialSetting.setModifyTime(modifyTime);
				serialSetting.setModifyUser(modifyUser);
				
				serialSetting.setStatus(SerialSetting.SERIAL_STATUS_ACTIVE);
				
				serialSettingService.save(serialSetting);

				Map<String, Object> result = new HashMap<String, Object>();
		
				result.put("target", target);
				result.put("action", action);
				result.put("SerialId", SerialId);
				result.put("count", existMids.size());
				result.put("deleteCount", deleteCount);
				
				SystemLogUtil.saveLogDebug("SerialSetting", "Create", modifyUser, result, target + "-" + action + "-" + SerialId);
				
				return result;
			}
			else{
				throw new BcsNoticeException("上傳沒有UID");
			}
		}
		else if(midsMap == null){
			throw new BcsNoticeException("上傳格式錯誤");
		}
		else{
			throw new BcsNoticeException("上傳沒有UID");
		}
	}

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public Map<String, Object> deleteSerialSetting(String serialId, String modifyUser) throws Exception{
		
		if(StringUtils.isNotBlank(serialId)){
			SerialSetting serialSetting = serialSettingService.findOne(serialId);
			
			if(serialSetting != null){
				String title = serialSetting.getSerialTitle();
				
				serialSettingService.delete(serialId);
				
				SystemLogUtil.saveLogDebug("SerialSetting", "Delete", modifyUser, title, serialId);
				
				String target = EVENT_TARGET_ACTION_TYPE.EVENT_SERIAL_SETTING.toString();
				String action = EVENT_TARGET_ACTION_TYPE.ACTION_UPLOAD_MID_SERIAL.toString();
				
				Long deleteCount = userEventSetService.deleteByTargetAndActionAndReferenceId(target, action, serialId);
	
				Map<String, Object> result = new HashMap<String, Object>();			
				result.put("deleteCount", deleteCount);
				
				SystemLogUtil.saveLogDebug("SerialSetting", "Delete", modifyUser, "deleteCount:" + deleteCount, target + "-" + action + "-" + serialId);
	
				return result;
			}
			else{
				throw new BcsNoticeException("刪除錯誤");
			}
		}
		else{
			throw new BcsNoticeException("刪除錯誤");
		}
	}
}
