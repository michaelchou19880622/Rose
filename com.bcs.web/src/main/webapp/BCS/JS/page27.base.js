/**
 *
 */
$(function(){

	var loadDataFunc = function(){

		$('.LyMain').block($.BCS.blockMsgUpload);
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getMgmSetting'
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
	};
	
	var deleteMgmSetting = function(){

		var r = confirm("請確認是否刪除!");
		if (r) {
			// confirm true
		} else {
		    return;
		}

		$('.LyMain').block($.BCS.blockMsgUpload);
		$.ajax({
            type: 'POST',
            url: bcs.bcsContextPath + '/admin/deleteMgmSetting',
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
	};
	
	var settingFunc = function(){

		var r = confirm("請確認是否設定,會覆蓋掉所有設定!");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		$('#upload_mid_btn').click();
	};

	$('.btn_draft').click(deleteMgmSetting);
	$('.btn_save').click(settingFunc);
	$('.btn_add').click(settingFunc);
	
	$('#upload_mid_btn').on("change", function(ev){

		var input = ev.currentTarget;
    	if (input.files && input.files[0]) {
    		var fileName = input.files[0].name;
    		console.info("fileName : " + fileName);
    		var form_data = new FormData();
    		
    		form_data.append("filePart",input.files[0]);

    		$('.LyMain').block($.BCS.blockMsgUpload);
    		$.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + '/edit/uploadMgmSetting',
                cache: false,
                contentType: false,
                processData: false,
                data: form_data
    		}).success(function(response){
            	console.info(response);
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
