/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath + '/market/sendGroupCreatePage');
	});
	
	var btn_copyFunc = function(){
		var groupId = $(this).attr('groupId');
		console.info('btn_copyFunc groupId:' + groupId);
 		window.location.replace(bcs.bcsContextPath + '/market/sendGroupCreatePage?groupId=' + groupId + '&actionType=Copy');
	};
	
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
			url : bcs.bcsContextPath + '/admin/deleteSendGroup?groupId=' + groupId
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
			url : bcs.bcsContextPath + '/market/getSendGroupList'
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
	
			$.each(response, function(i, o){
				var groupData = templateBody.clone(true);

				groupData.find('.groupTitle a').attr('href', bcs.bcsContextPath + '/market/sendGroupCreatePage?groupId=' + o.groupId + '&actionType=Edit');
				groupData.find('.groupTitle a').html(o.groupTitle);
				groupData.find('.groupDescription').html(o.groupDescription);
				if(o.modifyTime){
					groupData.find('.modifyTime').html(moment(o.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
				}
				else{
					groupData.find('.modifyTime').html('-');
				}
				groupData.find('.modifyUser').html(o.modifyUser);
	
				groupData.find('.btn_copy').attr('groupId', o.groupId);
				groupData.find('.btn_copy').click(btn_copyFunc);
				
				if (bcs.user.admin) {
					groupData.find('.btn_detele').attr('groupId', o.groupId);
					groupData.find('.btn_detele').click(btn_deteleFunc);
				} else {
					groupData.find('.btn_detele').remove();
				}
				
				if(o.groupId < 0){
					groupData.find('.btn_copy').remove();
					groupData.find('.btn_detele').remove();
				}
	
				$('#tableBody').append(groupData);
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