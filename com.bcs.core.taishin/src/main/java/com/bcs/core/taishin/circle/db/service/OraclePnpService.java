package com.bcs.core.taishin.circle.db.service;

import java.sql.Connection;
import java.sql.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.db.entity.EmployeeRecord;
import com.bcs.core.taishin.circle.PNP.db.repository.EmployeeRecordRepository;

@Service
public class OraclePnpService {
	/** Logger */
	private static Logger logger = Logger.getLogger(OraclePnpService.class);	
	@Autowired
	private EmployeeRecordRepository employeeRecordRepository;

	public void save(EmployeeRecord employeeRecord) {
		employeeRecordRepository.save(employeeRecord);
	}
	
	public void findAll(Integer maxRange) {
		logger.info("[findAll]");
		try{
			String ORACLE_DATASOURCE_URL = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_URL, true);
			String ORACLE_DATASOURCE_USERNAME = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_USERNAME, true);
			String ORACLE_DATASOURCE_PASSWORD = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_PASSWORD, true);
			String HR = CoreConfigReader.getString(CONFIG_STR.ORACLE_SCHEMA_HR, true);
			logger.info(ORACLE_DATASOURCE_URL);
			logger.info(ORACLE_DATASOURCE_USERNAME);
			logger.info(ORACLE_DATASOURCE_PASSWORD);
			logger.info(HR);
			
			//step1 load the driver class  
			Class.forName("oracle.jdbc.driver.OracleDriver");  

			//step2 create  the connection object  
			Connection con=DriverManager.getConnection(ORACLE_DATASOURCE_URL,ORACLE_DATASOURCE_USERNAME,ORACLE_DATASOURCE_PASSWORD);  

			//step3 create the statement object  
			Statement stmt=con.createStatement();  
			  
			//step4 execute query  
			String sqlString = "select * from " + HR + ".HR_EMP_SW";
			logger.info("sqlString:"+sqlString);
			
			ResultSet rs=stmt.executeQuery(sqlString);
			
			while(rs.next()) {
				String s = "";
				for(int i = 1; i <= maxRange; i++) {
					s += rs.getString(i)+",";
				}
				logger.info(s); 
			}
			
			//step5 close the connection object
			con.close(); 
		}catch(Exception e){
			logger.info("[findAll] error:" + e);
		}
	}
	public EmployeeRecord findByEmployeeId(String empId) {
		logger.info("[get HR_EMP_SW] EMP_ID="+empId);
		try{
			String ORACLE_DATASOURCE_URL = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_URL, true);
			String ORACLE_DATASOURCE_USERNAME = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_USERNAME, true);
			String ORACLE_DATASOURCE_PASSWORD = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_PASSWORD, true);
			String HR = CoreConfigReader.getString(CONFIG_STR.ORACLE_SCHEMA_HR, true);
			logger.info(ORACLE_DATASOURCE_URL);
			logger.info(ORACLE_DATASOURCE_USERNAME);
			logger.info(ORACLE_DATASOURCE_PASSWORD);
			logger.info(HR);
			
			//step1 load the driver class  
			Class.forName("oracle.jdbc.driver.OracleDriver");  

			//step2 create  the connection object  
			Connection con=DriverManager.getConnection(ORACLE_DATASOURCE_URL,ORACLE_DATASOURCE_USERNAME,ORACLE_DATASOURCE_PASSWORD);  

			//step3 create the statement object  
			Statement stmt=con.createStatement();  
			  
			//step4 execute query  
			String sqlString = "select * from " + HR + ".HR_EMP_SW LEFT OUTER JOIN " + HR + ".HR_DEPT_SW " + 
					"on (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO)" + 
					"where EMP_ID = '" + empId + "'";
			logger.info("sqlString:"+sqlString);
			
			ResultSet rs=stmt.executeQuery(sqlString);
			
			EmployeeRecord result = new EmployeeRecord();
			
			while(rs.next()) {
				for(int i = 1; i <= 8; i++) {
					logger.info(rs.getString(i)+"  "); 
				}
				result.setAccount(empId);
				result.setEmployeeId(empId);
				result.setDepartmentId(rs.getString(2));
				result.setDivisionName(rs.getString(6));
				result.setDepartmentName(rs.getString(7));
				result.setGroupName(rs.getString(8));
				result.setPccCode(rs.getString(3).trim() + rs.getString(4).trim());
			}
			
			//step5 close the connection object
			con.close(); 
			return result;
		}catch(Exception e){
			logger.info("[get HR_EMP_SW] error:" + e);
			return null;
		}
	}
}