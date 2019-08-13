/**
 *
 */
$(function(){

	$.BCS.actionTypeParam = $.urlParam("actionType");
	
	/**
	 * 紀錄 最後按鈕
	 */
	var btnTarget = "";
	var uploadList = "";
	
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
			}
		}
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
		var sendGroupId = 0;
		console.info('sendGroupId', sendGroupId);

		var serialId = "";
		console.info('serialId', serialId);


		// 訊息類別
		var msgTagList = [];
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

		var mainList = document.getElementById("mainList");
		var eventId = mainList.options[mainList.selectedIndex].value;
		if (eventId == "") {
			alert('請選擇活動');
			return;
		} 	
		
		if (uploadList.length == 0) {
			alert('請上傳發送清單');
			return;
		}
		
	
		var confirmStr = "請確認是否傳送";

		var r = confirm(confirmStr);
		if (r) {
			// confirm true
		} else {
		    return;
		}

		// 設定傳送資料
		//var postData = {};
//		postData.actionType = actionType;
		//postData.eventId = eventId;
		//postData.campaignCode = serialId;
		//postData.uids = uploadList;
		//postData.campaignName = 'test';
//		postData.modifyTime = MsgFrameContents;
		//postData.sendPoint = 88;
//		postData.sendPerson = "martin";
//		postData.sendedPerson = uploadList;   //將excel內的內容傳入。
//		postData.sendPerson = 3;
//		postData.sendedPerson = 4;  
//		postData.setUpUser = "aeg";
//		postData.campaignPersonNum = 5;
		
//		var msgAction = postData.actionType;
//
//		var msgId = $.urlParam("msgId");
//
//		if($.BCS.actionTypeParam == "Edit"){
//			postData.msgId = msgId;
//		}
		//console.info('postData: ', postData);

		var uids = uploadList;
		// 傳送資料  .stringify(postData)
	
		$('.LyMain').block($.BCS.blockMsgSave);
		$.ajax({
			type : "POST",
			url: bcs.bcsContextPath + '/market/pushLinePoint?eventId='+eventId,
            cache: false,
            contentType: 'application/json',
            processData: false,
            data: JSON.stringify(uids)
		}).success(
				
			function(response){
			console.info(response);
			alert('傳送成功');
			}).fail(function(response){
			 alert("fail");
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).error(function(response){
		    alert("error");
			console.info(response);
			
		}).done(function(){
			$('.LyMain').unblock();
		});
	};

	// 傳送
	$('.btn_save').click(function(){
		sendingMsgFunc("SendMsg");
	});

	// 傳送
	$('.btn_add').click(function(){
		sendingMsgFunc("SendMsg");
	});


	// 取消
	$('.btn_cancel').click(function(){

		var r = confirm("請確認是否取消");
		if (r) {
		window.location.reload(); //重新整理葉面
		} else {
		    return;
		}


	});

	
	var optionSelectChange_func = function(){
		var selectValue = $(this).find('option:selected').text();
		$(this).closest('.option').find('.optionLabel').html(selectValue);
	};

	$.BCS.ResourceMap = {};

	// 取得活動列表下拉選單 
	var loadDataFunc = function(){
		var serialId = "";

		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/market/getUndoneManualLinePointMainList'
		}).success(function(response){
			console.info('getLinePointList response:' + JSON.stringify(response));

			var mainList = document.getElementById("mainList");
			$.each(response, function(i, o){		
				console.info('getLinePointList o:' + JSON.stringify(o));
				 var opt = document.createElement('option');
				 opt.value = o.id;
				 opt.innerHTML = o.serialId + ' (' + o.title + ')';	
				mainList.appendChild(opt);
			});
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		var msgId = $.urlParam("msgId");
		var msgSendId = $.urlParam("msgSendId");

	};

	
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
    	var x = document.getElementById("sendList");
    	var c = document.getElementById("sendCount");
        var allTextLines = csv.split(/\r\n|\n/); //根據一行一行切開
        console.log(allTextLines);
        var num = 0;
        var removeList = []
        for (var i=0; i<allTextLines.length; i++) {
        	if (allTextLines[i].trim().length > 0) {
	        	var option = document.createElement("option");        	
	        	option.text = allTextLines[i];
	        	x.appendChild(option);
	        	num++;
        	} else 
        		removeList.push(i);
        }
        if (removeList.length > 0 ) {
	        removeList.reverse().forEach(function (index) {
	        	allTextLines.splice(index, 1)   //刪除第一行 uid
	        });
        }
        uploadList = allTextLines;
        // upload
        c.innerHTML = "共" + num + "筆"         
    }

    function errorHandler(evt) {
      if(evt.target.error.name == "NotReadableError") {
          alert("Canno't read file !");
      }
    }	
    /* File Upload and Parse CSV End */
    
	loadDataFunc();
});
