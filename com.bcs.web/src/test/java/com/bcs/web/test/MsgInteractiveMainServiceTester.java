package com.bcs.web.test;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bcs.core.api.test.SpringJUnit4BaseTester;
import com.bcs.core.bot.db.service.InteractiveResponseService;

public class MsgInteractiveMainServiceTester extends SpringJUnit4BaseTester {

	@Autowired
	InteractiveResponseService interactiveResponseService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(MsgInteractiveMainServiceTester.class);

	@Test
	public void countInteractiveResponseDistinct() throws Exception {
		Long iMsgId = 7L;
		
		Long count = interactiveResponseService.countInteractiveResponseDistinct(iMsgId, true);
		logger.info("countInteractiveResponseDistinct count:" + count);
	}
}
