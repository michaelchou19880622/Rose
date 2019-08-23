/**
 *
 */
$(function(){
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
	
	// LinePointDetails
    var uids = [], custIds = [], pts = [];
    var colMaxNum = 2;    
    
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
			console.info(response);
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
	
	// SendGroup:[上傳UID] Button
	$('.upload_mid').click(function(){
		var queryDataDoms = $('.dataTemplate');		
		// remove
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
            	console.info(response);
            	alert("匯入成功!");
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
		
		var groupTitle = $('#groupTitle').val();
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
        
        // get fix amount
        var fixAmount = parseInt($('#amount').val());
        console.info('fixAmount:', fixAmount);
        
        // split by column
        uids = []; custIds = []; pts = [];
        colMaxNum = 2;
        for (var i = 0; i < rows.length; i++) {
        	var cols = rows[i].split(/,/);
        	console.info('cols:', cols);
        	uids.push(cols[0]);
        	custIds.push(cols[1]);
        	if(cols.length == 3){
        		colMaxNum = 3;
        		pts.push(parseInt(cols[2]));
        	}else{
        		pts.push(fixAmount);
        	}
        }
        
        // calculate sum
        var needColMaxNum = ($('.sendAmountType')[0].checked)?2:3;
    	console.info('colMaxNum:', colMaxNum);
    	console.info('needColMaxNum:', needColMaxNum);
    	
        if(needColMaxNum != colMaxNum) {
        	alert('資料行數：' + colMaxNum + '與需要行數：' + needColMaxNum + '不符');
        	window.location.replace(bcs.bcsContextPath + '/edit/linePointCreatePage');
        }
        
        var sum = 0;
        if(colMaxNum == 2){
        	var amount = parseInt($('#amount').val());
        	if(isNaN(amount) || amount <= 0){
        		alert('發送數量必須大於零');
        		window.location.replace(bcs.bcsContextPath + '/edit/linePointCreatePage');
        	}
        	sum = rows.length * amount;
        	fileInformation.innerHTML = '本次共發送' + rows.length + '筆，合計發送點數為' + sum +'點';
        }else{
        	sum = 0;
            for (var i = 0; i < pts.length; i++) {
            	sum += pts[i];
            }
            fileInformation.innerHTML = '本次共發送' + rows.length + '筆，合計發送點數為' + sum +'點';
        }
        
        totalCount = rows.length;
        totalAmount = sum;
    }
    function errorHandler(evt) {
      if(evt.target.error.name == "NotReadableError") {
          alert("Canno't read file !");
      }
    }
	
	
	

	// ---- Send Message ----
	// 表單驗證
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

	// 訊息類別
	var msgTagContentFlag = $.BCS.contentFlagComponent('msgTag', 'MSG_SEND', {
		placeholder : '請輸入類別'
	});
	

	// 傳送訊息 SaveDraft
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
			return;
		}

		var sendingMsgTime = "";
		if (!sendingMsgValidator.form()) {
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

		// Do Confirm Check
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
				alert('傳送成功');
			}
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
			window.location.replace(bcs.bcsContextPath +'/edit/linePointCreatePage');
		}).done(function(){
			//$('.LyMain').unblock();
			linePointMainSave();
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
		}else if(sendingMsgType == "SCHEDULE"){
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

	// SendMessage:[預覽]Btn
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
	var sendingMsgLoadDataFunc = function(){

		var serialId = "";
		var msgId = $.urlParam("msgId");
		var msgSendId = $.urlParam("msgSendId");
		
		// Load Back Send Message Data
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
		}
	};

    $( ".datepicker" ).datepicker({ 'minDate' : 0, 'dateFormat' : 'yy-mm-dd'});
	$('.selectMonth').change(optionSelectChange_func);
	$('.selectWeek').change(optionSelectChange_func);
	$('.selectHour').change(optionSelectChange_func);
	$('.serialSetting').change(optionSelectChange_func);
	$('.selectMinuteOne').change(optionSelectChange_func);
	$('.selectMinuteTwo').change(optionSelectChange_func);

	sendingMsgLoadDataFunc();
	
	// ----- Project Creation ----

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
	//270-970
    

    
    
    // ---- Save ----
	// SendGroup:[儲存] Button
	$('.send_group_create').click(function(){
		// block
		$('.LyMain').block($.BCS.blockMsgUpload);
		
		// set Send Group Id
		var queryDataDoms = $('.dataTemplate');
		if(queryDataDoms.length == 0){
			alert('請設定輸入CSV檔案');
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
	 		//window.location.replace(bcs.bcsContextPath + '/edit/linePointCreatePage');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
			window.location.replace(bcs.bcsContextPath + '/edit/linePointCreatePage');
		}).done(function(){
			// goto save draft
			sendingMsgFunc("SaveDraft");
		});
	});
	
    // final save
    function linePointMainSave(){
//		var MsgFrameContents = $.BCS.getMsgFrameContent();
//		console.info('MsgFrameContents', MsgFrameContents);
//		if(MsgFrameContents.length > 4){
//			alert('發送內容不能超過4個');
//			return;
//		}
        /*
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
		*/
        
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
    	
//        if (!campaignName || !campaignCode || !sendPoint || !campaignPersonNum) {
//            alert("欄位不可為空");
//            return;
//        }

        var postData = {};
        postData.sendType = 'BCS';
        postData.title = title;
        postData.pccCode = pccCode;
        postData.serialId = serialId;
        postData.sendTimingType = sendTimingType;
        postData.sendAmountType = sendAmountType;
        postData.amount = amount;
        postData.doCheckFollowage = doCheckFollowage;
        postData.doAppendMessage = doAppendMessage;
        postData.appendMessageId = draftMsgId;
        postData.linePointSendGroupId = sendGroupId;
        
        postData.totalCount = totalCount;
        postData.totalAmount = totalAmount;
        postData.successfulCount = 0;
        postData.successfulAmount = 0;
        postData.failedCount = 0;
        postData.allowToSend = false;
        postData.status = 'IDLE';
        
        console.info('postData', postData);
        
        
        $.ajax({
            type: "POST",
            url: bcs.bcsContextPath + '/edit/createLinePointMain',
            cache: false,
            contentType: 'application/json',
            processData: false,
            data: JSON.stringify(postData)
        }).success(function(response) {
            console.info(response);
            linePointMain = response.id;
            console.info('linePointMain:',linePointMain);
        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
            window.location.replace(bcs.bcsContextPath + '/edit/linePointCreatePage');
            $('.LyMain').unblock();
        }).done(function(){
        	linePointDetailSave();
		});
        
    }
    
    // linePointDetailSave
    function linePointDetailSave(){
    	console.info('linePointDetailSave');
    	linePointMain  = 10;
    	var detailList = [];
    	var detail = {};
    	
    	for (var i = 0; i < totalCount; i++) {
    		detail.linePointMainId = linePointMain;
    		detail.custid = custIds[i];
    		detail.amount = pts[i];
    		detail.uid = uids[i];
    		
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
            window.location.replace(bcs.bcsContextPath + '/edit/linePointCreatePage');
        }).done(function(){
		});
    }
    
    // ---- initialize ---- 
	// SendGroup:Initialize
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
			console.info('market_getSendGroupCondition:', response);
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
	
	// LinePoint:Initialize
    var initLinePointMain = function() {
    	$('.LyMain').block($.BCS.blockMsgUpload);
    	
    	if($.urlParam('linePointMainId')){
    		linePointMainId = $.urlParam('linePointMainId');
    		console.info("linePointMainId:", $.urlParam('linePointMainId'));
    		
    		// get LinePointMain
            $.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + '/edit/findOneLinePointMain?linePointMainId=' + linePointMainId,
            }).success(function(o) {
                console.info('findOneLinePointMain response:', o);
                
                $('#title').val(o.title);
                $('#pccCode').val(o.pccCode);
                $('#serialId').val(o.serialId);
                
                var fileInformation = document.getElementById("fileInformation");
                fileInformation.innerHTML = '本次共發送' + o.totalCount + '筆，合計發送點數為' + o.totalAmount +'點';
                
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
 
                var sendAmountType = o.sendAmountType;
                if(sendAmountType=="UNIVERSAL"){
                	$('.sendAmountType')[0].click();
                	console.info('o.amount:', o.amount);
                	$('#amount').val(o.amount);
                }else{
                	$('.sendAmountType')[1].click();
                }
                
            }).fail(function(response) {
                console.info(response);
                $.FailResponse(response);
                $('.LyMain').unblock();
                
            }).done(function(){
            	$('.LyMain').unblock();
    		});
    	}
    	
    }
    
    sendGroupCreationLoadDataFunc();
    initLinePointMain();
});

