/**
 * 
 */

$(function(){
	//全域變數
	var prizeTrTemplate = {};    	//獎品的列
	var gameId = "";			//gameId
	var gameType = "turntable";      //遊戲類型
	var actionType = "";			//紀錄是編輯或新增
	var prizeCount = 0;			//目前獎品數量
	var consolationPrize = null; //安慰獎
	
	$("#tabs").tabs({ active: 0});
	//-------初始化頁面----------
	var initPage = function(){
		prizeTrTemplate = $('.prizeTrTemplate').clone(true);//取下prize格式
		$('.prizeTrTemplate').remove();//刪除prize
		gameId = $.urlParam("gameId"); //從列表頁導過來的參數
		actionType = $.urlParam("actionType"); //從列表頁導過來的參數
		
		//從列表進來
		if(gameId != null && gameId != ""){
			$.ajax({
                type: 'GET',
                url: bcs.bcsContextPath + '/edit/getGame/' + gameType + '/' + gameId,
    		}).success(function(response){
    			console.info("response :  ",response);
    			
    			$('#gameName').val(response.gameName);
    			$('#gameContent').val(response.gameContent);
    			
    			$('.headerImageTd').find('.imgId').val(response.headerImageId);
    			$('.headerImageTd').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.headerImageId);
    			
    			$('.footerImageTd').find('.imgId').val(response.footerImageId);
    			$('.footerImageTd').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.footerImageId);
    			
    			$('.turntableImageTd').find('.imgId').val(response.turntableImageId);
    			$('.turntableImageTd').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.turntableImageId);
    			
    			$('.turntableBGImageTd').find('.imgId').val(response.turntableBackgroundImageId);
    			$('.turntableBGImageTd').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.turntableBackgroundImageId);

    			$('.pointerImageTd').find('.imgId').val(response.pointerImageId);
    			$('.pointerImageTd').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.pointerImageId);

    			$('.shareImageTd').find('.imgId').val(response.shareImageId);
    			$('.shareImageTd').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.shareImageId);

    			$('.shareSmallImageTd').find('.imgId').val(response.shareSmallImageId);
    			$('.shareSmallImageTd').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.shareSmallImageId);
    			
    			$('.shareInput').val(response.shareMsg);
    			
    			var prizeTr;
    			prizeCount = response.prizes.length;
    			
    			for(var i=0; i<prizeCount; i++){
    				prizeTr = generatePrizeTr(i);
					
    				prizeTr.find('.prizeName').val(response.prizes[i].prizeName);
    				prizeTr.find('.imgId').val(response.prizes[i].prizeImageId);
    				prizeTr.find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.prizes[i].prizeImageId);
    				prizeTr.find('.prizeContent').val(response.prizes[i].prizeContent);
    				prizeTr.find('.prizeQuantity').val(response.prizes[i].prizeQuantity);
    				prizeTr.find('.prizeProbability').val(response.prizes[i].prizeProbability);
    				prizeTr.find('.message').val(response.prizes[i].messageText);
    				
    				if(response.prizes[i].isConsolationPrize){
    					prizeTr.find("input[name='isConsolationPrize']").prop('checked', true);
    				}else{
    					prizeTr.find("input[name='isConsolationPrize']").prop('checked', false);    					
    				}
    				
    				$('.gameTable').append(prizeTr);
				}
    			
    			$.each($('input[name="prizeNumber"]'), function(i, v) {
					if (v.value == prizeCount) {
						v.checked = true;
					}
				});
    			
    			$("input[name='isConsolationPrize']:checked").trigger("click");

    			$('.turntableImageTd').find('a').attr('href', '../../../BCS/images/turntable_'+ prizeCount +'.png');
    		}).fail(function(response){
    			console.info(response);
    			$.FailResponse(response);
    		}).done(function(){
    		});
		//從menu進來
		}else{
			actionType = "Create";
			var prizeNumber = $("input[name='prizeNumber']");
			prizeNumber[0].checked = true;
			$("input[name='prizeNumber']:checked").trigger("click");
		}
	}
	
	//------選擇prize數量-----------
	$("input[name='prizeNumber']").click(function(e){
		prizeCount = e.currentTarget.value;
		$('.prizeTrTemplate').remove();
		
		for(var i = 0; i < prizeCount; i++){
			$('.gameTable').append(generatePrizeTr(i));
		}
		
		$('.turntableImageTd').find('a').attr('href', '../../../BCS/images/turntable_'+ prizeCount +'.png');
	});
	
	//---------radios綁圖片---------
	var radios = $("input[name='prizeNumber']");
	$.each(radios, function(i, o){
		$(o).closest('.typeMenu').find('img').click(function(){
			$(o).click();
		});
	})
	
	//------選擇安慰獎-----------
	$("input[name='isConsolationPrize']").click(function(e){
		if(consolationPrize != null){
			consolationPrize.find('.prizeQuantity').removeAttr('disabled');
			consolationPrize.find('.prizeQuantity').attr("placeholder","");
		}
		consolationPrize = $(this).closest(".prizeTrTemplate");
		
		consolationPrize.find('.prizeQuantity').val('');
		consolationPrize.find('.prizeQuantity').attr("disabled","disabled");
		consolationPrize.find('.prizeQuantity').attr("placeholder","無限制");
	});
	
	//-----------動態產生prizeTr-----------
	var generatePrizeTr = function(prizeCount){
		var prizeTr = prizeTrTemplate.clone(true);
		
		var prizeTarget = 'prize' + prizeCount;
		prizeTr.attr('name', prizeTarget);
		var letter = String.fromCharCode(65 + prizeCount);
		prizeTr.find(".typeSideTxt").html(letter);
		
		return prizeTr;
	}
	
	//----------儲存按鈕--------------
	$('#save').click(function(){
		if(!checkTemplateTableValid()){
			return;
		}
		
		var game = getGameTableInformation();
		
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/edit/createGame/' + gameType + '?actionType=' + actionType + '&gameId=' + gameId,
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
		var prizes = [];
		var game = {};
		
		for(var i=0; i<prizeCount; i++){
			prizeTr = $('tbody[name="prize'+i+'"]');			
			prizes.push({
					prizeName : prizeTr.find('.prizeName').val(),
					prizeLetter : prizeTr.find('.typeSideTxt').text(),
					prizeImageId : prizeTr.find('.imgId').val(),
					prizeContent : prizeTr.find('.prizeContent').val(),
					prizeQuantity : prizeTr.find('.prizeQuantity').val(),
					prizeProbability : prizeTr.find('.prizeProbability').val(),
					message : prizeTr.find('.message').val()
					});
			
			if(prizeTr.find("input[name='isConsolationPrize']").prop('checked')){
				prizes[i].isConsolationPrize = true;
				prizes[i].prizeQuantity = 1;
			}
			else{
				prizes[i].isConsolationPrize = false;
			}
		}
				
		game.gameName = $('#gameName').val();
		game.gameContent = $('#gameContent').val();
		game.gameType = gameType;
		game.headerImageId = $('.headerImageTd').find('.imgId').val();
		game.footerImageId = $('.footerImageTd').find('.imgId').val();
		game.turntableImageId = $('.turntableImageTd').find('.imgId').val();
		game.turntableBGImageId = $('.turntableBGImageTd').find('.imgId').val();
		game.pointerImageId = $('.pointerImageTd').find('.imgId').val();
		game.shareImageId = $('.shareImageTd').find('.imgId').val();
		game.shareSmallImageId = $('.shareSmallImageTd').find('.imgId').val();
		game.prizes = prizes;
		game.shareMsg = $('.shareInput').val();
		
		return game;
	}
	
	//檢查是否有不合法資料
	var checkTemplateTableValid = function(){
		var prizeTr;
		var prizeLetter;
		var totalProbability = 0.00;
		
		if($('#gameName').val() == ""){
			alert("請輸入遊戲名稱!");
			return false;
		}
		
		if($('#gameContent').val() == ""){
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
		
		if($('.turntableImageTd').find('.imgId').val() == ""){
			alert("請上傳轉盤圖片!");
			return false;
		}
		
		if($('.turntableBGImageTd').find('.imgId').val() == ""){
			alert("請上傳轉盤背景圖片!");
			return false;
		}
		
		if($('.pointerImageTd').find('.imgId').val() == ""){
			alert("請上傳指針圖片!");
			return false;
		}
		
		if($('.shareInput').val() == ""){
			alert("請輸入分享訊息!");
			return false;
		}
		
		if(consolationPrize == null){
			alert("請選擇安慰獎!");
			return false;
		}
		
		for(var i=0; i<prizeCount; i++){
			prizeTr = $('tbody[name="prize'+i+'"]');
			prizeLetter = prizeTr.find('.typeSideTxt').text();
			
			if(prizeTr.find('.prizeName').val() == ""){
				alert("請輸入獎品" + prizeLetter + "名稱!");
				return false;
			}
			
			if(prizeTr.find('.imgId').val() == ""){
				alert("請上傳獎品" + prizeLetter + "圖片!");
				return false;
			}
			
			if(prizeTr.find('.prizeContent').val() == ""){
				alert("請輸入獎品" + prizeLetter + "內容!");
				return false;
			}
			
			if(!prizeTr.find("input[name='isConsolationPrize']").prop('checked')){
				if(prizeTr.find('.prizeQuantity').val() == ""){
					alert("請輸入獎品" + prizeLetter + "數量!");
					return false;
				}
				else if(prizeTr.find('.prizeQuantity').val() == "0"){
					alert("獎品" + prizeLetter + "數量不可為0");
					return false;
				}
			}
			
			if(prizeTr.find('.prizeProbability').val() == ""){
				alert("請輸入獎品" + prizeLetter + "機率!");
				return false;
			}
			
			//if(prizeTr.find('.message').val() == ""){
			//	alert("請輸入獎品" + prizeLetter + "的自動回應訊息!");
			//	return false;
			//}
			
			totalProbability += Number(prizeTr.find('.prizeProbability').val());
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
});