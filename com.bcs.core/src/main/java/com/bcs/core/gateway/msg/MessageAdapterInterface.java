package com.bcs.core.gateway.msg;

import java.util.List;

import com.bcs.core.db.entity.MsgDetail;

public interface MessageAdapterInterface {
	public static final String MSG_TYPE_TEXT = "Text";
	public static final String MSG_TYPE_IMAGE = "Image";
	public static final String MSG_TYPE_VIDEO = "Video";
	public static final String MSG_TYPE_WEB = "Web";
	public static final String MSG_TYPE_OPTION = "Option";

	public abstract List<MsgDetail> getMessageList();
}
