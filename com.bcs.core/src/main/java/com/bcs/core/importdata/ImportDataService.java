package com.bcs.core.importdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.SendGroupQuery;
import com.bcs.core.db.entity.SendGroupQueryTag;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.SendGroupQueryService;
import com.bcs.core.db.service.SendGroupQueryTagService;
import com.bcs.core.utils.ErrorRecord;

/**
 * 從檔案或其它來源匯入資料到資料庫的 Service
 * 
 * @author Kevin
 *
 */
@Service
public class ImportDataService {

	private static Logger logger = Logger.getLogger(ImportDataService.class);
	
	@Autowired
	private LineUserService lineUserService;
	
	@Autowired
	private SendGroupQueryService sendGroupQueryService;
	
	@Autowired
	private SendGroupQueryTagService sendGroupQueryTagService;
	
	/**
	 * 從檔案匯入 SendGroupQuery、SendGroupQueryTag
	 * 
	 * @param csvDirectory
	 *            放置 SendGroupQuery、SendGroupQueryTag CSV 檔的目錄
	 * @throws IOException
	 */
	public void importSendGroupQueryAndSendGroupQueryTagByFile(File csvDirectory) throws IOException {
		Validate.notNull(csvDirectory, "The csvDirectory must not be null");
		
		File sendGroupQueryCsv = new File(csvDirectory, "SEND_GROUP_QUERY.csv");
		
		if (!sendGroupQueryCsv.exists()) {
			throw new FileNotFoundException(sendGroupQueryCsv + " is not found.");
		}
		
		logger.info("======= Start importing SendGroupQuery data =======");
		
		Reader sendGroupQueryReader=new BufferedReader(new InputStreamReader(new FileInputStream(sendGroupQueryCsv), StandardCharsets.UTF_8));  
		try {
			Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().withTrim().parse(sendGroupQueryReader);
			for (CSVRecord record : records) {
			    String queryFieldId = record.get("QUERY_FIELD_ID");
			    String queryFieldName = record.get("QUERY_FIELD_NAME");
			    String queryFieldOp = record.get("QUERY_FIELD_OP");
			    String queryFieldFormat = record.get("QUERY_FIELD_FORMAT");
			    String queryFieldSet = record.get("QUERY_FIELD_SET");
			    
			    // SendGroupQuery 不存在才新增
			    if (!sendGroupQueryService.exists(queryFieldId)) {
				    SendGroupQuery sendGroupQuery = new SendGroupQuery();
				    sendGroupQuery.setQueryFieldId(queryFieldId);
				    sendGroupQuery.setQueryFieldName(queryFieldName);
				    sendGroupQuery.setQueryFieldOp(queryFieldOp);
				    sendGroupQuery.setQueryFieldFormat(queryFieldFormat);
				    sendGroupQuery.setQueryFieldSet(queryFieldSet);
				    sendGroupQueryService.save(sendGroupQuery);
			    }
			    
			    // SendGroupQueryTag
			    File sendGroupQueryTagCsv = new File(csvDirectory, queryFieldId + ".csv");
			    
				if (!sendGroupQueryTagCsv.exists()) {
			    	continue;
			    }
			    
			    importSendGroupQueryTag(queryFieldId, sendGroupQueryTagCsv);
			}
		} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
		} finally {
			IOUtils.closeQuietly(sendGroupQueryReader);
		}
		
