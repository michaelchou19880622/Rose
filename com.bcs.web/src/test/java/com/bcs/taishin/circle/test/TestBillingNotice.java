package com.bcs.taishin.circle.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import com.bcs.core.api.test.SpringJUnit4BaseTester;
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
import com.bcs.core.taishin.circle.PNP.scheduler.PnpPushMsgService;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpSMSMsgService;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
//import com.bcs.core.taishin.circle.BillingNoticeDataParseService;
//import com.bcs.core.taishin.circle.BillingNoticeFTPService;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeFtpDetail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/spring-base.xml", "classpath:spring/spring-security.xml"})

public class TestBillingNotice {

////	@Autowired
//////	BillingNoticeDataParseService billingNoticeDataParseService;
////	
////	@Autowired
//////	BillingNoticeFTPService billingNoticeFTPService;
//	
//	@Autowired
//	private LineUserService lineUserService;
//	
//	@Autowired
//	private PnpRepositoryCustom pnpRepositoryCustom;
//	
//	@Autowired
//	private PnpPushMsgService pnpPushMsgService;
//	
//	@Autowired
//	private PnpService pnpService;
//	
//	@Autowired
//	private PnpSMSMsgService pnpSMSMsgService;
//	
//	@Autowired
//    private LoadFtbPnpDataTask loadFtbPnpDataTask;
//	
//	
//	/** Logger */
//	private static Logger logger = Logger.getLogger(TestBillingNotice.class);
//	
//	@Test
//	public void getFTPFileNames() throws Exception {
////		System.out.println("++++++++++++++++++++++++getFTPFileNames+++++++++++++++++++++++");
////        String ftpServerName = CoreConfigReader.getString(CONFIG_STR.BN_FTP_SERVER_NAME, true);
////        int ftpPort = Integer.parseInt(CoreConfigReader.getString(CONFIG_STR.BN_FTP_SERVER_PORT, true));
////        String ftpUsr = CoreConfigReader.getString(CONFIG_STR.BN_FTP_USR, true);
////        String ftpPass = CoreConfigReader.getString(CONFIG_STR.BN_FTP_PASS, true);
//////        BillingNoticeFTPService billingNoticeFTPService = new BillingNoticeFTPService(ftpServerName,ftpPort,ftpUsr,ftpPass);
//////        billingNoticeFTPService.retrieveAllFile("/", "c:");
////		System.out.println("++++++++++++++++++++++++getFTPFileNames+++++++++++++++++++++++");
//		
//	}
//	
//	
////	public static void main(String[] args) throws IOException {
////		
////		System.out.println("12345");
////		File targetFile = new File("C:\\\\PNP\\every8d\\every8d.txt");
////	    InputStream targetStream = new FileInputStream(targetFile);
////		List<String> fileContents = IOUtils.readLines(targetStream, "big5");
////		String header = fileContents.get(0);
////		Calendar expiryTime = Calendar.getInstance();
////		expiryTime.add(Calendar.HOUR, 3);
////		String[] splitHeaderData = header.split("\\&",7);
////		String Subject = splitHeaderData[0];
////		String UserID = splitHeaderData[1].toUpperCase();
////		String Password = splitHeaderData[2];
////		String OrderTime = splitHeaderData[3];
////		String ExprieTime = splitHeaderData[4];
////		String MsgType = splitHeaderData[5];
////		String BatchID = splitHeaderData[6];//簡訊平台保留欄位，請勿填入資料
////		
////		System.out.println("Subject :"+Subject);
////		System.out.println("UserID :"+UserID);
////		System.out.println("Password :"+Password);
////		System.out.println("OrderTime :"+OrderTime);
////		System.out.println("ExprieTime :"+ExprieTime);
////		System.out.println("MsgType :"+MsgType);
////		System.out.println("BatchID :"+BatchID);
////		
////		for (int i = 1; i < fileContents.size(); i++) {
////			if (StringUtils.isNotBlank(fileContents.get(i))) {
////				System.out.println(fileContents.get(i).toString());
////				String[] detailData = fileContents.get(i).split("\\&",10);
////				if (detailData.length == 10) {
//////					BillingNoticeFtpDetail detail = new BillingNoticeFtpDetail();
////					System.out.println("SN :"+detailData[0]);//SN 名單流水號
////					System.out.println("DestName :"+detailData[1]);//DestName 收件者名稱
////					System.out.println("DestNo :"+detailData[2]);//DestNo 收訊人手機號碼，長度為20碼以內(格式為0933******或+886933******)
////					System.out.println("Content :"+detailData[3]);//Content 簡訊訊息內容
////					System.out.println("PID :"+detailData[4]);//PID 身份字號
////					System.out.println("CampaignID :"+detailData[5]);//CampaignID 行銷活動代碼(可為空值)
////					System.out.println("SegmentID :"+detailData[6]);//SegmentID 客群代號(可為空值)
////					System.out.println("ProgramID :"+detailData[7]);//ProgramID 階段代號(可為空值)
////					System.out.println("Variable1 :"+detailData[8]);//Variable1 擴充欄位1(可為空值)
////					System.out.println("Variable2 :"+detailData[9]);//Variable2 擴充欄位2(可為空值)
//////					detail.setUid(detailData[0]);
//////					detail.setTitle(detailData[1]);
//////					detail.setText(detailData[2]);
//////					detail.setTemplate(detailData[3]);
//////					details.add(detail);
////				}else {
////					logger.error("parseFtpFile error Data:" + detailData);
////				}
////			}
////		}
////	}
//	
//	
//	
//
//	@Test
//	public void getFTPFileNames2() throws Exception {
//		List<String> mobiles = new ArrayList<>();
////		mobiles.add("+886925915129");
////		mobiles.add("+886932287603");
////		mobiles.add("+886955133930");
////		mobiles.add("U355bd9fa3da602c56c10c84a36167de4");
////		mobiles.add("U44f4df385356c64f459dfad0cdc35448");
////		mobiles.add("U4695793f603264082890e2c351ca0aa7");
////		mobiles.add("+886925915129");
////		mobiles.add("+886932287603");
////		mobiles.add("+886955133930");
//		List<Object[]> a = lineUserService.findMidsByMobileIn(mobiles);
//		for(Object[] b:a) {
//			System.out.println(b[0] +"::"+b[1]);
//		}
//		
//	}
//	
//	@Test
//	public void findDetails() throws Exception {
//		PnpMainEvery8d waitMain = pnpRepositoryCustom.findFirstMainByStatusForUpdateEvery8d(AbstractPnpMainEntity.STAGE_BC,AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
//		logger.info("PnpMainEvery8d waitMain.getPnpMainId() :"+ waitMain.getPnpMainId());
//		List<String>  statusList = new ArrayList<String>();
//		statusList.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
//		List<? super PnpDetail>  details = pnpRepositoryCustom.findDetailByStatusForUpdateEvery8d(statusList, waitMain.getPnpMainId());
//		
//		logger.info("PnpDetailEvery8d details size :"+ details.size());
//		
//	}
//	
//	@Test
//	public void pnpPushMsg() throws Exception {
//		pnpPushMsgService.sendingEvery8dMain("123");
//		
//	}
//	@Test
//	public void testSendingEvery8dMain() throws Exception {
//		pnpPushMsgService.sendingEvery8dMain();
//		
//	}
//	
//	@Test
//	public void testPushLineMessage() throws Exception {
//		PnpMainEvery8d pnpMain = pnpPushMsgService.sendingEvery8dMain("test");
//		pnpService.pushLineMessage(pnpMain, null, null);
//		
//	}
//	
//	@Test
//	public void testPnpSMSMsg() throws Exception {
//		pnpSMSMsgService.smsEvery8dMain("TEST");
//		
//	}
//	
//	@Test
//	public void testLoadFtbPnpDataTask() throws Exception {
//		logger.info("==========================================");
//		logger.info(loadFtbPnpDataTask.toSHA256("0963009799"));
//		logger.info("==========================================");
//		
//	}
//	
//	
//	
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
//	
//	public static void main(String[] args) throws NoSuchAlgorithmException {
////		String phone = "0930882559";//59fe6cf511391ac23a29cd32ce5c361aae1909be3f22baaf2e6dd07f8bed9a04
////		String phone = "0938181332";//d01883913f09dc3e7505f22dbd7c93b1cb4f140fce77fb5d6ddacef804c69550
////		String phone = "0911248364";//afd95f0628f37a5545d080bf24ced699eb3930e53885736c41f0258bc971355b
////		String phone = "0928082842";//7db7dc635cde55a05bdd17c5bfe6a104f9b91715a24eee8425581c59d726c080
////		String phone = "0918202200";//28a9cf5212ac975acd7276ef608a4df9af29a3280d91b6151031ae1fda65b736
////		String phone = "0902286758";//abe691c7719f6f812ebd34f44317f10d026bef5744195f0e7bc5556c45ed081c
////		String phone = "0983647234";//4ba8d3f9ed81f083a9895f6c084309b05f6836c25f926f554d93eea9f8d1cd85
////		String phone = "0936848484";//aa3718cf458069259942103e2c0a15367ecf31aee6863c9500a1281a4a669f3e
//		String phone = "0977612591";//932636f457b8c4380f34ce0b8bd85d0d2ba53f6e27457d3db6d4c42097075111
//
//		MessageDigest digest = MessageDigest.getInstance("SHA-256");
//
//		if ("0".equals(phone.substring(0, 1))) {// phone有些可能已轉成e.164格式，0開頭的再做轉換
//			phone = "+886" + phone.substring(1);// 改成e.164格式，針對台灣手機號碼，其他地區可能需要修改此轉換邏輯
//		}
//
//		byte[] hash = digest.digest(phone.getBytes(StandardCharsets.UTF_8));
//
//		StringBuffer hexString = new StringBuffer();
//
//		for (int i = 0; i < hash.length; i++) {
//			String hex = Integer.toHexString(0xff & hash[i]);
//			if (hex.length() == 1)
//				hexString.append('0');
//			hexString.append(hex);
//		}
//
//		System.out.println( hexString.toString());
//	}
	
	
	
//	public static void main(String[] args) {
////		String s = "封鎖測試「台新貴賓您好,謝謝您使用台新信用卡消費30元，若您在國外需要台新銀行的服務，500請利用當地國際台轉接撥打信用卡背面之客服電話確";
////		String s = "  ";
//		String s = "天地玄黃宇宙洪荒日月盈昃>><<  ";
//		String p = "封鎖測試「台新貴賓您好,謝謝您使用台新信用卡消費>><<元，若您在國外需要台新銀行的服務，>><<請利用當地國際台轉接撥打信用卡背面之客服電話確";
//		p = p.replace(">><<", "¿¡");
//		System.out.println(p);
//		System.out.println(isMatch(s, p));
//
//	}
	public static boolean isMatch(String s, String p) {
        int i = 0, j = 0, iStar = -1, jStar = -1;
        
        while (i < s.length()) {
            if (j < p.length() && (s.charAt(i) == p.charAt(j) || p.charAt(j) == '¿')) {//倒立問號
                ++i; ++j;
            }else if (j < p.length() && p.charAt(j) == '¡') {//倒立驚嘆號
                iStar = i;
                jStar = j++;
            } else if (iStar >= 0) {
                i = ++iStar; 
                j = jStar + 1;
            } else  return false;
        }
        while (j < p.length() && p.charAt(j) == '¡') ++j;//倒立驚嘆號
        return j == p.length();
    }
	
	
//	
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
//	
//	@Test
//	public void testFTPRename() throws Exception {
//		
//		loadFtbPnpDataTask.startCircle();
//		
//		
//	}
//	
//	
//	@Test
//	public void testLoadFtbPnpDataTaskParseMingData() throws Exception {
//		
//		
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
//	
//	
////	public static void main(String[] args) throws ParseException {
////		
////		List<String> fileContents = new ArrayList<>();
////		fileContents.add("5820766;;0911888888;;明宣格式測試檔案;;2017-2-21 16:00:00;;TSBSMSTEST;;TSBSMSTEST;;0;;1;;86400");
////		
////		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
////		// 明宣沒有header所以從0開始
////		for (int i = 0; i < fileContents.size(); i++) {
////			if (StringUtils.isNotBlank(fileContents.get(i))) {
////				String[] detailData = fileContents.get(i).split("\\;;", 9);
////				if (detailData.length == 9) {
////					PnpDetailMing detail = new PnpDetailMing();
////					logger.info(detailData[0]);
////					logger.info(detailData[1]);
////					logger.info(detailData[2]);
////					logger.info(detailData[3]);
////					logger.info(detailData[4]);
////					logger.info(detailData[5]);
////					logger.info(detailData[6]);
////					logger.info(detailData[7]);
////					logger.info(detailData[8]);
////					detail.setSN(detailData[0]);// SN 流水號
////					detail.setPhone(detailData[1]);// 收訊人手機號碼，長度為20碼以內(格式為0933******或+886933******)
//////					detail.setPhoneHash(toSHA256(detailData[1]));
////					detail.setMsg(detailData[2]);// Content 簡訊訊息內容
////					detail.setDetailScheduleTime(sdf.parse(detailData[3]));// 預約時間
////					detail.setAccount1(detailData[4]);// 批次帳號1
////					detail.setAccount2(detailData[5]);// 批次帳號2
////					detail.setVariable1(detailData[6]);// Variable1 擴充欄位1(可為空值)
////					detail.setVariable2(detailData[7]);// Variable2 擴充欄位2(可為空值)
////					detail.setKeepSecond(detailData[8]);// 有效秒數
////				} else {
////					logger.error("parsePnpDetailMing error Data:" + detailData);
////				}
////			}
////		}
////
////		
////		
////		
////	}
//	
	
//	public static void main(String[] args) throws ParseException {
//		String orderTime = "20190624144032";
//		Date date = new Date();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//		if(orderTime.compareTo(sdf.format(date))<0){
//			
//		}
//		Date a  = sdf.parse(orderTime);
//		
//		System.out.println(sdf.format(date));
//		System.out.println(a.before(date));
//		
//	}
	
	public static void main(String[] args) throws ParseException {
		String fileName = "O_PRMSMS_250102OCSPENDING_20190509171101000.txt";

		String[] fileNameSP = fileName.split("_");
		String accountClass = fileNameSP[0];
		String sourceSystem = fileNameSP[1];
		String account = fileNameSP[2];
		String comeTime = fileNameSP[3];
		
		
		
		System.out.println(accountClass);
		System.out.println(sourceSystem);
		System.out.println(account);
		System.out.println(comeTime);
		
	}
	
	
	
//	public static void main(String[] args) {
//		String deliveryTags = "PNP_DELIVERY;;3;;85;;6352;;0938181332";
//		
//		String[] deliveryData = deliveryTags.split("\\;;", 5);
//		
//		for(int i = 0;i<deliveryData.length;i++) {
//			System.out.println(deliveryData[i]);
//		}
//		
//	}
	
}
