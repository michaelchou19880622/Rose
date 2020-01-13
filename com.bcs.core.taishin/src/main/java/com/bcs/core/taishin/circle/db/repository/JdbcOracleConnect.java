package com.bcs.core.taishin.circle.db.repository;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Repository;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/* This is for Oracle Local Testing */
@Repository
public class JdbcOracleConnect {

//	public static void main(String[] args) {
//		csvToXlsxImply();
//		//System.out.println("123");
//	}

	public static void csvToXlsxImply() {
        try {
        	String csvFileAddress = "C:\\bcs\\myUid.csv";
        	File fe = new File(csvFileAddress);
        	InputStream is = new FileInputStream(fe);
        	Workbook workBook = new XSSFWorkbook();
        	csvToXlsx(is, workBook);

	        String xlsxFileAddress = "C:\\bcs\\test345.xlsx"; //xlsx file address
	        FileOutputStream fileOutputStream =  new FileOutputStream(xlsxFileAddress);
	        workBook.write(fileOutputStream);
	        fileOutputStream.close();
	        System.out.println("Done");
        }catch(Exception e){
        	System.out.println(e);
        }
	}

	public static void csvToXlsx(InputStream is, Workbook workBook) {
	    try {
	    	Sheet sheet = workBook.createSheet("sheet1");
	        String currentLine=null;
	        int RowNum=0;

	        InputStreamReader isr = new InputStreamReader(is);
	        BufferedReader br = new BufferedReader(isr);
	        while ((currentLine = br.readLine()) != null) {
	            String str[] = currentLine.split(",");
	            RowNum++;
	            Row currentRow=sheet.createRow(RowNum);
	            for(int i=0;i<str.length;i++){
	                currentRow.createCell(i).setCellValue(str[i]);
	            }
	        }
	    } catch (Exception ex) {
	        System.out.println(ex.getMessage()+"Exception in try");
	    }
	}

	//	public static void csvToXLSX() {
	//    try {
	//        String csvFileAddress = "C:\\bcs\\myUid.csv"; //csv file address
	//        String xlsxFileAddress = "C:\\bcs\\test345.xlsx"; //xlsx file address
	//        XSSFWorkbook workBook = new XSSFWorkbook();
	//        XSSFSheet sheet = workBook.createSheet("sheet1");
	//        String currentLine=null;
	//        int RowNum=0;
	//        //BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	//        BufferedReader br = new BufferedReader(new FileReader(csvFileAddress));
	//        while ((currentLine = br.readLine()) != null) {
	//            String str[] = currentLine.split(",");
	//            RowNum++;
	//            XSSFRow currentRow=sheet.createRow(RowNum);
	//            for(int i=0;i<str.length;i++){
	//                currentRow.createCell(i).setCellValue(str[i]);
	//            }
	//        }
	//
	//        FileOutputStream fileOutputStream =  new FileOutputStream(xlsxFileAddress);
	//        workBook.write(fileOutputStream);
	//        fileOutputStream.close();
	//        System.out.println("Done");
	//    } catch (Exception ex) {
	//        System.out.println(ex.getMessage()+"Exception in try");
	//    }
	//}

