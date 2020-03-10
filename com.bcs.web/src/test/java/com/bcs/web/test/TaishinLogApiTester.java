package com.bcs.web.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.taishin.akka.service.RichartAkkaService;
import com.bcs.core.taishin.api.model.LogApiModel;

import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/spring-base.xml", "classpath*:spring/spring-security.xml"})
public class RichartLogApiTester extends TestCase {
	private static Logger logger = Logger.getLogger(RichartLogApiTester.class);
	
	@Autowired
	private RichartAkkaService richartAkkaSerive;
    
	@Test
	public void test_Log_Api() throws Exception {		
		
			//controller 輸入資料
			AdminUser adminUser = new AdminUser();
			adminUser.setAccount("test_adminUser");
			
			//LogApi 物件
			LogApiModel logApiModel = new LogApiModel.LogApiModelBuilder()
				.functionType(LogApiModel.READ)
				.functionName("testGet")
				.clientIp("127.0.0.1")
				.data(null)
				.queryString("SELECT * FROM TEST")
				.sensitiveData("")
				.functionStatus(LogApiModel.SUCCESS)
				.build();
			
			//執行LOG API 
			richartAkkaSerive.excuteLogApi(logApiModel);
	}

}
