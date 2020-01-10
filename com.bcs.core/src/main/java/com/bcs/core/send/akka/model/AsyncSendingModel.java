package com.bcs.core.send.akka.model;

import java.util.ArrayList;
import java.util.List;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author ???
 */
@EqualsAndHashCode(callSuper = false)
@Getter
public class AsyncSendingModel extends AbstractBcsEntity {

    private static final long serialVersionUID = 1L;

    private String channelId;
    private List<MsgGenerator> msgGenerators;
    private List<String> midList;
    private API_TYPE apiType;
    private Long updateMsgId;

    public AsyncSendingModel(String channelId, List<MsgGenerator> msgGenerators, List<String> midList, API_TYPE apiType) {
        this.channelId = channelId;
        this.msgGenerators = msgGenerators;
        this.midList = midList;
        this.apiType = apiType;
        this.updateMsgId = null;
    }

    public AsyncSendingModel(String channelId, List<MsgGenerator> msgGenerators, List<String> midList, API_TYPE apiType, Long updateMsgId) {
        this.channelId = channelId;
        this.msgGenerators = msgGenerators;
        this.midList = midList;
        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
    }

    public AsyncSendingModel(String channelId, List<MsgGenerator> msgGenerators, String mid, API_TYPE apiType) {
        this.channelId = channelId;
        this.msgGenerators = msgGenerators;
        List<String> list = new ArrayList<>();
        list.add(mid);
        this.midList = list;
        this.apiType = apiType;
    }

    public AsyncSendingModel(String channelId, List<MsgGenerator> msgGenerators, String mid, API_TYPE apiType, Long updateMsgId) {
        this.channelId = channelId;
        this.msgGenerators = msgGenerators;
        List<String> list = new ArrayList<>();
        list.add(mid);
        this.midList = list;
        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
    }

    public AsyncSendingModel(String mid, String channelId, List<MsgGenerator> msgGenerators, API_TYPE apiType, Long updateMsgId) {
        this.channelId = channelId;
        this.msgGenerators = msgGenerators;
        List<String> list = new ArrayList<>();
        list.add(mid);
        this.midList = list;
        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
    }

    public AsyncSendingModel(List<String> midList, String channelId, List<MsgGenerator> msgGenerators, API_TYPE apiType, Long updateMsgId) {
        this.channelId = channelId;
        this.msgGenerators = msgGenerators;
        this.midList = midList;
        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
    }
}
