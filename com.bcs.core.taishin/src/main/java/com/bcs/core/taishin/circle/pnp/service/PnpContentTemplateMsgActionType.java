package com.bcs.core.taishin.circle.pnp.service;

import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsgAction;
import org.json.JSONObject;

/**
 * 根據 BillingNoticeContentTemplateMsgActionType 組出JSONObject
 *
 * @author jessie
 */
public enum PnpContentTemplateMsgActionType {
    MESSAGE {
        @Override
        public JSONObject getJSONObject(BillingNoticeContentTemplateMsgAction action) {
            JSONObject actionObject = new JSONObject();
            actionObject.put("type", action.getActionType());
            actionObject.put("label", action.getActionLabel());
            actionObject.put("text", action.getActionText());
            return actionObject;

        }
    }, URI {
        @Override
        public JSONObject getJSONObject(BillingNoticeContentTemplateMsgAction action) {
            JSONObject actionObject = new JSONObject();
            actionObject.put("type", action.getActionType());
            actionObject.put("label", action.getActionLabel());
            actionObject.put("uri", action.getLinkId());
            return actionObject;
        }
    }, POSTBACK {
        @Override
        public JSONObject getJSONObject(BillingNoticeContentTemplateMsgAction action) {
            JSONObject actionObject = new JSONObject();
            actionObject.put("type", action.getActionType());
            actionObject.put("label", action.getActionLabel());
            actionObject.put("text", action.getActionText());
            actionObject.put("data", action.getActionData());
            return actionObject;
        }
    };

    public abstract JSONObject getJSONObject(BillingNoticeContentTemplateMsgAction action);

    /**
     * find Action Type
     *
     * @param type
     * @return
     */
    public static PnpContentTemplateMsgActionType findActionType(String type) {
        for (PnpContentTemplateMsgActionType actionType : values()) {
            if (actionType.toString().toLowerCase().equals(type)) {
                return actionType;
            }
        }
        return null;
    }
}
