package com.bcs.core.taishin.circle.pnp.akka.handler;

import akka.actor.UntypedActor;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStageEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.pnp.service.PnpService;
import lombok.extern.slf4j.Slf4j;

/**
 * 更新Datail狀態Actor
 *
 * @author jessie
 * @see PnpMainActor
 */
@Slf4j(topic = "PnpRecorder")
public class PnpUpdateStatusActor extends UntypedActor {
//
    @Override
    public void onReceive(Object object) {
//        try {
//            Thread.currentThread().setName("Actor-PNP-Update-" + Thread.currentThread().getId());
//
//            log.info("PnpUpdateStatusActor Receive!!");
//            if (object instanceof PnpDetail) {
//                log.info("Object instanceof PnpDetail!!");
//                PnpService pnpService = ApplicationContextProvider.getApplicationContext().getBean(PnpService.class);
//                PnpDetail pnpDetail = (PnpDetail) object;
//                saveResultAndUpdateSendTime(pnpService, pnpDetail);
////                updateStatus(pnpService, pnpDetail);
//            }
//            log.info("Update Actor End!!");
//        } catch (Exception e) {
//            log.error("Exception", e);
//
//        }
    }
//
//    /**
//     * 更新Datail狀態
//     * 儲存物件如果狀態為完成更新SendTime
//     */
//    private void saveResultAndUpdateSendTime(PnpService pnpService, PnpDetail pnpDetail) {
//        log.info("saveResultAndUpdateSendTime");
//        pnpService.saveDetail(pnpDetail);
//    }
//
//    /**
//     * 更新Main狀態
//     *
//     * @param pnpService pnpService
//     * @param pnpDetail  pnpDetail
//     */
//    private void updateStatus(PnpService pnpService, PnpDetail pnpDetail) {
//        log.info("updateStatus");
//        log.info(String.format("Update Status To Complete!! Main Id: %s, Detail Id: %s", pnpDetail.getPnpMainId(), pnpDetail.getPnpDetailId()));
//        pnpService.updateMainStatus(pnpDetail.getPnpMainId(), PnpFtpSourceEnum.findEnumByCode(pnpDetail.getSource()), PnpStageEnum.findEnumByName(pnpDetail.getProcStage()));
//    }
}