/**
 * 
 */
$(function(){
	
	$.BCS.actionTypeParam = $.urlParam("actionType");

	/**
	 * 紀錄 最後按鈕
	 */
	var btnTarget = "";
	
	// 表單驗證
	var validator = $('#formSendGroup').validate({
		rules : {
			
			// 使用者狀態
			'userStatus' : {
				required : {
			        param: true,
			        depends: function(element) {
						if(btnTarget == "SaveSetting"){
							return true;
						}
						return false;
			        }
				}
			},
			// 關鍵字
			'keywordInput' : {
				required : {
			        param: true,
			        depends: function(element) {
						if(btnTarget == "SaveSetting"){
							return true;
						}
						return false;
			        }
				}
			},
			// 回應內容
			'responseInput' : {
				required : {
			        param: true
				}
			},
			// 回應連結
			'linkInpu' : {
				required : {
			        param: true
				}
			},
			// 連結說明
			'linkDesc' : {
				required : {
			        param: true
				}
			},
		}
	});
	
	/**
	 * SendToMe
	 * SendToTestGroup
	 */
	// 傳送訊息
	var sendingMsgFunc = function(actionType){
		btnTarget = actionType;

		if($.BCS.actionTypeParam == "Look"){
			location.reload();
			return;
		}
		
		$.BCS.isValidate  = true;
		// 發送內容 設定
		var MsgFrameContents = {}; 
		console.info('MsgFrameContents', MsgFrameContents);
		/**
		 * Validate Error
		 */
		if(!$.BCS.isValidate){
			return;
		}
		
		var sendingMsgTime = "";

		if (!validator.form()) {
			return;
		}
		
		if(!MsgFrameContents || MsgFrameContents.length < 1){
			alert('請設定發送內容');
			return;
		}
		
		/**
		 * Do Confirm Check
		 */
		var confirmStr = "請確認是否傳送";

		var r = confirm(confirmStr);
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		// 設定傳送資料
		var postData = {};
		postData.actionType = actionType;
		
		postData.sendGroupId = null;
		postData.sendingMsgType = null;

		postData.sendMsgDetails = MsgFrameContents;
		
		postData.sendingMsgTime = null;
		
		postData.msgTag = null;

		console.info('postData', postData);

		// 傳送資料
		$('.LyMain').block($.BCS.blockMsgSave);
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath +'/edit/sendingMsg',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			
			alert('傳送成功');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};
	
	/**
	 * SaveSetting
	 */
	// 設定
	var saveSettingFunc = function(actionType){
		btnTarget = actionType;

		if($.BCS.actionTypeParam == "Look"){
			location.reload();
			return;
		}

		// 設定狀態
		var interactiveStatus = $('.interactiveStatus').val();
		console.info('interactiveStatus', interactiveStatus);
		
		// 使用者狀態
		var userStatus = $('.userStatus').val();
		console.info('userStatus', userStatus);
		
		// 關鍵字
		var keywordInput = $('#keywordInput').val();
		console.info('keywordInput', keywordInput);
		
		// 追加關鍵字
		var otherKeywordList = $('.otherKeyword');
		console.info('otherKeywordList', otherKeywordList);
		
		var otherKeywords = [];
		$.each(otherKeywordList, function(i ,o){
			var otherKeyword = $(o).val();
			otherKeywords.push(otherKeyword);
		});
		
		$.BCS.isValidate  = true;
		// 發送內容 設定
		var MsgFrameContents = [];
		
		var text = {};
		text.detailType = "TEXT";
		text.detailContent ='{"Text":"' + $("#responseInput").val() + '"}';
		MsgFrameContents.push(text);

		var link = {};
		link.detailType = "INTERACTIVE_LINK";
		link.detailContent ='{"textParams":"' + $('#linkDesc').val() + '","linkUriParams":"' + $('#linkInpu').val() + '","linkTag":""}';
		MsgFrameContents.push(link);
		
		
		console.info('MsgFrameContents', MsgFrameContents);
		/**
		 * Validate Error
		 */
		if(!$.BCS.isValidate){
			return;
		}
		
		var sendingMsgTime = "";

		if (!validator.form()) {
			return;
		}
		
		if(!MsgFrameContents || MsgFrameContents.length < 1){
			alert('請設定發送內容');
			return;
		}
		
		/**
		 * Do Confirm Check
		 */
		var confirmStr = "請確認是否設定";

		var r = confirm(confirmStr);
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		// 設定資料
		var postData = {};
		postData.actionType = actionType;

		postData.interactiveType = "INTERACTIVE";
		
		postData.interactiveStatus = interactiveStatus;
		
		postData.userStatus = userStatus;
		postData.keywordInput = keywordInput;

		postData.sendMsgDetails = MsgFrameContents;
		postData.otherKeywords = otherKeywords;

		var iMsgId = $.urlParam("iMsgId");
		
		if($.BCS.actionTypeParam == "Edit"){
			postData.iMsgId = iMsgId;
		}
		console.info('postData', postData);

		// 傳送資料
		$('.LyMain').block($.BCS.blockMsgSave);
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath +'/edit/settingInteractiveMsg',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			
			alert('設定成功');
			var fromParam = $.urlParam("from");
			var url = bcs.bcsContextPath +'/edit/interactiveResponsePage';
			
			if(interactiveStatus == 'DISABLE'){
				url = bcs.bcsContextPath +'/edit/interactiveResponseDisablePage';
			}
			else if(interactiveStatus == 'ACTIVE'){
				url = bcs.bcsContextPath +'/edit/interactiveResponsePage';
			}
			
			window.location.replace(url);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};
	
	// 設定
	$('.btn_save').click(function(){
		saveSettingFunc("SaveSetting");
	});
	
	// 設定
	$('.btn_add').click(function(){
		saveSettingFunc("SaveSetting");
	});
			
	var cancelFunc = function(){

		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		var fromParam = $.urlParam("from");
		var url = bcs.bcsContextPath +'/edit/interactiveResponseDisablePage';
		
		if(fromParam == 'interactiveResponseDisablePage'){
			url = bcs.bcsContextPath +'/edit/interactiveResponseDisablePage';
		}
		else if(fromParam == 'interactiveResponsePage'){
			url = bcs.bcsContextPath +'/edit/interactiveResponsePage';
		}
		
		window.location.replace(url);
	}
	
	$('.CancelBtn').click(cancelFunc);

	// 取消
	$('.btn_cancel').click(cancelFunc);
	
	// 取得設定資料
	var loadDataFunc = function(){

		var iMsgId = $.urlParam("iMsgId");
		
		/**
		 * Load Back Send Message Data
		 */
		if(iMsgId){
			
			var getDataUrl = bcs.bcsContextPath +'/edit/getInteractiveMsg';
			if(iMsgId){
				getDataUrl =getDataUrl + '?iMsgId=' + iMsgId;
			}

    		$('.LyMain').block($.BCS.blockMsgRead);
			$.ajax({
				type : "GET",
				url : getDataUrl
			}).success(function(response){
				$('.dataTemplate').remove();
				
				$.BCS.ResourceMap = response.ResourceMap;
				
				// 回寫 資料
				for(key in response.MsgMain){

					var keyObj = JSON.parse(key);
					console.info('keyObj', keyObj);
					var valueObj = response.MsgMain[key];
					console.info('valueObj', valueObj);

					var iMsgId = keyObj.iMsgId;
					console.info('iMsgId', iMsgId);
					
					$('.interactiveStatus').val(keyObj.interactiveStatus);
					
					$('.userStatus').val(keyObj.userStatus);
					$('.userStatus').change();
					
					$('#keywordInput').val(keyObj.mainKeyword);
					$('#keywordInput').trigger("keyup");
					
					// 重新產生 追加關鍵字
					var otherKeywords = $.BCS.ResourceMap['iMsgId-' + iMsgId];
					if(otherKeywords){
						$.each(otherKeywords, function(i, o){
							var otherKeyword = o.otherKeyword;
							createKeywordBody(otherKeyword);
						});
					}
					
					// 重新產生已經設定的訊息
					$.each(valueObj, function(i , o){
						var msgType = o.msgType;
						if(msgType == "TEXT"){
							$('#responseInput').val(o.text);
						}
						else if(msgType == "INTERACTIVE_LINK"){
							var linkUrl = $.BCS.ResourceMap[o.referenceId].linkUrl;
							$('#linkInpu').val(linkUrl);

							var linkTitle = $.BCS.ResourceMap[o.referenceId].linkTitle;
							$('#linkDesc').val(linkTitle);
						}
					});
				}
				
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
    			$('.LyMain').unblock();
			}).done(function(){
    			$('.LyMain').unblock();
			});
			
			// 查看訊息
			if($.BCS.actionTypeParam == "Look"){
				// Remove All Button
				$('.CHTtl').html('查看設定');
				$('.SendToMeBtn').remove();
				$('.SendToTestBtn').remove();
				$('.btn_add').remove();
				$('.btn_save').remove();

				$('.TypeMsgSolid').remove();
			}
			// 複製訊息
			else if($.BCS.actionTypeParam == "Copy"){

				$('.CHTtl').html('複製訊息');
			}
			// 編輯訊息
			else if($.BCS.actionTypeParam == "Edit"){

				$('.CHTtl').html('編輯訊息');
			}
		}
	};

	var optionSelectChange_func = function(){
		var selectValue = $(this).find('option:selected').text();
		$(this).closest('.option').find('.optionLabel').html(selectValue);
	};
	
	$('.userStatus').change(optionSelectChange_func);

	$('.inputKeyup').keyup(function() {
		var txtLength = $(this).val().length;
		var richMsgUrlTxtTr = $(this).closest("td");
		richMsgUrlTxtTr.find(".MdTxtInputCount").html(txtLength + "/" + richMsgUrlTxtTr.find(".MdTxtInputCount").attr('limit'));
	});
	
	var createKeywordBody = function(keywordSetting){

		var keywordBody = templateBody.clone(true);
		
		var nameStr = 'keyword' + keywordCount;
		keywordBody.find('input').attr('name', nameStr);
		keywordCount++;
		
		if(keywordSetting){
			keywordBody.find('input').val(keywordSetting);
		}
		
		keywordBody.find('.btn_delete').click(function(){
			$(this).closest('.dataTemplate').remove();
		});
		
		$('.addKeyword').before(keywordBody);

		$('#formSendGroup').find('[name="' + nameStr + '"]').rules("add", {
			required : {
		        param: true,
		        depends: function(element) {
					if(btnTarget == "SaveSetting"){
						return true;
					}
					return false;
		        }
			}
		});
	}
	
	var keywordCount = 0;
	$('.addKeyword').click(function(){
		createKeywordBody();
	});

	var templateBody = {};
	templateBody = $('.dataTemplate').clone(true);
	$('.dataTemplate').remove();
	
	loadDataFunc();
});