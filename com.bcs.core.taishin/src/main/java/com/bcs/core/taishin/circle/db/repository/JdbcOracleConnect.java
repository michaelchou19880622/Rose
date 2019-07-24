package com.bcs.core.taishin.circle.db.repository;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.db.entity.EmployeeRecord;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;  

@Repository
public class JdbcOracleConnect {
	/** Logger */
	private static Logger logger = Logger.getLogger(JdbcOracleConnect.class);
		
	public static void main(String[] args) {
		System.out.println(getAvailableEmpIdsByEmpId("MOPACK"));
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
	
	public static String getAvailableEmpIdsByEmpId(String empId) {
		System.out.println("[getAvailableEmpIdsByEmpId] EMP_ID="+empId);
		if(StringUtils.isBlank(empId)) return "";
		
		try{
			String HR = "HR";
			Class.forName("oracle.jdbc.driver.OracleDriver");  
			Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/XEPDB1","SYSTEM","123");  

			// get Employee Data by Id
			String sqlString = "SELECT EMP_ID, DEPT_SER_NO_ACT, ACCT_DEPT_CD, ACCT_GRP_CD, CARD_DIV, CARD_DEPT, DEPT_EASY_NM FROM " +
					HR + ".HR_EMP_SW LEFT OUTER JOIN " + HR + ".HR_DEPT_SW " + 
					"ON (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO) " + 
					"WHERE EMP_ID = '" + empId + "'";
			Statement stmt=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);  
			ResultSet rs=stmt.executeQuery(sqlString);
			EmployeeRecord emp = new EmployeeRecord();
			while(rs.next()) {
				for(int i = 1; i <= 5; i++) {
					System.out.println(rs.getString(i)+"  "); 
				}
				emp.setEmployeeId(empId);
				emp.setDepartmentId(rs.getString(2));
				emp.setDivisionName(rs.getString(5));
				emp.setDepartmentName(rs.getString(6));
				emp.setGroupName(rs.getString(7));
				emp.setPccCode(rs.getString(3).trim() + rs.getString(4).trim());
			}
			System.out.println("emp:"+emp);
			
			// get List of EmpIds
			List<String> EmpIds = new ArrayList();
			sqlString = "SELECT EMP_ID FROM " +
					HR + ".HR_EMP_SW LEFT OUTER JOIN " + HR + ".HR_DEPT_SW " + 
					"ON (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO) ";
			
			if(StringUtils.isNotBlank(emp.getGroupName())) { // 組權限
				sqlString += "WHERE CARD_DIV = '" + emp.getDivisionName() + "' AND CARD_DEPT = '" + emp.getDepartmentName() + "' AND DEPT_EASY_NM = '" + emp.getGroupName() + "' ";
			}else if(StringUtils.isNotBlank(emp.getDepartmentName()))  { // 部權限
				sqlString += "WHERE CARD_DIV = '" + emp.getDivisionName() + "' AND CARD_DEPT = '" + emp.getDepartmentName() + "' ";
			}else { // 處權限
				sqlString += "WHERE CARD_DIV = '" + emp.getDivisionName() + "' ";
			}
			System.out.println("sqlString:"+sqlString);
			Statement stmt2=con.createStatement(); 
			ResultSet rs2=stmt2.executeQuery(sqlString);
			
			int  i = 0;
			while(rs2.next()) {
				System.out.println(++i);
				System.out.println("get:"+rs2.getString(1));
				EmpIds.add(rs2.getString(1));
			}
			System.out.println("EmpIds:"+EmpIds);
			
			// Merge to IN('', '') String
			String mergeStr = "AND EMPLOYEE_ID IN ('" + StringUtils.join(EmpIds, "', '") + "') ";
			con.close();
			return mergeStr;
		}catch(Exception e){
			System.out.println("[getAvailableEmpIdsByEmpId] error:" + e);
			return null;
		}
	}

