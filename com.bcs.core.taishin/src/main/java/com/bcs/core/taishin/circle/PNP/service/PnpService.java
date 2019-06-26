package com.bcs.core.taishin.circle.PNP.service;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.EMAIL_CONFIG;
import com.bcs.core.enums.LINE_HEADER;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.taishin.circle.PNP.akka.PnpAkkaService;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpContentTemplateMsg;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpContentTemplateMsgAction;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDeliveryRecord;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMitakeRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailUnicaRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainMitakeRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpMainUnicaRepository;
//import com.bcs.core.taishin.circle.Pnp.db.repository.PnpContentTemplateMsgRepository;
//import com.bcs.core.taishin.circle.Pnp.db.repository.PnpDetailRepository;
//import com.bcs.core.taishin.circle.Pnp.db.repository.PnpMainRepository;
import com.bcs.core.utils.CryptUtil;
import com.bcs.core.utils.RestfulUtil;

import akka.actor.ActorRef;

@Service
public class PnpService {
	/** Logger */
	private static Logger logger = Logger.getLogger(PnpService.class);
	@Autowired
	private PnpMainMitakeRepository pnpMainMitakeRepository;
	@Autowired
	private PnpDetailMitakeRepository pnpDetailMitakeRepository;
	@Autowired
	private PnpMainEvery8dRepository pnpMainEvery8dRepository;
	@Autowired
	private PnpDetailEvery8dRepository pnpDetailEvery8dRepository;
	@Autowired
	private PnpMainUnicaRepository pnpMainUnicaRepository;
	@Autowired
	private PnpDetailUnicaRepository pnpDetailUnicaRepository;
	@Autowired
	private PnpMainMingRepository pnpMainMingRepository;
	@Autowired
	private PnpDetailMingRepository pnpDetailMingRepository;
//	@Autowired
//	private PnpContentTemplateMsgRepository PnpContentTemplateMsgRepository;
//	
	@Autowired
	private PnpAkkaService pnpAkkaService;
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	public PnpDetail save(Object pnpDetail) {
		String source = ((PnpDetail) pnpDetail).getSource();
		if(StringUtils.isBlank(source)){
			logger.error("PnpService save getting source is blank!!! MainID :"+((PnpDetail) pnpDetail).getPnpMainId());
		}
		
		switch (source) {
			case AbstractPnpMainEntity.SOURCE_MITAKE:
				return saveMitakeDetailStatus((PnpDetailMitake)pnpDetail);
			case AbstractPnpMainEntity.SOURCE_MING:
				return saveMingDetailStatus((PnpDetailMing)pnpDetail);
			case AbstractPnpMainEntity.SOURCE_EVERY8D:
				return saveEvery8dDetailStatus((PnpDetailEvery8d)pnpDetail);
			case AbstractPnpMainEntity.SOURCE_UNICA:
				return saveUnicaDetailStatus((PnpDetailUnica)pnpDetail);
		}
		
		return null;
	}

