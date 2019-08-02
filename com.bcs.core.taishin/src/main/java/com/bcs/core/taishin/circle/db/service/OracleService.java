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
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;
import com.bcs.core.taishin.circle.db.repository.TaishinEmployeeRepository;
import com.bcs.core.utils.ErrorRecord;
import com.tsib.RunBat;

import org.apache.commons.lang3.StringUtils;

@Service
public class OracleService {
	/** Logger */
	private static Logger logger = Logger.getLogger(OracleService.class);	
	@Autowired
	private TaishinEmployeeRepository taishinEmployeeRepository;

	public void save(TaishinEmployee employeeRecord) {
		taishinEmployeeRepository.save(employeeRecord);
	}
	

	
	public TaishinEmployee findByEmployeeId(String empId) {
		logger.info("[findByEmployeeId] EMP_ID="+empId);
		try{
			String ldapHost = CoreConfigReader.getString("oracleLdapHost");
			String apName = CoreConfigReader.getString("oracleApName");
			Integer apGroup = CoreConfigReader.getInteger("oracleApGroup");
			String searchBase = CoreConfigReader.getString("oracleSearchBase");
			String connection = getDBConnectionInfo(ldapHost, apName, apGroup, searchBase);
			logger.info("connection:" + connection);
			
			String[] split = connection.split(";");
			String USER = "";
			String PASSWORD = "";
			String DATABASENAME = "";
			for(String str : split){
				if(StringUtils.isNotBlank(str)){
					String[] keyvalue = str.split("=");
				
					if(keyvalue != null && keyvalue.length == 2){
						if("uid".equals(keyvalue[0])){
							USER = keyvalue[1];
						}
						if("pwd".equals(keyvalue[0])){
							PASSWORD = keyvalue[1];
						}
						if("database".equals(keyvalue[0])){
							DATABASENAME = keyvalue[1];
						}
					}
				}
			}
			USER = USER.toUpperCase();
			PASSWORD = PASSWORD.toUpperCase();
			DATABASENAME = DATABASENAME.toUpperCase();
			logger.info("USER:"+USER);
			logger.info("PASSWORD:"+PASSWORD);

			String HR = CoreConfigReader.getString(CONFIG_STR.ORACLE_SCHEMA_HR, true);
			logger.info("HR:"+HR);
			
			
			String oracleUrl = CoreConfigReader.getString("oracleUrl");
			logger.info("oracleUrl:"+oracleUrl);
			
			String ORACLE_DATASOURCE_URL = "jdbc:oracle:thin:@"+oracleUrl+":1521/" + DATABASENAME;
			logger.info("ORACLE_DATASOURCE_URL:"+ORACLE_DATASOURCE_URL);
			
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con=DriverManager.getConnection(ORACLE_DATASOURCE_URL,USER,PASSWORD);
			
			Statement stmt=con.createStatement();  			  
			String sqlString = "select EMP_ID, DEPT_SER_NO_ACT, ACCT_DEPT_CD, ACCT_GRP_CD, CARD_DIV, CARD_DEPT, DEPT_EASY_NM from " +
					HR + ".HR_EMP_SW LEFT OUTER JOIN " + HR + ".HR_DEPT_SW " + 
					"on (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO)" + 
					"where EMP_ID = '" + empId + "'";
			logger.info("sqlString:"+sqlString);
			
			TaishinEmployee emp = new TaishinEmployee();
			ResultSet rs=stmt.executeQuery(sqlString);
			while(rs.next()) {
				for(int i = 1; i <= 7; i++) {
					logger.info("[findByEmployeeId] i="+ i + ", s=" + rs.getString(i)); 
				}
				emp.setEmployeeId(empId);
				emp.setDepartmentId(trim(rs.getString(2)));
				emp.setPccCode(trim(rs.getString(3)) + trim(rs.getString(4)));
				emp.setDivisionName(trim(rs.getString(5)));
				emp.setDepartmentName(trim(rs.getString(6)));
				emp.setEasyName(trim(rs.getString(7)));
				emp.setGroupName(extractGroupName(emp));
			}
			logger.info("[findByEmployeeId] emp:"+emp);
			
			con.close(); 
			return emp;
		}catch(Exception e){
			logger.info("[findByEmployeeId] error:" + e);
			return null;
		}
	}
	