	public static void getEmpDataByEmpId(String empId) {
	System.out.println("getEmpDataByEmpId. EMP_ID="+empId);
	try{
		//step1 load the driver class  
		Class.forName("oracle.jdbc.driver.OracleDriver");  

		//step2 create  the connection object  
		Connection con=DriverManager.getConnection("jdbc:oracle:thin:@192.168.1.195:1521/XEPDB1","SYSTEM","123");  //thin/oci

		//step3 create the statement object  
		Statement stmt=con.createStatement();  
		
		String HR = "HR";
		
		//step4 execute query
		String sqlString = "select * from " + HR + ".HR_EMP_SW LEFT OUTER JOIN " + HR + ".HR_DEPT_SW " + 
				"on (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO)" + 
				"where EMP_ID = '" + empId + "'";
		System.out.println(sqlString);
		ResultSet rs=stmt.executeQuery(sqlString);

		while(rs.next()) {
			for(int i = 1; i <= 8; i++) {
				System.out.println(rs.getString(i)+"  "); 
			}
		}
		//step5 close the connection object  
		con.close();  
	}catch(Exception e){ 
		System.out.println(e);
	}  	
}	
	
//	public static void findAll() {
//	System.out.println("findAll");
//	try{
//		//step1 load the driver class  
//		Class.forName("oracle.jdbc.driver.OracleDriver");  
//
//		//step2 create  the connection object  
//		Connection con=DriverManager.getConnection("jdbc:oracle:thin:@192.168.1.195:1521/XEPDB1","SYSTEM","123");  //thin/oci
//
//		//step3 create the statement object  
//		Statement stmt=con.createStatement();  
//		
//		String HR = "HR";
//	
//		
//		//step4 execute query
//		String sqlString = "select * from " + HR + ".HR_EMP_SW";
//		System.out.println(sqlString);
//		ResultSet rs=stmt.executeQuery(sqlString);
//
//		while(rs.next()) {
//			String s = "";
//			for(int i = 1; i <= 4; i++) {
//				s += rs.getString(i)+",";
//			}
//			System.out.println(s); 
//		}
//		//step5 close the connection object  
//		con.close();  
//	}catch(Exception e){ 
//		System.out.println(e);
//	}  	
//}	

//	
//	public static void getHrEmpSw(String EMP_ID) {
//		System.out.println("get HR_EMP_SW. EMP_ID="+EMP_ID);
//		try{
//			//step1 load the driver class  
//			Class.forName("oracle.jdbc.driver.OracleDriver");  
//
//			//step2 create  the connection object  
//			Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/XEPDB1","SYSTEM","123");  
//
//			//step3 create the statement object  
//			Statement stmt=con.createStatement();  
//			  
//			//step4 execute query  
//			//ResultSet rs=stmt.executeQuery("select * from  HR.JOBS");
//			ResultSet rs=stmt.executeQuery("select DEPT_SER_NO_ACT, ACCT_GRP_CD, ACCT_DEPT_CD from HR.HR_EMP_SW where EMP_ID = " + EMP_ID);
//
//			while(rs.next()) {
//				System.out.println(rs.getString(1)+"  "+rs.getString(2) + " "+ rs.getString(3)+"  ");  
//			}
//			//step5 close the connection object  
//			con.close();  
//		}catch(Exception e){ 
//			System.out.println(e);
//		}  	
//	}
	
//	public static void getHrEmpSw(String EMP_ID) {
//		System.out.println("get HR_EMP_SW. EMP_ID="+EMP_ID);
//		try{
//			String ORACLE_DATASOURCE_URL = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_URL, true);
//			String ORACLE_DATASOURCE_USERNAME = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_USERNAME, true);
//			String ORACLE_DATASOURCE_PASSWORD = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_PASSWORD, true);
//			String ORACLE_DATASOURCE_DRIVER_NAME = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_DRIVER_NAME, true);
//			String ORACLE_SCHEMA_HR_EMP_SW = CoreConfigReader.getString(CONFIG_STR.ORACLE_SCHEMA_HR_EMP_SW, true);
//			
//			//step1 load the driver class  
//			Class.forName(ORACLE_DATASOURCE_DRIVER_NAME);  
//
//			//step2 create  the connection object  
//			Connection con=DriverManager.getConnection(ORACLE_DATASOURCE_URL, ORACLE_DATASOURCE_USERNAME, ORACLE_DATASOURCE_PASSWORD);  
//
//			//step3 create the statement object  
//			Statement stmt=con.createStatement();  
//			  
//			//step4 execute query  
//			ResultSet rs=stmt.executeQuery("select DEPT_SER_NO_ACT, ACCT_GRP_CD, ACCT_DEPT_CD from " + 
//				ORACLE_SCHEMA_HR_EMP_SW + ".HR_EMP_SW where EMP_ID = " + EMP_ID);
//			
//			while(rs.next()) {
//				System.out.println(rs.getString(1)+"  "+rs.getString(2) + " "+ rs.getString(3)+"  ");  
//			}
//			//step5 close the connection object  
//			con.close();  
//		}catch(Exception e){ 
//			System.out.println(e);
//		}
//	}
	

	
//	public static void getHRJOBS() {
//		System.out.println("HR.JOBS SQL");
//		// TODO Auto-generated method stub
//		try{  
//			//step1 load the driver class  
//			Class.forName("oracle.jdbc.driver.OracleDriver");  
//
//			//step2 create  the connection object  
//			Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/XEPDB1","SYSTEM","123");  
//
//			//step3 create the statement object  
//			Statement stmt=con.createStatement();  
//			  
//			//step4 execute query  
//			//ResultSet rs=stmt.executeQuery("select * from  HR.JOBS");
//			ResultSet rs=stmt.executeQuery("select JOB_ID, JOB_TITLE, MIN_SALARY, MAX_SALARY from  HR.JOBS");
//
//			while(rs.next()) {
//				System.out.println(rs.getString(1)+"  "+rs.getString(2) + " "+ rs.getInt(3)+"  "+rs.getInt(4)+"  ");  
//			}
//			//step5 close the connection object  
//			con.close();  
//		}catch(Exception e){ 
//			System.out.println(e);
//		}  	
//	}
//	
	private static ComboPooledDataSource oracleDataSource = null;
	public void execute(String sql){
		Connection conn = null;
//		String sql = "INSERT INTO BCS_SYSTEM_CONFIG (CONFIG_ID, DESCRIPTION, MODIFY_TIME, VALUE) VALUES (?,?,GETDATE(),?)";
		
		try{
			//step1 load the driver class  
			Class.forName("oracle.jdbc.driver.OracleDriver");  

			//step2 create the connection object  
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XEPDB1","system","oracle");  
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.executeUpdate();
			ps.close();
		}catch(Exception e){
			System.out.println("JdbcConnect Error!");
			System.out.println(e.getMessage());
			throw new RuntimeException(e);			
		}finally{
			if (conn != null){
				try{
					conn.close();
				}catch(SQLException e){
					System.out.println("JdbcConnect Error!");
					System.out.println(e.getMessage());
				}
			}
		}
	}
}