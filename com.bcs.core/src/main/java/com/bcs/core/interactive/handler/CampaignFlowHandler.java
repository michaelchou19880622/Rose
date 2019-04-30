package com.bcs.core.interactive.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.api.msg.MsgGeneratorExtend;
import com.bcs.core.db.entity.CampaignFlow;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.Invoice;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgInteractiveCampaign;
import com.bcs.core.db.entity.MsgInteractiveMain;
import com.bcs.core.db.service.CampaignFlowService;
import com.bcs.core.db.service.CampaignService;
import com.bcs.core.db.service.MsgDetailService;
import com.bcs.core.db.service.MsgInteractiveCampaignService;
import com.bcs.core.db.service.MsgInteractiveMainService;
import com.bcs.core.interactive.model.CampaignFlowData;
import com.bcs.core.invoice.service.InvoiceEventService;
import com.bcs.core.invoice.service.InvoiceStatus;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class CampaignFlowHandler {

	/** Logger */
	private static Logger logger = Logger.getLogger(CampaignFlowHandler.class);

    @Autowired
    private InvoiceEventService invoiceEventService;
    @Autowired
    private MsgInteractiveCampaignService msgInteractiveCampaignService;
    @Autowired
    private CampaignService campaignService;
    @Autowired
    private MsgDetailService msgDetailService;
    @Autowired
    private CampaignFlowService campaignFlowService;
    @Autowired
    private MsgInteractiveMainService msgInteractiveMainService;
	
	protected LoadingCache<String, CampaignFlowData> camapaignFlowDataCache;
	
	public CampaignFlowHandler(){

		camapaignFlowDataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, CampaignFlowData>() {
					@Override
					public CampaignFlowData load(String key) throws Exception {
						return new CampaignFlowData();
					}
				});
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] InteractiveHandler cleaning up...");
		try{
			if(camapaignFlowDataCache != null){
				camapaignFlowDataCache.invalidateAll();
				camapaignFlowDataCache = null;
			}
		}
		catch(Throwable e){}
		
		System.gc();
		logger.info("[DESTROY] InteractiveHandler destroyed.");
	}
		
	/**
	 * @param iMsgId
	 * @return
	 */
	public List<MsgDetail> checkIsOnCampaign(String MID, MsgInteractiveMain main, List<MsgDetail> details){
		logger.debug("checkIsInteractive");
		if(MsgInteractiveMain.INTERACTIVE_TYPE_INTERACTIVE.equals(main.getInteractiveType())){
			logger.debug("Interactive Detail Record Step 1");
			List<MsgDetail> list = new ArrayList<MsgDetail>();
			for(MsgDetail detail : details){
				if(MsgGeneratorExtend.MSG_TYPE_INTERACTIVE_LINK.equals(detail.getMsgType())){
					
				}
				else{
					list.add(detail);
				}
			}
			
			return list;
		}
		else{
			return details;
		}
	}
	
	public List<MsgDetail> startFlow(String MID, MsgInteractiveMain main) throws Exception {
	    List<MsgDetail> result = new ArrayList<MsgDetail>();

        MsgInteractiveCampaign iMsgCamapaign = msgInteractiveCampaignService.findByiMsgId(main.getiMsgId());
        
        List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_UPLOAD_INVOICE);
        
        if (CollectionUtils.isNotEmpty(details) && iMsgCamapaign != null) {
            result.addAll(details);

            CampaignFlowData data = new CampaignFlowData();
            data.setMsgInteractiveMain(main);
            data.setStep(1);
            data.setErrorCount(0);
            data.setLastModifiedTime(new Date());
            
            data.setErrorLimit(iMsgCamapaign.getErrorLimit());
            data.setTimeout(iMsgCamapaign.getTimeout());
            
            camapaignFlowDataCache.put(MID, data);
            saveToDB(data, MID);
        }
            
        return result; 
	}
	
	public CampaignFlowData getFromDB(String MID){

	    CampaignFlow campaignFlow = campaignFlowService.findOne(MID);
	    if(campaignFlow == null){
	        return null;
	    }
	    else{
	        MsgInteractiveCampaign iMsgCamapaign = msgInteractiveCampaignService.findByiMsgId(campaignFlow.getiMsgId());
	        
	        CampaignFlowData data = new CampaignFlowData();
	    	MsgInteractiveMain main = msgInteractiveMainService.findOne(campaignFlow.getiMsgId());
            data.setMsgInteractiveMain(main);
            data.setStep(campaignFlow.getStep());
            data.setErrorCount(campaignFlow.getErrorCount());
            data.setLastModifiedTime(campaignFlow.getLastModifiedTime());
            
            data.setErrorLimit(iMsgCamapaign.getErrorLimit());
            data.setTimeout(iMsgCamapaign.getTimeout());
            data.setInvNum(campaignFlow.getInvNum());
            data.setInvTerm(campaignFlow.getInvTerm());
            data.setRandomNumber(campaignFlow.getRandomNumber());
            
            return data;
	    }
	}
	
	public CampaignFlow saveToDB(CampaignFlowData data, String MID){

	    CampaignFlow campaignFlow = campaignFlowService.findOne(MID);
	    if(campaignFlow == null){
	    	campaignFlow = new CampaignFlow();
	    	campaignFlow.setMid(MID);
	    }

    	campaignFlow.setiMsgId(data.getMsgInteractiveMain().getiMsgId());
    	campaignFlow.setStep(data.getStep());
    	campaignFlow.setErrorCount(data.getErrorCount());
    	campaignFlow.setLastModifiedTime(data.getLastModifiedTime());
        
    	campaignFlow.setErrorLimit(data.getErrorLimit());
    	campaignFlow.setTimeout(data.getTimeout());
    	campaignFlow.setInvNum(data.getInvNum());
        campaignFlow.setInvTerm(data.getInvTerm());
        campaignFlow.setRandomNumber(data.getRandomNumber());
        
        return campaignFlowService.save(campaignFlow);
	}
	
	public CampaignFlowData handle(String MID, Object msg) throws Exception {
	    CampaignFlowData data = camapaignFlowDataCache.get(MID);
	    
	    // 分散式處理
	    data = getFromDB(MID);
	    
	    if (data == null || data.getStep() == null) {
	        return null;
	    }
	    
	    
	    if (isTimeout(data)) {
	        cancelFlow(MID);
            
            return null;
	    }
	    
	    MsgInteractiveMain main = data.getMsgInteractiveMain();
	    
	    List<MsgDetail> result = new ArrayList<MsgDetail>();
	    data.setCurrentResponse(result);

        Integer step = data.getStep();
	    if (step == 1) {
            if (msg instanceof String) {
                String text = (String) msg;
                if (text != null) {
                    text = text.toUpperCase();
                }
                /* 發票號碼 */
                Pattern pattern = Pattern.compile("[A-Z]{2}\\d{8}");
                Matcher matcher = pattern.matcher(text);
                if (matcher.matches()) {
                    List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_TYPE_IN_INVTERN);
                    
                    if (CollectionUtils.isNotEmpty(details)) {
                        result.addAll(details);
                        data.setInvNum(text);
                        data.setStep(2);
                    }
                } else {
                    List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_UPLOAD_INVOICE_FAIL);
                    
                    if (CollectionUtils.isNotEmpty(details)) {
                        result.addAll(details);
                        data.setStep(1);   
                    }
                    data.addErrorCount();
                }
                
            } else if (msg instanceof ContentResource) {
                ContentResource resource = (ContentResource) msg;
                MsgInteractiveMain iMsgMain = data.getMsgInteractiveMain();
                MsgInteractiveCampaign iMsgCamapaign = msgInteractiveCampaignService.findByiMsgId(iMsgMain.getiMsgId());
                
                Map<String, Object> validateResult = 
                        invoiceEventService.validateInvoice(MID, resource, iMsgCamapaign.getCampaignId());

                if (validateResult != null) {
                    handleInvoiceResult(validateResult, data, result);
                }
            }
        } else if (step == 2) {
            if (msg instanceof String) {
                String text = (String) msg;
                /* 發票號碼 */
                Pattern pattern = Pattern.compile("\\d{5}");
                Matcher matcher = pattern.matcher(text);
                if (matcher.matches()) {
                    List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_TYPE_IN_RANDON_NUM);
                    
                    if (CollectionUtils.isNotEmpty(details)) {
                        result.addAll(details);
                        data.setInvTerm(text);
                        data.setStep(3);
                    }
                } else {
                    List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_TYPE_IN_INVTERN_FAIL);
                    
                    if (CollectionUtils.isNotEmpty(details)) {
                        result.addAll(details);
                        data.addErrorCount();
                    }
                }
            } else {
            }
        } else if (step == 3) {
            String text = (String) msg;
            /* 發票號碼 */
            Pattern pattern = Pattern.compile("\\d{4}");
            Matcher matcher = pattern.matcher(text);
            if (matcher.matches()) {
                data.setRandomNumber(text);
                
                MsgInteractiveMain iMsgMain = data.getMsgInteractiveMain();
                MsgInteractiveCampaign iMsgCamapaign = msgInteractiveCampaignService.findByiMsgId(iMsgMain.getiMsgId());
                
                Map<String, Object> validateResult = invoiceEventService.validateInvoice(MID, data.getInvNum(), data.getInvTerm(), data.getRandomNumber(), iMsgCamapaign.getCampaignId());
                logger.info(ObjectUtil.objectToJsonStr(validateResult));
                
                handleInvoiceResult(validateResult, data, result);
            } else {
                List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_TYPE_IN_RANDON_NUM_FAIL);
                
                if (CollectionUtils.isNotEmpty(details)) {
                    result.addAll(details);
                    data.addErrorCount();
                }
            }
        }
	    
	    if (data.getErrorLimit() != 0 && data.getErrorCount() == data.getErrorLimit()) {
	        result = new ArrayList<MsgDetail>();
	        data.setCurrentResponse(result);
	        
	        List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_TOO_MUCH_ERROR);
            
            if (CollectionUtils.isNotEmpty(details)) {
                result.addAll(details);
            }
	        cancelFlow(MID);
	        
	        return data;
	    }
	    
	    if (CollectionUtils.isEmpty(data.getCurrentResponse())) {
	        cancelFlow(MID);
	        
	        return null;
	    }
        
	    data.setLastModifiedTime(new Date());
	    
	    // 分散式處理
	    saveToDB(data, MID);
	    return data;
	}
	
	public void cancelFlow(String MID) {
	    camapaignFlowDataCache.invalidate(MID);
	    try {
			campaignFlowService.delete(MID);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
    
    private void handleInvoiceResult(Map<String, Object> validateResult, CampaignFlowData data, List<MsgDetail> result) throws Exception {

        MsgInteractiveMain main = data.getMsgInteractiveMain();
        
        Invoice invoice = (Invoice) validateResult.get("invoice");

        /* 無法辦識或發生例外 */
        Boolean isDecodeFail = (null == invoice || 
                InvoiceStatus.DECODE_FAIL.equals(invoice.getStatus()));
        
        if (isDecodeFail) {
            
            List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_DECODE_FAIL);
            
            if (CollectionUtils.isNotEmpty(details)) {
                result.addAll(details);
            }
            data.addErrorCount();
            data.setStep(1);
            
        } else {
            /* 發票是否尚未同步至發票平台 */
            boolean isInvoiceNotFound = InvoiceStatus.NOT_FOUND.equals(invoice.getStatus());
            /* 發票是否在活動日期區間內 */
            boolean isTimeNotInInternal = InvoiceStatus.NOT_IN_INTERNAL.equals(invoice.getStatus());
            /* 發票金額是否在活動條件 */
            boolean isLessPayment = InvoiceStatus.LESS_PAYMENT.equals(invoice.getStatus());
            /* 是否符合活動資格 */
            boolean isValid = InvoiceStatus.VALID.equals(invoice.getStatus());
            /* 發票是否已參加過活動 */
            boolean isExisted = InvoiceStatus.EXISTED.equals(invoice.getStatus());
            
            
            if (isValid) {
                List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_UPLOAD_INVOICE_SUCCESS);
                
                if (CollectionUtils.isNotEmpty(details)) {
                    result.addAll(details);
                }
                data.setStep(4);
                
            } else if (isInvoiceNotFound) {
                List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_NOT_FOUND);
                
                if (CollectionUtils.isNotEmpty(details)) {
                    result.addAll(details);
                }
                data.setStep(4);
                
            } else if (isTimeNotInInternal) {
                List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_NOT_IN_INTERNAL);
                
                if (CollectionUtils.isNotEmpty(details)) {
                    result.addAll(details);
                }
                data.addErrorCount();
                data.setStep(1);
                
            } else if (isLessPayment) {
                List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_LESS_PAYMENT);
                
                if (CollectionUtils.isNotEmpty(details)) {
                    result.addAll(details);
                }
                data.addErrorCount();
                data.setStep(1);
            } else if (isExisted) {
                List<MsgDetail> details = msgDetailService.findByMsgIdAndEventType(main.getiMsgId(), MsgGeneratorExtend.EVENT_TYPE_INVOICE_IS_USED);
                
                if (CollectionUtils.isNotEmpty(details)) {
                    result.addAll(details);
                }
                data.addErrorCount();
                data.setStep(1);
            }
        }
    }
    
    private boolean isTimeout(CampaignFlowData data) {
        if (data != null && data.getTimeout() != null && data.getLastModifiedTime() != null) {
            Date last = data.getLastModifiedTime();
            Date now = new Date();
            Integer timeoutSec = data.getTimeout();
            
            return ((now.getTime() - last.getTime()) > timeoutSec * 1000 );
        }
        return false;
    }
}