	private PnpDetail saveMitakeDetailStatus(PnpDetailMitake pnpDetail) {
		if (pnpDetail.getStatus().equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)) {
			pnpDetail.setSendTime(Calendar.getInstance().getTime());
		}
		return pnpDetailMitakeRepository.save(pnpDetail);
		
	}
	
	private PnpDetail saveEvery8dDetailStatus(PnpDetailEvery8d pnpDetail) {
		if (pnpDetail.getStatus().equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)) {
			pnpDetail.setSendTime(Calendar.getInstance().getTime());
		}
		return pnpDetailEvery8dRepository.save(pnpDetail);
		
	}
	
	private PnpDetail saveUnicaDetailStatus(PnpDetailUnica pnpDetail) {
		if (pnpDetail.getStatus().equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)) {
			pnpDetail.setSendTime(Calendar.getInstance().getTime());
		}
		return pnpDetailUnicaRepository.save(pnpDetail);
		
	}
	
	private PnpDetail saveMingDetailStatus(PnpDetailMing pnpDetail) {
		if (pnpDetail.getStatus().equals(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)) {
			pnpDetail.setSendTime(Calendar.getInstance().getTime());
		}
		return pnpDetailMingRepository.save(pnpDetail);
		
	}
	
	/**
	 * 若明細已無重試或等待發送/排程中或者傳遞中的狀態資料，則更新主檔狀態為完成
	 * 
	 * @param mainId
	 * @return
	 */
	public void updatePnpMainStatusComplete(Long mainId , String source) {
		if(StringUtils.isBlank(source)){
			logger.error("PnpService save getting source is blank!!! MainID :"+mainId);
		}
		List<String> status = new ArrayList<>();
		status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
		status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING);
		status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
		status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
		status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_SCHEDULED);
		
		switch (source) {
			case AbstractPnpMainEntity.SOURCE_MITAKE:
				updatePnpMainMitakeStatusComplete(mainId, status);
				break;
			case AbstractPnpMainEntity.SOURCE_MING:
				updatePnpMainMingStatusComplete(mainId, status);
				break;
			case AbstractPnpMainEntity.SOURCE_EVERY8D:
				updatePnpMainEvery8dStatusComplete(mainId, status);
				break;
			case AbstractPnpMainEntity.SOURCE_UNICA:
				updatePnpMainUnicaStatusComplete(mainId, status);
				break;
		}
	}
	
	private void updatePnpMainMitakeStatusComplete(Long mainId , List<String> status) {
		if (pnpDetailMitakeRepository.countByPnpMainIdAndStatus(mainId,
				status) == 0) {
			Date  now = Calendar.getInstance().getTime();
			pnpMainMitakeRepository.updatePnpMainMitakeStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE,now, mainId);
		}
	}
	
	private void updatePnpMainEvery8dStatusComplete(Long mainId , List<String> status) {
		if (pnpDetailEvery8dRepository.countByPnpMainIdAndStatus(mainId,
				status) == 0) {
			Date  now = Calendar.getInstance().getTime();
			pnpMainEvery8dRepository.updatePnpMainEvery8dStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE,now, mainId);
		}
	}
	
	private void updatePnpMainUnicaStatusComplete(Long mainId , List<String> status) {
		if (pnpDetailUnicaRepository.countByPnpMainIdAndStatus(mainId,
				status) == 0) {
			Date  now = Calendar.getInstance().getTime();
			pnpMainUnicaRepository.updatePnpMainUnicaStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE,now, mainId);
		}
	}
	
	private void updatePnpMainMingStatusComplete(Long mainId , List<String> status) {
		if (pnpDetailMingRepository.countByPnpMainIdAndStatus(mainId,
				status) == 0) {
			Date  now = Calendar.getInstance().getTime();
			pnpMainMingRepository.updatePnpMainMingStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE,now, mainId);
		}
	}
	
	

	/**
	 * update PnpMain and PnpDetail status 
	 * 
	 * @param PnpMain
	 */
	@Transactional(rollbackFor = Exception.class, timeout = 30)
	public void updateMainAndDetailStatus(PnpMain pnpMain, String status) {
		List<PnpDetail> details = (List<PnpDetail>) pnpMain.getPnpDetails();
		for (PnpDetail detail : details) {
			detail.setStatus(status);
			save(detail);
		}
		Date  now = Calendar.getInstance().getTime();
		
		String source = pnpMain.getSource();
		
		switch (source) {
			case AbstractPnpMainEntity.SOURCE_MITAKE:
				pnpMainMitakeRepository.updatePnpMainMitakeStatus(status, now, pnpMain.getPnpMainId());
			case AbstractPnpMainEntity.SOURCE_MING:
				pnpMainMingRepository.updatePnpMainMingStatus(status, now, pnpMain.getPnpMainId());
			case AbstractPnpMainEntity.SOURCE_EVERY8D:
				pnpMainEvery8dRepository.updatePnpMainEvery8dStatus(status, now, pnpMain.getPnpMainId());
			case AbstractPnpMainEntity.SOURCE_UNICA:
				pnpMainUnicaRepository.updatePnpMainUnicaStatus(status, now, pnpMain.getPnpMainId());
		}
		
	}

