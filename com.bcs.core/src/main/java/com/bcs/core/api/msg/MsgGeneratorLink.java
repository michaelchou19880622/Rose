package com.bcs.core.api.msg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.bcs.core.utils.UrlUtil;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;

public class MsgGeneratorLink extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;

	public static boolean canSetMulti() throws Exception {
		return false;
	}

	public static final String templateId = "BCS_LINK";
	public static final String templateId_Sub = "BCS_Sub_LINK";
	
	private String previewUrl;
	private String textParams;
	private String subTextParams;
	private String linkTextParams;
	private String linkUriParams;

	/**
	 * @param previewUrl
	 * @param textParams
	 * @param linkTextParams
	 * @param linkUriParams
	 * @throws Exception
	 */
	public MsgGeneratorLink(String previewUrl, String textParams, String linkTextParams, String linkUriParams) throws Exception{
		super();
		generatorContent(previewUrl, getTemplateId(null), textParams, null, linkTextParams, linkUriParams);
	}

	/**
	 * @param previewUrl
	 * @param textParams
	 * @param subTextParams
	 * @param linkTextParams
	 * @param linkUriParams
	 * @throws Exception
	 */
	public MsgGeneratorLink(String previewUrl, String textParams, String subTextParams, String linkTextParams, String linkUriParams) throws Exception{
		super();
		generatorContent(previewUrl, getTemplateId(subTextParams), textParams, subTextParams, linkTextParams, linkUriParams);
	}

	/**
	 * @param previewUrl
	 * @param templateId
	 * @param textParams
	 * @param subTextParams
	 * @param linkTextParams
	 * @param linkUriParams
	 * @throws Exception
	 */
	public MsgGeneratorLink(String previewUrl, String templateId, String textParams, String subTextParams, String linkTextParams, String linkUriParams) throws Exception{
		super();
		generatorContent(previewUrl, templateId, textParams, subTextParams, linkTextParams, linkUriParams);
	}
	
	private String getTemplateId(String subTextParams){

		if(StringUtils.isNotBlank(subTextParams)){
			return templateId_Sub;
		}
		else{
			return templateId ;
		}
	}
	
	private void generatorContent(String previewUrl, String templateId, String textParams, String subTextParams, String linkTextParams, String linkUriParams) throws Exception{
		
		if(StringUtils.isBlank(templateId)){
			throw new Exception("templateId Can not be Null");
		}

		if(StringUtils.isBlank(textParams)){
			throw new Exception("textParams Can not be Null");
		}

		if(StringUtils.isBlank(linkTextParams)){
			throw new Exception("linkTextParams Can not be Null");
		}

		if(StringUtils.isBlank(linkUriParams)){
			throw new Exception("linkUriParams Can not be Null");
		}
		
		this.previewUrl = previewUrl;
		this.textParams = textParams;
		
		if(StringUtils.isBlank(subTextParams)){
			subTextParams = "-";
		}
		
		this.subTextParams = subTextParams;
		this.linkTextParams = linkTextParams;
		this.linkUriParams = linkUriParams;
	}
	
	public void updateLinkUri(String linkUriParams){

		this.linkUriParams = linkUriParams;
	}

	@Override
	public Message getMessageBot(String toMid) throws Exception{
		return getMessageBot(toMid, null);
	}
	
	@Override
	public Message getMessageBot(String toMid, Map<String, String> replaceParam) throws Exception{
		
		String url = UrlUtil.encodeAndHash(linkUriParams, toMid, null);
		
		if(replaceParam != null && replaceParam.size() > 0){
			
			for(String replcaTarget : replaceParam.keySet()){
				url = url.replaceAll("\\{" + replcaTarget + "\\}", replaceParam.get(replcaTarget));
			}
		}
		
		List<Action> actions = new ArrayList<Action>();
		URIAction action = new URIAction(linkTextParams, url);
		actions.add(action);
		
		ButtonsTemplate template = new ButtonsTemplate(previewUrl, textParams, subTextParams, actions);
		
		message = new TemplateMessage("連結訊息: " + textParams + "\n請使用手機查看", template);
		return message;
	}
}
