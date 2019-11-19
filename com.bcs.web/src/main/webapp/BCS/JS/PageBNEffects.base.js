$(function(){
	var originalTr = {};
	var sumTr = {};
	var startDate = null, endDate = null , pages = null;
	var hasData = false;
	var page = 1, totalPages = 0;
	var firstFatch = true;
	//$('.page').removeClass();
	startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
	endDate = moment(new Date()).format('YYYY-MM-DD');
	
	if($.urlParam("startDate")){
		startDate = $.urlParam("startDate");
	}
	if($.urlParam("endDate")){
		endDate = $.urlParam("endDate");
	}
	if($.urlParam("pages")){
		page = $.urlParam("pages");
		$('#page').val(page);
	}
	
	
	$(".datepicker").datepicker({
		 maxDate : 0,
		 dateFormat : 'yy-mm-dd',
		 changeMonth: true
	});

	$('.query').click(function(){
		if(dataValidate()) {
			$('.dataTemplate').remove();
			$('.sumTemplate').remove();
			startDate = $('#startDate').val();
			endDate = $('#endDate').val();
			
			loadData();
		}
	});
	
	$('.btn.prev').click(function(){
		if(page > 1) {
			page--;
			loadData();
			// set pageAndTotalPage
			$('#page').val(page);
			$('#TotalPages').text('/' + totalPages);
		}
	});
	$('.btn.next').click(function(){
		if(page < totalPages) {
			page++;
			loadData();
			// set pageAndTotalPage
			$('#page').val(page);
			$('#TotalPages').text('/' + totalPages);
		}
	});
	
	$('#jumpPage').click(function(){
		page = $('#page').val();
		if(page > totalPages) {
			alert("欲選取頁數大於總頁數")
		}else if(page == 0){
			alert("欲選取頁數不可為0")
		}else{
			loadData();
			// set pageAndTotalPage
			console.info(page + '/' + totalPages);
			$('#page').val(page);
			$('#TotalPages').text('/' + totalPages);
		}
	});
	
	var dataValidate = function() {
		startDate = $('#startDate').val();
		endDate = $('#endDate').val();
		if(!startDate) {
			alert('請填寫起始日期！');
			return false;
		}
		if(!endDate) {
			alert('請填寫結束日期！');
			return false;
		}
		if(!moment(startDate).add(31, 'days').isAfter(moment(endDate))){
			alert('起始日期與結束日期之間不可相隔超過一個月！');
			return false;
		}
		if(moment(startDate).isAfter(moment(endDate))){
			alert('起始日期不可大於結束日期！');
			return false;
		}
		firstFatch = true;
		return true;
	}
	
	var setExportButtonSource = function() {
		if(hasData) {
			var exportUrl = '../edit/exportToExcelForBNPushApiEffects?startDate='+ startDate + '&endDate=' + endDate;	
			$('.btn_add.exportToExcel').attr('href', exportUrl);
		} else {
			$('.btn_add.exportToExcel').attr('href', '#');
		}
	}
	
	// --------------------------------
	
	var loadData = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		$('.dataTemplate').remove();
		$('.sumTemplate').remove();
		console.info("firstFatch:", firstFatch);
		if(firstFatch){
			firstFatch = false;
			setTotal();
		}
		$.ajax({
			type : 'GET',
			url : bcs.bcsContextPath + '/edit/getBNEffectsList?startDate=' + startDate + '&endDate=' + endDate + '&page=' + page
		}).success(function(response){
			if(response.length === 0) {
				hasData = false;
			} else {
				hasData = true;
				var completeSum = 0;
				var failSum = 0;
				var hasSum = false;
				for(key in response){
					var rowDOM = originalTr.clone(true);
					
					var valueObj = response[key];
					
					console.info('key title: ', key);
					console.info('valueObj : ', valueObj);
					
					var encodeTitle = encodeURI(valueObj[1]);
					var link = bcs.bcsContextPath + '/admin/reportBNEffectsDetailPage?date=' + valueObj[0] + '&templateName=' + valueObj[2] + '&sendType=' + valueObj[3]
																				     +'&startDate=' + startDate + '&endDate=' + endDate + '&pages=' + page;
					
					rowDOM.find('.sendDate').html('<a>' + valueObj[0] + '</a>').end().find('a').attr('href', link);
					rowDOM.find('.templateType').text(valueObj[1]);
					rowDOM.find('.templateName').text(valueObj[2]);
					rowDOM.find('.sendType').text(valueObj[3]);
					rowDOM.find('.completeCount').text(valueObj[4]);
					rowDOM.find('.failCount').text(valueObj[5]);
					rowDOM.find('.total').text( parseInt(valueObj[4], 10) +  parseInt(valueObj[5], 10));
					rowDOM.appendTo($('#tableBody'));
					hasSum  = true;
					completeSum = parseInt(valueObj[4], 10) + parseInt(completeSum, 10);
					failSum = parseInt(valueObj[5], 10) + parseInt(failSum, 10);
				}
				
				if (hasSum){
					var sumRowDOM = sumTr.clone(true);
					sumRowDOM.find('.completeSum').text(completeSum);
					sumRowDOM.find('.failSum').text(failSum);
					sumRowDOM.find('.totalSum').text( parseInt(failSum, 10) + parseInt(completeSum, 10));
					sumRowDOM.appendTo($('#tableBody'));
				}
			}
			
			setExportButtonSource();
			
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};
	
	var setTotal = function(){
		// get Total
		$('.LyMain').block($.BCS.blockMsgRead);
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getBNEffectsTotalPages?startDate=' + startDate + '&endDate=' + endDate
		}).success(function(response){
			console.info('msg1: ', response['msg']);
			totalPages = parseInt(response['msg']);
			console.info('totalPages1: ', totalPages);
			// set pageAndTotalPage
			
			console.info(page + '/' + totalPages);
			$('#page').val(page);
			$('#TotalPages').text('/' + totalPages);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	}
	var initial = function(){
		originalTr = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		sumTr = $('.sumTemplate').clone(true);
		$('.sumTemplate').remove();
		
//		startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
//		endDate = moment(new Date()).format('YYYY-MM-DD');
		$('#startDate').val(startDate);
		$('#endDate').val(endDate);
	}
	
	initial();
	loadData();
});