//// 儲存草稿
//$('.btn_draft').click(function(){
//	sendingMsgFunc("SaveDraft");
//});
//// 傳送
//$('.btn_save').click(function(){
//	sendingMsgFunc("SendMsg");
//});
//// 儲存草稿
//$('.SaveDraftBtn').click(function(){
//	sendingMsgFunc("SaveDraft");
//});
//// 傳送給我
//$('.SendToMeBtn').click(function(){
//	sendingMsgFunc("SendToMe");
//});
//// 傳送測試群組
//$('.SendToTestBtn').click(function(){
//	sendingMsgFunc("SendToTestGroup");
//});
// 取消
//$('.btn_cancel').click(function(){
//
//	var r = confirm("請確認是否取消");
//	if (r) {
//		// confirm true
//	} else {
//	    return;
//	}
//
//	var fromParam = $.urlParam("from");
//	var url = bcs.bcsContextPath +'/edit/msgListPage';
//
//	if(fromParam == 'msgListDraftPage'){
//		url = bcs.bcsContextPath +'/edit/msgListDraftPage';
//	}
//	else if(fromParam == 'msgListDelayPage'){
//		url = bcs.bcsContextPath +'/edit/msgListDelayPage';
//	}
//	else if(fromParam == 'msgListSendedPage'){
//		url = bcs.bcsContextPath +'/edit/msgListSendedPage';
//	}
//	else if(fromParam == 'msgListSchedulePage'){
//		url = bcs.bcsContextPath +'/edit/msgListSchedulePage';
//	}
//	window.location.replace(url);
//});
//document.getElementById('fileupload').addEventListener('change', function (e) {
//// Check for the various File API support.
//  if (window.FileReader) {
//	
//      // FileReader are supported.
//	  var files = e.target.files;
//	  //proceed your files here
//	  var reader = new FileReader();
//      // Read file into memory as UTF-8      
//      reader.readAsText(files[0]);
//      // Handle errors load
//      reader.onload = loadHandler; //將內容打印出來
//      reader.onerror = errorHandler;
//	  
//  } else {
//      alert('FileReader are not supported in this browser.');
//  }		
//}, false);
//// SendGroup: New Row Validation
//var setValidationOnNewRow = function() {
//	var tableBody = $('#tableBody');
//	var queryBody = tableBody.find('.dataTemplate:last');
//	
//	// 重新修改 element 的 name，避免多個 element 有重複的 name 而導致表單驗證錯誤的問題
//	var rowIndex = tableBody.prop('rowIndex') || 0;
//	rowIndex++;
//	queryBody.find('.queryField, .queryOp').each(function(index, element) {
//		var jqElement = $(this);
//		jqElement.attr("name", jqElement.attr("name") + rowIndex);
//	});
//	queryBody.find('.queryValue').each(function(index, element) {
//		var jqElement = $(this);
//		jqElement.attr("name", jqElement.attr("name") + rowIndex + '_' + (index + 1));
//	});
//	tableBody.prop('rowIndex', rowIndex);
//	
//	// 對新增的欄位加上表單驗證
//	// 群組條件-欄位
//	queryBody.find('.queryField').rules("add", {
//		required : true
//	});
//	
//	// 群組條件-條件
//	queryBody.find('.queryOp').rules("add", {
//		required : true
//	});
//	
//	// 群組條件-數值
//	queryBody.find('.queryValue').each(function(index, element) {
//		var queryValue = $(this);
//		// 日期元件
//		if(queryValue.is("input.datepicker")){
//			queryValue.rules("add", {
//				required : true,
//				dateYYYYMMDD : true
//			});
//		// 一般輸入框
//		}else if(queryValue.is("input")){
//			queryValue.rules("add", {
//				required : true,
//				maxlength : 50
//			});
//		// 下拉選單
//		}else{
//			queryValue.rules("add", {
//				required : true
//			});
//		}
//	});
//};
//	// [條件結果] Button
//	$('.btn_query').click(function(){
//		var postData = getSendGroupDetailFunc();
//		console.info('postData', postData);
//		if(!postData){
//			return;
//		}
//		
//		$.ajax({
//			type : "POST",
//			url : bcs.bcsContextPath + '/market/getSendGroupConditionResult',
//            cache: false,
//            contentType: 'application/json',
//            processData: false,
//			data : JSON.stringify(postData)
//		}).success(function(response){
//			console.info(response);
//			alert('查詢結果共 ' + response + ' 筆');
//		}).fail(function(response){
//			console.info(response);
//			$.FailResponse(response);
//		}).done(function(){
//		});
//	});
	// [取消] Button
