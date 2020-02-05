package com.bcs.core.taishin.circle.pnp.akka.model;

import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetail;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
public class AsyncPnpSendModelError extends AbstractBcsEntity {

    private static final long serialVersionUID = 1L;

    private String channelId;
    private API_TYPE apiType;
    private String errorMsg;
    private List<PnpDetail> pnpDetails;
    private int retryTime = 0;
    private Date date;

    public AsyncPnpSendModelError(String channelId, API_TYPE apiType, String errorMsg, List<PnpDetail> pnpDetails, Date date) {
        this.channelId = channelId;
        this.pnpDetails = pnpDetails;
        this.apiType = apiType;
        this.errorMsg = errorMsg;
        this.date = date;
    }

    public int retryTimeAdd() {
        retryTime++;
        return retryTime;
    }
}
