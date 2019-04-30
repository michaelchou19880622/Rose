package com.bcs.core.api.msg;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.bcs.core.api.msg.model.RichMsgAction;
import com.linecorp.bot.model.message.ImagemapMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.imagemap.ImagemapAction;
import com.linecorp.bot.model.message.imagemap.ImagemapArea;
import com.linecorp.bot.model.message.imagemap.ImagemapBaseSize;
import com.linecorp.bot.model.message.imagemap.MessageImagemapAction;
import com.linecorp.bot.model.message.imagemap.URIImagemapAction;

public class MsgGeneratorRichMsg extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;

	private int height;
	private List<RichMsgAction> actionsData;
	private List<ImagemapAction> actions;
	
	private String DOWNLOAD_URL;
	private String ALT_TEXT;
	private ImagemapBaseSize size;
	
	public static boolean canSetMulti() throws Exception {
		return true;
	}

	public MsgGeneratorRichMsg(String DOWNLOAD_URL, String ALT_TEXT, int height, final List<RichMsgAction> actionsData) throws Exception{
		super();
		this.generatorContent(DOWNLOAD_URL, ALT_TEXT, height, actionsData, 1);
	}
	
	public MsgGeneratorRichMsg(String DOWNLOAD_URL, String ALT_TEXT, int height, List<RichMsgAction> actionsData, int toType) throws Exception{
		super();
		this.generatorContent(DOWNLOAD_URL, ALT_TEXT, height, actionsData, toType);
	}
	
	private void generatorContent(String DOWNLOAD_URL, String ALT_TEXT, int height, final List<RichMsgAction> actionsData, int toType) throws Exception{

		if(StringUtils.isBlank(DOWNLOAD_URL)){
			throw new Exception("DOWNLOAD_URL Can not be Null");
		}

		if(StringUtils.isBlank(ALT_TEXT)){
			throw new Exception("ALT_TEXT Can not be Null");
		}
		
		if(height < 1){
			throw new Exception("height is small than 1");
		}
		
		if(height > 2080){
			throw new Exception("height is larger than 2080");
		}
		
		this.height = height;

		generatorMarkupJson(height, actionsData);
		
		ImagemapBaseSize size = new ImagemapBaseSize(height, 1040);

		this.DOWNLOAD_URL = DOWNLOAD_URL;
		this.ALT_TEXT = ALT_TEXT;
		this.size = size;
	}

	@Override
	public Message getMessageBot(String toMid) throws Exception{
		message = new ImagemapMessage(DOWNLOAD_URL, ALT_TEXT, size, actions);
		return message;
	}

	@Override
	public Message getMessageBot(String toMid, Map<String, String> replaceParam) throws Exception{
		message = new ImagemapMessage(DOWNLOAD_URL, ALT_TEXT, size, actions);
		return message;
	}

	public Message getMessageBot(String toMid, List<RichMsgAction> actions) throws Exception{
		message = new ImagemapMessage(DOWNLOAD_URL, ALT_TEXT, size, generatorMarkupJsonNow(height, actions));
		return message;
	}

	public Message getMessageBot(String toMid, Map<String, String> replaceParam, List<RichMsgAction> actions) throws Exception{
		message = new ImagemapMessage(DOWNLOAD_URL, ALT_TEXT, size, generatorMarkupJsonNow(height, actions));
		return message;
	}
	
	public void updateActionsData(List<RichMsgAction> actionsData) throws Exception{
		generatorMarkupJson(height, actionsData);
	}
	
	private void generatorMarkupJson(int height, List<RichMsgAction> actionsData) throws Exception{

		if(height < 1){
			throw new Exception("height Can not be Null");
		}
		
		if(actionsData == null || actionsData.size() == 0){
			throw new Exception("RichMsgAction Can not be Null");
		}
		this.actionsData  = actionsData;

		actions = new ArrayList<ImagemapAction>();
		
		// For Loop Add action
		for(RichMsgAction actionData : actionsData){
			
			if(RichMsgAction.ACTION_TYPE_WEB.equals(actionData.getActionType())){
				ImagemapArea area = new ImagemapArea(actionData.getActionX(), actionData.getActionY(), actionData.getActionWidth(), actionData.getActionHeight());
				URIImagemapAction botAction = new URIImagemapAction(actionData.getActionContent(), area);
				
				this.actions.add(botAction);
			}
			else if(RichMsgAction.ACTION_TYPE_SEND_MESSAGE.equals(actionData.getActionType())){
				ImagemapArea area = new ImagemapArea(actionData.getActionX(), actionData.getActionY(), actionData.getActionWidth(), actionData.getActionHeight());
				MessageImagemapAction botAction = new MessageImagemapAction(actionData.getActionContent(), area);

				this.actions.add(botAction);
			}
		}
	}
	
	private ArrayList<ImagemapAction> generatorMarkupJsonNow(int height, List<RichMsgAction> actionsData) throws Exception{

		if(height < 1){
			throw new Exception("height Can not be Null");
		}
		
		if(actionsData == null || actionsData.size() == 0){
			throw new Exception("RichMsgAction Can not be Null");
		}

		ArrayList<ImagemapAction> actions = new ArrayList<ImagemapAction>();
		
		// For Loop Add action
		for(RichMsgAction actionData : actionsData){
			
			if(RichMsgAction.ACTION_TYPE_WEB.equals(actionData.getActionType())){
				ImagemapArea area = new ImagemapArea(actionData.getActionX(), actionData.getActionY(), actionData.getActionWidth(), actionData.getActionHeight());
				URIImagemapAction botAction = new URIImagemapAction(actionData.getActionContent(), area);
				
				actions.add(botAction);
			}
			else if(RichMsgAction.ACTION_TYPE_SEND_MESSAGE.equals(actionData.getActionType())){
				ImagemapArea area = new ImagemapArea(actionData.getActionX(), actionData.getActionY(), actionData.getActionWidth(), actionData.getActionHeight());
				MessageImagemapAction botAction = new MessageImagemapAction(actionData.getActionContent(), area);

				actions.add(botAction);
			}
		}
		
		return actions;
	}

	public List<RichMsgAction> getActionsData() {
		return actionsData;
	}
}
