package com.bcs.core.utils;

import java.math.BigInteger;

public class DBResultUtil {

	public static BigInteger caseCountResult(Object count){
		return caseCountResult(count, true);
	}
	
	/**
	 * Case Count Result
	 * @param count
	 * @return BigInteger
	 */
	public static BigInteger caseCountResult(Object count, boolean returnNull){
		if(count == null){
			if(returnNull){
				return null;
			}
			else{
				return BigInteger.ZERO;
			}
		}
		
		if (count instanceof BigInteger) {
			return (BigInteger) count;
		}
		else if(count instanceof Integer) {
			Integer i = (Integer) count;
			Long l = new Long(i);
			return new BigInteger(l.toString());
		}
		else if(count instanceof Long){
			Long l = (Long) count;
			return new BigInteger(l.toString());
		}
		
		return BigInteger.ZERO;
	}
}
