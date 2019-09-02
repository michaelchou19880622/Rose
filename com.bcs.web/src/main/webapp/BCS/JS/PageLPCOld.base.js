$(function() {
	// Global Variables
    var uids = [], custIds = [], pts = [];
    var colMaxNum = 2;
	
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
	
	
	
    $('.btn_save').click(function() {
		var MsgFrameContents = $.BCS.getMsgFrameContent();
		console.info('MsgFrameContents', MsgFrameContents);
		if(MsgFrameContents.length > 4){
			alert('發送內容不能超過4個');
			return;
		}
		
        var title = $('#title').val();
        var pccCode = $('#pccCode').val();
        var serialId = $('#serialId').val();
        var sendTimingType = ($('.sendTimingType')[0].checked)?"IMMEDIATE":"SCHEDULE";
        var sendAmountType = ($('.sendAmountType')[0].checked)?"UNIVERSAL":"INDIVIDUAL";
        var amount = ($('.sendAmountType')[0].checked)?($('#amount').val()):0;

        console.info("va1:", sendAmountType);
        
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
        
//        if (!campaignName || !campaignCode || !sendPoint || !campaignPersonNum) {
//            alert("欄位不可為空");
//            return;
//        }
//
//        var postData = {};
//        postData.title = campaignName;
//        postData.serialId = campaignCode;
//        postData.amount = sendPoint;
//        postData.totalCount = campaignPersonNum;
//        postData.sendType = sendType;
//        postData.status = "IDLE";
//        postData.successfulCount = 0;
//        postData.failedCount = 0;
//        
//        console.info('postData', postData);

        /*
        $.ajax({
            type: "POST",
            url: bcs.bcsContextPath + '/market/createLinePointMain',
            cache: false,
            contentType: 'application/json',
            processData: false,
            data: JSON.stringify(postData)

        }).success(
            function(response) {
                console.info(response);
                alert('儲存成功');
                window.location.replace(bcs.bcsContextPath + '/market/linePointListPage');
            }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
        })
        */
    });
    
	/* File Upload and Parse CSV Begin */
	document.getElementById('fileupload').addEventListener('change', function (e) {
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
	}, false);
	
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
        if(colMaxNum == 2){
        	var amount = parseInt($('#amount').val());
        	if(isNaN(amount) || amount <= 0){
        		alert('發送數量必須大於零');
        		window.location.replace(bcs.bcsContextPath + '/edit/linePointCreatePage');
        	}
        	fileInformation.innerHTML = '本次共發送' + rows.length + '筆，合計發送點數為' + rows.length * amount +'點';
        }else{
        	var sum = 0;
            for (var i = 0; i < pts.length; i++) {
            	sum += pts[i];
            }
            fileInformation.innerHTML = '本次共發送' + rows.length + '筆，合計發送點數為' + sum +'點';
        }
    }

    function errorHandler(evt) {
      if(evt.target.error.name == "NotReadableError") {
          alert("Canno't read file !");
      }
    }
    /* File Upload and Parse CSV End */
    
//	var sendType = 'MANUAL';
//	
//	$(".sendType").click(function(e){
//		sendType = e.currentTarget.value;
//		console.info("selectedSendType:", sendType);
//		console.info("sendTimingType:", $('.sendTimingType')[0].checked);
//	});
});