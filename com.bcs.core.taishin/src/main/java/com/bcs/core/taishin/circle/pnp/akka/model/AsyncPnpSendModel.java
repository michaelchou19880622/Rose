package com.bcs.core.taishin.circle.pnp.akka.model;

import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetail;
import lombok.Getter;

import java.util.List;

/**
 * @author ???
 */
@Getter
public class AsyncPnpSendModel extends AbstractBcsEntity {

    private static final long serialVersionUID = 1L;

    private String channelId;
    private API_TYPE apiType;
    private List<PnpDetail> pnpDetails;

    public AsyncPnpSendModel(String channelId, API_TYPE apiType, List<PnpDetail> pnpDetails) {
        this.channelId = channelId;
        this.apiType = apiType;
        this.pnpDetails = pnpDetails;
    }
}
