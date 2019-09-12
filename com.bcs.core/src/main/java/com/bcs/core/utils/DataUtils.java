package com.bcs.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    public static String toPrettyJsonUseJackson(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.getClass().getName() + '@' + Integer.toHexString(obj.hashCode());
        }
    }
}