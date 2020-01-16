package com.bcs.core.taishin.circle.PNP.akka.model;

import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.taishin.circle.PNP.plugin.MsgGenerator;
import lombok.Getter;

import java.util.Date;
import java.util.List;

/**
 * @author ???
 */
@Getter
public class AsyncSendingModelError extends AbstractBcsEntity {

    private static final long serialVersionUID = 1L;

    private String channelId;
    private List<MsgGenerator> msgGeneratorList;
    private List<String> midList;
    private API_TYPE apiType;
    private String errorMsg;
    private Long updateMsgId;
    private int retryTime = 0;
    private Date date;

    public AsyncSendingModelError(String channelId, List<MsgGenerator> msgGeneratorList, List<String> midList, API_TYPE apiType, String ErrorMsg, Date date) {
        this.channelId = channelId;
        this.msgGeneratorList = msgGeneratorList;
        this.midList = midList;
        this.apiType = apiType;
        this.errorMsg = ErrorMsg;
        this.date = date;
    }

    public AsyncSendingModelError(String channelId, List<MsgGenerator> msgGeneratorList, List<String> midList, API_TYPE apiType, String ErrorMsg, Long updateMsgId, Date date) {
        this.channelId = channelId;
        this.msgGeneratorList = msgGeneratorList;
        this.midList = midList;
        this.apiType = apiType;
        this.errorMsg = ErrorMsg;
        this.updateMsgId = updateMsgId;
        this.date = date;
    }

    public AsyncSendingModelError(List<MsgGenerator> msgGeneratorList, String channelId, List<String> midList, API_TYPE apiType, String ErrorMsg, Long updateMsgId, Date date) {
        this.channelId = channelId;
        this.msgGeneratorList = msgGeneratorList;
        this.midList = midList;
        this.apiType = apiType;
        this.errorMsg = ErrorMsg;
        this.updateMsgId = updateMsgId;
        this.date = date;
    }

    public int retryTimeAdd() {
        retryTime++;
        return retryTime;
    }
}
