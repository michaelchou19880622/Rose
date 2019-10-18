package com.bcs.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 工具類別
 *
 * @author Alan
 */
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
            ObjectMapper mapper = new ObjectMapper();
            Object jsonObject = mapper.readValue(jsonString, Object.class);

            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(jsonObject);
        } catch (Exception e) {
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
}