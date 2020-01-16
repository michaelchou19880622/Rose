package com.bcs.core.taishin.circle.PNP.akka.model;

import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
public class AsyncSendingModelSuccess extends AbstractBcsEntity {
    private static final long serialVersionUID = 1L;

    private String channelId;
    private List<String[]> midList;
    private API_TYPE apiType;
    private Long updateMsgId;
    private Date date;

    public AsyncSendingModelSuccess(String channelId, List<String[]> midList, API_TYPE apiType, Date date) {
        this.channelId = channelId;
        this.midList = midList;
        this.apiType = apiType;
        this.date = date;
    }

    public AsyncSendingModelSuccess(String channelId, List<String[]> midList, API_TYPE apiType, Long updateMsgId, Date date) {
        this.channelId = channelId;
        this.midList = midList;
        this.apiType = apiType;
        this.updateMsgId = updateMsgId;
        this.date = date;
    }
}
