/**
 * 
 */
$(function(){
	
	$.BCS.actionTypeParam = $.urlParam("actionType");

	/**
	 * 紀錄 最後按鈕
	 */
	var btnTarget = "";
	
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
		
		// 參數
		var configId = $('#configId').val();
		console.info('configId', configId);
		
		// 說明
		var description = $('#description').val();
		console.info('description', description);
		
		// 數值
		var value = $('#value').val();
		console.info('value', value);
		
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
		postData.configId = configId;
		postData.description = description;
		postData.value = value;

		var configId = $.urlParam("configId");
		
		if($.BCS.actionTypeParam == "Edit"){
			postData.configId = configId;
		}
		console.info('postData', postData);

		// 傳送資料
		$('.LyMain').block($.BCS.blockMsgSave);
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath +'/admin/settingConfig',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			
			alert('設定成功');
			var url = bcs.bcsContextPath +'/admin/configListPage';
			
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
		var url = bcs.bcsContextPath +'/admin/configListPage';
		
		window.location.replace(url);
	}
	
	$('.CancelBtn').click(cancelFunc);

	// 取消
	$('.btn_cancel').click(cancelFunc);
	
	// 取得設定資料
	var loadDataFunc = function(){

		var configId = $.urlParam("configId");
		
		/**
		 * Load Config Data
		 */
		if(configId){
			
			var getDataUrl = bcs.bcsContextPath +'/admin/getConfig?configId=' + configId;

    		$('.LyMain').block($.BCS.blockMsgRead);
			$.ajax({
				type : "GET",
				url : getDataUrl
			}).success(function(response){
				
				$('#configId').val(response.configId);
				$('#description').val(response.description);
				$('#value').val(response.value);
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
    			$('.LyMain').unblock();
			}).done(function(){
    			$('.LyMain').unblock();
			});
		}
	};
	
	loadDataFunc();
});