/**
 * 
 */
$(function(){
	var campaignId = $.urlParam("campaignId");
	var actionType = $.urlParam("actionType") || 'Create';
	console.info('actionType', actionType);
	var from = $.urlParam("from") || 'disable';
	console.info('from', from);
	
	var dateFormat = "YYYY-MM-DD HH:mm:ss";
	
	// 表單驗證
	var validator = $('#formContentCampaign').validate({
		rules : {
			
			// 優惠券標題
			'campaignTitle' : {
				required : true,
				maxlength : 50
			},
			
			// 分享次數
			'shareTimes' : {
				required : true,
				maxlength : 5,
				digits:true
			},
			
			// 活動圖片
			'actionImageUpload' : {
				required : '#actionImgId:blank'
			},
			
			// 分享圖片
			'shareImageUpload' : {
				required : '#shareImgId:blank'
			},
			
			// 說明圖片
			'descriptionImageUpload' : {
				required : '#descriptionImgId:blank'
			},

			// 活動時間
			'campaignStartTime' : {
				required : true,
				dateYYYYMMDD : true
			},
			
			'campaignStartTimeHour' : {
				required : true
			},
			
			'campaignStartTimeMinute' : {
				required : true
			},
			
			'campaignEndTime' : {
				required : true,
				dateYYYYMMDD : true,
				compareDate : {
					compareType : 'after',
					dateFormat : dateFormat,
					getThisDateStringFunction : function() {
						var yearMonthDay = $('#campaignEndTime').val();
						var hour = $('#campaignEndTimeHour').val();
						var minute = $('#campaignEndTimeMinute').val();		
						return yearMonthDay + ' ' + hour + ':' + minute + ':00';
					},
					getAnotherDateStringFunction : function() {
						var yearMonthDay = $('#campaignStartTime').val();
						var hour = $('#campaignStartTimeHour').val();
						var minute = $('#campaignStartTimeMinute').val();		
						return yearMonthDay + ' ' + hour + ':' + minute + ':00';
					},
					thisDateName : '使用效期結束日期',
					anotherDateName : '使用效期開始日期'
				}
			},
			
			'campaignEndTimeHour' : {
				required : true
			},
			
			'campaignEndTimeMinute' : {
				required : true
			}
		}
	});
	
	// 綁訂計算字數函式到輸入框
	var bindCountTextFunctionToInput = function() {
		$('#campaignTitle').keyup(function() {
			var txtLength = $(this).val().length;
			var tr = $(this).closest("tr");
			var inputCount = tr.find(".MdTxtInputCount");
			var countText = inputCount.text();
			inputCount.text(countText.replace(/\d+\//, txtLength + '/'));
		});
		
		$("#campaignShareMsg").keyup(function(e) {
			var txtLength = $(this).val().length;
			var tr = $(this).closest("tr");
			var inputCount = tr.find(".floatRight");
			var countText = inputCount.text();
			inputCount.text(countText.replace(/\d+\//, txtLength + '/'));
		});

	};
	bindCountTextFunctionToInput();
	
	/**
	 * 從欄位取得日期(型態是 Moment.js 的 date wraps)
	 */
	var getMomentByElement = function(elementId) {
		var yearMonthDay = $('#' + elementId).val();
		var hour = $('#' + elementId + 'Hour').val();
		var minute = $('#' + elementId + 'Minute').val();		
		var momentDate = moment(yearMonthDay + ' ' + hour + ':' + minute + ':00', dateFormat);
		return momentDate;
	}
	
	/**
	 * 設定日期時間欄位值
	 */
	var setElementDate = function(elementId, timestamp) {
		if (!timestamp) {
			return;
		}
		
		var momentDate = moment(timestamp);
		$('#' + elementId).val(momentDate.format('YYYY-MM-DD'));
		
		var hour = momentDate.hour();
		$('#' + elementId + 'Hour').val(hour < 10 ? '0' + hour : hour).change();
		
		var minute = momentDate.minute();
		$('#' + elementId + 'Minute').val(minute < 10 ? '0' + minute : minute).change();		
	}
	
	// 下拉選項
	var optionSelectChange_func = function(){
		var selectValue = $(this).find('option:selected').text();
		$(this).closest('.option').find('.optionLabel').html(selectValue);
	};
	
	$('.optionSelect').change(optionSelectChange_func);

	// 日期元件
	$(".datepicker").datepicker({ 'minDate' : 0, 'dateFormat' : 'yy-mm-dd'});
	
	// 取消
	$('.btn_cancel').click(function(){

		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}

		if(from == 'disable'){
			window.location.replace(bcs.bcsContextPath + '/edit/shareCampaignListDisablePage');
		}
		else if(from == 'active'){
			window.location.replace(bcs.bcsContextPath + '/edit/shareCampaignListPage');
		}
		else if(from == 'api'){
			window.location.replace(bcs.bcsContextPath + '/edit/shareCampaignListApiPage');
		}
		else{
			window.location.replace(bcs.bcsContextPath + '/edit/shareCampaignListPage');
		}
	});
	
	var getDateFromUI = function(){

		if (!validator.form()) {
			return;
		}
		
		var campaignId = $.urlParam("campaignId");
		console.info('campaignId', campaignId);
		
		if(actionType == 'Copy'){
			campaignId = null;
		}
		
		// MGM標題
		var campaignTitle = $('#campaignTitle').val();

		// 分享次數
		var shareTimes = $('#shareTimes').val();
		
		// 活動圖片
		var actionImgId = $('#actionImgId').val();
		
		// 分享圖片
		var shareImgId = $('#shareImgId').val();
		
		// 說明圖片
		var descriptionImgId = $('#descriptionImgId').val();
		
		// 活動時間
		var momentCampaignStartTime = getMomentByElement('campaignStartTime');
		var momentCampaignEndTime = getMomentByElement('campaignEndTime');

		// 分享訊息
		var campaignShareMsg = $('#campaignShareMsg').val();

		var shareCampaign = {
				campaignId : campaignId,
				campaignName : campaignTitle,
				shareTimes : shareTimes,
				actionImgReferenceId : actionImgId,
				shareImgReferenceId : shareImgId,
				descriptionImgReferenceId : descriptionImgId,
				startTime : momentCampaignStartTime.format(dateFormat),
				endTime : momentCampaignEndTime.format(dateFormat),
				shareMsg : campaignShareMsg
			};
		return shareCampaign;
	};
	
	// 儲存按鍵
	$('.btn_save').click(function(){

		if(actionType == "Read"){
			location.reload();
			return;
		}
		
		if (!validator.form()) {
			return;
		}
		
		var postData = getDateFromUI();
		
		console.info('postData', postData);
		
		if (!confirm(actionType == 'Create' ? '請確認是否建立' : '請確認是否儲存')) {
			return false;
		}

		$('.LyMain').block($.BCS.blockMsgSave);
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/edit/saveShareCampaign',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			alert( '儲存成功');

			if(from == 'disable'){
				window.location.replace(bcs.bcsContextPath + '/edit/shareCampaignListDisablePage');
			}
			else if(from == 'active'){
				window.location.replace(bcs.bcsContextPath + '/edit/shareCampaignListPage');
			}
			else if(from == 'api'){
				window.location.replace(bcs.bcsContextPath + '/edit/shareCampaignListApiPage');
			}
			else{
				window.location.replace(bcs.bcsContextPath + '/edit/shareCampaignListPage');
			}
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	});
	
	var loadDataFunc = function(){

		if (campaignId) {
			
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/edit/getShareCampaign?campaignId=' + campaignId
			}).success(function(response){
				console.info(response);
				
				// 活動標題
				$('#campaignTitle').val(response.campaignName);

				// 分享次數
				$('#shareTimes').val(response.shareTimes);
				
				// 活動圖片
				$('#actionImgId').val(response.actionImgReferenceId);
				if(response.actionImgReferenceId){
					$('#actionImg').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.actionImgReferenceId);
				}
				
				// 分享圖片
				$('#shareImgId').val(response.shareImgReferenceId);
				if(response.shareImgReferenceId){
		            $('#shareImg').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.shareImgReferenceId);
				}
				
				// 說明圖片
				$('#descriptionImgId').val(response.descriptionImgReferenceId);
				if(response.descriptionImgReferenceId){
		            $('#descriptionImg').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.descriptionImgReferenceId);
				}

            	setElementDate('campaignStartTime', response.startTime);
            	setElementDate('campaignEndTime', response.endTime);

            	$('#campaignShareMsg').val(response.shareMsg);
            	
            	// 計算字數
            	$('#campaignTitle').keyup();
            	$('#campaignShareMsg').keyup();
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});

			if(actionType == "Read"){
				$('.btn_save').remove();
				$('#campaignTitle').attr('disabled',true);
				$('.MdBtnUpload').remove();
				$('#campaignStartTime').attr('disabled',true);
				$('#campaignStartTimeHour').attr('disabled',true);
				$('#campaignStartTimeMinute').attr('disabled',true);
				$('#campaignEndTime').attr('disabled',true);
				$('#campaignEndTimeHour').attr('disabled',true);
				$('#campaignEndTimeMinute').attr('disabled',true);
				$('#campaignShareMsg').attr('disabled',true);
			}
		}
	};

	var imgUploadFormat = {
			actionImageUpload : {rightWidth : 'eq750'},
			shareImageUpload : {rightWidth : 'eq750'},
			descriptionImageUpload : {rightWidth : 'eq750'}
	};
	
	// 上傳活動圖片
	$('#actionImageUpload').on("change", function(event){

		if(actionType == "Read"){
			location.reload();
			return;
		}
		
		if (!validator.element(this)) {
			return false;
		}
		
		var input = event.currentTarget;
		
    	if (input.files && input.files[0]) {
    		if(input.files[0].size < 1048576) {
	    		var fileName = input.files[0].name;
	    		console.info("fileName : " + fileName);
	    		var form_data = new FormData();
	    		
	    		var ajaxUrl = bcs.bcsContextPath + "/edit/createResource?resourceType=IMAGE";
	    		
	    		var thisImgUploadFormat = imgUploadFormat[$(input).attr('name')];
	    		if(thisImgUploadFormat){
	    			ajaxUrl = bcs.bcsContextPath + "/edit/createResource?resourceType=IMAGE&rightContentType=" + $(input).attr('accept');
	    			if(thisImgUploadFormat.rightWidth){
	    				ajaxUrl += "&rightWidth=" + thisImgUploadFormat.rightWidth;
	    			}
	    			if(thisImgUploadFormat.rightHeight){
	    				ajaxUrl += "&rightHeight=" + thisImgUploadFormat.rightHeight;
	    			}
	    			if(thisImgUploadFormat.rightSize){
	    				ajaxUrl += "&rightSize=" + thisImgUploadFormat.rightSize;
	    			}
	    		}
	    		
	    		form_data.append("filePart",input.files[0]);
	
	    		$('.LyMain').block($.BCS.blockMsgUpload);
	    		$.ajax({
	                type: 'POST',
	                url: ajaxUrl,
	                cache: false,
	                contentType: false,
	                processData: false,
	                data: form_data
	    		}).success(function(response){
	            	console.info(response);
	            	
	            	if(typeof response === 'string' && response.indexOf("WARING") == 0){
	            		alert(response);
	            		$(input).val('');
	            		return;
	            	}
	            	
	            	alert("上傳成功!");
	            	$('#actionImgId').val(response.resourceId);
	            	$('#actionImg').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
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
	
	// 上傳分享圖片
	$('#shareImageUpload').on("change", function(event){

		if(actionType == "Read"){
			location.reload();
			return;
		}
		
		if (!validator.element(this)) {
			return false;
		}
		
		var input = event.currentTarget;
		
    	if (input.files && input.files[0]) {
    		if(input.files[0].size < 1048576) {
	    		var fileName = input.files[0].name;
	    		console.info("fileName : " + fileName);
	    		var form_data = new FormData();
	    		
	    		var ajaxUrl = bcs.bcsContextPath + "/edit/createResource?resourceType=IMAGE";
	    		
	    		var thisImgUploadFormat = imgUploadFormat[$(input).attr('name')];
	    		if(thisImgUploadFormat){
	    			ajaxUrl = bcs.bcsContextPath + "/edit/createResource?resourceType=IMAGE&rightContentType=" + $(input).attr('accept');
	    			if(thisImgUploadFormat.rightWidth){
	    				ajaxUrl += "&rightWidth=" + thisImgUploadFormat.rightWidth;
	    			}
	    			if(thisImgUploadFormat.rightHeight){
	    				ajaxUrl += "&rightHeight=" + thisImgUploadFormat.rightHeight;
	    			}
	    			if(thisImgUploadFormat.rightSize){
	    				ajaxUrl += "&rightSize=" + thisImgUploadFormat.rightSize;
	    			}
	    		}
	    		
	    		form_data.append("filePart",input.files[0]);
	
	    		$('.LyMain').block($.BCS.blockMsgUpload);
	    		$.ajax({
	                type: 'POST',
	                url: ajaxUrl,
	                cache: false,
	                contentType: false,
	                processData: false,
	                data: form_data
	    		}).success(function(response){
	            	console.info(response);
	            	
	            	if(typeof response === 'string' && response.indexOf("WARING") == 0){
	            		alert(response);
	            		$(input).val('');
	            		return;
	            	}
	            	
	            	alert("上傳成功!");
	            	$('#shareImgId').val(response.resourceId);
	            	$('#shareImg').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
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
	
	// 上傳說明圖片
	$('#descriptionImageUpload').on("change", function(event){

		if(actionType == "Read"){
			location.reload();
			return;
		}
		
		if (!validator.element(this)) {
			return false;
		}
		
		var input = event.currentTarget;
		
    	if (input.files && input.files[0]) {
    		if(input.files[0].size < 1048576) {
	    		var fileName = input.files[0].name;
	    		console.info("fileName : " + fileName);
	    		var form_data = new FormData();
	    		
	    		var ajaxUrl = bcs.bcsContextPath + "/edit/createResource?resourceType=IMAGE";
	    		
	    		var thisImgUploadFormat = imgUploadFormat[$(input).attr('name')];
	    		if(thisImgUploadFormat){
	    			ajaxUrl = bcs.bcsContextPath + "/edit/createResource?resourceType=IMAGE&rightContentType=" + $(input).attr('accept');
	    			if(thisImgUploadFormat.rightWidth){
	    				ajaxUrl += "&rightWidth=" + thisImgUploadFormat.rightWidth;
	    			}
	    			if(thisImgUploadFormat.rightHeight){
	    				ajaxUrl += "&rightHeight=" + thisImgUploadFormat.rightHeight;
	    			}
	    			if(thisImgUploadFormat.rightSize){
	    				ajaxUrl += "&rightSize=" + thisImgUploadFormat.rightSize;
	    			}
	    		}
	    		
	    		form_data.append("filePart",input.files[0]);
	
	    		$('.LyMain').block($.BCS.blockMsgUpload);
	    		$.ajax({
	                type: 'POST',
	                url: ajaxUrl,
	                cache: false,
	                contentType: false,
	                processData: false,
	                data: form_data
	    		}).success(function(response){
	            	console.info(response);
	            	
	            	if(typeof response === 'string' && response.indexOf("WARING") == 0){
	            		alert(response);
	            		$(input).val('');
	            		return;
	            	}
	            	
	            	alert("上傳成功!");
	            	$('#descriptionImgId').val(response.resourceId);
	            	$('#descriptionImg').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
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

	loadDataFunc();
});