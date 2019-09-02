package com.bcs.core.api.msg.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.bcs.core.api.msg.MsgGeneratorAbstract;
import com.bcs.core.api.msg.MsgGeneratorRichMsg;
import com.bcs.core.api.msg.model.RichMsgAction;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.service.ContentRichMsgService;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.FileUtil;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.message.Message;

public class MsgGeneratorBcsRichMsg extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(MsgGeneratorBcsRichMsg.class);
	
	private MsgGeneratorRichMsg richMsg;
	
	private final List<String> richMsgData;
	private final List<RichMsgAction> actionsData;

	public static boolean canSetMulti() throws Exception {
		return MsgGeneratorRichMsg.canSetMulti();
	}

	public MsgGeneratorBcsRichMsg(ObjectNode node) throws Exception{
		super();
		String richId = node.get("richId").textValue();
		
		richMsgData = getRichMsgData(richId);
		actionsData = getRichMsgAction();
		
		createBcsRichMsg(richId);
	}

	public MsgGeneratorBcsRichMsg(MsgDetail detail) throws Exception{
		super();
		String richId = detail.getReferenceId();

		richMsgData = getRichMsgData(richId);
		actionsData = getRichMsgAction();
		
		createBcsRichMsg(richId);
	}
	
	private List<String> getRichMsgData(String richId){
		Map<String, List<String>> richMsgDataResult =  ApplicationContextProvider.getApplicationContext().getBean(ContentRichMsgService.class).getContentRichMsg(richId);
		return richMsgDataResult.get(richId);
	}
	
	private List<RichMsgAction> getRichMsgAction(){

		logger.debug("richMsgData-" + richMsgData);

		List<RichMsgAction> actionsData = generateAction(richMsgData);
		
		return Collections.unmodifiableList(actionsData);
	}

	private void createBcsRichMsg(String richId) throws Exception{
		
		String DOWNLOAD_URL = UriHelper.getStaticResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, FileUtil.RESIZE_PRE + richMsgData.get(2));
		if(StringUtils.isBlank(DOWNLOAD_URL)){
			DOWNLOAD_URL = UriHelper.getCdnResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, richMsgData.get(2));
			//DOWNLOAD_URL = UriHelper.getResourceUri(ContentResource.RESOURCE_TYPE_IMAGE, richMsgData.get(2));
		}
		
		String ALT_TEXT = richMsgData.get(1);
		int heightTotal =  Integer.parseInt(richMsgData.get(10));

		richMsg = new MsgGeneratorRichMsg(DOWNLOAD_URL,ALT_TEXT,heightTotal, actionsData );
	}

	@Override
	public Message getMessageBot(String toMid) throws Exception{
		return getMessageBot(toMid, null);
	}

	@Override
	public Message getMessageBot(String toMid, Map<String, String> replaceParam) throws Exception{	
		// Change Url
		List<RichMsgAction> actions = new ArrayList<RichMsgAction>();
		
		for(final RichMsgAction action : actionsData){

			RichMsgAction clone = ((RichMsgAction)action.clone());
			
			if(StringUtils.isBlank(action.getActionType()) || RichMsgAction.ACTION_TYPE_WEB.equals(action.getActionType())){
				String uri  = UriHelper.parseBcsPage(action.getActionContent(), toMid) ;
				
				clone.setActionContent(uri);
			}
			actions.add(clone);
		}
		
		return richMsg.getMessageBot(toMid, replaceParam, actions);
	}
	
	private List<RichMsgAction> generateAction(List<String> list){

		List<RichMsgAction> actionsData = new ArrayList<RichMsgAction>();

		String[] links = new String[]{""};
		if(list.get(3) != null){
			links = list.get(3).split(",");
		}
		String[] titles = new String[]{""};
		if(list.get(4) != null){
			titles = list.get(4).split(",");
		}
		String[] xStarts = list.get(6).split(",");
		String[] yStarts = list.get(7).split(",");
		String[] xEnds = list.get(8).split(",");
		String[] yEnds = list.get(9).split(",");
		String[] linkIds = list.get(12).split(",");
		String[] actionTypes = null;
		if(list.get(14) != null){
			actionTypes = list.get(14).split(",");
		}
		else{
			actionTypes = new String[]{"web"};
		}
		for(int i = 0; i < links.length; i++){
			String link = "";
			if(RichMsgAction.ACTION_TYPE_SEND_MESSAGE.equals(actionTypes[i])){
				link = linkIds[i];
			}
			else{
				link = UriHelper.getLinkPattern(linkIds[i]);
			}
			String title = links[i];
			try{
				if(titles != null && titles.length > i){
					title = titles[i];
				}
			}
			catch(Exception e){} // Skip

			String actionType = actionTypes[i];
			if(StringUtils.isBlank(actionType)){
				actionType = RichMsgAction.ACTION_TYPE_WEB;
			}
			
			int x = Integer.parseInt(xStarts[i]);
			int y = Integer.parseInt(yStarts[i]);
			int width = Integer.parseInt(xEnds[i]) - Integer.parseInt(xStarts[i]);
			int height = Integer.parseInt(yEnds[i]) - Integer.parseInt(yStarts[i]);
			final RichMsgAction action = new RichMsgAction("link" + i, actionType , title, link, x, y, width,height);
			actionsData.add(action);
		}
		
		return actionsData;
	}
}
