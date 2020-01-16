package com.bcs.core.taishin.circle.PNP.akka.model;

import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.taishin.circle.PNP.plugin.MsgGenerator;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AsyncSendingModel extends AbstractBcsEntity {

    private static final long serialVersionUID = 1L;

    private String channelId;
    private List<MsgGenerator> msgGeneratorList;
    private List<String> midList;
    private API_TYPE apiType;
    private Long updateMsgId;

    public AsyncSendingModel(String channelId, List<MsgGenerator> msgGeneratorList, List<String> midList, API_TYPE apiType) {
        this.channelId = channelId;
        this.msgGeneratorList = msgGeneratorList;
        this.midList = midList;
        this.apiType = apiType;
        this.updateMsgId = null;
    }

    public AsyncSendingModel(String channelId, List<MsgGenerator> msgGeneratorList, List<String> midList, API_TYPE apiType, Long updateMsgId) {
        this.channelId = channelId;
        this.msgGeneratorList = msgGeneratorList;
        this.midList = midList;
        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
    }

    public AsyncSendingModel(String channelId, List<MsgGenerator> msgGeneratorList, String mid, API_TYPE apiType) {
        this.channelId = channelId;
        this.msgGeneratorList = msgGeneratorList;

        List<String> list = new ArrayList<>();
        list.add(mid);
        this.midList = list;

        this.apiType = apiType;
    }

    public AsyncSendingModel(String channelId, List<MsgGenerator> msgGeneratorList, String mid, API_TYPE apiType, Long updateMsgId) {
        this.channelId = channelId;
        this.msgGeneratorList = msgGeneratorList;

        List<String> list = new ArrayList<>();
        list.add(mid);
        this.midList = list;

        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
    }

    public AsyncSendingModel(String mid, String channelId, List<MsgGenerator> msgGeneratorList, API_TYPE apiType, Long updateMsgId) {
        this.channelId = channelId;
        this.msgGeneratorList = msgGeneratorList;

        List<String> list = new ArrayList<>();
        list.add(mid);
        this.midList = list;

        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
    }

    public AsyncSendingModel(List<String> midList, String channelId, List<MsgGenerator> msgGeneratorList, API_TYPE apiType, Long updateMsgId) {
        this.channelId = channelId;
        this.msgGeneratorList = msgGeneratorList;

        this.midList = midList;

        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
    }
}
