package com.bcs.core.gateway.msg;

import java.util.ArrayList;

import org.json.JSONObject;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.db.entity.MsgDetail;

public class WebMessageAdapter extends MessageAdapter {
	private static final long serialVersionUID = 1L;
	
	private MsgDetail message = null;
	private String patternRegex = "(?i)<br */?>";
	private String newline = "\n";
	
	public WebMessageAdapter(JSONObject message) throws Exception {
		messageList = new ArrayList<MsgDetail>();
		
		this.message = new MsgDetail();
		
		this.message.setMsgType(MsgGenerator.MSG_TYPE_TEXT);
		this.message.setText(message.getString("showContent").replaceAll(patternRegex, newline) + "\n" + message.getString("showUrl"));
		
		messageList.add(this.message);
	}
}