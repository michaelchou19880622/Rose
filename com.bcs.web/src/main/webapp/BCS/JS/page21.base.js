/**
 * 
 */
$(function(){
	var linkId = $.urlParam("linkId");
	var linkUrl = $.urlParam("linkUrl");
	var startDate = $.urlParam("startDate");
	var endDate = $.urlParam("endDate");
	// 日期元件
	$(".datepicker").datepicker({
		'maxDate' : -1, //最多只能選至前一天
		'dateFormat' : 'yy-mm-dd'
	});	
	
	var validateTimeRange = function() {
		startDate = moment($('#reportStartDate').val(), "YYYY-MM-DD");
		endDate = moment($('#reportEndDate').val(), "YYYY-MM-DD");
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
		var n = parseInt((new Date(endDate) - new Date(startDate)) / 86400000);
		if (n > 30) {
			alert("僅限查詢一個月內資料");
			endDate = startDate.dates(startDate.dates() + 30); // 取得前一個月的時間
			$('#reportEndDate').val(endDate.format('YYYY-MM-DD'));
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
 		window.location.replace(bcs.bcsContextPath +'/admin/reportLinkClickPage');
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
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForLinkClickReportNew?startDate=' + startDate + '&endDate=' + endDate + '&linkUrl=' + linkUrl + '&linkId=' + linkId;
		var downloadReport = $('#downloadReport');
		downloadReport.attr("src", url);
	});
	
	$('.exportMid').click(function(){
		if(!validateTimeRange()){
			return false;
		}
		var startDate = $('#reportStartDate').val();
		var endDate = $('#reportEndDate').val();
		var url =  bcs.bcsContextPath + '/edit/exportMidForLinkClickReportNew?startDate=' + startDate + '&endDate=' + endDate + '&linkUrl=' + linkUrl + '&linkId=' + linkId;
		var downloadReport = $('#downloadReport');
		downloadReport.attr("src", url);
	});
	
	var dataTemplate = {};
	var initTemplate = function(){
		dataTemplate = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		var nowDate = moment(); //取得現在時間
		var reportStartDate = moment().dates(nowDate.dates() - 7);
		var reportEndDate = moment().dates(nowDate.dates() - 1);
		if (startDate != null) {
			$('#reportStartDate').val(startDate)
		}
		else {
			$('#reportStartDate').val(reportStartDate.format('YYYY-MM-DD'));
		}
		if (endDate != null) {
			$('#reportEndDate').val(endDate)
		}
		else {
			$('#reportEndDate').val(nowDate.format('YYYY-MM-DD'));
		}
	}
	
	var loadDataFunc = function() {
		$('#linkUrl').html(decodeURIComponent(linkUrl));
		startDate = $('#reportStartDate').val();
		endDate = $('#reportEndDate').val();
		console.info("linkId", linkId);
		console.info("linkUrl", linkUrl);
		console.info("startDate", startDate);
		console.info("endDate", endDate);
		var n = parseInt((new Date(endDate) - new Date(startDate)) / 86400000);
		$('.MdTxtNotice01').html("顯示以下來源的" + (n+1) + "天資料 " + startDate + "~" + endDate);
		$('.LyMain').block($.BCS.blockMsgRead);
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/countLinkIdList?startDate=' + startDate  + '&endDate=' + endDate + '&linkUrl=' + linkUrl + '&linkId=' + linkId
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
				var linkCount = valueObj.LINK_COUNT;
				if(!linkCount){
					linkCount = 0;
				}
				var linkUserCount = valueObj.LINK_DISTINCT_COUNT;
				if(!linkUserCount){
					linkUserCount = 0;
				}
				data.find('.clickDate').html(key);
				data.find('.clickCount').html($.BCS.formatNumber(linkCount, 0));
				data.find('.clickUser').html($.BCS.formatNumber(linkUserCount, 0));
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
	if (validateTimeRange()) {
	    loadDataFunc();
	}
});