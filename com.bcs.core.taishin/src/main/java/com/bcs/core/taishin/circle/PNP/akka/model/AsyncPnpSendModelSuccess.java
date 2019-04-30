package com.bcs.core.taishin.circle.PNP.akka.model;

import java.util.Date;
import java.util.List;

import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;

public class AsyncPnpSendModelSuccess extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	private String ChannelId;
	private API_TYPE apiType;
	private List<PnpDetail> pnpDetails;
	private Date date;
	
	public AsyncPnpSendModelSuccess(String ChannelId, API_TYPE apiType, List<PnpDetail> pnpDetails, Date date){
		this.ChannelId = ChannelId;
		this.pnpDetails = pnpDetails;
		this.apiType = apiType;
		this.date = date;
	}

	public String getChannelId() {
		return ChannelId;
	}

	public List<PnpDetail> getPnpDetails() {
		return pnpDetails;
	}

	public API_TYPE getApiType() {
		return apiType;
	}

	public Date getDate() {
		return date;
	}
}
