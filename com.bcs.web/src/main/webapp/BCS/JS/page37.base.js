/**
 * 
 */
$(function(){
	
	var detailDialog = $('#detailDialog');

	// 取得資料列表
	var loadDataFunc = function(){
		queryCampaignUserList();
	};

	$('.query').click(function(){
		if(!validateTimeRange()){
			return false;
		}

		queryCampaignUserList();
	});

	var queryCampaignUserList = function() {
		$('.LyMain').block($.BCS.blockMsgRead);
		var iMsgId = $.urlParam("iMsgId");

		var startDate = $('#startDate').val();
		var endDate = $('#endDate').val();
		
		var queryString = '';
		if (startDate && endDate) {
			queryString = '&startDate' + startDate + '&endDate' + endDate;
		}

		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath +'/edit/getCampaignUserList?iMsgId=' + iMsgId + queryString
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
	
			$.each(response, function(i, o){
				var userData = templateBody.clone(true);

				userData.find('.userName').html(o.userName);
				userData.find('.mobile').html(o.mobile);
				userData.find('.address').html(o.address);
				userData.find('.prizeName').html(o.prizeName);
				userData.find('.invNum').html(o.invNum);
				userData.find('.invStatus').html(o.invStatus);
				if (o.resourceId) {
					userData.find('.btn_detail').attr('alt-resource-id', o.resourceId);
					userData.find('.btn_detail').click(btn_detailFunc);
				} else {
					userData.find('.btn_detail').remove();
				}
				userData.find('.modifyTime').html($.formatTime(new Date(o.modifyTime)));
	
				$('#tableBody').append(userData);
			});

			var startTime = new moment($('#tableBody').find('.modifyTime:last').html(), "YYYY/MM/DD");
			var endTime = new moment($('#tableBody').find('.modifyTime:first').html(), "YYYY/MM/DD");
			
			$('#startDate').val(startTime.format('YYYY-MM-DD'));
			$('#endDate').val(endTime.format('YYYY-MM-DD'));
			
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	}
	
	var btn_detailFunc = function() {
		var resourceId = $(this).attr('alt-resource-id');
		var dom = '<img src="' + bcs.bcsContextPath + '/getResource/IMAGE/' + resourceId + '" alt="Type2" style="width:300px">';
		
		detailDialog.find('.mainFrame').html(dom);
		detailDialog.dialog('open');
	};

	var templateBody = {};
	
	var initTemplate = function(){

		templateBody = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
	}

	$('#detailDialog').dialog({
		autoOpen: false,
		resizable: false, //不可縮放
		modal: true, //畫面遮罩
		draggable: false, //不可拖曳
		minWidth : 400,
		position: { my: "top", at: "top", of: window  }
	});

	$('.exportToExcel').click(function(){
		if(!validateTimeRange()){
			return false;
		}
		
		var iMsgId = $.urlParam("iMsgId");
		var startDate = $('#startDate').val();
		var endDate = $('#endDate').val();
		
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForCampaignUserList?iMsgId=' + iMsgId + '&startDate=' + startDate + '&endDate=' + endDate;
		
		var downloadWinnerList = $('#download');
		downloadWinnerList.attr("src", url);
	});
	
	var validateTimeRange = function(){

		var startDate = moment($('#startDate').val(), "YYYY-MM-DD");
		var endDate = moment($('#endDate').val(), "YYYY-MM-DD");
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
	
	$(".datepicker").datepicker({
		'dateFormat' : 'yy-mm-dd'
	});
	
	initTemplate();
	loadDataFunc();
});