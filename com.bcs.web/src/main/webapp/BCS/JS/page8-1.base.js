/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath + '/edit/couponCreatePage?actionType=Create&from=disable');
	});
	$('.ActiveBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/couponListPage');
	});
	$('.DisableBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/couponListDisablePage');
	});
	
	var btn_copyFunc = function(){
		var couponId = $(this).attr('couponId');
		console.info('btn_copyFunc couponId:' + couponId);
 		window.location.replace(bcs.bcsContextPath + '/edit/couponCreatePage?couponId=' + couponId + '&actionType=Copy&from=disable');
	};
	
	var btn_deteleFunc = function(){
		var couponId = $(this).attr('couponId');
		console.info('btn_deteleFunc couponId:' + couponId);

		if (!confirm('請確認是否刪除')) {
			return false;
		}
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/admin/deleteContentCoupon?couponId=' + couponId
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
		var couponId = $(this).attr('couponId');
		console.info('redesignFunc couponId:' + couponId);

		if (!confirm('請確認是否生效')) {
			return false;
		}
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/edit/redesignContentCoupon?couponId=' + couponId
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
			url : bcs.bcsContextPath + '/edit/getContentCouponListDisable'
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
						
			$.each(response, function(i, o){
				var contentCoupon = o.contentCoupon;
				var eventReferenceTitle = o.eventReferenceTitle;
				
				var queryBody = templateBody.clone(true);
				
				var msgContent = "";

				msgContent += '<img src="' + bcs.bcsContextPath + '/getResource/IMAGE/' + contentCoupon.couponListImageId + '" alt="Type2" style="cursor: pointer; width:100px"><br>';
				msgContent += '<img src="' + bcs.bcsContextPath + '/getResource/IMAGE/' + contentCoupon.couponImageId + '" alt="Type2" style="cursor: pointer; width:100px"><br>';
				msgContent += contentCoupon.couponTitle;
				
				queryBody.find('.couponTitle a')
					.attr('href', bcs.bcsContextPath + '/edit/couponCreatePage?couponId=' + contentCoupon.couponId + '&actionType=Edit&from=disable')
					.html(msgContent);
			
				if(contentCoupon.eventReference===null){
					queryBody.find('.couponEventReference a').remove();
					queryBody.find('.couponEventReference').append('<a>無</a>');
				}else{
					queryBody.find('.couponEventReference a').remove();
					queryBody.find('.couponEventReference').append(contentCoupon.eventReference==='REWARD_CARD'?'集點卡:</br>'+eventReferenceTitle:contentCoupon.eventReference==='SCRATCH_CARD'?'刮刮樂:</br>'+eventReferenceTitle:'無');
				}
				
				queryBody.find('.couponUsingTime').html(
						moment(contentCoupon.couponStartUsingTime).format('YYYY-MM-DD HH:mm:ss') 
						+ '<br> ~ ' 
						+ moment(contentCoupon.couponEndUsingTime).format('YYYY-MM-DD HH:mm:ss'));
				
				queryBody.find('.modifyUser').html(moment(contentCoupon.modifyTime).format('YYYY-MM-DD HH:mm:ss') + "<br>" + contentCoupon.modifyUser);

				queryBody.find('.status span').html($.BCS.parseInteractiveStatus(contentCoupon.status));
				
//				queryBody.find('.couponRemainingNumber a').attr('href', bcs.bcsContextPath +'/edit/couponReportPage?couponId=' + o.couponId);
				queryBody.find('.couponRemainingNumber a').html((contentCoupon.couponGetLimitNumber===null)?'無張數限制':$.BCS.formatNumber(contentCoupon.couponGetLimitNumber-contentCoupon.couponGetNumber,0));
				
//				queryBody.find('.couponTotalNumber a').attr('href', bcs.bcsContextPath +'/edit/couponReportPage?couponId=' + o.couponId);
				queryBody.find('.couponTotalNumber a').html((contentCoupon.couponGetLimitNumber===null)?'無張數限制':$.BCS.formatNumber(contentCoupon.couponGetLimitNumber,0));
				
				queryBody.find('.couponGetNumber a').attr('href', bcs.bcsContextPath +'/edit/couponReportPage?couponId=' + contentCoupon.couponId);
				queryBody.find('.couponGetNumber a').html($.BCS.formatNumber(contentCoupon.couponGetNumber,0));
				
				queryBody.find('.couponUsingNumber a').attr('href', bcs.bcsContextPath +'/edit/couponReportPage?couponId=' + contentCoupon.couponId);
				queryBody.find('.couponUsingNumber a').html($.BCS.formatNumber(contentCoupon.couponUsingNumber,0));

				queryBody.find('.btn_redeisgn').attr('couponId', contentCoupon.couponId);
				queryBody.find('.btn_redeisgn').click(redesignFunc);
				
				queryBody.find('.btn_copy')
					.attr('couponId', contentCoupon.couponId)
					.click(btn_copyFunc);
				
				queryBody.find('.btn_detele')
					.attr('couponId', contentCoupon.couponId)
					.click(btn_deteleFunc);
				
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