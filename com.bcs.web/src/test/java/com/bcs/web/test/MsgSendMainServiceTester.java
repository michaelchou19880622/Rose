package com.bcs.web.test;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bcs.core.api.test.SpringJUnit4BaseTester;
import com.bcs.core.db.entity.MsgSendMain;
import com.bcs.core.db.service.MsgSendMainService;

public class MsgSendMainServiceTester extends SpringJUnit4BaseTester {

	@Autowired
	MsgSendMainService msgSendMainService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(MsgSendMainServiceTester.class);

	@Test
	public void increaseSendCountByMsgSendId() throws InterruptedException {
		CountDownLatch cdl = new CountDownLatch(1);
		
		MsgSendMain msgSendMain = new MsgSendMain();
		msgSendMain.setStatus(MsgSendMain.MESSAGE_STATUS_PROCESS);
		msgSendMain.setSendCount(0L);
		msgSendMain.setSendTotalCount(4L);
		msgSendMainService.save(msgSendMain);
		
		logger.info("increaseSendCountByMsgSendId: MsgSendId : " + msgSendMain.getMsgSendId());
		
		msgSendMainService.increaseSendCountByMsgSendId(msgSendMain.getMsgSendId(), 10L);
		
		for(int i = 0; i < 55; i++){
			msgSendMainService.increaseSendCountByMsgSendId(msgSendMain.getMsgSendId());
		}
		cdl.await();
	}
}