//	$('.btn_cancel').click(function(){
//		var r = confirm("請確認是否取消");
//		if(!r){
//			return;
//		}
// 		window.location.replace(bcs.bcsContextPath + '/edit/linePointSendPage');
//	});
	// [加入條件] Button
//	$('.add_rule').click(function(){
//		var queryBody = templateBody.clone(true);
//		queryBody.find('.btn_delete').click(btn_deteleFunc);
//		queryBody.find('.optionSelect').change(optionSelectChange_func);
//		queryBody.find(".datepicker").datepicker({'dateFormat' : 'yy-mm-dd'});
//		$('#tableBody').append(queryBody);
//		setValidationOnNewRow();
//	});
	// [拉下選單]
//	var optionSelectChange_func = function(){
//		var select = $(this);
//		var selectValue = select.find('option:selected').text();
//		select.closest('.option').find('.optionLabel').html(selectValue);
//		
//		// 若是[欄位]下拉選單
//		if (select.hasClass('queryField')) {
//			setGroupQueryComponent(select);
//		}
//	};
//	/**
//	 * 選擇[欄位]要動態切換[條件]下拉選單的選項、[數值]元件
//	 * @param queryFieldSelect [欄位]下拉選單
//	 */
//	var setGroupQueryComponent = function(queryFieldSelect){		
//		if (!sendGroupCondition) {
//			return;
//		}
//		
//		// 包含[欄位]下拉選單的 <tr/>
//		var tr = queryFieldSelect.closest('tr');
//		
//		// [條件]下拉選單
//		var queryOpSelect = tr.find('.queryOp');
//		queryOpSelect.find('option[value!=""]').remove();
//		queryOpSelect.change();
//		
//		// [數值]元件
//		var queryValueComponent = tr.find('.queryValueComponent');
//		
//		// 移除表單驗證所加上的錯誤 css class
//		queryValueComponent.find('.queryValue').removeClass('error').next('label.error').remove();
//		
//		var queryValueComponentSelectList = queryValueComponent.find('.queryValueComponentSelectList');
//		queryValueComponentSelectList.hide().find('option[value!=""]').remove();
//		var queryValueComponentInput = queryValueComponent.find('.queryValueComponentInput');
//		queryValueComponentInput.hide().find(':text').val('');
//		var queryValueComponentDatepicker = queryValueComponent.find('.queryValueComponentDatepicker');
//		queryValueComponentDatepicker.hide().find(':text').val('');
//		
//		var queryFieldId = queryFieldSelect.val();
//		
//		if (!queryFieldId) {
//			return;
//		}
//		
//		// 設定[條件]下拉選單的選項
//		$.each(sendGroupCondition[queryFieldId].queryFieldOp, function(index, value) {
//			queryOpSelect.append('<option value="' + value + '">' + value + '</option>');
//		});
//		queryOpSelect.change();
//		
//		// 判斷要使用的[數值]元件
//		switch (sendGroupCondition[queryFieldId].queryFieldSet) {
//		case 'SelectList':
//			$.each(sendGroupCondition[queryFieldId].sendGroupQueryTag, function(index, sendGroupQueryTag) {
//				queryValueComponentSelectList
//					.find('select')
//					.append('<option value="' + sendGroupQueryTag.queryFieldTagValue + '">' 
//							+ sendGroupQueryTag.queryFieldTagDisplay + '</option>');
//			});
//			queryValueComponentSelectList.show().find('select').change();
//			break;
//		case 'Input':
//			queryValueComponentInput.show();
//			break;
//		case 'DatePicker':
//			queryValueComponentDatepicker.show();
//			break;
//		default:
//			break;
//		}
//	};
//	$('.SaveProjectBtn').click(function(){
//		$('#send_group_create').click();
//	});
	
//	var sendType = 'MANUAL';
//	
//	$(".sendType").click(function(e){
//		sendType = e.currentTarget.value;
//		console.info("selectedSendType:", sendType);
//		console.info("sendTimingType:", $('.sendTimingType')[0].checked);
//	});
    
	// 表單驗證
//	var sendGroupCreationValidator = $('#formSendGroup').validate({
//		rules : {
//			// 群組名稱
//			'groupTitle' : {
//				required : {
//			        param: true,
//			        depends: function(element) {
//						if(btnTarget == "btn_save"){
//							return true;
//						}
//						return false;
//			        }
//				},
//				maxlength : 50
//			},
//			
//			// 群組說明
//			'groupDescription' : {
//				required : {
//			        param: true,
//			        depends: function(element) {
//						if(btnTarget == "btn_save"){
//							return true;
//						}
//						return false;
//			        }
//				},
//				maxlength : 700
//			}
//		}
//	});

