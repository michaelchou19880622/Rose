package com.bcs.core.taishin.circle.PNP.akka.model;

import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ???
 */
@Getter
@Setter
@NoArgsConstructor
public class AsyncSendingClusterModel extends AbstractBcsEntity {

    private static final long serialVersionUID = 1L;

    private String channelId;
    private List<MsgDetail> msgDetails;
    private List<String> midList;
    private String apiType;
    private Long updateMsgId;
    private List<PnpDetail> pnpDetails;


    public AsyncSendingClusterModel(String channelId, List<MsgDetail> msgDetails, List<String> midList, String apiType) {
        this.channelId = channelId;
        this.msgDetails = msgDetails;
        this.midList = midList;
        this.apiType = apiType;
        this.updateMsgId = null;
    }

    public AsyncSendingClusterModel(String channelId, List<MsgDetail> msgDetails, List<String> midList, String apiType, Long updateMsgId) {
        this.channelId = channelId;
        this.msgDetails = msgDetails;
        this.midList = midList;
        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
    }

    public AsyncSendingClusterModel(String channelId, List<MsgDetail> msgDetails, String mid, String apiType) {
        this.channelId = channelId;
        this.msgDetails = msgDetails;

        List<String> list = new ArrayList<>();
        list.add(mid);
        this.midList = list;

        this.apiType = apiType;
    }

    public AsyncSendingClusterModel(String channelId, List<MsgDetail> msgDetails, String mid, String apiType, Long updateMsgId) {
        this.channelId = channelId;
        this.msgDetails = msgDetails;

        List<String> list = new ArrayList<>();
        list.add(mid);
        this.midList = list;

        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
    }

    public AsyncSendingClusterModel(List<PnpDetail> pnpDetails, String channelId, String apiType) {
        this.channelId = channelId;
        this.pnpDetails = pnpDetails;
        this.apiType = apiType;
    }
}
