package com.bcs.core.taishin.circle.PNP.scheduler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFtpService;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFtpSetting;
//import com.bcs.core.taishin.circle.PNP.service.PnpService;
import com.bcs.core.taishin.circle.db.entity.CircleEntityManagerControl;

import scala.collection.mutable.StringBuilder;

/**
 * 循環執行 回組發送失敗資料成來源資料格式  > 將TXT檔放到SMS指定位置 > 更新資料狀態
 * 
 * @author Kenneth
 *
 */

@Service
public class PnpSMSMsgService {

	/** Logger */
	private static Logger logger = Logger.getLogger(PnpSMSMsgService.class);
	@Autowired
	private CircleEntityManagerControl entityManagerControl;
//	@Autowired
//	private PnpService pnpService;
	@Autowired
	private PnpAkkaService pnpAkkaService;
	@Autowired
	private PnpRepositoryCustom pnpRepositoryCustom;
	@Autowired
	private PNPFtpService pnpFtpService;
	
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> scheduledFuture = null;

	
	String ftpServerName = null;
	int ftpPort = 0;
	String ftpUsr= null;
	String ftpPass= null;
	String downloadSavePath= null;
	String uploadPath= null;
	
	
	public PnpSMSMsgService() {
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
			logger.error(" PnpSMSMsgService TimeUnit error :" + time  + unit);
			return;
		}
		scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				// 排程工作
				logger.debug(" PnpSMSMsgService startCircle....");
				
				//#.pnp.bigswitch = 0(停止排程) 1(停止排程，並轉發SMS) 其他(正常運行)
				int bigSwitch = CoreConfigReader.getInteger(CONFIG_STR.PNP_BIGSWITCH, true, false);
				if (1==bigSwitch || 0==bigSwitch) { //大流程關閉時不做
					logger.warn("PNP_BIGSWITCH : "+bigSwitch +"PnpSMSMsgService stop transfer file to SMS FTP ...");
					return;
				}
				
