package com.bcs.core.taishin.circle.PNP.akka.model;

import java.util.ArrayList;
import java.util.List;

import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;

public class AsyncSendingClusterModel extends AbstractBcsEntity{
	
	private static final long serialVersionUID = 1L;
	
	private String ChannelId;
	private List<MsgDetail> msgDetails;
	private List<String> mids;
	private String apiType;
	private Long updateMsgId;
	private List<PnpDetail> pnpDetails;
	
	public AsyncSendingClusterModel(){
		
	}
	
	public AsyncSendingClusterModel(String ChannelId, List<MsgDetail> msgDetails, List<String> mids, String apiType){
		this.ChannelId = ChannelId;
		this.setMsgDetails(msgDetails);
		this.mids = mids;
		this.apiType = apiType;
		this.updateMsgId = null;
	}
	
	public AsyncSendingClusterModel(String ChannelId, List<MsgDetail> msgDetails, List<String> mids, String apiType, Long updateMsgId){
		this.ChannelId = ChannelId;
		this.setMsgDetails(msgDetails);
		this.mids = mids;
		this.apiType = apiType;
		this.updateMsgId = updateMsgId;
	}
	
	public AsyncSendingClusterModel(String ChannelId, List<MsgDetail> msgDetails, String mid, String apiType){
		this.ChannelId = ChannelId;
		this.setMsgDetails(msgDetails);
		
		List<String> mids = new ArrayList<String>();
		mids.add(mid);
		this.mids = mids;
		
		this.apiType = apiType;
	}
	
	public AsyncSendingClusterModel(String ChannelId, List<MsgDetail> msgDetails, String mid, String apiType, Long updateMsgId){
		this.ChannelId = ChannelId;
		this.setMsgDetails(msgDetails);
		
		List<String> mids = new ArrayList<String>();
		mids.add(mid);
		this.mids = mids;
		
		this.apiType = apiType;
		this.updateMsgId = updateMsgId;
	}
	
   public AsyncSendingClusterModel(List<PnpDetail> pnpDetails, String ChannelId, String apiType){
        this.ChannelId = ChannelId;
        this.pnpDetails = pnpDetails;
        this.apiType = apiType;
    }

	public String getChannelId() {
		return ChannelId;
	}

	public List<String> getMids() {
		return mids;
	}

	public String getApiType() {
		return apiType;
	}

	public Long getUpdateMsgId() {
		return updateMsgId;
	}

	public void setChannelId(String channelId) {
		ChannelId = channelId;
	}

	public void setMids(List<String> mids) {
		this.mids = mids;
	}

	public void setApiType(String apiType) {
		this.apiType = apiType;
	}

	public void setUpdateMsgId(Long updateMsgId) {
		this.updateMsgId = updateMsgId;
	}

	public List<MsgDetail> getMsgDetails() {
		return msgDetails;
	}

	public void setMsgDetails(List<MsgDetail> msgDetails) {
		this.msgDetails = msgDetails;
	}

    public List<PnpDetail> getPnpDetails() {
        return pnpDetails;
    }

    public void setPnpDetails(List<PnpDetail> pnpDetails) {
        this.pnpDetails = pnpDetails;
    }
	
}
