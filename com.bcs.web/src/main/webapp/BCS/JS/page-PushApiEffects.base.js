$(function(){
	var clonedDOM = null;
	var startDate = null, endDate = null;
	var hasData = false;
	
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
	
	initial();
	
	function initial() {
		console.log('Push API 成效列表');
		
		clonedDOM = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		
		startDate = moment(new Date()).format('YYYY-MM-DD');
		endDate = moment(new Date()).format('YYYY-MM-DD');
		
		$('#startDate').val(startDate);
		$('#endDate').val(endDate);
		
		getEffects(startDate, endDate);
	}
	
	function getEffects(startDate, endDate) {
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getPushApiEffects?startDate=' + startDate + '&endDate=' + endDate
		}).success(function(response){
			if(response.length === 0) {
				hasData = false;
				
				$('<tr class="dataTemplate"><td colspan="4">此日期區間無任何資料</td></tr>').appendTo($('#tableBody'));
			} else {
				hasData = true;
				
				response.forEach(function(element){
					var exportUrl = '../edit/exportToExcelForPushApiEffectDetail?createTime=';
					var rowDOM = clonedDOM.clone(true);
					
					rowDOM.find('.createDate').html('<a>' + moment(element.createTime).format('YYYY-MM-DD HH:mm:ss') + '</a>').end().find('a').attr('href', exportUrl + element.createTime);
					rowDOM.find('.department').text(element.department);
					rowDOM.find('.serviceName').text(element.serviceName);
					rowDOM.find('.pushTheme').text(element.pushTheme);
					rowDOM.find('.successCount').text(element.successCount);
					rowDOM.find('.failCount').text(element.failCount);
					
					rowDOM.appendTo($('#tableBody'));
				});
			}
			
			setExportButtonSource(startDate, endDate);
		}).fail(function(response){
			console.info(response);
		}).done(function(){
		});
	}
	
	function setExportButtonSource(startDate, endDate) {
		if(hasData) {
			var exportUrl = '../edit/exportToExcelForPushApiEffects?startDate='+ startDate + '&endDate=' + endDate;
			
			$('.btn_add.exportToExcel').attr('href', exportUrl);
		} else {
			$('.btn_add.exportToExcel').attr('href', '#');
		}
	}
	
	function dataValidate() {
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
});