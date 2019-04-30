package com.bcs.web.ui.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.api.service.LineConvertingMidToUidService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.report.export.ExportToTextFromList;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.upload.ImportDataFromExcel;
import com.bcs.core.upload.ImportDataFromText;
import com.bcs.core.utils.LineIdUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class MidToUidUIService {
	/** Logger */
	private static Logger logger = Logger.getLogger(MidToUidUIService.class);
	
	@Autowired
	private ImportDataFromExcel importMidFromExcel;
	@Autowired
	private ImportDataFromText importMidFromText;
	@Autowired
	private LineConvertingMidToUidService lineConvertingMidToUidService;

	protected LoadingCache<String, List<String>> dataCache;

	public MidToUidUIService(){

		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(10, TimeUnit.MINUTES)
				.build(new CacheLoader<String, List<String>>() {
					@Override
					public List<String> load(String key) throws Exception {
						return new ArrayList<String>();
					}
				});
	}

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public Map<String, Object> uploadMidToUid(MultipartFile filePart, String modifyUser, Date modifyTime) throws Exception{

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
			
			String access_token = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelToken.toString(), true);
			
			List<String> list = new ArrayList<String>(mids);

			List<String> existMids = new ArrayList<String>();
			
			// Check MID Exist by Part
			List<String> check = new ArrayList<String>();
			for(int i = 1; i <= list.size(); i++){
				
				String mid = list.get(i-1);
				if(LineIdUtil.isLineMID(mid)){
					check.add(mid);
					
					if(i % 50000 == 0){
						logger.info("check.size():" + check.size());

						ObjectNode nodeList = lineConvertingMidToUidService.callPostConvertingAPI(access_token, check);
						
						if(nodeList != null && nodeList.get("list") != null && nodeList.get("list").size() > 0){
							for(JsonNode json : nodeList.get("list")){
								existMids.add(json.asText());
							}
						}
						check.clear();
					}
				}
			}
			logger.info("check.size():" + check.size());
			if(check.size() > 0){
				
				ObjectNode nodeList = lineConvertingMidToUidService.callPostConvertingAPI(access_token, check);
				
				if(nodeList != null && nodeList.get("list") != null && nodeList.get("list").size() > 0){
					for(JsonNode json : nodeList.get("list")){
						existMids.add(json.asText());
					}
				}
				check.clear();
			}

			if(existMids != null && existMids.size() > 0){
				logger.debug("existMids:" + existMids);
				
				String referenceId = UUID.randomUUID().toString().toLowerCase();
				
				dataCache.put(referenceId, existMids);

				Map<String, Object> result = new HashMap<String, Object>();
		
				result.put("tempId", referenceId);
				result.put("count", existMids.size());
				
				SystemLogUtil.saveLogDebug("MidToUid", "Create", modifyUser, result, referenceId);
				
				return result;
			}
			else{
				throw new BcsNoticeException("上傳沒有MID");
			}
		}
		else if(mids == null){
			throw new BcsNoticeException("上傳格式錯誤");
		}
		else{
			throw new BcsNoticeException("上傳沒有MID");
		}
	}
	
	public List<String> getMidToUidList(String referenceId) throws ExecutionException{
		return dataCache.get(referenceId);
	}

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public Map<String, Object> uploadSaveMidToUid(MultipartFile filePart, String modifyUser, Date modifyTime) throws Exception{

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
			
			Date date = new Date();
			
			String filePath = ExportToTextFromList.exportToTextPath("MidToUid");
			String saveFileName = ExportToTextFromList.exportToTextName("MidToUid", date);
			
			String access_token = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelToken.toString(), true);
			
			List<String> list = new ArrayList<String>(mids);
			List<ObjectNode> results = new ArrayList<ObjectNode>();
			
			// Check MID Exist by Part
			List<String> check = new ArrayList<String>();
			for(int i = 1; i <= list.size(); i++){
				
				String mid = list.get(i-1);
				if(StringUtils.isNotBlank(mid) && 33 == mid.length()){
					check.add(mid);
					
					if(i % 50000 == 0){
						logger.info("check.size():" + check.size());
						
						ObjectNode nodeResult = lineConvertingMidToUidService.callPostConvertingAPI(access_token, check, true, filePath + System.getProperty("file.separator") + saveFileName);

						logger.info("nodeResult:" + nodeResult);
						results.add(nodeResult);
						check.clear();
					}
				}
			}
			logger.info("check.size():" + check.size());
			if(check.size() > 0){
				
				ObjectNode nodeResult = lineConvertingMidToUidService.callPostConvertingAPI(access_token, check, true, filePath + System.getProperty("file.separator") + saveFileName);

				logger.info("nodeResult:" + nodeResult);
				results.add(nodeResult);
				check.clear();
			}

			if(results != null && results.size() > 0){
				logger.debug("results:" + results);
				
				String referenceId = UUID.randomUUID().toString().toLowerCase();

				List<String> path = new ArrayList<String>();
				path.add(filePath);
				path.add(saveFileName);
				dataCache.put(referenceId, path);

				Map<String, Object> result = new HashMap<String, Object>();
		
				result.put("tempId", referenceId);
				result.put("count", list.size());
				
				SystemLogUtil.saveLogDebug("MidToUid", "Create", modifyUser, result, referenceId);
				
				return result;
			}
			else{
				throw new BcsNoticeException("上傳沒有MID");
			}
		}
		else if(mids == null){
			throw new BcsNoticeException("上傳格式錯誤");
		}
		else{
			throw new BcsNoticeException("上傳沒有MID");
		}
	}
}
