/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath + '/edit/shareCampaignCreatePage?actionType=Create&from=active');
	});
	$('.ActiveBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/shareCampaignListPage');
	});
	$('.DisableBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/shareCampaignListDisablePage');
	});
	
	var btn_copyFunc = function(){
		var campaignId = $(this).attr('campaignId');
		console.info('btn_copyFunc campaignId:' + campaignId);
 		window.location.replace(bcs.bcsContextPath + '/edit/shareCampaignCreatePage?campaignId=' + campaignId + '&actionType=Copy&from=active');
	};
	
	var btn_deteleFunc = function(){
		var campaignId = $(this).attr('campaignId');
		console.info('btn_deteleFunc campaignId:' + campaignId);

		if (!confirm('請確認是否刪除')) {
			return false;
		}
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/admin/deleteShareCampaign?campaignId=' + campaignId
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

		if (!confirm('請確認是否取消')) {
			return false;
		}
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/edit/redesignShareCampaign?campaignId=' + campaignId
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
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getShareCampaignList'
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
						
			$.each(response, function(i, o){
				var queryBody = templateBody.clone(true);

				var msgContent = "";

//				msgContent += '<img src="' + bcs.bcsContextPath + '/getResource/IMAGE/' + o.campaignListImageId + '" alt="Type2" style="cursor: pointer; width:100px"><br>';
//				msgContent += '<img src="' + bcs.bcsContextPath + '/getResource/IMAGE/' + o.campaignImageId + '" alt="Type2" style="cursor: pointer; width:100px"><br>';
				msgContent += o.campaignName;
				
				queryBody.find('.campaignTitle a')
					.attr('href', bcs.bcsContextPath + '/edit/shareCampaignCreatePage?campaignId=' + o.campaignId + '&actionType=Edit&from=active')
					.html(msgContent);
				
				queryBody.find('.campaignTime').html(
						moment(o.startTime).format('YYYY-MM-DD HH:mm:ss') 
						+ '<br> ~ ' 
						+ moment(o.endTime).format('YYYY-MM-DD HH:mm:ss'));
				
				queryBody.find('.modifyUser').html(moment(o.modifyTime).format('YYYY-MM-DD HH:mm:ss') + "<br>" + o.modifyUser);

				queryBody.find('.status span').html($.BCS.parseInteractiveStatus(o.status));
				
				queryBody.find('.campaignShareNumber a').attr('href', bcs.bcsContextPath +'/edit/shareCampaignReportPage?campaignId=' + o.campaignId);					

				countUserRecord(queryBody, o.campaignId);

//				queryBody.find('.campaignClickNumber a').attr('href', bcs.bcsContextPath +'/edit/couponReportPage?couponId=' + o.couponId);
//				queryBody.find('.campaignClickNumber a').html($.BCS.formatNumber(o.couponUsingNumber,0));

				queryBody.find('.btn_redeisgn').attr('campaignId', o.campaignId);
				queryBody.find('.btn_redeisgn').click(redesignFunc);
				
				queryBody.find('.btn_copy')
					.attr('campaignId', o.campaignId)
					.click(btn_copyFunc);
				
				queryBody.find('.btn_detele')
					.attr('campaignId', o.campaignId)
					.click(btn_deteleFunc);
				
				queryBody.find('.btn_css')
				.attr('campaignId', o.campaignId)
				.attr('campaignName', o.campaignName)
				.click(dialogFunc);
				
				$('#tableBody').append(queryBody);
			});
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};

	var countUserRecord = function(queryBody, campaignId){
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/countShareUserRecord?campaignId=' + campaignId
		}).success(function(response){
			
			console.info(response);
			queryBody.find('.campaignShareNumber a').html($.BCS.formatNumber(response,0));

		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	}
	
	var templateBody = {};
	templateBody = $('.dataTemplate').clone(true);
	$('.dataTemplate').remove();
	
	$('#linkDialog').dialog({
    	autoOpen: false, //初始化不會是open
    	resizable: false, //不可縮放
    	modal: true, //畫面遮罩
    	draggable: false, //不可拖曳
    	minWidth : 500,
    	position: { my: "top", at: "top", of: window  }
    });
	
	// 開啓優惠券連結視窗按鈕
	var mgmTracingUrlPre = $('#mgmTracingUrlPre').val();
	
	var dialogFunc = function(){
		var campaignId = $(this).attr('campaignId');
		
		$('#dialogTitle').find('div').text($(this).attr('campaignName'));
		
		$('#dialogUrl').find('div').text(mgmTracingUrlPre + campaignId);
		
		$('#linkDialog').dialog('open');
	};
	
	loadDataFunc();
});