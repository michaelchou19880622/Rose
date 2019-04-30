package com.bcs.core.taishin.api.model;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import com.bcs.core.json.AbstractBcsEntity;

public class LogApiModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	// function type
	public static String CREATE = "A";// 新增
	public static String DELETE = "D";// 刪除
	public static String UPDATE = "E";// 變更
	public static String READ = "Q";// 查詢
	public static String REPORT = "R";// 報表
	public static String EXPORT = "O";// 匯出下載
	public static String PRINT = "P";// 列印
	public static String LOGIN = "L";// 登入
	public static String LOGOUT = "X";// 登出
	// function status
	public static String SUCCESS = "Y";// 登出
	// System Code
	public static String SYSTEM_CODE = "LINE-BC";

	private String systemCode = SYSTEM_CODE;
	private String userId = "";
	private String sensitiveData = "";
	
	private String functionType;
	private String functionName;
	private String functionStatus;
	private String clientIp;
	
	private String queryString;
	private Object data;
	
	private LogApiModel(LogApiModelBuilder logApiModelBuilder){
		this.userId = logApiModelBuilder.userId;
		this.sensitiveData = logApiModelBuilder.sensitiveData;
		this.functionType=logApiModelBuilder.functionType;
		this.functionName = logApiModelBuilder.functionName;
		this.functionStatus = logApiModelBuilder.functionStatus;
		this.clientIp = logApiModelBuilder.clientIp;
		this.queryString = logApiModelBuilder.queryString;
		this.data = logApiModelBuilder.data;
	}
	
	public static class LogApiModelBuilder {
		private String userId = "";
		private String sensitiveData = "";

		private String functionType;
		private String functionName;
		private String functionStatus;
		private String clientIp;

		private String queryString;
		private Object data;
		

		public LogApiModelBuilder() {
		}
		
		public LogApiModelBuilder functionName(String functionName){
			this.functionName = functionName;
			return this;
		}
		
		public LogApiModelBuilder functionType(String functionType){
			this.functionType = functionType;
			return this;
		}
		
		public LogApiModelBuilder functionStatus(String functionStatus){
			this.functionStatus = functionStatus;
			return this;
		}
		
		public LogApiModelBuilder clientIp(String clientIp){
			this.clientIp = clientIp;
			return this;
		}
		
		public LogApiModelBuilder queryString(String queryString){
			this.queryString = queryString;
			return this;
		}
		
		public LogApiModelBuilder data(Object data){
			this.data = data;
			return this;
		}
		
		public LogApiModelBuilder userId(String userId){
			this.userId = userId;
			return this;
		}
		
		public LogApiModelBuilder sensitiveData(String sensitiveData){
			this.sensitiveData = sensitiveData;
			return this;
		}
		
		public LogApiModel build() {
		      return new LogApiModel(this);
		}
	}

	public String getSystemCode() {
		return systemCode;
	}

	public String getUserId() {
		return userId;
	}

	public String getSensitiveData() {
		return sensitiveData;
	}
	
	public String getFunctionType() {
		return functionType;
	}

	public String getFunctionName() {
		return functionName;
	}

	public String getFunctionStatus() {
		return functionStatus;
	}

	public String getClientIp() {
		return clientIp;
	}

	public String getQueryString() {
		return queryString;
	}
	public Object getData() {
		return data;
	}
	
	
}
