package com.bcs.core.gateway.msg;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;

import org.json.JSONObject;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.ContentResourceFile;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.repository.ContentResourceRepository;
import com.bcs.core.db.service.ContentResourceFileService;
import com.bcs.core.db.service.ContentResourceService;
import com.bcs.core.spring.ApplicationContextProvider;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.FileUtil;

public class ImageMessageAdapter extends MessageAdapter {
	private static final long serialVersionUID = 1L;
	
	private ContentResourceFileService contentResourceFileService = ApplicationContextProvider.getApplicationContext().getBean(ContentResourceFileService.class);
	private ContentResourceRepository contentResourceRepository = ApplicationContextProvider.getApplicationContext().getBean(ContentResourceRepository.class);

	private MsgDetail message = null;
	private String imageResourceId = null;
	
	public ImageMessageAdapter() {}

	public ImageMessageAdapter(JSONObject message) throws Exception {
		String[] imageUrlList = message.getString("showUrl").split(";");
		messageList = new ArrayList<MsgDetail>();

		for (String imageUrl : imageUrlList) {
			this.message = new MsgDetail();
			imageResourceId = getImageResourceId(imageUrl);

			if (imageResourceId != null) {
				this.message.setReferenceId(imageResourceId);
				this.message.setMsgType(MsgGenerator.MSG_TYPE_IMAGE);
				messageList.add(this.message);
			}
		}
	}

	/*
	 * ===== 處理圖片，並回傳圖片的 RESOURCE_ID =====
	 * String imageUrl
	 */
	protected String getImageResourceId(String imageUrl) throws Exception{
		String urlString = imageUrl.split("://")[1];
		MessageDigest md = MessageDigest.getInstance("MD5");
		
		md.update(urlString.getBytes());
		String resourceId = new BigInteger(1, md.digest()).toString(16);
		
		ContentResourceFile contentResourceFile = contentResourceFileService.findOne(resourceId);
		
		if (contentResourceFile != null) {
			return contentResourceFile.getResourceId();
		} else {
			ContentResource uploadedImage = FileUtil.uploadFile(null, imageUrl, ContentResource.RESOURCE_TYPE_IMAGE, "BOT");
			
			contentResourceRepository.save(uploadedImage);
			
			DataSyncUtil.settingReSync(ContentResourceService.RESOURCE_SYNC);
			
			return uploadedImage.getResourceId();
		}
	}
}
