package com.bcs.core.utils;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.SystemConfig;
import com.bcs.core.db.service.SystemConfigService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;

@Service
public class DataSyncUtil {
	public static final String REGISTER_SYNC = "REGISTER_SYNC";
	public static final String REGISTER_MAIN = "REGISTER_MAIN";
	
	/** Logger */
	private static Logger logger = Logger.getLogger(DataSyncUtil.class);
	
	private Timer flushTimer = new Timer();
	
	private class CustomTask extends TimerTask{
        
        @Override
        public void run() {

            try{
                registerServer();
            }
            catch(Throwable e){
                logger.error(ErrorRecord.recordError(e));
            }
        }
    }
	
	public DataSyncUtil() {
	    flushTimer.schedule(new CustomTask(), 120000, 300000);
	}
	
	public static Boolean checkIp(String targetIp){
		try{
			InetAddress address = InetAddress.getLocalHost();
			String thisIp = address.getHostAddress();
			
			if(StringUtils.isBlank(targetIp) || targetIp.equals(thisIp)){
				return true;
			}
		}
		catch(Throwable e){
			logger.error(ErrorRecord.recordError(e));
		}
		return false;
	}
	
	public static String thisServerIp(){

		try{
			InetAddress address = InetAddress.getLocalHost();
			String ip = address.getHostAddress();

	    	String systemId = CoreConfigReader.getString(CONFIG_STR.SYSTEM_ID);
	    	ip += systemId;
			logger.debug("thisServerIp:" + ip);
			return ip;
		}
		catch(Throwable e){
			logger.error(ErrorRecord.recordError(e));
		}
		
		return "-";
	}

	public static Boolean isReSyncData(String input){
		try{
			InetAddress address = InetAddress.getLocalHost();
			String ip = address.getHostAddress();

	    	String systemId = CoreConfigReader.getString(CONFIG_STR.SYSTEM_ID);
	    	ip += systemId;
	    	
			String isReSyncData = CoreConfigReader.getString(ip, input, true, false);
			logger.debug("isReSyncData:" + ip + "." + input);
			return Boolean.parseBoolean(isReSyncData);
		}
		catch(Throwable e){
			logger.error(ErrorRecord.recordError(e));
		}
		return true;
	}

	public static void syncDataFinish(String input){
		try{
			InetAddress address = InetAddress.getLocalHost();
			String ip = address.getHostAddress();

	    	String systemId = CoreConfigReader.getString(CONFIG_STR.SYSTEM_ID);
	    	ip += systemId;

			SystemConfig config = new SystemConfig();
			config.setConfigId(ip + "." + input);
			config.setValue("false");
			config.setModifyTime(new Date());
			
			ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class).save(config);
			logger.debug("syncDataFinish:" + ip + "." + input);
		}
		catch(Throwable e){
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	public static void registerServer(){

		try{
			InetAddress address = InetAddress.getLocalHost();
			String ip = address.getHostAddress();

	    	String systemId = CoreConfigReader.getString(CONFIG_STR.SYSTEM_ID);
			
			SystemConfig config = new SystemConfig();
			config.setConfigId(ip + systemId + "." + REGISTER_SYNC);
			config.setValue(ip + "," + systemId);
			config.setModifyTime(new Date());
			
			ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class).save(config);
			logger.debug("registerServer:" + ip);
		}
		catch(Throwable e){
			logger.error(ErrorRecord.recordError(e));
		}

		try{
			InetAddress address = InetAddress.getLocalHost();
			String ip = address.getHostAddress();

	    	String systemId = CoreConfigReader.getString(CONFIG_STR.SYSTEM_ID);
			
			SystemConfig config = new SystemConfig();
			config.setConfigId(ip + systemId + "." + REGISTER_MAIN);
			config.setValue( ip + "," + CoreConfigReader.isMainSystem());
			config.setModifyTime(new Date());
			
			ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class).save(config);
			logger.debug("registerServer:" + ip);
		}
		catch(Throwable e){
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	public static String getMainIp(){

		try{
			List<Object[]> servers = ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class).findLikeConfigId("%." + REGISTER_MAIN);
			if(servers != null && servers.size() > 0){
			    
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			    
			    Calendar c = Calendar.getInstance();
			    c.setTime(new Date());
			    c.add(Calendar.MINUTE, -10);
			    Date standardTime = c.getTime();
			    
				for(Object[] server : servers){
					
					String value = (String)server[1];
					Date modifyTime = server[2] == null? null : SQLDateFormatUtil.formatSqlStringToDate(server[2], sdf);
					
					if(StringUtils.isNotBlank(value) && value.indexOf("true") > 0 && (modifyTime == null || modifyTime.after(standardTime))){
						String[] split = value.split(",");
						if(split != null && split.length == 2){
							return split[0];
						}
					}
				}
			}
		}
		catch(Throwable e){
			logger.error(ErrorRecord.recordError(e));
		}
		
		return null;
	}
	
	public static void settingReSync(String input){

		try{
			List<Object[]> servers = ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class).findLikeConfigId("%." + REGISTER_SYNC);
			if(servers != null && servers.size() > 0){
				for(Object[] server : servers){
					String ip = (String) server[1];
					
					ip = ip.replaceAll(",", "");
	
					SystemConfig config = new SystemConfig();
					config.setConfigId(ip + "." + input);
					config.setValue("true");
					config.setModifyTime(new Date());
					
					ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class).save(config);
					logger.debug("settingReSync:" + ip + "." + input);
				}
			}
		}
		catch(Throwable e){
			logger.error(ErrorRecord.recordError(e));
		}
	}
	
	public static List<String> getServersIp(){

		List<String> result = new ArrayList<String>();

		try{
			List<Object[]> servers = ApplicationContextProvider.getApplicationContext().getBean(SystemConfigService.class).findLikeConfigId("%." + REGISTER_SYNC);
			if(servers != null && servers.size() > 0){
			    
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	                
                Calendar c = Calendar.getInstance();
                c.setTime(new Date());
                c.add(Calendar.MINUTE, -10);
                Date standardTime = c.getTime();
                
				for(Object[] server : servers){
					String ip = (String)server[1];
					Date modifyTime = server[2] == null? null : SQLDateFormatUtil.formatSqlStringToDate(server[2], sdf);
					
					if(modifyTime == null || modifyTime.after(standardTime)) {
	
						String[] split = ip.split(",");
						if(split != null && split.length == 2){
							result.add(split[0]);
						}
						else{
							result.add(ip);
						}
					}
				}
			}
		}
		catch(Throwable e){
			logger.error(ErrorRecord.recordError(e));
		}
		
		return result;
	}
}
