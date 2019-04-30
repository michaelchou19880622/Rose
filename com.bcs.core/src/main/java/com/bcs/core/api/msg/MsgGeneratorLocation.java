package com.bcs.core.api.msg;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.bcs.core.db.entity.MsgDetail;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.message.LocationMessage;

public class MsgGeneratorLocation extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;

	public static boolean canSetMulti() throws Exception {
		return true;
	}
	
	private String text;
	private String title;
	private BigDecimal latitude;
	private BigDecimal longitude;

	public MsgGeneratorLocation(String text, String title, BigDecimal latitude, BigDecimal longitude) throws Exception{
		super();
		generatorContent(text, title, latitude, longitude, 1);
		
		message = new LocationMessage(title, text, latitude.doubleValue(), longitude.doubleValue());
	}
	
	public MsgGeneratorLocation(String text, String title, BigDecimal latitude, BigDecimal longitude, int toType) throws Exception{
		super();
		generatorContent(text, title, latitude, longitude, toType);
		
		message = new LocationMessage(title, text, latitude.doubleValue(), longitude.doubleValue());
	}
	
	public MsgGeneratorLocation(ObjectNode node) throws Exception{
		super();
		if (node.get("toType") == null) {
			generatorContent(node.get("text").textValue(),
					node.get("title").textValue(),
					new BigDecimal(node.get("latitude").textValue()),
					new BigDecimal(node.get("longitude").textValue()), 1);
			
			message = new LocationMessage(title, text, latitude.doubleValue(), longitude.doubleValue());
		} else {
			generatorContent(node.get("text").textValue(),
					node.get("title").textValue(),
					new BigDecimal(node.get("latitude").textValue()),
					new BigDecimal(node.get("longitude").textValue()), node
							.get("toType").intValue());
			
			message = new LocationMessage(title, text, latitude.doubleValue(), longitude.doubleValue());
		}
	}

	public MsgGeneratorLocation(MsgDetail detail) throws Exception{
		super();
		generatorContent(detail, 1);
		
		message = new LocationMessage(title, text, latitude.doubleValue(), longitude.doubleValue());
	}
	
	public MsgGeneratorLocation(MsgDetail detail, int toType) throws Exception{
		super();
		generatorContent(detail, toType);
		
		message = new LocationMessage(title, text, latitude.doubleValue(), longitude.doubleValue());
	}
	
	private ObjectNode generatorContent(MsgDetail detail, int toType) throws Exception{
		throw new Exception("MsgGeneratorLocation generatorContent from detail Error");
	}
	
	private void generatorContent(String text, String title, BigDecimal latitude, BigDecimal longitude, int toType) throws Exception{
		
		if(StringUtils.isBlank(text)){
			throw new Exception("text Can not be Null");
		}

		if(StringUtils.isBlank(title)){
			throw new Exception("title Can not be Null");
		}

		if(latitude == null){
			throw new Exception("latitude Can not be Null");
		}

		if(longitude == null){
			throw new Exception("longitude Can not be Null");
		}
		
		this.text = text;
		this.title = title;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
