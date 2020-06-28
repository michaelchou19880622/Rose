package com.bcs.web.ui.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.bot.akka.service.AkkaBotService;
import com.bcs.core.db.entity.ContentEsnDetail;
import com.bcs.core.db.entity.ContentEsnMain;
import com.bcs.core.db.service.ContentEsnDetailService;
import com.bcs.core.db.service.ContentEsnMainService;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.send.akka.model.AsyncEsnSendingModel;
import com.bcs.core.upload.ImportDataFromExcel;
import com.bcs.core.upload.ImportDataFromText;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CustomUser;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

@Service
public class EsnUIService {

    /** Logger */
    private static Logger logger = Logger.getLogger(EsnUIService.class);

	@Autowired
	private ImportDataFromExcel importFromExcel;
	@Autowired
	private ImportDataFromText importFromText;
	@Autowired
	private ContentEsnMainService contentEsnMainService;
	@Autowired
	private ContentEsnDetailService contentEsnDetailService;
	@Autowired
	private AkkaBotService akkaBotService;

	/**
	 * 建立電子序號資料
	 * @param esnName String 活動名稱
	 * @param esnMsg String 活動內容
	 * @param file MultipartFile 上傳檔案
	 * @param customUser CustomUser 使用者資訊
	 * @param type String 建立電子序號類別
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class, timeout = 180)
	public void createEsnData(String esnId, String esnName, String esnMsg, MultipartFile file, CustomUser customUser) throws Exception {
		checkEsn(esnId, esnName, esnMsg, file);

		String action = StringUtils.isBlank(esnId)? "Create":"Edit";

		List<Map<String, String>> esnList = new ArrayList<>();

	    if(file != null) {
	        String contentType = file.getContentType();

	        // 比對檔案類型
	        if("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) || "application/vnd.ms-excel".equals(contentType)){
	            esnList = importFromExcel.importCSVDataKeyValueList(file.getInputStream());
	        }
	        else if("text/plain".equals(contentType)){
	            esnList = importFromText.importCSVDataKeyValueList(file.getInputStream());
	        }
	        else {
	            throw new BcsNoticeException("檔案格式錯誤");
	        }
	    }


	    ContentEsnMain contentEsnMain = null;

		// create UUID
	    if(StringUtils.isBlank(esnId)) {
	        if(esnList.isEmpty()) {
	            throw new BcsNoticeException("檔案不得為空");
	        }
	        esnId = contentEsnMainService.generateEsnId();
	    }
	    else {
	        contentEsnMain = contentEsnMainService.findOne(esnId);
	        contentEsnMain.setStatus(ContentEsnMain.STATUS_ACTIVE);
	    }

	    Date now = new Date();

		// 建立主檔
	    if(contentEsnMain == null) {
	        contentEsnMain = new ContentEsnMain();
	        contentEsnMain.setStatus(ContentEsnMain.STATUS_DISABLE);
	    }
		contentEsnMain.setEsnId(esnId);
		contentEsnMain.setEsnName(esnName);
		contentEsnMain.setEsnMsg(esnMsg);
		contentEsnMain.setModifyUser(customUser.getAccount());
		contentEsnMain.setModifyTime(now);
		contentEsnMainService.save(contentEsnMain);

		if (!esnList.isEmpty()) {
		    createDetails(esnList, esnId);
		}

		createSystemLog(action, contentEsnMain, customUser.getAccount(), now, esnId);
	}

	private void createDetails(final List<Map<String, String>> esnList, final String esnId) {

	    Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

                if (!esnList.isEmpty()) {
        			logger.info("Started a new thread to create content ESN details , ESNID = " + esnId);
        	        
                    // 建立細項檔
        			List<ContentEsnDetail> detailList = new ArrayList<>();
        			int i = 0;
                    for (Map<String, String> map : esnList) {

                        if(StringUtils.isNotBlank(map.get("UID")) && StringUtils.isNotBlank(map.get("ESN"))) {
                            ContentEsnDetail detail = new ContentEsnDetail();
                            detail.setEsnId(esnId);
                            detail.setEsn(map.get("ESN"));
                            detail.setUid(map.get("UID"));
                            detail.setStatus(ContentEsnDetail.STATUS_READY);
//                            contentEsnDetailService.save(detail);                            
                            detailList.add(detail);
                            i ++;
                            /* 每一千筆處理一次 */
                            if (i % 1000 == 0) {
//                            	logger.info("detailList size:" + detailList.size());
                                contentEsnDetailService.save(detailList);
                                detailList.clear();
                            }   
                        }
                    }
                    /* Update userEventSet */
                    if (!detailList.isEmpty()) {
                    	logger.info("The Last detailList size is = " + detailList.size());
                        contentEsnDetailService.save(detailList);
                        detailList.clear();
                    }        

        			logger.info("Finished a new thread to create content ESN details , ESNID = " + esnId);
                    contentEsnMainService.updateStatusByEsnId(ContentEsnMain.STATUS_ACTIVE, esnId);
                }
            }
        });

	    t.start();
	}

	private void checkEsn(String esnId, String esnName, String esnMsg, MultipartFile file) {
        Validate.notBlank(esnName, "Esn Name Null");
        Validate.notBlank(esnMsg, "Esn Msg Null");

        if(StringUtils.isBlank(esnId)) {
            Validate.notNull(file, "Esn File Null");
        }
    }

    private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime, String referenceId) {
        SystemLogUtil.saveLogDebug("ContentEsn", action, modifyUser, content, referenceId);
    }

    public void sendEsnMsg(String esnId, CustomUser customUser) throws Exception {

        ContentEsnMain contentEsnMain = contentEsnMainService.findOne(esnId);

        if(ContentEsnMain.SEND_STATUS_READY.equals(contentEsnMain.getSendStatus())) {
            contentEsnMain.setSendStatus(ContentEsnMain.SEND_STATUS_PROCESS);
            contentEsnMainService.save(contentEsnMain);

            sendEsnMsg(esnId, contentEsnMain.getEsnMsg());
            createSystemLog("SendEsnMsg", contentEsnMain, customUser.getAccount(), new Date(), esnId);
        }
        else {
            throw new BcsNoticeException("已發送過");
        }
    }

    private void sendEsnMsg(final String esnId, final String esnMsg) {

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    List<ContentEsnDetail> contentEsnDetails = contentEsnDetailService.findByEsnIdAndStatusAndUidNotNull(esnId, ContentEsnDetail.STATUS_READY);

                    if(contentEsnDetails != null && contentEsnDetails.size() > 0) {

                        List<Message> messages = new ArrayList<>();

                        TextMessage esnMsgMessage = new TextMessage(esnMsg);
                        messages.add(esnMsgMessage);

                        int pageSize = 150;

                        int page = 0;
                        List<ContentEsnDetail> sendDetails = new ArrayList<>();
                        for(ContentEsnDetail detail : contentEsnDetails) {
                            sendDetails.add(detail);

                            if(sendDetails.size() % pageSize == 0){
                                AsyncEsnSendingModel model = new AsyncEsnSendingModel(CONFIG_STR.DEFAULT.toString(), messages, sendDetails, API_TYPE.BOT);
                                akkaBotService.sendingMsgs(model);

                                sendDetails = new ArrayList<>();

                                page++;
                                if(page % 10 == 0) {
                                    // delay 10 seconds
                                    Thread.sleep(10*1000);
                                }
                            }
                        }

                        if(sendDetails.size() > 0) {
                            AsyncEsnSendingModel model = new AsyncEsnSendingModel(CONFIG_STR.DEFAULT.toString(), messages, sendDetails, API_TYPE.BOT);
                            akkaBotService.sendingMsgs(model);
                        }
                    }

                }catch(Exception e) {
                    logger.error(ErrorRecord.recordError(e));
                }finally {
                    contentEsnMainService.updateSendStatusByEsnId(ContentEsnMain.SEND_STATUS_FINISH, esnId);
                }
            }
        });

        t.start();
    }
}
