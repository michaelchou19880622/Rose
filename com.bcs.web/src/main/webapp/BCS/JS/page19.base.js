/**
 * 
 */
$(function(){
	var reportType = $.urlParam("reportType"); //關鍵字 或 互動式
	var iMsgId = $.urlParam("iMsgId");
	var userStatus = $.urlParam("userStatus");
	
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
		if (reportType == "Keyword") {
	 		window.location.replace(bcs.bcsContextPath +'/edit/keywordResponsePage');
		} else if (reportType == "Interactive") {
	 		window.location.replace(bcs.bcsContextPath +'/edit/interactiveResponsePage');
		} else if (reportType == "BlackKeyword") {
	 		window.location.replace(bcs.bcsContextPath +'/edit/blackKeywordResponsePage');
		}
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
		
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForKeywordReport?startDate=' + startDate + '&endDate=' + endDate
					+ '&iMsgId=' + iMsgId + '&userStatus=' + userStatus;
		
		var downloadReport = $('#downloadReport');
		downloadReport.attr("src", url);
	});
	
	$('.exportMid').click(function(){
		if(!validateTimeRange()){
			return false;
		}
		var reportType = $.urlParam("reportType");
		
		var startDate = $('#reportStartDate').val();
		var endDate = $('#reportEndDate').val();
		
		var url =  bcs.bcsContextPath + '/edit/exportMidForKeywordReport?startDate=' + startDate + '&endDate=' + endDate
					+ '&iMsgId=' + iMsgId + '&userStatus=' + userStatus + '&reportType=' + reportType;
		
		var downloadReport = $('#downloadReport');
		downloadReport.attr("src", url);
	});
	
	var dataTemplate = {};
	var initTemplate = function(){
		dataTemplate = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		
		var nowDate = moment(); //取得現在時間
//		var yesterday = moment().dates(nowDate.dates() - 1) //取得昨天的時間
		var lastWeek = moment().dates(nowDate.dates() - 6); //取得前7天(上一週)的時間
		
		$('#reportStartDate').val(lastWeek.format('YYYY-MM-DD'));
		$('#reportEndDate').val(nowDate.format('YYYY-MM-DD'));
	}
	
	var loadDataFunc = function() {

		if (reportType == "Keyword") {
			$("#type").html("關鍵字");
		} else if (reportType == "Interactive") {
			$("#type").html("互動式");
		} else if (reportType == "BlackKeyword") {
			$("#type").html("黑名單");
		}
		
		var startDate = $('#reportStartDate').val();
		var endDate = $('#reportEndDate').val();
		
		var n = parseInt((new Date(endDate) - new Date(startDate)) / 86400000);
		$('.MdTxtNotice01').html("顯示以下來源的" + (n+1) + "天資料 " + startDate + "~" + endDate);

		$('.LyMain').block($.BCS.blockMsgRead);
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getKeywordOrInteractiveReport?startDate=' + startDate 
				+ '&endDate=' + endDate + '&iMsgId=' + iMsgId + '&userStatus=' + userStatus
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
				
				var UNBIND_Count = valueObj.UNBIND_Count;
				if(!UNBIND_Count){
					UNBIND_Count = 0;
				}
				var UNBIND_DistinctCount = valueObj.UNBIND_DistinctCount;
				if(!UNBIND_DistinctCount){
					UNBIND_DistinctCount = 0;
				}
				var BINDED_Count = valueObj.BINDED_Count;
				if(!BINDED_Count){
					BINDED_Count = 0;
				}
				var BINDED_DistinctCount = valueObj.BINDED_DistinctCount;
				if(!BINDED_DistinctCount){
					BINDED_DistinctCount = 0;
				}
				
				data.find('.responseDate').html(key);
				data.find('.resposneCountForUnbind').html($.BCS.formatNumber(UNBIND_Count, 0));
				data.find('.responsePersonsForUnbind').html($.BCS.formatNumber(UNBIND_DistinctCount, 0));
				data.find('.responseCountForBinded').html($.BCS.formatNumber(BINDED_Count, 0));
				data.find('.responsePersonsForBinded').html($.BCS.formatNumber(BINDED_DistinctCount, 0));
				
				$('#reportList').append(data);
			}
			
			if (userStatus == "UNBIND") {
				$('.binded').remove();
				$('.responseCountForBinded').remove();
				$('.responsePersonsForBinded').remove();
			} else if (userStatus == "BINDED") {
				$('.unbind').remove();
				$('.resposneCountForUnbind').remove();
				$('.responsePersonsForUnbind').remove();
			}
			
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});

		var target = "";
		if (reportType == "BlackKeyword") {
			target = "getBlackInteractiveMsg";
		}
		else{
			target = "getInteractiveMsg";
		}
		var getDataUrl = bcs.bcsContextPath +'/edit/' + target;
		if(iMsgId){
			getDataUrl =getDataUrl + '?iMsgId=' + iMsgId;
		}

		$.ajax({
			type : "GET",
			url : getDataUrl
		}).success(function(response){

			if (reportType == "BlackKeyword") {
				$("#mainKeyword").html(response.MsgMain.mainKeyword);
			}
			else{
				// 回寫 資料
				for(key in response.MsgMain){
	
					var keyObj = JSON.parse(key);
					console.info('keyObj', keyObj);
					var valueObj = response.MsgMain[key];
					console.info('valueObj', valueObj);
					
					$("#mainKeyword").html(keyObj.mainKeyword);
				}
			}
			
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	}
	
	initTemplate();
	loadDataFunc();
});