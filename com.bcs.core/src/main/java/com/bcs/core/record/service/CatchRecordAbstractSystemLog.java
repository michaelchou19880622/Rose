package com.bcs.core.record.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;


public abstract class CatchRecordAbstractSystemLog extends CatchRecordAbstract {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(CatchRecordAbstractSystemLog.class);
	
	protected void loadFromSystemLog(UserTraceLogService systemLogService, String target, String action){

		if(!CoreConfigReader.isSystemTypeProduction()){
			return;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE,  -3);
		Date dayEnd = calendar.getTime();

		Sort.Order order = new Sort.Order(Direction.DESC, "logId");
		Sort sort = new Sort(order);
		
		try{
			int page = 0;
			int pageSize = 10000;
			while(true){
				Pageable pageable = new PageRequest(page, pageSize, sort);
				Page<UserTraceLog> result = systemLogService.findByTargetAndAction(target, action, pageable);
				if(result != null){
					List<UserTraceLog> logs =  result.getContent();
					if(logs != null && logs.size() > 0){
						for(UserTraceLog log : logs){
							Date date = log.getModifyTime();
							// if > 10 day return
							if(dayEnd.getTime() > date.getTime()){
								return;
							}
							
							this.incrementCount(date);
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
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
	}
}
