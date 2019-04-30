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

			// 發送群組
			'sendGroupSelect' : {
				required : {
			        param: true,
			        depends: function(element) {
						if(btnTarget == "SendMsg" || btnTarget == "QueryGroup"){
							return true;
						}
						return false;
			        }
				}
			},
			'sendTimeType' : {
				required : {
			        param: true,
			        depends: function(element) {
						if(btnTarget == "SendMsg"){
							return true;
						}
						return false;
			        }
				}
			}
		}
	});

	// 訊息類別
	var msgTagContentFlag = $.BCS.contentFlagComponent('msgTag', 'MSG_SEND', {
		placeholder : '請輸入類別'
	});
	
	/**
	 * SaveDraft
	 * SendMsg
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

		// 發送群組
		var sendGroupId = $('.sendGroupSelect').val();
		console.info('sendGroupId', sendGroupId);

		var serialId = $('.serialSetting').val();
		console.info('serialId', serialId);

		// 發送時間
		var sendingMsgType = $('[name="sendTimeType"]:checked').val();
		console.info('sendingMsgType', sendingMsgType);

		// 訊息類別
		var msgTagList = msgTagContentFlag.getContentFlagList();
		console.info('msgTagList', msgTagList);

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

		// 驗證輸入
		if(actionType == "SendMsg"){

			if(!sendingMsgType){
				alert('請設定發送時間');
				return;
			}
			else{
				if(sendingMsgType == "SCHEDULE" || sendingMsgType == "DELAY"){
					sendingMsgTime = getSendMsgTime(sendingMsgType, true);
					if(!sendingMsgTime){
						return;
					}
				}
			}
		}
		else{
			sendingMsgTime = getSendMsgTime(sendingMsgType, false);
		}

		if(!MsgFrameContents || MsgFrameContents.length < 1){
			alert('請設定發送內容');
			return;
		}
		else if(MsgFrameContents.length > 4){
			alert('發送內容不能超過4個');
			return;
		}

		/**
		 * Do Confirm Check
		 */
		var confirmStr = "請確認是否傳送";
		if(actionType  == "SaveDraft"){
			confirmStr = "請確認是否儲存";
		}
		var r = confirm(confirmStr);
		if (r) {
			// confirm true
		} else {
		    return;
		}

		// 設定傳送資料
		var postData = {};
		postData.actionType = actionType;

		postData.sendGroupId = sendGroupId;
		postData.serialId = serialId;
		postData.sendingMsgType = sendingMsgType;

		postData.sendMsgDetails = MsgFrameContents;

		postData.sendingMsgTime = sendingMsgTime;

		postData.msgTagList = msgTagList;

		var msgAction = postData.actionType;

		var msgId = $.urlParam("msgId");

		if($.BCS.actionTypeParam == "Edit"){
			postData.msgId = msgId;
		}
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
			if(postData.actionType == "SaveDraft"){
				alert('儲存成功');
				window.location.replace(bcs.bcsContextPath +'/edit/msgListPage');
			}
			else if(postData.actionType == "SendMsg"){
				if(sendingMsgType == "DELAY"){
					alert('預約發送成功');
				}
				else if(sendingMsgType == "SCHEDULE"){
					alert('排程發送成功');
				}
				else{
					alert('立即傳送成功');
				}
				window.location.replace(bcs.bcsContextPath +'/edit/msgListSendedPage');
			}
			else{
				alert('傳送成功');
			}
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};

	// 取得 傳送設定時間
	var getSendMsgTime = function(sendingMsgType, isShowAlert){
		var sendingMsgTime = null;

		if(sendingMsgType == "DELAY"){
			var datepickerVal = $('#delaySelect .datepicker').val();
			console.info('datepickerVal', datepickerVal);
			if(!datepickerVal){
				if(isShowAlert){
					alert('請設定發送時間 : 定時發送');
				}
				return null;
			}

			var selectHour = $('#delaySelect .selectHour').val();
			console.info('selectHour', selectHour);

			var selectMinuteOne = $('#delaySelect .selectMinuteOne').val();
			console.info('selectMinuteOne', selectMinuteOne);

			var selectMinuteTwo = $('#delaySelect .selectMinuteTwo').val();
			console.info('selectMinuteTwo', selectMinuteTwo);

			sendingMsgTime = datepickerVal + " " + selectHour + ":" + selectMinuteOne + selectMinuteTwo + ":00";
			console.info('sendingMsgTime', sendingMsgTime);
		}
		else if(sendingMsgType == "SCHEDULE"){

			var scheduleType = $('[name="schedule"]:checked').val();
			console.info('scheduleType', scheduleType);

			if(!scheduleType){
				if(isShowAlert){
					alert('請設定發送時間 : 排程發送');
				}
				return null;
			}

			sendingMsgTime = scheduleType;

			if('EveryMonth' == scheduleType){
				var selectMonth = $('#scheduleSelect .selectMonth').val();
				console.info('selectMonth', selectMonth);
				sendingMsgTime += ' ' + selectMonth;
			}

			if('EveryWeek' == scheduleType){
				var selectWeek = $('#scheduleSelect .selectWeek').val();
				console.info('selectWeek', selectWeek);
				sendingMsgTime += ' ' + selectWeek;
			}

			var selectHour = $('#scheduleSelect .selectHour').val();
			console.info('selectHour', selectHour);

			var selectMinuteOne = $('#scheduleSelect .selectMinuteOne').val();
			console.info('selectMinuteOne', selectMinuteOne);

			var selectMinuteTwo = $('#scheduleSelect .selectMinuteTwo').val();
			console.info('selectMinuteTwo', selectMinuteTwo);

			sendingMsgTime += " " + selectHour + ":" + selectMinuteOne + selectMinuteTwo + ":00";
			console.info('sendingMsgTime', sendingMsgTime);
		}

		return sendingMsgTime;
	};

	// 發送時間 設定
	$('[name="sendTimeType"]').click(function(){

		var sendingMsgType = $('[name="sendTimeType"]:checked').val();

		$('#formSendGroup').find('[name="sendDate"]').rules("remove");
		$('#formSendGroup').find('[name="schedule"]').rules("remove");
		$('#delaySelect').css('display', 'none');
		$('.schedule').css('display', 'none');

		if(sendingMsgType == "DELAY"){
			$('#formSendGroup').find('[name="sendDate"]').rules("add", {
				required : {
			        param: true,
			        depends: function(element) {
						if(btnTarget == "SendMsg"){
							return true;
						}
						return false;
			        }
				},
				dateYYYYMMDD : true
			});

			$('#delaySelect').css('display', '');
		}
		else if(sendingMsgType == "SCHEDULE"){
			$('#formSendGroup').find('[name="schedule"]').rules("add", {
				required : {
			        param: true,
			        depends: function(element) {
						if(btnTarget == "SendMsg"){
							return true;
						}
						return false;
			        }
				}
			});
			$('.schedule').css('display', '');
		}
	});

	// 預計發送人員數查詢
	$('.SendGroupQueryBtn').click(function(){

		var sendGroupId = $('.sendGroupSelect').val();
		console.info('sendGroupId', sendGroupId);

		btnTarget = "QueryGroup";
		if(!validator.form()){
			return;
		}

		if(!sendGroupId){
			alert('請設定發送群組');
			return;
		}

		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/market/getSendGroupQueryResult?groupId=' + sendGroupId
		}).success(function(response){
			console.info(response);
			alert('查詢結果共 ' + response + ' 筆');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
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
			case 'COUPON':
				$.BCS.previewMsgCoupon(previewDialog, detailContent);
				break;
			case 'REWARDCARD':
				$.BCS.previewMsgRewardCard(previewDialog, detailContent);
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

	// 儲存草稿
	$('.btn_draft').click(function(){
		sendingMsgFunc("SaveDraft");
	});

	// 傳送
	$('.btn_save').click(function(){
		sendingMsgFunc("SendMsg");
	});

	// 傳送
	$('.btn_add').click(function(){
		sendingMsgFunc("SendMsg");
	});

	// 儲存草稿
	$('.SaveDraftBtn').click(function(){
		sendingMsgFunc("SaveDraft");
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
		var url = bcs.bcsContextPath +'/edit/msgListPage';

		if(fromParam == 'msgListDraftPage'){
			url = bcs.bcsContextPath +'/edit/msgListDraftPage';
		}
		else if(fromParam == 'msgListDelayPage'){
			url = bcs.bcsContextPath +'/edit/msgListDelayPage';
		}
		else if(fromParam == 'msgListSendedPage'){
			url = bcs.bcsContextPath +'/edit/msgListSendedPage';
		}
		else if(fromParam == 'msgListSchedulePage'){
			url = bcs.bcsContextPath +'/edit/msgListSchedulePage';
		}
		window.location.replace(url);
	});

	// 排程發送 選擇
	$('[name="schedule"]').click(function(){

		var scheduleType = $('[name="schedule"]:checked').val();
		console.info('scheduleType', scheduleType);

		// 每月
		if('EveryMonth' == scheduleType ){
			$('.selectMonth').closest('.option').css('display', '');
			$('.selectWeek').closest('.option').css('display', 'none');
		}
		// 每週
		else if('EveryWeek' == scheduleType ){
			$('.selectMonth').closest('.option').css('display', 'none');
			$('.selectWeek').closest('.option').css('display', '');
		}
		// 每日
		else if('EveryDay' == scheduleType ){
			$('.selectMonth').closest('.option').css('display', 'none');
			$('.selectWeek').closest('.option').css('display', 'none');
		}

		$('#scheduleSelect .selectHour').closest('.option').css('display', '');
		$('#scheduleSelect .selectMinuteOne').closest('.option').css('display', '');
		$('#scheduleSelect .selectMinuteTwo').closest('.option').css('display', '');
	}) ;

	var optionSelectChange_func = function(){
		var selectValue = $(this).find('option:selected').text();
		$(this).closest('.option').find('.optionLabel').html(selectValue);
	};

	$.BCS.ResourceMap = {};

	// 取得設定資料
	var loadDataFunc = function(){

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
		
		var msgId = $.urlParam("msgId");
		var msgSendId = $.urlParam("msgSendId");
		
		/**
		 * Load Back Send Message Data
		 */
		if(msgId || msgSendId){

			var getDataUrl = bcs.bcsContextPath +'/edit/getSendMsg';
			if(msgSendId){
				getDataUrl =getDataUrl + '?msgSendId=' + msgSendId;
			}
			else{
				getDataUrl =getDataUrl + '?msgId=' + msgId;
			}

    		$('.LyMain').block($.BCS.blockMsgRead);
    		
			$.ajax({
				type : "GET",
				url : getDataUrl
			}).success(function(response){
				$('.dataTemplate').remove();
				
				// 回寫 發送群組
				for(key in response.SendGroup){

					var groupTitle = response.SendGroup[key];
					console.info('groupId', key);
					console.info('groupTitle', groupTitle);

					var sendGroup = $('<option value=""></option>');

					sendGroup.val(key);
					sendGroup.html(groupTitle);

					$('.sendGroupSelect').append(sendGroup);
				}

				$('.sendGroupSelect').change(optionSelectChange_func);

				$.BCS.ResourceMap = response.ResourceMap;

				// 回寫 資料
				for(key in response.MsgMain){

					var keyObj = JSON.parse(key);
					console.info('keyObj', keyObj);
					var valueObj = response.MsgMain[key];
					console.info('valueObj', valueObj);
					
					msgTagContentFlag.findContentFlagList(keyObj.msgId);
					
					$('.sendGroupSelect').val(keyObj.groupId);
					$('.sendGroupSelect').change();

					serialId = keyObj.serialId;
					$('.serialSetting').val(serialId);
					$('.serialSetting').change();

					// 回寫 發送時間
					$('[name="sendTimeType"][value="' + keyObj.sendType + '"]').prop("checked",true);
					$('[name="sendTimeType"][value="' + keyObj.sendType + '"]').click();

					// 回寫 發送時間
					if("DELAY" == keyObj.sendType){
						var scheduleTime = keyObj.scheduleTime;
						console.info('scheduleTime', scheduleTime);
						if(scheduleTime){
							var splits = scheduleTime.split(' ');
							console.info('splits', splits);

							$('#delaySelect .datepicker').val(splits[0]);

							if(splits[1]){
								$('#delaySelect .selectHour').val(splits[1].split(':')[0]);
							}
							$('#delaySelect .selectHour').change();

							if(splits[1]){
								$('#delaySelect .selectMinuteOne').val(splits[1].split(':')[1].substr(0,1));
							}
							$('#delaySelect .selectMinuteOne').change();

							if(splits[1]){
								$('#delaySelect .selectMinuteTwo').val(splits[1].split(':')[1].substr(1,2));
							}
							$('#delaySelect .selectMinuteTwo').change();
						}
					}
					// 回寫 發送時間
					else if("SCHEDULE" == keyObj.sendType){
						var scheduleTime = keyObj.scheduleTime;
						console.info('scheduleTime', scheduleTime);
						if(scheduleTime){
							var splits = scheduleTime.split(' ');
							console.info('splits', splits);
							$('[name="schedule"][value="' + splits[0] + '"]').prop("checked",true);
							$('[name="schedule"][value="' + splits[0] + '"]').click();

							var timeStr = splits[1];
							if('EveryMonth' == splits[0]){

								$('#scheduleSelect .selectMonth').val(splits[1]);
								$('#scheduleSelect .selectMonth').change();
								timeStr = splits[2];
							}
							else if('EveryWeek' == splits[0]){

								$('#scheduleSelect .selectWeek').val(splits[1]);
								$('#scheduleSelect .selectWeek').change();
								timeStr = splits[2];
							}

							$('#scheduleSelect .selectHour').val(timeStr.split(':')[0]);
							$('#scheduleSelect .selectHour').change();

							$('#scheduleSelect .selectMinuteOne').val(timeStr.split(':')[1].substr(0,1));
							$('#scheduleSelect .selectMinuteOne').change();

							$('#scheduleSelect .selectMinuteTwo').val(timeStr.split(':')[1].substr(1,2));
							$('#scheduleSelect .selectMinuteTwo').change();
						}
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
				$('.CHTtl').html('查看訊息');
				$('.SendGroupQueryBtn').remove();
				$('.SaveDraftBtn').remove();
				$('.SendToMeBtn').remove();
				$('.SendToTestBtn').remove();
				$('.btn_add').remove();
				$('.sendGroupSelect').attr('disabled',true);
				$('.serialSetting').attr('disabled',true);
				$('#delaySelect .datepicker').attr('disabled',true);
				$('.selectMonth').attr('disabled',true);
				$('.selectWeek').attr('disabled',true);
				$('.selectHour').attr('disabled',true);
				$('.selectMinuteOne').attr('disabled',true);
				$('.selectMinuteTwo').attr('disabled',true);
				$('[name="sendTimeType"]').attr('disabled',true);

				$('[name="schedule"]').attr('disabled',true);

				$('.TypeMsgSolid').remove();
				
				msgTagContentFlag.disabled();

				$('.btn_draft').remove();
				$('.btn_save').remove();
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

		// 建立訊息
		else{
			/**
			 * Get Send Group List
			 */
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/market/getSendGroupTitleList'
			}).success(function(response){
				console.info(response);

				$.each(response, function(i, o){

					var sendGroup = $('<option value=""></option>');

					sendGroup.val(i);
					sendGroup.html(o);

					$('.sendGroupSelect').append(sendGroup);
				});

				$('.sendGroupSelect').change(optionSelectChange_func);

			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
		}
	};

    $( ".datepicker" ).datepicker({ 'minDate' : 0, 'dateFormat' : 'yy-mm-dd'});
	$('.selectMonth').change(optionSelectChange_func);
	$('.selectWeek').change(optionSelectChange_func);
	$('.selectHour').change(optionSelectChange_func);
	$('.serialSetting').change(optionSelectChange_func);
	$('.selectMinuteOne').change(optionSelectChange_func);
	$('.selectMinuteTwo').change(optionSelectChange_func);

	loadDataFunc();
});
