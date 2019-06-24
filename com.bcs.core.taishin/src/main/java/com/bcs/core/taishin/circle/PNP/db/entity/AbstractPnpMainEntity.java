package com.bcs.core.taishin.circle.PNP.db.entity;

import java.io.Serializable;

import com.bcs.core.utils.ObjectUtil;

public abstract class AbstractPnpMainEntity implements Serializable, Cloneable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String SEND_TYPE_IMMEDIATE = "IMMEDIATE";
	public static final String SEND_TYPE_DELAY = "DELAY";
	//排程時間小於現在時間則視為立即發送，為了好追查，多設一個type，SCHEDULE_EXPIRED，針對排程時間過期使用
	public static final String SEND_TYPE_SCHEDULE_TIME_EXPIRED  = "SCH_EXPRED";
	
	//未發送 ：已存進DB未開始發送
	public static final String MSG_SENDER_STATUS_PROCESS = "PROCESS";
	public static final String MSG_SENDER_STATUS_FINISH = "FINISH";
	public static final String MSG_SENDER_STATUS_FAIL = "FAIL";
	//發完PNP後進入此狀態   ，待web hook在24小時內收到DELIVERY則將該則訊息update成COMPLETE，若24小時內沒收到DELIVERY則將該訊息轉發SMS
	public static final String MSG_SENDER_STATUS_CHECK_DELIVERY= "CHECK_DELIVERY";
	
	public static final String MSG_SENDER_STATUS_SENDING = "SENDING";
	public static final String MSG_SENDER_STATUS_DELETE = "DELETE";
	
	//草案：正在存進DB中
	public static final String DATA_CONVERTER_STATUS_DRAFT = "DRAFT";
	public static final String DATA_CONVERTER_STATUS_WAIT = "WAIT";
	//完成
	public static final String DATA_CONVERTER_STATUS_COMPLETE = "COMPLETE";
	//預約
	public static final String DATA_CONVERTER_STATUS_SCHEDULED = "SCHEDULED";
	
	//三竹來源
	public static final String SOURCE_MITAKE = "1";
	//互動來源
	public static final String SOURCE_EVERY8D = "2";
	//明宣來源
	public static final String SOURCE_MING = "3";
	//UNICA來源
	public static final String SOURCE_UNICA = "4";
	
	//通路參數 : 寄BC 失敗直接結束
	public static final String PROC_FLOW_BC = "1";
	//通路參數 : 寄BC 失敗轉發SMS後結束
	public static final String PROC_FLOW_BC_SMS = "2";
	//通路參數 : 寄BC 失敗後寄PNP失敗後寄SMS結束
	public static final String PROC_FLOW_BC_PNP_SMS = "3";
	
	//stage : BC 
	public static final String STAGE_BC = "BC";
	//stage : PNP 
	public static final String STAGE_PNP = "PNP";
	//stage : SMS 
	public static final String STAGE_SMS = "SMS";
	
	@Override
	public String toString() {
	   return ObjectUtil.objectToJsonStr(this);
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
