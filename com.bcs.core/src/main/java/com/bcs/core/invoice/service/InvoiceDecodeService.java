package com.bcs.core.invoice.service;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Invoice;
import com.bcs.core.db.entity.InvoiceDetail;
import com.bcs.core.db.service.InvoiceService;
import com.bcs.core.utils.BarcodeDetector;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.HttpClientUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.zxing.Result;

@Service
public class InvoiceDecodeService {
    
    @Autowired
    private InvoiceService invoiceService;
    
    public static final String INV_NUM = "invNum";
    public static final String INV_TERM = "invTerm";
    public static final String RANDOM_NUMBER = "randomNumber";
    
    public static final int QR_CODE = 0;
    public static final int CODE_39 = 1;

    public static final int MSG_INVOICE_SUCCESS = 0;
    public static final int MSG_INVOICE_NOT_FOUND = 1;
    public static final int MSG_INVOICE_PARAMETER_ERROR = 2;
    public static final int MSG_INVOICE_INTERNAL_ERROR = 3;
    
	/** Logger */
	private static Logger logger = Logger.getLogger(InvoiceDecodeService.class);
	
	public Invoice decode(BufferedImage image) {
		try {
			Result[] decodeQRCodeResults = BarcodeDetector.decodeQRCode(image);
        	logger.debug("length:" + decodeQRCodeResults.length);
	        for (Result decodeResult : decodeQRCodeResults) {
	        	logger.debug(decodeResult.getText());
	        	
	            Invoice invoice = getInvoice(decodeResult.getText(), QR_CODE);
	            if (invoice != null && !InvoiceStatus.DECODE_FAIL.equals(invoice.getStatus())) {
	                return invoice;
	            }
	        }
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		try {
	        Result[] decodeCode39Results = BarcodeDetector.decodeCode39(image);
        	logger.debug("length:" + decodeCode39Results.length);
	        for (Result decodeResult : decodeCode39Results) {
	        	logger.debug(decodeResult.getText());

                Invoice invoice = getInvoice(decodeResult.getText(), QR_CODE);
                if (invoice != null && !InvoiceStatus.DECODE_FAIL.equals(invoice.getStatus())) {
                    return invoice;
                }
	        }
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		try {
			Result[] decodeQRCodeResults = BarcodeDetector.decodeQRCode(image, true);
        	logger.debug("length:" + decodeQRCodeResults.length);
	        for (Result decodeResult : decodeQRCodeResults) {
	        	logger.debug(decodeResult.getText());
                
                Invoice invoice = getInvoice(decodeResult.getText(), QR_CODE);
                if (invoice != null && !InvoiceStatus.DECODE_FAIL.equals(invoice.getStatus())) {
                    return invoice;
                }
	        }
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		try {
	        Result[] decodeCode39Results = BarcodeDetector.decodeCode39(image, true);
        	logger.debug("length:" + decodeCode39Results.length);
	        for (Result decodeResult : decodeCode39Results) {
	        	logger.debug(decodeResult.getText());
                
                Invoice invoice = getInvoice(decodeResult.getText(), QR_CODE);
                if (invoice != null && !InvoiceStatus.DECODE_FAIL.equals(invoice.getStatus())) {
                    return invoice;
                }
	        }
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
        
		Invoice invoice = new Invoice();
		invoice.setStatus(InvoiceStatus.DECODE_FAIL);
        return invoice;
	}
	
	private ObjectNode decodeBarcodeText(String text, Integer type) {
	    if (validateDecodeText(text, type)) {
    	    if (type == QR_CODE) {
                ObjectNode node = new ObjectMapper().createObjectNode();
                node.put(INV_NUM, text.substring(0, 10));
                //yyyMMdd
                node.put(INV_TERM, text.substring(10, 17));
                node.put(RANDOM_NUMBER, text.substring(17, 21));
                return node;
                
    	    } else if (type == CODE_39) {
                ObjectNode node = new ObjectMapper().createObjectNode();
                node.put(INV_TERM, text.substring(0, 5));
                //yyyMM
                node.put(INV_NUM, text.substring(5, 15));
                node.put(RANDOM_NUMBER, text.substring(15, 19));
                return node;
                
    	    } else {
    	        // Default type is QR Code
    	        return decodeBarcodeText(text, QR_CODE);
    	    }
	    }
        
        return null;
    }
	
    private static String getURL(String invNum,String invTerm, String randomNumber) {
        String url = "https://einvoice.nat.gov.tw/PB2CAPIVAN/invapp/InvApp?version=0.3&type=Barcode&invNum=%s&action=qryInvDetail&generation=V2&invTerm=%s&invDate=&encrypt=&sellerID=&UUID=a&randomNumber=%s&appID=EINV9201511136249";
        return String.format(url, invNum, invTerm, randomNumber);
    }
    
    private Invoice getInvoice(String qrCodeText, int type) throws Exception {
        String invNum = null;
        String invTerm = null;
        String randomNumber = null;
        
        ObjectNode param = this.decodeBarcodeText(qrCodeText, type);
        if (param != null) {
            invNum = getText(param, INV_NUM);
            invTerm = getText(param, INV_TERM);
            randomNumber = getText(param, RANDOM_NUMBER);
        }
        return this.getInvoice(invNum, invTerm, randomNumber);
    }
    
    /*
     * 將日期轉成yyyMM且月份為雙數月
     */
    private String handleInvTerm(String dateStr) {
        String invTerm = null;
        if (dateStr != null) {
            
            try {
                DateFormat yyyMMDf = new SimpleDateFormat("yyyMM");
                DateFormat yyyMMddDf = new SimpleDateFormat("yyyMMdd");
                Date date = null;
                if (dateStr.length() == 5) {
                    date = yyyMMDf.parse(dateStr);
                    
                } else if (dateStr.length() == 7) {
                    date = yyyMMddDf.parse(dateStr);
                }
                
                if (date != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    int mmInt = cal.get(Calendar.MONTH) + 1;
                    if (mmInt % 2 == 1) {
                        cal.add(Calendar.MONTH, 1);
                    }
                    invTerm = yyyMMDf.format(cal.getTime());
                }
            } catch (ParseException e) {
                logger.error(ErrorRecord.recordError(e));
            }
        }
        return invTerm;
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    private ObjectNode get(String invNum,String invTerm, String randomNumber) throws Exception {
        invNum = (invNum != null ? invNum.toUpperCase() : invNum);
        invTerm = handleInvTerm(invTerm);
        
        int status = 0;
        HttpClient httpClient = HttpClientUtil.generateClient();
        
        String uri = getURL(invNum, invTerm, randomNumber);
        logger.info("uri=" + uri);
        
        HttpGet requestGet = new HttpGet(uri);
        
        // execute Call
        HttpResponse clientResponse = httpClient.execute(requestGet);
        
        status = clientResponse.getStatusLine().getStatusCode();
        logger.info("status=" + status);

        String result = "";
        if(clientResponse != null && clientResponse.getEntity() != null && clientResponse.getEntity().getContent() != null){
            InputStream is = clientResponse.getEntity().getContent();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            result += buffer.lines().collect(Collectors.joining("\n"));
        }
        
        requestGet.releaseConnection();

        return (ObjectNode)(new ObjectMapper()).readTree(result);
        
    }
    
    public Invoice getInvoice(String invNum,String invTerm, String randomNumber) throws Exception {
        if (invNum != null && invTerm != null && randomNumber != null) {
            ObjectNode invoiceJson = get(invNum, invTerm, randomNumber);
            if (invoiceJson != null) {
                Invoice invoice = parseInvoice(invoiceJson);
                invoice.setInvTerm(invTerm);
                invoice.setRandomNumber(randomNumber);
                
                int validateResult = validateInvoiceResp(invoiceJson);
                if (MSG_INVOICE_SUCCESS == validateResult) {
                    
                } else if (MSG_INVOICE_NOT_FOUND == validateResult) {
                    invoice.setStatus(InvoiceStatus.NOT_FOUND);
                } else {
                    invoice.setStatus(InvoiceStatus.FAKE);
                }
                return invoice;
            }
        }

        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatus.DECODE_FAIL);
        return invoice;
    }
    
    private String getText(ObjectNode node, String key){
    	if(node.get(key) != null){
    		return node.get(key).asText();
    	}
    	else{
    		return null;
    	}
    }
    
    private String getText(JsonNode node, String key){
    	if(node.get(key) != null){
    		return node.get(key).asText();
    	}
    	else{
    		return null;
    	}
    }
    
    private Invoice parseInvoice(ObjectNode node) throws Exception {
        Invoice invoice = new Invoice();
        String invoiceId = invoiceService.generateInvoiceId();
        invoice.setInvoiceId(invoiceId);
        
        invoice.setInvNum(getText(node,"invNum"));
        
        if (StringUtils.isNoneBlank(getText(node,"invDate")) &&
                StringUtils.isNoneBlank(getText(node,"invoiceTime"))) {
            
            String invDate = getText(node,"invDate") + " " + getText(node,"invoiceTime");
            DateFormat df = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
            invoice.setInvDate(df.parse(invDate));
        }

        invoice.setSellerName(getText(node,"sellerName"));
        invoice.setInvStatus(getText(node,"invStatus"));
        invoice.setInvPeriod(getText(node,"invPeriod"));
        invoice.setSellerBan(getText(node,"sellerBan"));
        invoice.setSellerAddress(getText(node,"sellerAddress"));
        invoice.setV(getText(node,"v"));
        invoice.setCode(getText(node,"code"));
        invoice.setMsg(getText(node,"msg"));
        
        invoice.setUploadTime(new Date());
        
        List<InvoiceDetail> invDetails = new ArrayList<InvoiceDetail>();
        JsonNode details = node.get("details");
        if (details != null) {
            for (JsonNode detail : details) {
                InvoiceDetail invDetail = new InvoiceDetail();
                
                Double amount = detail.get("amount") != null ? detail.get("amount").asDouble() : null;
                invDetail.setAmount(amount);
                invDetail.setDescription(getText(detail, "description"));
                
                Double unitPrice = detail.get("unitPrice") != null ? detail.get("unitPrice").asDouble() : null;
                invDetail.setUnitPrice(unitPrice);
                
                Integer quantity = detail.get("quantity") != null ? detail.get("quantity").asInt() : null;
                invDetail.setQuantity(quantity);
                invDetail.setInvoiceId(invoiceId);
                
                invDetails.add(invDetail);
            }
        }
        invoice.setInvoiceDetails(invDetails);
        
        return invoice;
    }
    
    public boolean validateDecodeText(String text, Integer type) {
        if (type == QR_CODE) {
            if (text != null && text.length() >= 21) {
                Pattern pattern = Pattern.compile("[A-Z]{2}\\d{8}\\d{7}\\d{4}", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(text.substring(0, 21));
                logger.debug(text.substring(0, 21));
                logger.debug(matcher.matches());
                return matcher.matches();
            }
        } else if (type == CODE_39) {
            if (text != null && text.length() == 19) {
                Pattern pattern = Pattern.compile("\\d{5}[A-Z]{2}\\d{8}\\d{4}", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(text.substring(0, 19));
                logger.debug(text.substring(0, 19));
                logger.debug(matcher.matches());
                return matcher.matches();
            }
        }
        return false;
    }
    
    public int validateInvoiceResp(ObjectNode node) {
        try {
            Integer statusCode = node.get("code").asInt();
            if (200 == statusCode) {
                String invStatus = getText(node,"invStatus");
                if ("已確認".equals(invStatus)) {
                    return MSG_INVOICE_SUCCESS;
                } else {
                    return MSG_INVOICE_NOT_FOUND;
                }
            } else if (903 == statusCode) {
                return MSG_INVOICE_PARAMETER_ERROR;
            }
            
            logger.error("statusCode=" + statusCode);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        } 
        return MSG_INVOICE_INTERNAL_ERROR;
    }
}