	public String getAvailableEmpIdsByEmpId(String empId) {
		logger.info("[getAvailableEmpIdsByEmpId] EMP_ID="+empId);
		if(StringUtils.isBlank(empId)) return "";
		
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
			logger.info("sqlString:"+sqlString);
			
			Statement stmt=con.createStatement();  
			ResultSet rs=stmt.executeQuery(sqlString);
			TaishinEmployee emp = new TaishinEmployee();
			while(rs.next()) {
				for(int i = 1; i <= 7; i++) {
					logger.info("[findByEmployeeId] i="+ i + ", s=" + rs.getString(i)); 
				}
				emp.setEmployeeId(empId);
				emp.setDepartmentId(trim(rs.getString(2)));
				emp.setPccCode(trim(rs.getString(3)) + trim(rs.getString(4)));
				emp.setDivisionName(trim(rs.getString(5)));
				emp.setDepartmentName(trim(rs.getString(6)));
				emp.setEasyName(trim(rs.getString(7)));
				emp.setGroupName(extractGroupName(emp));
			}
			logger.info("[findByEmployeeId] emp:"+emp);
			
			// get List of EmpIds
			List<String> EmpIds = new ArrayList();
			sqlString = "SELECT EMP_ID FROM " +
					HR + ".HR_EMP_SW LEFT OUTER JOIN " + HR + ".HR_DEPT_SW " + 
					"ON (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO) ";
			if(StringUtils.isNotBlank(emp.getGroupName())) { // 組權限
				sqlString += "WHERE TRIM(CARD_DIV) = '" + emp.getDivisionName() + "' "
					+ "AND TRIM(CARD_DEPT) = '" + emp.getDepartmentName() + "' "
					+ "AND DEPT_EASY_NM LIKE '%" + emp.getGroupName() + "%' ";
			}else if(StringUtils.isNotBlank(emp.getDepartmentName()))  { // 部權限
				sqlString += "WHERE TRIM(CARD_DIV) = '" + emp.getDivisionName() + "' "
					+ "AND TRIM(CARD_DEPT) = '" + emp.getDepartmentName() + "' ";
			}else { // 處權限
				sqlString += "WHERE TRIM(CARD_DIV) = '" + emp.getDivisionName() + "' ";
			}
			logger.info("sqlString2:"+sqlString);
			
			Statement stmt2=con.createStatement(); 
			ResultSet rs2=stmt2.executeQuery(sqlString);
			while(rs2.next()) {
				EmpIds.add(rs2.getString(1));
			}
			logger.info("EmpIds:"+EmpIds);
			
			// Merge to IN('', '') String
			String mergeStr = "AND EMPLOYEE_ID IN ('" + StringUtils.join(EmpIds, "', '") + "') ";
			logger.info("mergeStr:"+mergeStr);
			con.close();
			return mergeStr;
		}catch(Exception e){
			logger.info("[getAvailableEmpIdsByEmpId] error:" + e);
			return null;
		}
	}
	
	
	// ---- Tools ----
	public static String extractGroupName(TaishinEmployee emp) {
		String s = emp.getEasyName();
		s = s.replaceAll(emp.getDivisionName(), "");	// cut 處
		s = s.replaceAll(emp.getDepartmentName(), "");	// cut 部
		logger.info("extractGroupName:" + s);
		return s;
	}
	
	public static String trim(String s) {
		if(StringUtils.isBlank(s)) return "";
		return s.trim();
	}
	
	private String getDBConnectionInfo(String ldapHost, String apName, int apGroup, String searchBase) {
		try {
			RunBat ap1 = new RunBat();
			ap1.SSL = false;
			ap1.ldapHost = ldapHost;
			ap1.searchBase = searchBase;
			return ap1.GetRunBat(apName, apGroup);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		return "";
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