package com.bcs.core.db.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentEsnMain;
import com.bcs.core.db.repository.ContentEsnMainRepository;

@Service
public class ContentEsnMainService {

	@Autowired
	private ContentEsnMainRepository contentEsnMainRepository;
	
	/**
	 * 依據ID，取得電子序號檔
	 * @param esnId String
	 * @return ContentEsnMain
	 */
	public ContentEsnMain findOne(String esnId) {
	    return contentEsnMainRepository.findOne(esnId);
	}
	
	/**
	 * 修改電子序號檔
	 * @param contentEsnMain ContentEsnMain
	 */
	public void save(ContentEsnMain contentEsnMain) {
		contentEsnMainRepository.save(contentEsnMain);
	}

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void delete(ContentEsnMain esnMain) {
		contentEsnMainRepository.updateStatusByEsnId(ContentEsnMain.STATUS_DELETE, esnMain.getEsnId());
	}

    /**
     * TODO 依據電子序號狀態取得資料
     * @param status
     * @return
     * @throws Exception 
     */
    @Transactional(readOnly = true, timeout = 60)
    public List<Object[]> findDataByStatusAndModifyTime(List<String> statusList, String startDate, String endDate) throws Exception {
        
        if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            Calendar c = Calendar.getInstance();
            c.setTime(end);
            c.add(Calendar.DATE, 1); //增加一天，因為轉換的date其分秒是0，因此查詢時，今天新增的發送報告有設定時與分時，可能會撈不到
            c.add(Calendar.SECOND, -1); //減一秒，因為可能今天新增的發送報告時間是隔天且無設定時與分，會與增加一天的時間重疊，導致可能撈到隔天的資料
            end = c.getTime();
            
            return contentEsnMainRepository.findDataByStatusAndModifyTime(statusList, start, end);
        }
        
        return contentEsnMainRepository.findDataByStatus(statusList);
    }
	
	/**
     * 產生ESN_ID
     * @return
     */
    public String generateEsnId() {
        String esnId = UUID.randomUUID().toString().toLowerCase();
        
        while (findOne(esnId) != null) {
            esnId = UUID.randomUUID().toString().toLowerCase();
        }
        return esnId;
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public int updateSendStatusByEsnId(String sendStatus, String esnId) {
        return contentEsnMainRepository.updateSendStatusByEsnId(sendStatus, esnId);
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public int updateStatusByEsnId(String status, String esnId) {
        return contentEsnMainRepository.updateStatusByEsnId(status, esnId);
    }
}
