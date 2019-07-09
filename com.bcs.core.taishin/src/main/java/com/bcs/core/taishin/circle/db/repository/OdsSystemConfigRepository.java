package com.bcs.core.taishin.circle.db.repository;
import org.springframework.stereotype.Repository;
import com.bcs.core.taishin.circle.db.entity.OdsSystemConfig;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class OdsSystemConfigRepository {

	public void save(OdsSystemConfig odsSystemConfig){
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
			ps.setString(1, odsSystemConfig.getConfigId());
			ps.setString(2, odsSystemConfig.getDescription());
			ps.setString(3, odsSystemConfig.getValue());
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