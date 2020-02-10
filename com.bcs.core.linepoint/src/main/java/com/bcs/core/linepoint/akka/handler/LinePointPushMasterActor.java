package com.bcs.core.linepoint.akka.handler;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.resource.CoreConfigReader;
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
        pushMessageRouterActor = new AkkaRouterFactory<>(getContext(), LinePointPushMessageActor.class, true).routerActor;
//        pushApiRouterActor = new AkkaRouterFactory<>(getContext(), LinePointPushApiActor.class, true).routerActor;
//        pushMessageRecordRouterActor = new AkkaRouterFactory<>(getContext(), LinePointPushMessageRecordActor.class, true).routerActor;
//        ftpTaskRouterActor = new AkkaRouterFactory<>(getContext(), LinePointFtpTaskActor.class, true).routerActor;
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
        JSONArray detailIds = object.getDetailIds();
        log.info("Total Detail Size: {}", detailIds.toList().size());
        final int maxAkkaCount = getMaxActorCount();
        final int buffer = getBuffer(detailIds.toList().size(), maxAkkaCount);
        final int arrayLength = detailIds.length();
        int pointer = 0;

        while (pointer < arrayLength) {
            JSONArray partitionDetailIds = new JSONArray();

            for (int counter = 0; (counter < buffer) && (pointer < arrayLength); counter++, pointer++) {
                partitionDetailIds.put(detailIds.get(pointer));
            }

            LinePointPushModel pushApiModelClone = (LinePointPushModel) object.clone();
            pushApiModelClone.setDetailIds(partitionDetailIds);
            log.info("To Akka Detail {} Size: {}", pointer, pushApiModelClone.getDetailIds().toList().size());
            pushMessageRouterActor.tell(pushApiModelClone, this.getSelf());
        }
    }

    private int getBuffer(final int detailSize, final int maxActorCount) {
        if (detailSize <= maxActorCount) {
            return 1;
        }
        if (detailSize % maxActorCount == 0) {
            return detailSize / maxActorCount;
        }
        return detailSize / maxActorCount + 1;
    }

    private int getMaxActorCount() {
        int count = CoreConfigReader.getInteger("bn.push.detail.max.actor.count");
        if (count <= 0) {
            count = 100;
        }
        return count;
    }

    }