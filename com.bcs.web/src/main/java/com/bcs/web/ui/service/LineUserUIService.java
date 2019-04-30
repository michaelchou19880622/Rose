package com.bcs.web.ui.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.UserFieldSet;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.UserFieldSetService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.upload.ImportDataFromExcel;
import com.bcs.core.upload.ImportDataFromText;
import com.bcs.core.utils.ErrorRecord;

@Service
public class LineUserUIService {

	/** Logger */
	private static Logger logger = Logger.getLogger(LineUserUIService.class);

    @Autowired
    private LineUserService lineUserService;
    @Autowired
    private UserFieldSetService userFieldSetService;
    @Autowired
    private ImportDataFromExcel importDataFromExcel;
    @Autowired
    private ImportDataFromText importDataFromText;

	@Transactional(rollbackFor=Exception.class, timeout = 3600)
    public Map<String, Object> uploadLineUserList(MultipartFile filePart, String modifyUser, Date modifyTime) throws Exception{

        String fileName = filePart.getOriginalFilename();
        logger.info("getOriginalFilename:" + fileName);
        String contentType = filePart.getContentType();
        logger.info("getContentType:" + contentType);
        logger.info("getSize:" + filePart.getSize());

        Map<String, List<String>> dataMap = null;
        if("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) || "application/vnd.ms-excel".equals(contentType)){
            dataMap = importDataFromExcel.importDataKeyValueList(filePart.getInputStream());    
        }
        else if("text/plain".equals(contentType)){
            dataMap = importDataFromText.importDataKeyValueList(filePart.getInputStream()); 
        }
        
        int count = 0;
        if(dataMap != null && dataMap.size() > 0){
            
            String[] keyRows = {"name", "description", "type", "format"}; 
            
            List<String> nameList = dataMap.get(keyRows[0]);
            List<String> descriptionList = dataMap.get(keyRows[1]);
            List<String> typeList = dataMap.get(keyRows[2]);
            List<String> formatList = dataMap.get(keyRows[3]);
            
            for (String key : dataMap.keySet()) { 
                try {
                    boolean isSkip = false;
                    for (String keyRow : keyRows) {
                        if (keyRow.equals(key)) {
                            isSkip = true;
                        }
                    }
                    if (isSkip) {
                        continue;
                    }
                    
                    
                    List<String> valueList = dataMap.get(key);
                    
                    String mid = valueList.get(0);
                    
                    LineUser user = new LineUser();
                    user.setMid(mid);
                    user.setStatus(LineUser.STATUS_BINDED);
                    user.setCreateTime(new Date());
                    user.setModifyTime(new Date());
                    user.setSoureType(MsgBotReceive.SOURCE_TYPE_USER);
                    
                    for (int i = 1; i < valueList.size(); i++) {
                        if (valueList.size() <= i) {
                            break;
                        }
                        
                        String name = nameList.get(i);
                        String description = descriptionList.get(i);
                        String type = typeList.get(i);
                        String format = formatList.size() > i ? formatList.get(i) : null;
                        String value = valueList.get(i);
                        
                        
                        if ("MID".equals(name)) {
                            user.setMid(value);
                        } else if ("Name".equals(name)) {
                            user.setName(value);
                        } else if ("Mobile".equals(name)) {
                            user.setMobile(value);
                        } else if ("Email".equals(name)) {
                            user.setEmail(value);
                        } else {
                            UserFieldSet ufs = new UserFieldSet();
                            ufs.setMid(mid);
                            ufs.setKeyData(name);
                            ufs.setName(description);
                            ufs.setValue(value);
                            ufs.setSetTime(new Date());
                            ufs.setType(type);
                            ufs.setFormat(format);
                            userFieldSetService.save(ufs);
                        }
                    }
                    
                    lineUserService.save(user);
                    count++;
                    logger.info("count=" + count);
                } catch (Exception e) {
                    logger.error(ErrorRecord.recordError(e));
                    logger.error("key=" + key);
                }
            }
            

            Map<String, Object> result = new HashMap<String, Object>();
            result.put("count", count);
            
            return result;
        }
        else if(dataMap == null){
            throw new BcsNoticeException("上傳格式錯誤");
        }
        else{
            throw new BcsNoticeException("沒有上傳資料");
        }
    }
	
	
	public enum USERFIELD {
        CUSTID("CUSTID","身分證字號","String"), 
        PHONE("PHONE","手機號碼","String"), 
        GENDER("GENDER","性別","String"), 
        BIRTH("BIRTH","生日","Date"),
		ADDRESS("ADDRESS","通訊地址","String"),
		CITYDISTRICT("CITYDISTRICT","縣市行政區","String"),
		HASINV("HASINV","是否申請信託帳戶","String"),
		BIRTHYEAR("BIRTHYEAR","出生年","String"),
		BIRTHMONTH("BIRTHMONTH","出生月","String"),
		BIRTHDAY("BIRTHDAY","出生日","String"),
		HASCC("HASCC","有無gogo卡","String"),
		HASSA("HASSA","有無Richart帳戶(台幣)","String"),
		UID("UID","LineID","String"),
		BINDINGTIME("BINDINGTIME","綁定時間","Date"),
		SENDINGTIME("SENDINGTIME","資料更新時間","Date"),
		STATUS("STATUS","綁定狀態","String");
		
