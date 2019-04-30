package com.bcs.core.db.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.RecordReport;
import com.bcs.core.db.repository.RecordReportRepository;

@Service
public class RecordReportService {
	/** Logger */
	private static Logger logger = Logger.getLogger(RecordReportService.class);
	@Autowired
	private RecordReportRepository recordReportRepository;
	
	public void delete(RecordReport recordReport){
		recordReportRepository.delete(recordReport);
	}

	public RecordReport saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(String recordTimeStr, String referenceId, String contentType, String dataType, Long recordCount) throws Exception{
		logger.debug("saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date recordTime = sdf.parse(recordTimeStr);
		
		Date now = new Date();
		now = sdf.parse(sdf.format(now));
		// Today Can not Record
		if(recordTime.compareTo(now) >= 0){
			return null;
		}
		
		if(StringUtils.isBlank(referenceId)){
			return null;
		}
		
		if(StringUtils.isBlank(contentType)){
			return null;
		}
		
		if(StringUtils.isBlank(dataType)){
			return null;
		}
		
		RecordReport record =  new RecordReport();
		
		record.setRecordTime(recordTime);
		if(referenceId.length() > 50){
			record.setIncreaseId(referenceId);
		}
		else{
			record.setReferenceId(referenceId);
		}
		record.setContentType(contentType);
		record.setDataType(dataType);
		
		record.setRecordCount(recordCount);
		
		return recordReportRepository.save(record);
	}
	
	public RecordReport findRecordReportByRecordTime(String referenceId, String contentType, String dataType, String recordTimeStr) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date recordTime = sdf.parse(recordTimeStr);
		
		return recordReportRepository.findByReferenceIdAndContentTypeAndDataTypeAndRecordTime(referenceId, contentType, dataType, recordTime);
	}
	
	public Map<String, Long> findRecordReportListByRecordTime(String referenceId, String contentType, String dataType, String startTimeStr, String endTimeStr) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.parse(startTimeStr);
		sdf.parse(endTimeStr);
		
		List<RecordReport> list = recordReportRepository.findRecordReportListByRecordTime(referenceId, contentType, dataType, startTimeStr, endTimeStr);
		return parseDataToMap(list);
	}
	
	public Map<String, Long> findRecordReportList(String referenceId, String contentType, String dataType){
		List<RecordReport> list = recordReportRepository.findByReferenceIdAndContentTypeAndDataType(referenceId, contentType, dataType);
		return parseDataToMap(list);
	}
	
	private  Map<String, Long> parseDataToMap(List<RecordReport> list){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Map<String, Long> result = new HashMap<String, Long>();
		
		for(RecordReport record : list){
			String timeStr = sdf.format(record.getRecordTime());
			
			result.put(timeStr, record.getRecordCount());
		}
		
		return result;
	}
	
	public List<RecordReport> findRecordReportListByRecordTime(String startTimeStr, String endTimeStr){
		return recordReportRepository.findRecordReportListByRecordTime(startTimeStr, endTimeStr);
	}
	
	public List<RecordReport> findRecordReportListByRecordTimeAndReferenceId(String startTimeStr, String endTimeStr, String referenceId){
		return recordReportRepository.findRecordReportListByRecordTimeAndReferenceId(startTimeStr, endTimeStr, referenceId);
	}
	
	public List<RecordReport> findRecordReportListByRecordTimeAndContentType(String startTimeStr, String endTimeStr, String contentType){
		return recordReportRepository.findRecordReportListByRecordTimeAndContentType(startTimeStr, endTimeStr, contentType);
	}
	
	public List<RecordReport> findRecordReportListByRecordTimeAndDataType(String startTimeStr, String endTimeStr, String dataType){
		return recordReportRepository.findRecordReportListByRecordTimeAndDataType(startTimeStr, endTimeStr, dataType);
	}
	
	/**
	 * Key : Time
	 * 	Map : Key dataType
	 * 				Value Count
	 * @param referenceId
	 * @param contentType
	 * @param startTimeStr
	 * @param endTimeStr
	 * @return Map<String, Map<String, Long>>
	 * @throws ParseException
	 */
	public Map<String, Map<String, Long>> findRecordReportListByContentType(String referenceId, String contentType, String startTimeStr, String endTimeStr) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.parse(startTimeStr);
		sdf.parse(endTimeStr);
		
		List<RecordReport> list = recordReportRepository.findRecordReportListByRecordTime(referenceId, contentType, startTimeStr, endTimeStr);

		return parseDataToMapWithDataType(list);
	}
	
	/**
	 * Key : Time
	 * 	Map : Key dataType
	 * 				Value Count
	 * 
	 * @param referenceId
	 * @param contentType
	 * @return Map<String, Map<String, Long>>
	 */
	public Map<String, Map<String, Long>> findRecordReportListByContentType(String referenceId, String contentType){
		
		List<RecordReport> list = recordReportRepository.findByReferenceIdAndContentType(referenceId, contentType);
		
		return parseDataToMapWithDataType(list);
	}
	
	private Map<String, Map<String, Long>> parseDataToMapWithDataType(List<RecordReport> list){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Map<String, Map<String, Long>> result = new HashMap<String, Map<String, Long>>();
		
		for(RecordReport record : list){
			String timeStr = sdf.format(record.getRecordTime());
					
			Map<String, Long> map = result.get(timeStr);
			if(map == null){
				map = new HashMap<String, Long>();
				result.put(timeStr, map);
			}
			
			map.put(record.getDataType(), record.getRecordCount());
		}
		
		return result;
	}
}
