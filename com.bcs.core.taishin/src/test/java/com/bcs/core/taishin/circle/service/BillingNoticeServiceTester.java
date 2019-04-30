package com.bcs.core.taishin.circle.service;

import java.util.Calendar;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsg;

import junit.framework.Assert;

public class BillingNoticeServiceTester {

	private static BillingNoticeService service = null;
	private BillingNoticeContentTemplateMsg template = null;
	
	@BeforeClass
	public static void beforClass() {
		service = new BillingNoticeService();
	}

	@Before
	public void beforMethod() {
		 template = new BillingNoticeContentTemplateMsg();
	}
	
	/**
	 * 不設定宵禁
	 * @throws Exception
	 */
	@Test
	public void isCurfew_Null() throws Exception {
		Calendar endOfMarch = Calendar.getInstance();
		boolean result = service.isCurfew(template, endOfMarch);
		Assert.assertFalse(result);
	}
	
	/**
	 * 格式錯誤
	 */
	@Test
	public void isCurfew_Exception(){
		Calendar endOfMarch = Calendar.getInstance();
		endOfMarch.set(2019, Calendar.MARCH, 11, 10, 30, 0);
		template.setCurfewStartTime("2019-03-01 10:00:00");
		template.setCurfewEndTime("13:10:00");
		boolean result = service.isCurfew(template, endOfMarch);
		Assert.assertFalse(result);
	}
	
	/**
	 * 跨日宵禁 - 未達宵禁時間
	 * @throws Exception
	 */
	@Test
	public void isCurfew_SameDay() throws Exception {
		Calendar endOfMarch = Calendar.getInstance();
		endOfMarch.set(2019, Calendar.MARCH, 11, 9, 30,0);
		template.setCurfewStartTime("10:00:00");
		template.setCurfewEndTime("00:01:00");
		boolean result = service.isCurfew(template, endOfMarch);
		Assert.assertFalse(result);
	}
	
	/**
	 * 跨日宵禁 - 宵禁時間 跨日前 3/11 10:00 ~ 3/12 01:00 宵禁 now = 3/11 23:10
	 * @throws Exception
	 */
	@Test
	public void isCurfew_StartTimeSameDay() throws Exception {
		Calendar endOfMarch = Calendar.getInstance();
		endOfMarch.set(2019, Calendar.MARCH, 11, 23, 10, 0);
		template.setCurfewStartTime("10:00:00");
		template.setCurfewEndTime("00:01:00");
		boolean result = service.isCurfew(template, endOfMarch);
		Assert.assertTrue(result);
	}
	
	/**
	 * 跨日宵禁 - 宵禁時間 跨日後 3/11 14:00 ~ 3/12 01:00 宵禁 now = 3/12 00:05
	 * @throws Exception
	 */
	@Test
	public void isCurfew_EndTimeSameDay() throws Exception {
		Calendar endOfMarch = Calendar.getInstance();
		endOfMarch.set(2019, Calendar.MARCH, 12, 0, 5, 0);
		template.setCurfewStartTime("14:00:00");
		template.setCurfewEndTime("01:00:00");
		boolean result = service.isCurfew(template, endOfMarch);
		Assert.assertTrue(result);
	}
	
	/**
	 * 未跨日宵禁 - 未達宵禁時間
	 * @throws Exception
	 */
	@Test
	public void isCurfew_Normal_False() throws Exception {
		Calendar endOfMarch = Calendar.getInstance();
		endOfMarch.set(2019, Calendar.MARCH, 11, 8, 11, 0);
		template.setCurfewStartTime("10:00:00");
		template.setCurfewEndTime("13:10:00");
		boolean result = service.isCurfew(template, endOfMarch);
		Assert.assertFalse(result);
	}
	
	/**
	 * 未跨日宵禁 - 達宵禁時間
	 * @throws Exception
	 */
	@Test
	public void isCurfew_Normal_True() throws Exception {
		Calendar endOfMarch = Calendar.getInstance();
		endOfMarch.set(2019, Calendar.MARCH, 11, 11, 11, 0);
		template.setCurfewStartTime("10:00:00");
		template.setCurfewEndTime("13:10:00");
		boolean result = service.isCurfew(template, endOfMarch);
		Assert.assertTrue(result);
	}
}
