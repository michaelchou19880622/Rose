$(function(){
	
	//form驗證
	var validator = $('#formBusinessGroup').validate({
		rules : {
			
			// 群組名稱
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
			url : bcs.bcsContextPath + '/admin/createBusinessGroup',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			alert( '儲存成功');
	 		window.location.replace(bcs.bcsContextPath + '/admin/businessGroupListPage');
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
		
 		window.location.replace(bcs.bcsContextPath + '/admin/businessGroupListPage');
	});
	
	var loadDataFunc = function(){
		var groupId = $.urlParam("groupId");
		var actionType = $.urlParam("actionType");
			
		if(groupId){
			
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/admin/getBusinessGroup?groupId=' + groupId
			}).success(function(response){
				console.info(response);

				$('#groupName').val(response.groupName);
				
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
			
			
			if(actionType == "Edit"){
				$('.CHTtl').html('編輯通路群組');
			}

		
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/admin/getBusinessList?groupId=' + groupId
			}).success(function(response){
				$('.dataTemplate').remove();
				console.info(response);
		
				$.each(response, function(i, o){
					var businessData = templateBody.clone(true);

					businessData.find('.bizId a').attr('href', bcs.bcsContextPath + '/admin/businessCreatePage?id=' + o.id + '&actionType=Edit&groupId=' + groupId);
					businessData.find('.bizId a').html(o.bizId);
					if(o.modifyTime){
						businessData.find('.modifyTime').html(moment(o.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
					}
					else{
						businessData.find('.modifyTime').html('-');
					}
					businessData.find('.modifyUser').html(o.modifyUser);
					
					if (bcs.user.admin) {
						businessData.find('.btn_detele').attr('id', o.id);
						businessData.find('.btn_detele').click(btn_deteleFunc);
					} else {
						businessData.find('.btn_detele').remove();
					}
		
					$('#tableBody').append(businessData);
				});
				
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});

			$('.btn_add').click(function(){
		 		window.location.replace(bcs.bcsContextPath + '/admin/businessCreatePage?groupId=' + groupId);
			});
			
			var btn_deteleFunc = function(){
				var id = $(this).attr('id');
				console.info('btn_deteleFunc id:' + id);

				var r = confirm("請確認是否刪除");
				if (r) {
					
				} else {
				    return;
				}
				
				$.ajax({
					type : "DELETE",
					url : bcs.bcsContextPath + '/admin/deleteBusiness?id=' + id
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
			$('#businessListBody').remove();
		}
	};

	var templateBody = {};
	templateBody = $('.dataTemplate').clone(true);
	$('.dataTemplate').remove();

	loadDataFunc();
});