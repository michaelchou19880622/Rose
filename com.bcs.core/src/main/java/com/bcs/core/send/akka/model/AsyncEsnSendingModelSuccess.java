package com.bcs.core.send.akka.model;

import java.util.Date;
import java.util.List;

import com.bcs.core.json.AbstractBcsEntity;

public class AsyncEsnSendingModelSuccess extends AbstractBcsEntity{
	
	private static final long serialVersionUID = 1L;

	private List<Long> successDetailIds;
	private Date date;
	
	public AsyncEsnSendingModelSuccess(List<Long> successDetailIds, Date date){
	    this.successDetailIds = successDetailIds;
	    this.date = date;
	}

    public List<Long> getSuccessDetailIds() {
        return successDetailIds;
    }

    public Date getDate() {
        return date;
    }
}
