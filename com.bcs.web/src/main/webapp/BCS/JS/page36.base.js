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
			// 回應連結
			'campaignId' : {
				required : {
			        param: true
				}
			},
			// 錯誤次數上限
			'errorLimit' : {
				number : true
			},
			// Timeout秒數
			'timeout' : {
				number : true
			}
		}
	});

	var eventTypeMap = {
		'UPLOAD_INVOICE': {
			dialogId: 'dialogMsgUploadInvoice',
			id: 'msgUploadInvoice',
			eventType: 'UPLOAD_INVOICE'
		},
		'UPLOAD_INVOICE_SUCCESS': {
			dialogId: 'dialogMsgUploadInvoiceSuccess',
			id: 'msgUploadInvoiceSuccess',
			eventType: 'UPLOAD_INVOICE_SUCCESS'
		},
		'UPLOAD_INVOICE_FAIL': {
			dialogId: 'dialogMsgUploadInvoiceFail',
			id: 'msgUploadInvoiceFail',
			eventType: 'UPLOAD_INVOICE_FAIL'
		},
		'TYPE_IN_INVTERN': {
			dialogId: 'dialogMsgTypeInInvtern',
			id: 'msgTypeInInvtern',
			eventType: 'TYPE_IN_INVTERN'
		},
		'TYPE_IN_INVTERN_FAIL': {
			dialogId: 'dialogMsgTypeInInvternFail',
			id: 'msgTypeInInvternFail',
			eventType: 'TYPE_IN_INVTERN_FAIL'
		},
		'TYPE_IN_RANDON_NUM': {
			dialogId: 'dialogMsgTypeInRandonNum',
			id: 'msgTypeInRandonNum',
			eventType: 'TYPE_IN_RANDON_NUM'
		},
		'TYPE_IN_RANDON_NUM_FAIL': {
			dialogId: 'dialogMsgTypeInRandonNumFail',
			id: 'msgTypeInRandonNumFail',
			eventType: 'TYPE_IN_RANDON_NUM_FAIL'
		},
		'DECODE_FAIL': {
			dialogId: 'dialogMsgDeocodeFail',
			id: 'msgDecodeFail',
			eventType: 'DECODE_FAIL'
		},
		'NOT_IN_INTERNAL': {
			dialogId: 'dialogMsgNotInInternal',
			id: 'msgNotInInternal',
			eventType: 'NOT_IN_INTERNAL'
		},
		'LESS_PAYMENT': {
			dialogId: 'dialogMsgLessPayment',
			id: 'msgLessPayment',
			eventType: 'LESS_PAYMENT'
		},
		'INVOICE_IS_USED': {
			dialogId: 'dialogMsgInvoiceIsUsed',
			id: 'msgInvoiceIsUsed',
			eventType: 'INVOICE_IS_USED'
		},
		'NOT_FOUND': {
			dialogId: 'dialogMsgNotFound',
			id: 'msgNotFound',
			eventType: 'NOT_FOUND'
		},
		'TOO_MUCH_ERROR': {
			dialogId: 'dialogMsgTooMuchError',
			id: 'msgTooMuchError',
			eventType: 'TOO_MUCH_ERROR'
		}
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

		//參加活動
		var campaignId = $('.campaignId').val();
		console.info('campaignId', campaignId);
		
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

		// 錯誤次數上限
		var errorLimit = $('#errorLimit').val();
		console.info('errorLimit', errorLimit);

		// Timeout秒數
		var timeout = $('#timeout').val();
		console.info('timeout', timeout);
		
		$.BCS.isValidate  = true;
		var isValid = true;

		// 發送內容 設定
		var MsgFrameContents = [];
		
		for (var key in eventTypeMap) {
			var obj = eventTypeMap[key];
			var msgContents = $.BCS.getMsgFrameContent(obj.dialogId);
			if (msgContents.length == 0) {
				$('#' + obj.id).html('<span style="color:red">請輸入訊息</span>');
				isValid = false;
			};

			msgContents.forEach(function(msgContent, idx) {
				msgContent.eventType = obj.eventType;
				MsgFrameContents.push(msgContent);
			});
		};
		
		
		console.info('MsgFrameContents', MsgFrameContents);
		/**
		 * Validate Error
		 */
		if(!$.BCS.isValidate || !isValid){
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

		postData.interactiveType = "CAMPAIGN";
		
		postData.interactiveStatus = interactiveStatus;
		
		postData.userStatus = userStatus;
		postData.keywordInput = keywordInput;

		postData.sendMsgDetails = MsgFrameContents;
		postData.otherKeywords = otherKeywords;

		postData.campaignId = campaignId;
		postData.errorLimit = errorLimit;
		postData.timeout = timeout;

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
			var url = bcs.bcsContextPath +'/edit/campaignResponsePage';
			
			if(interactiveStatus == 'DISABLE'){
				url = bcs.bcsContextPath +'/edit/campaignResponseDisablePage';
			}
			else if(interactiveStatus == 'ACTIVE'){
				url = bcs.bcsContextPath +'/edit/campaignResponsePage';
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
		var url = bcs.bcsContextPath +'/edit/campaignResponseDisablePage';
		
		if(fromParam == 'campaignResponseDisablePage'){
			url = bcs.bcsContextPath +'/edit/campaignResponseDisablePage';
		}
		else if(fromParam == 'campaignResponsePage'){
			url = bcs.bcsContextPath +'/edit/campaignResponsePage';
		}
		
		window.location.replace(url);
	}
	
	$('.CancelBtn').click(cancelFunc);

	// 取消
	$('.btn_cancel').click(cancelFunc);
	
	// 取得設定資料
	var loadDataFunc = function(){

        $.ajax({
            type : "GET",
            async : false,
            url : bcs.bcsContextPath + '/admin/getCampaignList?isActive=true'
        }).success(function(response){
            console.info(response);

            $.each(response, function(i, o){

                var campaign = $('<option value=""></option>');

                campaign.val(o.campaignId);
                campaign.html(o.campaignName);

                $('.campaignId').append(campaign);
            });

            $('.campaignId').change(optionSelectChange_func);
            
        }).fail(function(response){
            console.info(response);
            $.FailResponse(response);
        }).done(function(){
        });

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

					$.each(valueObj, function(i , o){
						if (o.eventType) {
							var dialogId = eventTypeMap[o.eventType].dialogId;
							$.BCS.currentMsgDialogId = dialogId;
						}
						var msgType = o.msgType;
						$.BCS.createMsgFrame(msgType, o);
					});

					var msgCampaign = response.MsgCampaignMap['iMsgId-' + iMsgId];
					if(msgCampaign){
						$('.campaignId').val(msgCampaign.campaignId);
						$('.campaignId').change();

						$('#errorLimit').val(msgCampaign.errorLimit);

						$('#timeout').val(msgCampaign.timeout);
					}
				}

				/* 顯示已設定多少訊息 */
				for (var key in eventTypeMap) {
					var obj = eventTypeMap[key];
					setMsgDisplay(obj.eventType);
				};
				
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

		for (var key in eventTypeMap) {
			var obj = eventTypeMap[key];
			initDialog(obj.dialogId);
		};
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

	$('.btn_show_dialog').click(function() {
		var eventType = $(this).attr('alt-event-type')
		var dialogId = eventTypeMap[eventType].dialogId;

		$.BCS.currentMsgDialogId = dialogId;
		$('#' + dialogId).dialog('open');
	});

	$('.btn_draft').click(function() {
		var eventType = $(this).attr('alt-event-type')
		var dialogId = eventTypeMap[eventType].dialogId;
		var outputId = eventTypeMap[eventType].id;

		setMsgDisplay(eventType);

		if(!$.BCS.isValidate){
			return false;
		}

		$.BCS.currentMsgDialogId = undefined;

		$('#' + dialogId).dialog('close')
	});

    /**
     * 顯示已設定的訊息數量
     */
	var setMsgDisplay = function(eventType) {
		$.BCS.isValidate = true;

		var eventTypeObj = eventTypeMap[eventType];
		var dialogId = eventTypeObj.dialogId;

		var msgFrameContents = $.BCS.getMsgFrameContent(dialogId);
		console.log(msgFrameContents);

		if($.BCS.isValidate){
			var count = msgFrameContents.length;
			var msg = '';
			if (count > 0) {
				msg = '已設定' + count + '則訊息。';
			}
			$('#' + eventTypeObj.id).html(msg);
		}
	}

	var initDialog = function(id) {
		$('#' + id).dialog({
	    	autoOpen: false,
			resizable: false,
			modal: true,
			draggable: true,
	    	minWidth : 750,
	    	position: { my: "top", at: "top", of: window  }
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