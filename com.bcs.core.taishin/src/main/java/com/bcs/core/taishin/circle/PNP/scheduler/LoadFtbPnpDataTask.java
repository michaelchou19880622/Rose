package com.bcs.core.taishin.circle.PNP.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailEvery8dRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMingRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMitakeRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMitakeRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailUnicaRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailUnicaRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainMitakeRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainUnicaRepository;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFtpService;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFtpSetting;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.utils.ErrorRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LoadFtbPnpDataTask {

	private static Logger logger = Logger.getLogger(LoadFtbPnpDataTask.class);

	private static List<Map<String, Object>> ftpInfoList;

	private DateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> scheduledFuture = null;

	@Autowired
	private PNPFtpService pnpFtpService;

	@Autowired
	private PnpDetailMingRepositoryCustom pnpDetailMingRepositoryCustom;
	@Autowired
	private PnpDetailUnicaRepositoryCustom pnpDetailUnicaRepositoryCustom;
	@Autowired
	private PnpDetailEvery8dRepositoryCustom pnpDetailEvery8dRepositoryCustom;
	@Autowired
	private PnpDetailMitakeRepositoryCustom pnpDetailMitakeRepositoryCustom;

	@Autowired
	private PnpMainUnicaRepository pnpMainUnicaRepository;
	
	@Autowired
	private PnpDetailUnicaRepository pnpDetailUnicaRepository;

	@Autowired
	private PnpMainEvery8dRepository pnpMainEvery8dRepository;
	
	@Autowired
	private PnpDetailEvery8dRepository pnpDetailEvery8dRepository;
	
	@Autowired
	private PnpMainMingRepository pnpMainMingRepository;
	
	@Autowired
	private PnpDetailMingRepository pnpDetailMingRepository;

	@Autowired
	private PnpMainMitakeRepository pnpMainMitakeRepository;
	
	@Autowired
	private PnpDetailMitakeRepository pnpDetailMitakeRepository;

//	String ftpServerName = null;
//	int ftpPort = 0;
//	String ftpUsr = null;
//	String ftpPass = null;
//	String downloadSavePath = null;
//	String downloadPath = null;
//	String uploadPath = null;

	static {
		String ftpInfo = CoreConfigReader.getString(CONFIG_STR.SYSTEM_PNP_FTP_INFO);

		if (StringUtils.isNotBlank(ftpInfo)) {
			try {
				ftpInfoList = new ObjectMapper().readValue(ftpInfo, new TypeReference<List<Map<String, Object>>>() {
				});
			} catch (IOException e) {
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}

	/**
	 * Start Schedule
	 * 
	 * @throws SchedulerException
	 * @throws InterruptedException
	 */
	public void startCircle() throws SchedulerException, InterruptedException {
		String unit = CoreConfigReader.getString(CONFIG_STR.PNP_SCHEDULE_UNIT, true, false);
		int time = CoreConfigReader.getInteger(CONFIG_STR.PNP_SCHEDULE_TIME, true, false);
		if (time == -1 || TimeUnit.valueOf(unit) == null) {
			logger.error(" LoadFtbPnpDataTask TimeUnit error :" + time + unit);
			return;
		}
		scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				// 排程工作
			logger.info(" LoadFtbPnpDataTask ftpProcessHandler SOURCE_MITAKE....");
				try {
				ftpProcessHandler(AbstractPnpMainEntity.SOURCE_MITAKE);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				logger.info(" LoadFtbPnpDataTask ftpProcessHandler SOURCE_MING....");
				try {
					ftpProcessHandler(AbstractPnpMainEntity.SOURCE_MING);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				logger.info(" LoadFtbPnpDataTask ftpProcessHandler SOURCE_EVERY8D....");
				try {
					ftpProcessHandler(AbstractPnpMainEntity.SOURCE_EVERY8D);
				} catch (Exception e) {
					e.printStackTrace();
				}
				 
				logger.info(" LoadFtbPnpDataTask ftpProcessHandler SOURCE_UNICA....");
				try {
					ftpProcessHandler(AbstractPnpMainEntity.SOURCE_UNICA);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, time, TimeUnit.valueOf(unit));

	}

	/**
	 * 執行流程
	 * 
	 * @throws InterruptedException
	 */
	private void ftpProcessHandler(String source) throws InterruptedException {
		logger.info(" ftpProcessHandler startCircle...." + source);
		// #.pnp.bigswitch = 0(停止排程) 1(停止排程，並轉發SMS) 其他(正常運行)
		int bigSwitch = CoreConfigReader.getInteger(CONFIG_STR.PNP_BIGSWITCH, true, false);

		if (0 == bigSwitch) { // 1.1.1 0(停止排程)
			logger.info("ftpProcessHandler PNP PNP_BIGSWITCH is : " + bigSwitch);
			logger.info("ftpProcessHandler STOP process !!");
			return;
		} else if (1 == bigSwitch) {// 1.1.2 1(停止排程，並轉發SMS)
			// 1.1 將檔案rename(加L)放到SMS指定路徑
			logger.info("ftpProcessHandler PNP PNP_BIGSWITCH is : " + bigSwitch);
			logger.info("ftpProcessHandler start put file to SMS FTP path process !!");
			// 因為路徑可用UI換，所以每次都要重拿連線資訊
			transFileToSMSFlow(source);
			// log
			logger.info("transFileToSMSFlow complate!");
			
			return;
		} else {
			// 1.3 解析資料存到DB
			praseDataFlow(source);
		}
	}

	/**
	 * 執行解析檔案資料到DB流程
	 * 
	 * @throws InterruptedException
	 */
	private void praseDataFlow(String source) throws InterruptedException {

		// 跟據來源不同取各自連線資訊
		PNPFtpSetting pnpFtpSetting = pnpFtpService.getFtpSettings(source);

		logger.info(" source...." + pnpFtpSetting.getChannelId());
		logger.info(" ftpServerName...." + pnpFtpSetting.getServerHostName());
		logger.info(" ftpServerNamePort...." + pnpFtpSetting.getServerHostNamePort());
		logger.info(" ftphost...." + pnpFtpSetting.getHost());
		logger.info(" ftpPort...." + pnpFtpSetting.getPort());
		logger.info(" ftpUsr...." + pnpFtpSetting.getAccount());
		logger.info(" ftpPass...." + pnpFtpSetting.getPassword());
		logger.info(" downloadSavePath...." + pnpFtpSetting.getDownloadSavePath());
		logger.info(" downloadPath...." + pnpFtpSetting.getPath());
		logger.info(" uploadPath...." + pnpFtpSetting.getUploadPath());

//		if (!validateFtpHostData(ftpServerName, ftpPort, ftpUsr, ftpPass, downloadSavePath)) {
//			return;
//		}
//
//		FTPClient ftpClient = new FTPClient();
	try {
//			ftpClient.connect(ftpServerName, ftpPort);
//			boolean loginResult = ftpClient.login(ftpUsr, ftpPass);
//			logger.info(loginResult ? "ftp login success!!" : "ftp login fail!!");
//			ftpClient.enterLocalPassiveMode();
//			ftpClient.changeWorkingDirectory(downloadPath);//
//			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//			ftpClient.setAutodetectUTF8(true);
//			ftpClient.setControlEncoding("UTF-8");
//			ftpClient.setStrictReplyParsing(false);// 新加設定解決org.apache.commons.net.MalformedServerReplyException:
//													// Truncated server reply: ).
//
//			// 取得FTP中的files
//			FTPFile[] files = ftpClient.listFiles();
//			List<String> handleFiles = new ArrayList<String>();
//			for (FTPFile file : files) {
//				String fileName = file.getName();
//				if (!file.isDirectory() && fileName.endsWith("txt")) {
//					handleFiles.add(fileName);
//				}
//			}

//			String fileExtension = CoreConfigReader.getString(CONFIG_STR.BN_FTP_FILE_EXTENSION, true);
		
			// 1. ftp get file
			Map<String,byte[]> lReturnDatas = new HashMap<String,byte[]>();
			Map<String,byte[]> returnDatas = pnpFtpService.downloadMutipleFileByType(source , pnpFtpSetting.getPath(), "TXT", pnpFtpSetting);
			pnpFtpSetting.clearFileNames();
			for(String fileName : returnDatas.keySet()) {
				pnpFtpSetting.addFileNames(fileName);
				logger.info(" FTP--" + pnpFtpSetting.getChannelId() + ":" + fileName );
			}
			lReturnDatas.putAll(returnDatas);
		
			// 2. prase data to object
			List<Object> mains = new ArrayList<Object>();
			
			for (String fileName : lReturnDatas.keySet()) {
				String encoding = "UTF-8";
				if (pnpFtpSetting.containsFileName(fileName)) {
					encoding = pnpFtpSetting.getFileEncoding();
					logger.info(fileName + " encoding:" +  encoding );
				}
				byte[] fileData = lReturnDatas.get(fileName);
				logger.info(" LoadFtbPnpDataTask handle file:" + pnpFtpSetting.getDownloadSavePath() + File.separator + fileName);
				File targetFile = new File(pnpFtpSetting.getDownloadSavePath() + File.separator + fileName);
				FileUtils.writeByteArrayToFile(targetFile, fileData);
//				FileUtils.copyInputStreamToFile(inputStream, targetFile);
				InputStream targetStream = new FileInputStream(targetFile);

				// 1.2白名單檢核檔名上的帳號是否與來源對應
				if (validateWhiteListAccountPccode(source, fileName)) {// TODO
					List<String> fileContents = IOUtils.readLines(targetStream, encoding);
					
					// 依來源解成各格式
					List<Object> pnpMains = parseFtpFile(source, fileName, fileContents);
					if (pnpMains != null) {
						mains.addAll(pnpMains);
					}
					targetStream.close();
				} else {// 1.2.1 白名單檢核錯誤
						// 1.1 將檔案rename(加L)放到SMS只定路徑
						// 因為路徑可用UI換，所以每次都要重拿連線資訊
					String changedOrigFileName = fileName.substring(0, fileName.lastIndexOf("_")) + "_L"
							+ fileName.substring(fileName.lastIndexOf("_"));
					uploadFileToSMS(source, targetStream,changedOrigFileName);
				}
			}
			// 3. save data to db = Status = DRAFT
			// 資料來源不同存入不同DB
			switch (source) {
			case AbstractPnpMainEntity.SOURCE_MITAKE:
				procMitakeData(mains);
				break;
			case AbstractPnpMainEntity.SOURCE_MING:
				procMingData(mains);
				break;
			case AbstractPnpMainEntity.SOURCE_EVERY8D:
				procEvery8dData(mains);
				break;
			case AbstractPnpMainEntity.SOURCE_UNICA:
				procUnicaData(mains);
				break;
			}

			// 4. remove file
			if (!lReturnDatas.isEmpty()) {
				pnpFtpService.deleteFileByType(pnpFtpSetting.getPath(), pnpFtpSetting.getFileNames().toArray(new String[0]), pnpFtpSetting);
			}

		} catch (Exception ex) {
			logger.error("Error: " + ex.getMessage());
			logger.error(ex);
			ex.printStackTrace();
		} finally {
			
		}
	}

	/**
	 * 執行將檔案轉移到SMS FTP流程
	 * 
	 * @throws InterruptedException
	 */
	private void transFileToSMSFlow(String source) {

		logger.info("start transfer File To SMS path");
		PNPFtpSetting pnpFtpSetting = pnpFtpService.getFtpSettings(source);
		try {
		// 1. ftp get file
			Map<String,byte[]> lReturnDatas = new HashMap<String,byte[]>();
			Map<String,byte[]> returnDatas = pnpFtpService.downloadMutipleFileByType(source , pnpFtpSetting.getPath(), "txt", pnpFtpSetting);
			pnpFtpSetting.clearFileNames();
			for(String fileName : returnDatas.keySet()) {
				pnpFtpSetting.addFileNames(fileName);
				logger.info(" FTP--" + pnpFtpSetting.getChannelId() + ":" + fileName );
			}
			lReturnDatas.putAll(returnDatas);
		
			// 2. prase data to object
			List<Object> mains = new ArrayList<Object>();
			
			for (String fileName : lReturnDatas.keySet()) {
				String encoding = pnpFtpSetting.getFileEncoding();
				if (pnpFtpSetting.containsFileName(fileName)) {
					encoding = pnpFtpSetting.getFileEncoding();
					logger.info(fileName + " encoding:" +  encoding );
				}
				byte[] fileData = lReturnDatas.get(fileName);
				logger.info(" LoadFtbPnpDataTask handle file:" + pnpFtpSetting.getDownloadSavePath() + File.separator + fileName);
				File targetFile = new File(pnpFtpSetting.getDownloadSavePath() + File.separator + fileName);
				FileUtils.writeByteArrayToFile(targetFile, fileData);
//						FileUtils.copyInputStreamToFile(inputStream, targetFile);
				InputStream targetStream = new FileInputStream(targetFile);
				// 1.1 將檔案rename(加L)放到SMS只定路徑
				// 因為路徑可用UI換，所以每次都要重拿連線資訊
				String changedOrigFileName = fileName.substring(0, fileName.lastIndexOf("_")) + "_L"
						+ fileName.substring(fileName.lastIndexOf("_"));
				uploadFileToSMS(source, targetStream,changedOrigFileName);

				logger.info("uploadFileToSMS : "+changedOrigFileName);
			}

			// 4. remove file
			if (!lReturnDatas.isEmpty()) {
					pnpFtpService.deleteFileByType(pnpFtpSetting.getPath(), pnpFtpSetting.getFileNames().toArray(new String[0]), pnpFtpSetting);
			}

		} catch (Exception ex) {
			logger.error("Error: " + ex.getMessage());
			logger.error(ex);
			ex.printStackTrace();
		} 
	}

	private void procMitakeData(List<Object> mains) {
		 for (Object pnpMainMitake : mains) {
			 saveMitakeDB(pnpMainMitake);
		 }
		 // Status = DRAFT --> Retry or WAIT
		 for (Object pnpMainMitake : mains) {
			 updateMitakeMainStatus(pnpMainMitake);
		 }
	}

	private void procEvery8dData(List<Object> mains) {
		for (Object pnpMainEvery8d : mains) {
			saveEvery8dDB(pnpMainEvery8d);
		}
		 // Status = DRAFT --> WAIT
		 for (Object pnpMainEvery8d : mains) {
			 updateEvery8dMainStatus(pnpMainEvery8d);
		 }
	}

	private void procMingData(List<Object> mains) {
		for (Object pnpMainMing : mains) {
			saveMingDB(pnpMainMing);
		}
		 // Status = DRAFT --> Retry or WAIT
		 for (Object pnpMainMing : mains) {
			 updateMingMainStatus(pnpMainMing);
		 }
	}

	private void procUnicaData(List<Object> mains) {
		for (Object pnpMainUnica : mains) {
			saveUnicaDB(pnpMainUnica);
		}
		// Status = DRAFT --> WAIT
		for (Object pnpMainUnica : mains) {
		    updateUnicaMainStatus(pnpMainUnica);
		}
	}

	private List<? super PnpDetail> parsePnpDetailMing(List<String> fileContents) throws Exception {
		List<? super PnpDetail> details = new ArrayList();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 明宣沒有header所以從0開始
		for (int i = 0; i < fileContents.size(); i++) {
			if (StringUtils.isNotBlank(fileContents.get(i))) {
				String[] detailData = fileContents.get(i).split("\\;;", 9);
				if (detailData.length == 9) {
					PnpDetailMing detail = new PnpDetailMing();
					detail.setSN(detailData[0]);// SN 流水號
					detail.setPhone(detailData[1]);// 收訊人手機號碼，長度為20碼以內(格式為0933******或+886933******)
					detail.setPhoneHash(toSHA256(detailData[1]));
					detail.setMsg(detailData[2]);// Content 簡訊訊息內容
					detail.setDetailScheduleTime(detailData[3]);// 預約時間
					detail.setAccount1(detailData[4]);// 批次帳號1
					detail.setAccount2(detailData[5]);// 批次帳號2
					detail.setVariable1(detailData[6]);// Variable1 擴充欄位1(可為空值)
					detail.setVariable2(detailData[7]);// Variable2 擴充欄位2(可為空值)
					detail.setKeepSecond(detailData[8]);// 有效秒數
					details.add(detail);
				} else {
					logger.error("parsePnpDetailMing error Data:" + detailData);
				}
			}
		}

		return details;
	}

	private List<? super PnpDetail> parsePnpDetailMitake(List<String> fileContents) throws Exception {
		List<? super PnpDetail> details = new ArrayList();
		for (int i = 1; i < fileContents.size(); i++) {
			if (StringUtils.isNotBlank(fileContents.get(i))) {
				String[] detailData = fileContents.get(i).split("\\&", 4);
				if (detailData.length == 4) {
					PnpDetailMitake detail = new PnpDetailMitake();
					detail.setDestCategory(detailData[0]);//DestCategory 掛帳代碼
					detail.setDestName(detailData[1]);// DestName  請填入系統有意義之流水號(open端可辯示之唯一序號)
					detail.setPhone(detailData[2]);// DestNo 手機門號/請填入09帶頭的手機號碼。  
					detail.setPhoneHash(toSHA256(detailData[2]));
					detail.setMsg(detailData[3]);// MsgData 簡訊訊息內容
					details.add(detail);
				} else {
					logger.error("parsePnpDetailMitake error Data:" + detailData);
				}
			}
		}
		
		return details;
	}
	
	private List<? super PnpDetail> parsePnpDetailEvery8d(List<String> fileContents) throws Exception {
		List<? super PnpDetail> details = new ArrayList();
		for (int i = 1; i < fileContents.size(); i++) {
			if (StringUtils.isNotBlank(fileContents.get(i))) {
				String[] detailData = fileContents.get(i).split("\\&", 10);
				if (detailData.length == 10) {
					PnpDetailEvery8d detail = new PnpDetailEvery8d();
					detail.setSN(detailData[0]);// SN 名單流水號
					detail.setDestName(detailData[1]);// DestName 收件者名稱
					detail.setPhone(detailData[2]);// DestNo 收訊人手機號碼，長度為20碼以內(格式為0933******或+886933******)
					detail.setPhoneHash(toSHA256(detailData[2]));
					detail.setMsg(detailData[3]);// Content 簡訊訊息內容
					detail.setPID(detailData[4]);// PID 身份字號
					detail.setCampaignID(detailData[5]);// CampaignID 行銷活動代碼(可為空值)
					detail.setSegmentID(detailData[6]);// SegmentID 客群代號(可為空值)
					detail.setProgramID(detailData[7]);// ProgramID 階段代號(可為空值)
					detail.setVariable1(detailData[8]);// Variable1 擴充欄位1(可為空值)
					detail.setVariable2(detailData[9]);// Variable2 擴充欄位2(可為空值)
					details.add(detail);
				} else {
					logger.error("parsePnpDetailEvery8d error Data:" + detailData);
				}
			}
		}

		return details;
	}
	
	private List<? super PnpDetail> parsePnpDetailUnica(List<String> fileContents) throws Exception {
		List<? super PnpDetail> details = new ArrayList();
		for (int i = 1; i < fileContents.size(); i++) {
			if (StringUtils.isNotBlank(fileContents.get(i))) {
				String[] detailData = fileContents.get(i).split("\\&", 10);
				if (detailData.length == 10) {
					PnpDetailUnica detail = new PnpDetailUnica();
					detail.setSN(detailData[0]);// SN 名單流水號
					detail.setDestName(detailData[1]);// DestName 收件者名稱
					detail.setPhone(detailData[2]);// DestNo 收訊人手機號碼，長度為20碼以內(格式為0933******或+886933******)
					detail.setPhoneHash(toSHA256(detailData[2]));
					detail.setMsg(detailData[3]);// Content 簡訊訊息內容
					detail.setPID(detailData[4]);// PID 身份字號
					detail.setCampaignID(detailData[5]);// CampaignID 行銷活動代碼(可為空值)
					detail.setSegmentID(detailData[6]);// SegmentID 客群代號(可為空值)
					detail.setProgramID(detailData[7]);// ProgramID 階段代號(可為空值)
					detail.setVariable1(detailData[8]);// Variable1 擴充欄位1(可為空值)
					detail.setVariable2(detailData[9]);// Variable2 擴充欄位2(可為空值)
					details.add(detail);
				} else {
					logger.error("parsePnpDetailUnica error Data:" + detailData);
				}
			}
		}
		
		return details;
	}

	/**
	 * 整理txt data
	 * 
	 * @param origFileName
	 * @param fileContents
	 * @throws Exception
	 */
	private List<Object> parseFtpFile(String source, String origFileName, List<String> fileContents) throws Exception {
		switch (source) {
		case AbstractPnpMainEntity.SOURCE_MITAKE:
			return praseMitakeFiles(origFileName, fileContents);
		case AbstractPnpMainEntity.SOURCE_MING:
			return praseMingFiles(origFileName, fileContents);
		case AbstractPnpMainEntity.SOURCE_EVERY8D:
			return praseEvery8DFiles(origFileName, fileContents);
		case AbstractPnpMainEntity.SOURCE_UNICA:
			return praseUnicaFiles(origFileName, fileContents);
		}
		return null;
	}

	private List<Object> praseMingFiles(String origFileName, List<String> fileContents) throws Exception {

		List<Object> mains = new ArrayList<Object>();

		if (!fileContents.isEmpty()) {
			// 明宣沒有header
			// String header = fileContents.get(0);
			// String[] splitHeaderData = header.split("\\&",7);
			// String Subject = splitHeaderData[0];
			// String UserID = splitHeaderData[1];
			// String Password = splitHeaderData[2];
			// String OrderTime = splitHeaderData[3];
			// String ExprieTime = splitHeaderData[4];
			// String MsgType = splitHeaderData[5];
			// String BatchID = splitHeaderData[6];//簡訊平台保留欄位，請勿填入資料
			PnpMainMing pnpMainMing = new PnpMainMing();
			// pnpMainEvery8d.setGroupId(new Long(mains.size() + 1)); 分堆用
			pnpMainMing.setOrigFileName(origFileName);
			pnpMainMing.setSource(AbstractPnpMainEntity.SOURCE_MING);
			pnpMainMing.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
			pnpMainMing.setProcFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_MING, true, false));
			pnpMainMing.setProcStage("BC");
			// 明宣預約時間散在各detail中
			// String sendType = StringUtils.isBlank(OrderTime) ?
			// AbstractPnpMainEntity.SEND_TYPE_IMMEDIATE :
			// AbstractPnpMainEntity.SEND_TYPE_DELAY;
			String sendType = AbstractPnpMainEntity.SEND_TYPE_IMMEDIATE;//TODO 
			pnpMainMing.setSendType(sendType);
			// //原生欄位
			// pnpMainMing.setSubject(Subject);
			// pnpMainMing.setUserID(UserID);
			// pnpMainMing.setPassword(Password);
			// pnpMainMing.setOrderTime(OrderTime);
			// pnpMainMing.setExprieTime(ExprieTime);
			// pnpMainMing.setMsgType(MsgType);
			// pnpMainMing.setBatchID(BatchID);
			// pnpMainMing.setTempId(templates.get(0).getTemplateId());
			// if (sendType.equals(AbstractPnpMainEntity.SEND_TYPE_DELAY)) {
			// pnpMainMing.setScheduleTime(OrderTime);
			// }

			pnpMainMing.setPnpDetails(parsePnpDetailMing(fileContents));
			mains.add(pnpMainMing);
		} else {
			logger.error("praseMingFiles fileContents.isEmpty");
		}
		return mains;
	}

	private List<Object> praseMitakeFiles(String origFileName, List<String> fileContents) throws Exception {
		
		List<Object> mains = new ArrayList<Object>();
		
		if (!fileContents.isEmpty()) {
			String header = fileContents.get(0);
			String[] splitHeaderData = header.split("\\&", 6);
			String groupID = splitHeaderData[0];
			String username = splitHeaderData[1];
			String userPassword = splitHeaderData[2];
			String orderTime = splitHeaderData[3];
			String validityTime = splitHeaderData[4];
			String msgType = splitHeaderData[5];
			PnpMainMitake pnpMainMitake = new PnpMainMitake();
			// pnpMainMitake.setGroupId(new Long(mains.size() + 1)); 分堆用
			pnpMainMitake.setOrigFileName(origFileName);
			pnpMainMitake.setSource(AbstractPnpMainEntity.SOURCE_MITAKE);
			pnpMainMitake.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
			pnpMainMitake.setProcFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_MITAKE, true, false));
			pnpMainMitake.setProcStage("BC");
			String sendType = StringUtils.isBlank(orderTime) ? AbstractPnpMainEntity.SEND_TYPE_IMMEDIATE
					: AbstractPnpMainEntity.SEND_TYPE_DELAY;
			pnpMainMitake.setSendType(sendType);
			// 原生欄位
			pnpMainMitake.setGroupIDSource(groupID);
			pnpMainMitake.setUsername(username);
			pnpMainMitake.setUserPassword(userPassword);
			pnpMainMitake.setOrderTime(orderTime);
			pnpMainMitake.setValidityTime(validityTime);
			pnpMainMitake.setMsgType(msgType);
			
			// pnpMainMitake.setTempId(templates.get(0).getTemplateId());
			if (sendType.equals(AbstractPnpMainEntity.SEND_TYPE_DELAY)) {
				pnpMainMitake.setScheduleTime(orderTime);
			}
			
			pnpMainMitake.setPnpDetails(parsePnpDetailMitake(fileContents));
			mains.add(pnpMainMitake);
		} else {
			logger.error("praseMitakeFiles fileContents.isEmpty");
		}
		return mains;
	}
	
	private List<Object> praseEvery8DFiles(String origFileName, List<String> fileContents) throws Exception {

		List<Object> mains = new ArrayList<Object>();

		if (!fileContents.isEmpty()) {
			String header = fileContents.get(0);
			String[] splitHeaderData = header.split("\\&", 7);
			String Subject = splitHeaderData[0];
			String UserID = splitHeaderData[1];
			String Password = splitHeaderData[2];
			String OrderTime = splitHeaderData[3];
			String ExprieTime = splitHeaderData[4];
			String MsgType = splitHeaderData[5];
			String BatchID = splitHeaderData[6];// 簡訊平台保留欄位，請勿填入資料
			PnpMainEvery8d pnpMainEvery8d = new PnpMainEvery8d();
			// pnpMainEvery8d.setGroupId(new Long(mains.size() + 1)); 分堆用
			pnpMainEvery8d.setOrigFileName(origFileName);
			pnpMainEvery8d.setSource(AbstractPnpMainEntity.SOURCE_EVERY8D);
			pnpMainEvery8d.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
			pnpMainEvery8d.setProcFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_EVERY8D, true, false));
			pnpMainEvery8d.setProcStage("BC");
			String sendType = StringUtils.isBlank(OrderTime) ? AbstractPnpMainEntity.SEND_TYPE_IMMEDIATE
					: AbstractPnpMainEntity.SEND_TYPE_DELAY;
			pnpMainEvery8d.setSendType(sendType);
			// 原生欄位
			pnpMainEvery8d.setSubject(Subject);
			pnpMainEvery8d.setUserID(UserID);
			pnpMainEvery8d.setPassword(Password);
			pnpMainEvery8d.setOrderTime(OrderTime);
			pnpMainEvery8d.setExprieTime(ExprieTime);
			pnpMainEvery8d.setMsgType(MsgType);
			pnpMainEvery8d.setBatchID(BatchID);
			// pnpMainEvery8d.setTempId(templates.get(0).getTemplateId());
			if (sendType.equals(AbstractPnpMainEntity.SEND_TYPE_DELAY)) {
				pnpMainEvery8d.setScheduleTime(OrderTime);
			}

			pnpMainEvery8d.setPnpDetails(parsePnpDetailEvery8d(fileContents));
			mains.add(pnpMainEvery8d);
		} else {
			logger.error("praseEvery8DFiles fileContents.isEmpty");
		}
		return mains;
	}

	private List<Object> praseUnicaFiles(String origFileName, List<String> fileContents) throws Exception {

		List<Object> mains = new ArrayList<Object>();

		if (!fileContents.isEmpty()) {
			String header = fileContents.get(0);
			String[] splitHeaderData = header.split("\\&", 7);
			String Subject = splitHeaderData[0];
			String UserID = splitHeaderData[1];
			String Password = splitHeaderData[2];
			String OrderTime = splitHeaderData[3];
			String ExprieTime = splitHeaderData[4];
			String MsgType = splitHeaderData[5];
			String BatchID = splitHeaderData[6];// 簡訊平台保留欄位，請勿填入資料
			PnpMainUnica pnpMainUnica = new PnpMainUnica();
			// pnpMainUnica.setGroupId(new Long(mains.size() + 1)); 分堆用
			pnpMainUnica.setOrigFileName(origFileName);
			pnpMainUnica.setSource(AbstractPnpMainEntity.SOURCE_UNICA);
			pnpMainUnica.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
			pnpMainUnica.setProcFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_UNICA, true, false));
			pnpMainUnica.setProcStage("BC");
			String sendType = StringUtils.isBlank(OrderTime) ? AbstractPnpMainEntity.SEND_TYPE_IMMEDIATE
					: AbstractPnpMainEntity.SEND_TYPE_DELAY;
			pnpMainUnica.setSendType(sendType);
			// 原生欄位
			pnpMainUnica.setSubject(Subject);
			pnpMainUnica.setUserID(UserID);
			pnpMainUnica.setPassword(Password);
			pnpMainUnica.setOrderTime(OrderTime);
			pnpMainUnica.setExprieTime(ExprieTime);
			pnpMainUnica.setMsgType(MsgType);
			pnpMainUnica.setBatchID(BatchID);
			// pnpMainUnica.setTempId(templates.get(0).getTemplateId());
			if (sendType.equals(AbstractPnpMainEntity.SEND_TYPE_DELAY)) {
				pnpMainUnica.setScheduleTime(OrderTime);
			}

			pnpMainUnica.setPnpDetails(parsePnpDetailUnica(fileContents));
			mains.add(pnpMainUnica);
		} else {
			logger.error("praseUnicaFiles fileContents.isEmpty");
		}
		return mains;
	}
	
	/**
	 * save db Status = DRAFT
	 * 
	 * @param origFileName
	 * @param fileContents
	 */
	@Transactional(rollbackFor = Exception.class, timeout = 3000)
	private void saveMitakeDB(Object sourceMain) {
		PnpMainMitake pnpMainMitake = (PnpMainMitake) sourceMain;
		List<? super PnpDetail> originalDetails = pnpMainMitake.getPnpDetails();
		logger.info(" saveMitakeDB MitakeDetails size:" + originalDetails.size());
		pnpMainMitake.setOrigFileName(pnpMainMitake.getOrigFileName().replace(".ok", ""));
		pnpMainMitake = pnpMainMitakeRepository.save(pnpMainMitake);
		List<PnpDetailMitake> details = new ArrayList<>();
		for (Object detail : originalDetails) {
			PnpDetailMitake pnpDetailMitake = (PnpDetailMitake)detail;
			pnpDetailMitake.setPnpMainId(pnpMainMitake.getPnpMainId());
			pnpDetailMitake.setProcFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_MITAKE, true, false));
			pnpDetailMitake.setProcStage(AbstractPnpMainEntity.STAGE_BC);
			pnpDetailMitake.setSource(AbstractPnpMainEntity.SOURCE_MITAKE);
			pnpDetailMitake.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
			details.add(pnpDetailMitake);
		}
		if (!details.isEmpty()) {
			pnpDetailMitakeRepositoryCustom.batchInsertPnpDetailMitake(details);
		}
	}
	
	
	
	/**
	 * save db Status = DRAFT
	 * 
	 * @param origFileName
	 * @param fileContents
	 */
	@Transactional(rollbackFor = Exception.class, timeout = 3000)
	private void saveUnicaDB(Object sourceMain) {
		PnpMainUnica pnpMainUnica = (PnpMainUnica) sourceMain;
		List<? super PnpDetail> originalDetails = pnpMainUnica.getPnpDetails();
		logger.info(" saveEvety8dDB UnicaDetails size:" + originalDetails.size());
		
		pnpMainUnica = pnpMainUnicaRepository.save(pnpMainUnica);
		List<PnpDetailUnica> details = new ArrayList<>();
		for (Object detail : originalDetails) {
			PnpDetailUnica pnpDetailUnica = (PnpDetailUnica)detail;
			pnpDetailUnica.setPnpMainId(pnpMainUnica.getPnpMainId());
			pnpDetailUnica.setProcFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_UNICA, true, false));
			pnpDetailUnica.setProcStage(AbstractPnpMainEntity.STAGE_BC);
			pnpDetailUnica.setSource(AbstractPnpMainEntity.SOURCE_UNICA);
			pnpDetailUnica.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
			details.add(pnpDetailUnica);
		}
		if (!details.isEmpty()) {
			pnpDetailUnicaRepositoryCustom.batchInsertPnpDetailUnica(details);
		}
	}
	
	
	/**
	 * save db Status = DRAFT
	 * 
	 * @param origFileName
	 * @param fileContents
	 */
	@Transactional(rollbackFor = Exception.class, timeout = 3000)
	private void saveEvery8dDB(Object sourceMain) {
		PnpMainEvery8d pnpMainEvery8d = (PnpMainEvery8d) sourceMain;
		List<? super PnpDetail> originalDetails = pnpMainEvery8d.getPnpDetails();
		logger.info(" saveEvety8dDB Every8dDetails size:" + originalDetails.size());

		pnpMainEvery8d = pnpMainEvery8dRepository.save(pnpMainEvery8d);
		List<PnpDetailEvery8d> details = new ArrayList<>();
		for (Object detail : originalDetails) {
			PnpDetailEvery8d pnpDetailEvery8d =(PnpDetailEvery8d)detail;
			pnpDetailEvery8d.setPnpMainId(pnpMainEvery8d.getPnpMainId());
			pnpDetailEvery8d.setProcFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_EVERY8D, true, false));
			pnpDetailEvery8d.setProcStage(AbstractPnpMainEntity.STAGE_BC);
			pnpDetailEvery8d.setSource(AbstractPnpMainEntity.SOURCE_EVERY8D);
			pnpDetailEvery8d.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
			details.add(pnpDetailEvery8d);
		}
		if (!details.isEmpty()) {
			pnpDetailEvery8dRepositoryCustom.batchInsertPnpDetailEvery8d(details);
		}
	}
	
	

	/**
	 * save db Status = DRAFT
	 * 
	 * @param origFileName
	 * @param fileContents
	 */
	@Transactional(rollbackFor = Exception.class, timeout = 3000)
	private void saveMingDB(Object sourceMain) {
		PnpMainMing pnpMainMing = (PnpMainMing) sourceMain;
		List<? super PnpDetail> originalDetails = pnpMainMing.getPnpDetails();
		logger.info(" saveMingDB MingDetails size:" + originalDetails.size());
		pnpMainMing.setOrigFileName(pnpMainMing.getOrigFileName().replace(".ok", ""));
		pnpMainMing = pnpMainMingRepository.save(pnpMainMing);
		logger.info(" saveMingDB pnpMainMing id:" + pnpMainMing.getPnpMainId());
		List<PnpDetailMing> details = new ArrayList<>();
		for (Object detail : originalDetails) {
			PnpDetailMing pnpDetail = (PnpDetailMing)detail;
			pnpDetail.setPnpMainId(pnpMainMing.getPnpMainId());
			pnpDetail.setProcFlow(CoreConfigReader.getString(CONFIG_STR.PNP_PROC_FLOW_MING, true, false));
			pnpDetail.setProcStage(AbstractPnpMainEntity.STAGE_BC);
			pnpDetail.setSource(AbstractPnpMainEntity.SOURCE_MING);
			pnpDetail.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
			details.add(pnpDetail);
		}
		if (!details.isEmpty()) {
			pnpDetailMingRepositoryCustom.batchInsertPnpDetailMing(details);
		}
	}
	
	
	/**
	 * 三竹
	 * 資料解析完狀態改為retry or wait
	 * @param
	 */
	 @Transactional(rollbackFor=Exception.class, timeout = 3000)
	 private void updateMitakeMainStatus(Object sourceMain) {
		PnpMainMitake pnpMainMitake = (PnpMainMitake)sourceMain;
		Long mainId = pnpMainMitake.getPnpMainId();
		String status = AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT;
		Date now = Calendar.getInstance().getTime();
		pnpMainMitakeRepository.updatePnpMainMitakeStatus(status,now,mainId);
		pnpDetailMitakeRepository.updateStatusByMainId(status,now, mainId);
	 }
	
	 /**
	  * 互動
	  * 資料解析完狀態改為retry or wait
	  * @param
	  */
	 @Transactional(rollbackFor=Exception.class, timeout = 3000)
	 private void updateEvery8dMainStatus(Object sourceMain) {
		 PnpMainEvery8d pnpMainEvery8d = (PnpMainEvery8d)sourceMain;
		 Long mainId = pnpMainEvery8d.getPnpMainId();
		 String status = AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT;
		 Date now = Calendar.getInstance().getTime();
		 pnpMainEvery8dRepository.updatePnpMainEvery8dStatus(status,now,mainId);
		 pnpDetailEvery8dRepository.updateStatusByMainId(status,now, mainId);
	 }
	 
	 /**
	  * 互動
	  * 資料解析完狀態改為retry or wait
	  * @param
	  */
	 @Transactional(rollbackFor=Exception.class, timeout = 3000)
	 private void updateUnicaMainStatus(Object sourceMain) {
		 PnpMainUnica pnpMainUnica = (PnpMainUnica)sourceMain;
		 Long mainId = pnpMainUnica.getPnpMainId();
		 String status = AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT;
		 Date now = Calendar.getInstance().getTime();
		 pnpMainUnicaRepository.updatePnpMainUnicaStatus(status,now,mainId);
		 pnpDetailUnicaRepository.updateStatusByMainId(status,now, mainId);
	 }
	 
	 /**
	  * 明宣
	  * 資料解析完狀態改為retry or wait
	  * @param
	  */
	 @Transactional(rollbackFor=Exception.class, timeout = 3000)
	 private void updateMingMainStatus(Object sourceMain) {
		 PnpMainMing pnpMainMing = (PnpMainMing)sourceMain;
		 Long mainId = pnpMainMing.getPnpMainId();
		 String status = AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT;
		 Date now = Calendar.getInstance().getTime();
		 pnpMainMingRepository.updatePnpMainMingStatus(status,now,mainId);
		 pnpDetailMingRepository.updateStatusByMainId(status,now, mainId);
	 }

	/**
	 * check header
	 * 
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
			String scheduleTime = splitData[2];
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
	 * 檢查ftpserver setting
	 * 
	 * @return
	 */
	private boolean validateFtpHostData(String ftpServerName, int ftpPort, String ftpUsr, String ftpPass,
			String downloadSavePath) {
		if (ftpPort == -1) {
			return false;
		}
		if (StringUtils.isBlank(ftpServerName) || StringUtils.isBlank(downloadSavePath)) {
			return false;
		}
		if (StringUtils.isBlank(ftpUsr) || StringUtils.isBlank(ftpPass)) {
			return false;
		}
		return true;
	}

	// 白名單帳號檢核，比對檔名上的帳號與白名單設定上的帳號是否相同
	private boolean validateWhiteListAccountPccode(String source, String fileName) {
		String whiteListAccountPccode = getSourceWhiteListAccountPccode(source);
		// String fileNameAccountPccode =
		// fileName.substring(9,21);//ex:O_PRMSMS_25OCSPENDING_YYYYMMDDhhmmss
		// TODO return whiteListAccountPccode.equals(fileNameAccountPccode);
		return true;
	}

	private String getSourceWhiteListAccountPccode(String source) {
		String ACCOUNT_PCCCODE = null;
		switch (source) {
		case AbstractPnpMainEntity.SOURCE_MITAKE:
			ACCOUNT_PCCCODE = CoreConfigReader.getString(CONFIG_STR.PNP_WHITELIST_ACCOUNT_PCCCODE_MITAKE, true, false);
			break;
		case AbstractPnpMainEntity.SOURCE_MING:
			ACCOUNT_PCCCODE = CoreConfigReader.getString(CONFIG_STR.PNP_WHITELIST_ACCOUNT_PCCCODE_MING, true, false);
			break;
		case AbstractPnpMainEntity.SOURCE_EVERY8D:
			ACCOUNT_PCCCODE = CoreConfigReader.getString(CONFIG_STR.PNP_WHITELIST_ACCOUNT_PCCCODE_EVERY8D, true, false);
			break;
		case AbstractPnpMainEntity.SOURCE_UNICA:
			ACCOUNT_PCCCODE = CoreConfigReader.getString(CONFIG_STR.PNP_WHITELIST_ACCOUNT_PCCCODE_UNICA, true, false);
			break;
		}
		return ACCOUNT_PCCCODE;
	}

	public String toSHA256(String phone) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");

		if ("0".equals(phone.substring(0, 1))) {// phone有些可能已轉成e.164格式，0開頭的再做轉換
			phone = "+886" + phone.substring(1);// 改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
		}

		byte[] hash = digest.digest(phone.getBytes(StandardCharsets.UTF_8));

		StringBuffer hexString = new StringBuffer();

		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}

		return hexString.toString();
	}

	public void uploadFileToSMS(String source ,InputStream targetStream, String fileName) throws IOException {
		logger.info("start uploadFileToSMS ");
		
		PNPFtpSetting setting = pnpFtpService.getFtpSettings(source);
		
		logger.info(" fileName...."+fileName);
		
		pnpFtpService.uploadFileByType(targetStream, fileName, setting.getUploadPath(), setting);
	}

	private static class ExecuteSendPnpRunnable implements Runnable {

		private Long pnpMainId;

		public ExecuteSendPnpRunnable(Long pnpMainId) {
			this.pnpMainId = pnpMainId;
		}

		@Override
		public void run() {
			try {
				if (pnpMainId != null) {
					ExecuteSendPnpTask task = new ExecuteSendPnpTask();
					task.executeSendPnp(pnpMainId);
				}
			} catch (Exception e) {
				logger.error(ErrorRecord.recordError(e));
			}
		}
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
			logger.info(" LoadFtbPnpDataTask cancel....");
		}

		if (scheduler != null && !scheduler.isShutdown()) {
			logger.info(" LoadFtbPnpDataTask shutdown....");
			scheduler.shutdown();
		}

	}

}
