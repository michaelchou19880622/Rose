package com.bcs.core.invoice.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Campaign;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.Invoice;
import com.bcs.core.db.entity.InvoiceDetail;
import com.bcs.core.db.entity.Product;
import com.bcs.core.db.service.CampaignService;
import com.bcs.core.db.service.InvoiceDetailService;
import com.bcs.core.db.service.InvoiceService;
import com.bcs.core.db.service.ProductService;
import com.bcs.core.utils.FileUtil;

@Service
public class InvoiceEventService {
    
    @Autowired
    private InvoiceDecodeService invoiceDecodeService;
    @Autowired
    private InvoiceService invoiceService;
	@Autowired
	private InvoiceDetailService invoiceDetailService;
    @Autowired
    private CampaignService campaignService;
    @Autowired
    private ProductService productService;

    /** Logger */
    private static Logger logger = Logger.getLogger(InvoiceEventService.class);

    @Transactional(rollbackFor=Exception.class, timeout = 300)
	public Map<String, Object> validateInvoice(String mid, ContentResource resource, String campaignId) throws Exception{

        Campaign campaign = getCampaignIfValid(campaignId);
        
        ByteArrayInputStream sourceFile = FileUtil.getFile(resource);
		BufferedImage sourceImage = ImageIO.read(sourceFile);
		
        Invoice invoice = invoiceDecodeService.decode(sourceImage);
        if (invoice != null) {
            invoice.setPicPath(resource.getResourceId());
        }

        Map<String, Object> result = validateInvoice(mid, invoice, campaign);
        
        return result;
	}

    @Transactional(rollbackFor=Exception.class, timeout = 300)
    public Map<String, Object> validateInvoice(String mid, String invNum,String invTerm, String randomNumber, String campaignId) throws Exception{
        Campaign campaign = getCampaignIfValid(campaignId);
        
        Invoice invoice = invoiceDecodeService.getInvoice(invNum, invTerm, randomNumber);

        Map<String, Object> result = validateInvoice(mid, invoice, campaign);
        
        return result;
    }
    
    private Campaign getCampaignIfValid(String campaignId) throws Exception {
        Campaign campaign = campaignService.findOne(campaignId);
        if (campaign == null) {
            throw new Exception("Campaign is Null");
        } else if (campaign.getGroupId() == null) {
            throw new Exception("Group Id is Null");
        }
        return campaign;
    }
    
    private Map<String, Object> validateInvoice(String mid, Invoice invoice, Campaign campaign) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("invoice", invoice);
        
        if (invoice != null && campaign != null && StringUtils.isNotBlank(invoice.getInvoiceId())) {
            String groupId = campaign.getGroupId();
            String campaignId = campaign.getCampaignId();
            
            invoice.setCampaignId(campaignId);
            invoice.setMid(mid);
            
            
            if (invoice.getInvNum() != null) {
                boolean isExisted = invoiceService.checkIsExisted(invoice.getInvNum(), campaignId);
                if (isExisted) {
                    invoice.setStatus(InvoiceStatus.EXISTED);
                }
            }
            

            if (invoice.getStatus() == null && invoice.getInvoiceDetails() != null) {
                
                Double totalPrice = 0D;
                List<String> productNames = new ArrayList<String>();
                
                List<Product> productNamesInGroup = productService.findByGroupId(groupId);
                
                for (InvoiceDetail invDetail : invoice.getInvoiceDetails()) {
                    String productName = invDetail.getDescription();
                    
                    for (Product p : productNamesInGroup) {
                        String productNameInGroup = p.getProductName();
                        /* 模糊比對發票品項名稱與使用者登入的品項 */
                        if (productName.contains(productNameInGroup)) {
                            totalPrice += invDetail.getAmount();
                            productNames.add(productName);
                            
                            break;
                        }
                    }
                }
                
                
                boolean isTimeInInternal = (invoice.getInvDate()) != null && (invoice.getInvDate().getTime() >= campaign.getStartTime().getTime()) &&
                        (invoice.getInvDate().getTime() <= campaign.getEndTime().getTime());
                
                boolean isValid = totalPrice >= campaign.getPrice() && isTimeInInternal;
                if (isValid) {
                    invoice.setStatus(InvoiceStatus.VALID);
                } else {
                    if (isTimeInInternal) {
                        invoice.setStatus(InvoiceStatus.LESS_PAYMENT);
                    } else {
                        invoice.setStatus(InvoiceStatus.NOT_IN_INTERNAL);
                    }
                }
                
                result.put("productNames", productNames);
                result.put("totalPrice", totalPrice);
                
                
                for (InvoiceDetail invDetail : invoice.getInvoiceDetails()) {
                    invoiceDetailService.save(invDetail);
                }
            }
            
            //使用者有上傳發票照片一律儲存，手動輸入只存有正常取到發票的
            logger.info("invoice=" + invoice);
            if (invoice.getPicPath() != null ||
                    !InvoiceStatus.FAKE.equals(invoice.getStatus())) {
                invoiceService.save(invoice);
            }
        }
        
