$(function(){
	var originalTr = {};
	var date = null, title = null, sendType = null;
	var page = 1, totalPages = 0;
	var firstFatch = true;
	
	$('.btn.prev').click(function(){
		if(page > 1) {
			page--;
			loadData();
			// set pageAndTotalPage
			//console.info(page + '/' + totalPages);
			$('#pageAndTotalPages').text(page + '/' + totalPages);
		}
	});
	$('.btn.next').click(function(){
		if(page < totalPages) {
			page++;
			loadData();
			// set pageAndTotalPage
			console.info(page + '/' + totalPages);
			$('#pageAndTotalPages').text(page + '/' + totalPages);
		}
	});
	
	var initial = function(){
		// clone
		originalTr = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		
		// params
		date = $.urlParam("date");
		title = $.urlParam("title");
		sendType = $.urlParam("sendType");
		
		// set back button
		$('.btn_add.back').attr('href', '../admin/reportBNEffectsPage');

		// set ExportButton
		var exportUrl = '../edit/exportToExcelForBNPushApiEffectsDetail?date=' + date + '&title=' + title + '&sendType=' + sendType;
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
			url : '../edit/getBNEffectsDetailList?date=' + date + '&title=' + title + '&sendType=' + sendType + '&page=' + page
		}).success(function(response){
			if(response.length === 0) {
				//$('<tr class="dataTemplate"><td colspan="4">此日期區間無任何資料</td></tr>').appendTo($('#tableBody'));
			} else {
				for(key in response){
					var rowDOM = originalTr.clone(true);
					
					var valueObj = response[key];
					
					console.info('key title: ', key);
					console.info('valueObj : ', valueObj);
					
					rowDOM.find('.title').text(valueObj[0]);
					rowDOM.find('.createTime').text(moment(valueObj[1]).format('YYYY-MM-DD HH:mm:ss'));
					rowDOM.find('.modifyTime').text(moment(valueObj[2]).format('YYYY-MM-DD HH:mm:ss'));
					rowDOM.find('.sendTime').text(moment(valueObj[3]).format('YYYY-MM-DD HH:mm:ss'));
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
			url : bcs.bcsContextPath + '/edit/getBNEffectsDetailTotalPages?date=' + date + '&title=' + title + '&sendType=' + sendType
		}).success(function(response){
			console.info('msg1: ', response['msg']);
			totalPages = parseInt(response['msg']);
			console.info('totalPages1: ', totalPages);
			// set pageAndTotalPage
			page = 1;
			console.info(page + '/' + totalPages);
			$('#pageAndTotalPages').text(page + '/' + totalPages);
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