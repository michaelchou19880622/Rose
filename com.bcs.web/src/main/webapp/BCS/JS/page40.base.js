/**
 * 
 */
$(function(){
	
	// 表單驗證
	var validator = $('#formBusiness').validate({
		rules : {
			
			// 通路統編
			'bizId' : {
				required : {
			        param: true
				},
				maxlength : 8,
				minlength : 8
			}
		}
	});
		
	$('.btn_save').click(function(){
		if (!validator.form()) {
			return;
		}
	
		var bizId = $('#bizId').val();
		console.info('bizId', bizId);
		var id = $.urlParam("id");
		console.info('id', id);
		var actionType = $.urlParam("actionType");
		console.info('actionType', actionType);
		var groupId = $.urlParam("groupId");
		console.info('groupId', groupId);
		
		var postData = {};
		if (id) {
			postData.id = id;
		}
		if (groupId) {
			postData.groupId = groupId;
		}
		postData.bizId = bizId;
		
		console.info('postData', postData);

		/**
		 * Do Confirm Check
		 */
		var confirmStr = "請確認是否建立";
		if(id && actionType == 'Edit'){
			confirmStr = "請確認是否儲存";
		}

		var r = confirm(confirmStr);
		if (r) {
			// confirm true
		} else {
		    return;
		}

		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/admin/createBusiness',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			alert( '儲存成功');
 			window.location.replace(bcs.bcsContextPath + '/admin/businessGroupCreatePage?actionType=Edit&groupId=' + groupId);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	});
	$('.btn_upload_business').click(function(){
		$('#upload_business').click();
	});
	
	$('#upload_business').on("change", function(ev){

		var input = ev.currentTarget;
    	if (input.files && input.files[0]) {
    		var fileName = input.files[0].name;
    		console.info("fileName : " + fileName);
			var groupId = $.urlParam("groupId");
			console.info('groupId', groupId);
    		var form_data = new FormData();
    		
    		form_data.append("filePart",input.files[0]);
    		form_data.append("groupId", groupId);

    		$('.LyMain').block($.BCS.blockMsgUpload);
    		$.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + '/admin/uploadBusinessList',
                cache: false,
                contentType: false,
                processData: false,
                data: form_data
    		}).success(function(response){
            	console.info(response);
            	alert("匯入成功!");
 				window.location.replace(bcs.bcsContextPath + '/admin/businessGroupCreatePage?actionType=Edit&groupId=' + groupId);
    		}).fail(function(response){
    			console.info(response);
    			$.FailResponse(response);
    			$('.LyMain').unblock();
    		}).done(function(){
    			$('.LyMain').unblock();
    		});
        } 
	});
	
	$('.btn_cancel').click(function(){
		
		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		var groupId = $.urlParam("groupId");
 		window.location.replace(bcs.bcsContextPath + '/admin/businessGroupCreatePage?actionType=Edit&groupId=' + groupId);
	});

	var loadDataFunc = function(){
		var id = $.urlParam("id");
			
		if(id){
			
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/admin/getBusiness?id=' + id
			}).success(function(response){
				console.info(response);

				$('#bizId').val(response.bizId);
				
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
			
			var actionType = $.urlParam("actionType");
			
			if(actionType == "Edit"){
				$('.CHTtl').html('編輯商品');
			}
		}
	};

	loadDataFunc();
});