package com.bcs.core.taishin.circle.PNP.ftp;

import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;

public enum PNPFTPType {
	
	MITAKE("mitake",AbstractPnpMainEntity.SOURCE_MITAKE),
	EVERY8D("every8d", AbstractPnpMainEntity.SOURCE_EVERY8D),
	MING("ming" , AbstractPnpMainEntity.SOURCE_MING),
	UNICA("unica", AbstractPnpMainEntity.SOURCE_UNICA);
	
	private String source;
	private String value;
    
	PNPFTPType(String value, String source) {
        this.value = value;
    }
	
	public String toString() {
		return value;
	}
	
	public String getSource() {
		return source;
	}
}
