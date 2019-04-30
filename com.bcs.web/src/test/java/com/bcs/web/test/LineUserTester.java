package com.bcs.web.test;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bcs.core.api.test.SpringJUnit4BaseTester;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.service.LineUserService;

public class LineUserTester extends SpringJUnit4BaseTester {

	@Autowired
	LineUserService lineUserService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(LineUserTester.class);

	@Test
	public void createTestLine() throws Exception {
		logger.info("createTestLine");
		for(int i = 0; i < 200000; i++){
			LineUser user = new LineUser();
			user.setMid(i+"");
			lineUserService.save(user);
		}
	}
}
