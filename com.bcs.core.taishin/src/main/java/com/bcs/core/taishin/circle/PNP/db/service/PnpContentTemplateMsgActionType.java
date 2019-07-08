package com.bcs.core.taishin.circle.PNP.db.service;

import org.json.JSONObject;

import com.bcs.core.resource.UriHelper;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpContentTemplateMsgAction;

/**
 * 根據 BillingNoticeContentTemplateMsgActionType 組出JSONObject
 * @author jessie
 *
 */
public enum PnpContentTemplateMsgActionType {
	MESSAGE{
	    @Override
	    public JSONObject getJSONObject(PnpContentTemplateMsgAction action, String MID) {
	    	JSONObject actionObject = new JSONObject();
			actionObject.put("type", action.getActionType());
            actionObject.put("label", action.getActionLabel());
            actionObject.put("text", action.getActionText());
            return actionObject;
            
	    }
	},  URI{
		@Override
	    public JSONObject getJSONObject(PnpContentTemplateMsgAction action, String MID) {
			String uri = UriHelper.getLinkUri( action.getLinkId(), MID);
	    	JSONObject actionObject = new JSONObject();
			actionObject.put("type", action.getActionType());
            actionObject.put("label", action.getActionLabel());
            actionObject.put("uri", uri);
            return actionObject;
	    }
	}, POSTBACK{
		@Override
	    public JSONObject getJSONObject(PnpContentTemplateMsgAction action, String MID) {
	    	JSONObject actionObject = new JSONObject();
			actionObject.put("type", action.getActionType());
            actionObject.put("label", action.getActionLabel());
            actionObject.put("text", action.getActionText());
        	actionObject.put("data", action.getActionData());
            return actionObject;
	    }
	};
	
	public abstract JSONObject getJSONObject(PnpContentTemplateMsgAction action, String MID);
	
	/**
	 * find Action Type
	 * @param type
	 * @return
	 */
	public static PnpContentTemplateMsgActionType findActionType(String type) {
		for(PnpContentTemplateMsgActionType actionType: values()) {
			if (actionType.toString().toLowerCase().equals(type)) {
				return actionType;
			}
		}
		return null;
	}
}
