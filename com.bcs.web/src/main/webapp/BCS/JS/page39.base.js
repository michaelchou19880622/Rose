/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath + '/admin/businessGroupCreatePage');
	});
	
	var btn_deteleFunc = function(){
		var groupId = $(this).attr('groupId');
		console.info('btn_deteleFunc groupId:' + groupId);

		var r = confirm("請確認是否刪除");
		if (r) {
			
		} else {
		    return;
		}
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/admin/deleteBusinessGroup?groupId=' + groupId
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
			url : bcs.bcsContextPath + '/admin/getBusinessGroupList'
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
	
			$.each(response, function(i, o){
				var productData = templateBody.clone(true);

				productData.find('.groupName a').attr('href', bcs.bcsContextPath + '/admin/businessGroupCreatePage?groupId=' + o.groupId + '&actionType=Edit');
				productData.find('.groupName a').html(o.groupName);
				if(o.modifyTime){
					productData.find('.modifyTime').html(moment(o.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
				}
				else{
					productData.find('.modifyTime').html('-');
				}
				productData.find('.modifyUser').html(o.modifyUser);
				
				if (bcs.user.admin) {
					productData.find('.btn_detele').attr('groupId', o.groupId);
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
	};

	var templateBody = {};
	templateBody = $('.dataTemplate').clone(true);
	$('.dataTemplate').remove();
	
	loadDataFunc();
});