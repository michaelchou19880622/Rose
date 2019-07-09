package com.bcs.core.taishin.jdbc.db.repository;

import org.springframework.stereotype.Repository;
import com.bcs.core.taishin.jdbc.db.component.SystemConfigJDBC;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class SystemConfigJDBCDAO {

	public void addMember(SystemConfigJDBC systemConfig){
		Connection conn = null;
		String sql = "INSERT INTO BCS_SYSTEM_CONFIG (CONFIG_ID, DESCRIPTION, MODIFY_TIME, VALUE) VALUES (?,?,GETDATE(),?)";
		
		try {

			SQLServerDataSource ds = new SQLServerDataSource();  
			ds.setUser("sa");  
			ds.setPassword("XtremeApp53811062");  
			ds.setServerName("172.104.113.123");  
			ds.setPortNumber(1433);
			ds.setDatabaseName("Taishin_Customer_Connect");
			conn = ds.getConnection();
			
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, "mopack1002");
			ps.setString(2, "mopack1002desc");
			ps.setString(3, "mopack1002value");
			ps.executeUpdate();
			ps.close();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
			
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}
	}
}