package com.bcs.core.taishin.circle.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.util.SystemOutLogger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SystemPropertyUtils;

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.RecordReport;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.RecordReportService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFtpService;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFtpSetting;
import com.bcs.core.taishin.circle.PNP.scheduler.LoadFtbPnpDataTask;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpPushMsgService;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpSMSMsgService;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.ftp.FtpSetting;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/spring-base.xml", "classpath:spring/spring-security.xml"})

public class PNPTester {

	@Autowired
	private LineUserService lineUserService;
	
	@Autowired
	private PnpRepositoryCustom pnpRepositoryCustom;
	
	@Autowired
	private PnpPushMsgService pnpPushMsgService;
	
	@Autowired
	private PnpService pnpService;
	
	@Autowired
	private PnpSMSMsgService pnpSMSMsgService;
	
	@Autowired
	private LoadFtbPnpDataTask loadFtbPnpDataTask;
	@Autowired
	private PNPFtpService pnpFtpService;
	
	
	/** Logger */
	private static Logger logger = Logger.getLogger(PNPTester.class);
	
	
	@Test
	public void getevery8dFTPFile_ftp() throws Exception {
		/*every8d 簡訊發送資料檔上傳完成後，必須再上傳一確認檔案確認已
		傳送完成。如上傳之發送資料檔為Msg1234.txt，在Msg1234.txt傳送
		完成後，請再傳送一檔名為Msg1234.txt.ok之檔案，檔案內容為空。
		如此可避免簡訊平台在檔案未傳送完成前即進行收檔作業，造成傳送
		錯誤。*/
		
		String ftpServerName = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_HOST_EVERY8D, true);
		int ftpPort = CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_PORT_EVERY8D, true);
		String ftpUsr = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_USR_EVERY8D, true);
		String ftpPass = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PASS_EVERY8D, true);
		String downloadSavePath = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_TO_LOCAL_PATH_EVERY8D, true);
		String downloadPath = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_PATH_EVERY8D, true);
		String uploadPath = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_UPLOAD_SMS_PATH_EVERY8D, true);
		
		
		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect(ftpServerName, ftpPort);
			boolean loginResult = ftpClient.login(ftpUsr, ftpPass);
			logger.info(loginResult ? "ftp login success!!" : "ftp login fail!!");
			ftpClient.enterLocalPassiveMode();
			ftpClient.changeWorkingDirectory(downloadPath);//
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.setAutodetectUTF8(true);
			ftpClient.setControlEncoding("UTF-8");
			ftpClient.setStrictReplyParsing(false);// 新加設定解決org.apache.commons.net.MalformedServerReplyException:
													// Truncated server reply: ).
		
		
			// 取得FTP中的files
			FTPFile[] files = ftpClient.listFiles();
			List<String> handleFiles = new ArrayList<String>();
			for (FTPFile file : files) {
				String fileName = file.getName();
				if (!file.isDirectory() && fileName.endsWith("txt.ok")) {
					fileName = fileName.substring(0, fileName.lastIndexOf("."));
					handleFiles.add(fileName);
				}
			}
			
			for(String file : handleFiles) {
				System.out.println(file);
			}
			
			
			// 4. remove file
			for (String procfileName : handleFiles) {
				boolean success = ftpClient.deleteFile(procfileName);
				if (!success) {
					logger.error("remove fail: " + procfileName);
				}
				success = ftpClient.deleteFile(procfileName+".ok");
				if (!success) {
					logger.error("remove fail: " + procfileName+".ok");
				}
			}
			
			
		} catch (Exception ex) {
			logger.error("Error: " + ex.getMessage());
			logger.error(ex);
			ex.printStackTrace();
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error("Error: " + ex.getMessage());
			}
		}
	
	
	}

	@Test
	public void getevery8dFTPFile_sftp() throws Exception {
		/*every8d 簡訊發送資料檔上傳完成後，必須再上傳一確認檔案確認已
		傳送完成。如上傳之發送資料檔為Msg1234.txt，在Msg1234.txt傳送
		完成後，請再傳送一檔名為Msg1234.txt.ok之檔案，檔案內容為空。
		如此可避免簡訊平台在檔案未傳送完成前即進行收檔作業，造成傳送
		錯誤。*/
		
		PNPFtpSetting setting = pnpFtpService.getFtpSettings(AbstractPnpMainEntity.SOURCE_EVERY8D);
		
		logger.info(setting.getAccount());
		logger.info(setting.getPassword());
		logger.info(setting.getHost());
		logger.info(setting.getPort());
		logger.info(setting.getChannelId());
		
		
		Map<String, byte[]> lReturnDatas = new HashMap<String, byte[]>();
		ChannelSftp channelSftp = null;
		Session session = null;
		
		/**
		 *  三竹來源  SOURCE_MITAKE = "1";
		 *	互動來源  SOURCE_EVERY8D = "2";
		 *	明宣來源  SOURCE_MING = "3";
		 *	UNICA來源   SOURCE_UNICA = "4";
		 */
		String source = setting.getChannelId();//判斷來源 
		String pDirectory = setting.getPath();
		String extension = "txt";// TODO 加到PNPFtpSetting裡
		
		try {
			JSch jsch = new JSch();
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			session = jsch.getSession(setting.getAccount(), setting.getHost(), setting.getPort());
			session.setPassword(setting.getPassword());
			session.setConfig(sshConfig);
			session.connect();
			logger.info("session.isConnected() : "+session.isConnected());
			if (session.isConnected()) {
				channelSftp = (ChannelSftp) session.openChannel("sftp");
				channelSftp.connect();
				if (channelSftp.isConnected()) {
					channelSftp.cd(pDirectory);
					// 取得FTP中的files
					Vector<ChannelSftp.LsEntry> list = channelSftp.ls("*." + extension+".ok");
					for (ChannelSftp.LsEntry lFtpFile : list) {
						String fileName = lFtpFile.getFilename();
						logger.info("downloadMutipleFileInSFTPForDev fileName:" + fileName);
						ByteArrayOutputStream lDataTemp = new ByteArrayOutputStream();
						channelSftp.get(fileName, lDataTemp);
						lDataTemp.flush();
						lReturnDatas.put(fileName, lDataTemp.toByteArray());
						lDataTemp.close();
					}
				}else {
					logger.error(" downloadMutipleFileInSFTPForDev channelSftp isConnected faile ");
				}
				
			}else {
				logger.error(" downloadMutipleFileInSFTPForDev session isConnected faile ");
			}
			

		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(" downloadMutipleFileInSFTPForDev Error: " + ex.getMessage());
		} finally {
			try {
				if (channelSftp != null && channelSftp.isConnected()) {
					channelSftp.disconnect();
				}
				if (session != null && session.isConnected()) {
					session.disconnect();
				}
				channelSftp = null;
				session = null;
			} catch (Exception ex) {
				logger.error(" downloadMutipleFileInSFTPForDev Error: " + ex.getMessage());
			}
		}
		
		for (String fileName : lReturnDatas.keySet()) {
			System.out.println(fileName);
		}
		
		
		
	}
	
	@Test
	public void getFTPFile_ftp() throws Exception {
		/*every8d 簡訊發送資料檔上傳完成後，必須再上傳一確認檔案確認已
		傳送完成。如上傳之發送資料檔為Msg1234.txt，在Msg1234.txt傳送
		完成後，請再傳送一檔名為Msg1234.txt.ok之檔案，檔案內容為空。
		如此可避免簡訊平台在檔案未傳送完成前即進行收檔作業，造成傳送
		錯誤。*/
		
		String source = AbstractPnpMainEntity.SOURCE_MING;
		String ftpServerName = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_HOST_MING, true);
		int ftpPort = CoreConfigReader.getInteger(CONFIG_STR.PNP_FTP_PORT_MING, true);
		String ftpUsr = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_USR_MING, true);
		String ftpPass = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_PASS_MING, true);
		String downloadSavePath = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_TO_LOCAL_PATH_MING, true);
		String downloadPath = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_DOWNLOAD_PATH_MING, true);
		String uploadPath = CoreConfigReader.getString(CONFIG_STR.PNP_FTP_UPLOAD_SMS_PATH_MING, true);
		
		
		FTPClient ftpClient = new FTPClient();
		try {
			ftpClient.connect(ftpServerName, ftpPort);
			boolean loginResult = ftpClient.login(ftpUsr, ftpPass);
			logger.info(loginResult ? "ftp login success!!" : "ftp login fail!!");
			ftpClient.enterLocalPassiveMode();
			ftpClient.changeWorkingDirectory(downloadPath);//
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			ftpClient.setAutodetectUTF8(true);
			ftpClient.setControlEncoding("UTF-8");
			ftpClient.setStrictReplyParsing(false);// 新加設定解決org.apache.commons.net.MalformedServerReplyException:
													// Truncated server reply: ).
		
		
			// 取得FTP中的files
			FTPFile[] files = ftpClient.listFiles();
			List<String> handleFiles = new ArrayList<String>();
			
			if(!source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D) && !source.equals(AbstractPnpMainEntity.SOURCE_UNICA)){
				//三竹、明宣需要使用rename依現行簡訊平台檢核機制 若可以rename為.ok表示檔案上傳完畢即可抓走，若無法rename表示檔案正在上傳  >>by 志豪 20190422 mail【台新 Line PNP】相關問題
				for (FTPFile file : files) {
					String fileName = file.getName();
					if (!file.isDirectory() && fileName.endsWith("txt")) {
						logger.info("start rename!!!!");
						logger.info("fileName :"+fileName);
						logger.info("ftpClient.printWorkingDirectory() :"+ftpClient.printWorkingDirectory());
						logger.info(ftpClient.rename(fileName, fileName+".ok"));
					}
				}
				logger.info("ftpClient.printWorkingDirectory() :"+ftpClient.printWorkingDirectory());
				FTPFile[] filesOK = ftpClient.listFiles();
				for (FTPFile file : filesOK) {
					String fileName = file.getName();
					if (!file.isDirectory() && fileName.endsWith("txt.ok")) {
						logger.info("add to handleFiles!!!!");
						logger.info("fileName :"+fileName);
						handleFiles.add(fileName);
					}
				}
				
			}else {
				for (FTPFile file : files) {
					String fileName = file.getName();
					if (!file.isDirectory() && fileName.endsWith("txt.ok")) {
						fileName = fileName.substring(0, fileName.lastIndexOf("."));//ex:資料夾裡有123.txt 、123.txt.ok，要抓取123.txt所以把.ok去掉
						handleFiles.add(fileName);
					}
				}
			}
			
			
			for(String file : handleFiles) {
				logger.info(file);
			}
			
			
			// 4. remove file
			for (String procfileName : handleFiles) {
				boolean success = false; 
				if(source.equals(AbstractPnpMainEntity.SOURCE_EVERY8D)||source.equals(AbstractPnpMainEntity.SOURCE_UNICA)) {
					success = ftpClient.deleteFile(procfileName);
					if (!success) {
						logger.error("remove fail: " + procfileName);
					}
				}
				success = ftpClient.deleteFile(procfileName+".ok");
				if (!success) {
					logger.error("remove fail: " + procfileName+".ok");
				}
			}
			
			
		} catch (Exception ex) {
			logger.error("Error: " + ex.getMessage());
			logger.error(ex);
			ex.printStackTrace();
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error("Error: " + ex.getMessage());
			}
		}
	
	
	}
	
	@Test
	public void testDeleteFtpFiles() {
		PNPFtpSetting setting = pnpFtpService.getFtpSettings(AbstractPnpMainEntity.SOURCE_MITAKE);
		
		String[]  pFileNames = {"mitake_1_20190415.txt.ok"};
		
		pnpFtpService.deleteFileByType(setting.getPath(), pFileNames, setting);
		
	}
	
	
	@Test
	public void getFTPFileNames() throws Exception {
//		System.out.println("++++++++++++++++++++++++getFTPFileNames+++++++++++++++++++++++");
//        String ftpServerName = CoreConfigReader.getString(CONFIG_STR.BN_FTP_SERVER_NAME, true);
//        int ftpPort = Integer.parseInt(CoreConfigReader.getString(CONFIG_STR.BN_FTP_SERVER_PORT, true));
//        String ftpUsr = CoreConfigReader.getString(CONFIG_STR.BN_FTP_USR, true);
//        String ftpPass = CoreConfigReader.getString(CONFIG_STR.BN_FTP_PASS, true);
////        BillingNoticeFTPService billingNoticeFTPService = new BillingNoticeFTPService(ftpServerName,ftpPort,ftpUsr,ftpPass);
////        billingNoticeFTPService.retrieveAllFile("/", "c:");
//		System.out.println("++++++++++++++++++++++++getFTPFileNames+++++++++++++++++++++++");
//		
	}
	
	@Test
	public void testParseEvery8D() throws Exception {
//		
//		System.out.println("12345");
//		File targetFile = new File("C:\\\\PNP\\every8d\\every8d.txt");
//	    InputStream targetStream = new FileInputStream(targetFile);
//		List<String> fileContents = IOUtils.readLines(targetStream, "big5");
//		String header = fileContents.get(0);
//		Calendar expiryTime = Calendar.getInstance();
//		expiryTime.add(Calendar.HOUR, 3);
//		String[] splitHeaderData = header.split("\\&",7);
//		String Subject = splitHeaderData[0];
//		String UserID = splitHeaderData[1].toUpperCase();
//		String Password = splitHeaderData[2];
//		String OrderTime = splitHeaderData[3];
//		String ExprieTime = splitHeaderData[4];
//		String MsgType = splitHeaderData[5];
//		String BatchID = splitHeaderData[6];//簡訊平台保留欄位，請勿填入資料
//		
//		System.out.println("Subject :"+Subject);
//		System.out.println("UserID :"+UserID);
//		System.out.println("Password :"+Password);
//		System.out.println("OrderTime :"+OrderTime);
//		System.out.println("ExprieTime :"+ExprieTime);
//		System.out.println("MsgType :"+MsgType);
//		System.out.println("BatchID :"+BatchID);
//		
//		for (int i = 1; i < fileContents.size(); i++) {
//			if (StringUtils.isNotBlank(fileContents.get(i))) {
//				System.out.println(fileContents.get(i).toString());
//				String[] detailData = fileContents.get(i).split("\\&",10);
//				if (detailData.length == 10) {
////					BillingNoticeFtpDetail detail = new BillingNoticeFtpDetail();
//					System.out.println("SN :"+detailData[0]);//SN 名單流水號
//					System.out.println("DestName :"+detailData[1]);//DestName 收件者名稱
//					System.out.println("DestNo :"+detailData[2]);//DestNo 收訊人手機號碼，長度為20碼以內(格式為0933******或+886933******)
//					System.out.println("Content :"+detailData[3]);//Content 簡訊訊息內容
//					System.out.println("PID :"+detailData[4]);//PID 身份字號
//					System.out.println("CampaignID :"+detailData[5]);//CampaignID 行銷活動代碼(可為空值)
//					System.out.println("SegmentID :"+detailData[6]);//SegmentID 客群代號(可為空值)
//					System.out.println("ProgramID :"+detailData[7]);//ProgramID 階段代號(可為空值)
//					System.out.println("Variable1 :"+detailData[8]);//Variable1 擴充欄位1(可為空值)
//					System.out.println("Variable2 :"+detailData[9]);//Variable2 擴充欄位2(可為空值)
////					detail.setUid(detailData[0]);
////					detail.setTitle(detailData[1]);
////					detail.setText(detailData[2]);
////					detail.setTemplate(detailData[3]);
////					details.add(detail);
//				}else {
//					logger.error("parseFtpFile error Data:" + detailData);
//				}
//			}
//		}
	}
	
	
	

	@Test
	public void testGatMobile() throws Exception {
		List<String> mobiles = new ArrayList<>();
//		mobiles.add("+886925915129");
//		mobiles.add("+886932287603");
//		mobiles.add("+886955133930");
//		mobiles.add("U355bd9fa3da602c56c10c84a36167de4");
//		mobiles.add("U44f4df385356c64f459dfad0cdc35448");
//		mobiles.add("U4695793f603264082890e2c351ca0aa7");
//		mobiles.add("+886925915129");
//		mobiles.add("+886932287603");
//		mobiles.add("+886955133930");
		List<Object[]> a = lineUserService.findMidsByMobileIn(mobiles);
		for(Object[] b:a) {
			System.out.println(b[0] +"::"+b[1]);
		}
		
	}
	
	@Test
	public void findDetails() throws Exception {
		PnpMainEvery8d waitMain = pnpRepositoryCustom.findFirstMainByStatusForUpdateEvery8d(AbstractPnpMainEntity.STAGE_BC,AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
		logger.info("PnpMainEvery8d waitMain.getPnpMainId() :"+ waitMain.getPnpMainId());
		List<String>  statusList = new ArrayList<String>();
		statusList.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
		List<? super PnpDetail>  details = pnpRepositoryCustom.findDetailByStatusForUpdateEvery8d(statusList, waitMain.getPnpMainId());
		
		logger.info("PnpDetailEvery8d details size :"+ details.size());
		
	}
	
	@Test
	public void pnpPushMsg() throws Exception {
		pnpPushMsgService.sendingEvery8dMain("123");
		
	}
	@Test
	public void testSendingEvery8dMain() throws Exception {
		pnpPushMsgService.sendingEvery8dMain();
		
	}
	
	@Test
	public void testPushLineMessage() throws Exception {
		PnpMainEvery8d pnpMain = pnpPushMsgService.sendingEvery8dMain("test");
		pnpService.pushLineMessage(pnpMain, null, null);
		
	}
	
	@Test
	public void testPnpSMSMsg() throws Exception {
		pnpSMSMsgService.smsEvery8dMain("TEST");
		
	}
	
//	@Test
//	public void testLoadFtbPnpDataTask() throws Exception {
//		logger.info("==========================================");
//		logger.info(loadFtbPnpDataTask.toSHA256("0935152035"));
//		logger.info("==========================================");
//		
//	}
//	
////	@Test
////	public void testPnpSMSMsgService_FTPUPLOADFILE() throws Exception {
////		logger.info("==========================================");
////		logger.info(pnpSMSMsgService.uploadFileToSMS("2", "every8d_1_L_20190408.txt"));
////		logger.info("==========================================");
////		
////		
////		
////		
////	}
//	
//	@Test
//	public void testLoadFtbPnpDataTaskParseData() throws Exception {
//		logger.info("==========================================");
//		logger.info("====================loadFtbPnpDataTask.startCircle();======================");
//		loadFtbPnpDataTask.startCircle();
//		logger.info("==========================================");
//		
//	}
//	
//	@Test
//	public void testUploadToFtp() throws IOException {
//		
//		String body = "B2BOP_FISC_SMS&B2B_HK&&&&1&\r\n" + 
//				"3&XX靜&0988888888&failFile&&&&&&\r\n" + 
//				"4&XX憲&0910888888&failFile&&&&&&\r\n" + 
//				"7&Tester&0985888888&failFile&&&&&&";
//		
//		
//		InputStream targetStream = new ByteArrayInputStream((body.toString()).getBytes());
//		
//		loadFtbPnpDataTask.uploadFileToSMS(AbstractPnpMainEntity.SOURCE_EVERY8D, "every8d_Eason_L_20190408.txt", targetStream);
//	}
//	
//	@Test
//	public void testParseMingData() throws Exception {
//		
//		List<String> fileContents = new ArrayList<>();
//		fileContents.add("5820766;;0911888888;;明宣格式測試檔案;;2017/2/21 16:00;;TSBSMSTEST;;TSBSMSTEST;;0;;1;;86400");
//		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		// 明宣沒有header所以從0開始
//		for (int i = 0; i < fileContents.size(); i++) {
//			if (StringUtils.isNotBlank(fileContents.get(i))) {
//				String[] detailData = fileContents.get(i).split("\\;;", 9);
//				if (detailData.length == 9) {
//					PnpDetailMing detail = new PnpDetailMing();
//					logger.info(detailData[0]);
//					logger.info(detailData[1]);
//					logger.info(detailData[2]);
//					logger.info(detailData[3]);
//					logger.info(detailData[4]);
//					logger.info(detailData[5]);
//					logger.info(detailData[6]);
//					logger.info(detailData[7]);
//					logger.info(detailData[8]);
//					detail.setSN(detailData[0]);// SN 流水號
//					detail.setPhone(detailData[1]);// 收訊人手機號碼，長度為20碼以內(格式為0933******或+886933******)
////					detail.setPhoneHash(toSHA256(detailData[1]));
//					detail.setMsg(detailData[2]);// Content 簡訊訊息內容
//					detail.setDetailScheduleTime(sdf.parse(detailData[3]));// 預約時間
//					detail.setAccount1(detailData[4]);// 批次帳號1
//					detail.setAccount2(detailData[5]);// 批次帳號2
//					detail.setVariable1(detailData[6]);// Variable1 擴充欄位1(可為空值)
//					detail.setVariable2(detailData[7]);// Variable2 擴充欄位2(可為空值)
//					detail.setKeepSecond(detailData[8]);// 有效秒數
//				} else {
//					logger.error("parsePnpDetailMing error Data:" + detailData);
//				}
//			}
//		}
//
//	}
	
}
