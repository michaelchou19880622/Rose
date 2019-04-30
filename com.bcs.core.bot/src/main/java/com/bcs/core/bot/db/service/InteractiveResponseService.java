package com.bcs.core.bot.db.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.db.repository.MsgBotReceiveRepository;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.DBResultUtil;

@Service
public class InteractiveResponseService {
	/** Logger */
	private static Logger logger = Logger.getLogger(InteractiveResponseService.class);
	@Autowired
	private MsgBotReceiveRepository msgBotReceiveService;
	
	/**
	 * Count Interactive Response Distinct
	 * 
	 * @param iMsgId
	 * @return
	 * @throws ParseException
	 */
	public Long countInteractiveResponseDistinct(Long iMsgId) throws ParseException{
		return countInteractiveResponseDistinct(iMsgId, false);
	}
	
	private Map<String, Long> interactiveResponseDistinctCache = new HashMap<String, Long>();
	
	/**
	 * Count Interactive Response Distinct
	 * 
	 * @param iMsgId
	 * @param withCache
	 * @return
	 * @throws ParseException
	 */
	public Long countInteractiveResponseDistinct(Long iMsgId, boolean withCache) throws ParseException{
		
		if(withCache){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			String systemStartDate =CoreConfigReader.getString(CONFIG_STR.SYSTEM_START_DATE);
			
			Calendar yesterdayCalendar = Calendar.getInstance();
			yesterdayCalendar.add(Calendar.DATE, -1);

			Calendar nextCalendar = Calendar.getInstance();
			nextCalendar.add(Calendar.DATE, 1);
			
			String yesterdayStr =  iMsgId + "--" + sdf.format(yesterdayCalendar.getTime());
			String nowDate = sdf.format(new Date());
			logger.debug("nowDate:" + nowDate);
			String nextDate = sdf.format(nextCalendar.getTime());
			logger.debug("nextDate:" + nextDate);
			
			Long yesterdayCount = interactiveResponseDistinctCache.get(yesterdayStr);
			if(yesterdayCount == null){
				List<Object[]> list  = msgBotReceiveService.countReceiveByReferenceId(iMsgId.toString(), systemStartDate, nowDate);

				for(Object[] objArray : list){
					yesterdayCount = DBResultUtil.caseCountResult(objArray[1], false).longValue();
				}
				
				interactiveResponseDistinctCache.put(yesterdayStr, yesterdayCount);
			}

			Long result = 0L;
			List<Object[]> list  = msgBotReceiveService.countReceiveByReferenceId(iMsgId.toString(), nowDate, nextDate);

			for(Object[] objArray : list){
				result = DBResultUtil.caseCountResult(objArray[1], false).longValue();
			}
			
			return result + yesterdayCount;
		}
		
		return msgBotReceiveService.countReferenceIdResponseDistinct(iMsgId.toString());
	}
}
