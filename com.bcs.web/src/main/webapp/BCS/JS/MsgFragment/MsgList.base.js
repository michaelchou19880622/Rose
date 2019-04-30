/**
 * 
 */
$(function(){

	// 設定 傳送類型 中文
	$.BCS.parseSendType = function(sendType){
		var str = '發送時間:';
		if("IMMEDIATE" ==sendType ){
			return str + "立即傳送";
		}
		else if("DELAY" ==sendType ){
			return str + "預約發送";
		}
		else if("SCHEDULE" ==sendType ){
			return str + "排程發送";
		}
		else{
			return str + "未設定";
		}
	}
	
	// 設定 訊息類型 中文
	$.BCS.parseMsgType = function(msgType){
		if("TEXT" ==msgType ){
			return "文字";
		}
		else if("IMAGE" ==msgType ){
			return "圖片";
		}
		else if("AUDIO" ==msgType ){
			return "聲音";
		}
		else if("VIDEO" ==msgType ){
			return "影片";
		}
		else if("LOCATION" ==msgType ){
			return "位置";
		}
		else if("STICKER" ==msgType ){
			return "貼圖";
		}
		else if("RICH_MSG" ==msgType ){
			return "圖文訊息";
		}
		else if("BCS_PAGE" ==msgType ){
			return "卡友頁面";
		}
		else if("LINK" ==msgType ){
			return "連結";
		}
		else if("COUPON" ==msgType ){
			return "優惠券";
		}
		else if("TEMPLATE" ==msgType ){
			return "樣板訊息";
		}
		else{
			return "其他";
		}
	}
	
	// 設定 使用者狀態 中文
	$.BCS.parseUserStatus = function(userStatus){
		if("UNBIND" ==userStatus ){
			return "一般";
		}
		else if("BINDED" ==userStatus ){
			return "已串聯";
		}
		else if("ALL" ==userStatus ){
			return "全部";
		}
	}
	
	// 設定 生效狀態 中文
	$.BCS.parseInteractiveStatus = function(interactiveStatus){
		if("ACTIVE" ==interactiveStatus ){
			return "生效";
		}
		else if("DISABLE" ==interactiveStatus ){
			return "取消";
		}
	}
	
	// 設定 順位 中文
	$.BCS.parseInteractiveIndex = function(index){
		if(1 ==index ){
			return "第一順位";
		}
		else if(2 ==index ){
			return "第二順位";
		}
		else if(3 ==index ){
			return "第三順位";
		}
		
		return null;
	}
	
	// 設定 其他條件 中文
	$.BCS.parseInteractiveOtherRole = function(OtherRole){
		if("ThisMonthBirthday" ==OtherRole ){
			return "本月生日";
		}
		
		return null;
	}
});