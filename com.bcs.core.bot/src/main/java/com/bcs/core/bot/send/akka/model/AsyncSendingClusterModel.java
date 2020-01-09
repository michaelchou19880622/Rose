package com.bcs.core.bot.send.akka.model;

import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.json.AbstractBcsEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ???
 */
@EqualsAndHashCode(callSuper = false)
@Getter
public class AsyncSendingClusterModel extends AbstractBcsEntity {

    private static final long serialVersionUID = 1L;

    private String channelId;
    private List<MsgDetail> msgDetailList;
    private List<String> midList;
    private String apiType;
    private Long updateMsgId;

    public AsyncSendingClusterModel() {
    }

    public AsyncSendingClusterModel(String channelId, List<MsgDetail> msgDetailList, List<String> midList, String apiType) {
        this.channelId = channelId;
        this.msgDetailList = msgDetailList;
        this.midList = midList;
        this.apiType = apiType;
        this.updateMsgId = null;
    }

    public AsyncSendingClusterModel(String channelId, List<MsgDetail> msgDetailList, List<String> midList, String apiType, Long updateMsgId) {
        this.channelId = channelId;
        this.msgDetailList = msgDetailList;
        this.midList = midList;
        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
    }

    public AsyncSendingClusterModel(String channelId, List<MsgDetail> msgDetailList, String mid, String apiType) {
        this.channelId = channelId;
        this.msgDetailList = msgDetailList;
        List<String> list = new ArrayList<>();
        list.add(mid);
        this.midList = list;
        this.apiType = apiType;
    }

    public AsyncSendingClusterModel(String channelId, List<MsgDetail> msgDetailList, String mid, String apiType, Long updateMsgId) {
        this.channelId = channelId;
        this.msgDetailList = msgDetailList;
        List<String> list = new ArrayList<>();
        list.add(mid);
        this.midList = list;
        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
    }
}
