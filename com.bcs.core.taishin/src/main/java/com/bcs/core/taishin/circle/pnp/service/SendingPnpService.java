package com.bcs.core.taishin.circle.pnp.service;

import com.bcs.core.enums.API_TYPE;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.pnp.akka.CircleAkkaBotService;
import com.bcs.core.taishin.circle.pnp.akka.model.AsyncPnpSendModel;
import com.bcs.core.taishin.circle.pnp.akka.model.AsyncSendingClusterModel;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.pnp.plugin.PostLineResponse;
import com.bcs.core.utils.ErrorRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j(topic = "PnpRecorder")
@Service
public class SendingPnpService {

    @Autowired
    private CircleAkkaBotService akkaBotService;

    public void sendToLineAsync(List<PnpDetail> sendDetails, API_TYPE apiType) throws Exception {
        sendToLineAsync(CONFIG_STR.DEFAULT.toString(), sendDetails, apiType);
    }

    public void sendToLineAsync(String ChannelId, List<PnpDetail> sendDetails, API_TYPE apiType) throws Exception {
        log.debug("sendToLineAsync");
        if (ChannelId == null) {
            ChannelId = CONFIG_STR.DEFAULT.toString();
        }

        if (sendDetails != null && sendDetails.size() > 0) {
            AsyncPnpSendModel msgs = new AsyncPnpSendModel(ChannelId, apiType, sendDetails);
            boolean sendThis = CoreConfigReader.getBoolean(CONFIG_STR.BCS_API_CLUSTER_SEND_THIS.toString());

            String url = CoreConfigReader.getString(CONFIG_STR.BCS_API_CLUSTER_SEND.toString());

            if (sendThis) {
                log.info("sendToLineAsync:This Server:Phones:" + sendDetails.size());
                akkaBotService.sendingPnp(msgs);
            } else {
                if (StringUtils.isNotBlank(url)) {
                    try {
                        log.info("sendToLineAsync:Different Server:Phones:" + sendDetails.size());
                        AsyncSendingClusterModel model = new AsyncSendingClusterModel(sendDetails, ChannelId, apiType.toString());
                        PostLineResponse response = CircleBcsApiClusterService.clusterApiSend(model);
                        if (200 != response.getStatus()) {
                            throw new Exception(response.toString());
                        }
                    } catch (Exception e) {
                        log.error(ErrorRecord.recordError(e));
                        log.info("Cluster send message failed,Use this server sendToLineAsync:This Server:Phones:" + sendDetails.size());
                        akkaBotService.sendingPnp(msgs);
                    }
                } else {
                    log.info("Cluster url is empty,sendToLineAsync:This Server:Phones:" + sendDetails.size());
                    akkaBotService.sendingPnp(msgs);
                }
            }
        }
    }
}
