package com.bcs.core.json;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class CustomDateDeserializer extends JsonDeserializer<Date> {

	@Override
	public Date deserialize(JsonParser jsonparser,
			DeserializationContext deserializationcontext) throws IOException,
			JsonProcessingException {
		String strDate = jsonparser.getText();
		
		if (StringUtils.isBlank(strDate)) {
			return null;
		}
		
		try {
			return DateUtils.parseDate(strDate, new String[] {
					"yyyy-MM-dd HH:mm:ss", 
					"yyyy-MM-dd", 
					"yyyy/MM/dd HH:mm:ss",
					"yyyy/MM/dd",
					"yyyy-MM-dd'T'HH:mm:ss'Z'",
					"yyyy/MM/dd'T'HH:mm:ss'Z'"});
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

	}

}