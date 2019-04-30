/**
 * 
 */
$(function(){
	var pageUrl = $.urlParam("pageUrl");
	
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
 		window.location.replace(bcs.bcsContextPath +'/admin/reportPageVisitPage');
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
		
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForPageVisitReport?startDate=' + startDate + '&endDate=' + endDate + '&pageUrl=' + pageUrl;
		
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
		$('#pageUrl').html(pageUrl);
		
		var startDate = $('#reportStartDate').val();
		var endDate = $('#reportEndDate').val();
		
		var n = parseInt((new Date(endDate) - new Date(startDate)) / 86400000);
		$('.MdTxtNotice01').html("顯示以下來源的" + (n+1) + "天資料 " + startDate + "~" + endDate);

		$('.LyMain').block($.BCS.blockMsgRead);
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/countPageVisitList?startDate=' + startDate  + '&endDate=' + endDate + '&pageUrl=' + pageUrl
		}).success(function(response){
			$('.dataTemplate').remove();
			$('#noDataTxt').remove();
			if (response.length == 0) {
				$('.exportToExcel').hide();
				$('#reportList').append('<tr id="noDataTxt"><td colspan="13"><span style="color:red">查無資料</span></td></tr>');
				return false;
			}
			
			for(key in response){
				var data = dataTemplate.clone(true);
				
				var valueObj = response[key];
				console.info('valueObj', valueObj);
				
				var PAGE_COUNT = valueObj.PAGE_COUNT;
				if(!PAGE_COUNT){
					PAGE_COUNT = 0;
				}
				var PAGE_DISTINCT_COUNT = valueObj.PAGE_DISTINCT_COUNT;
				if(!PAGE_DISTINCT_COUNT){
					PAGE_DISTINCT_COUNT = 0;
				}
				
				data.find('.visitDate').html(key);
				data.find('.visitCount').html($.BCS.formatNumber(PAGE_COUNT, 0));
				data.find('.visitUser').html($.BCS.formatNumber(PAGE_DISTINCT_COUNT, 0));
				
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