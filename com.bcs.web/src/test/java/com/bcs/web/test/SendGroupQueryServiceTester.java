package com.bcs.web.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bcs.core.api.test.SpringJUnit4BaseTester;
import com.bcs.core.db.entity.SendGroupQuery;
import com.bcs.core.db.entity.SendGroupQueryTag;
import com.bcs.core.db.service.SendGroupQueryService;

public class SendGroupQueryServiceTester extends SpringJUnit4BaseTester {

	@Autowired
	SendGroupQueryService sendGroupQueryService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(SendGroupQueryServiceTester.class);

	@Test
	public void sendGroupQueryServiceSave() throws Exception {
	    logger.info("sendGroupQueryServiceSave");

	    // 縣市
	    List<SendGroupQueryTag> IK0108List = new ArrayList<SendGroupQueryTag>();
	    SendGroupQuery IK0108 = new SendGroupQuery();
	    IK0108.setQueryFieldId("IK0108");
	    IK0108.setQueryFieldName("縣市");
	    IK0108.setQueryFieldOp("=");
	    IK0108.setQueryFieldFormat("String");
	    IK0108.setQueryFieldSet("SelectList");
	    IK0108.setSendGroupQueryTag(IK0108List);

	    sendGroupQueryService.save(IK0108);
	    
	    SendGroupQueryTag tag = new SendGroupQueryTag();
	    tag.setSendGroupQuery(IK0108);
	    tag.setQueryFieldTagValue("台北市");
	    tag.setQueryFieldTagDisplay("台北市");
	    tag.setTagIndex(0);
	    IK0108List.add(tag);
	    SendGroupQueryTag tag2 = new SendGroupQueryTag();
	    tag2.setSendGroupQuery(IK0108);
	    tag2.setQueryFieldTagValue("新竹縣");
	    tag2.setQueryFieldTagDisplay("新竹縣");
	    tag2.setTagIndex(1);
	    IK0108List.add(tag2);
	    sendGroupQueryService.save(IK0108);
	    
	    // 所屬店點
	    List<SendGroupQueryTag> IK0136List = new ArrayList<SendGroupQueryTag>();
	    SendGroupQuery IK0136 = new SendGroupQuery();
	    IK0136.setQueryFieldId("IK0136");
	    IK0136.setQueryFieldName("所屬店點");
	    IK0136.setQueryFieldOp("=");
	    IK0136.setQueryFieldFormat("String");
	    IK0136.setQueryFieldSet("SelectList");
	    IK0136.setSendGroupQueryTag(IK0136List);

	    sendGroupQueryService.save(IK0136);
	    
	    SendGroupQueryTag tag3 = new SendGroupQueryTag();
	    tag3.setSendGroupQuery(IK0136);
	    tag3.setQueryFieldTagValue("AWS");
	    tag3.setQueryFieldTagDisplay("敦北");
	    tag3.setTagIndex(0);
	    IK0136List.add(tag3);
	    SendGroupQueryTag tag4 = new SendGroupQueryTag();
	    tag4.setSendGroupQuery(IK0136);
	    tag4.setQueryFieldTagValue("TYS");
	    tag4.setQueryFieldTagDisplay("桃園");
	    tag4.setTagIndex(1);
	    IK0136List.add(tag4);
	    sendGroupQueryService.save(IK0136);
	    
	    // NES status
	    List<SendGroupQueryTag> NES_TAGList = new ArrayList<SendGroupQueryTag>();
	    SendGroupQuery NES_TAG = new SendGroupQuery();
	    NES_TAG.setQueryFieldId("NES_TAG");
	    NES_TAG.setQueryFieldName("NES status");
	    NES_TAG.setQueryFieldOp("=");
	    NES_TAG.setQueryFieldFormat("String");
	    NES_TAG.setQueryFieldSet("SelectList");
	    NES_TAG.setSendGroupQueryTag(NES_TAGList);

	    sendGroupQueryService.save(NES_TAG);
	    
	    SendGroupQueryTag tag5 = new SendGroupQueryTag();
	    tag5.setSendGroupQuery(NES_TAG);
	    tag5.setQueryFieldTagValue("NEW");
	    tag5.setQueryFieldTagDisplay("NEW");
	    tag5.setTagIndex(0);
	    NES_TAGList.add(tag5);
	    SendGroupQueryTag tag6 = new SendGroupQueryTag();
	    tag6.setSendGroupQuery(NES_TAG);
	    tag6.setQueryFieldTagValue("Existing");
	    tag6.setQueryFieldTagDisplay("Existing");
	    tag6.setTagIndex(1);
	    NES_TAGList.add(tag6);
	    sendGroupQueryService.save(NES_TAG);
	    
	    // HF歷史累計消費次數
	    SendGroupQuery HF_Total_Cnt = new SendGroupQuery();
	    HF_Total_Cnt.setQueryFieldId("HF_Total_Cnt");
	    HF_Total_Cnt.setQueryFieldName("HF歷史累計消費次數");
	    HF_Total_Cnt.setQueryFieldOp(">,>=,<,<=,=");
	    HF_Total_Cnt.setQueryFieldFormat("Integer");
	    HF_Total_Cnt.setQueryFieldSet("Input");
	    HF_Total_Cnt.setSendGroupQueryTag(new ArrayList<SendGroupQueryTag>());

	    sendGroupQueryService.save(HF_Total_Cnt);
	    
	    // HF歷史累計消費次數
	    SendGroupQuery HF_Last_Buy_Date = new SendGroupQuery();
	    HF_Last_Buy_Date.setQueryFieldId("HF_Last_Buy_Date");
	    HF_Last_Buy_Date.setQueryFieldName("HF最後一次消費日期");
	    HF_Last_Buy_Date.setQueryFieldOp(">,>=,<,<=,=");
	    HF_Last_Buy_Date.setQueryFieldFormat("Date");
	    HF_Last_Buy_Date.setQueryFieldSet("DatePicker");
	    HF_Last_Buy_Date.setSendGroupQueryTag(new ArrayList<SendGroupQueryTag>());

	    sendGroupQueryService.save(HF_Last_Buy_Date);
	    
	    // 自動化溝通線
	    List<SendGroupQueryTag> Contact_typeList = new ArrayList<SendGroupQueryTag>();
	    SendGroupQuery Contact_type = new SendGroupQuery();
	    Contact_type.setQueryFieldId("Contact_type");
	    Contact_type.setQueryFieldName("自動化溝通線");
	    Contact_type.setQueryFieldOp("=");
	    Contact_type.setQueryFieldFormat("Integer");
	    Contact_type.setQueryFieldSet("SelectList");
	    Contact_type.setSendGroupQueryTag(Contact_typeList);

	    sendGroupQueryService.save(Contact_type);
	    
	    SendGroupQueryTag tag9 = new SendGroupQueryTag();
	    tag9.setSendGroupQuery(Contact_type);
	    tag9.setQueryFieldTagValue("101");
	    tag9.setQueryFieldTagDisplay("通知壽星領取主餐買一送一卷");
	    tag9.setTagIndex(0);
	    Contact_typeList.add(tag9);
	    SendGroupQueryTag tag10 = new SendGroupQueryTag();
	    tag10.setSendGroupQuery(Contact_type);
	    tag10.setQueryFieldTagValue("201");
	    tag10.setQueryFieldTagDisplay("當月卡友價商品享有卡友價");
	    tag10.setTagIndex(1);
	    Contact_typeList.add(tag10);
	    sendGroupQueryService.save(Contact_type);
	}
}
