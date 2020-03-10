package com.bcs.core.taishin.service;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.UserFieldSet;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.UserFieldSetService;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.UserTraceLogUtil;
import com.bcs.core.record.service.CatchRecordBinded;
import com.bcs.core.taishin.api.model.UpdateStatusFieldModel;
import com.bcs.core.taishin.api.model.UpdateStatusModel;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.LineIdUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class TaishinValidateService {
    @Autowired
    private LineUserService lineUserService;
    @Autowired
    private CatchRecordBinded catchRecordBinded;
    @Autowired
    private UserFieldSetService userFieldSetService;

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(TaishinValidateService.class);

    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void bindedLineUser(UpdateStatusModel model) throws Exception {
        logger.debug("bindedLineUser");

        if (LineIdUtil.isLineUID(model.getUid())) {
            // Validate
        } else {
            throw new Exception("UidError:" + model.getUid());
        }

        String uid = model.getUid();

        if (LineUser.STATUS_BINDED.equals(model.getStatus()) || LineUser.STATUS_UNBIND.equals(model.getStatus())) {
            // Validate
        } else {
            throw new Exception("StatusError:" + model.getStatus());
        }

        //TODO 目前使用Taishin MLBC的時間，未來確認是否有相關用途，如無可更改為現在時間
        Date date = new Date(model.getTime());

        LineUser lineUser = lineUserService.findByMid(uid);
        if (lineUser == null) {
            lineUser = new LineUser();
        }
        lineUser.setMid(uid);
        lineUser.setStatus(lineUser.getStatus().equals(LineUser.STATUS_BLOCK) ? lineUser.getStatus() : model.getStatus());
        lineUser.setIsBinded(model.getStatus());
        lineUser.setSoureType(MsgBotReceive.SOURCE_TYPE_USER);
        lineUser.setModifyTime(date);

        if (lineUser.getCreateTime() == null) {
            lineUser.setCreateTime(date);
        }
        lineUserService.save(lineUser);
        logger.info("After save user: " + DataUtils.toPrettyJsonUseJackson(lineUser));

        List<UserFieldSet> exsitedUserFieldSets = userFieldSetService.findByMid(uid);

        // Save UpdateStatusFieldModel
        List<UpdateStatusFieldModel> fields = model.getField();
        if (fields != null && fields.size() > 0) {
            for (int i = 0; i < fields.size(); i++) {
                boolean isExsited = false;
                UpdateStatusFieldModel field = fields.get(i);
                if (StringUtils.isBlank(field.getKey())) {
                    throw new Exception("FieldError:[" + i + "]:KeyNull");
                }
                if (StringUtils.isBlank(field.getName())) {
                    throw new Exception("FieldError:[" + i + "]:NameNull");
                }
                if (StringUtils.isBlank(field.getType())) {
                    throw new Exception("FieldError:[" + i + "]:TypeNull");
                }
                if (StringUtils.isBlank(field.getValue())) {
                    throw new Exception("FieldError:[" + i + "]:ValueNull");
                }

                //更新資料
                //更新已存在相同KEY的資料
                for (UserFieldSet exsitedUserFieldSet : exsitedUserFieldSets) {
                    if (exsitedUserFieldSet.getKeyData().equals(field.getKey().toUpperCase())) {
                        exsitedUserFieldSet.setValue(field.getValue());
                        exsitedUserFieldSet.setSetTime(date);
                        exsitedUserFieldSet.setFormat("API");
                        userFieldSetService.save(exsitedUserFieldSet);
                        isExsited = true;
                        break;
                    }
                }
                //新增資料
                if (!isExsited) {
                    UserFieldSet set = new UserFieldSet();
                    set.setKeyData(field.getKey().toUpperCase());
                    set.setName(field.getName());
                    set.setSetTime(date);
                    set.setType(field.getType());
                    set.setMid(uid);
                    set.setValue(field.getValue());
                    set.setFormat("API");
                    userFieldSetService.save(set);
                }
            }
        }
        UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_LineUser, LOG_TARGET_ACTION_TYPE.ACTION_Binded, uid, lineUser, uid);
        // Catch Record Binded
        catchRecordBinded.incrementCount();
    }

    public boolean isActive(String Uid) {

        try {
            if (StringUtils.isNotBlank(Uid)) {
                LineUser lineUser = lineUserService.findByMid(Uid);
                // Validate UID is Active
                if (lineUser != null && (LineUser.STATUS_BINDED.equals(lineUser.getStatus()) || LineUser.STATUS_UNBIND.equals(lineUser.getStatus()))) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        return false;
    }
}
