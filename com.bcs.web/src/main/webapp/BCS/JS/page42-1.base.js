/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath + '/edit/rewardCardCreatePage?actionType=Create&from=disable');
	});
	$('.ActiveBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/rewardCardListPage');
	});
	$('.DisableBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/rewardCardListDisablePage');
	});
	$('.btn_point_record').click(function(){
		console.log("btn_point_record");
		var rewardCardId = $(this).attr('rewardCardId');
		window.location.replace(bcs.bcsContextPath +'/edit/rewardCardPointRecordPage?rewardCardId='+rewardCardId);
	});
	$('.btn_coupon_record').click(function(){	
		console.log("btn_coupon_record");
		var rewardCardId = $(this).attr('rewardCardId');	
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForRewardCardCouponRecord?rewardCardId=' + rewardCardId;
		var downloadCouponRecordList = $('#downloadCouponRecordList');
		downloadCouponRecordList.attr("src", url);
	});
	
	var btn_copyFunc = function(){
		var rewardCardId = $(this).attr('rewardCardId');
		console.info('btn_copyFunc rewardCardId:' + rewardCardId);
 		window.location.replace(bcs.bcsContextPath + '/edit/rewardCardCreatePage?rewardCardId=' + rewardCardId + '&actionType=Copy&from=disable');
	};
	
	var btn_deteleFunc = function(){
		var rewardCardId = $(this).attr('rewardCardId');
		console.info('btn_deteleFunc rewardCardId:' + rewardCardId);

		if (!confirm('請確認是否刪除')) {
			return false;
		}
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/admin/deleteContentRewardCard?rewardCardId=' + rewardCardId
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
	
	var btn_qrFunc = function(){
		var rewardCardId = $(this).attr('rewardCardId');
		console.info('btn_qrFunc rewardCardId:' + rewardCardId);
		window.open(bcs.bcsContextPath + '/edit/rewardCardQRCodePage?rewardCardId=' + rewardCardId);
	};
	
	// 改變狀態按鈕
	var redesignFunc = function(){
		var rewardCardId = $(this).attr('rewardCardId');
		console.info('redesignFunc rewardCardId:' + rewardCardId);

		if (!confirm('請確認是否生效')) {
			return false;
		}
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/redesignContentRewardCard?rewardCardId=' + rewardCardId
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
			url : bcs.bcsContextPath + '/edit/getContentRewardCardListDisable'
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
						
			$.each(response, function(i, o){
				var contentRewardCard = o.contentRewardCard;
				var contentCouponList = o.contentCouponList;
				
				var queryBody = templateBody.clone(true);
				
				queryBody.find('.rewardCardTitle a')
					.attr('href', bcs.bcsContextPath + '/edit/rewardCardCreatePage?rewardCardId=' + contentRewardCard.rewardCardId + '&actionType=Edit&from=disable')
					.html(contentRewardCard.rewardCardMainTitle);
				
				queryBody.find('.expireTime').html(
						moment(contentRewardCard.rewardCardStartUsingTime).format('YYYY-MM-DD HH:mm:ss') 
						+ '<br> ~ ' 
						+ moment(contentRewardCard.rewardCardEndUsingTime).format('YYYY-MM-DD HH:mm:ss'));
				
				for(var index in contentCouponList){
					var contentCoupon = contentCouponList[index];
					var couponLink = "../edit/couponCreatePage?couponId="+contentCoupon.couponId+"&actionType=Edit&from=disable";
					queryBody.find('.couponTitle').append('<a href='+couponLink+'>'+contentCoupon.couponTitle+'</a>'+'</br>');
				}
				
				queryBody.find('.couponTitle').html(contentRewardCard.couponTitle);
				queryBody.find('#couponId').val(contentRewardCard.couponId);
				
				queryBody.find('.modifyUser').html(moment(contentRewardCard.modifyTime).format('YYYY-MM-DD HH:mm:ss') + "<br>" + contentRewardCard.modifyUser);

				queryBody.find('.status span').html($.BCS.parseInteractiveStatus(contentRewardCard.status));

				queryBody.find('.btn_redeisgn').attr('rewardCardId',contentRewardCard.rewardCardId);
				queryBody.find('.btn_redeisgn').click(redesignFunc);
				
				queryBody.find('.btn_copy')
					.attr('rewardCardId', contentRewardCard.rewardCardId)
					.click(btn_copyFunc);
				
				queryBody.find('.btn_detele')
					.attr('rewardCardId', contentRewardCard.rewardCardId)
					.click(btn_deteleFunc);
				
				queryBody.find('.btn_qr_code')
				.attr('rewardCardId', contentRewardCard.rewardCardId)
				.click(btn_qrFunc);
				
				queryBody.find('.btn_point_record').attr('rewardCardId', contentRewardCard.rewardCardId);
				queryBody.find('.btn_coupon_record').attr('rewardCardId', contentRewardCard.rewardCardId);
				
				$('#tableBody').append(queryBody);
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