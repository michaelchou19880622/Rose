package com.bcs.core.api.msg;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bcs.core.api.msg.plugins.MsgGeneratorBcsCoupon;
import com.bcs.core.api.msg.plugins.MsgGeneratorBcsInteractiveLink;
import com.bcs.core.api.msg.plugins.MsgGeneratorBcsLink;
import com.bcs.core.api.msg.plugins.MsgGeneratorBcsPage;
import com.bcs.core.api.msg.plugins.MsgGeneratorBcsRewardCard;
import com.bcs.core.api.msg.plugins.MsgGeneratorBcsRichMsg;
import com.bcs.core.api.msg.plugins.MsgGeneratorBcsTemplateMsg;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.exception.BcsNoticeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linecorp.bot.model.message.Message;


public class MsgGeneratorFactory {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(MsgGeneratorFactory.class);
	
	public static MsgGenerator createFromString(String type, String msgStr) throws Exception{
		
		return createFromNode(type, (ObjectNode)(new ObjectMapper()).readTree(msgStr));
	}
	
	/**
	 * Replace Message Body Mid
	 * 
	 * @param msgBody
	 * @param Mid
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String replcaeMsgMid(String msgBody, String Mid) throws UnsupportedEncodingException{

		return msgBody.replaceAll("\\{" + MsgGeneratorAbstract.REPLACE_MSG_MID + "\\}", URLEncoder.encode(Mid, "UTF-8"));
	}
	
	public static MsgGenerator createFromNode(String type, ObjectNode node) throws Exception{

		if(MsgGenerator.MSG_TYPE_TEXT.equals(type)){
			MsgGeneratorText result = new MsgGeneratorText(node);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_IMAGE.equals(type)){
			MsgGeneratorImage result = new MsgGeneratorImage(node);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_AUDIO.equals(type)){
			MsgGeneratorAudio result = new MsgGeneratorAudio(node);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_VIDEO.equals(type)){
			MsgGeneratorVideo result = new MsgGeneratorVideo(node);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_LOCATION.equals(type)){
			MsgGeneratorLocation result = new MsgGeneratorLocation(node);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_STICKER.equals(type)){
			MsgGeneratorSticker result = new MsgGeneratorSticker(node);
			return result;
		}
		else if(MsgGeneratorExtend.MSG_TYPE_BCS_PAGE.equals(type)){
			MsgGeneratorBcsPage result = new MsgGeneratorBcsPage(node);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_LINK.equals(type)){
			MsgGeneratorBcsLink result = new MsgGeneratorBcsLink(node);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_RICH_MSG.equals(type)){
			MsgGeneratorBcsRichMsg result = new MsgGeneratorBcsRichMsg(node);
			return result;
		}
		else if(MsgGeneratorExtend.MSG_TYPE_COUPON.equals(type)){
			MsgGeneratorBcsCoupon result = new MsgGeneratorBcsCoupon(node);
			return result;
		}
		else if (MsgGeneratorExtend.MSG_TYPE_REWARDCARD.equals(type)){
			MsgGeneratorBcsRewardCard result = new MsgGeneratorBcsRewardCard(node);
			return result;
			
		}else if(MsgGeneratorExtend.MSG_TYPE_TEMPLATE.equals(type)){
			MsgGeneratorBcsTemplateMsg result = new MsgGeneratorBcsTemplateMsg(node);
			return result;
		}
		
		return null;
	}
	
	public static MsgGenerator createFromDetail(String type, MsgDetail detail) throws Exception{

		if(MsgGenerator.MSG_TYPE_TEXT.equals(type)){
			MsgGeneratorText result = new MsgGeneratorText(detail);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_IMAGE.equals(type)){
			MsgGeneratorImage result = new MsgGeneratorImage(detail);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_AUDIO.equals(type)){
			MsgGeneratorAudio result = new MsgGeneratorAudio(detail);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_VIDEO.equals(type)){
			MsgGeneratorVideo result = new MsgGeneratorVideo(detail);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_LOCATION.equals(type)){
			MsgGeneratorLocation result = new MsgGeneratorLocation(detail);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_STICKER.equals(type)){
			MsgGeneratorSticker result = new MsgGeneratorSticker(detail);
			return result;
		}
		else if(MsgGeneratorExtend.MSG_TYPE_BCS_PAGE.equals(type)){
			MsgGeneratorBcsPage result = new MsgGeneratorBcsPage(detail);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_LINK.equals(type)){
			MsgGeneratorBcsLink result = new MsgGeneratorBcsLink(detail);
			return result;
		}
		else if(MsgGenerator.MSG_TYPE_RICH_MSG.equals(type)){
			MsgGeneratorBcsRichMsg result = new MsgGeneratorBcsRichMsg(detail);
			return result;
		}
		else if(MsgGeneratorExtend.MSG_TYPE_INTERACTIVE_LINK.equals(type)){
			MsgGeneratorBcsInteractiveLink result = new MsgGeneratorBcsInteractiveLink(detail);
			return result;
		}
		else if(MsgGeneratorExtend.MSG_TYPE_COUPON.equals(type)){
			MsgGeneratorBcsCoupon result = new MsgGeneratorBcsCoupon(detail);
			return result;
		}else if(MsgGeneratorExtend.MSG_TYPE_REWARDCARD.equals(type)){
			MsgGeneratorBcsRewardCard result = new MsgGeneratorBcsRewardCard(detail);
			return result;
		}else if(MsgGeneratorExtend.MSG_TYPE_TEMPLATE.equals(type)){
			MsgGeneratorBcsTemplateMsg result = new MsgGeneratorBcsTemplateMsg(detail);
			return result;
		}
		
		return null;
	}
	
	public static List<Message> validateMessagesWichMessage(List<MsgDetail> details, String toMid) throws Exception{
		return validateMessagesWichMessage(details, toMid, null);
	}
	
	public static List<Message> validateMessagesWichMessage(List<MsgDetail> details, String toMid, Map<String, String> replaceParam) throws Exception{

		List<Message> result = new ArrayList<Message>();
		
		for(MsgDetail detail : details){

			MsgGenerator msg = createFromDetail(detail.getMsgType(), detail);
			if(msg != null){
				Message message = msg.getMessageBot(toMid, replaceParam); 
				if(message != null){
					result.add(message);
				}
				else{
					logger.error("Message Generator Error");
					throw new BcsNoticeException("訊息產生錯誤");
				}
			}
			else{
				logger.error("Message Generator Error");
				throw new BcsNoticeException("訊息產生錯誤");
			}
		}
		
		return result;
	}

	public static List<MsgGenerator> validateMessages(List<MsgDetail> details) throws Exception{

		if(details != null && details.size() > 0){
			List<MsgGenerator> result = new ArrayList<MsgGenerator>();
			
			for(MsgDetail detail : details){

				MsgGenerator msg = createFromDetail(detail.getMsgType(), detail);
				if(msg != null){
					result.add(msg);
				}
				else{
					logger.error("Message Generator Error");
					throw new BcsNoticeException("訊息產生錯誤");
				}
			}
			
			return result;
		}

		throw new Exception("MsgDetail Null");
	}
}