				smsMitakeMain();
				smsEvery8dMain();
				smsUnicaMain();
				smsMingMain();
			}
		}, 0, time, TimeUnit.valueOf(unit));
		
		

	}
	
	public void smsMitakeMain(){
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}
			
		} catch (Exception e) {
			logger.error("SMS MitakeMain getHostName error:" + e.getMessage());
		}
		try {
			PnpMainMitake pnpMainMitake =smsMitakeMain(procApName);
			if (null == pnpMainMitake) {
				logger.debug("SMS MitakeMain not data");
			}else {
				pnpAkkaService.tell(pnpMainMitake);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("SMS MitakeMain error:" + e.getMessage());
		}
	}
	
	/**
	 * Retry detail 找一筆後找出他的main + Main status = WAIT者找一筆
	 * 更新BillingNoticeMain & BillingNoticeDetail status
	 * @param limits
	 * @param procApName
	 * @return
	 * @throws IOException 
	 */
	public PnpMainMitake smsMitakeMain(String procApName) throws IOException{
		PnpMainMitake pnpMainMitake = null;
//		List<String> templateIds = pnpService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
//		if (templateIds == null || templateIds.isEmpty()) {
//			return pnpMainMitakes;
//		}
		
		//找一筆需要SMS的Detail
		PnpDetailMitake pnpDetail  =  pnpRepositoryCustom.findFirstDetailByStatusForUpdateMitake(
				AbstractPnpMainEntity.STAGE_SMS,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
		if (null == pnpDetail) {
			logger.info("smsMitakeMain : there is no data for PNP .");
			return null;
		}
		
		Long mainId = pnpDetail.getPnpMainId();
		
		List<? super PnpDetail> details = pnpRepositoryCustom.findDetailsWaitForPNPMitake(
				AbstractPnpMainEntity.STAGE_SMS,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS,mainId);
		
		logger.info("smsMitakeMain details size :"+ details.size());
		
		if(CollectionUtils.isEmpty(details)) {
			logger.error("smsMitakeMain : there is a main has no details!!!");
			return null;
		}
		
		pnpMainMitake = pnpRepositoryCustom.findMainByMainIdMitake(mainId);
		if(null == pnpMainMitake){
			logger.error("smsMitakeMain : mainId mapping error ; no main correspond this  mainId : "+mainId);
			return null;
		}
		//回組成來源資料格式
		pnpMainMitake.setPnpDetails(details);
		
		/**
		 *三竹 header

		 *  欄位                                    型態                   長度                      説明                                                                                                                                                      備註
		 *GroupID	     Varchar    10                  群組代號 (TSBANK) 需由三竹簡訊中心設定後方能使用，使用者無法更改。/必填            必填

		 *Username       Varchar    20                  使用者(帳號)新版簡訊掛帳依據                                                                                                   必填

		 *UserPassword   Varchar    10                  此欄位(不需填寫)請保留空格(以’&’符號分隔)
		 *                                  ex.TSBANK&jack& &2003********00&9999&0                必填

		 *OrderTime      Char       14                  預約時間 ‘yyyymmddhhmmss’固定14位數簡訊預約時間。
		 *                                  也就是希望簡訊何時送達手機，格式為YYYYMMDDhhmmss，若預約時間大於系統時間，則為預約簡訊。
		 *                                  若預約時間已過或為空白則為即時簡訊。
		 *                                  即時簡訊為儘早送出，若受到第6個宵禁欄位的限制，就不一定是立刻送出。	

		 *ValidityTime   Char       14                  有效分鐘數(受限於電信業者：建議勿超過24 H)(只能帶數字)	

		 *MsgType        Char       1                     訊息型態 :宵禁‘0’ 表一般通知簡訊 ->run 09:00~19:00  ‘1’ 
		 *                                  表警急通知簡訊 ->run 00:00~24:00                           必填
		 */
		//來源資料HEADER
		StringBuilder header = new StringBuilder();
		header.append(pnpMainMitake.getGroupIDSource()+"&");
		header.append(pnpMainMitake.getUsername()+"&");
		header.append(pnpMainMitake.getUserPassword()+"&");
		header.append(pnpMainMitake.getOrderTime()+"&");
		header.append(pnpMainMitake.getValidityTime()+"&");
		header.append(pnpMainMitake.getMsgType()+"\r\n");
		
		logger.info(header.toString());
		
		/**
		 * 
		 *  三竹  body
		 * 欄位    型態 長度 説明 備註
		 * DestCategory    Char      8  ”掛帳代碼” =>  PCC Code       此欄位新版簡訊無特別用處                       Dept_id(必填)
	
		 * DestName        Varchar   20 請填入系統有意義之流水號(open端可辯示之唯一序號)                   Msg_idx

		 * DestNo          Varchar   20 手機門號/請填入09帶頭的手機號碼。                                                                                          必填 Tel(必填)

		 * MsgData         Varchar      請勿輸入 % $ '  字元，不可使用‘&’分隔號，或以全型字使用/簡訊內容。
		 *                              若有換行的需求，請以ASCII Code 6代表換行。必填。(333個字)         Content(必填)
		 * 
		 * 
		 */
		
		StringBuilder body = new StringBuilder();
		
		PnpDetailMitake pnpDetailMitake = null;
		for(Object detail : details) {
			pnpDetailMitake = (PnpDetailMitake) detail;
			body.append(pnpDetailMitake.getDestCategory()+"&");
			body.append(pnpDetailMitake.getDestName()+"&");
			body.append(pnpDetailMitake.getPhone()+"&");
			body.append(pnpDetailMitake.getMsg()+"\r\n");
		}
		
		//更換檔名(加L)
		String origFileName = pnpMainMitake.getOrigFileName();
		String changedOrigFileName = origFileName.substring(0, origFileName.lastIndexOf("_"))+"_L"+origFileName.substring(origFileName.lastIndexOf("_"));
		
		logger.info("==================================================");
		logger.info(changedOrigFileName);
		logger.info("==================================================");
		
		InputStream targetStream = new ByteArrayInputStream((header.toString()+body.toString()).getBytes());
		
		
		//傳檔案到SMS FTP
		uploadFileToSMS(AbstractPnpMainEntity.SOURCE_MITAKE ,targetStream, changedOrigFileName);
		
		//update待發送資料 status(Sending) &excuter name(hostname)
		updateStatusSuccess(procApName,pnpMainMitake , details);
		
		return pnpMainMitake;
	}
	public void smsEvery8dMain(){
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}

		} catch (Exception e) {
			logger.error("SMS Every8dMain getHostName error:" + e.getMessage());
		}
		try {
			PnpMainEvery8d pnpMainEvery8d =smsEvery8dMain(procApName);
			if (null == pnpMainEvery8d) {
				logger.debug("SMS Every8dMain not data");
			}else {
				pnpAkkaService.tell(pnpMainEvery8d);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("SMS Every8dMain error:" + e.getMessage());
		}
	}
	
	/**
	 * Retry detail 找一筆後找出他的main + Main status = WAIT者找一筆
	 * 更新BillingNoticeMain & BillingNoticeDetail status
	 * @param limits
	 * @param procApName
	 * @return
	 * @throws IOException 
	 */
	public PnpMainEvery8d smsEvery8dMain(String procApName) throws IOException{
		PnpMainEvery8d pnpMainEvery8d = null;
//		List<String> templateIds = pnpService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
//		if (templateIds == null || templateIds.isEmpty()) {
//			return pnpMainEvery8ds;
//		}
		
		//找一筆需要SMS的Detail
		PnpDetailEvery8d pnpDetail  =  pnpRepositoryCustom.findFirstDetailByStatusForUpdateEvery8d(
				AbstractPnpMainEntity.STAGE_SMS,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
		if (null == pnpDetail) {
			logger.info("smsEvery8dMain : there is no data for PNP .");
			return null;
		}
		
		Long mainId = pnpDetail.getPnpMainId();
		
		List<? super PnpDetail> details = pnpRepositoryCustom.findDetailsWaitForPNPEvery8d(
				AbstractPnpMainEntity.STAGE_SMS,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS,mainId);
		
		logger.info("smsEvery8dMain details size :"+ details.size());

		if(CollectionUtils.isEmpty(details)) {
			logger.error("smsEvery8dMain : there is a main has no details!!!");
			return null;
		}
		
		pnpMainEvery8d = pnpRepositoryCustom.findMainByMainIdEvery8d(mainId);
		if(null == pnpMainEvery8d){
			logger.error("smsEvery8dMain : mainId mapping error ; no main correspond this  mainId : "+mainId);
			return null;
		}
		//回組成來源資料格式
		pnpMainEvery8d.setPnpDetails(details);
		
		/**
		 * 互動 header
		 *   &          char        1       N        分隔符號
		 *  名稱	                         屬性                         長度                 Null?     說明
		 * Subject	    NVARCHAR   200      Y        簡訊主旨
		 * UserID       CHAR                N        批次使用者帳號，必須存在於互動簡訊系統中且為啟用
		 * Password     CHAR                Y        使用者密碼(可不填)
		 * OrderTime                                 預約發送時間（YYYYMMDDhhmmss），預約發送時間必須大於系統時間，否則不予傳送。未填入代表立即傳送。
		 * ExprieTime                                (暫未開放，請填入空值)重傳間隔。手機端於時限內，未收訊成功時，則重傳簡訊。
		 * MsgType                                   宵禁延遲發送旗標，此旗標為1時，則不受系統所設定之宵禁條件所約束，此旗標為0時，則受到系統設定宵禁條件所約束，該筆簡訊則自動轉為預約簡訊，預約時間為宵禁結束之時間點。(上班日 AM 9:00~PM19:00)
		 * BatchID      char        36      Y        簡訊平台保留欄位，請勿填入資料
		 * 
		 */
		//來源資料HEADER
		StringBuilder header = new StringBuilder();
		header.append(pnpMainEvery8d.getSubject()+"&");
		header.append(pnpMainEvery8d.getUserID()+"&");
		header.append(pnpMainEvery8d.getPassword()+"&");
		header.append(pnpMainEvery8d.getOrderTime()+"&");
		header.append(pnpMainEvery8d.getExprieTime()+"&");
		header.append(pnpMainEvery8d.getMsgType()+"&");
		header.append(pnpMainEvery8d.getBatchID()+"\r\n");
		
		logger.info(header.toString());

		/**
		 * 
		 * 分隔符號       &	   char	  1	   N	 分隔符號
		 * 
		 * 互動 body
		 *   名稱	                          屬性	                       長度	  Null?	          說明
		 *   SN	        char        15     N     名單流水號-每批名單中之流水號。每批名單中之流水號不可重覆寫入odcpn.CMM_SMS_FB [VAR1]
		 *   DestName   char        36	   Y     收件者名稱。接收者名稱，可放置客戶姓名，任何可供補助辯識之資訊，發報結果將此欄位一起回寫至發報檔中。長度限制為50碼。DestName
		 *   Mobile	    char        20	   N     收訊人手機號碼，長度為20碼以內。(格式為0933******或+886933******)DestNo
		 *   Content    nvarchar    756	   N     簡訊訊息內容，純英文長度為756字，中英混合或純中文最長為333字。MsgData
		 *   PID        char        11	   Y     身份字號
		 *   CampaignID	varchar	    28     Y     行銷活動代碼(可為空值)
		 *   SegmentID	varchar	    10     Y     客群代號(可為空值)
		 *   ProgramID	varchar	    20     Y     階段代號(可為空值)
		 *   Variable1	varchar	    15     Y     擴充欄位1(可為空值)
		 *   Variable2	varchar	    15     Y     擴充欄位2(可為空值)
		 * 
		 */
		
		StringBuilder body = new StringBuilder();
		
		PnpDetailEvery8d pnpDetailEvery8d = null;
		for(Object detail : details) {
			pnpDetailEvery8d = (PnpDetailEvery8d) detail;
			body.append(pnpDetailEvery8d.getSN()+"&");
			body.append(pnpDetailEvery8d.getDestName()+"&");
			body.append(pnpDetailEvery8d.getPhone()+"&");
			body.append(pnpDetailEvery8d.getMsg()+"&");
			body.append(pnpDetailEvery8d.getPID()+"&");
			body.append(pnpDetailEvery8d.getCampaignID()+"&");
			body.append(pnpDetailEvery8d.getSegmentID()+"&");
			body.append(pnpDetailEvery8d.getProgramID()+"&");
			body.append(pnpDetailEvery8d.getVariable1()+"&");
			body.append(pnpDetailEvery8d.getVariable2()+"\r\n");
		}
		
		//更換檔名(加L)
		String origFileName = pnpMainEvery8d.getOrigFileName();
		String changedOrigFileName = origFileName.substring(0, origFileName.lastIndexOf("_"))+"_L"+origFileName.substring(origFileName.lastIndexOf("_"));
		
		logger.info("==================================================");
		logger.info(changedOrigFileName);
		logger.info("==================================================");
		
		InputStream targetStream = new ByteArrayInputStream((header.toString()+body.toString()).getBytes());
		
		
		//傳檔案到SMS FTP
		uploadFileToSMS(AbstractPnpMainEntity.SOURCE_EVERY8D,targetStream, changedOrigFileName);
		
		//update待發送資料 status(Sending) &excuter name(hostname)
		updateStatusSuccess(procApName,pnpMainEvery8d , details);
		
		return pnpMainEvery8d;
	}
	
	public void smsUnicaMain(){
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}
			
		} catch (Exception e) {
			logger.error("SMS UnicaMain getHostName error:" + e.getMessage());
		}
		try {
			PnpMainUnica pnpMainUnica =smsUnicaMain(procApName);
			if (null == pnpMainUnica) {
				logger.debug("SMS UnicaMain not data");
			}else {
				pnpAkkaService.tell(pnpMainUnica);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("SMS UnicaMain error:" + e.getMessage());
		}
	}
	
	/**
	 * Retry detail 找一筆後找出他的main + Main status = WAIT者找一筆
	 * 更新Main & Detail status
	 * @param limits
	 * @param procApName
	 * @return
	 * @throws IOException 
	 */
	public PnpMainUnica smsUnicaMain(String procApName) throws IOException{
		PnpMainUnica pnpMainUnica = null;
//		List<String> templateIds = pnpService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
//		if (templateIds == null || templateIds.isEmpty()) {
//			return pnpMainUnicas;
//		}
		
		//找一筆需要SMS的Detail
		PnpDetailUnica pnpDetail  =  pnpRepositoryCustom.findFirstDetailByStatusForUpdateUnica(
				AbstractPnpMainEntity.STAGE_SMS,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
		if (null == pnpDetail) {
			logger.info("smsUnicaMain : there is no data for PNP .");
			return null;
		}
		
		Long mainId = pnpDetail.getPnpMainId();
		
		List<? super PnpDetail> details = pnpRepositoryCustom.findDetailsWaitForPNPUnica(
				AbstractPnpMainEntity.STAGE_SMS,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS,mainId);
		
		logger.info("smsUnicaMain details size :"+ details.size());
		
		if(CollectionUtils.isEmpty(details)) {
			logger.error("smsUnicaMain : there is a main has no details!!!");
			return null;
		}
		
		pnpMainUnica = pnpRepositoryCustom.findMainByMainIdUnica(mainId);
		if(null == pnpMainUnica){
			logger.error("smsUnicaMain : mainId mapping error ; no main correspond this  mainId : "+mainId);
			return null;
		}
		//回組成來源資料格式
		pnpMainUnica.setPnpDetails(details);
		
		/**
		 * 互動 header
		 *   &          char        1       N        分隔符號
		 *  名稱	                         屬性                         長度                 Null?     說明
		 * Subject	    NVARCHAR   200      Y        簡訊主旨
		 * UserID       CHAR                N        批次使用者帳號，必須存在於互動簡訊系統中且為啟用
		 * Password     CHAR                Y        使用者密碼(可不填)
		 * OrderTime                                 預約發送時間（YYYYMMDDhhmmss），預約發送時間必須大於系統時間，否則不予傳送。未填入代表立即傳送。
		 * ExprieTime                                (暫未開放，請填入空值)重傳間隔。手機端於時限內，未收訊成功時，則重傳簡訊。
		 * MsgType                                   宵禁延遲發送旗標，此旗標為1時，則不受系統所設定之宵禁條件所約束，此旗標為0時，則受到系統設定宵禁條件所約束，該筆簡訊則自動轉為預約簡訊，預約時間為宵禁結束之時間點。(上班日 AM 9:00~PM19:00)
		 * BatchID      char        36      Y        簡訊平台保留欄位，請勿填入資料
		 * 
		 */
		//來源資料HEADER
		StringBuilder header = new StringBuilder();
		header.append(pnpMainUnica.getSubject()+"&");
		header.append(pnpMainUnica.getUserID()+"&");
		header.append(pnpMainUnica.getPassword()+"&");
		header.append(pnpMainUnica.getOrderTime()+"&");
		header.append(pnpMainUnica.getExprieTime()+"&");
		header.append(pnpMainUnica.getMsgType()+"&");
		header.append(pnpMainUnica.getBatchID()+"\r\n");
		
		logger.info(header.toString());
		
		/**
		 * 
		 * 分隔符號       &	   char	  1	   N	 分隔符號
		 * 
		 * 互動 body
		 *   名稱	                          屬性	                       長度	  Null?	          說明
		 *   SN	        char        15     N     名單流水號-每批名單中之流水號。每批名單中之流水號不可重覆寫入odcpn.CMM_SMS_FB [VAR1]
		 *   DestName   char        36	   Y     收件者名稱。接收者名稱，可放置客戶姓名，任何可供補助辯識之資訊，發報結果將此欄位一起回寫至發報檔中。長度限制為50碼。DestName
		 *   Mobile	    char        20	   N     收訊人手機號碼，長度為20碼以內。(格式為0933******或+886933******)DestNo
		 *   Content    nvarchar    756	   N     簡訊訊息內容，純英文長度為756字，中英混合或純中文最長為333字。MsgData
		 *   PID        char        11	   Y     身份字號
		 *   CampaignID	varchar	    28     Y     行銷活動代碼(可為空值)
		 *   SegmentID	varchar	    10     Y     客群代號(可為空值)
		 *   ProgramID	varchar	    20     Y     階段代號(可為空值)
		 *   Variable1	varchar	    15     Y     擴充欄位1(可為空值)
		 *   Variable2	varchar	    15     Y     擴充欄位2(可為空值)
		 * 
		 */
		
		StringBuilder body = new StringBuilder();
		
		PnpDetailUnica pnpDetailUnica = null;
		for(Object detail : details) {
			pnpDetailUnica = (PnpDetailUnica) detail;
			body.append(pnpDetailUnica.getSN()+"&");
			body.append(pnpDetailUnica.getDestName()+"&");
			body.append(pnpDetailUnica.getPhone()+"&");
			body.append(pnpDetailUnica.getMsg()+"&");
			body.append(pnpDetailUnica.getPID()+"&");
			body.append(pnpDetailUnica.getCampaignID()+"&");
			body.append(pnpDetailUnica.getSegmentID()+"&");
			body.append(pnpDetailUnica.getProgramID()+"&");
			body.append(pnpDetailUnica.getVariable1()+"&");
			body.append(pnpDetailUnica.getVariable2()+"\r\n");
		}
		
		//更換檔名(加L)
		String origFileName = pnpMainUnica.getOrigFileName();
		String changedOrigFileName = origFileName.substring(0, origFileName.lastIndexOf("_"))+"_L"+origFileName.substring(origFileName.lastIndexOf("_"));
		
		logger.info("==================================================");
		logger.info(changedOrigFileName);
		logger.info("==================================================");
		
		InputStream targetStream = new ByteArrayInputStream((header.toString()+body.toString()).getBytes());
		
		
		//傳檔案到SMS FTP
		uploadFileToSMS(AbstractPnpMainEntity.SOURCE_UNICA,targetStream, changedOrigFileName);
		
		//update待發送資料 status(Sending) &excuter name(hostname)
		updateStatusSuccess(procApName,pnpMainUnica , details);
		
		return pnpMainUnica;
	}

	public void smsMingMain(){
		String procApName = null;
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null) {
				procApName = localAddress.getHostName();
			}

		} catch (Exception e) {
			logger.error("SMS MingMain getHostName error:" + e.getMessage());
		}
		try {
			PnpMainMing pnpMainMing =smsMingMain(procApName);
			if (null == pnpMainMing) {
				logger.debug("SMS MingMain not data");
			}else {
				pnpAkkaService.tell(pnpMainMing);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("SMS MingMain error:" + e.getMessage());
		}
	}
	
	
	/**
	 * Retry detail 找一筆後找出他的main + Main status = WAIT者找一筆
	 * 更新BillingNoticeMain & BillingNoticeDetail status
	 * @param limits
	 * @param procApName
	 * @return
	 * @throws IOException 
	 */
	public PnpMainMing smsMingMain(String procApName) throws IOException{
		PnpMainMing pnpMainMing = null;
//		List<String> templateIds = pnpService.findProductSwitchOnTemplateId(); // find ProductSwitchOn template
//		if (templateIds == null || templateIds.isEmpty()) {
//			return pnpMainMings;
//		}
		
		//找一筆需要SMS的Detail
		PnpDetailMing pnpDetail  =  pnpRepositoryCustom.findFirstDetailByStatusForUpdateMing(
				AbstractPnpMainEntity.STAGE_SMS,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
		if (null == pnpDetail) {
			logger.info("smsMingMain : there is no data for PNP .");
			return null;
		}
		
		Long mainId = pnpDetail.getPnpMainId();
		
		List<? super PnpDetail> details = pnpRepositoryCustom.findDetailsWaitForPNPMing(
				AbstractPnpMainEntity.STAGE_SMS,AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS,mainId);
		
		logger.info("smsMingMain details size :"+ details.size());

		if(CollectionUtils.isEmpty(details)) {
			logger.error("smsMingMain : there is a main has no details!!!");
			return null;
		}
		
		pnpMainMing = pnpRepositoryCustom.findMainByMainIdMing(mainId);
		if(null == pnpMainMing){
			logger.error("smsMingMain : mainId mapping error ; no main correspond this  mainId : "+mainId);
			return null;
		}
		//回組成來源資料格式
		pnpMainMing.setPnpDetails(details);
		
		/**
		 * 明宣 header
		 * 無
		 */
		//來源資料HEADER
//		StringBuilder header = new StringBuilder();
//		header.append(pnpMainMing.getSubject()+"&");
//		header.append(pnpMainMing.getUserID()+"&");
//		header.append(pnpMainMing.getPassword()+"&");
//		header.append(pnpMainMing.getOrderTime()+"&");
//		header.append(pnpMainMing.getExprieTime()+"&");
//		header.append(pnpMainMing.getMsgType()+"&");
//		header.append(pnpMainMing.getBatchID()+"\r\n");
//		logger.info(header.toString());

		/**
		 * 
		 * 分隔符號       ;;   char	  1	   N	 分隔符號
		 * 
		 * 明宣 body
		 *             分隔符號為兩個分號";;"
		 *             流水號;;手機號碼;;簡訊內容;;預約時間;;批次帳號;;批次帳號;;0;;1;;有效秒數
		 */
		
		StringBuilder body = new StringBuilder();
		
		PnpDetailMing pnpDetailMing = null;
		for(Object detail : details) {
			pnpDetailMing = (PnpDetailMing) detail;
			body.append(pnpDetailMing.getSN()+";;");
			body.append(pnpDetailMing.getPhone()+";;");
			body.append(pnpDetailMing.getMsg()+";;");
			body.append(pnpDetailMing.getDetailScheduleTime()+";;");
			body.append(pnpDetailMing.getAccount1()+";;");
			body.append(pnpDetailMing.getAccount2()+";;");
			body.append(pnpDetailMing.getVariable1()+";;");
			body.append(pnpDetailMing.getVariable2()+";;");
			body.append(pnpDetailMing.getKeepSecond()+"\r\n");
		}
		
		//更換檔名(加L)
		String origFileName = pnpMainMing.getOrigFileName();
		String changedOrigFileName = origFileName.substring(0, origFileName.lastIndexOf("_"))+"_L"+origFileName.substring(origFileName.lastIndexOf("_"));
		
		logger.info("==================================================");
		logger.info(changedOrigFileName);
		logger.info("==================================================");
		
		InputStream targetStream = new ByteArrayInputStream((body.toString()).getBytes());
		
		//傳檔案到SMS FTP
		uploadFileToSMS(AbstractPnpMainEntity.SOURCE_MING,targetStream, changedOrigFileName);
		
		//update待發送資料 status(Sending) &excuter name(hostname)
		updateStatusSuccess(procApName, pnpMainMing,details);
		
		return pnpMainMing;
	}
	
	public void uploadFileToSMS(String source ,InputStream targetStream, String fileName) throws IOException {
		logger.info("start uploadFileToSMS ");
		
		PNPFtpSetting setting = pnpFtpService.getFtpSettings(source);
		
		logger.info(" fileName...."+fileName);
		
		try {
			pnpFtpService.uploadFileByType(targetStream, fileName, setting.getUploadPath(), setting);
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("SMS uploadFileToSMS error:" + e.getMessage());
		} 
	}
	
	
	/**
	 * update mains and details status to sending
	 * @param procApName
	 * @param pnpMains
	 * @param allDetails
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private void updateStatusSuccess(String procApName , PnpMain main , List<? super PnpDetail> allDetails) {
		Date  now = Calendar.getInstance().getTime();
		if (main != null) {
			List<Object> mains = new ArrayList<>();
			main.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE);
			main.setProcApName(procApName);
			main.setModifyTime(now);
			main.setProcStage(AbstractPnpMainEntity.STAGE_SMS);
			main.setSendTime(now);
			main.setSmsTime(now);
			mains.add(main);
			entityManagerControl.merge(mains);
		}
		if (allDetails != null) {
			List<Object> details = new ArrayList<>();
			for(Object detail : allDetails) {
				((PnpDetail) detail).setSmsTime(now);
				((PnpDetail) detail).setSendTime(now);
				((PnpDetail) detail).setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE);
				details.add(detail);
			}
			if (!details.isEmpty()) {
				entityManagerControl.merge(details);
			}
		}
	}

	
	/**
	 * 檢查ftpserver setting
	 * @return
	 */
	private boolean validateFtpHostData(String ftpServerName ,
			int ftpPort ,
			String ftpUsr ,
			String ftpPass ,
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
	
	/**
	 * Stop Schedule : Wait for Executing Jobs to Finish
	 * 
	 * @throws SchedulerException
	 */
	@PreDestroy
	public void destroy() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(true);
			logger.info(" BillingNoticeSendMsgService cancel....");
		}

		if (scheduler != null && !scheduler.isShutdown()) {
			logger.info(" BillingNoticeSendMsgService shutdown....");
			scheduler.shutdown();
		}

	}

}
