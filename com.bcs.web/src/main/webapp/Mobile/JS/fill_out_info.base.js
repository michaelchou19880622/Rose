$(function(){
	var ALP_STR = "ABCDEFGHJKLMNPQRSTUVXYWZIO";
	var NUM_STR = "0123456789";
	var COUPONID = $.urlParam("referenceId");
	var userForm = null;
	
	if(!COUPONID){
		COUPONID = $('#couponId').val();
	}
	
	pageInit();
	
	$('.check').click(function(){
		window.location.reload();
	});
	
	$('#confirm').click(function(){
		userForm = getFormDetail();
		
		if(formValidator(userForm)){
			$('html, body').animate({
				scrollTop: 0
			}, 600);
			
			$('.popup_wrapper').removeAttr('style');
		}
	});
	
	$('.popup_btn .cancel').click(function(){
		$('.popup_wrapper').attr('style', 'display: none;');
	});
	
	$('.popup_btn .confirm').click(function(){		
		if(formValidator(userForm)){
			$('.popup_wrapper').attr('style', 'display: none;');
			$.LoadingOverlay("show");
			
			$.ajax({
				type : 'POST',
				url : '../m/sendUserInfo',
	            cache: false,
	            contentType: 'application/json',
				processData: false,
				data : JSON.stringify(userForm),
	            success: function(response) {
	            	$.LoadingOverlay("hide");
	            	location.href = '../m/getCoupon?counponId=' + COUPONID;
	          	},
	          	error: function(response) {
	          		$.LoadingOverlay("hide");
	          		var str = "";
	          		if(response && response.status == 501){
	    				str = "<br>[" + response.responseText + "]";
	    			}
	          		$('.wrapper').unblock();
	    			$('.popup_text').html('使用者資訊送出失敗' + str);
	    			$('.popup_box').css('margin-top', '80%').mousemove();
	    			$('.cancel').css('display', 'none');
	    			$('.confirm').css('display', 'none');
	    			$('.check').css('display', '');
	    			$('.popup_wrapper').css('display', '');
	          	}
	        });
		}
	});
	
	function pageInit(){
		/* 拿取「個人資料保護之法定公告事項」 的 URL */
		$.ajax({
			type : 'GET',
			url : '../m/Game/getAnnouncementUrl',
	        success: function(response) {
	        	$('#announcement').attr('href', response);
	      	},
	      	error: function(response) {
	      		console.error(response);
	      	}
	    });
		
		$('.popup_box').css('width', '66%');
		$('.popup_text').css({'font-size': '1.1em', 'line-height': '25px'});
		$('.popup_btn').css('height', '40px');
		$('.popup_btn a').css({'line-height': '40px', 'font-size': '10px'});
	}
	
	function getFormDetail(){
		var userForm = {};
		userForm.name = $('#name').val();
		userForm.id_card_number = $('#id_card_number').val();
		userForm.phone = $('#phone').val();
		userForm.address = $('#address').val();
		userForm.couponId = COUPONID;
		
		return userForm;
	}
	
	function formValidator(input_object){
		if(input_object.name == ""){
			alert("請輸入姓名！");
			return false;
		}
		
		if(input_object.id_card_number == ""){
			alert("請輸入身份證字號！");
			return false;
		}
		
		if(input_object.phone == ""){
			alert("請輸入電話！");
			return false;
		}
		
		if(input_object.address == ""){
			alert("請輸入地址！");
			return false;
		}
		
		if(!CheckPID(input_object.id_card_number)){
			alert("身份證字號格式錯誤！");
			return false;
		}
		
		if(!$('input[name="announcement"]').is(':checked')){
			alert("請點閱個人資料保護之法定公告事項");
			return false;
		}
		
		return true;
	}
	
	/* 檢查身分證字號格式 */
	function CheckPID(sPID) {
		var sMsg = "正確";

		if (sPID == '') {
			sMsg = "請輸入身分證字號";
			return false;
		} else if (sPID.length != 10) {
			sMsg = "長度應為 10 ！";
			return false;
		} else {
			sPID = sPID.toUpperCase();
			if (!chkPID_CHAR(sPID)) return false;

			var iChkNum = getPID_SUM(sPID);

			if (iChkNum % 10 != 0) {
				var iLastNum = sPID.substr(9, 1) * 1;
				for (i=0; i<10; i++) {
					var xRightAlpNum = iChkNum - iLastNum + i;
					if ((xRightAlpNum % 10) ==0) {
						sMsg = "最後一個數應為：" + i;
						//break;
						return false;
					}
				}
			}
		}
		//alert(sMsg);
		return true;
	}
	
	/* 檢查身分證字號是否有不合法字元 */
	function chkPID_CHAR(sPID) {
		var sMsg = "";
		var iPIDLen = String(sPID).length;

		var sChk = ALP_STR + NUM_STR;
		for(i=0;i<iPIDLen;i++) {
			if (sChk.indexOf(sPID.substr(i,1)) < 0) {
				sMsg = "這個身分證字號含有不正確的字元！";
				break;
			}
		}

		if (sMsg.length == 0) {
			if (ALP_STR.indexOf(sPID.substr(0,1)) < 0) {
				sMsg = "身分證字號第 1 碼應為英文字母(A~Z)。";
			} else if ((sPID.substr(1,1) != "1") && (sPID.substr(1,1) != "2")) {
				sMsg = "身分證字號第 2 碼應為數字(1~2)。";
			} else {
				for(var i=2; i<iPIDLen; i++) {
					if (NUM_STR.indexOf(sPID.substr(i, 1)) < 0) {
						sMsg = "第 " + (i+1) + " 碼應為數字(0~9)。";
						break;
					}
				}
			}
		}

		if (sMsg.length != 0) {
			//alert(sMsg);
			return false;
		} else {
			return true;
		}
	}
	
	/* 驗證身份證字號 */
	function getPID_SUM(sPID) {
		var iChkNum = 0;

		// 第 1 碼
		iChkNum = ALP_STR.indexOf(sPID.substr(0,1)) + 10;
		iChkNum = Math.floor(iChkNum/10) + (iChkNum%10*9);

		// 第 2 - 9 碼
		for(var i=1; i<sPID.length-1; i++) {
			iChkNum += sPID.substr(i,1) * (9-i);
		}

		// 第 10 碼
		iChkNum += sPID.substr(9,1)*1;

		return iChkNum;
	}
});