package com.bcs.core.utils;

import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 工具類別
 *
 * @author Alan
 */
@UtilityClass
public class DataUtils {
    public static String toPrettyJson(Object obj) {
        return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(obj);
    }

    public static String formatDateToString(Date date, String format){
        return new SimpleDateFormat(format).format(date);
    }
}