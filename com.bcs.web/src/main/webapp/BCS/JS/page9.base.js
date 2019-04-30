/**
 * 
 */
$(function(){
	var actionType = $.urlParam("actionType") || 'adminCreate';
	console.info('actionType', actionType);
	
	// 表單驗證
	var validator = $('#formAdminUser').validate({
		rules : {
			
			// 管理權限
			'role' : {
				required : true
			},
			
			// 管理帳號
			'account' : {
				required : true,
				alphanumeric : true, // 只接受字母，數字和下劃線
				maxlength : 50
			},
			
			// 帳號名稱
			'userName' : {
				required : true,
				maxlength : 50
			},
			
			// 管理密碼
			'password' : {
				required : function(element) {
					return actionType == 'adminCreate'; // 新增時密碼必填，修改時可不填
				},
				minlength : 8,
				maxlength : 200
			},
			
			// 確認密碼
			'confirmPassword' : {
				equalTo : "#password"
			}
		}
	});
	
	var optionSelectChange_func = function(){
		var selectValue = $(this).find('option:selected').text();
		$(this).closest('.option').find('.optionLabel').html(selectValue);
	};
	
	$('.optionSelect').change(optionSelectChange_func);
	
	// 取消綁訂按鍵
	$('#cancelBindLineAccount').click(function(){
		
		var r = confirm("請確認是否取消綁定");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		var account = $('#account').val();
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/market/cancelBindLineAccount?account=' + account,
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify({})
		}).success(function(response){
			console.info(response);
			alert('取消綁訂成功');
	 		window.location.reload();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	});
	
	// 儲存按鍵
	$('.btn_save').click(function(){
		if (!validator.form()) {
			return;
		}
		
		var role = $('#role').val();
		var account = $('#account').val();
		var userName = $('#userName').val();
		var password = $('#password').val();
		var confirmPassword = $('#confirmPassword').val();
		var lineAccount = $('#lineAccount').val();
		
		var postData = {
			role : role,
			account : account,
			userName : userName,
			password : password,
			mid : lineAccount
		};
		
		console.info('postData', postData);
				
		switch (actionType) {
		case 'adminCreate':
			if (!confirm('請確認是否建立')) {
				return false;
			}
			
			var inputLineAccount = (lineAccount ? 'true' : 'false');
			$.ajax({
				type : "POST",
				url : bcs.bcsContextPath + '/admin/createAdminUserForAdmin?inputLineAccount=' + inputLineAccount,
	            cache: false,
	            contentType: 'application/json',
	            processData: false,
				data : JSON.stringify(postData)
			}).success(function(response){
				console.info(response);
				alert('建立成功');
		 		window.location.replace(bcs.bcsContextPath + '/admin/adminUserListPage');
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
			break;
		case 'adminEdit':
			if (!confirm('請確認是否儲存')) {
				return false;
			}
			
			var inputLineAccount = (lineAccount ? 'true' : 'false');
			$.ajax({
				type : "POST",
				url : bcs.bcsContextPath + '/admin/saveAdminUserForAdmin?inputLineAccount=' + inputLineAccount,
	            cache: false,
	            contentType: 'application/json',
	            processData: false,
				data : JSON.stringify(postData)
			}).success(function(response){
				console.info(response);
				alert('儲存成功');
		 		window.location.replace(bcs.bcsContextPath + '/admin/adminUserListPage');
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
			break;
		case 'userEdit':
			if (!confirm('請確認是否儲存')) {
				return false;
			}
			
			var inputLineAccount = (lineAccount ? 'true' : 'false');
			$.ajax({
				type : "POST",
				url : bcs.bcsContextPath + '/market/saveAdminUserForUser?inputLineAccount=' + inputLineAccount,
	            cache: false,
	            contentType: 'application/json',
	            processData: false,
				data : JSON.stringify(postData)
			}).success(function(response){
				console.info(response);
				alert('儲存成功');
				window.location.reload();
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
	
	// 取消按鍵
	$('#cancel').click(function(){

		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		// 若是一般使用者
		if (actionType == 'userEdit') {
			window.location.replace(bcs.bcsContextPath + '/index');
			
		// 若是 Admin
		} else {
			window.location.replace(bcs.bcsContextPath + '/admin/adminUserListPage');
		}
	});
	
	var loadDataFunc = function(){
		var account = $.urlParam("account");
		
		switch (actionType) {
		case 'adminCreate':
			$('#fromTitle').text('建立管理權限者');
			break;
		case 'adminEdit':
			$('#fromTitle').text('更改管理權限者');
			$('#account').prop('disabled', true);
			break;
		case 'userEdit':
			$('#fromTitle').text('更改管理權限者');
			$('#account').prop('disabled', true);
			$('#role').prop('disabled', true);
			break;
		default:
			break;
		}
		
		if(account){
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/market/getAdminUser?account=' + account
			}).success(function(response){
				console.info(response);

				$('#role').val(response.role).trigger('change');
				$('#account').val(response.account);
				$('#userName').val(response.userName);
				
				if (response.mid) {
					$('#noBindLineAccount').hide();
					$('#hasBindLineAccount').show();
				} else {
					$('#noBindLineAccount').show();
					$('#hasBindLineAccount').hide();
				}
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
		}
	};
	
	loadDataFunc();
});