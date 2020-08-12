package com.bcs.web.ui.model;

import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentLinkTracing;
import com.bcs.core.json.AbstractBcsEntity;

public class TracingLinkListModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	private ContentLinkTracing contentLinkTracing;
	private ContentLink contentLinkUnbind;
	private ContentLink contentLinkBind;
	private ContentLink contentLinkUnmobile;
	
	public ContentLinkTracing getContentLinkTracing() {
		return contentLinkTracing;
	}
	public void setContentLinkTracing(ContentLinkTracing contentLinkTracing) {
		this.contentLinkTracing = contentLinkTracing;
	}
	public ContentLink getContentLinkUnbind() {
		return this.contentLinkUnbind;
	}
	public void setContentLinkUnbind(ContentLink contentLinkUnbind) {
		this.contentLinkUnbind = contentLinkUnbind;
	}
	public ContentLink getContentLinkBind() {
		return this.contentLinkBind;
	}
	public void setContentLinkBind(ContentLink contentLinkBind) {
		this.contentLinkBind = contentLinkBind;
	}
	public ContentLink getContentLinkUnmobile() {
		return this.contentLinkUnmobile;
	}
	public void setContentLinkUnmobile(ContentLink contentLinkUnmobile) {
		this.contentLinkUnmobile = contentLinkUnmobile;
	}
}
