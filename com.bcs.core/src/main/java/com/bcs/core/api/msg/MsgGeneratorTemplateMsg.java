package com.bcs.core.api.msg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.bcs.core.api.msg.model.TemplateMsg;
import com.bcs.core.api.msg.model.TemplateMsgAction;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.service.ContentTemplateMsgService;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.spring.ApplicationContextProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;

public class MsgGeneratorTemplateMsg extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;
	
	private final Map<String, List<String>> templateMsgData;
	
	private String templateType;
	private List<CarouselColumn> carouselColumns = new ArrayList<CarouselColumn>();
	private String altText = "";
	private String imageUrl = null;
	private String title = "";
	private String text = "";
	private List<Action> actions = new ArrayList<Action>();

	public MsgGeneratorTemplateMsg(ObjectNode node) {
		super();		
		String templateId = node.get("templateId").textValue();
		
		templateMsgData = getTemplateMsgData(templateId);
		
		createTemplateMsg(templateId);
	}
	
	public MsgGeneratorTemplateMsg(MsgDetail detail) throws Exception{
		super();
		String templateId = detail.getReferenceId();

		templateMsgData = getTemplateMsgData(templateId);
		
		createTemplateMsg(templateId);
	}
	
	private Map<String, List<String>> getTemplateMsgData(String templateId){
		Map<String, List<String>> templateMsgDataResult =  ApplicationContextProvider.getApplicationContext().getBean(ContentTemplateMsgService.class).getContentTemplateMsg(templateId);
		return templateMsgDataResult;
	}
	
	private void createTemplateMsg(String templateId){
		
		for (Object key : templateMsgData.keySet()) {
			List<String> templateDetail = templateMsgData.get(key);

			if(templateId.equals(key)){
				altText = templateDetail.get(0);
			}
			
			templateType = templateDetail.get(1);
			
			if(templateDetail.get(2) != null){
				imageUrl = UriHelper.getStaticResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, templateDetail.get(2));
				if(StringUtils.isBlank(imageUrl)){
					imageUrl = UriHelper.getResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, templateDetail.get(2));
				}
			}
			title = templateDetail.get(3);
			text = templateDetail.get(4);
			actions = getTemplateMsgActions(templateDetail);

			carouselColumns.add(new CarouselColumn(imageUrl, title, text, actions));
		}
	}

	@Override
	public Message getMessageBot(String toMid) throws Exception{
	
		switch(templateType){
			case TemplateMsg.TEMPLATE_TYPE_BUTTONS:
				message = new TemplateMessage(altText, new ButtonsTemplate(imageUrl, title, text, actions));
				break;
			case TemplateMsg.TEMPLATE_TYPE_CONFIRM:
				message = new TemplateMessage(altText, new ConfirmTemplate(text, actions));
				break;
			case TemplateMsg.TEMPLATE_TYPE_CAROUSEL:
				message = new TemplateMessage(altText, new CarouselTemplate(carouselColumns));
				break;
		}
		return message;
	}

	@Override
	public Message getMessageBot(String toMid, Map<String, String> replaceParam) throws Exception{
	
		switch(templateType){
			case TemplateMsg.TEMPLATE_TYPE_BUTTONS:
				message = new TemplateMessage(altText, new ButtonsTemplate(imageUrl, title, text, actions));
				break;
			case TemplateMsg.TEMPLATE_TYPE_CONFIRM:
				message = new TemplateMessage(altText, new ConfirmTemplate(text, actions));
				break;
			case TemplateMsg.TEMPLATE_TYPE_CAROUSEL:
				message = new TemplateMessage(altText, new CarouselTemplate(carouselColumns));
				break;
		}
		return message;
	}

	public Message getMessageBot(String toMid, List<CarouselColumn> carouselColumns, List<Action> actions) throws Exception{
	
		switch(templateType){
			case TemplateMsg.TEMPLATE_TYPE_BUTTONS:
				message = new TemplateMessage(altText, new ButtonsTemplate(imageUrl, title, text, actions));
				break;
			case TemplateMsg.TEMPLATE_TYPE_CONFIRM:
				message = new TemplateMessage(altText, new ConfirmTemplate(text, actions));
				break;
			case TemplateMsg.TEMPLATE_TYPE_CAROUSEL:
				message = new TemplateMessage(altText, new CarouselTemplate(carouselColumns));
				break;
		}
		return message;
	}

	public Message getMessageBot(String toMid, Map<String, String> replaceParam, List<CarouselColumn> carouselColumns, List<Action> actions) throws Exception{
	
		switch(templateType){
			case TemplateMsg.TEMPLATE_TYPE_BUTTONS:
				message = new TemplateMessage(altText, new ButtonsTemplate(imageUrl, title, text, actions));
				break;
			case TemplateMsg.TEMPLATE_TYPE_CONFIRM:
				message = new TemplateMessage(altText, new ConfirmTemplate(text, actions));
				break;
			case TemplateMsg.TEMPLATE_TYPE_CAROUSEL:
				message = new TemplateMessage(altText, new CarouselTemplate(carouselColumns));
				break;
		}
		return message;
	}
	
	private List<Action> getTemplateMsgActions(List<String> templateDetail){
		List<Action> actions = new ArrayList<Action>();
		String actionType = "";
		String actionLabel = "";
		String actionData = "";
		String actionText = "";
		String uriId = "";
		
		for(int i = 6; i<templateDetail.size(); i++){
			actionType = templateDetail.get(i++);
			actionLabel = templateDetail.get(i++);
			actionData = templateDetail.get(i++);
			actionText = templateDetail.get(i++);
			templateDetail.get(i++);
			uriId = templateDetail.get(i);
			switch (actionType){
				case TemplateMsgAction.ACTION_TYPE_URI :
					actions.add(new URIAction(actionLabel, UriHelper.getLinkPattern(uriId)));
					break;
				case TemplateMsgAction.ACTION_TYPE_MESSAGE :
					actions.add(new MessageAction(actionLabel, actionText));
					break;
				case TemplateMsgAction.ACTION_TYPE_POSTBACK :
					if(actionText != null){
						actions.add(new PostbackAction(actionLabel, actionData, actionText));
					}else{
						actions.add(new PostbackAction(actionLabel, actionData));
					}
					break;
			}
		}
		
		return actions;
	}

	public String getTemplateType() {
		return templateType;
	}

	public List<CarouselColumn> getCarouselColumns() {
		return carouselColumns;
	}

	public List<Action> getActions() {
		return actions;
	}
}
