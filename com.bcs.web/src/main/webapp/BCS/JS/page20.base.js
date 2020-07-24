/**
 * 
 */
$(function(){
	var page = 0;
	var paramPage = $.urlParam("page");
	var templateBody = {};
	if(paramPage){
		page = paramPage;
		page--;
	}
	
	$('.LeftBtn').click(function(){
		if(page > 0){
			page--;
			loadDataFunc();
		}
	});
	
	$('.RightBtn').click(function(){
		page++;
		loadDataFunc();
	});

	$('.query').click(function(){
		page = 0;
		loadDataFunc();
	});
	
	$('.exportToExcel').click(function(){
		if(!validateTimeRange()){
			return false;
		}
		var url =  bcs.bcsContextPath + '/edit/exportLinkClickReportListNew?startDate=' + startDate + '&endDate=' + endDate;
		var downloadReport = $('#downloadReport');
		downloadReport.attr("src", url);
	});
	
	var validateTimeRange = function() {
		var startDate = moment($('#campaignStartTime').val(), "YYYY-MM-DD");
		var endDate = moment($('#campaignEndTime').val(), "YYYY-MM-DD");
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
			return false;
		}
		return true;
	}
	
	//選取日期元件
	$(".datepicker").datepicker({
		'maxDate' : 0, //最多只能選至今天
		'dateFormat' : 'yy-mm-dd'
	});
	
	$('.queryDate').click(function(){
		page = 0;
		loadDataFunc();
	});

	var loadDataFunc = function(){
		var campaignStartTime = moment($('#campaignStartTime').val(), "YYYY-MM-DD");
		var campaignEndTime = moment($('#campaignEndTime').val(), "YYYY-MM-DD");
		var startDate = $("#campaignStartTime").val();
		var endDate = $("#campaignEndTime").val();
		console.info("startDate", startDate);
		console.info("endDate", endDate);
		if(!validateTimeRange()){
			return;
		}
		var postData = {};
		postData.queryFlag = $("#queryFlag").val();
		postData.page = page;
		postData.pageSize = 20;
		postData.startDate = startDate;
		postData.endDate = endDate;
		$('.LyMain').block($.BCS.blockMsgRead);
		console.info("queryFlag", postData.queryFlag);
		console.info("page", postData.page);
		$('#pageText').html(page+1);
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/edit/getLinkClickReportListNew',
			cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
			var contentLinkTracingList = response.ContentLinkTracingList;
			var tracingUrlPre = response.TracingUrlPre;
			$.each(contentLinkTracingList, function(i, o){
				var groupData = templateBody.clone(true);
				groupData.find('.tracingLink').html(tracingUrlPre + o.tracingLink);
				groupData.find('.linkTitle').html(o.linkTitle);
				groupData.find('.linkUrl').html(o.linkUrl);
				var linkFlag = moment(o.linkTime).format("YYYY/MM/DD");
				for (var i = 0; i < o.flags.length; i++) {
					linkFlag += "<br/><br/>" + o.flags[i];
				}
				groupData.find('.linkFlag').html(linkFlag);
				var linkId = encodeURIComponent(o.linkId);
				var linkUrl = encodeURIComponent(o.linkUrl);
				groupData.find('.totalCount a').attr('href', bcs.bcsContextPath + '/admin/reportLinkClickDetailPage?linkUrl=' + linkUrl + "&linkId=" + linkId + "&startDate=" + startDate + "&endDate=" + endDate)
				groupData.find('.totalCount a').html($.BCS.formatNumber(o.totalCount,0));
				groupData.find('.userCount a').attr('href', bcs.bcsContextPath + '/admin/reportLinkClickDetailPage?linkUrl=' + linkUrl  + "&linkId=" + linkId + "&startDate=" + startDate + "&endDate=" + endDate)
				groupData.find('.userCount a').html($.BCS.formatNumber(o.userCount,0));
				$('#tableBody').append(groupData);
			});
			
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};
	
	var initTemplate = function(){
		$("#queryFlag").val("");
		templateBody = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		var nowDate = moment(); //取得現在時間
		var lastWeek = moment().dates(nowDate.dates() - 6); // 取得前7天(上一週)的時間
		$('#campaignStartTime').val(lastWeek.format('YYYY-MM-DD'));
		$('#campaignEndTime').val(nowDate.format('YYYY-MM-DD'));
	}
	initTemplate();
	loadDataFunc("");
});