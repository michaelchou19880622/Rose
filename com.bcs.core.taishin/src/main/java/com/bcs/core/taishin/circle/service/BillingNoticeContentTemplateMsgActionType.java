package com.bcs.core.taishin.circle.service;

import org.json.JSONObject;

import com.bcs.core.resource.UriHelper;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsgAction;

/**
 * 根據 BillingNoticeContentTemplateMsgActionType 組出JSONObject
 * @author jessie
 *
 */
public enum BillingNoticeContentTemplateMsgActionType {
	MESSAGE{
	    @Override
	    public JSONObject getJSONObject(BillingNoticeContentTemplateMsgAction action, String MID) {
	    	JSONObject actionObject = new JSONObject();
			actionObject.put("type", action.getActionType());
            actionObject.put("label", action.getActionLabel());
            actionObject.put("text", action.getActionText());
            return actionObject;
            
	    }
	},  URI{
		@Override
	    public JSONObject getJSONObject(BillingNoticeContentTemplateMsgAction action, String MID) {
			String uri = UriHelper.getLinkUri( action.getLinkId(), MID);
	    	JSONObject actionObject = new JSONObject();
			actionObject.put("type", action.getActionType());
            actionObject.put("label", action.getActionLabel());
            actionObject.put("uri", uri);
            return actionObject;
	    }
	}, POSTBACK{
		@Override
	    public JSONObject getJSONObject(BillingNoticeContentTemplateMsgAction action, String MID) {
	    	JSONObject actionObject = new JSONObject();
			actionObject.put("type", action.getActionType());
            actionObject.put("label", action.getActionLabel());
            actionObject.put("text", action.getActionText());
        	actionObject.put("data", action.getActionData());
            return actionObject;
	    }
	};
	
	public abstract JSONObject getJSONObject(BillingNoticeContentTemplateMsgAction action, String MID);
	
	/**
	 * find Action Type
	 * @param type
	 * @return
	 */
	public static BillingNoticeContentTemplateMsgActionType findActionType(String type) {
		for(BillingNoticeContentTemplateMsgActionType actionType: values()) {
			if (actionType.toString().toLowerCase().equals(type)) {
				return actionType;
			}
		}
		return null;
	}
}
