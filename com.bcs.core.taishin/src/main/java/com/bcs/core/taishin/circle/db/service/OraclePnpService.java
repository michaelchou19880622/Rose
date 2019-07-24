package com.bcs.core.taishin.circle.db.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.db.entity.EmployeeRecord;
import com.bcs.core.taishin.circle.PNP.db.repository.EmployeeRecordRepository;
import org.apache.commons.lang3.StringUtils;

@Service
public class OraclePnpService {
	/** Logger */
	private static Logger logger = Logger.getLogger(OraclePnpService.class);	
	@Autowired
	private EmployeeRecordRepository employeeRecordRepository;

	public void save(EmployeeRecord employeeRecord) {
		employeeRecordRepository.save(employeeRecord);
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
			String sqlString = "select EMP_ID, DEPT_SER_NO_ACT, ACCT_DEPT_CD, ACCT_GRP_CD, CARD_DIV, CARD_DEPT, DEPT_EASY_NM from " +
					HR + ".HR_EMP_SW LEFT OUTER JOIN " + HR + ".HR_DEPT_SW " + 
					"on (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO)" + 
					"where EMP_ID = '" + empId + "'";
			logger.info("sqlString:"+sqlString);
			
			ResultSet rs=stmt.executeQuery(sqlString);
			
			EmployeeRecord result = new EmployeeRecord();
			
			while(rs.next()) {
				for(int i = 1; i <= 5; i++) {
					logger.info(rs.getString(i)+"  "); 
				}
				result.setEmployeeId(empId);
				result.setDepartmentId(rs.getString(2));
				
				result.setDivisionName(rs.getString(5));
				result.setDepartmentName(rs.getString(6));
				result.setGroupName(rs.getString(7));
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
	
	public String getAvailableEmpIdsByEmpId(String empId) {
		logger.info("[getAvailableEmpIdsByEmpId] EMP_ID="+empId);
		try{
			String ORACLE_DATASOURCE_URL = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_URL, true);
			String ORACLE_DATASOURCE_USERNAME = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_USERNAME, true);
			String ORACLE_DATASOURCE_PASSWORD = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_PASSWORD, true);
			String HR = CoreConfigReader.getString(CONFIG_STR.ORACLE_SCHEMA_HR, true);
			Class.forName("oracle.jdbc.driver.OracleDriver");  
			Connection con=DriverManager.getConnection(ORACLE_DATASOURCE_URL,ORACLE_DATASOURCE_USERNAME,ORACLE_DATASOURCE_PASSWORD);  

			// get Employee Data by Id
			String sqlString = "SELECT EMP_ID, DEPT_SER_NO_ACT, ACCT_DEPT_CD, ACCT_GRP_CD, CARD_DIV, CARD_DEPT, DEPT_EASY_NM FROM " +
					HR + ".HR_EMP_SW LEFT OUTER JOIN " + HR + ".HR_DEPT_SW " + 
					"ON (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO) " + 
					"WHERE EMP_ID = '" + empId + "'";
			Statement stmt=con.createStatement();  
			ResultSet rs=stmt.executeQuery(sqlString);
			EmployeeRecord emp = new EmployeeRecord();
			while(rs.next()) {
				for(int i = 1; i <= 5; i++) {
					logger.info(rs.getString(i)+"  "); 
				}
				emp.setEmployeeId(empId);
				emp.setDepartmentId(rs.getString(2));
				emp.setDivisionName(rs.getString(5));
				emp.setDepartmentName(rs.getString(6));
				emp.setGroupName(rs.getString(7));
				emp.setPccCode(rs.getString(3).trim() + rs.getString(4).trim());
			}
			logger.info("emp:"+emp);
			
			// get List of EmpIds
			List<String> EmpIds = new ArrayList();
			sqlString = "SELECT EMP_ID, DEPT_SER_NO_ACT, ACCT_DEPT_CD, ACCT_GRP_CD, CARD_DIV, CARD_DEPT, DEPT_EASY_NM FROM " +
					HR + ".HR_EMP_SW LEFT OUTER JOIN " + HR + ".HR_DEPT_SW " + 
					"ON (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO) ";
			if(StringUtils.isNotBlank(emp.getGroupName())) { // 組權限
				sqlString += "WHERE DEPT_EASY_NM = '" + emp.getGroupName() + "'";
			}else if(StringUtils.isNotBlank(emp.getDepartmentName()))  { // 部權限
				sqlString += "WHERE DEPT_EASY_NM = '" + emp.getDepartmentName() + "'";
			}else { // 處權限
				sqlString += "WHERE DEPT_EASY_NM = '" + emp.getDivisionName() + "'";
			}
			
			Statement stmt2=con.createStatement(); 
			ResultSet rs2=stmt2.executeQuery(sqlString);
			while(rs2.next()) {
				EmpIds.add(rs.getString(1));
			}
			
			// Merge to IN('', '') String
			String mergeStr = "IN ('" + StringUtils.join(EmpIds, "', '") + "') ";
			con.close();
			return mergeStr;
		}catch(Exception e){
			logger.info("[getAvailableEmpIdsByEmpId] error:" + e);
			return null;
		}
	}
	
//	public void findAll(Integer maxRange) {
//		logger.info("[findAll]");
//		try{
//			String ORACLE_DATASOURCE_URL = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_URL, true);
//			String ORACLE_DATASOURCE_USERNAME = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_USERNAME, true);
//			String ORACLE_DATASOURCE_PASSWORD = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_PASSWORD, true);
//			String HR = CoreConfigReader.getString(CONFIG_STR.ORACLE_SCHEMA_HR, true);
//			logger.info(ORACLE_DATASOURCE_URL);
//			logger.info(ORACLE_DATASOURCE_USERNAME);
//			logger.info(ORACLE_DATASOURCE_PASSWORD);
//			logger.info(HR);
//			
//			//step1 load the driver class  
//			Class.forName("oracle.jdbc.driver.OracleDriver");  
//
//			//step2 create  the connection object  
//			Connection con=DriverManager.getConnection(ORACLE_DATASOURCE_URL,ORACLE_DATASOURCE_USERNAME,ORACLE_DATASOURCE_PASSWORD);  
//
//			//step3 create the statement object  
//			Statement stmt=con.createStatement();  
//			  
//			//step4 execute query  
//			String sqlString = "select * from " + HR + ".HR_EMP_SW";
//			logger.info("sqlString:"+sqlString);
//			
//			ResultSet rs=stmt.executeQuery(sqlString);
//			
//			while(rs.next()) {
//				String s = "";
//				for(int i = 1; i <= maxRange; i++) {
//					s += rs.getString(i)+",";
//				}
//				logger.info(s); 
//			}
//			
//			//step5 close the connection object
//			con.close(); 
//		}catch(Exception e){
//			logger.info("[findAll] error:" + e);
//		}
//	}

}