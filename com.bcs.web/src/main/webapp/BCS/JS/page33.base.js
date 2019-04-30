/**
 * 
 */
$(function(){
	
	// 表單驗證
	var validator = $('#formProductGroup').validate({
		rules : {
			
			// 商品名稱
			'groupName' : {
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
	
		var groupName = $('#groupName').val();
		console.info('groupName', groupName);
		var groupId = $.urlParam("groupId");
		console.info('groupId', groupId);
		var actionType = $.urlParam("actionType");
		console.info('actionType', actionType);
		
		var postData = {};
		if (groupId) {
			postData.groupId = groupId;
		}
		postData.groupName = groupName;
		
		console.info('postData', postData);

		/**
		 * Do Confirm Check
		 */
		var confirmStr = "請確認是否建立";
		if(groupId && actionType == 'Edit'){
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
			url : bcs.bcsContextPath + '/admin/createProductGroup',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			alert( '儲存成功');
	 		window.location.replace(bcs.bcsContextPath + '/admin/productGroupListPage');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	});
	
	$('.btn_cancel').click(function(){
		
		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
 		window.location.replace(bcs.bcsContextPath + '/admin/productGroupListPage');
	});

	var loadDataFunc = function(){
		var groupId = $.urlParam("groupId");
		var actionType = $.urlParam("actionType");
			
		if(groupId){
			
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/admin/getProductGroup?groupId=' + groupId
			}).success(function(response){
				console.info(response);

				$('#groupName').val(response.groupName);
				
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
			
			
			if(actionType == "Edit"){
				$('.CHTtl').html('編輯商品組合');
			}

		
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/admin/getProductList?groupId=' + groupId
			}).success(function(response){
				$('.dataTemplate').remove();
				console.info(response);
		
				$.each(response, function(i, o){
					var productData = templateBody.clone(true);

					productData.find('.productName a').attr('href', bcs.bcsContextPath + '/admin/productCreatePage?productId=' + o.productId + '&actionType=Edit&groupId=' + groupId);
					productData.find('.productName a').html(o.productName);
					if(o.modifyTime){
						productData.find('.modifyTime').html(moment(o.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
					}
					else{
						productData.find('.modifyTime').html('-');
					}
					productData.find('.modifyUser').html(o.modifyUser);
					
					if (bcs.user.admin) {
						productData.find('.btn_detele').attr('productId', o.productId);
						productData.find('.btn_detele').click(btn_deteleFunc);
					} else {
						productData.find('.btn_detele').remove();
					}
		
					$('#tableBody').append(productData);
				});
				
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});

			$('.btn_add').click(function(){
		 		window.location.replace(bcs.bcsContextPath + '/admin/productCreatePage?groupId=' + groupId);
			});
			
			var btn_deteleFunc = function(){
				var productId = $(this).attr('productId');
				console.info('btn_deteleFunc productId:' + productId);

				var r = confirm("請確認是否刪除");
				if (r) {
					
				} else {
				    return;
				}
				
				$.ajax({
					type : "DELETE",
					url : bcs.bcsContextPath + '/admin/deleteProduct?productId=' + productId
				}).success(function(response){
					console.info(response);
					alert("刪除成功");
					loadDataFunc();
				}).fail(function(response){
					console.info(response);
					$.FailResponse(response);
				}).done(function(){
				});
			};

		} else {
			$('#productListBody').remove();
		}
	};

	var templateBody = {};
	templateBody = $('.dataTemplate').clone(true);
	$('.dataTemplate').remove();

	loadDataFunc();
});