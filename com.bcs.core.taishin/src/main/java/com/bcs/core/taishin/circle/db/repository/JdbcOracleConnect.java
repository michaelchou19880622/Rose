package com.bcs.core.taishin.circle.db.repository;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class JdbcOracleConnect {
	/** Logger */
	private static Logger logger = Logger.getLogger(JdbcOracleConnect.class);
	
	private static ComboPooledDataSource oracleDataSource = null;
	public void execute(String sql){
		Connection conn = null;
//		String sql = "INSERT INTO BCS_SYSTEM_CONFIG (CONFIG_ID, DESCRIPTION, MODIFY_TIME, VALUE) VALUES (?,?,GETDATE(),?)";
		
		try{
			SQLServerDataSource ds = new SQLServerDataSource();
			ds.setUser("sa");  
			ds.setPassword("XtremeApp53811062");  
			ds.setServerName("172.104.113.123");  
			ds.setPortNumber(1433);
			ds.setDatabaseName("Taishin_Customer_Connect");
			conn = ds.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.executeUpdate();
			ps.close();
		}catch(SQLException e){
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