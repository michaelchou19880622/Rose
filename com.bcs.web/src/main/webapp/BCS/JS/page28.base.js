/**
 *
 */
$(function(){
	var SerialId = $.urlParam("SerialId");

	var loadDataFunc = function(){

		if(SerialId){
			$('.LyMain').block($.BCS.blockMsgUpload);
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/edit/getSerialSetting?SerialId=' + SerialId
			}).success(function(response){
				console.info(response);
				
				$('.settingTime').html(response.settingTime);
				$('.settingFile').html(response.settingFile);
				$('.settingCount').html(response.settingCount);
				
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
				$('.LyMain').unblock();
			}).done(function(){
				$('.LyMain').unblock();
			});
		}
	};
	
	var deleteSerialSetting = function(){

		if(SerialId){
			var r = confirm("請確認是否刪除!");
			if (r) {
				// confirm true
			} else {
			    return;
			}
	
			$('.LyMain').block($.BCS.blockMsgUpload);
			$.ajax({
	            type: 'POST',
	            url: bcs.bcsContextPath + '/admin/deleteSerialSetting?SerialId=' + SerialId,
	            cache: false,
	            contentType: false,
	            processData: false
			}).success(function(response){
	        	console.info(response);
	        	loadDataFunc();
	        	alert("刪除成功! 比數 : " + response.deleteCount);
			}).fail(function(response){
				console.info(response);
	        	loadDataFunc();
				$.FailResponse(response);
				$('.LyMain').unblock();
			}).done(function(){
				$('.LyMain').unblock();
			});
		}
		else{
			alert('沒有資料');
		}
	};
	
	var settingFunc = function(){

		var r = confirm("請確認是否設定,會覆蓋掉所有設定!");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		$('#upload_Serial_btn').click();
	};

	$('.btn_draft').click(deleteSerialSetting);
	$('.btn_save').click(settingFunc);
	$('.btn_add').click(settingFunc);
	
	$('#upload_Serial_btn').on("change", function(ev){
		
		var url = bcs.bcsContextPath + '/edit/uploadSerialSetting';
		if(SerialId){
			url += '?SerialId=' + SerialId;
		}
		
		var input = ev.currentTarget;
    	if (input.files && input.files[0]) {
    		var fileName = input.files[0].name;
    		console.info("fileName : " + fileName);
    		var form_data = new FormData();
    		
    		form_data.append("filePart",input.files[0]);

    		$('.LyMain').block($.BCS.blockMsgUpload);
    		$.ajax({
                type: 'POST',
                url: url,
                cache: false,
                contentType: false,
                processData: false,
                data: form_data
    		}).success(function(response){
            	console.info(response);
            	SerialId = response.SerialId;
            	loadDataFunc();
            	alert("匯入成功! 比數 : " + response.count);
    		}).fail(function(response){
    			console.info(response);
            	loadDataFunc();
    			$.FailResponse(response);
    			$('.LyMain').unblock();
    		}).done(function(){
    			$('.LyMain').unblock();
    		});
        } 
	});
	
	loadDataFunc();
});
