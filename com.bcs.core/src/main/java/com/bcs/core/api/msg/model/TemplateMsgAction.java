package com.bcs.core.api.msg.model;

import com.bcs.core.json.AbstractBcsEntity;

public class TemplateMsgAction extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	public static final String ACTION_TYPE_URI = "uri";
	public static final String ACTION_TYPE_MESSAGE = "message";
	public static final String ACTION_TYPE_POSTBACK = "postback";
}
