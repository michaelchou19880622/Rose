/**
 * 
 */
$(function(){
	var page = 0;
	var paramPage = $.urlParam("page");
	var templateBody = {};
	$("#queryFlag").val("");
	templateBody = $('.dataTemplate').clone(true);
	$('.dataTemplate').remove();
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
		var postData = {};
		var campaignStartTime = moment($('#campaignStartTime').val(), "YYYY-MM-DD");
		var campaignEndTime = moment($('#campaignEndTime').val(), "YYYY-MM-DD");
		var startDate = $("#campaignStartTime").val();
		var endDate = $("#campaignEndTime").val();
		console.info("startDate", startDate);
		console.info("endDate", endDate);
		//需要有日期
		if(startDate == '' || endDate == ''){
			var d = new Date();
			endDate = d.getFullYear() + '-';
			if (parseInt(d.getMonth()) < 9) {
			    endDate += '0';
			}
			endDate += (parseInt(d.getMonth()) + 1) + '-';
			if (parseInt(d.getDate()) < 10) {
			    endDate += '0';
			}
			endDate += d.getDate();
			d.setDate(d.getDate() - 6);
			startDate = d.getFullYear() + '-';
			if (parseInt(d.getMonth()) < 9) {
			    startDate += '0';
			}
			startDate += (parseInt(d.getMonth()) + 1) + '-';
			if (parseInt(d.getDate()) < 10) {
			    startDate += '0';
			}
			startDate += d.getDate();
			$('#campaignStartTime').val(startDate);
			$('#campaignEndTime').val(endDate);
		}else if (campaignStartTime.isAfter(campaignEndTime)){
			alert("起始日不能大於結束日");
			return;
		}
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
			url : bcs.bcsContextPath + '/edit/getLinkUrlReportListNew',
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

	loadDataFunc("");
});