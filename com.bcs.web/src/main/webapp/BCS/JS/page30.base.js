/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath + '/admin/campaignCreatePage');
	});
	$('.ActiveBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/admin/campaignListPage?isActive=true');
	});
	$('.DisableBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/admin/campaignListPage?isActive=false');
	});
	
	var btn_deteleFunc = function(){
		var campaignId = $(this).attr('campaignId');
		console.info('btn_deteleFunc campaignId:' + campaignId);

		var r = confirm("請確認是否刪除");
		if (r) {
			
		} else {
		    return;
		}
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/admin/deleteCampaign?campaignId=' + campaignId
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

	// 改變狀態按鈕
	var redesignFunc = function(){
		var campaignId = $(this).attr('campaignId');
		console.info('redesignFunc campaignId:' + campaignId);
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/admin/redesignCampaign?campaignId=' + campaignId
		}).success(function(response){
			console.info(response);
			alert("改變成功");
			loadDataFunc();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};

	var loadDataFunc = function(){
		var isActive = ($.urlParam("isActive") == undefined) ? true : $.urlParam("isActive");
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/getCampaignList?isActive=' + isActive
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
	
			$.each(response, function(i, o){
				var campaignData = templateBody.clone(true);

				campaignData.find('.campaignName a').attr('href', bcs.bcsContextPath + '/admin/campaignCreatePage?campaignId=' + o.campaignId + '&actionType=admin');
				campaignData.find('.campaignName a').html(o.campaignName);
				var time = '';
				if (o.startTime) {
					time += moment(o.startTime).format('YYYY-MM-DD HH:mm:ss')
				}
				time += ' - ';
				if (o.endTime) {
					time += moment(o.endTime).format('YYYY-MM-DD HH:mm:ss')
				}
				campaignData.find('.time').html(time);
				campaignData.find('.price').html(o.price);
				if (isActive == 'false') {
					campaignData.find('.isActive>span').html('取消');
					campaignData.find('.btn_redeisgn').val('生效');
				} else {
					campaignData.find('.isActive>span').html('生效');
					campaignData.find('.btn_redeisgn').val('取消');
				}
				campaignData.find('.btn_redeisgn')
								.attr('campaignId', o.campaignId)
								.click(redesignFunc);
				if(o.modifyTime){
					campaignData.find('.modifyTime').html(moment(o.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
				} else {
					campaignData.find('.modifyTime').html('-');
				}
				campaignData.find('.modifyUser').html(o.modifyUser);
				
				if (bcs.user.admin) {
					campaignData.find('.btn_detele').attr('campaignId', o.campaignId);
					campaignData.find('.btn_detele').click(btn_deteleFunc);
				} else {
					campaignData.find('.btn_detele').remove();
				}
	
				$('#tableBody').append(campaignData);
			});
			
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});

		if (isActive == 'false') {
			$('#inactive_li').addClass('ExSelected');
		} else {
			$('#active_li').addClass('ExSelected');
		}
	};

	var templateBody = {};
	templateBody = $('.dataTemplate').clone(true);
	$('.dataTemplate').remove();
	
	loadDataFunc();
});