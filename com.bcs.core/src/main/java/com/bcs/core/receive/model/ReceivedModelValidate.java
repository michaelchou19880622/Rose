package com.bcs.core.receive.model;

import com.bcs.core.json.AbstractBcsEntity;


public class ReceivedModelValidate extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	private ReceivedModelOriginal msg;
	private boolean isPass;
	
	public ReceivedModelValidate(ReceivedModelOriginal msg, boolean isPass){
		this.msg = msg;
		this.isPass = isPass;
	}

	public ReceivedModelOriginal getMsg() {
		return msg;
	}

	public boolean isPass() {
		return isPass;
	}
}
