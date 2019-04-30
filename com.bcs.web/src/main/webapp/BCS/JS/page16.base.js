/**
 * 
 */
$(function(){
	var actionType = $.urlParam("actionType") || 'Create';
	console.info('actionType', actionType);
	var dateFormat = "YYYY-MM-DD HH:mm:ss";
	
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
	var setElementDate = function(timestamp) {
		if (!timestamp) {
			return;
		}
		
		var momentDate = moment(timestamp);
		$('#pushDate').val(momentDate.format('YYYY-MM-DD'));
		
		var hour = momentDate.hour();
		$('#pushTimeHour').val(hour < 10 ? '0' + hour : hour).change();
		
		var minute = momentDate.minute();
		$('#pushTimeMinute').val(minute < 10 ? '0' + minute : minute).change();		
	}
	
	// 下拉選項
	$('.optionSelect').change(function() {
		var selectValue = $(this).find('option:selected').text();
		$(this).closest('.option').find('.optionLabel').html(selectValue);
	});
		
	
	//設定文字的input事件
	var inputTxtKeyupEvent = function() {
		$(".txtInputCount").keyup(function(e) {
			var txtLength = $(this).val().length;
			var txtInputCountTd = $(this).closest("td");
			var maxLength = $(this).attr("maxlength")
			txtInputCountTd.find(".MdTxtNotice01").html(txtLength + "/" + maxLength);
		});
		$(".txtInputCount").trigger("keyup");
	}
	inputTxtKeyupEvent();
	
	//設定數字的input事件
	var inputNumberKeydownEvent = function() {
		//只能輸入數字
		$("input[name='numberInput']").keydown(function(e) {
			// Allow: backspace, delete, tab, escape, enter and .
	        if ($.inArray(e.keyCode, [46, 8, 9, 27, 13, 110, 190]) !== -1 ||
	             // Allow: Ctrl+A, Command+A
	            (e.keyCode == 65 && ( e.ctrlKey === true || e.metaKey === true ) ) || 
	             // Allow: home, end, left, right, down, up
	            (e.keyCode >= 35 && e.keyCode <= 40)) {
	                 // let it happen, don't do anything
	                 return;
	        }
	        // Ensure that it is a number and stop the keypress
	        if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57)) && (e.keyCode < 96 || e.keyCode > 105)) {
	            e.preventDefault();
	        }
		});
	}
	inputNumberKeydownEvent();
	
	var calTTLClick = function() {
		var newTotal = Number($('.clickNumber').val());
		if (reportIsRichMsg) {
			var differ = Number($('.clickNumber').val()) - Number(pastReportData.reportPushUrlClick);
			newTotal = Number(pastReportData.reportPushTotalClick) + differ;
		}
		
		$('#totalClicks').html(newTotal);
	}
	var calCtrValue = function() {
		var totalClicks = Number($('#totalClicks').html());
		var pushNumber = Number($('#pushNumber').val());
		
		if (totalClicks > 0 && pushNumber == 0) {
			$('#ctrValue').html("無限");
		} else if ((totalClicks == 0 && pushNumber == 0)) {
			$('#ctrValue').html("0%");
		} else {
			var ctrValue = Math.round(totalClicks / pushNumber * 10000) / 100; //小數點第二位
			$('#ctrValue').html(ctrValue + "%");
		}
	}
	var calUrlDeep = function() {
		var urlProductViews = Number($('#urlProductViews').val());
		var urlVisits = Number($('#urlVisits').val());
		if (urlProductViews == 0 && urlVisits == 0) {
			$('#urlDeep').html(0);
		} else {
			var urlDeep = Math.round(urlProductViews / urlVisits * 100) / 100;
			$('#urlDeep').html(urlDeep);
		}
	}
	var calTTLClickThroughs = function() {
		var newTotal = Number($('.clickThroughs').val());
		if (reportIsRichMsg) {
			var differ = Number($('.clickThroughs').val()) - Number(pastReportData.reportPushUrlClickThrough);
			newTotal = Number(pastReportData.reportPushTotalClickThrough) + differ;
		}
		
		$('#totalClickThroughs').html(newTotal);
	}
	var calTTLVisits = function() {
		var newTotal = Number($('.visits').val());
		if (reportIsRichMsg) {
			var differ = Number($('.visits').val()) - Number(pastReportData.reportPushUrlVisit);
			newTotal = Number(pastReportData.reportPushTotalVisit) + differ;
		}
		
		$('#totalVisits').html(newTotal);
	}
	var calTTLProductViews = function() {
		var newTotal = Number($('.productViews').val());
		if (reportIsRichMsg) {
			var differ = Number($('.productViews').val()) - Number(pastReportData.reportPushUrlProductView);
			var newTotal = Number(pastReportData.reportPushTotalProductView) + differ;
		}
		
		$('#totalProductViews').html(newTotal);
	}
	var calTTLDeep = function() {
		var totalProductViews = Number($('#totalProductViews').html());
		var totalVisits = Number($('#totalVisits').html());
		if (totalProductViews == 0 && totalVisits == 0) {
			$('#totalDeep').html(0);
		} else {
			var totalDeep = Math.round(totalProductViews / totalVisits * 100) / 100;
			$('#totalDeep').html(totalDeep);
		}
	}
	
	$('#pushNumber').keyup(function() {
		calCtrValue();
	});
	$('.clickNumber').keyup(function() {
		calTTLClick();
		calCtrValue();
	});
	$('.clickThroughs').keyup(function() {
		calTTLClickThroughs();
	});
	$('.visits').keyup(function() {
		calTTLVisits();
	});
	$('.productViews').keyup(function() {
		calTTLProductViews();
	});
	$('.deep').keyup(function() {
		calUrlDeep();
		calTTLDeep();
	});
	
	// 日期元件
	$(".datepicker").datepicker({ 'minDate' : -30, 'maxDate' : 0, 'dateFormat' : 'yy-mm-dd'});
	
	// 上傳圖片
	$('#pushImageInput').on("change", function(event){
		var input = event.currentTarget;
		
    	if (input.files && input.files[0]) {
    		if(input.files[0].size < 1048576) {
	    		var fileName = input.files[0].name;
	    		console.info("fileName : " + fileName);
	    		var form_data = new FormData();
	    		
	    		form_data.append("filePart",input.files[0]);
	    		
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
	            	$('#pushImageId').val(response.resourceId);
	            	$('#pushImage').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
	    		}).fail(function(response){
	    			console.info(response);
	    		}).done(function(){
	    		});
    		} else {
    			alert("圖片大小不可大於 1MB！");
    		}
        } 
	});
	
	// 儲存按鍵
	$('.btn_save').click(function(){
		var reportId = $.urlParam("reportId");
		console.info('reportId', reportId);
		
		if(actionType == 'Create'){
			reportId = null;
		}
		
		var pushTime = $('#pushDate').val() + " " + $('#pushTimeHour').val() + ":" + $('#pushTimeMinute').val() + ":00";
		var reportPushType = $('#reportPushType').val();
		if (!reportId && !reportIsRichMsg) { //Create時，才須檢查這幾個欄位
			//發送日期
			if (!moment(pushTime, dateFormat).isValid()) {
				alert('請輸入完整的發送日期');
				return false;
			}
			
			// 主要圖片
			richId = $('#richId').val();
			if (richId != null && richId.trim().length != 0) { //有設定richMsgId
				var pushImageId = $('#pushImageId').val();
				if(!pushImageId){
					alert('請上傳主要圖片');
					return false;
				}
			}
		}
		
		var validate = true;
		$.each($("input[name='numberInput']"), function(i, v) {
			if ($(this).val() == null || $(this).val() == "") {
				validate = false;
				return false; //break
			}
		});
		$.each($('.txtInputCount'), function(i, v) {
			if (($(this).val() == null || $(this).val() == "") && !$(this).attr("notValidate")) {
				validate = false;
				return false; //break
			}
		});
		if (!validate) {
			alert("資料輸入不全，請確認！");
			return;
		}
		
		var postData = {
			reportId : reportId,
			reportRichId : richId,
			reportMsgSendId : reportMsgSendId,
			reportPushTime : pushTime,
			reportPushType : $('#reportPushType').val(),
			reportImageId : $('#pushImageId').val(),
			reportPushTxt : $('#pushTxt').val(),
			reportPushNumber : Number($('#pushNumber').val()),
			reportPushUrlClick : Number($('#urlClicks').val()),
			reportPushTotalClick : Number($('#totalClicks').html()),
			reportPushCtrValue : Number($('#ctrValue').html().replace("%", "")),
			reportPushUrl : $('#pushUrl').val(),
			reportPushTrackingCode : $('#urlTrackingCode').val(),
			reportPushUrlClickThrough : Number($('#urlClickThroughs').val()),
			reportPushUrlVisit : Number($('#urlVisits').val()),
			reportPushUrlProductView : Number($('#urlProductViews').val()),
			reportPushUrlDeep : Number($('#urlDeep').html()),
			reportPushTotalClickThrough : Number($('#totalClickThroughs').html()),
			reportPushTotalVisit : Number($('#totalVisits').html()),
			reportPushTotalProductView : Number($('#totalProductViews').html()),
			reportPushTotalDeep : Number($('#totalDeep').html())
		};
		
		console.info('postData', postData);
		
		if (!confirm(actionType == 'Create' ? '請確認是否建立' : '請確認是否儲存')) {
			return false;
		}
		
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/edit/savePushReport?actionType='+ actionType,
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			alert('儲存成功');
	 		window.location.replace(bcs.bcsContextPath + '/admin/reportPage');
		}).fail(function(response){
			console.info(response);
			alert('錯誤發生 請找開發人員協助');
		}).done(function(){
		});
	});
	
	$('.btn_cancel').click(function() {
		if (!confirm("請確認是否取消？")) {
			return false;
		}
		window.location.replace(bcs.bcsContextPath + '/admin/reportPage');
	});
	
	// 將後端回傳的資料至入前端
	var loadReportData = function(response) {
		pastReportData = response;
		
		setElementDate(response.reportPushTime);
		
		$('#pushDate').prop('disabled', true);
		$('#pushTimeHour').prop('disabled', true);
		$('#pushTimeMinute').prop('disabled', true);
		$('#reportPushType').prop('disabled', true);
		$('#urlClicks').prop('disabled', true);
		$('#pushNumber').prop('disabled', true);
		$('#pushDate').closest('div').css({'background-color': '#eee'});
		$('#pushTimeHour').closest('div').css({'background-color': '#eee'});
		$('#pushTimeMinute').closest('div').css({'background-color': '#eee'});
		$('#reportPushType').closest('div').css({'background-color': '#eee'});
		$('#pushNumber').closest('div').css({'background-color': '#eee'});
		$('#urlClicks').closest('div').css({'background-color': '#eee'});
		
		$('#reportPushType').val(response.reportPushType);
		$('#pushTxt').val(response.reportPushTxt);
    	$('#pushNumber').val(response.reportPushNumber);
    	
    	$('#urlClicks').val(response.reportPushUrlClick);
    	$('#totalClicks').html(response.reportPushTotalClick);
    	$('#ctrValue').html(response.reportPushCtrValue);
    	
    	$('#pushUrl').val(response.reportPushUrl);
    	$('#urlTrackingCode').val(response.reportPushTrackingCode);
    	
    	$('#urlClickThroughs').val(response.reportPushUrlClickThrough);
    	$('#totalClickThroughs').html(response.reportPushTotalClickThrough);
    	
    	$('#urlVisits').val(response.reportPushUrlVisit);
    	$('#totalVisits').html(response.reportPushTotalVisit);
    	
    	$('#urlProductViews').val(response.reportPushUrlProductView);
    	$('#totalProductViews').html(response.reportPushTotalProductView);
    	
    	$('#urlDeep').html(response.reportPushUrlDeep);
    	$('#totalDeep').html(response.reportPushTotalDeep);
    	
    	$(".txtInputCount").trigger("keyup");
    	$("input[name='numberInput']").trigger("keyup");
	}
	
	// 清除已填入的資料
	var cleanLoadData = function() {
		reportIsRichMsg = false;
		
		$('.MdBtnUpload').show(); //上傳按鈕
		$('.MdFRM03File .MdTxtNotice01').show(); //圖片提醒註解
		
		$('#pushDate').prop('disabled', false);
		$('#pushTimeHour').prop('disabled', false);
		$('#pushTimeMinute').prop('disabled', false);
		$('#reportPushType').prop('disabled', false);
		$('#pushNumber').prop('disabled', false);
		$('#pushDate').closest('div').css({'background-color': 'white'});
		$('#pushTimeHour').closest('div').css({'background-color': 'white'});
		$('#pushTimeMinute').closest('div').css({'background-color': 'white'});
		$('#reportPushType').closest('div').css({'background-color': 'white'});
		$('#pushNumber').closest('div').css({'background-color': 'white'});

		$('#pushDate').val("");
		$('#pushTimeHour').val("00").change();
		$("#pushTimeMinute").val("00").change();
		$('#reportPushType').val("");
		
		$(".txtInputCount").val("");
		$("input[name='numberInput']").val("");
		$("#pushUrl").val("http://");
		
		$('#pushImageId').val("");
    	$('#pushImage').attr("src", "");
		
		$(".txtInputCount").trigger("keyup");
    	$("input[name='numberInput']").trigger("keyup");
	}
	
	$('#richId').blur(function() {
		richId = $('#richId').val();
		
		cleanLoadData();
		
		if (richId != null && richId.trim().length != 0) {
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/edit/getPushReportForRichMsg?richId=' + richId
			}).success(function(response){
				console.info(response);
				
				if (response != null && response != "") {
					reportIsRichMsg = true;
					
					loadReportData(response);
					
					$('#pushImageId').val(response.reportImageId);
	            	$('#pushImage').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.reportImageId);
					$('.MdBtnUpload').hide(); //上傳按鈕
					$('.MdFRM03File .MdTxtNotice01').hide(); //圖片提醒註解
					$('#urlClicks').prop('disabled', false);
					$('#urlClicks').closest('div').css({'background-color': 'white'});
				}
			}).fail(function(response){
				console.info(response);
			}).done(function(){
			});
		}
	});
	
	var richId = null;
	var pastReportData; //記錄舊報告的資訊
	var reportIsRichMsg = false; //是否為圖文訊息成效報告
	var reportMsgSendId = null;
	var loadDataFunc = function(){
		var reportId = $.urlParam("reportId");
		var richId = $.urlParam("richId");
		reportMsgSendId = $.urlParam("reportMsgSendId");
		
		if (reportId) {
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/edit/getPushReport?reportId=' + reportId + '&actionType=Edit'
			}).success(function(response){
				console.info(response);
				
				reportMsgSendId = response.reportMsgSendId;
				
				richId = response.reportRichId;
				$('#richId').val(richId);
				if (richId != null && richId.trim().length != 0) {
					reportIsRichMsg = true;
					$('#richId').prop('disabled', true);
					$('#richId').closest('div').css({'background-color': '#eee'});
					
					$('#pushImageId').val(response.reportImageId);
	            	$('#pushImage').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.reportImageId);
					$('.MdBtnUpload').hide(); //上傳按鈕
					$('.MdFRM03File .MdTxtNotice01').hide(); //圖片提醒註解
				} else {
					$('#richId').closest('tr').hide();
					$('.MdFRM03File').closest('tr').hide();
				}
				
				loadReportData(response);
			}).fail(function(response){
				console.info(response);
			}).done(function(){
			});
		}
		else if(richId && reportMsgSendId){
			$('#richId').val(richId);
			$('#richId').blur();
		}
		else {
			$(".txtInputCount").trigger("keyup");
			$("input[name='numberInput']").trigger("keyup");
		}
	};
	
	loadDataFunc();
});