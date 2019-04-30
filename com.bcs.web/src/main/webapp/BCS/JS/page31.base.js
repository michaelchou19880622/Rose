/**
 * 
 */
$(function(){
	
	// 表單驗證
	var validator = $('#formProduct').validate({
		rules : {
			
			// 商品名稱
			'productName' : {
				required : {
			        param: true
				},
				maxlength : 100
			}
		}
	});
		
	$('.btn_save').click(function(){
		if (!validator.form()) {
			return;
		}
	
		var productName = $('#productName').val();
		console.info('productName', productName);
		var productId = $.urlParam("productId");
		console.info('productId', productId);
		var actionType = $.urlParam("actionType");
		console.info('actionType', actionType);
		var groupId = $.urlParam("groupId");
		console.info('groupId', groupId);
		
		var postData = {};
		if (productId) {
			postData.productId = productId;
		}
		if (groupId) {
			postData.groupId = groupId;
		}
		postData.productName = productName;
		
		console.info('postData', postData);

		/**
		 * Do Confirm Check
		 */
		var confirmStr = "請確認是否建立";
		if(productId && actionType == 'Edit'){
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
			url : bcs.bcsContextPath + '/admin/createProduct',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			alert( '儲存成功');
 			window.location.replace(bcs.bcsContextPath + '/admin/productGroupCreatePage?actionType=Edit&groupId=' + groupId);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	});
	$('.btn_upload_product').click(function(){
		$('#upload_product').click();
	});
	
	$('#upload_product').on("change", function(ev){

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
                url: bcs.bcsContextPath + '/admin/uploadProductList',
                cache: false,
                contentType: false,
                processData: false,
                data: form_data
    		}).success(function(response){
            	console.info(response);
            	alert("匯入成功!");
 				window.location.replace(bcs.bcsContextPath + '/admin/productGroupCreatePage?actionType=Edit&groupId=' + groupId);
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
 		window.location.replace(bcs.bcsContextPath + '/admin/productGroupCreatePage?actionType=Edit&groupId=' + groupId);
	});

	var loadDataFunc = function(){
		var productId = $.urlParam("productId");
			
		if(productId){
			
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/admin/getProduct?productId=' + productId
			}).success(function(response){
				console.info(response);

				$('#productName').val(response.productName);
				
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