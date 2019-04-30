/**
 * 
 */

$(function(){
	//全域變數
	var prizeTrTemplate = {};   	//獎品的列
	var gameId = "";		//gameId
	var gameType = "scratchCard";      //遊戲類型
	var actionType = "";			//紀錄是編輯或新增
	var prizeCount = 0;	//目前獎品數量
	var scratchCardPrizeCount = 0;    // 刮刮卡原有的獎品數量
	var consolationPrize = null; //安慰獎
	var MsgFrameTemplate = {};	
	var couponTrTemplate = {};
	var currentCouponList = [];
	
	var initMsgTemplate = function(){
		
		var MsgFrames = $('.MsgPlace .MsgFrame');
		
		$.each(MsgFrames, function(i, o){
			var MsgFrame = $(o).clone();
			MsgFrameTemplate[MsgFrame.attr('type')] = MsgFrame;
		});

		$('.MsgPlace .MsgFrame').remove();
		
		console.info('MsgFrameTemplate', MsgFrameTemplate);
	};

	initMsgTemplate();

	var initCouponTemplate = function(){
	    couponTrTemplate = $('.couponTrTemplate').clone(true);
	    $('.couponTrTemplate').remove();
	};

	initCouponTemplate();
	
	$('#dialogCouponSelect').dialog({
	    autoOpen: false,
	    resizable: false, //不可縮放
	    modal: true, //畫面遮罩
	    draggable: false, //不可拖曳
	    minWidth : 750,
	    position: { my: "top", at: "top", of: window  }
	});
	
	// 建立 COUPON
	var createMsgFrameCOUPON = function(msgType, settingObj){

		/**
		 * Coupon MsgFrame Setting
		 */
		if(settingObj){
			var valueObj = $.BCS.ResourceMap[settingObj.referenceId];
			var src = bcs.bcsContextPath + "/getResource/IMAGE/" + valueObj.couponListImageId;
			settingCouponSelectResult(settingObj.referenceId);
		}
		else{
			// Get Image
			var loadImage = $('#couponListTable').attr('loadImage');
			if(!loadImage){
				// console.info('couponListTable loadImage');
				var couponImgTitle = $('#couponListTable .couponImgTitle img');
				$.each(couponImgTitle, function(i , o){
					var srcUrl = $(o).attr('srcUrl');
					$(o).attr('src', srcUrl);
				});
				$('#couponListTable').attr('loadImage', 'true');
			}
			
			$('#dialogCouponSelect').dialog('open');
		}
	}
	
	// 設定 Delete Message Content Event
	var deleteMsgContentEvent = function(){
		var parentNode = $(this).closest('tbody').attr('id');
		
		$(this).closest('.MsgFrame').remove();
		
		var deletedCouponId = $(this).closest('.MsgFrame').find('.COUPON_ID').val();
		
		if(typeof deletedCouponId !== 'undefined'){
			currentCouponList.splice(findStringInArray(currentCouponList, deletedCouponId), 1);
		}
		
		$('#' + parentNode).find('.TypeMsgSolid').show();
		
		getCouponList();
	};
	
	$.BCS.createMsgFrame = function(msgType, settingObj){
		// Set Each Event
		// console.log("Message type: " + msgType);
		// console.log(settingObj);
		if(msgType == "COUPON"){
			createMsgFrameCOUPON(msgType, settingObj);
		}
	}
	
	var setAddMsgContentBtnEvent = function(){
		var MdBtns = $('.MdBtn');

		$.each(MdBtns, function(i, o){
			var inputBtn = $(o).find('input');
			
			inputBtn.click(function(){
				var msgType = inputBtn.attr('msgType');
				
				sessionStorage.setItem('prizeTrId', $(this).closest('tbody').attr('id'));
				
				$.BCS.createMsgFrame(msgType);
			});
		});
	};
	
	//setAddMsgContentBtnEvent();
	
	var couponSelectEventFunc = function(){
		var selectedCoupon = $(this);
	    var couponId = selectedCoupon.attr('couponId');
	    var tr = selectedCoupon.closest('tr');
	    var src = tr.find('.couponImgTitle').find('img').attr('src');
	    var couponDescription = tr.find('.couponDescription').text();
	    var couponTitle = tr.find('a').text();
	    
	    settingCouponSelectResult(couponId);	    
	    currentCouponList.push(couponId);
	    
	    $('#dialogCouponSelect').dialog('close');
	    
	    getCouponList();
	};
	
	var settingCouponSelectResult = function(couponId){

	    var appendBody = MsgFrameTemplate["COUPON"].clone();
	    
	    $.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getContentCoupon?couponId=' + couponId
		}).success(function(response){
//			console.info(response);
			
			appendBody.css('display', '');

		    appendBody.find('.COUPON_ID').val(couponId);
		    appendBody.find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.couponListImageId);
		    appendBody.find('.COUPON_DESCRIPTION').text(response.couponDescription);
		    appendBody.find('.mdCMN07HeadTtl01').text('優惠劵：' + response.couponTitle);
		    
		    appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);
		    
		    $('#' + sessionStorage.getItem('prizeTrId') + ' .MsgPlace').append(appendBody);
		    $('#' + sessionStorage.getItem('prizeTrId') + ' .TypeMsgSolid').hide();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};
	
	/**
	 * Get Coupon List
	 */
	var getCouponList = function(){
		var param1 = (gameId && actionType === 'Edit') ? gameId : '';
		
		$.ajax({
		    type : "GET",
		    url : bcs.bcsContextPath + '/edit/getUnusedContentCouponList?gameId=' + param1
		}).success(function(response){
		    $('.couponTrTemplate').remove();
		    var coupons = response;
		    
		    $.each(coupons, function(index, coupon) {
		        var couponTr = couponTrTemplate.clone(true);
		        
		        if(currentCouponList.indexOf(coupon.couponId) === -1){
				    couponTr.find('.couponId').val(coupon.couponId);
				    couponTr.find('.couponTitle').text(coupon.couponTitle);
				    couponTr.find('.couponDescription').text(coupon.couponDescription);
				    
				    couponTr.find('.couponImgTitle img').attr('couponId', coupon.couponId);
				    couponTr.find('.couponImgTitle img').attr('srcUrl', bcs.bcsContextPath + "/getResource/IMAGE/" + coupon.couponListImageId);
				    couponTr.find('.couponImgTitle img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + coupon.couponListImageId);
				    couponTr.find('.couponImgTitle img').click(couponSelectEventFunc); 
				    
				    couponTr.find('.couponImgTitle a').attr('couponId', coupon.couponId);
				    couponTr.find('.couponImgTitle a').click(couponSelectEventFunc);
				    
				    couponTr.find('.couponCreateTime').text(moment(coupon.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
				    couponTr.find('.couponCreateUser').text(coupon.modifyUser);
				    
				    $('#couponListTable').append(couponTr);
		        }
		    });
		}).fail(function(response){
		    console.info(response);
		    $.FailResponse(response);
		}).done(function(){
		});
	}
	
	//-------初始化頁面----------
	var initPage = function(){
		prizeTrTemplate = $('.prizeTrTemplate').clone(true);//取下prize格式
		$('.prizeTrTemplate').remove();//刪除prize
		gameId = $.urlParam("gameId"); //從列表頁導過來的參數
		actionType = $.urlParam("actionType"); //從列表頁導過來的參數
		
		/* 編輯優惠券 */
		if(actionType === 'Edit' || actionType === 'Copy'){
			$.ajax({
                type: 'GET',
                url: bcs.bcsContextPath + '/edit/getGame/' + gameType + '/' + gameId,
    		}).success(function(response){
    			console.log(response);
    			$('#gameName').val(response.gameName);
    			$('#gameContent').val(response.gameContent);
    			
    			$('.headerImageTd').find('.imgId').val(response.headerImageId);
    			$('.headerImageTd').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.headerImageId);
    			
    			$('.footerImageTd').find('.imgId').val(response.footerImageId);
    			$('.footerImageTd').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.footerImageId);
    			
    			$('.scratchCardBGImageTd').find('.imgId').val(response.scratchcardBackgroundImageId);
    			$('.scratchCardBGImageTd').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.scratchcardBackgroundImageId);
    			
    			$('.scratchCardFrontImageTd').find('.imgId').val(response.scratchcardFrontImageId);
    			$('.scratchCardFrontImageTd').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.scratchcardFrontImageId);
    			
    			$('.scratchCardStartButtonImageTd').find('.imgId').val(response.scratchcardStartButtonImageId);
    			$('.scratchCardStartButtonImageTd').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.scratchcardStartButtonImageId);
    			
    			if(actionType === 'Edit'){
	    			var prizeTr;
	    			prizeCount = response.couponList.length;
	    			scratchCardPrizeCount = prizeCount;
	    			
	    			for(var i=0; i < prizeCount; i++){
	    				var selectedCouponMsgFrame = MsgFrameTemplate["COUPON"].clone();
	    				prizeTr = generatePrizeTr(i);
	    				
	    				prizeTr.find('.TypeMsgSolid').hide();
	    				
	    				selectedCouponMsgFrame.css('display', '');
	
	    				selectedCouponMsgFrame.find('.COUPON_ID').val(response.couponList[i].couponId);
	    				selectedCouponMsgFrame.find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.couponList[i].couponListImageId);
	    				selectedCouponMsgFrame.find('.COUPON_DESCRIPTION').text(response.couponList[i].couponDescription);
	    				selectedCouponMsgFrame.find('.mdCMN07HeadTtl01').text('優惠劵：' + response.couponList[i].couponTitle);
	    				prizeTr.find('.coupon_probability').val(response.couponList[i].probability);
	    			    
	    				selectedCouponMsgFrame.find('.MdBtn03Delete').click(deleteMsgContentEvent);
	    				selectedCouponMsgFrame.find('.MdBtn03Delete').css("display", "none");
	    				
	    				prizeTr.find('.MsgPlace').append(selectedCouponMsgFrame);
	    				$('.gameTable').append(prizeTr);
	    				
	    				currentCouponList.push(response.couponList[i].couponId);
					}
	    			
	    			setAddMsgContentBtnEvent();
	    			setActionOperationButtonsVisable();
	    			
	    			console.log(currentCouponList);
    			} else {
    				addPrize();
    			}
    			getCouponList();
    		}).fail(function(response){
    			console.info(response);
    			$.FailResponse(response);
    		}).done(function(){
    		});
		} else {    //  建立新優惠券
			actionType = "Create";
			getCouponList();
			addPrize();
		}
	}
	
	//------選擇安慰獎-----------
	/*$("input[name='isConsolationPrize']").click(function(e){
		if(consolationPrize != null){
			consolationPrize.find('.prizeQuantity').removeAttr('disabled');
			consolationPrize.find('.prizeQuantity').attr("placeholder","");
		}
		consolationPrize = $(this).closest(".prizeTrTemplate");
		
		consolationPrize.find('.prizeQuantity').val('');
		consolationPrize.find('.prizeQuantity').attr("disabled","disabled");
		consolationPrize.find('.prizeQuantity').attr("placeholder","無限制");
	});*/
	
	//-----------動態產生prizeTr-----------
	var generatePrizeTr = function(prizeCount){
		var prizeTr = prizeTrTemplate.clone(true);
		
		var prizeTarget = 'prize' + prizeCount;
		prizeTr.attr('id', prizeTarget);
		var letter = String.fromCharCode(65 + prizeCount);
		prizeTr.find(".typeSideTxt").html(letter);
		
		return prizeTr;
	}
	
	//-------增加獎品----------
	var addPrize = function(){		
		$('.gameTable').append(generatePrizeTr(prizeCount));
		prizeCount++;
		
		setAddMsgContentBtnEvent();
		setActionOperationButtonsVisable();
	}
	
	$('#addPrize').click(addPrize);
	
	//-------刪除獎品-------------
	var deletePrize = function(){
		var deletedCouponId = $('.gameTable .prizeTrTemplate:last-child').find('.COUPON_ID').val();
		
		if(typeof deletedCouponId !== 'undefined'){
			currentCouponList.splice(findStringInArray(currentCouponList, deletedCouponId), 1);
		}
		
		$('.gameTable .prizeTrTemplate:last-child').remove();
		prizeCount--;
		
		setActionOperationButtonsVisable();
		getCouponList();
	}
	
	$('#deletePrize').click(deletePrize);
	
	/* 「刪除獎品」的按鈕的 消失/顯示 控制 */
	var setActionOperationButtonsVisable = function(){
		if(actionType === 'Edit'){
			if(prizeCount == scratchCardPrizeCount){
				$('#deletePrize').hide();
			} else {
				$('#deletePrize').show();
			}
		} else {
			if(prizeCount == 1){
				$('#deletePrize').hide();
			} else {
				$('#deletePrize').show();
			}
		}
	}
	
	//----------儲存按鈕--------------
	$('#save').click(function(){
		var saveGameUrl = '';
		
		if(!checkTemplateTableValid()){
			return;
		}
		
		var game = getGameTableInformation();
		
		for(var key in game){
			console.info('key : '+key+'   value : '+game[key]);
		}
		
		if(gameId)
			saveGameUrl = bcs.bcsContextPath + '/edit/createGame/' + gameType + '?actionType=' + actionType + '&gameId=' + gameId;
		else
			saveGameUrl = bcs.bcsContextPath + '/edit/createGame/' + gameType + '?actionType=' + actionType + '&gameId=';
		
		if (!confirm(actionType == 'Create' ? '請確認是否建立' : '請確認是否儲存')) {
			return false;
		}
		
		$('.LyMain').block($.BCS.blockMsgSave);
		
		$.ajax({
			type : "POST",
			url : saveGameUrl,
	        cache: false,
	        contentType: 'application/json',
	        processData: false,
			data : JSON.stringify(game)
		}).success(function(response){
			console.info('response',response);
			
			if (actionType == "Edit") {
				alert("儲存遊戲成功！");
			} else {
				alert("建立遊戲成功！");
			}
			
			window.location.replace(bcs.bcsContextPath + '/edit/gameListPage');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	});
	
	//取得game的資料
	var getGameTableInformation = function(){
		var prizeTr;
		var coupon_list = [];
		var game = {};
		
		for(var i=0; i<prizeCount; i++){
			prizeTr = $('tbody[id="prize'+i+'"]');			
			
			coupon_list.push({
				id: prizeTr.find('.COUPON_ID').val(), 
				probability: prizeTr.find('.coupon_probability').val(),
				identityLetter: prizeTr.find('.typeSideTxt').text()
			});
		}
		
		game.gameName = $('.gameTable').find('#gameName').val();
		game.gameContent = $('.gameTable').find('#gameContent').val();
		game.gameType = gameType;
		game.headerImageId = $('.headerImageTd').find('.imgId').val();
		game.footerImageId = $('.footerImageTd').find('.imgId').val();
		game.scratchCardBGImageId = $('.scratchCardBGImageTd').find('.imgId').val();
		game.scratchCardFrontImageId = $('.scratchCardFrontImageTd').find('.imgId').val();
		game.scratchCardStartButtonImageId = $('.scratchCardStartButtonImageTd').find('.imgId').val();
		game.couponList = coupon_list;
		
		return game;
	}
	
	//檢查是否有不合法資料
	var checkTemplateTableValid = function(){
		var prizeTr;
		var prizeLetter;
		var totalProbability = 0.00;
		
		if($('.gameTable').find('#gameName').val() == ""){
			alert("請輸入遊戲名稱!");
			return false;
		}
		
		if($('.gameTable').find('#gameContent').val() == ""){
			alert("請輸入遊戲內容!");
			return false;
		}
		
		if($('.headerImageTd').find('.imgId').val() == ""){
			alert("請上傳活動標題圖片!");
			return false;
		}
		
		if($('.footerImageTd').find('.imgId').val() == ""){
			alert("請上傳活動說明圖片!");
			return false;
		}
		
		if($('.scratchCardBGImageTd').find('.imgId').val() == ""){
			alert("請上傳刮刮卡背景!");
			return false;
		}
		
		if($('.scratchCardFrontImageTd').find('.imgId').val() == ""){
			alert("請上傳刮刮卡圖案!");
			return false;
		}
		
		if($('.scratchCardStartButtonImageTd').find('.imgId').val() == ""){
			alert("請上傳「開始遊戲」按鈕圖案!");
			return false;
		}
		
		/*if(consolationPrize == null){
			alert("請選擇安慰獎!");
			return false;
		}*/
		console.log(prizeCount);
		
		for(var i=0; i<prizeCount; i++){
			prizeTr = $('tbody[id="prize'+i+'"]');
			prizeLetter = prizeTr.find('.typeSideTxt').text();
			
			if(!prizeTr.find('.COUPON_ID').val()){
				alert("請選擇獎品" + prizeLetter + "的優惠券!");
				return false;
			}
			if(!prizeTr.find('.coupon_probability').val()){
				alert("請輸入獎品" + prizeLetter + "的中獎機率!");
				return false;
			}
			if(prizeTr.find('.coupon_probability').val() === 0){
				alert("獎品" + prizeLetter + "的中獎機率必須大於0!");
				return false;
			}
			
			totalProbability += Number(prizeTr.find('.coupon_probability').val());
		}
		
		if(totalProbability != 100.00){
			console.info("totalProbability : "+totalProbability);
			alert("全部獎品機率總和不為100.00!");
			return false;
		}
		
		return true;
	}
	
	//取消鈕
	$('input[name="cancel"]').click(function() {
		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		window.location.replace(bcs.bcsContextPath + '/edit/gameListPage');
	});
	
	//--------上傳圖片------------------OK
	$(".image").on("change", function(e) {
		var input = e.currentTarget;
    	if (input.files && input.files[0]) {
    		if(input.files[0].size < 1048576) {
	    		var fileName = input.files[0].name;
	    		var form_data = new FormData();
	    		
	    		form_data.append("filePart",input.files[0]);
	
	    		$('.LyMain').block($.BCS.blockMsgUpload);
	    		$.ajax({
	                type: 'POST',
	                url: bcs.bcsContextPath + "/edit/createResource?resourceType=IMAGE",
	                cache: false,
	                contentType: false,
	                processData: false,
	                data: form_data
	    		}).success(function(response){
	            	console.info(response);
	            	alert("上傳成功!");
	            	$(input).closest('.MdFRM03File').find('.imgId').val(response.resourceId);
	            	$(input).closest('.MdFRM03File').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
	    		}).fail(function(response){
	    			console.info(response);
	    			$.FailResponse(response);
	    			$('.LyMain').unblock();
	    		}).done(function(){
	    			$('.LyMain').unblock();
	    		});
    		} else {
    			alert("圖片大小不可大於 1MB！");
    		}
        } 
	});
	
	initPage();
	
	function findStringInArray(array, target){
		var index = -1;
		
		for(var i = 0; i < array.length; i++){
			if(array[i] === target){
				index = i;
				
				return index;
			}
		}
		
		return index;
	}
});