        return result;
    }
    
    public void refreshAllNotFoundInvoice() {
        List<Invoice> invoiceList = invoiceService.findByStatus(InvoiceStatus.NOT_FOUND);
        if (CollectionUtils.isNotEmpty(invoiceList)) {
            List<Invoice> toUpdate = new ArrayList<Invoice>();
            int validCount = 0;
            int invalidCount = 0;
            int errorCount = 0;

            Date now = new Date();
            
            for (Invoice invoice : invoiceList) {
                try {
                    Invoice invoiceNew = invoiceDecodeService.getInvoice(invoice.getInvNum(), invoice.getInvTerm(), invoice.getRandomNumber());
                    invoice = parse(invoice, invoiceNew);
                    
                    Campaign campaign = getCampaignIfValid(invoice.getCampaignId());
                    
                    validateInvoice(invoice.getMid(), invoice, campaign);
                    
                    if (InvoiceStatus.NOT_FOUND.equals(invoice.getStatus())) {
                        if (now.getTime() - invoice.getUploadTime().getTime() > 48 * 60 * 60 * 1000) {
                            invoice.setStatus(InvoiceStatus.FAKE);
                        }
                    }

                    if (!InvoiceStatus.NOT_FOUND.equals(invoice.getStatus())) {
                        if (InvoiceStatus.VALID.equals(invoice.getStatus())) {
                            validCount++;
                        } else {
                            invalidCount++;
                        }
                        
                        toUpdate.add(invoice);
                        invoiceService.save(invoice);
                    }
                    
                } catch (Exception e) {
                    logger.error("refreshAllNotFoundInvoice error! invNum=" + invoice.getInvNum(), e);
                    errorCount++;
                }
            }
            
//            if (CollectionUtils.isNotEmpty(toUpdate)) {
//                invoiceService.bulkPersist(toUpdate);
//            }

            logger.info("toUpdate size=" + (toUpdate != null ? toUpdate.size() : 0));
            logger.info("validCount=" + validCount);
            logger.info("invalidCount=" + invalidCount);
            logger.info("errorCount=" + errorCount);
        }
    }
    
    private Invoice parse(Invoice oldInv, Invoice newInv) {
        oldInv.setInvNum(newInv.getInvNum());
        
        oldInv.setInvDate(newInv.getInvDate());

        oldInv.setSellerName(newInv.getSellerName());
        oldInv.setInvStatus(newInv.getInvStatus());
        oldInv.setInvPeriod(newInv.getInvPeriod());
        oldInv.setSellerBan(newInv.getSellerBan());
        oldInv.setSellerAddress(newInv.getSellerAddress());
        oldInv.setV(newInv.getV());
        oldInv.setCode(newInv.getCode());
        oldInv.setMsg(newInv.getMsg());
        
        oldInv.setStatus(newInv.getStatus());
        
        oldInv.setInvoiceDetails(newInv.getInvoiceDetails());
        
        return oldInv;
    }
}
