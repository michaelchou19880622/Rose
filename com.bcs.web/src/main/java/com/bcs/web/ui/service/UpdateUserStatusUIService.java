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

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.UserEventSetService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.upload.ImportDataFromExcel;
import com.bcs.core.upload.ImportDataFromText;
import com.bcs.core.utils.LineIdUtil;

@Service
public class UpdateUserStatusUIService {
	/** Logger */
	private static Logger logger = Logger.getLogger(UpdateUserStatusUIService.class);

	@Autowired
	private ImportDataFromExcel importMidFromExcel;
	@Autowired
	private ImportDataFromText importMidFromText;
	@Autowired
	private LineUserService lineUserService;
	@Autowired
	private UserEventSetService userEventSetService;

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public Map<String, Object> uploadUserStatus(MultipartFile filePart, String modifyUser, Date modifyTime) throws Exception{

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
			for(int i = 1; i <= list.size(); i++){

				String mid = list.get(i-1);
				if(LineIdUtil.isLineUID(mid)){
					existMids.add(mid);
				}
			}
			logger.info("existMids.size():" + existMids.size());

			if(existMids != null && existMids.size() > 0){
				logger.debug("existMids:" + existMids);
				
				Integer count = 0;
				
				for(String mid : existMids){
					
					String status = midsMap.get(mid);
					
					if(StringUtils.isNotBlank(status) && 
						(
							LineUser.STATUS_BINDED.equals(status) || 
							LineUser.STATUS_BLOCK.equals(status) || 
							LineUser.STATUS_UNBIND.equals(status) || 
							LineUser.STATUS_UNFRIEND.equals(status)
						)){
						
						LineUser lineUser = lineUserService.findByMidAndCreateUnbind(mid);
						
						if(lineUser != null){
							lineUser.setStatus(status);
							
							lineUserService.save(lineUser);
							
							count++;
						}
					}
				}

				Map<String, Object> result = new HashMap<String, Object>();
		
				result.put("count", count);
				
				SystemLogUtil.saveLogDebug("UserStatus", "Upload", modifyUser, result, "Upload");
				
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
}
