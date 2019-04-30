/**
 * 
 */

$(function(){
	var gameId = "";			//gameId
	var checkboxArray = [
		'Hair',
		'Oral',
		'House',
		'Female',
		'Male',
		'Baby'
	];
	var ALP_STR = "ABCDEFGHJKLMNPQRSTUVXYWZIO";
	var NUM_STR = "0123456789";
	
	var shareMsg = "";
	
	var initPage = function(){
		gameId = $.urlParam("gameId");
		MID =$.urlParam("UID");
		
		if(!gameId){
			gameId = $('#gameId').val();
		}
		
		history.pushState(null, "", location.href.split("?")[0]);    // 將網址後面的 MID 資訊拿掉
		
		$.ajax({
            type: 'GET',
            url: '../m/Game/getPrizeDetail/'+gameId,
            success: function(response){
        	    $('.prizeImageShow').find('img').attr('src', "../bcs/getResource/IMAGE/" + response['prizeImageId']);
          	},
          	error: function(response){
          		console.info(response);
          	}, 
          	complete: function(){
          	}
        });
		
		$.ajax({
			type: 'GET',
            url: '../m/Game/scratchCard/' + gameId,
            success: function(response){
            	$('.headerImage').find('img').attr('src', '../bcs/getResource/IMAGE/' + response.headerImageId);
    			$('.footerImage').find('img').attr('src', '../bcs/getResource/IMAGE/' + response.footerImageId);
    			if(response.shareImageId && response.shareImageId != ""){
    				$('.shareImage').find('img').attr('src', '../bcs/getResource/IMAGE/' + response.shareImageId);
    			}
    			if(response.shareSmallImageId && response.shareSmallImageId != ""){
    				$('.shareSmallImage').find('img').attr('src', '../bcs/getResource/IMAGE/' + response.shareSmallImageId);
    			}
    			shareMsg = response.shareMsg;
          	},
          	error: function(response){
          		console.info(response);
          	}, 
          	complete: function(){
          	}
        });
	}
	
	$('#confirm').click(function(){
		var acceptPrizeModel = {};
		
		if(!checkValid()){
			return;
		}
		acceptPrizeModel.winnerName = $("#winner_name").val();
		acceptPrizeModel.winnerIdCardNumber = $("#winner_id_card").val();
		acceptPrizeModel.winnerPhone = $("#winner_phone").val();
		acceptPrizeModel.winnerAddress = $("#winner_address").val();
		
		$.LoadingOverlay("show");
		
		$.ajax({
			type : 'POST',
			url : '../m/Game/acceptPrize/' + gameId,
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(acceptPrizeModel),
            success: function(response) {
            	$.LoadingOverlay("hide");
            	
    			if(response == 'success') {
    				//window.location = 'm/turntableIndexPage?gameId=' + gameId;
    			} else {
    				alert("您已領取過獎品!");
    			}
    			
    			location.reload();
          	},
          	error: function(response) {
          		console.info(response);
          	
          		alert("系統錯誤!\n請稍後再試 ~");
          		
          		$.LoadingOverlay("hide");
          	}, 
          	complete: function() {
          	}
        });
	});
	
	//檢查是否有不合法資料
	var checkValid = function(){
		if($('#winner_name').val() == ""){
			alert("請輸入姓名！");
			return false;
		}
		
		if($('#winner_id_card').val() == ""){
			alert("請輸入身份證字號！");
			return false;
		}
		
		if($('#winner_phone').val() == ""){
			alert("請輸入電話！");
			return false;
		}
		
		if($('#winner_address').val() == ""){
			alert("請輸入地址！");
			return false;
		}
		
		if(!CheckPID($('#winner_id_card').val())){
			alert("身份證字號格式錯誤！");
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
		//sPID = trim(sPID.toUpperCase());
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
	
	initPage();    // 初始化頁面
});