package com.bcs.core.api.msg.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.bcs.core.api.msg.MsgGeneratorAbstract;
import com.bcs.core.api.msg.MsgGeneratorRichMsg;
import com.bcs.core.api.msg.MsgGeneratorTemplateMsg;
import com.bcs.core.api.msg.model.TemplateMsg;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.resource.UriHelper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.template.CarouselColumn;

public class MsgGeneratorBcsTemplateMsg extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;
	
	private MsgGeneratorTemplateMsg templateMsg;
	private List<Action> actions = new ArrayList<Action>();
	private List<CarouselColumn> carouselColumns = new ArrayList<CarouselColumn>();

	public static boolean canSetMulti() throws Exception {
		return MsgGeneratorRichMsg.canSetMulti();
	}

	public MsgGeneratorBcsTemplateMsg(ObjectNode node) throws Exception{
		super();
		templateMsg = new MsgGeneratorTemplateMsg(node);
		actions = Collections.unmodifiableList(templateMsg.getActions());
		carouselColumns = Collections.unmodifiableList(templateMsg.getCarouselColumns());
	}

	public MsgGeneratorBcsTemplateMsg(MsgDetail detail) throws Exception{
		super();
		templateMsg = new MsgGeneratorTemplateMsg(detail);
		actions = templateMsg.getActions();
		carouselColumns = templateMsg.getCarouselColumns();
	}

	@Override
	public Message getMessageBot(String toMid) throws Exception{
		return getMessageBot(toMid, null);
	}
	
	private List<Action> changeActionUri(String toMid, List<Action> actions, Map<String, String> replaceParam){
		
		List<Action> result = new ArrayList<Action>();
		
		for(Action actionOrg : actions){

			if (actionOrg instanceof URIAction) {
				URIAction action = (URIAction) actionOrg;
				String label = action.getLabel();
				String uri = UriHelper.parseBcsPage(action.getUri(), toMid);
				result.add(new URIAction(label, uri));
			}
			else{
				result.add(actionOrg);
			}
		}
		
		return result;
	}
	
	private List<CarouselColumn> changeCarouselColumnUri(String toMid, List<CarouselColumn> carouselColumns, Map<String, String> replaceParam){
		
		List<CarouselColumn> result = new ArrayList<CarouselColumn>();
		
		for(CarouselColumn carouselColumnOrg : carouselColumns){

			List<Action> actions = carouselColumnOrg.getActions();
			actions = this.changeActionUri(toMid, actions, replaceParam);
			String imageUrl = carouselColumnOrg.getThumbnailImageUrl();
			String title = carouselColumnOrg.getTitle();
			String text = carouselColumnOrg.getText();

			result.add(new CarouselColumn(imageUrl, title, text, actions));
		}
		
		return result;
	}

	@Override
	public Message getMessageBot(String toMid, Map<String, String> replaceParam) throws Exception{	

		switch(templateMsg.getTemplateType()){
			case TemplateMsg.TEMPLATE_TYPE_BUTTONS:
			{
				List<Action> actions = this.actions;
				actions = this.changeActionUri(toMid, actions, replaceParam);
				return templateMsg.getMessageBot(toMid, replaceParam, null, actions);
			}
			case TemplateMsg.TEMPLATE_TYPE_CONFIRM:
			{
				List<Action> actions = this.actions;
				actions = this.changeActionUri(toMid, actions, replaceParam);
				return templateMsg.getMessageBot(toMid, replaceParam, null, actions);
			}
			case TemplateMsg.TEMPLATE_TYPE_CAROUSEL:
			{
				List<CarouselColumn> carouselColumns = this.carouselColumns;
				carouselColumns = this.changeCarouselColumnUri(toMid, carouselColumns, replaceParam);
				return templateMsg.getMessageBot(toMid, replaceParam, carouselColumns, null);
			}
		}
		
		return templateMsg.getMessageBot(toMid, replaceParam);
	}
}
