package com.bcs.core.importdata;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.repository.UserTraceLogRepository;
import com.bcs.core.db.service.ContentReportService;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ImportLinkIdToReportService {
	@Autowired
	private ContentReportService contentReportService;
	@Autowired
	private UserTraceLogRepository userTraceLogRepository;
	
	private static Logger logger = Logger.getLogger(ImportLinkIdToReportService.class);
	
	public void createPushReportByLinkId() {
		try {
			
			int page = 0;
			int pageSize = 1000;
			while(true){
				Pageable pageable = new PageRequest(page, pageSize);
				Page<UserTraceLog> result = userTraceLogRepository.findByTargetAndAction("ContentLink", "ClickLink", pageable);
				if(result != null){
					List<UserTraceLog> logs = result.getContent();
					if(logs != null && logs.size() > 0){
						for(UserTraceLog log : logs){
							contentReportService.countClickNumber(log.getReferenceId(), log.getModifyTime());
						}
					}
					else{
						break;
					}
				}
				else{
					break;
				}
				
				page++;
			}
		} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
		}
		logger.info("======= Import linkId to report is finished =======");
	}
}
