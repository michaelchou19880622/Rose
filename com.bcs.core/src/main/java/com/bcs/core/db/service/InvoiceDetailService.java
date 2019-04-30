package com.bcs.core.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.InvoiceDetail;
import com.bcs.core.db.repository.InvoiceDetailRepository;

@Service
public class InvoiceDetailService {
	
	@Autowired
	private InvoiceDetailRepository invoiceDetailRepository;

    public void save(InvoiceDetail invoiceDetail){
        invoiceDetailRepository.save(invoiceDetail);
    }
}
