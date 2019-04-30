/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath + '/market/adminUserCreatePage?actionType=adminCreate');
	});
		
	var btn_deteleFunc = function(){
		var account = $(this).attr('account');
		console.info('btn_deteleFunc account:' + account);

		if (!confirm('請確認是否刪除')) {
			return false;
		}
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/admin/deleteAdminUserForAdmin?account=' + account
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

	var loadDataFunc = function(){
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/getAdminUserList'
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
			
			var userAccount = bcs.user.account; // 登入者帳號
			
			$.each(response.adminUserList, function(i, o){
				var accountData = templateBody.clone(true);
				var role = '';
				console.log(o);
				accountData.find('.account a')
					.attr('href', bcs.bcsContextPath + '/market/adminUserCreatePage?account=' + o.account + '&actionType=adminEdit')
					.html(o.account);
				accountData.find('.userName').html(o.userName);
				
				if(o.role == 'ROLE_ADMIN')
					role='管理者';
				else if(o.role == 'ROLE_EDIT')
					role='行銷人員';
				else if(o.role == 'ROLE_MARKET')
					role='編輯人員';
				accountData.find('.role').html(role);
				
				accountData.find('.mid').html(o.mid ? '已綁訂' : '未綁訂');
				accountData.find('.modifyTime').html(moment(o.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
				accountData.find('.modifyUser').html(o.modifyUser);
				
				if (userAccount != o.account) {
					accountData.find('.btn_detele').attr('account', o.account);
					accountData.find('.btn_detele').click(btn_deteleFunc);
				} else {
					accountData.find('.btn_detele').remove();
				}
				
				$('#tableBody').append(accountData);
			});
			
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};

	var templateBody = {};
	templateBody = $('.dataTemplate').clone(true);
	$('.dataTemplate').remove();
	
	loadDataFunc();
});