package com.bcs.core.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SQLDateFormatUtil {

    
    public static Date formatSqlStringToDate(Object target, SimpleDateFormat sdf) throws ParseException {
        
        if(target instanceof String) {
            return sdf.parse(target.toString().substring(0, 19));
        }
        else {
            return (Date)target;
        }
    }
}