        private String colum_en;
        private String colum_ch;
        private String type;
        
        // 构造方法
        private USERFIELD(String colum_en,String colum_ch,String type) {
            this.colum_en = colum_en;
            this.colum_ch = colum_ch;
            this.type=type;
        }

        public static USERFIELD getByColum(final String colum) {
            for (USERFIELD e : USERFIELD.values()) {
                if (e.colum_en.equalsIgnoreCase(colum)) {
                    return e;
                }
            }
            return null;
        }

		public String getColum_en() {
			return colum_en;
		}

		public void setColum_en(String colum_en) {
			this.colum_en = colum_en;
		}

		public String getColum_ch() {
			return colum_ch;
		}

		public void setColum_ch(String colum_ch) {
			this.colum_ch = colum_ch;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
        
    }
	
    public int uploadLineUserListCSVData(MultipartFile filePart, String modifyUser, Date modifyTime) throws Exception{
        String fileName = filePart.getOriginalFilename();
        logger.info("getOriginalFilename:" + fileName);
        String contentType = filePart.getContentType();
        logger.info("getContentType:" + contentType);
        logger.info("getSize:" + filePart.getSize());

        List<Map<String, String>> lineUserList = null;
        if("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) || "application/vnd.ms-excel".equals(contentType)){
        	lineUserList = importDataFromExcel.importCSVDataKeyValueList(filePart.getInputStream());    
        }
        else if("text/plain".equals(contentType)){
        	lineUserList = importDataFromText.importCSVDataKeyValueList(filePart.getInputStream()); 
        }
        
        if(CollectionUtils.isEmpty(lineUserList)){
            throw new BcsNoticeException("上傳格式錯誤");
        }
        
        final List<Map<String, String>> lineUserListFinal = lineUserList;
        
		Thread thread = new Thread(new Runnable() {
			public void run() {
				updateLineUserAndUserFieldSet(lineUserListFinal);
			}
		});
		
		thread.start();
        
		return lineUserListFinal.size();

    }
    
