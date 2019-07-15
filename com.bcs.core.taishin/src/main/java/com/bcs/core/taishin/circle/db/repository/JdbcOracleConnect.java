package com.bcs.core.taishin.circle.db.repository;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.*;  

@Repository
public class JdbcOracleConnect {
	/** Logger */
	private static Logger logger = Logger.getLogger(JdbcOracleConnect.class);
		
//	public static void main(String[] args) {
//		getEmpDataByEmpId("1000588");
//		//getEmpDataByEmpId("MOPACK");
//	}
//	public static void getEmpDataByEmpId(String empId) {
//		System.out.println("getEmpDataByEmpId. EMP_ID="+empId);
//		try{
//			//step1 load the driver class  
//			Class.forName("oracle.jdbc.driver.OracleDriver");  
//	
//			//step2 create  the connection object  
//			Connection con=DriverManager.getConnection("jdbc:oracle:thin:@192.168.1.195:1521/XEPDB1","SYSTEM","123");  //thin/oci
//	
//			//step3 create the statement object  
//			Statement stmt=con.createStatement();  
//			
//			String HR = "HR";
//			
//			//step4 execute query
//			String sqlString = "select * from " + HR + ".HR_EMP_SW LEFT OUTER JOIN " + HR + ".HR_DEPT_SW " + 
//					"on (HR_EMP_SW.DEPT_SER_NO_ACT = HR_DEPT_SW.DEPT_SERIAL_NO)" + 
//					"where EMP_ID = '" + empId + "'";
//			System.out.println(sqlString);
//			ResultSet rs=stmt.executeQuery(sqlString);
//	
//			while(rs.next()) {
//				for(int i = 1; i <= 8; i++) {
//					System.out.println(rs.getString(i)+"  "); 
//				}
//			}
//			//step5 close the connection object  
//			con.close();  
//		}catch(Exception e){ 
//			System.out.println(e);
//		}  	
//	}	
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
			logger.info("JdbcConnect Error!");
			logger.info(e.getMessage());
			throw new RuntimeException(e);			
		}finally{
			if (conn != null){
				try{
					conn.close();
				}catch(SQLException e){
					logger.info("JdbcConnect Error!");
					logger.info(e.getMessage());
				}
			}
		}
	}
}