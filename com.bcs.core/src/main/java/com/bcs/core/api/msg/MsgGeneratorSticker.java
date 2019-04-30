package com.bcs.core.api.msg;



import org.apache.commons.lang3.StringUtils;

import com.bcs.core.db.entity.ContentSticker;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.service.ContentStickerService;
import com.bcs.core.spring.ApplicationContextProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.message.StickerMessage;

public class MsgGeneratorSticker extends MsgGeneratorAbstract {
	private static final long serialVersionUID = 1L;

	public static boolean canSetMulti() throws Exception {
		return true;
	}
	
	private String STKID;
	private String STKPKGID;
	private String STKVER;

	public MsgGeneratorSticker(String STKID, String STKPKGID, String STKVER) throws Exception{
		super();
		generatorContent(STKID, STKPKGID, STKVER, 1);
		
		message = new StickerMessage(STKPKGID, STKID);
	}
	
	public MsgGeneratorSticker(String STKID, String STKPKGID, String STKVER, int toType) throws Exception{
		super();
		generatorContent(STKID, STKPKGID, STKVER, toType);
		
		message = new StickerMessage(STKPKGID, STKID);
	}
	
	public MsgGeneratorSticker(ObjectNode node) throws Exception{
		super();
		if (node.get("toType") == null) {
			generatorContent(node.get("STKID").textValue(),
					node.get("STKPKGID").textValue(), node.get("STKVER")
							.textValue(), 1);
			
			message = new StickerMessage(STKPKGID, STKID);
		} else {
			generatorContent(node.get("STKID").textValue(),
					node.get("STKPKGID").textValue(), node.get("STKVER")
							.textValue(), node.get("toType").intValue());
			
			message = new StickerMessage(STKPKGID, STKID);
		}
	}

	public MsgGeneratorSticker(MsgDetail detail) throws Exception{
		super();
		generatorContent(detail, 1);
		
		message = new StickerMessage(STKPKGID, STKID);
	}
	
	public MsgGeneratorSticker(MsgDetail detail, int toType) throws Exception{
		super();
		generatorContent(detail, toType);
		
		message = new StickerMessage(STKPKGID, STKID);
	}
	
	private void generatorContent(MsgDetail detail, int toType) throws Exception{

		ContentStickerService contentStickerService = ApplicationContextProvider.getApplicationContext().getBean(ContentStickerService.class);
		
		ContentSticker contentSticker = contentStickerService.findOne(detail.getReferenceId());
		
		generatorContent(contentSticker.getStickerStkid(), contentSticker.getStickerStkpkgid(), contentSticker.getStickerStkver(), toType);
	}
	
	private void generatorContent(String STKID, String STKPKGID, String STKVER, int toType) throws Exception{
		
		if(StringUtils.isBlank(STKID)){
			throw new Exception("STKID Can not be Null");
		}

		if(StringUtils.isBlank(STKPKGID)){
			throw new Exception("STKPKGID Can not be Null");
		}
		
		this.STKID = STKID;
		this.STKPKGID = STKPKGID;
		this.STKVER = STKVER;
	}

	public String getSTKID() {
		return STKID;
	}

	public String getSTKPKGID() {
		return STKPKGID;
	}

	public String getSTKVER() {
		return STKVER;
	}
}
