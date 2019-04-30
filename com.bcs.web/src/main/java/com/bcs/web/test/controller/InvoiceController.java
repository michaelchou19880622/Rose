package com.bcs.web.test.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.service.ContentResourceService;
import com.bcs.core.db.service.InvoiceDetailService;
import com.bcs.core.db.service.ProductService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.invoice.service.InvoiceDecodeService;
import com.bcs.core.invoice.service.InvoiceEventService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;

@Controller
@RequestMapping("/bcs")
public class InvoiceController extends BCSBaseController {
    
    @Autowired
    private InvoiceEventService invoiceService;
    @Autowired
    private InvoiceDetailService invoiceDetailService;
    @Autowired
    private InvoiceEventService invoiceEventService;
    @Autowired
    private ProductService productService;
    @Autowired
    private InvoiceDecodeService invoiceUIService;
    @Autowired
    private ContentResourceService contentResourceService;
    
	/** Logger */
	private static Logger logger = Logger.getLogger(InvoiceController.class);
    
    @RequestMapping(method = RequestMethod.GET, value = "/admin/uploadPicturePage")
    public String uploadPicturePage(HttpServletRequest request, HttpServletResponse response) {
        String page = BcsPageEnum.UploadPicturePage.toString();
        return page;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/refreshInvoicePage")
    public String refreshInvoicePage(HttpServletRequest request, HttpServletResponse response) {
        String page = BcsPageEnum.RefreshInvoicePage.toString();
        return page;
    }
	
	@RequestMapping(method = RequestMethod.POST, value = "/admin/uploadPicture")
	@ResponseBody
	public ResponseEntity<?> uploadPicture(@RequestPart MultipartFile filePart,
			@CurrentUser CustomUser customUser,
	        HttpServletRequest request, HttpServletResponse response) {

	    String campaignId = request.getParameter("campaignId");
	    try {
            if (filePart == null) {
                throw new Exception("Upload Picture File is Null");
            } else if (campaignId == null) {
                throw new Exception("Campaign Id is Null");
            } else {
				String modifyUser = customUser.getAccount();
				logger.debug("modifyUser:" + modifyUser);
            	
            	ContentResource resource = contentResourceService.uploadFile(filePart, ContentResource.RESOURCE_TYPE_RECEIVEIMAGE, modifyUser);
            	
            	Map<String, Object> result = invoiceService.validateInvoice(modifyUser, resource, campaignId);
                
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));

            if(e instanceof BcsNoticeException){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            }
            else{
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/admin/refreshInvoice")
    @ResponseBody
    public ResponseEntity<?> refreshInvoice(HttpServletRequest request, HttpServletResponse response) {

        try {
            invoiceEventService.refreshAllNotFoundInvoice();
            
            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));

            if(e instanceof BcsNoticeException){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            }
            else{
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
