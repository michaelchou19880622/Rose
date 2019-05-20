$(function(){
	var originalTr = {};

	var initial = function(){
		originalTr = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		
		// params
		var date = $.urlParam("date");
		var title = $.urlParam("title");
		var sendType = $.urlParam("sendType");
		
		// set table
		var link = '/edit/getBNEffectsDetailList?date=' + date + '&title=' + title + '&sendType=' + sendType;
		getEffects(link);
		
		// set ExportButton
		var exportUrl = '../edit/exportToExcelForBNPushApiEffectsDetail?date=' + date + '&title=' + title + '&sendType=' + sendType;
		$('.btn_add.exportToExcel').attr('href', exportUrl);
		
		var backUrl = '../admin/reportBNEffectsPage';
		$('.btn_add.back').attr('href', backUrl);
	};
	
	var getEffects = function(link) {
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + link
		}).success(function(response){
			if(response.length === 0) {
				$('<tr class="dataTemplate"><td colspan="4">此日期區間無任何資料</td></tr>').appendTo($('#tableBody'));
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
		}).done(function(){
		});
	}

	initial();
});