	//public static void main(String[] args) {
	//	//System.out.println(findByEmployeeId("MOPACK"));
	//	System.out.println(findByEmployeeId("123"));
	//	//System.out.println(getAvailableEmpIdsByEmpId("123"));
	//	//System.out.println(getAvailableEmpIdsByEmpId("MOPACK"));
	//}
	public static TaishinEmployee findByEmployeeId(String empId) {
		System.out.println("[get HR_EMP_SW] EMP_ID="+empId);
		try{
			// connect to database
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection con=DriverManager.getConnection("jdbc:oracle:thin:@LOCALHOST:1521/XEPDB1","SYSTEM","123");


			// get data from table
			String HR = "HR";
			String sqlString = "select EMP_ID, DEPT_SER_NO_ACT, ACCT_DEPT_CD, ACCT_GRP_CD, CARD_DIV, CARD_DEPT, DEPT_EASY_NM from " +
					HR + ".HR_EMP LEFT OUTER JOIN " + HR + ".HR_DEPT " +
					"on (HR_EMP.DEPT_SER_NO_ACT = HR_DEPT.DEPT_SERIAL_NO) " +
					"where EMP_ID = '" + empId + "'";
			System.out.println("sqlString:"+sqlString);

			Statement stmt=con.createStatement();
			ResultSet rs=stmt.executeQuery(sqlString);

			TaishinEmployee emp = new TaishinEmployee();
			while(rs.next()) {
				for(int i = 1; i <= 7; i++) {
					System.out.println("[findByEmployeeId] i="+ i + ", s=" + rs.getString(i));
				}
				emp.setEmployeeId(empId);
				emp.setDepartmentId(trim(rs.getString(2)));
				emp.setPccCode(trim(rs.getString(3)) + trim(rs.getString(4)));
				emp.setDivisionName(trim(rs.getString(5)));
				emp.setDepartmentName(trim(rs.getString(6)));
				emp.setEasyName(trim(rs.getString(7)));
				emp.setGroupName(extractGroupName(emp));
			}
			System.out.println("emp:"+emp);

			//step5 close the connection object
			con.close();
			return emp;
		}catch(Exception e){
			System.out.println("[get HR_EMP_SW] error:" + e);
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
					HR + ".HR_EMP LEFT OUTER JOIN " + HR + ".HR_DEPT " +
					"ON (HR_EMP.DEPT_SER_NO_ACT = HR_DEPT.DEPT_SERIAL_NO) " +
					"WHERE EMP_ID = '" + empId + "'";
			Statement stmt=con.createStatement();
			ResultSet rs=stmt.executeQuery(sqlString);
			TaishinEmployee emp = new TaishinEmployee();
			while(rs.next()) {
				for(int i = 1; i <= 7; i++) {
					System.out.println(rs.getString(i)+"  ");
				}
				emp.setEmployeeId(empId);
				emp.setDepartmentId(trim(rs.getString(2)));
				emp.setPccCode(trim(rs.getString(3)) + trim(rs.getString(4)));
				emp.setDivisionName(trim(rs.getString(5)));
				emp.setDepartmentName(trim(rs.getString(6)));
				emp.setEasyName(trim(rs.getString(7)));
				emp.setGroupName(extractGroupName(emp));

			}
			System.out.println("emp:"+emp);

			// get List of EmpIds
			List<String> EmpIds = new ArrayList();
			sqlString = "SELECT EMP_ID FROM " +
					HR + ".HR_EMP LEFT OUTER JOIN " + HR + ".HR_DEPT " +
					"ON (HR_EMP.DEPT_SER_NO_ACT = HR_DEPT.DEPT_SERIAL_NO) ";

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
			String sqlString = "select * from " + HR + ".HR_EMP LEFT OUTER JOIN " + HR + ".HR_DEPT " +
					"on (HR_EMP.DEPT_SER_NO_ACT = HR_DEPT.DEPT_SERIAL_NO)" +
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
	public static String trim(String s) {
		if(StringUtils.isBlank(s)) return "";
		return s.trim();
	}
	public static String extractGroupName(TaishinEmployee emp) {
		String s = emp.getEasyName();
		s = s.replaceAll(emp.getDivisionName(), "");	// cut 處
		s = s.replaceAll(emp.getDepartmentName(), "");	// cut 部
		System.out.println("extractGroupName:" + s);
		return s;
	}


	//public static void findAll() {
	//System.out.println("findAll");
	//try{
	//	//step1 load the driver class
	//	Class.forName("oracle.jdbc.driver.OracleDriver");
	//
	//	//step2 create  the connection object
	//	Connection con=DriverManager.getConnection("jdbc:oracle:thin:@192.168.1.195:1521/XEPDB1","SYSTEM","123");  //thin/oci
	//
	//	//step3 create the statement object
	//	Statement stmt=con.createStatement();
	//
	//	String HR = "HR";
	//
	//
	//	//step4 execute query
	//	String sqlString = "select * from " + HR + ".HR_EMP_SW";
	//	System.out.println(sqlString);
	//	ResultSet rs=stmt.executeQuery(sqlString);
	//
	//	while(rs.next()) {
	//		String s = "";
	//		for(int i = 1; i <= 4; i++) {
	//			s += rs.getString(i)+",";
	//		}
	//		System.out.println(s);
	//	}
	//	//step5 close the connection object
	//	con.close();
	//}catch(Exception e){
	//	System.out.println(e);
	//}
	//}

	//
	//public static void getHrEmpSw(String EMP_ID) {
	//	System.out.println("get HR_EMP_SW. EMP_ID="+EMP_ID);
	//	try{
	//		//step1 load the driver class
	//		Class.forName("oracle.jdbc.driver.OracleDriver");
	//
	//		//step2 create  the connection object
	//		Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/XEPDB1","SYSTEM","123");
	//
	//		//step3 create the statement object
	//		Statement stmt=con.createStatement();
	//
	//		//step4 execute query
	//		//ResultSet rs=stmt.executeQuery("select * from  HR.JOBS");
	//		ResultSet rs=stmt.executeQuery("select DEPT_SER_NO_ACT, ACCT_GRP_CD, ACCT_DEPT_CD from HR.HR_EMP_SW where EMP_ID = " + EMP_ID);
	//
	//		while(rs.next()) {
	//			System.out.println(rs.getString(1)+"  "+rs.getString(2) + " "+ rs.getString(3)+"  ");
	//		}
	//		//step5 close the connection object
	//		con.close();
	//	}catch(Exception e){
	//		System.out.println(e);
	//	}
	//}

	//public static void getHrEmpSw(String EMP_ID) {
	//	System.out.println("get HR_EMP_SW. EMP_ID="+EMP_ID);
	//	try{
	//		String ORACLE_DATASOURCE_URL = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_URL, true);
	//		String ORACLE_DATASOURCE_USERNAME = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_USERNAME, true);
	//		String ORACLE_DATASOURCE_PASSWORD = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_PASSWORD, true);
	//		String ORACLE_DATASOURCE_DRIVER_NAME = CoreConfigReader.getString(CONFIG_STR.ORACLE_DATASOURCE_DRIVER_NAME, true);
	//		String ORACLE_SCHEMA_HR_EMP_SW = CoreConfigReader.getString(CONFIG_STR.ORACLE_SCHEMA_HR_EMP_SW, true);
	//
	//		//step1 load the driver class
	//		Class.forName(ORACLE_DATASOURCE_DRIVER_NAME);
	//
	//		//step2 create  the connection object
	//		Connection con=DriverManager.getConnection(ORACLE_DATASOURCE_URL, ORACLE_DATASOURCE_USERNAME, ORACLE_DATASOURCE_PASSWORD);
	//
	//		//step3 create the statement object
	//		Statement stmt=con.createStatement();
	//
	//		//step4 execute query
	//		ResultSet rs=stmt.executeQuery("select DEPT_SER_NO_ACT, ACCT_GRP_CD, ACCT_DEPT_CD from " +
	//			ORACLE_SCHEMA_HR_EMP_SW + ".HR_EMP_SW where EMP_ID = " + EMP_ID);
	//
	//		while(rs.next()) {
	//			System.out.println(rs.getString(1)+"  "+rs.getString(2) + " "+ rs.getString(3)+"  ");
	//		}
	//		//step5 close the connection object
	//		con.close();
	//	}catch(Exception e){
	//		System.out.println(e);
	//	}
	//}
	//public static void getHRJOBS() {
	//	System.out.println("HR.JOBS SQL");
	//	// TODO Auto-generated method stub
	//	try{
	//		//step1 load the driver class
	//		Class.forName("oracle.jdbc.driver.OracleDriver");
	//
	//		//step2 create  the connection object
	//		Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/XEPDB1","SYSTEM","123");
	//
	//		//step3 create the statement object
	//		Statement stmt=con.createStatement();
	//
	//		//step4 execute query
	//		//ResultSet rs=stmt.executeQuery("select * from  HR.JOBS");
	//		ResultSet rs=stmt.executeQuery("select JOB_ID, JOB_TITLE, MIN_SALARY, MAX_SALARY from  HR.JOBS");
	//
	//		while(rs.next()) {
	//			System.out.println(rs.getString(1)+"  "+rs.getString(2) + " "+ rs.getInt(3)+"  "+rs.getInt(4)+"  ");
	//		}
	//		//step5 close the connection object
	//		con.close();
	//	}catch(Exception e){
	//		System.out.println(e);
	//	}
	//}
	//
	//private static ComboPooledDataSource oracleDataSource = null;
	//public void execute(String sql){
	//	Connection conn = null;
	////	String sql = "INSERT INTO BCS_SYSTEM_CONFIG (CONFIG_ID, DESCRIPTION, MODIFY_TIME, VALUE) VALUES (?,?,GETDATE(),?)";
	//
	//	try{
	//		//step1 load the driver class
	//		Class.forName("oracle.jdbc.driver.OracleDriver");
	//
	//		//step2 create the connection object
	//		conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XEPDB1","system","oracle");
	//
	//		PreparedStatement ps = conn.prepareStatement(sql);
	//		ps.executeUpdate();
	//		ps.close();
	//	}catch(Exception e){
	//		System.out.println("JdbcConnect Error!");
	//		System.out.println(e.getMessage());
	//		throw new RuntimeException(e);
	//	}finally{
	//		if (conn != null){
	//			try{
	//				conn.close();
	//			}catch(SQLException e){
	//				System.out.println("JdbcConnect Error!");
	//				System.out.println(e.getMessage());
	//			}
	//		}
	//	}
	//}
}