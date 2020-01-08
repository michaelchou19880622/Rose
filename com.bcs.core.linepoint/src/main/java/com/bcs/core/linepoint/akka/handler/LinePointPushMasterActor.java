package com.bcs.core.linepoint.akka.handler;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.utils.AkkaRouterFactory;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;

/**
 * @author ???
 * @see com.bcs.core.linepoint.akka.service.LinePointPushAkkaService
 */
@Slf4j
public class LinePointPushMasterActor extends UntypedActor {
    private final ActorRef pushMessageRouterActor;
//    private final ActorRef pushApiRouterActor;
//    private final ActorRef pushMessageRecordRouterActor;
//    private final ActorRef ftpTaskRouterActor;

    public LinePointPushMasterActor() {
        pushMessageRouterActor = new AkkaRouterFactory<LinePointPushMessageActor>(getContext(), LinePointPushMessageActor.class, true).routerActor;
//        pushApiRouterActor = new AkkaRouterFactory<LinePointPushApiActor>(getContext(), LinePointPushApiActor.class, true).routerActor;
//        pushMessageRecordRouterActor = new AkkaRouterFactory<LinePointPushMessageRecordActor>(getContext(), LinePointPushMessageRecordActor.class, true).routerActor;
//        ftpTaskRouterActor = new AkkaRouterFactory<LinePointFtpTaskActor>(getContext(), LinePointFtpTaskActor.class, true).routerActor;
    }

    @Override
    public void onReceive(Object object) throws Exception {
        try {
            if (object instanceof LinePointPushModel) {
                methodA((LinePointPushModel) object);
            }
//        else if (object instanceof LinePointDetail) {
//            LinePointDetail linePointDetail = (LinePointDetail) object;
//            pushApiRouterActor.tell(linePointDetail, this.getSelf());
//        } else if (object instanceof FtpTaskModel) {
//            FtpTaskModel ftpTaskModel = (FtpTaskModel) object;
//
//            if (ftpTaskModel.getFileHead().getMessageSendType().equals(LinePointPushModel.SEND_TYPE_IMMEDIATE)) {    // 立即發送
//                ftpTaskRouterActor.tell(object, this.getSelf());
//            } else if (ftpTaskModel.getFileHead().getMessageSendType().equals(LinePointPushModel.SEND_TYPE_DELAY)) {    // 延遲發送
//                if (ftpTaskModel.getIsScheduled() != null && ftpTaskModel.getIsScheduled())
//                    ftpTaskRouterActor.tell(object, this.getSelf());
//                else {
//                    ApplicationContextProvider.getApplicationContext().getBean(LinePointPushMessageTaskService.class).startTaskFromFtp(ftpTaskModel);
//                }
//            }
//        } else if (object instanceof LinePointPushMessageRecord) {
//            pushMessageRecordRouterActor.tell(object, this.getSelf());
//        }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void methodA(LinePointPushModel object) throws CloneNotSupportedException {
        LinePointPushModel pushApiModel = object;
        /* Partition Size */
        int buffer = 200;
        JSONArray detailIds = pushApiModel.getDetailIds();
        log.info("Total Detail Size: {}", detailIds.toList().size());
        int arrayLength = detailIds.length();
        int pointer = 0;

        while (pointer < arrayLength) {
            JSONArray partitionDetailIds = new JSONArray();

            /* 每一百筆 */
            for (int counter = 0; (counter < buffer) && (pointer < arrayLength); counter++, pointer++) {
                partitionDetailIds.put(detailIds.get(pointer));
            }

            LinePointPushModel pushApiModelClone = (LinePointPushModel) pushApiModel.clone();
            pushApiModelClone.setDetailIds(partitionDetailIds);
            log.info("To Akka Detail {} Size: {}", pointer, pushApiModelClone.getDetailIds().toList().size());
            pushMessageRouterActor.tell(pushApiModelClone, this.getSelf());
        }
    }
}