/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace('configCreatePage');
	});

	var loadDataFunc = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/getConfigList'
		}).success(function(response){
			$('.configTrTemplate').remove();

			$.each(response, function(i, o){
				var configTr = configTrTemplate.clone(true);

				configTr.find('.configId').attr('configId', o.configId);
				configTr.find('.configId a').attr('href', bcs.bcsContextPath + '/admin/configCreatePage?configId=' + o.configId + '&actionType=Edit');
				configTr.find('.configId a').html(o.configId);
				configTr.find('.description').html(o.description);
				configTr.find('.value').html(o.value);

				$('#configListTable').append(configTr);
			});
			
			setDeleteBtnEvent();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};
	
	var setDeleteBtnEvent = function() {
		$('.btn_detele').click(function(e) {
			var deleteConfirm = confirm("請確認是否刪除");
			if (!deleteConfirm) return; //點擊取消
			
			var configTr = $(this).closest(".configTrTemplate");
			var configId = configTr.find('.configId').attr('configId');
			$.ajax({
				type : "DELETE",
				url : bcs.bcsContextPath + '/admin/deleteConfig/' + configId
			}).success(function(response){
				alert("刪除成功！");
				loadDataFunc();
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
		});
		
		$('.btn_copy').click(function(e) {
			var configTr = $(this).closest(".configTrTemplate");
			var configId = configTr.find('.configId').attr('configId');
			window.location.replace(bcs.bcsContextPath + '/admin/configCreatePage?configId=' + configId + '&actionType=Copy');
		});
	}

	var configTrTemplate = {};
	var initTemplate = function(){
		configTrTemplate = $('.configTrTemplate').clone(true);
		$('.configTrTemplate').remove();
	}

	initTemplate();
	loadDataFunc();
});