//	/**
//	 * send email to system admin setting user
//	 * @param title
//	 * @param content
//	 */
//	public void sendMail(String title, String content) {
//		logger.info("Billing Notice Send email : [" + title + "]" +  content);
//		String fromAddress = CoreConfigReader.getString(null, EMAIL_CONFIG.FROM.toString(),true);
//		if (StringUtils.isBlank(fromAddress)) {
//			logger.error("Billing Notice Send email from Address is null ");
//			return;
//		}
//		String host = CoreConfigReader.getString(null, EMAIL_CONFIG.HOST.toString(),true);
//		int port = -1;
//		try{
//			port = Integer.parseInt(CoreConfigReader.getString(null, EMAIL_CONFIG.PORT.toString(),true));
//		}
//		catch(Exception e){
//			logger.error("Billing Notice Send email smtp port is not number ");
//		}
//		final String username = CoreConfigReader.getString(null, EMAIL_CONFIG.USERNAME.toString(),true);
//		final String password = CoreConfigReader.getString(null, EMAIL_CONFIG.PASSWORD.toString(),true);// your password
//		String auth = CoreConfigReader.getString(null, EMAIL_CONFIG.AUTH.toString(),true);
//		String starttls = CoreConfigReader.getString(null, EMAIL_CONFIG.STARTTLS_ENABLE.toString(),true);
//		String debug = CoreConfigReader.getString(null, EMAIL_CONFIG.DEBUG.toString(),true);
//		Properties props = new Properties();
//		props.put("mail.smtp.host", host);
//		props.put("mail.smtp.auth", StringUtils.isBlank(auth) ? "false" : auth);
//		props.put("mail.smtp.starttls.enable", StringUtils.isBlank(starttls) ? "false" : starttls);
//		props.put("mail.smtp.port", port);
//		props.put("mail.debug", StringUtils.isBlank(debug) ? "false" : debug);
//		if (StringUtils.isBlank(host) || port == -1 || StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
//			logger.error("Billing Notice Send email setting wrong ");
//			return;
//		}
//		String adminMail = CoreConfigReader.getString(null, EMAIL_CONFIG.TO_ADMIN.toString(),true);
//		if (StringUtils.isBlank(adminMail)) {
//			logger.error("Billing Notice Send email Recipients is empty ");
//			return;
//		}
//		String secret = CoreConfigReader.getString(CONFIG_STR.AES_SECRET_KEY, true);
//		String iv = CoreConfigReader.getString(CONFIG_STR.AES_INITIALIZATION_VECTOR, true);
//		
//
//		try {
//			final String decodePassword = CryptUtil.Decrypt(CryptUtil.AES, password, secret, iv);
//			Session session = Session.getInstance(props, new Authenticator() {
//				protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
//					return new javax.mail.PasswordAuthentication(username, decodePassword);
//				}
//			});
//			List<Address> recipients = new ArrayList<>();
//			String[] toArray = adminMail.split(",");
//			for (final String address : toArray) {
//				if (StringUtils.isNotBlank(address)) {
//					recipients.add(new InternetAddress(address.trim()));
//				}
//			}
//
//			Message message = new MimeMessage(session);
//			message.setFrom(new InternetAddress(fromAddress));
//			message.setRecipients(Message.RecipientType.TO, recipients.toArray(new InternetAddress[0]));
//			message.setSubject(title);
//			message.setText(content);
//
//			Transport transport = session.getTransport("smtp");
//			transport.connect(host, port, username, password);
//
//			Transport.send(message);
//
//		} catch (MessagingException e) {
//			logger.error("Billing Notice Send email error : " + e.getMessage());
//		} catch (Exception e1) {
//			logger.error("Billing Notice Send email error : " + e1.getMessage());
//		}
//	}

	/**
	 * send line message
	 * 
	 * @param PnpMain
	 * @param sendeRef
	 * @param selfActorRef
	 */
	public void pushLineMessage(PnpMain pnpMain, ActorRef sendeRef, ActorRef selfActorRef) {

		String url = CoreConfigReader.getString(CONFIG_STR.LINE_MESSAGE_PUSH_URL.toString());
		String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(),
				CONFIG_STR.ChannelToken.toString(), true);
		String serviceCode = CoreConfigReader.getString(CONFIG_STR.AutoReply.toString(),
				CONFIG_STR.ChannelServiceCode.toString(), true);
		
		/* 設定 request headers */
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);
		List<PnpDetail> details = (List<PnpDetail>) pnpMain.getPnpDetails();
		
		logger.info("pushLineMessage pnpMain.getProcFlow():" + pnpMain.getProcFlow());
		//TODO 樣版
