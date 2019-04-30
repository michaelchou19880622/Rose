package com.bcs.core.taishin.circle.PNP.akka.model;

import java.util.List;

import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;

public class AsyncPnpSendModel extends AbstractBcsEntity{
	
	private static final long serialVersionUID = 1L;
	
	private String ChannelId;
	private API_TYPE apiType;
	private List<PnpDetail> pnpDetails;
	
	public AsyncPnpSendModel(String ChannelId, API_TYPE apiType, List<PnpDetail> pnpDetails){
		this.ChannelId = ChannelId;
		this.apiType = apiType;
		this.pnpDetails = pnpDetails;
	}

	public String getChannelId() {
		return ChannelId;
	}

	public API_TYPE getApiType() {
	    return apiType;
	}
	
	public List<PnpDetail> getPnpDetails() {
		return pnpDetails;
	}

}