		logger.info("======= Import SendGroupQuery data is finished =======");
	}
		
	/**
	 * 從檔案匯入 LineUser
	 * 
	 * @param file
	 * @throws FileNotFoundException 
	 */
	public void importLineUserByFileAsync(final File file) throws FileNotFoundException {
		Validate.notNull(file, "The file must not be null");
		
		// 執行前先檢查檔案是否存在
		if (!file.exists()) {
			throw new FileNotFoundException(file + " is not found.");
		}
		
		Thread thread = new Thread(new Runnable() {
			public void run() {
				importLineUserByFile(file);
			}
		});
		
		thread.start();
	}
	
	private void importLineUserByFile(File file) {		
		logger.info("======= Start importing LineUser data =======");
		
		long startTime = System.currentTimeMillis();
		long prev1000Time = startTime;
		List<String> mids = new ArrayList<String>();
		List<LineUser> lineUsers = new ArrayList<LineUser>();
		LineIterator lineIterator = null;
		int bulkPersistSize = 1000;
		int size = 0;
		
		try {
			lineIterator = FileUtils.lineIterator(file, "UTF-8");
		    while (lineIterator.hasNext()) {
		        String mid = lineIterator.nextLine();
		        mid = StringUtils.trimToEmpty(mid);
		        
		        if (StringUtils.isBlank(mid)) {
		        	continue;
		        }
		        
		        mids.add(mid);
		        
		        // 每隔一千筆新增一次，記錄時間差
		        if (mids.size() % bulkPersistSize == 0) {
		        	size = size + bulkPersist(lineUsers, mids);
		        	
		        	long current1000Time = System.currentTimeMillis();
		        	logger.info("Size : " + size + ", cost : " + (current1000Time - prev1000Time) + " ms");
		        	prev1000Time = current1000Time;
		        }
		    }
		    
		    // 新增最後不足 bulkPersistSize 的剩餘資料
		    if (!mids.isEmpty()) {
		    	size = size + bulkPersist(lineUsers, mids);
		    }
		} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
		} finally {
		    LineIterator.closeQuietly(lineIterator);
		    
			// 記錄總筆數、總共花費時間
			long endTime = System.currentTimeMillis();
			logger.info("======= Import LineUser data is finished =======");
			logger.info("Total size : " + size);
			logger.info("Total cost : " + (endTime - startTime) + " ms");
		}
	}
	
	private int bulkPersist(List<LineUser> lineUsers, List<String> mids) {
		
		// 去除已存在資料庫的 mid
		List<String> existsMid = lineUserService.findMidByMidIn(mids);
		mids.removeAll(existsMid);
		
		if (mids.isEmpty()) {
			return 0;
		}
		
		// mids to lineUsers
		for (String mid : mids) {
	        LineUser lineUser = new LineUser();
	        lineUser.setMid(mid);
	        lineUser.setStatus(LineUser.STATUS_UNBIND);
	        lineUsers.add(lineUser);
		}
		
    	lineUserService.bulkPersist(lineUsers);
    	int persistSize = lineUsers.size();
    	lineUsers.clear();
    	mids.clear();
    	return persistSize;
	}
	
	private void importSendGroupQueryTag(String queryFieldId, File sendGroupQueryTagCsv) throws FileNotFoundException, UnsupportedEncodingException {	    
	    logger.info("Start importing SendGroupQuery data : " + sendGroupQueryTagCsv);

		Reader sendGroupQueryTagReader=new BufferedReader(new InputStreamReader(new FileInputStream(sendGroupQueryTagCsv), StandardCharsets.UTF_8));  
	    try {
			Iterable<CSVRecord> tagRecords = CSVFormat.EXCEL.withHeader().withTrim().parse(sendGroupQueryTagReader);
			for (CSVRecord tagRecord : tagRecords) {
				String queryFieldTagDisplay = tagRecord.get("QUERY_FIELD_TAG_DISPLAY");
				String queryFieldTagValue = tagRecord.get("QUERY_FIELD_TAG_Value");
				int tagIndex = Integer.parseInt(tagRecord.get("TAG_INDEX"));
				
				// 略過相同的 queryFieldId、queryFieldTagValue 的 SendGroupQueryTag
				if (sendGroupQueryTagService.countBySendGroupQueryQueryFieldIdAndQueryFieldTagValue(
						queryFieldId, queryFieldTagValue) > 0) {
					continue;
				}
				
				SendGroupQuery sendGroupQuery = sendGroupQueryService.findOne(queryFieldId);
				
				SendGroupQueryTag sendGroupQueryTag = new SendGroupQueryTag();
				sendGroupQueryTag.setSendGroupQuery(sendGroupQuery);
				sendGroupQueryTag.setQueryFieldTagDisplay(queryFieldTagDisplay);
				sendGroupQueryTag.setQueryFieldTagValue(queryFieldTagValue);
				sendGroupQueryTag.setTagIndex(tagIndex);
				sendGroupQueryTagService.save(sendGroupQueryTag);
			}
		} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
		} finally {
			IOUtils.closeQuietly(sendGroupQueryTagReader);
		}
	    
	    logger.info(sendGroupQueryTagCsv + " import data is finished");
	}
}