//		PnpContentTemplateMsg templateMsg = PnpMain.getTemplate();
//		List<PnpContentTemplateMsgAction> actions = PnpMain.getTemplateActions();
//		if (templateMsg == null || actions == null) {
//			logger.error("Template Error : PnpContentTemplateMsg or PnpContentTemplateMsgAction is empty" );
//			return;
//		}
		
		boolean sendFailFlag = false;
		for (PnpDetail detail : details) {
			// only send wait & retry user
			if(StringUtils.isBlank(detail.getUid())) {
				sendFailFlag = false;
				logger.info("detail find no UID trans to PNP!!  "+  " mainId :"+detail.getPnpMainId() +  " detailId :"+detail.getPnpDetailId() );
			}else {
				JSONObject requestBody = new JSONObject();
				requestBody.put("to", detail.getUid());
				
	//			JSONArray messageArray = combineLineMessage(templateMsg, actions, detail);
				JSONArray messageArray = combineLineTextMessage(detail);
				requestBody.put("messages", messageArray);
	
				/* 將 headers 跟 body 塞進 HttpEntity 中 */
				HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
				try {
					RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
					restfulUtil.execute();
					if("200".equals(restfulUtil.getStatusCode())){
						sendFailFlag = true;
					}else {
						sendFailFlag = false;
						logger.info(restfulUtil.getStatusCode());
					}
				} catch (KeyManagementException | NoSuchAlgorithmException e1) {
					sendFailFlag = false;//寄送失敗
					logger.info("Send line push fail PnpDetailId:" + detail.getPnpDetailId());
					logger.error("Send line push fail KeyManagementException | NoSuchAlgorithmException :" + e1.getMessage());
				} catch (Exception e) {
					sendFailFlag = false;//寄送失敗
					logger.info("Send line push fail PnpDetailId:" + detail.getPnpDetailId());
					logger.error("Send line push fail Exception:" + e.getMessage());
					if (e instanceof HttpClientErrorException) {
						logger.error("HttpClientErrorException error : " + e.getMessage());
							HttpClientErrorException exception = (HttpClientErrorException) e;
							JSONObject errorMessage = new JSONObject(exception.getResponseBodyAsString());
							if(errorMessage.has("message")) {
								logger.error("HttpClientErrorException statusCode: " + exception.getStatusCode().toString());
								if(errorMessage.has("details")) {
									logger.error("HttpClientErrorException details : " + errorMessage.getJSONArray("details").toString());
								}
							}
					}
				}
			}
			
			if(sendFailFlag) {
				detail.setStatus(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE);
			}else {
				String procFlow = pnpMain.getProcFlow();
				detail.setLinePushTime(Calendar.getInstance().getTime());
				switch (procFlow) {
					case AbstractPnpMainEntity.PROC_FLOW_BC:
						//結束 
						detail.setProcStage(AbstractPnpMainEntity.STAGE_BC);
						detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_FINISH);
						break;
					case AbstractPnpMainEntity.PROC_FLOW_BC_SMS:
						detail.setProcStage(AbstractPnpMainEntity.STAGE_SMS);
						detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
						break;
					case AbstractPnpMainEntity.PROC_FLOW_BC_PNP_SMS:
						detail.setProcStage(AbstractPnpMainEntity.STAGE_PNP);
						detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
						break;
				}
			}
			if (sendeRef != null) {
				sendeRef.tell(detail, selfActorRef);
			} else {
				pnpAkkaService.tell(detail);
			}

		}

	}

	/**
	 * send line message
	 * 
	 * @param PnpMain
	 * @param sendeRef
	 * @param selfActorRef
	 */
	public void pushPNPMessage(PnpMain pnpMain, ActorRef sendeRef, ActorRef selfActorRef) {

		String url = CoreConfigReader.getString(CONFIG_STR.LINE_PNP_PUSH_VERIFIED.toString());
		String accessToken = CoreConfigReader.getString(CONFIG_STR.Default.toString(),
				CONFIG_STR.ChannelToken.toString(), true);
		String serviceCode = CoreConfigReader.getString(CONFIG_STR.AutoReply.toString(),
				CONFIG_STR.ChannelServiceCode.toString(), true);
		
		/* 設定 request headers */
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
		headers.set(LINE_HEADER.HEADER_BOT_ServiceCode.toString(), serviceCode);
		List<PnpDetail> details = (List<PnpDetail>) pnpMain.getPnpDetails();
		
		String source = pnpMain.getSource();
		logger.info("pushLineMessage pnpMain.getProcFlow():" + pnpMain.getProcFlow());
		//TODO 樣版
//		PnpContentTemplateMsg templateMsg = PnpMain.getTemplate();
//		List<PnpContentTemplateMsgAction> actions = PnpMain.getTemplateActions();
//		if (templateMsg == null || actions == null) {
//			logger.error("Template Error : PnpContentTemplateMsg or PnpContentTemplateMsgAction is empty" );
//			return;
//		}
		
		boolean sendFailFlag = false;
		for (PnpDetail detail : details) {
			
			StringBuffer sb = new StringBuffer();
			sb.append(PnpDeliveryRecord.THIS_TYPE);
			sb.append(";;");
			sb.append(source);
			sb.append(";;");
			sb.append(detail.getPnpMainId().toString());
			sb.append(";;");
			sb.append(detail.getPnpDetailId().toString());
			sb.append(";;");
			sb.append(detail.getPhoneHash());
	        // 64 <= tag length <= 100，不夠則補空格
	        String deliveryTag = String.format("%1$-"+ 64 +"s", sb.toString());
	        
	        headers.set("X-Line-Delivery-Tag", deliveryTag);
	        logger.debug("X-Line-Delivery-Tag : " + deliveryTag);
			
			
			// only send wait & retry user
			JSONObject requestBody = new JSONObject();
			requestBody.put("to", detail.getPhoneHash());
			
//			JSONArray messageArray = combineLineMessage(templateMsg, actions, detail);
			JSONArray messageArray = combineLineTextMessage(detail);
			requestBody.put("messages", messageArray);

			/* 將 headers 跟 body 塞進 HttpEntity 中 */
			HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
			try {
				RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity);
				restfulUtil.execute();
				if("200".equals(restfulUtil.getStatusCode())){
					sendFailFlag = true;
				}else {
					sendFailFlag = false;
					logger.info(restfulUtil.getStatusCode());
				}
			} catch (KeyManagementException | NoSuchAlgorithmException e1) {
				sendFailFlag = false;//寄送失敗
				logger.info("Send PNP fail PnpDetailId:" + detail.getPnpDetailId());
				logger.error("Send PNP fail KeyManagementException | NoSuchAlgorithmException :" + e1.getMessage());
			} catch (Exception e) {
				sendFailFlag = false;//寄送失敗
				logger.info("Send PNP fail PnpDetailId:" + detail.getPnpDetailId());
				logger.error("Send PNP fail Exception:" + e.getMessage());
				if (e instanceof HttpClientErrorException) {
					logger.error("HttpClientErrorException error : " + e.getMessage());
						HttpClientErrorException exception = (HttpClientErrorException) e;
						JSONObject errorMessage = new JSONObject(exception.getResponseBodyAsString());
						if(errorMessage.has("message")) {
							logger.error("Send PNP fail HttpClientErrorException statusCode: " + exception.getStatusCode().toString());
							if(errorMessage.has("details")) {
								logger.error("Send PNP fail HttpClientErrorException details : " + errorMessage.getJSONArray("details").toString());
							}
						}
				}
			}
			detail.setPnpTime(Calendar.getInstance().getTime());
			if(sendFailFlag) {
				//發完PNP後進入此狀態   ，待web hook在24小時內收到DELIVERY則將該則訊息update成COMPLETE，若24小時內沒收到DELIVERY則將該訊息轉發SMS
				detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_CHECK_DELIVERY);
				//設定PnpDeliveryExpireTime,SMS排程將抓取status = CHECK_DELIVERY 且 now date > PnpDeliveryExpireTime 的資料
				Date date =new Date(); 
				Calendar calendar = new GregorianCalendar(); 
				calendar.setTime(date); 
				
				int expiredUnit =  CoreConfigReader.getInteger(CONFIG_STR.PNP_DELIVERY_EXPIRED_TIME_UNIT, true, false);
				int expired =  CoreConfigReader.getInteger(CONFIG_STR.PNP_DELIVERY_EXPIRED_TIME, true, false);
				
//				logger.info("==================PNP_DELIVERY_EXPIRED_TIME==================");
//				logger.info("expiredUnit :"+expiredUnit +" expired :"+expired);
//				logger.info("==================PNP_DELIVERY_EXPIRED_TIME==================");
				
				calendar.add(expiredUnit,expired); //參數化時間
				detail.setPnpDeliveryExpireTime(calendar.getTime());
			}else {
				detail.setProcStage(AbstractPnpMainEntity.STAGE_SMS);
				detail.setStatus(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
			}
			if (sendeRef != null) {
				sendeRef.tell(detail, selfActorRef);
			} else {
				pnpAkkaService.tell(detail);
			}

		}

	}
	
	
