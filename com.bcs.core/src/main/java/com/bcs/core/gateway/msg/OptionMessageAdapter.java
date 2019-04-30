package com.bcs.core.gateway.msg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.db.entity.ContentTemplateMsg;
import com.bcs.core.db.entity.ContentTemplateMsgAction;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.repository.ContentTemplateMsgActionRepository;
import com.bcs.core.db.repository.ContentTemplateMsgRepository;
import com.bcs.core.db.service.ContentTemplateMsgService;
import com.bcs.core.spring.ApplicationContextProvider;

public class OptionMessageAdapter extends MessageAdapter {
	private static final long serialVersionUID = 1L;
	
	ContentTemplateMsgActionRepository contentTemplateMsgActionRepository = ApplicationContextProvider.getApplicationContext().getBean(ContentTemplateMsgActionRepository.class);
	ContentTemplateMsgRepository contentTemplateMsgRepository = ApplicationContextProvider.getApplicationContext().getBean(ContentTemplateMsgRepository.class);
	ContentTemplateMsgService contentTemplateMsgService = ApplicationContextProvider.getApplicationContext().getBean(ContentTemplateMsgService.class);
	
	private MsgDetail message = null;
	private String patternRegex = "(?i)<br */?>";
	private String newline = "\n";
	
	public OptionMessageAdapter(JSONObject message) throws Exception {
		JSONArray optionList = message.getJSONArray("showOptions");
		messageList = new ArrayList<MsgDetail>();
		
		this.message = new MsgDetail();
		this.message.setMsgType(MsgGenerator.MSG_TYPE_TEXT);
		this.message.setText(message.getString("showContent").replaceAll(patternRegex, newline));
		
		messageList.add(this.message);
		
		String templateMsgId = generateTemplateMessage(optionList);
		
		this.message = new MsgDetail();
		
		this.message.setMsgType(MsgGenerator.MSG_TYPE_TEMPLATE);
		this.message.setReferenceId(templateMsgId);
		messageList.add(this.message);
	}
	
	/*
	 * 產生樣板訊息 
	*/
	private String generateTemplateMessage(JSONArray optionList) throws Exception {
		boolean isImageOptionMsg = optionList.getJSONObject(0).has("image");
		
		if(isImageOptionMsg) {
			return generateCarouselTemplateMsg(optionList);	// Carousel Template Message
		} else {
			return generateButtonsTemplateMsg(optionList);	// Buttons Template Message
		}
	}
	
	private String generateButtonsTemplateMsg(JSONArray optionList) {
		List<String> optionTextList = new ArrayList<String>();
		List<ContentTemplateMsgAction> templateMsgActionList = new ArrayList<>();	
		ContentTemplateMsg templateMsg = null;
		ContentTemplateMsgAction templateMsgAction = null;
		String templateMsgText = "";
		
		for(int i = 0; i < optionList.length(); i++) {
			optionTextList.add(optionList.getJSONObject(i).getString("title"));
		}
		
		templateMsg = new ContentTemplateMsg();
		
		String templateMsgId = checkDuplicateUUID("1");
		templateMsg.setTemplateId(templateMsgId);
		templateMsg.setAltText("請至手機上確認訊息內容。");
		templateMsg.setTemplateType("buttons");
		templateMsg.setTemplateLevel("MAIN");
		templateMsg.setModifyUser("BOT");
		templateMsg.setModifyTime(new Date());
		templateMsg.setStatus(ContentTemplateMsg.STATUS_AUTOREPLY);
		templateMsg.setTemplateLetter("0");
		templateMsg.setTemplateTitle("您可能也想問：");
		
		for(int i = 0; i < optionTextList.size(); i++) {
			templateMsgAction = new ContentTemplateMsgAction();
			
			templateMsgAction.setTemplateIdAction(checkDuplicateUUID("2"));
			templateMsgAction.setTemplateId(templateMsgId);
			templateMsgAction.setActionLetter(Character.toString((char)(65 + i)));
			templateMsgAction.setActionType("message");
			templateMsgAction.setActionLabel("選項 " + Character.toString((char)(65 + i)));
			templateMsgAction.setActionText(optionTextList.get(i));
			templateMsgAction.setStatus(ContentTemplateMsgAction.STATUS_AUTOREPLY);
			
			templateMsgActionList.add(templateMsgAction);
			
			templateMsgText += (Character.toString((char)(65 + i)) + ". " + optionTextList.get(i) + "\n");
		}
		
		templateMsg.setTemplateText(templateMsgText);
		
		for(int j = 0; j < templateMsgActionList.size(); j++){
    		contentTemplateMsgActionRepository.save(templateMsgActionList.get(j));
    	}
		
    	return contentTemplateMsgRepository.save(templateMsg).getTemplateId();
	}
	
	private String generateCarouselTemplateMsg(JSONArray optionList) throws Exception {
		ContentTemplateMsg templateMsg = null;
		ContentTemplateMsgAction templateMsgAction = null;
		ImageMessageAdapter imageMessageAdapter = new ImageMessageAdapter();
		String templateMsgId = null;
		String imageResourceId = null;
		String templateParentId = null;
		
		for(int i = 0; i < optionList.length(); i++) {
			templateMsg = new ContentTemplateMsg();
			
			imageResourceId = imageMessageAdapter.getImageResourceId(optionList.getJSONObject(i).getString("image"));
			templateMsgId = checkDuplicateUUID("1");
			
			templateMsg.setTemplateId(templateMsgId);
			templateMsg.setAltText("請至手機上確認訊息內容。");
			templateMsg.setTemplateImageId(imageResourceId);
			templateMsg.setTemplateText("您可能也想問：");
			templateMsg.setTemplateType("carousel");
			templateMsg.setTemplateLevel((i == 0) ? "MAIN" : "COLUMN");
			templateMsg.setModifyUser("BOT");
			templateMsg.setModifyTime(new Date());
			templateMsg.setStatus(ContentTemplateMsg.STATUS_AUTOREPLY);
			templateMsg.setTemplateLetter(Integer.toString(i));
			
			if(i == 0)
				templateParentId = templateMsgId;
			else
				templateMsg.setTemplateParentId(templateParentId);
			
			templateMsgAction = new ContentTemplateMsgAction();
			
			templateMsgAction.setTemplateIdAction(checkDuplicateUUID("2"));
			templateMsgAction.setTemplateId(templateMsgId);
			templateMsgAction.setActionLetter("A");
			templateMsgAction.setActionType("message");
			templateMsgAction.setActionLabel(optionList.getJSONObject(i).getString("title"));
			templateMsgAction.setActionText(optionList.getJSONObject(i).getString("title"));
			templateMsgAction.setStatus(ContentTemplateMsgAction.STATUS_AUTOREPLY);
			
			contentTemplateMsgActionRepository.save(templateMsgAction);
		
			contentTemplateMsgRepository.save(templateMsg).getTemplateId();
		}
		
		return templateParentId;
	}
	
	/*
	 * 產生一組 UUID
	 */
	private String checkDuplicateUUID(String queryType) {
		String uuid = UUID.randomUUID().toString().toLowerCase();
		Boolean duplicateUUID = contentTemplateMsgService.checkDuplicateUUID(queryType, uuid);
		while (duplicateUUID) {
			uuid = UUID.randomUUID().toString().toLowerCase();
			duplicateUUID = contentTemplateMsgService.checkDuplicateUUID(queryType, uuid);
		}
		
		return uuid;
	}
}
