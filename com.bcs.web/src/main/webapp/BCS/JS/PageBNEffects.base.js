$(function(){
	
	var originalTr = {};
	var startDate = null, endDate = null;
	var hasData = false;
	
	var initial = function(){
		originalTr = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		
		
		startDate = '1911-01-01';
		endDate = '3099-01-01';
		getEffects(startDate, endDate);
	};
	
	$(".datepicker").datepicker({
		 maxDate : 0,
		 dateFormat : 'yy-mm-dd',
		 changeMonth: true
	});
 
	$('.query').click(function(){
		if(dataValidate()) {
			$('.dataTemplate').remove();
						
			startDate = $('#startDate').val();
			endDate = $('#endDate').val();
			
			getEffects(startDate, endDate);
		}
	});
	
	var dataValidate = function() {
		var startDate = $('#startDate').val();
		var endDate = $('#endDate').val();
		if(!startDate) {
			alert('請填寫起始日期！');
			return false;
		}
		if(!$('#endDate').val()) {
			alert('請填寫結束日期！');
			return false;
		}
		if(moment(startDate).isAfter(moment(endDate))) {
			alert('起始日期不可大於結束日期！');
			return false;
		}		
		return true;
	}

	var getEffects = function(startDate, endDate) {
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getBNEffectsList?startDate=' + startDate + '&endDate=' + endDate
		}).success(function(response){
			if(response.length === 0) {
				hasData = false;
				$('<tr class="dataTemplate"><td colspan="4">此日期區間無任何資料</td></tr>').appendTo($('#tableBody'));
			} else {
				hasData = true;
				
				for(key in response){
					var rowDOM = originalTr.clone(true);
					
					var valueObj = response[key];
					
					console.info('key title: ', key);
					console.info('valueObj : ', valueObj);
					
					var link = bcs.bcsContextPath + '/admin/reportBNEffectsDetailPage?date=' + valueObj[1] + '&title=' + valueObj[0] + '&sendType=' + valueObj[2];

					rowDOM.find('.sendDate').html('<a>' + valueObj[1] + '</a>').end().find('a').attr('href', link);
					rowDOM.find('.sendType').text(valueObj[2]);
					rowDOM.find('.title').text(valueObj[0]);
					rowDOM.find('.completeCount').text(valueObj[3]);
					rowDOM.find('.failCount').text(valueObj[4]);
					
					rowDOM.appendTo($('#tableBody'));
				}
			}
			
			setExportButtonSource(startDate, endDate);
			
		}).fail(function(response){
			console.info(response);
		}).done(function(){
		});
	}

	var setExportButtonSource = function(startDate, endDate) {
		if(hasData) {
			var exportUrl = '../edit/exportToExcelForBNPushApiEffects?startDate='+ startDate + '&endDate=' + endDate;	
			$('.btn_add.exportToExcel').attr('href', exportUrl);
		} else {
			$('.btn_add.exportToExcel').attr('href', '#');
		}
	}
	
	initial();
});