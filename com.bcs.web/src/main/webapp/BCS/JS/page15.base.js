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
		var MsgFrameContents = $.BCS.getMsgFrameContent();
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
		
		// 生效時間
		var interactiveTimeType = $('[name="sendTimeType"]:checked').val();
		console.info('interactiveTimeType', interactiveTimeType);
		
		var interactiveStartTime = "";
		var interactiveEndTime = "";
		if(interactiveTimeType == "TYPE_DAY"){
			interactiveStartTime = $('.typeDaySetting .startHour').val() + ":" + $('.typeDaySetting .startMinute').val();
			interactiveEndTime = $('.typeDaySetting .endHour').val() + ":" + $('.typeDaySetting .endMinute').val();
		}
		else if(interactiveTimeType == "TYPE_RANGE"){
			interactiveStartTime = $('#startTime').val() + " " + $('.typeRangeSetting .startHour').val() + ":" + $('.typeRangeSetting .startMinute').val();
			interactiveEndTime = $('#endTime').val() + " " + $('.typeRangeSetting .endHour').val() + ":" + $('.typeRangeSetting .endMinute').val();
		}
		
		// 使用者狀態
		var userStatus = $('.userStatus').val();
		console.info('userStatus', userStatus);
		
		// 關鍵字
		var keywordInput = $('#keywordInput').val();
		console.info('keywordInput', keywordInput);

		// 設定其他條件
		var otherRole = $('.otherRole').val();
		console.info('otherRole', otherRole);

		var serialId = $('.serialSetting').val();
		console.info('serialId', serialId);

		// 設定順位
		var interactiveIndex = $('.interactiveIndex').val();
		console.info('interactiveIndex', interactiveIndex);
		
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
		var MsgFrameContents = $.BCS.getMsgFrameContent();
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

		postData.interactiveType = "KEYWORD";
		
		postData.interactiveStatus = interactiveStatus;
		
		postData.userStatus = userStatus;
		postData.keywordInput = keywordInput;
		
		postData.otherRole = otherRole;
		postData.serialId = serialId;
		postData.interactiveIndex = interactiveIndex;

		postData.interactiveTimeType = interactiveTimeType;
		postData.interactiveStartTime = interactiveStartTime;
		postData.interactiveEndTime = interactiveEndTime;

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
			var url = bcs.bcsContextPath +'/edit/keywordResponsePage';
			
			if(interactiveStatus == 'DISABLE'){
				url = bcs.bcsContextPath +'/edit/keywordResponseDisablePage';
			}
			else if(interactiveStatus == 'ACTIVE'){
				url = bcs.bcsContextPath +'/edit/keywordResponsePage';
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
	
	// 生效時間 設定
	$('[name="sendTimeType"]').click(function(){

		var interactiveTimeType = $('[name="sendTimeType"]:checked').val();

		$('.typeDaySetting').css('display', 'none');
		$('.typeRangeSetting').css('display', 'none');

		if(interactiveTimeType == "TYPE_RANGE"){
			$('.typeRangeSetting').css('display', '');
		}
		else if(interactiveTimeType == "TYPE_DAY"){
			$('.typeDaySetting').css('display', '');
		}
	});

	// 預覽
	$('.SendPreviewBtn').click(function(event) {

		// 取得要預覽的各類訊息
		var MsgFrameContents = $.BCS.getMsgFrameContent();
		console.log(MsgFrameContents);

		if (MsgFrameContents.length == 0) {
			return false;
		}
		
		var checkOK = true;
		
		// 對話框
		var previewDialog = $('#previewDialog');

		// 依各類訊息呼叫對應的 preview 函式，將訊息放入對話框
		$.each(MsgFrameContents, function(index, value) {
			var detailContent = JSON.parse(value.detailContent);

			switch (value.detailType) {
			case 'TEXT': // 文字
				$.BCS.previewMsgText(previewDialog, detailContent);
				break;
			case 'STICKER': // 貼圖
				$.BCS.previewMsgSticker(previewDialog, detailContent);
				break;
			case 'IMAGE': // 照片
				$.BCS.previewMsgImage(previewDialog, detailContent);
				break;
			case 'RICH_MSG': // 圖文訊息
				var content = $('<div style="margin-bottom: 10px;"></div>');
				previewDialog.append(content);
				var richId = detailContent.richId;

				$.ajax({
					type: 'GET',
					url: bcs.bcsContextPath + "/edit/getRichMsg/" + richId,
				}).success(function(response){
					var valueObj = response[richId];
					console.info('valueObj', valueObj);
					$.BCS.previewRichMsgImage(content, valueObj);
				}).fail(function(response){
	    			console.info(response);
	    			$.FailResponse(response);
	    		}).done(function(){
	    		});
				break;
			case 'LINK': // 連結訊息
				if (!detailContent.linkUriParams.lastIndexOf('http://', 0)==0 
						&& !detailContent.linkUriParams.lastIndexOf('https://', 0)==0
						&& !detailContent.linkUriParams.lastIndexOf('BcsPage:', 0)==0) {
					alert("URL必須包含http或是BcsPage字樣！");
					checkOK = false;
					return false;
				}
				
				$.BCS.previewMsgLink(previewDialog, detailContent);
				break;
			case 'BCS_PAGE': // BCS卡友頁面
				$.BCS.previewMsgLink(previewDialog, {
					"textParams" : "卡友頁面",
					"linkUriParams" : "BcsPage:UserPage"
				});
				break;
			case 'TEMPLATE':
				var templateId = detailContent.templateId;
				$.ajax({
					type: 'GET',
					url: bcs.bcsContextPath + "/edit/getTemplateMsg/" + templateId,
				}).success(function(response){
					console.info('response', response);
					$.BCS.previewMsgTemplate(previewDialog, response);
				}).fail(function(response){
	    			console.info(response);
	    			$.FailResponse(response);
	    		}).done(function(){
	    		});
				break;
			default:
				break;
			}
		});

		// 檢核失敗就清空對話框內容並返回
		if (!checkOK) {
			previewDialog.empty();
			return false;
		}
		
		// 取消對話框自動 focus 內容的功能
		$.ui.dialog.prototype._focusTabbable = function(){};
		
		// 初始化對話框
		$.BCS.newPreviewDialog("預覽畫面", previewDialog, {
			close : function(event, ui) {
				previewDialog.empty();
			}
		});
		
		// 開啟對話框
		previewDialog.dialog('open');
	});
	
	// 設定
	$('.btn_save').click(function(){
		saveSettingFunc("SaveSetting");
	});
	
	// 設定
	$('.btn_add').click(function(){
		saveSettingFunc("SaveSetting");
	});
	
	// 傳送給我
	$('.SendToMeBtn').click(function(){
		sendingMsgFunc("SendToMe");
	});
	
	// 傳送測試群組
	$('.SendToTestBtn').click(function(){
		sendingMsgFunc("SendToTestGroup");
	});

	// 取消
	$('.btn_cancel').click(function(){

		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		var fromParam = $.urlParam("from");
		var url = bcs.bcsContextPath +'/edit/keywordResponseDisablePage';
		
		if(fromParam == 'keywordResponseDisablePage'){
			url = bcs.bcsContextPath +'/edit/keywordResponseDisablePage';
		}
		else if(fromParam == 'keywordResponsePage'){
			url = bcs.bcsContextPath +'/edit/keywordResponsePage';
		}
		
		window.location.replace(url);
	});
	
	// 取得設定資料
	var loadDataFunc = function(){

		var otherRole = "";

		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/market/getSendGroupList'
		}).success(function(response){
			console.info(response);
	
			$.each(response, function(i, o){
				
				var option = $('<option value="GROUPID' + o.groupId + '">' + o.groupTitle + '</option>');
				
				$('.otherRole').append(option);
			});

			$('.otherRole').val(otherRole);
			$('.otherRole').change();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});

		var serialId = "";

		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getSerialSettingList'
		}).success(function(response){
			console.info(response);

			var SerialSettingList = response.SerialSettingList;
			$.each(SerialSettingList, function(i, o){
				
				var option = $('<option value="' + o.serialId + '">' + o.serialTitle + '</option>');
				
				$('.serialSetting').append(option);
			});

			$('.serialSetting').val(serialId);
			$('.serialSetting').change();
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

					otherRole = keyObj.otherRole;
					$('.otherRole').val(otherRole);
					$('.otherRole').change();

					serialId = keyObj.serialId;
					$('.serialSetting').val(serialId);
					$('.serialSetting').change();

					$('.interactiveIndex').val(keyObj.interactiveIndex);
					$('.interactiveIndex').change();

					// 回寫 生效時間
					$('[name="sendTimeType"][value="' + keyObj.interactiveTimeType + '"]').prop("checked",true);
					$('[name="sendTimeType"][value="' + keyObj.interactiveTimeType + '"]').click();
					
					if(keyObj.interactiveTimeType == "TYPE_DAY"){
						var startTime = moment(keyObj.interactiveStartTime);
						var startHour = startTime.hour();
						var startMinute = startTime.minute();
						$('.typeDaySetting .startHour').val(startHour);
						$('.typeDaySetting .startHour').change();
						
						$('.typeDaySetting .startMinute').val(startMinute);
						$('.typeDaySetting .startMinute').change();
						
						var endTime = moment(keyObj.interactiveEndTime);
						var endHour = endTime.hour();
						var endMinute = endTime.minute();
						$('.typeDaySetting .endHour').val(endHour);
						$('.typeDaySetting .endHour').change();
						
						$('.typeDaySetting .endMinute').val(endMinute);
						$('.typeDaySetting .endMinute').change();
					}
					else if(keyObj.interactiveTimeType == "TYPE_RANGE"){
						var startTime = moment(keyObj.interactiveStartTime);
						var startDay = startTime.format("YYYY-MM-DD");
						$('#startTime').val(startDay);
						
						var startHour = startTime.hour();
						var startMinute = startTime.minute();
						$('.typeRangeSetting .startHour').val(startHour);
						$('.typeRangeSetting .startHour').change();
						
						$('.typeRangeSetting .startMinute').val(startMinute);
						$('.typeRangeSetting .startMinute').change();
						
						var endTime = moment(keyObj.interactiveEndTime);
						var endDay = endTime.format("YYYY-MM-DD");
						$('#endTime').val(endDay);
						
						var endHour = endTime.hour();
						var endMinute = endTime.minute();
						$('.typeRangeSetting .endHour').val(endHour);
						$('.typeRangeSetting .endHour').change();
						
						$('.typeRangeSetting .endMinute').val(endMinute);
						$('.typeRangeSetting .endMinute').change();
					}
					
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
						$.BCS.createMsgFrame(msgType, o);
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

    $( ".datepicker" ).datepicker({  'dateFormat' : 'yy-mm-dd'});

	$('.startHour').change(optionSelectChange_func);
	$('.startMinute').change(optionSelectChange_func);
	$('.endHour').change(optionSelectChange_func);
	$('.endMinute').change(optionSelectChange_func);
	
	$('.userStatus').change(optionSelectChange_func);
	$('.otherRole').change(optionSelectChange_func);
	$('.serialSetting').change(optionSelectChange_func);
	$('.interactiveIndex').change(optionSelectChange_func);

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