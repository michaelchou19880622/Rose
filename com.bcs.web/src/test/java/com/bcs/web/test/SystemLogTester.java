package com.bcs.web.test;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bcs.core.akka.service.AkkaCoreService;
import com.bcs.core.api.test.SpringJUnit4BaseTester;
import com.bcs.core.db.entity.SystemLog;

public class SystemLogTester extends SpringJUnit4BaseTester {

	@Autowired
	AkkaCoreService akkaCoreService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(SystemLogTester.class);

	@Test
	public void systemLogMsgs() throws Exception {
		CountDownLatch cdl = new CountDownLatch(1);
		logger.info("systemLogMsgs");
		
		SystemLog msgs = new SystemLog();
		msgs.setAction("Test");
		msgs.setModifyTime(new Date());
		msgs.setModifyUser("Ted");
		msgs.setLevel(SystemLog.SYSTEM_LOG_LEVEL_TEST);
		
		akkaCoreService.recordMsgs(msgs);
		
		cdl.await();
	}
}
