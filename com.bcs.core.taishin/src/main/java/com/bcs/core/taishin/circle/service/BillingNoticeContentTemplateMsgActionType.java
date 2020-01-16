package com.bcs.core.taishin.circle.service;

import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentLink;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsgAction;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentLinkRepository;
import org.json.JSONObject;

/**
 * 根據 BillingNoticeContentTemplateMsgActionType 組出JSONObject
 *
 * @author jessie
 */
public enum BillingNoticeContentTemplateMsgActionType {
    MESSAGE {
        @Override
        public JSONObject getJSONObject(BillingNoticeContentTemplateMsgAction action, String MID) {
            JSONObject actionObject = new JSONObject();
            actionObject.put("type", action.getActionType());
            actionObject.put("label", action.getActionLabel());
            actionObject.put("text", action.getActionText());
            return actionObject;
        }
    }, URI {
        @Override
        public JSONObject getJSONObject(BillingNoticeContentTemplateMsgAction action, String MID) {
            BillingNoticeContentLinkRepository billingNoticeContentLinkRepository = ApplicationContextProvider.getApplicationContext().getBean(BillingNoticeContentLinkRepository.class);
            BillingNoticeContentLink billingNoticeContentLink = billingNoticeContentLinkRepository.findOne(action.getLinkId());
            String uri = billingNoticeContentLink.getLinkUrl();

            // Original
            //String uri = UriHelper.getLinkUri( action.getLinkId(), MID);

            JSONObject actionObject = new JSONObject();
            actionObject.put("type", action.getActionType());
            actionObject.put("label", action.getActionLabel());
            actionObject.put("uri", uri);
            return actionObject;
        }
    }, POSTBACK {
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
     *
     * @param type
     * @return
     */
    public static BillingNoticeContentTemplateMsgActionType findActionType(String type) {
        for (BillingNoticeContentTemplateMsgActionType actionType : values()) {
            if (actionType.toString().toLowerCase().equals(type)) {
                return actionType;
            }
        }
        return null;
    }
}
