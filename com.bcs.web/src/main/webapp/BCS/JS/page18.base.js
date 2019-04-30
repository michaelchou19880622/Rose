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
		}
	});
	
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
		
		var sendingMsgTime = "";

		if (!validator.form()) {
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

		postData.interactiveType = "BLACK_KEYWORD";
		
		postData.interactiveStatus = interactiveStatus;
		
		postData.userStatus = userStatus;
		postData.keywordInput = keywordInput;

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
			var url = bcs.bcsContextPath +'/edit/blackKeywordResponsePage';
			
			if(interactiveStatus == 'DISABLE'){
				url = bcs.bcsContextPath +'/edit/blackKeywordResponseDisablePage';
			}
			else if(interactiveStatus == 'ACTIVE'){
				url = bcs.bcsContextPath +'/edit/blackKeywordResponsePage';
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
		var url = bcs.bcsContextPath +'/edit/blackKeywordResponseDisablePage';
		
		if(fromParam == 'blackKeywordResponseDisablePage'){
			url = bcs.bcsContextPath +'/edit/blackKeywordResponseDisablePage';
		}
		else if(fromParam == 'blackKeywordResponsePage'){
			url = bcs.bcsContextPath +'/edit/blackKeywordResponsePage';
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
			
			var getDataUrl = bcs.bcsContextPath +'/edit/getBlackInteractiveMsg';
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
				var keyObj = response.MsgMain;
				console.info('keyObj', keyObj);

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

				$('.CHTtl').html('複製黑名單');
			}
			// 編輯訊息
			else if($.BCS.actionTypeParam == "Edit"){

				$('.CHTtl').html('編輯黑名單');
			}
		}
	};

	var optionSelectChange_func = function(){
		var selectValue = $(this).find('option:selected').text();
		$(this).closest('.option').find('.optionLabel').html(selectValue);
	};
	
	$('.userStatus').change(optionSelectChange_func);

	$('#keywordInput').keyup(function() {
		var txtLength = $(this).val().length;
		var richMsgUrlTxtTr = $(this).closest("td");
		richMsgUrlTxtTr.find(".MdTxtInputCount").html(txtLength + "/30");
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