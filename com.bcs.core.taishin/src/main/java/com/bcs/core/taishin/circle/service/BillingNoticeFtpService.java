package com.bcs.core.taishin.circle.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsg;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeDetailRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeMainRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeRepositoryCustom;
import com.bcs.core.taishin.circle.ftp.FtpService;
import com.bcs.core.taishin.circle.ftp.FtpSetting;

@Service
public class BillingNoticeFtpService {

	/** Logger */
	private static Logger logger = Logger.getLogger(BillingNoticeFtpService.class);
	private DateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	@Autowired
	private BillingNoticeRepositoryCustom billingNoticeRepositoryCustom;
	@Autowired
	private BillingNoticeMainRepository billingNoticeMainRepository;
	@Autowired
	private BillingNoticeDetailRepository billingNoticeDetailRepository;
	@Autowired
	private BillingNoticeContentTemplateMsgRepository billingNoticeContentTemplateMsgRepository;
	@Autowired
	private FtpService ftpService;
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> scheduledFuture = null;
	private final String DEFAULT_TEMPLATE = "default";

	public BillingNoticeFtpService() {
	}

	/**
	 * Start Schedule
	 * 
	 * @throws SchedulerException
	 * @throws InterruptedException
	 */
	public void startCircle() throws SchedulerException, InterruptedException {
		String unit = CoreConfigReader.getString(CONFIG_STR.BN_SCHEDULE_UNIT, true);
		int time = CoreConfigReader.getInteger(CONFIG_STR.BN_SCHEDULE_TIME, true);
		if (time == -1 || TimeUnit.valueOf(unit) == null) {
			logger.error(" BillingNoticeFtpService TimeUnit error :" + time  + unit);
			return;
		}
		scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				// 排程工作
				logger.debug(" BillingNoticeFtpService startCircle....");
				ftpProcessHandler();
			}
		}, 0,  time, TimeUnit.valueOf(unit));

	}
	

	/**
	 * 執行流程
	 */
	private void ftpProcessHandler() {
		boolean bigSwitch = CoreConfigReader.getBoolean(CONFIG_STR.BN_BIGSWITCH, true);
		String downloadSavePath = CoreConfigReader.getString(CONFIG_STR.BN_FTP_DOWNLOAD_SAVEFILEPATH, true);
		String fileExtension = CoreConfigReader.getString(CONFIG_STR.BN_FTP_FILE_EXTENSION, true);
		if (!bigSwitch) { //大流程關閉時不做
			return;
		}
		
		try {
			// 1. ftp get file
			Map<String,byte[]> lReturnDatas = new HashMap<String,byte[]>();
			List<FtpSetting> ftpSettings = ftpService.getFtpSettings();
			for(FtpSetting ftp : ftpSettings) {
				ftp.clearFileNames();	
				Map<String,byte[]> returnDatas = ftpService.downloadMutipleFileByType(ftp.getPath(), fileExtension, ftp);
				for(String fileName : returnDatas.keySet()) {
					ftp.addFileNames(fileName);
					logger.info(" FTP--" + ftp.getChannelId() + ":" + fileName );
				}
				lReturnDatas.putAll(returnDatas);
			}
			
			// 2. prase data to object
			List<BillingNoticeMain> mains = new ArrayList<BillingNoticeMain>();
			for (String fileName : lReturnDatas.keySet()) {
				String encoding = "UTF-8";
				for(FtpSetting ftp : ftpSettings) {
					if (ftp.containsFileName(fileName)) {
						encoding = ftp.getFileEncoding();
						logger.info(fileName + " encoding:" +  encoding );
					}
				}
				byte[] fileData = lReturnDatas.get(fileName);
				logger.info(" BillingNoticeFtpService handle file:" + downloadSavePath + File.separator + fileName );
				File targetFile = new File(downloadSavePath + File.separator + fileName);
			    FileUtils.writeByteArrayToFile(targetFile, fileData);
			    InputStream targetStream = new FileInputStream(targetFile);
				List<String> fileContents = IOUtils.readLines(targetStream, encoding);
				List<BillingNoticeMain> billingNoticeMains = parseFtpFile(fileName, fileContents);
				if (billingNoticeMains != null) {
					mains.addAll(billingNoticeMains);
				}
				targetStream.close();
			}
			// 3. save data to db = Status = DRAFT
			for (BillingNoticeMain billingNoticeMain : mains) {
				saveDB(billingNoticeMain);
			}
			// Status = DRAFT --> Retry or WAIT
			for (BillingNoticeMain billingNoticeMain : mains) {
				updateStatus(billingNoticeMain);
			}
			
			// 4. remove file 
			if (!lReturnDatas.isEmpty()) {
				for(FtpSetting ftp : ftpSettings) {
					ftpService.deleteFileByType(ftp.getPath(), ftp.getFileNames().toArray(new String[0]), ftp);
				}
				
			}

		} catch (Exception ex) {
			logger.error("Error: " + ex.getMessage());
			logger.error(ex);
			ex.printStackTrace();
		} finally {
			
		}
	}

	
	private Map<String, List<BillingNoticeFtpDetail>> parseDetail(List<String> fileContents){
		List<BillingNoticeFtpDetail> details = new ArrayList<BillingNoticeFtpDetail>();
		for (int i = 1; i < fileContents.size(); i++) {
			if (StringUtils.isNotBlank(fileContents.get(i))) {
				String[] detailData = fileContents.get(i).split("\\|");
				if (detailData.length == 4) {
					BillingNoticeFtpDetail detail = new BillingNoticeFtpDetail();
					detail.setUid(detailData[0]);
					detail.setTitle(detailData[1]);
					detail.setText(detailData[2]);
					detail.setTemplate(detailData[3]);
					details.add(detail);
				}else {
					logger.error("parseFtpFile error Data:" + detailData);
				}
			}
		}
		
		Map<String, List<BillingNoticeFtpDetail>> resultMap = new HashMap<String, List<BillingNoticeFtpDetail>>();

		for (BillingNoticeFtpDetail deatil : details) {
		    String key  = deatil.getTemplate();
		    if(resultMap.containsKey(key)){
		        List<BillingNoticeFtpDetail> list = resultMap.get(key);
		        list.add(deatil);

		    }else{
		        List<BillingNoticeFtpDetail> list = new ArrayList<BillingNoticeFtpDetail>();
		        list.add(deatil);
		        resultMap.put(key, list);
		    }

		}
		return resultMap;
	}
	
	/**
	 * 整理txt data 
	 * @param origFileName
	 * @param fileContents
	 */
	private List<BillingNoticeMain> parseFtpFile(String origFileName, List<String> fileContents) {
		List<BillingNoticeMain> mains = new ArrayList<BillingNoticeMain>();
		if (!fileContents.isEmpty()) {
			String header = fileContents.get(0);
			if (validateHeader(header)) {
				Calendar expiryTime = Calendar.getInstance();
				expiryTime.add(Calendar.HOUR, 3);
				String[] splitHeaderData = header.split("\\|");
				String originalFileType = splitHeaderData[0];
				String sendType = splitHeaderData[1].toUpperCase();
				String scheduleTime = splitHeaderData[2];
				
				// TemplateTitle => Details
				Map<String, List<BillingNoticeFtpDetail>> resultMap = parseDetail(fileContents);
				
				for (String key : resultMap.keySet()) {
					// get the Main Template
					BillingNoticeContentTemplateMsg template = null;
					List<BillingNoticeContentTemplateMsg> templates =  billingNoticeContentTemplateMsgRepository.findMainOnTemplateByTitle(key);
					if (templates == null || templates.isEmpty()) {
						logger.info("Template :" + key + " not exist!  Use Default");
						// 拿不到範本取default
						List<BillingNoticeContentTemplateMsg> defaultTemplates =  billingNoticeContentTemplateMsgRepository.findByTemplateTitleAndProductSwitchOn(DEFAULT_TEMPLATE);
						if (defaultTemplates == null || defaultTemplates.isEmpty()) {
							logger.error("Default Template is not exist!");
						}else {
							template = defaultTemplates.get(0);
						}
					}else {
						// carousel button
						template = templates.get(0);
					}
					
					if (template == null) {
						logger.error("Template is not exist!:" + key);
					}else {
						// set Basics
						BillingNoticeMain billingNoticeMain = new BillingNoticeMain();
						billingNoticeMain.setGroupId(new Long(mains.size() + 1));
						billingNoticeMain.setSendType(sendType);
						billingNoticeMain.setOrigFileName(origFileName);
						billingNoticeMain.setOrigFileType(originalFileType);
						billingNoticeMain.setStatus(BillingNoticeMain.NOTICE_STATUS_DRAFT);
						billingNoticeMain.setExpiryTime(expiryTime.getTime());
						billingNoticeMain.setTempId(template.getTemplateId()); 
						if (sendType.equals(BillingNoticeMain.SENDING_MSG_TYPE_DELAY)) {
							billingNoticeMain.setScheduleTime(scheduleTime);
						}
						billingNoticeMain.setTemplate(template);
						
						// set Details
						List<BillingNoticeFtpDetail> ftpDetails = resultMap.get(key);
						List<BillingNoticeDetail> details = new ArrayList<BillingNoticeDetail>();
						for (BillingNoticeFtpDetail ftpDetail : ftpDetails) {
							BillingNoticeDetail detail = new BillingNoticeDetail();
							detail.setMsgType(BillingNoticeDetail.MSG_TYPE_TEMPLATE);
							detail.setStatus(BillingNoticeMain.NOTICE_STATUS_DRAFT);
							detail.setUid(ftpDetail.getUid());
							detail.setTitle(ftpDetail.getTitle());
							detail.setText(ftpDetail.getText().replaceAll("\\\\n", "\n"));
							detail.setNoticeMainId(billingNoticeMain.getNoticeMainId());
							details.add(detail);
						}
						billingNoticeMain.setDetails(details);
						mains.add(billingNoticeMain);
					}
				}
			}
		}
		return mains;
	}
	
	/**
	 * save db Status = DRAFT
	 * @param origFileName
	 * @param fileContents
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private void saveDB(BillingNoticeMain billingNoticeMain) {
		List<BillingNoticeDetail> originalDetails = billingNoticeMain.getDetails();
		logger.info(" BillingNoticeFtpService BillingNoticeDetail size:" + originalDetails.size() );
		billingNoticeMain = billingNoticeMainRepository.save(billingNoticeMain);
		List<BillingNoticeDetail> details = new ArrayList<>();
		for( BillingNoticeDetail detail : originalDetails) {
			detail.setNoticeMainId(billingNoticeMain.getNoticeMainId());
			details.add(detail);
		}
		if (!details.isEmpty()) {
			billingNoticeRepositoryCustom.batchInsertBillingNoticeDetail(details);
		}
	}
	
	
	/**
	 * 資料解析完狀態改為retry or wait
	 * @param billingNoticeMain
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private void updateStatus(BillingNoticeMain billingNoticeMain) {
		BillingNoticeContentTemplateMsg template = billingNoticeContentTemplateMsgRepository.findOne(billingNoticeMain.getTempId());
		Long mainId = billingNoticeMain.getNoticeMainId();
		String status = BillingNoticeMain.NOTICE_STATUS_WAIT;
		// 流程開關
		if (!template.isProductSwitch()) {
			status = BillingNoticeMain.NOTICE_STATUS_RETRY;
			logger.info("Curfew NOTICE_STATUS_RETRY mainId:" + mainId);
		}
		Date  now = Calendar.getInstance().getTime();
		billingNoticeMainRepository.updateBillingNoticeMainStatus(status,now, mainId);
		billingNoticeDetailRepository.updateStatusByMainId(status,now, mainId);
	}

	/**
	 * check header 
	 * @param header
	 * @return
	 */
	private boolean validateHeader(String header) {
		if (StringUtils.isBlank(header)) {
			return false;
		}
		String[] splitData = header.split("\\|");
		int length = splitData.length;
		if (length != 3) {
			return false;
		}
		String sendType = splitData[1].toUpperCase();
		if (!sendType.equals(BillingNoticeMain.SENDING_MSG_TYPE_DELAY)
				&& !sendType.equals(BillingNoticeMain.SENDING_MSG_TYPE_IMMEDIATE)) {
			return false;
		}
		if (sendType.equals(BillingNoticeMain.SENDING_MSG_TYPE_DELAY)) {
			String scheduleTime =splitData[2];
			try {
				dataFormat.parse(scheduleTime);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("ScheduleTime format Error :" + scheduleTime);
				return false;
			}
		}

		return true;
	}

	/**
	 * Stop Schedule : Wait for Executing Jobs to Finish
	 * 
	 * @throws SchedulerException
	 */
	@PreDestroy
	public void destroy() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(true);
			logger.info(" BillingNoticeFtpService cancel....");
		}

		if (scheduler != null && !scheduler.isShutdown()) {
			logger.info(" BillingNoticeFtpService shutdown....");
			scheduler.shutdown();
		}

	}

}
