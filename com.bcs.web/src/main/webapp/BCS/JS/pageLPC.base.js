/**
 *
 */
$(function(){

	console.info("bcs.user.role = ", bcs.user.role);

	if (bcs.user.role == 'ROLE_REPORT') {
		window.location.replace(bcs.bcsContextPath +'/index');
		
		alert('很抱歉，您的帳號無權限登入此頁面。');
		
		return;
	}
	
	var LyMain = document.getElementById("LyMain");
	LyMain.style.display = 'block';
	
	//--------- Initialize --------- 
	//假如重整 就把已經填好的值放回去
	if($.urlParam("title")){
		$('#title').val(decodeURI($.urlParam("title")));
	}
	if($.urlParam("pccCode")){
		$('#pccCode').val($.urlParam("pccCode"));
	}
	if($.urlParam("serialId")){
		$('#serialId').val($.urlParam("serialId"));
	}
	if($.urlParam("sendTimeType")){
		console.info("sendTimeType = ", $.urlParam("sendTimeType"))
		
		$('[name="sendTimeType"][value="' + $.urlParam("sendTimeType") + '"]').prop("checked",true);
		$('[name="sendTimeType"][value="' + $.urlParam("sendTimeType") + '"]').click();
		
		if($.urlParam("sendTimeType") == 'DELAY'){
			$('#delaySelect .datepicker').val($.urlParam("datepicker"));
			//$('#delaySelect .selectHour').val($.urlParam("selectHour"));
			$('#delaySelect .selectHour').closest('.option').find('.optionLabel').html($.urlParam("selectHour"));
			//$('#delaySelect .selectMinuteOne').val($.urlParam("selectMinuteOne"));
			$('#delaySelect .selectMinuteOne').closest('.option').find('.optionLabel').html($.urlParam("selectMinuteOne"));
			//$('#delaySelect .selectMinuteTwo').val($.urlParam("selectMinuteTwo"));
			$('#delaySelect .selectMinuteTwo').closest('.option').find('.optionLabel').html($.urlParam("selectMinuteTwo"));
		}
		
	}
	
	if($.urlParam("sendAmountType")){
		$('[name="sendAmountType"][value="' + $.urlParam("sendAmountType") + '"]').click();
		if($.urlParam("sendAmountType") == 'UNIVERSAL'){
			$('#amount').val($.urlParam("amount"));
		}
	}
	// ---- Global Variables ----
	// BCS Parameters
	$.BCS.actionTypeParam = $.urlParam("actionType");
	var btnTarget = ""; // Last Target Button

	// Send Group Parameters
	var sendGroupCondition = null; // Send Group Default Conditions
	var templateBody = {};		   // Send Group Row (clone of dataTemplate)
    
	// LinePointMain
	var linePointMainId = null;
	var sendGroupId = null;
	var draftMsgId = null;
	var totalCount = 0;
	var totalAmount = 0;
	var outCount = 0, outTotalAmount = 0;
	var TrimmedCount = 0, TrimmedTotalAmount = 0;
	// LinePointDetails
    var uids = [], custIds = [], pts = [];
    var outUids = [], outCustIds = [], outPts = [];
    var colMaxNum = 2;
    var caveatLinePoint = 0 ;
    // ---- Send Group ----
	// SendGroup:[匯出MID] Button
	$('.download_mid').click(function(){
		var postData = getSendGroupDetailFunc();
		console.info('postData', postData);
		if(!postData){
			return;
		}
		
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/market/createSendGroupMidExcelTemp',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info('createSendGroupMidExcelTemp response:', response);
			if(response.count > 0){
				var url =  bcs.bcsContextPath + '/market/exportToExcelForSendGroup?tempId=' + response.tempId;
				var downloadReport = $('#downloadReport');
				downloadReport.attr("src", url);
			}else{
				alert('查詢結果共 ' + response.count + ' 筆');
			}
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	});
	$('#downloadReport').load(function () {
        //if the download link return a page
        //load event will be triggered
		$('.LyMain').unblock();
    });
	
//	$('#amount').blur(function(){
//		var amount = $('#amount').val();
//		if(!/^[0-9]{1,10}$/.test(amount)){
//			alert('每人發送點數格式有誤');
//		}
//	});
	
	// SendGroup:[上傳UID] Button
	$('.upload_mid').click(function(){
		// remove
		var queryDataDoms = $('.dataTemplate');	
		if(queryDataDoms.length >= 1){
			$('.dataTemplate').remove();
			queryDataDoms = $('.dataTemplate');
		}
		
		$('#upload_mid_btn').click();
	});
	$('#upload_mid_btn').on("change", function(ev){
		// calculate & extract csv data
		csvEventListener(ev);
		
		// import xlsx data
		var input = ev.currentTarget;
    	if (input.files && input.files[0]) {
    		var fileName = input.files[0].name;
    		console.info("fileName : " + fileName);
    		$('#fileNameShow').text(fileName);
    		
    		var form_data = new FormData();
    		
    		form_data.append("filePart",input.files[0]);

    		// url: bcs.bcsContextPath + '/market/uploadMidSendGroup',
    		$('.LyMain').block($.BCS.blockMsgUpload);
    		$.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + '/edit/csvToExcel',
                cache: false,
                contentType: false,
                processData: false,
                data: form_data
    		}).success(function(response){
            	console.info('response csvToExcel:', response);
            	console.info('response.count:', response.count);
            	//alert("匯入成功!");
        		var queryBody = templateBody.clone(true);
        		//queryBody.find('.btn_delete').click(btn_deteleFunc);
        		queryBody.find('.labelField').html("UID匯入");
        		queryBody.find('.labelField').show();
        		queryBody.find('.labelOp').html(fileName);
        		queryBody.find('.labelOp').show();
        		queryBody.find('.labelValue').html(response.count + " 筆 UID");
        		queryBody.find('.labelValue').show();
        		queryBody.find('.option').remove();

        		queryBody.find('.labelValue').attr('fileName', fileName);
        		queryBody.find('.labelValue').attr('referenceId', response.referenceId);
        		queryBody.find('.labelValue').attr('count', response.count);
        		$('#tableBody').append(queryBody);
    		}).fail(function(response){
    			console.info(response);
    			$.FailResponse(response);
    			$('.LyMain').unblock();
    		}).done(function(){
    			$('.LyMain').unblock();
    		});
        } 

    	
	});
	
	// SendGroup: Get Send Group Rows
	var getSendGroupDetailFunc = function(){
		var queryDataDoms = $('.dataTemplate');
		var groupId = sendGroupId;
		
//		if(!$('#title').val()){
//			alert('請先輸入專案名稱');
//			return;
//		}
		
		var groupTitle = 'LPSG;' + $('#title').val();
		console.info('groupTitle', groupTitle);
		
		var postData = {};
		postData.groupTitle = groupTitle;
		
		if(groupId < 0){ // 預設群組不需要設定
			postData.groupId = groupId;
		}else{
//			if(queryDataDoms.length == 0){
//				alert('請設定群組條件');
//				return;
//			}
//			btnTarget = "btn_query";
//			if (!sendGroupCreationValidator.form()) {
//				return;
//			}
		}
		// Get Query Data
		var sendGroupDetail = [];
		$.each(queryDataDoms, function(i, o){
			var dom = $(o);
			var queryData = {};
			if(dom.find('.labelField').is(':visible')){
				queryData.queryField = 'UploadMid';
				queryData.queryOp = dom.find('.labelValue').attr('fileName');
				queryData.queryValue = dom.find('.labelValue').attr('referenceId') + ":" + dom.find('.labelValue').attr('count');
			}else{
				queryData.queryField = dom.find('.queryField').val();
				queryData.queryOp = dom.find('.queryOp').val();
				queryData.queryValue = dom.find('.queryValue:visible').val();
			}
			sendGroupDetail.push(queryData);
		});
		postData.sendGroupDetail = sendGroupDetail;
		return postData;
	}

	// SendGroup: File Upload and Parse CSV
	function csvEventListener(e) {
		// Check for the various File API support.
	      if (window.FileReader) {
	          // FileReader are supported.
	    	  var files = e.target.files;
	    	  //proceed your files here
	    	  var reader = new FileReader();
	          // Read file into memory as UTF-8      
	          reader.readAsText(files[0]);
	          // Handle errors load
	          reader.onload = loadHandler; //將內容打印出來
	          reader.onerror = errorHandler;
	    	  
	      } else {
	          alert('FileReader are not supported in this browser.');
	      }		
	};
	function loadHandler(event) {
      var csv = event.target.result;
      processData(csv);
    }
    function processData(csv) {
    	var fileInformation = document.getElementById("fileInformation");
    	// split by row
        var rows = csv.split(/\r\n|\n/);
        console.info('original rows:', rows);
        
        // delete empty rows
        var removeIndexs = [];
        for (var i = 0; i < rows.length; i++){
        	if (rows[i].trim().length <= 0) {
        		removeIndexs.push(i);
        	}
        }
        if(removeIndexs.length > 0) {
	        removeIndexs.reverse().forEach(function(index){
	        	rows.splice(index, 1);
	        });
        }
        console.info('trimmed rows:', rows);
        
        // get fix amount & need column max number
        var fixAmount = parseInt($('#amount').val());
        var needColMaxNum = ($('.sendAmountType')[0].checked)?2:3;
    	console.info('fixAmount:', fixAmount);
    	console.info('needColMaxNum:', needColMaxNum);
    	
        // split by column
        uids = []; custIds = []; pts = [];
        outUids = []; outCustIds = []; outPts = [];
        colMaxNum = 0;
        var maxPts = 0;
        for (var i = 0; i < rows.length; i++) {
        	var cols = rows[i].split(/,/);
        	console.info('cols:', cols);
        	
        	if(cols.length > 3){
        		alert("名單格式有問題，請檢查");
        		return;
        	}
        	
    		var uidFormat = /^U[a-z0-9]{32}$/;
    		if(!uidFormat.test(cols[0])){
    			alert('名單格式有問題，請檢查');
    			$('.LyMain').unblock();
    			return;
    		}
        	
        	
        	uids.push(cols[0]);
        	custIds.push(cols[1]);
        	
        	// set column max number
        	if(cols.length > colMaxNum){
        		colMaxNum = cols.length;
        	}
        	
        	// save & check columns
        	if(needColMaxNum == 2){ // universal
        		if(cols.length == 3){ // check universal but with points
        			alert('您選擇統一發送數量，但上傳的資料格式不符');
        			windowReplace();
        			return;
        		}else{
        			pts.push(fixAmount);
        		}
        	}else{ // individual
        		if(cols.length == 3){
        			pts.push(cols[2]);
        			// store max points
        			if(cols[2] > maxPts){
        				maxPts = cols[2];
        			}
        			var amoutFormat = /^[0-9]*$/;
        			if(!amoutFormat.test(cols[2])){
        				alert('名單格式有問題，請檢查');
        				$('.LyMain').unblock();
        				return;
        			}
        		}else{
        			pts.push(0);
        		}
        	}
        }
        $.ajax({
            type: 'get',
            url: bcs.bcsContextPath + '/edit/getCaveatLinePoint',
            processData: false
		}).success(function(response){
			console.info('caveatLinePoint : ' , response);
			caveatLinePoint = response;
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			return;
		}).done(function(){
			 //從系統參數抓警告點數上限
			 if(parseInt(maxPts)>= parseInt(caveatLinePoint) ){
		    		var r = confirm('您的名單中有單筆超過'+caveatLinePoint+'點的發送需求');
		    		if (r) {
		    		} else {
		    			windowReplace();
		    			return;
		    		}
		        }
		});
        
        // check max points
        
        // check individual but column max number = 2
        console.info('colMaxNum:', colMaxNum);
        if(needColMaxNum == 3 && colMaxNum == 2){
			alert('您上傳的資料中未包含發送數量資訊');
			windowReplace();
			//return;
        }
        $('.sendAmountType').attr('disabled', true);
        //$('#amount').attr('disabled', true);
        // no need to check follow-age
        //改成是否檢核加入的uid
//        if($('.doCheckFollowage')[1].checked){
//        	calculateSum();
//        	return;
//        }
        
        // check follow-age
        $.ajax({
            type: 'POST',
            url: bcs.bcsContextPath + '/edit/checkActiveUids',
            cache: false,
            contentType: 'application/json',
            processData: false,
            data : JSON.stringify(uids)
		}).success(function(response){
        	console.info('checkActiveUids response:', response);
        	console.info('checkActiveUids response.length:', response.length);
        	removeIndexs = response;
        	if(removeIndexs.length != 0){
        		// trim inactive UIds
                if(removeIndexs.length > 0) {
        	        removeIndexs.reverse().forEach(function(index){
        	        	outUids.push(uids[index]);
        	        	outCustIds.push(custIds[index]);
        	        	outPts.push( pts[index]);
        	        	uids.splice(index, 1);
        	        	custIds.splice(index, 1);
        	        	pts.splice(index, 1);
        	        });
                }
                console.info('trimmed uids:', uids);
                console.info('trimmed custIds:', custIds);
                console.info('trimmed pts:', pts);
                console.info('out uids:', outUids);
                console.info('out custIds:', outCustIds);
                console.info('out pts:', outPts);
        	}
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			return;
		}).done(function(){
			calculateSum();
		});
    }
    
    function calculateSum(){
        // calculate sum
        var sum = 0;
        var outSum = 0;
        if(colMaxNum == 2){
        	var amount = parseInt($('#amount').val());
        	if(isNaN(amount) || amount <= 0){
        		alert('發送數量必須大於零');
        		windowReplace();
        	}
        	sum = uids.length * amount;
        	outSum = outUids.length * amount;
        }else{
        	sum = 0;
        	outSum = 0
            for (var i = 0; i < pts.length; i++) {
            	if(pts[i].trim == ''){
            		pts[i] = 0;
            	}
            	sum += parseInt(pts[i], 10);
            }
        	for (var i = 0; i < outPts.length; i++) {
        		if(outPts[i].trim == ''){
        			outPts[i] = 0;
        		}
        		outSum += parseInt(outPts[i], 10);
        	}
        }
    	outCount = outUids.length;
    	outTotalAmount = outSum;
    	TrimmedCount = uids.length;
    	TrimmedTotalAmount = sum;
    	
    	console.info('TrimmedCount = ', TrimmedCount);
    	console.info('outCount = ', outCount);
    	
        // export to global variables & front-end
    	var doCheckFollowage = $('[name="doCheckFollowage"]:checked').val();
    	if(doCheckFollowage == 'true'){
    		totalCount  = TrimmedCount;
    		totalAmount = TrimmedTotalAmount;
    		fileInformation.innerHTML = '本次共發送' + TrimmedCount + '筆，合計' + TrimmedTotalAmount +'點LINE POINTS';
    	}else{
    		totalCount  = TrimmedCount+outCount;
    		totalAmount = TrimmedTotalAmount+outTotalAmount;
    		fileInformation.innerHTML = '本次共發送' + totalCount + '筆，合計' + totalAmount +'點LINE POINTS';
    	}
    }
    //選擇是否加入會員
    $('[name="doCheckFollowage"]').click(function(){
    	var doCheckFollowage = $('[name="doCheckFollowage"]:checked').val();
    	if(doCheckFollowage == 'true'){
    		totalCount  = TrimmedCount;
    		totalAmount = TrimmedTotalAmount;
	   		fileInformation.innerHTML = '本次共發送' + TrimmedCount + '筆，合計' + TrimmedTotalAmount +'點LINE POINTS';
	   	}else{
	   		totalCount  = TrimmedCount+outCount;
    		totalAmount = TrimmedTotalAmount+outTotalAmount;
	   		fileInformation.innerHTML = '本次共發送' + totalCount + '筆，合計' + totalAmount +'點LINE POINTS';
	   	}
    });
    function errorHandler(evt) {
      if(evt.target.error.name == "NotReadableError") {
          alert("Canno't read file !");
      }
    }
	
	
	// ---- Send Message ----
	// SendMessage:Validator
	var sendingMsgValidator = $('#formSendGroup').validate({
		rules : {

			// 發送群組
			/*
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
			*/
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

	// SendMessage:[預覽] Button
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
	var msgTagContentFlag = $.BCS.contentFlagComponent('msgTag', 'MSG_SEND', {
		placeholder : '請輸入類別'
	});
	
	// SendMessage: Get SendTimingTime
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
		return sendingMsgTime;
	};

	// SendMessage: Get SendTimingType
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
	});

	// SendMessage: Option Change Function
	var optionSelectChange_func = function(){
		var selectValue = $(this).find('option:selected').text();
		$(this).closest('.option').find('.optionLabel').html(selectValue);
	};
    $( ".datepicker" ).datepicker({ 'minDate' : 0, 'dateFormat' : 'yy-mm-dd'});
	$('.selectMonth').change(optionSelectChange_func);
	$('.selectWeek').change(optionSelectChange_func);
	$('.selectHour').change(optionSelectChange_func);
	$('.serialSetting').change(optionSelectChange_func);
	$('.selectMinuteOne').change(optionSelectChange_func);
	$('.selectMinuteTwo').change(optionSelectChange_func);
	
	$(".doAppendMessage").click(function(e){
		var doAppendMessage = e.currentTarget.value;
		switch(doAppendMessage){
			case 'false' :
				$("#AppendMessageView").hide();
				$("#AppendMessageTitleTr").hide();
				break;
			case 'true' :
				$("#AppendMessageView").show();
				$("#AppendMessageTitleTr").show();
				break;
		}
	});
	
    // ---- Save ----
	// Save:SendGroup
	$('.send_group_create').click(function(){
		// block
		$('.LyMain').block($.BCS.blockMsgUpload);
		
		//欄位檢核
		var title = $('#title').val();
		if(title.length == 0 ){
			alert('專案名稱不可空白');
			$('.LyMain').unblock();
			return;
		}
		
		var pccCode = $('#pccCode').val();
		if(pccCode.length == 0 ){
			alert('掛帳PCC不可空白');
			$('.LyMain').unblock();
			return;
		}else if(pccCode.length < 9 || pccCode.length > 12){
			alert('掛帳PCC格式有誤');
			$('.LyMain').unblock();
			return;
		}else{
			if(pccCode.indexOf('-') == -1){
				var pccCodeFormat = /^[a-zA-Z0-9]{9,11}$/;
				if(!pccCodeFormat.test(pccCode)){
					alert('掛帳PCC格式有誤');
					$('.LyMain').unblock();
					return;
				}
			}else{
				pccCode = pccCode.replace("-","");
				var pccCodeFormat = /^[a-zA-Z0-9]{9,10}$/;
				if(!pccCodeFormat.test(pccCode)){
					alert('掛帳PCC格式有誤');
					$('.LyMain').unblock();
					return;
				}
			}
		}
		
		var serialId = $('#serialId').val();
		var serialIdFormat = /^\d{14}$/;
		if(serialId.length == 0){
			alert('campaign code 不可空白');
			$('.LyMain').unblock();
			return;
		}else if(!serialIdFormat.test(serialId)){
			alert('campaign code 格式有誤');
			$('.LyMain').unblock();
			return;
		}
		
		var sendTimeType = $('.sendTimeType:checked').val();
		if(sendTimeType == "DELAY" ){
			var datepicker = $('#delaySelect .datepicker').val();
			if(datepicker.length == 0){
				alert('請選擇預約發送時間');
				$('.LyMain').unblock();
				return;
			}
			var sendingMsgTime ;
			var datepickerVal = $('#delaySelect .datepicker').val();
			var selectHour = $('#delaySelect .selectHour').val();
			console.info('selectHour', selectHour);
			var selectMinuteOne = $('#delaySelect .selectMinuteOne').val();
			console.info('selectMinuteOne', selectMinuteOne);
			var selectMinuteTwo = $('#delaySelect .selectMinuteTwo').val();
			console.info('selectMinuteTwo', selectMinuteTwo);
			sendingMsgTime = datepickerVal + " " + selectHour + ":" + selectMinuteOne + selectMinuteTwo + ":00";
			console.info('sendingMsgTime', sendingMsgTime);
			var today=new Date();
			if(today > new Date(sendingMsgTime)){
				alert('預約發送時間必須大於現在');
				$('.LyMain').unblock();
				return;
			}
			
		}
		
		var sendAmountType = $('.sendAmountType:checked').val();
		if( sendAmountType == "UNIVERSAL"){
			var amount = $('#amount').val();
			var amountFormat = /^\d{1,10}$/;
			if(amount.length == 0){
				alert('請輸入每人發送幾點');
				$('.LyMain').unblock();
				return;
			}else if(!amountFormat.test(amount)){
				alert('每人發送幾點只能輸入正整數');
				$('.LyMain').unblock();
				return;
			}

		}
		
		// set Send Group Id
		var queryDataDoms = $('.dataTemplate');
		if(queryDataDoms.length == 0){
			alert('請設定輸入CSV檔案');
			$('.LyMain').unblock();
			return;
		}
		
//		btnTarget = "btn_save";
//		if (!sendGroupCreationValidator.form()) {
//			return;
//		}
		
		// set linePointMain settings
		var groupTitle = 'LPSG;' + $('#title').val();
		var groupDescription = 'LINE_POINT_SEND_GROUP';
		var groupId = $.urlParam("sendGroupId");
		var actionType = $.urlParam("actionType");
		console.info('groupTitle', groupTitle);
		console.info('groupDescription', groupDescription);
		console.info('groupId', groupId);
		console.info('actionType', actionType);
		
		var msgAction = "Create";
		if(groupId && actionType == 'Edit'){
			msgAction = "Change"
		}else if(actionType == 'Copy'){
			groupId = null;
		}
		
		// Get Query Data
		var sendGroupDetail = [];
		$.each(queryDataDoms, function(i, o){
			var dom = $(o);
			var queryData = {};
			
			if(dom.find('.labelField').is(':visible')){
				queryData.queryField = 'UploadMid';
				queryData.queryOp = dom.find('.labelValue').attr('fileName');
				queryData.queryValue = dom.find('.labelValue').attr('referenceId') + ":" + dom.find('.labelValue').attr('count');
			}else{
				queryData.queryField = dom.find('.queryField').val();
				queryData.queryOp = dom.find('.queryOp').val();
				queryData.queryValue = dom.find('.queryValue:visible').val();
			}
			
			sendGroupDetail.push(queryData);
		});
		
		var postData = {};
		postData.groupId = groupId;
		postData.groupTitle = groupTitle;
		postData.groupDescription = groupDescription;
		postData.sendGroupDetail = sendGroupDetail;
		
		console.info('postData', postData);

		// Do Confirm Check
//		var confirmStr = "請確認是否建立";
//		if(msgAction  == "Change"){
//			confirmStr = "請確認是否儲存";
//		}
//		var r = confirm(confirmStr);
//		if (!r) {
//			return;
//		}
		
		// create send group
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/market/createSendGroup',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info('createSendGroup response:', response);
			sendGroupId = response.groupId;
			console.info('sendGroupId:', sendGroupId);
			
			//alert('儲存成功');
	 		//windowReplace();
		}).fail(function(response){
			console.info('response = ', response);
			console.info('response.status = ', response.status);
			$.FailResponse(response);
			$('.LyMain').unblock();
			windowReplace();
		}).done(function(){
			var doAppendMessage = ($('.doAppendMessage')[0].checked)?false:true;
			
			if(doAppendMessage==false){
				// don't save draft
				var sendingMsgType = $('[name="sendTimeType"]:checked').val();
				console.info('sendingMsgType', sendingMsgType);
				
				if(sendingMsgType == "SCHEDULE" || sendingMsgType == "DELAY"){
					sendingMsgTime = getSendMsgTime(sendingMsgType, true);
					if(!sendingMsgTime){
						return;
					}
				}else{
					sendingMsgTime = getSendMsgTime(sendingMsgType, false);
				}

				console.info('sendingMsgTime:', sendingMsgTime);
				linePointMainSave(sendingMsgTime);	
			}else{
				// save draft
				sendingMsgFunc("SaveDraft");				
			}
		});
	});
	
	// Save:SaveDraft
	var sendingMsgFunc = function(actionType){
		btnTarget = actionType;

		if($.BCS.actionTypeParam == "Look"){
			location.reload();
			return;
		}

		// 發送群組
		var serialId = $('.serialSetting').val();
		console.info('sendGroupId', sendGroupId);
		console.info('serialId', serialId);

		// 發送時間
		var sendingMsgType = $('[name="sendTimeType"]:checked').val();
		console.info('sendingMsgType', sendingMsgType);
		
		// 訊息類別
		var msgTagList = [];
		//var msgTagList = msgTagContentFlag.getContentFlagList();
		console.info('msgTagList', msgTagList);

		$.BCS.isValidate  = true;
		// 發送內容 設定
		var MsgFrameContents = $.BCS.getMsgFrameContent();
		console.info('MsgFrameContents', MsgFrameContents);
		// Validate Error
		if(!$.BCS.isValidate){
			$('.LyMain').unblock();
			return;
		}

		var sendingMsgTime = "";
		if (!sendingMsgValidator.form()) {
			$('.LyMain').unblock();
			return;
		}

		// 驗證輸入
		if(actionType == "SendMsg"){
			if(!sendingMsgType){
				alert('請設定發送時間');
				$('.LyMain').unblock();
				return;
			}
			else{
				if(sendingMsgType == "SCHEDULE" || sendingMsgType == "DELAY"){
					sendingMsgTime = getSendMsgTime(sendingMsgType, true);
					if(!sendingMsgTime){
						$('.LyMain').unblock();
						return;
					}
				}
			}
		}else{
			sendingMsgTime = getSendMsgTime(sendingMsgType, false);
		}
		console.info('sendingMsgTime:', sendingMsgTime);
		
		
		if(!MsgFrameContents || MsgFrameContents.length < 1){
			alert('請設定發送推播內容');
			$('.LyMain').unblock();
			return;
		}
		else if(MsgFrameContents.length > 4){
			alert('發送推播內容不能超過4個');
			$('.LyMain').unblock();
			return;
		}

		// Do Confirm Check
		var confirmStr = "請確認是否傳送";
		if(actionType  == "SaveDraft"){
			confirmStr = "請確認是否儲存";
		}
		var r = confirm(confirmStr);
		if (r) {
			// confirm true
		} else {
			$('.LyMain').unblock();
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

		console.info('msgTagList:', msgTagList);
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
			console.info('sendingMsg response:', response);
			draftMsgId = response;
			console.info('draftMsgId:', draftMsgId);
			
			if(postData.actionType == "SaveDraft"){
				//alert('儲存成功');
				//window.location.replace(bcs.bcsContextPath +'/edit/msgListPage');
			}else if(postData.actionType == "SendMsg"){
				if(sendingMsgType == "DELAY"){
					//alert('預約發送成功');
				}else{
					//alert('立即傳送成功');
				}
				//window.location.replace(bcs.bcsContextPath +'/edit/linePointCreatePage');
			}else{
				//alert('傳送成功');
			}
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
			window.location.replace(bcs.bcsContextPath +'/edit/linePointCreatePage');
		}).done(function(){
			//$('.LyMain').unblock();
			linePointMainSave(sendingMsgTime);
		});
	};
	
    // Save:LinePointMain
    function linePointMainSave(sendingMsgTime){
        
        var title = $('#title').val();
        var pccCode = $('#pccCode').val();
        var serialId = $('#serialId').val();
        var sendTimingType = ($('.sendTimeType')[0].checked)?"IMMEDIATE":"SCHEDULE";
        var sendAmountType = ($('.sendAmountType')[0].checked)?"UNIVERSAL":"INDIVIDUAL";
        var amount = ($('.sendAmountType')[0].checked)?($('#amount').val()):0;
        var doCheckFollowage = ($('.doCheckFollowage')[0].checked)?true:false;
        var doAppendMessage = ($('.doAppendMessage')[0].checked)?false:true;
        console.info('sendGroupId:', sendGroupId);
    	console.info('draftMsgId:', draftMsgId);
    	
    	var sendTimingTime = null;
    	if(sendTimingType == 'SCHEDULE'){
    		 sendTimingTime = moment(sendingMsgTime).format('YYYY-MM-DD HH:mm:ss');
    	}
    	console.info('sendTimingTime:', sendTimingTime);

        var postData = {};

        // Type Information
        postData.sendType = 'BCS';
        // modifyUser
        // modifyTime
        postData.title = title;
        postData.serialId = serialId;
        postData.pccCode = pccCode;
        // deparmentFullName
        postData.status = 'IDLE';
        postData.sendTimingType = sendTimingType;
        postData.sendAmountType = sendAmountType;
        postData.sendTimingTime = sendTimingTime;
        postData.amount = amount;
        postData.doCheckFollowage = doCheckFollowage;
        postData.doAppendMessage = doAppendMessage;
        postData.allowToSend = false;
        postData.appendMessageId = draftMsgId;
        postData.linePointSendGroupId = sendGroupId;
        // sendStartTime
        
        // Count/Amount Information
        postData.totalCount = totalCount;
        postData.totalAmount = totalAmount;
        postData.successfulCount = 0;
        postData.successfulAmount = 0;
   	 	postData.failedCount = 0;
       
//   	 	if(!doCheckFollowage){
//        	 postData.failedCount = 0;
//        }else{
//        	postData.failedCount = outCount;
//        }
        
        console.info('postData', postData);
        linePointMainId = $.urlParam("linePointMainId");
        if(linePointMainId === null){
        }else{
        	postData.id = linePointMainId;
        }
        
        $.ajax({
            type: "POST",
            url: bcs.bcsContextPath + '/edit/createLinePointMain',
            cache: false,
            contentType: 'application/json',
            processData: false,
            data: JSON.stringify(postData)
        }).success(function(response) {
            console.info('createLinePointMain response:', response);
            linePointMain = response.id;
            console.info('linePointMain:',linePointMain);
        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
            windowReplace();
            $('.LyMain').unblock();
        }).done(function(){
        	linePointDetailSave();
		});
    }
    
    // Save:LinePointDetail
    function linePointDetailSave(){
    	console.info('linePointDetailSave');
    	console.info('TrimmedCount = ', TrimmedCount);
    	console.info('outCount = ', outCount);
    	
//    	var detail = {};
    	var detailList = [];
    	for (var i = 0; i < TrimmedCount; i++) {
    	//	每次都要新增一筆  不然會出錯
    		var detail = {};
    		detail.linePointMainId = linePointMain;
    		detail.custid = custIds[i];
    		detail.amount = pts[i];
    		detail.uid = uids[i];
    		detail.isMember = 1;  //紀錄是否是會員
    		detailList.push(detail);
        }
    	var doCheckFollowage = $('[name="doCheckFollowage"]:checked').val();
    	for (var i = 0; i < outCount ; i++) {
        	//	每次都要新增一筆  不然會出錯
        		var detail = {};
        		detail.linePointMainId = linePointMain;
        		detail.custid = outCustIds[i];
        		detail.amount = outPts[i];
        		detail.uid = outUids[i];
        		
        		// 如果要在建立專案匯入名單的時候就做檢核開啟，就把下面打開
//        		if(doCheckFollowage == 'true'){
//        			detail.status = 'FAIL';
//        			detail.message = '客戶已封鎖';
//        		}
        		detail.isMember = 0;
        		detailList.push(detail);
            }
    	console.info('detailList:', detailList);

        $.ajax({
            type: "POST",
            url: bcs.bcsContextPath + '/edit/createLinePointDetailList',
            cache: false,
            contentType: 'application/json',
            processData: false,
            data: JSON.stringify(detailList)
        }).success(function(response) {
            console.info('linePointDetailSave response:', response);
            alert('儲存成功');
            $('.LyMain').unblock();
            window.location.replace(bcs.bcsContextPath + '/edit/linePointCreatePage');
        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
            $('.LyMain').unblock();
            windowReplace();
        }).done(function(){
		});
    }
    
    // ---- initialize ---- 
	// Initialize:SendGroup
	var sendGroupCreationLoadDataFunc = function(){
		// get urlParam
		sendGroupId = $.urlParam("sendGroupId");
		
		// clone & remove SendGroup Rows
		templateBody = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		
		// get Send Group Default Conditions
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/market/getSendGroupCondition'
		}).success(function(response){
//			console.info('market_getSendGroupCondition:', response);
			sendGroupCondition = response;
			
			$.each(sendGroupCondition, function(queryFieldId, queryFieldObject){
				templateBody.find('.queryField').append(
						'<option value="' + queryFieldId + '">' + queryFieldObject.queryFieldName + '</option>');
			});
			
			// get SendGroup Rows
			var groupId = $.urlParam("sendGroupId");
			if(groupId){
				$.ajax({
					type : "GET",
					url : bcs.bcsContextPath + '/market/getSendGroup?groupId=' + groupId
				}).success(function(response){
					$('.dataTemplate').remove();
					console.info(response);

					$('#groupTitle').val(response.groupTitle);
					$('#groupDescription').val(response.groupDescription);
					
					if(groupId > 0){
						// custom group 
						$.each(response.sendGroupDetail, function(i, o){
							var queryBody = templateBody.clone(true);
							queryBody.find(".datepicker").datepicker({ 'dateFormat' : 'yy-mm-dd'});
							queryBody.find('.optionSelect').change(optionSelectChange_func);
							
							if('UploadMid' == o.queryField){
								var split = o.queryValue.split(':');
								
								//fileNameShow
								$('#fileNameShow').text(o.queryOp);
								
				        		queryBody.find('.labelField').html("UID匯入");
				        		queryBody.find('.labelField').show();
				        		queryBody.find('.labelOp').html(o.queryOp);
				        		queryBody.find('.labelOp').show();
				        		queryBody.find('.labelValue').html(split[1] + " 筆 UID");
				        		queryBody.find('.labelValue').show();
				        		queryBody.find('.option').remove();

				        		queryBody.find('.labelValue').attr('fileName', o.queryOp);
				        		queryBody.find('.labelValue').attr('referenceId', split[0]);
				        		queryBody.find('.labelValue').attr('count', split[1]);
							}else{
								queryBody.find('.queryField').val(o.queryField).change();
								queryBody.find('.queryOp').val(o.queryOp).change();
								queryBody.find('.queryValue').val(o.queryValue).change();
							}
	
							//queryBody.find('.btn_delete').click(btn_deteleFunc);
							
							$('#tableBody').append(queryBody);
							//setValidationOnNewRow();
						});
					}else{
						// default group
						$('#groupTitle').attr('disabled',true);
						$('#groupDescription').attr('disabled',true);
						
						$('.btn_save').remove();
						$('#queryContent').remove();
					}
				}).fail(function(response){
					console.info(response);
					$.FailResponse(response);
				}).done(function(){
				});
			}
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};
	
	// Initialize:LinePoint
    var initLinePointMain = function() {
    	if($.urlParam('linePointMainId')){
    		$('.LyMain').block($.BCS.blockMsgUpload);
    		linePointMainId = $.urlParam('linePointMainId');
//    		console.info("linePointMainId:", $.urlParam('linePointMainId'));
    		// get LinePointMain
            $.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + '/edit/findOneLinePointMainByMainId?linePointMainId=' + linePointMainId,
            }).success(function(o) {
//                console.info('findOneLinePointMainByMainId response:', o);
                
                $('#title').val(o.title);
                $('#pccCode').val(o.pccCode);
                $('#serialId').val(o.serialId);
                
                var fileInformation = document.getElementById("fileInformation");

                fileInformation.innerHTML = '本次共發送' + o.totalCount + '筆，合計' + o.totalAmount +'點LINE POINTS';
                totalCount = o.totalCount;
                totalAmount = o.totalAmount;
                var doAppendMessage = o.doAppendMessage;
                if(doAppendMessage){
                	$('.doAppendMessage')[1].click();
                }else{
                	$('.doAppendMessage')[0].click();
                }
                
                var doCheckFollowage = o.doCheckFollowage;
                if(doCheckFollowage){
                	$('.doCheckFollowage')[0].click();
                }else{
                	$('.doCheckFollowage')[1].click();
                }
                
                var sendTimingType = o.sendTimingType;
                if(sendTimingType=="IMMEDIATE"){
                	$('.sendTimeType')[0].click();
                }else{
                	$('.sendTimeType')[1].click();
                	// set schedule Time
                	var scheduleTime = o.sendTimingTime;
//    				console.info('scheduleTime', scheduleTime);
    				if(scheduleTime){
    					var splits = scheduleTime.split(' ');
//    					console.info('splits', splits);

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
 
                var sendAmountType = o.sendAmountType;
                if(sendAmountType=="UNIVERSAL"){
                	$('.sendAmountType')[0].click();
//                	console.info('o.amount:', o.amount);
                	$('#amount').val(o.amount);
                }else{
                	$('.sendAmountType')[1].click();
                	colMaxNum = 3 ;
                }
                
		        // get date data
		        var currentTime = moment();
		        var sendTimingTime = moment(o.sendTimingTime).format('YYYY-MM-DD HH:mm:ss');
//		        console.info('currentTime:', currentTime);
//		        console.info('sendTimingTime:', sendTimingTime);
//		        console.info('isAfter:', currentTime.isAfter(sendTimingTime));
		        
                // hide Send Group Create Button
                if(o.status == 'COMPLETE'){
                	$('.send_group_create').hide();
                }
                if(o.sendTimingType == 'SCHEDULE' && currentTime.isAfter(sendTimingTime)){
                	$('.send_group_create').hide();
                }
                
            }).fail(function(response) {
                console.info(response);
                $.FailResponse(response);
                $('.LyMain').unblock();
                
            }).done(function(){
            	$('.LyMain').unblock();
            	initLinePointDetail();
    		});
    	}
    	
    }
    
 // Initialize:LinePointDetail
    function initLinePointDetail(){
    	if($.urlParam('linePointMainId')){
    		linePointMainId = $.urlParam('linePointMainId');
    		console.info("linePointMainId:", $.urlParam('linePointMainId'));
    		$.ajax({
                type: 'GET',
                url: bcs.bcsContextPath + '/edit/findAllLinePointDetailByMainId?linePointMainId=' + linePointMainId
    		}).success(function(response){
    			console.info('LinePointDetail response:', response);
    			uids = []; custIds = []; pts = [];
    			outUids = []; outCustIds = []; outPts = [];
    			console.info('LinePointDetail length :' ,response.length);
    			for(var i = 0 ; i < response.length ; i++){
    				if(response[i].isMember == 0){
    					outUids.push(response[i].uid);
    					outCustIds.push(response[i].custid);
    					outPts.push(response[i].amount);
    				}else{
    					uids.push(response[i].uid);
        				custIds.push(response[i].custid);
        				pts.push(response[i].amount);
    				}
    			}
    			console.info('uids :' ,uids);
    			console.info('custIds :' ,custIds);
    			console.info('pts :' ,pts);
    		}).fail(function(response){
    		}).done(function(){
    			calculateSum();
    		});
    	}
    }
	// Initialize:SendMessage
	$.BCS.ResourceMap = {};
	var sendingMsgLoadDataFunc = function(){
		var serialId = "";
		var msgId = $.urlParam("msgId");
		var msgSendId = $.urlParam("msgSendId");
		console.info('msgSendId = ', msgSendId);
		
		// Load Back Send Message Data
		if(msgId || msgSendId){
			var getDataUrl = bcs.bcsContextPath +'/edit/getSendMsg';
			if(msgSendId){
				getDataUrl =getDataUrl + '?msgSendId=' + msgSendId;
			}else if (msgId == 'null'){
				return;
			}else{
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
//					console.info('groupId', key);
//					console.info('groupTitle', groupTitle);

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
					
					//msgTagContentFlag.findContentFlagList(keyObj.msgId);
					
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
	};
	
    sendGroupCreationLoadDataFunc();
    sendingMsgLoadDataFunc();
    initLinePointMain();
    //當匯入的UID有誤的時候會重整介面，為保留以填寫的資料，會判斷是修改還是新增的 然後放回原本的地方。
    
    function windowReplace(){
    	
    	if($.urlParam("actionType")){
    		window.location.replace(bcs.bcsContextPath + '/edit/linePointCreatePage?linePointMainId=' +$.urlParam("linePointMainId")+
    																				'&sendGroupId='+$.urlParam("sendGroupId")+
    																				'&msgId='+$.urlParam("msgId")+
    																				'&actionType='+$.urlParam("actionType"));
    	}else{
    		var title 			= $('#title').val();
	    	var pccCode 		= $('#pccCode').val();
	    	var serialId 		= $('#serialId').val();
	    	var sendTimeType 	= $('.sendTimeType:checked').val();
	    	var sendAmountType 	= $('.sendAmountType:checked').val();
	    	var amount		 	= $('#amount').val();
	    	var datepicker 		= $('#delaySelect .datepicker').val();
	    	var selectHour 		= $('#delaySelect .selectHour').val();
	    	var selectMinuteOne = $('#delaySelect .selectMinuteOne').val();
	    	var selectMinuteTwo = $('#delaySelect .selectMinuteTwo').val();
	    	window.location.replace(bcs.bcsContextPath + '/edit/linePointCreatePage?title='				+title+
	    																			'&pccCode='			+pccCode+
	    																			'&serialId='		+serialId+
	    																			'&sendTimeType='	+sendTimeType+
	    																			'&datepicker='		+datepicker+
	    																			'&selectHour='		+selectHour+
	    																			'&selectMinuteOne='	+selectMinuteOne+
	    																			'&selectMinuteTwo='	+selectMinuteTwo+
	    																			'&amount='			+amount+
	    																			'&sendAmountType='	+sendAmountType);
    	}
    	
    	
    }
    
    
});


