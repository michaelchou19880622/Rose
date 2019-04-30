package com.bcs.core.interactive.model;

import java.util.Date;
import java.util.List;

import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgInteractiveMain;

public class CampaignFlowData {
    
    private Integer step;
    
    private MsgInteractiveMain msgInteractiveMain;

    private List<MsgDetail> currentResponse;
    
    private Date lastModifiedTime;
    
    private Integer errorCount;
    
    private Integer errorLimit;
    
    private String invNum;
    
    private String invTerm;
    
    private String randomNumber;
    
    private Integer timeout;
    

    public CampaignFlowData() {
        lastModifiedTime = new Date();
        errorCount = new Integer(0);
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public MsgInteractiveMain getMsgInteractiveMain() {
        return msgInteractiveMain;
    }

    public void setMsgInteractiveMain(MsgInteractiveMain msgInteractiveMain) {
        this.msgInteractiveMain = msgInteractiveMain;
    }

    public List<MsgDetail> getCurrentResponse() {
        return currentResponse;
    }

    public void setCurrentResponse(List<MsgDetail> currentResponse) {
        this.currentResponse = currentResponse;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }
    
    public Integer addErrorCount() {
        if (this.errorCount == null) {
            this.errorCount = 0;
        }
        return ++this.errorCount;
    }

    public Integer getErrorLimit() {
        return errorLimit;
    }

    public void setErrorLimit(Integer errorLimit) {
        this.errorLimit = errorLimit;
    }

    public String getInvNum() {
        return invNum;
    }

    public void setInvNum(String invNum) {
        this.invNum = invNum;
    }

    public String getInvTerm() {
        return invTerm;
    }

    public void setInvTerm(String invTerm) {
        this.invTerm = invTerm;
    }

    public String getRandomNumber() {
        return randomNumber;
    }

    public void setRandomNumber(String randomNumber) {
        this.randomNumber = randomNumber;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
    
}
