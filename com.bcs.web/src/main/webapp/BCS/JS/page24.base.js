/**
 * 
 */
$(function(){
	var couponId = $.urlParam("couponId");
	
	// 日期元件
	$(".datepicker").datepicker({
		'maxDate' : 0, //最多只能選至今天
		'dateFormat' : 'yy-mm-dd'
	});
	
	
	var validateTimeRange = function() {
		var startDate = moment($('#reportStartDate').val(), "YYYY-MM-DD");
		var endDate = moment($('#reportEndDate').val(), "YYYY-MM-DD");
		if (!startDate.isValid()) {
			alert("請選擇起始日期");
			return false;
		}
		if (!endDate.isValid()) {
			alert("請選擇結束日期");
			return false;
		}
		if (startDate.isAfter(endDate)) {
			alert("起始日不能大於結束日");
			return false;
		}
		
		return true;
	}
	
	$('.query').click(function(){
		if(!validateTimeRange()){
			return false;
		}
		loadDataFunc();
	});
	
	$('.btn_cancel').click(function() {
 		window.location.replace(bcs.bcsContextPath +'/edit/couponListPage');
	});
	
	$('#downloadReport').load(function () {
        //if the download link return a page
        //load event will be triggered
		$('.LyMain').unblock();
    });
	
	$('.exportToExcel').click(function(){
		if(!validateTimeRange()){
			return false;
		}
		
		var startDate = $('#reportStartDate').val();
		var endDate = $('#reportEndDate').val();
		
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForCouponReport?startDate=' + startDate + '&endDate=' + endDate + '&couponId=' + couponId;
		
		var downloadReport = $('#downloadReport');
		downloadReport.attr("src", url);
	});
	
	$('.exportMid').click(function(){
		if(!validateTimeRange()){
			return false;
		}
		
		var startDate = $('#reportStartDate').val();
		var endDate = $('#reportEndDate').val();
		
		var url =  bcs.bcsContextPath + '/edit/exportMidForCouponReport?startDate=' + startDate + '&endDate=' + endDate + '&couponId=' + couponId;
		
		var downloadReport = $('#downloadReport');
		downloadReport.attr("src", url);
	});
	
	var dataTemplate = {};
	var initTemplate = function(){
		dataTemplate = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		
		var nowDate = moment(); //取得現在時間
		var lastWeek = moment().dates(nowDate.dates() - 6); //取得前7天(上一週)的時間
		
		$('#reportStartDate').val(lastWeek.format('YYYY-MM-DD'));
		$('#reportEndDate').val(nowDate.format('YYYY-MM-DD'));
	}
	
	var loadDataFunc = function() {
		
		var startDate = $('#reportStartDate').val();
		var endDate = $('#reportEndDate').val();
		
		var n = parseInt((new Date(endDate) - new Date(startDate)) / 86400000);
		$('.MdTxtNotice01').html("顯示以下來源的" + (n+1) + "天資料 " + startDate + "~" + endDate);

		$('.LyMain').block($.BCS.blockMsgRead);
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/countCouponReport?startDate=' + startDate  + '&endDate=' + endDate + '&couponId=' + couponId
		}).success(function(response){
			$('.dataTemplate').remove();
			$('#noDataTxt').remove();
			
			var couponTitle = response.CouponTitle;
			console.info(couponTitle);
			$('#couponTitle').html(Object.keys(couponTitle)[0]);
			
			for(key in response.Get){
				var data = dataTemplate.clone(true);
				
				var valueGetObj = response.Get[key];
				console.info('valueGetObj', valueGetObj);
				
				var valueUseObj = response.Use[key];
				console.info('valueUseObj', valueUseObj);
				
				var COUPON_GET_COUNT = valueGetObj.COUPON_COUNT;
				if(!COUPON_GET_COUNT){
					COUPON_GET_COUNT = 0;
				}
				var COUPON_USE_COUNT = valueUseObj.COUPON_COUNT;
				if(!COUPON_USE_COUNT){
					COUPON_USE_COUNT = 0;
				}
				
				data.find('.reportDate').html(key);
				data.find('.getCount').html($.BCS.formatNumber(COUPON_GET_COUNT, 0));
				data.find('.useCount').html($.BCS.formatNumber(COUPON_USE_COUNT, 0));
				
				$('#reportList').append(data);
			}
			
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	}
	
	initTemplate();
	loadDataFunc();
});