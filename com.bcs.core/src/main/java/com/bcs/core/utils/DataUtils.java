package com.bcs.core.utils;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.bcs.core.resource.CoreConfigReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 工具類別
 *
 * @author Alan
 */
@Slf4j
@UtilityClass
public class DataUtils {
    /**
     * To Pretty Json user Gson
     *
     * @param obj obj
     * @return JSON String
     */
    public static String toPrettyJson(Object obj) {
        return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(obj);
    }

    public static String toNormalJson(Object obj) {
        return new GsonBuilder().serializeNulls().create().toJson(obj);
    }

    /**
     * To Pretty Json user Jackson
     *
     * @param obj onj
     * @return JSON String
     */
    public static String toPrettyJsonUseJackson(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.getClass().getName() + '@' + Integer.toHexString(obj.hashCode());
        }
    }

    /**
     * To Pretty Json user Jackson
     *
     * @param jsonString onj
     * @return JSON String
     */
    public static String toPrettyJsonUseJackson(String jsonString) {
        try {
        	if (jsonString == null || jsonString.length() == 0) {
        		return null;
        	}
        	
            ObjectMapper mapper = new ObjectMapper();
            Object jsonObject = mapper.readValue(jsonString, Object.class);

            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(jsonObject);
        } catch (Exception e) {
        	log.info("Exception = {}", e);
            return "";
        }
    }

    /**
     * Format Date to String
     *
     * @param date   date
     * @param format format pattern
     * @return Format Date String
     */
    public static String formatDateToString(Date date, String format) {
        if (date == null || format.trim().isEmpty()) {
            return null;
        }
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * Conv str to date date.
     *
     * @param str    the str
     * @param format the format
     * @return the date
     */
    public static Date convStrToDate(String str, String format) {
        if (str == null || str.trim().isEmpty() || format == null || format.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(format).parse(str);
        } catch (ParseException se) {
            return null;
        }
    }

    /**
     * Conv str to date date.
     *
     * @param str         the str
     * @param inputFormat the format
     * @return the date
     */
    public static String convDateStrToString(String str, String inputFormat, String outputFormate) {
        if (str == null || str.trim().isEmpty() || inputFormat == null || inputFormat.trim().isEmpty()) {
            return "";
        }
        try {
            Date date = new SimpleDateFormat(inputFormat).parse(str);
            return date == null ? null : new SimpleDateFormat(outputFormate).format(date);
        } catch (ParseException se) {
            return "";
        }
    }

    /**
     * Conv date to str string.
     *
     * @param date   the date
     * @param format the format
     * @return the string
     */
    public static String convDateToStr(Date date, String format) {
        if (date == null || format.trim().isEmpty()) {
            return null;
        }
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * Conv date to hhmm string.
     *
     * @param date the date
     * @return the string
     */
    public static String convDateToHHMM(Date date) {
        if (date == null) {
            return null;
        }
        try {
            return new SimpleDateFormat("HH:mm").format(date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Replace Unnecessary Space
     *
     * @param str str
     * @return Replace String
     */
    public static String replaceUnnecessarySpace(String str) {
        return str.replaceAll("\\s{1,}", " ");
    }

    public static Object getOrDefault(Map map, Object key, Object defaultValue) {
        return map.get(key) != null || map.containsKey(key) ? map.get(key) : defaultValue;
    }

    /**
     * isFuture
     *
     * @param compareDate compareDate
     * @return isFuture
     */
    public static boolean isFuture(Date compareDate) {
        return before(new Date(), compareDate);
    }

    public static boolean before(Date date, Date compareDate) {
        if (date == null || compareDate == null) {
            return false;
        }
        return date.before(compareDate);
    }

    /**
     * isPast
     *
     * @param compareDate compareDate
     * @return isPast
     */
    public static boolean isPast(Date compareDate) {
        return after(new Date(), compareDate);
    }

    public static boolean after(Date date, Date compareDate) {
        if (date == null || compareDate == null) {
            return false;
        }
        return date.after(compareDate);

    }

    /**
     * Get Page Row Start And Row End;
     *
     * @param page    page number
     * @param pageRow one page row number
     * @return int[rowStart, rowEnd]
     */
    public static int[] pageRowCalculate(final Integer page, final Integer pageRow) {
        int rowStart;
        int rowEnd;
        int index = page == null || page == 0 ? 0 : page - 1;
        rowStart = index * pageRow + 1;
        rowEnd = rowStart + pageRow;
        return new int[] { rowStart, rowEnd };
    }

    /**
     * 設定為時分秒23:59:59:999
     */
    public static Date truncEndDate(Date srcDate) {
        return customDateTime(srcDate, 23, 59, 59, 999);
    }

    /**
     * 設定為時分秒00:00:00:000
     */
    public static Date truncDate(Date srcDate) {
        return customDateTime(srcDate, 0, 0, 0, 0);
    }

    public static Date customDateTime(Date srcDate, int h, int m, int s, int x) {
        Calendar cal = Calendar.getInstance();
        Date rtnDate = null;
        if (srcDate != null) {
            cal.setTime(srcDate);
            cal.set(Calendar.HOUR_OF_DAY, h);
            cal.set(Calendar.MINUTE, m);
            cal.set(Calendar.SECOND, s);
            cal.set(Calendar.MILLISECOND, x);
            rtnDate = cal.getTime();
        }
        return rtnDate;
    }

    /**
     * Mask String
     *
     * @param sourceString sourceString
     * @param replaceChar  replaceChar
     * @param startIndex   startIndex
     * @param lastIndex    lastIndex
     * @apiNote ("0900123456", "*", 2, 3) => 09*****456
     * @return after mask string
     */
    public static String maskString(String sourceString, char replaceChar, int startIndex, int lastIndex) {
        if (StringUtils.isBlank(sourceString)) {
            return "";
        }
        startIndex = Math.max(startIndex, 0);
        lastIndex = Math.max(lastIndex, 0);
        char[] strArray = sourceString.toCharArray();
        startIndex = Math.min(startIndex, strArray.length);
        lastIndex = lastIndex > strArray.length ? 0 : lastIndex;
        for (int i = startIndex; i < strArray.length - lastIndex; i++) {
            strArray[i] = replaceChar;
        }

        return String.valueOf(strArray);
    }

    public static int calTotalPage(int totalCount, int onePageCount){
        return totalCount / onePageCount + (totalCount % onePageCount == 0 ? 0 : 1);
    }

    public static boolean inBetween(Date compareTime, Date startTime, Date endTime){
        if (compareTime == null || startTime == null || endTime == null) {
            return false;
        }
        return compareTime.before(endTime) && compareTime.after(startTime);
    }


    public static String getProcApName() {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            if (localAddress != null) {
                return localAddress.getHostName();
            }
        } catch (Exception e) {
            log.error("Exception" + e.getMessage());
        }
        return null;
    }

    public static String getProcApIp() {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            if (localAddress != null) {
                return localAddress.getHostAddress();
            }
        } catch (Exception e) {
            log.error("Exception" + e.getMessage());
        }
        return null;
    }

    public static String getRandomProcApName() {
        String processApName = "";

        // FIXME 1 This statement is Hard-Code, should change to 2
        String environment = CoreConfigReader.getString("environment", false);
        switch (environment) {
            case "sit":
                break;
            case "linux":
                processApName = "taishin";
                break;
            case "uat":
                processApName = "AIMLAP-T";
                break;
            case "prod":
                processApName = String.format("AIBCWEB%d", randomNumber(1, 6, Collections.singletonList(2)));
                break;
            default:
                processApName = "";
                break;
        }

        //FIXME 2 use ServerInfo table type is 'AP' list random one
        //TODO ...


        log.info("Process Ap Name -> {}", processApName);
        return processApName;
    }


    public static int randomNumber(int start, int end, List<Integer> excludeNumbers) {
        int value;
        do {
            value = new Random().nextInt(end - start + 1) + start;
        } while (excludeNumbers.indexOf(value) != -1);
        return value;
    }

}