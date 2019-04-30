package com.bcs.core.db.connection;

import java.util.HashMap;
import java.util.Properties;

import net.sourceforge.jtds.jdbc.Driver;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.tsib.RunBat;
import com.zaxxer.hikari.HikariConfig;

public class BcsHikariConfig extends HikariConfig {

	/** Logger */
	private static Logger logger = Logger.getLogger(BcsHikariConfig.class);

	HashMap<String, String> config = new HashMap<String, String>();
	
	public BcsHikariConfig() {
		super();
		logger.info("BcsHikariConfig");
		settingHashMap();
	}

	public BcsHikariConfig(Properties properties) {
		super();
		logger.info("BcsHikariConfig:properties");
		settingHashMap();
	}

	public BcsHikariConfig(String propertyFileName) {
		super();
		logger.info("BcsHikariConfig:propertyFileName");
		settingHashMap();
	}
	
	private void settingHashMap(){

		try {
			String ldapHost = CoreConfigReader.getString("ldapHost");
			String apName = CoreConfigReader.getString("apName");
			Integer apGroup = CoreConfigReader.getInteger("apGroup");
			String searchBase = CoreConfigReader.getString("searchBase");
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
			
			if(StringUtils.isNotBlank(USER)){
				config.put( Driver.USER, USER);
			}
			if(StringUtils.isNotBlank(PASSWORD)){
				config.put( Driver.PASSWORD, PASSWORD);
			}
			if(StringUtils.isNotBlank(DATABASENAME)){
				config.put( Driver.DATABASENAME, DATABASENAME);
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
   public void setPassword(String password)
   {
		logger.info("setPassword");
		   super.setPassword(password);
   }
	
   public void setUsername(String username)
   {
		logger.info("setUsername");
		   super.setUsername(username);
   }

   public void addDataSourceProperty(String propertyName, Object value)
   {
		logger.info("addDataSourceProperty");
	   super.addDataSourceProperty(propertyName, value);
   }

   public void setDataSourceProperties(Properties dsProperties)
   {
		logger.info("setDataSourceProperties");
		
		String USER = config.get(Driver.USER);
		if(StringUtils.isNotBlank(USER)){
			dsProperties.put("user", USER);
		}
		String PASSWORD = config.get(Driver.PASSWORD);
		if(StringUtils.isNotBlank(PASSWORD)){
			dsProperties.put("password", PASSWORD);
		}
		String DATABASENAME = config.get(Driver.DATABASENAME);
		if(StringUtils.isNotBlank(DATABASENAME)){
			dsProperties.put("databaseName", DATABASENAME);
		}
		
	   super.setDataSourceProperties(dsProperties);
   }

	private String getDBConnectionInfo(String ldapHost, String apName,
			int apGroup, String searchBase) {
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
}
