$(function(){
	var originalTr = {};
	var date = null, title = null, sendType = null;
	var bnType = $.urlParam("bnType");
	var startDate = $.urlParam("startDate");
	var endDate = $.urlParam("endDate");
	var page = 1, totalPages = 0;
	var pages = $.urlParam("pages");
	var firstFatch = true;
	
	$('.btn.prev').click(function(){
		if(page > 1) {
			page--;
			loadData();
			// set pageAndTotalPage
			//console.info(page + '/' + totalPages);
			$('#page').val(page);
			$('#TotalPages').text('/' + totalPages);
		}
	});
	$('.btn.next').click(function(){
		if(page < totalPages) {
			page++;
			loadData();
			// set pageAndTotalPage
			console.info(page + '/' + totalPages);
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
	
	var initial = function(){
		// clone
		originalTr = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		
		// params
		date = $.urlParam("date");
		templateName = $.urlParam("templateName");
		sendType = $.urlParam("sendType");
		
		// set back button
		$('.btn_add.back').attr('href', '../admin/reportBNEffectsPage?startDate=' + startDate + '&endDate=' + endDate + '&pages=' + pages);

		// set ExportButton
		var exportUrl = '../edit/exportToExcelForBNPushApiEffectsDetail?date=' + date + '&title=' + templateName + '&sendType=' + sendType + '&bnType=' + bnType;
		$('.btn_add.exportToExcel').attr('href', exportUrl);
		
		// set table
		loadData();
	};
	
	var loadData = function() {
		$('.LyMain').block($.BCS.blockMsgRead);
		$('.dataTemplate').remove();
		
		//console.info("firstFatch:", firstFatch);
		if(firstFatch){
			firstFatch = false;
			setTotal();
		}
		
		$.ajax({
			type : "GET",
			url : '../edit/getBNEffectsDetailList?date=' + date + '&templateName=' + templateName + '&sendType=' + sendType + '&page=' + page + '&bnType=' + bnType
		}).success(function(response){
			if(response.length === 0) {
				//$('<tr class="dataTemplate"><td colspan="4">此日期區間無任何資料</td></tr>').appendTo($('#tableBody'));
			} else {
				for(key in response){
					var rowDOM = originalTr.clone(true);
					
					var valueObj = response[key];
					
					console.info('key title: ', key);
					console.info('valueObj : ', valueObj);
					
					rowDOM.find('.createTime').text(moment(valueObj[0]).format('YYYY-MM-DD HH:mm:ss'));
					rowDOM.find('.sendType').text(valueObj[1]);
					rowDOM.find('.messageTitle').text(valueObj[2]);
					rowDOM.find('.messageText').text(valueObj[3]);
					rowDOM.find('.status').text(valueObj[4]);
					rowDOM.find('.uid').text(valueObj[5]);
					
					rowDOM.appendTo($('#tableBody'));
				}
			}
		}).fail(function(response){
			console.info(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	}

	var setTotal = function(){
		// get Total
		$('.LyMain').block($.BCS.blockMsgRead);
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getBNEffectsDetailTotalPages?date=' + date + '&templateName=' + templateName + '&sendType=' + sendType
		}).success(function(response){
			console.info('msg1: ', response['msg']);
			totalPages = parseInt(response['msg']);
			console.info('totalPages1: ', totalPages);
			// set pageAndTotalPage
			page = 1;
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
	
	initial();
});