    private void updateLineUserAndUserFieldSet(List<Map<String, String>> lineUserList){
		Date time = new Date();

        if(lineUserList != null && lineUserList.size() > 0){        
            for (Map<String,String> lineUser : lineUserList) { 
                try {
                	logger.info("lineUser: "+lineUser);
                	String UID = lineUser.get(USERFIELD.UID.getColum_en());
                	List<UserFieldSet> existedUserFieldSets = userFieldSetService.findByMid(UID);
                	                	
                	LineUser user = lineUserService.findByMid(UID);
                	
                	if(user==null){
                       user = new LineUser(); 
                    }

                  //針對 lineUser 欄位,csv 有資料就覆蓋,沒有就新增
                  user.setMid((lineUser.get(USERFIELD.UID.toString()) != null)?lineUser.get(USERFIELD.UID.toString()):user.getMid()); 
                  user.setStatus((lineUser.get(USERFIELD.STATUS.toString()) != null && user.getStatus() != LineUser.STATUS_BLOCK )?lineUser.get(USERFIELD.STATUS.toString()):user.getStatus());   
                  user.setIsBinded((lineUser.get(USERFIELD.STATUS.toString()) != null )?lineUser.get(USERFIELD.STATUS.toString()):user.getStatus());
                  user.setSoureType(MsgBotReceive.SOURCE_TYPE_USER);

                  user.setAddress((lineUser.get(USERFIELD.ADDRESS.toString()) != null )?lineUser.get(USERFIELD.ADDRESS.toString()):user.getAddress()); 
                  user.setBirthday((lineUser.get(USERFIELD.BIRTHDAY.toString()) != null )?lineUser.get(USERFIELD.BIRTHDAY.toString()):user.getBirthday()); 
                  user.setCityDistrict((lineUser.get(USERFIELD.CITYDISTRICT.toString()) != null )?lineUser.get(USERFIELD.CITYDISTRICT.toString()):user.getCityDistrict());  
                  user.setCustId((lineUser.get(USERFIELD.CUSTID.toString()) != null )?lineUser.get(USERFIELD.CUSTID.toString()):user.getCustId()); 
                  user.setGender((lineUser.get(USERFIELD.GENDER.toString()) != null )?lineUser.get(USERFIELD.GENDER.toString()):user.getGender()); 
                  user.setHasInv((lineUser.get(USERFIELD.HASINV.toString()) != null )?lineUser.get(USERFIELD.HASINV.toString()):user.getHasInv()); 
                  user.setPhone((lineUser.get(USERFIELD.PHONE.toString()) != null )?lineUser.get(USERFIELD.PHONE.toString()):user.getPhone()); 
                                   
                  user.setCreateTime((user.getCreateTime()==null )?time:user.getCreateTime()); 
                  user.setModifyTime(time);
                  
                  lineUserService.save(user);
                	
                	for (Map.Entry<String, String> lineUserColum : lineUser.entrySet()) {
                			Boolean isExsited = false; 
	                    	USERFIELD userField  =USERFIELD.getByColum(lineUserColum.getKey());//尋找是否有ENUM可以處理的欄位
	                    	UserFieldSet userFieldSet = new UserFieldSet();	
	                    	
	                    	if(userField!=null && !existedUserFieldSets.isEmpty()){//是否有現存的資料存在
	                    		for(UserFieldSet existedUserFieldSet :existedUserFieldSets){//如果有重複的KEYDATA就覆蓋更新
	                        		if(existedUserFieldSet.getKeyData().equals(userField.getColum_en())){
	                        			existedUserFieldSet.setValue(lineUserColum.getValue());
	                        			existedUserFieldSet.setSetTime(time);
	                        			userFieldSetService.save(existedUserFieldSet);
	                        			isExsited = true;
	                        			break;
	                        		}
	                        	}
	                    	}
	                    	if(userField!=null && !isExsited){//如果有未存在的就新增
	                    		userFieldSet.setMid(UID);//MID
	                    		userFieldSet.setType(userField.getType());//資料格式
	                    		userFieldSet.setName(userField.getColum_ch());//中文欄位名稱
	                    		userFieldSet.setKeyData(userField.getColum_en());//英文欄位名稱
	                    		userFieldSet.setValue(lineUserColum.getValue());//資料
	                    		userFieldSet.setSetTime(time);//時間
	                    		userFieldSetService.save(userFieldSet);
	                    	}
                	}                	
                } catch (Exception e) {
                    logger.error(ErrorRecord.recordError(e));
                }
            }
        }
    }
}