//	/**
//	 * 組合line Template message
//	 * @param templateMsg
//	 * @param actions
//	 * @param detail
//	 * @return
//	 */
//	private JSONArray combineLineTemplateMessage(PnpContentTemplateMsg templateMsg,
//			List<PnpContentTemplateMsgAction> actions, PnpDetail detail) {
//		JSONArray messageArray = new JSONArray();
//		JSONObject message = new JSONObject();
//		//  套用 template 發送
//		JSONObject templateObject = new JSONObject();
//		JSONArray columnsArray = new JSONArray();
//		JSONArray actionsArray = new JSONArray();
//		JSONObject columnObject = new JSONObject();
//		for(PnpContentTemplateMsgAction action :actions){
//			PnpContentTemplateMsgActionType type = PnpContentTemplateMsgActionType.findActionType(action.getActionType());
//			if (type != null) {
//				JSONObject actionObject = type.getJSONObject(action);
//				actionsArray.put(actionObject);
//			}else {
//				logger.error("Template Error : PnpContentTemplateMsgActionType is not support . Type:"+ action.getActionType() );
//			}
//			
//		}
//		columnObject.put("title", detail.getTitle());  
//		columnObject.put("text", detail.getText());  
//		String imageUrl = UriHelper.getResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, templateMsg.getTemplateImageId());
//		columnObject.put("thumbnailImageUrl",  imageUrl);
//		columnObject.put("actions", actionsArray);
//		columnsArray.put(columnObject);
//		
//		templateObject.put("columns", columnsArray);
//		templateObject.put("type", templateMsg.getTemplateType()); //carousel
//		
//		message.put("type", detail.getMsgType()); //template
//		message.put("altText", templateMsg.getAltText());
//		message.put("template", templateObject);
//		messageArray.put(message);
//		return messageArray;
//	}
	
	/**
	 * 組合line text message
	 * @param templateMsg
	 * @param actions
	 * @param detail
	 * @return
	 */
	private JSONArray combineLineTextMessage(PnpDetail detail) {
		JSONArray messageArray = new JSONArray();
		JSONObject message = new JSONObject();
		message.put("type", "text");
		message.put("text", detail.getMsg());
		messageArray.put(message);
		return messageArray;
	}

}
