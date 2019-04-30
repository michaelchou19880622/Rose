/**
 * 
 */
$(function(){
	
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
	
	$('#downloadReport').load(function () {
        //if the download link return a page
        //load event will be triggered
		$('.LyMain').unblock();
    });
	
	$('.btn_add').click(function(){
		var sheetName = $(this).attr('name');
		
		if(!validateTimeRange()){
			return false;
		}
			
		//var linkUrl = encodeURIComponent('http://localhost:8080/bcs/m/goIndex?toPage=TurntablePage&referenceId=03e5227b-fdda-42e0-b904-b754ead6bd51--TurntablePage');
		
		var startDate = $('#reportStartDate').val();
		var endDate = $('#reportEndDate').val();

		var url =  bcs.bcsContextPath + '/edit/exportExcelTest?sheetName=' + sheetName + '&startDate=' + startDate + '&endDate=' + endDate; //+ '&linkUrl=' + linkUrl;

		var downloadReport = $('#downloadReport');
		downloadReport.attr("src", url);
	});
});