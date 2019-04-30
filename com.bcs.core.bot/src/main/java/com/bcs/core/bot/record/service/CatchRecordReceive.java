package com.bcs.core.bot.record.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.db.service.MsgBotReceiveService;
import com.bcs.core.record.service.CatchRecordAbstract;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;

@Service
public class CatchRecordReceive extends CatchRecordAbstract {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(CatchRecordReceive.class);

	@Autowired
	private MsgBotReceiveService msgBotReceiveService;
	
	@Override
	public void loadInitData() {

		if(!CoreConfigReader.isSystemTypeProduction()){
			return;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE,  -3);
		Date dayEnd = calendar.getTime();

		Sort.Order order = new Sort.Order(Direction.DESC, "id");
		Sort sort = new Sort(order);
		
		try{
			int page = 0;
			int pageSize = 10000;
			while(true){
				Pageable pageable = new PageRequest(page, pageSize, sort);
				Page<MsgBotReceive> result = msgBotReceiveService.findAll(pageable);
				if(result != null){
					List<MsgBotReceive> logs =  result.getContent();
					if(logs != null && logs.size() > 0){
						for(MsgBotReceive log : logs){
							Date date = log.getReceiveTime();
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
