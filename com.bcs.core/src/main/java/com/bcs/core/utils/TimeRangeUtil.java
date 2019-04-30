package com.bcs.core.utils;

import java.util.Calendar;

public class TimeRangeUtil {
	
	/**
	 * Compare Time in Day Range : 
	 * 	Ex: Start 09:30 ~ End 18:20 > now 09:40 return true;
	 * 	Ex: Start 09:30 ~ End 18:20 > now 12:40 return true;
	 * 	Ex: Start 09:30 ~ End 18:20 > now 07:40 return false;
	 * 	Ex: Start 09:30 ~ End 18:20 > now 19:40 return false;
	 * @param calendarNow
	 * @param calendarStart
	 * @param calendarEnd
	 * @return
	 */
	public static boolean isDayRange(Calendar calendarNow, Calendar calendarStart, Calendar calendarEnd){

		// 09:30 ~ 1820  : now hour > 09 && now hour < 18
		if(calendarNow.get(Calendar.HOUR_OF_DAY) > calendarStart.get(Calendar.HOUR_OF_DAY) && 
				calendarNow.get(Calendar.HOUR_OF_DAY) < calendarEnd.get(Calendar.HOUR_OF_DAY)){
			return true;
		}
		// 09:10 ~ 18:20 : now hour == 09
		else if(calendarNow.get(Calendar.HOUR_OF_DAY) == calendarStart.get(Calendar.HOUR_OF_DAY)){
			// 09:10 ~ 09:20 : now hour == 09 && now hour == 09
			if(calendarNow.get(Calendar.HOUR_OF_DAY) == calendarEnd.get(Calendar.HOUR_OF_DAY)){
				if(calendarNow.get(Calendar.MINUTE) >= calendarStart.get(Calendar.MINUTE) && 
						calendarNow.get(Calendar.MINUTE) < calendarEnd.get(Calendar.MINUTE)){
					return true;
				}	
			}
			// 09:10 ~ 18:20 : now hour == 09 && now minute >= 10
			else if(calendarNow.get(Calendar.MINUTE) >= calendarStart.get(Calendar.MINUTE)){
				return true;
			}	
		}
		// 09:10 ~ 18:20 : now hour == 18
		else if(calendarNow.get(Calendar.HOUR_OF_DAY) == calendarEnd.get(Calendar.HOUR_OF_DAY)){
			// 09:10 ~ 18:20 : now hour == 18 && now minute < 20
			if(calendarNow.get(Calendar.MINUTE) < calendarEnd.get(Calendar.MINUTE)){
				return true;
			}	
		}
		
		return false;
	}
}
