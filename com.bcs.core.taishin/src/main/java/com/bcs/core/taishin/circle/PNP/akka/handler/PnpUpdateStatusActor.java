package com.bcs.core.taishin.circle.PNP.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 更新Datail狀態Actor
 *
 * @author jessie
 * @see PnpMainActor
 */
@Slf4j
public class PnpUpdateStatusActor extends UntypedActor {
	private static Logger logger = Logger.getLogger(PnpUpdateStatusActor.class);
    @Override
    public void onReceive(Object object) {

    	logger.info("PnpUpdateStatusActor Receive!!");
        if (object instanceof PnpDetail) {
        	logger.info("Object instanceof PnpDetail!!");
            PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);
            PnpDetail pnpDetail = (PnpDetail) object;
            saveResultAndUpdateSendTime(pnpService, pnpDetail);
            updateStatus(pnpService, pnpDetail);

        try {
            Thread.currentThread().setName("Actor-PNP-Update-" + Thread.currentThread().getId());

            log.info("PnpUpdateStatusActor Receive!!");
            if (object instanceof PnpDetail) {
                log.info("Object instanceof PnpDetail!!");
                PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);
                PnpDetail pnpDetail = (PnpDetail) object;
                saveResultAndUpdateSendTime(pnpService, pnpDetail);
                updateStatus(pnpService, pnpDetail);
            }
            log.info("Update Actor End!!");
        }catch(Exception e){
            log.error("{}",e);

        }
    }

    /**
     * 更新Datail狀態
     * 儲存物件如果狀態為完成更新SendTime
     */
    private void saveResultAndUpdateSendTime(PnpService pnpService, PnpDetail pnpDetail) {
        log.info("saveResultAndUpdateSendTime");
        pnpService.saveBySourceType(pnpDetail);
    }

    /**
     * 更新Main狀態
     *
     * @param pnpService pnpService
     * @param pnpDetail  pnpDetail
     */
    private void updateStatus(PnpService pnpService, PnpDetail pnpDetail) {
        log.info("updateStatus");
        if (checkCanUpdateStatusToComplete(pnpDetail)) {
        	logger.info(String.format("Update Status To Complete!! Main Id: %s, Detail Id: %s", pnpDetail.getPnpMainId(), pnpDetail.getPnpDetailId()));
            pnpService.updatePnpMainStatusComplete(pnpDetail.getPnpMainId(), pnpDetail.getSource(), pnpDetail.getProcStage());
        }
    }

    /**
     * 檢查是否能更新成Complete
     *
     * @param pnpDetail pnpDetail
     * @return 是否可更新
     */
    private boolean checkCanUpdateStatusToComplete(PnpDetail pnpDetail) {
        List<String> status = new ArrayList<>();
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS);
        status.add(AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING);
        status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_DRAFT);
        status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
        status.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_SCHEDULED);
        logger.info(String.format("Check Status: %s, Can Update: %s", pnpDetail.getStatus(), !status.contains(pnpDetail.getStatus())));
        return !status.contains(pnpDetail.getStatus());
    }
}