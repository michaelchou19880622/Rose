/**
 * 
 */

$(function(){
	// ---- Global Variables ----
	// input data
	var employeeRecordId = "";
	var divisionName = "";
	var departmentName = "";
	var groupName = "";
	var pccCode = "";
	var account = "";
	var employeeId = "";
	
	// result
	var hasData = false;
	var templateCount = 0;
	var oringinalTr = {};
	var originalTable = {};
	var startDate = null, endDate = null;
	var hasData = false;
	var page = 1, totalPages = 0;
	var firstFatch = true;
	
	// ---- Functions ----
	
	// set Date
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
			$('#pageAndTotalPages').text(page + '/' + totalPages);
		}
	});
	$('.btn.next').click(function(){
		if(page < totalPages) {
			page++;
			loadData();
			// set pageAndTotalPage
			$('#pageAndTotalPages').text(page + '/' + totalPages);
		}
	});
	
	// do Search
	$('.btn_add.search').click(function(){
		// block
		$('.LyMain').block($.BCS.blockMsgRead);
		// get all list data
		loadData();
	});
	
	// do Download
	$('.btn_add.download').click(function(){
		divisionName = $('#divisionName').val();
		departmentName = $('#departmentName').val();
		groupName = $('#groupName').val();
		pccCode = $('#pccCode').val();
		account = $('#account').val();
		employeeId = $('#employeeId').val();
		
		var exportUrl = '../edit/exportToExcelForPNPMaintainAccount?' + 
		'divisionName='+ divisionName + '&departmentName='+ departmentName + '&groupName='+ groupName + 
		'&pccCode=' + pccCode + '&account=' + account + '&employeeId=' + employeeId + '&accountType=Normal';
	
		$('.btn_add.download').attr('href', exportUrl);
	});
	
//	var setExportButtonSource = function() {
//		if(hasData) {
//			var exportUrl = '../edit/exportToExcelForBNPushApiEffects?startDate='+ startDate + '&endDate=' + endDate;	
//			$('.btn_add.exportToExcel').attr('href', exportUrl);
//		} else {
//			$('.btn_add.exportToExcel').attr('href', '#');
//		}
//	}
	
	// get Employee Account Information
//	$('.LyMain').block($.BCS.blockMsgRead);
//	$.ajax({
//		type : 'GET',
//		url : bcs.bcsContextPath + '/edit/getEmpAccount',
//        contentType: 'application/json',
//	}).success(function(response) {
//		console.info("response:", response);
//		$('#accountTb').val(response.account);
//		$('#pccCodeTb').val(response.pccCode);
//		employeeId = response.employeeId;
//	}).fail(function(response) {
//		console.info(response);
//		$.FailResponse(response);
//	}).done(function() {
//		$('.LyMain').unblock();
//    });

//	divisionName = $('#divisionName').val();
//	departmentName = $('#departmentName').val();
//	groupName = $('#groupName').val();
//	pccCode = $('#pccCode').val();
//	account = $('#account').val();
//	employeeId = $('#employeeId').val();
//	
//	var postData = {
//			divisionName: divisionName,
//			departmentName: departmentName,
//			groupName: groupName,
//			pccCode: pccCode,
//			account: account,
//			employeeId: employeeId,
//			accountType: 'Normal'
//	};
//	console.info('postData:', postData);
//        
//	$.ajax({
//		type : 'POST',
//		url : bcs.bcsContextPath + url,
//        cache: false,
//        contentType: 'application/json',
//        processData: false,
//		data : JSON.stringify(postData)
//	}).success(function(response) {
//		var resultTable = originalTable.clone(true);
//		
//		console.info("response:", response);
//		$.each(response, function(i, trData){
//			console.info(trData);
//			var resultTr = originalTr.clone(true);
//
//			resultTr.find('.pnpMaintainAccountId').val(trData.id);
//			resultTr.find('.account').html(trData.account);
//			resultTr.find('.accountAttribute').html(trData.accountAttribute);
//			resultTr.find('.accountClass').html(trData.accountClass);
//			resultTr.find('.departmentId').html(trData.departmentId);
//			resultTr.find('.divisionName').html(trData.divisionName);
//			resultTr.find('.departmentName').html(trData.departmentName);
//			resultTr.find('.groupName').html(trData.groupName);
//			resultTr.find('.employeeId').html(trData.employeeId);
//			resultTr.find('.id').html(trData.id);
//			resultTr.find('.pccCode').html(trData.pccCode);
//			resultTr.find('.pnpContent').html(trData.pnpContent);
//			resultTr.find('.sourceSystem').html(trData.sourceSystem);
//			resultTr.find('.status').html(trData.status);
//			resultTr.find('.template').html(trData.template);
//			
//			//resultTr.find('.pathway').html(trData.pathway);
//			if(trData.pathway == '3'){
//				resultTr.find('.pathway').html('BC-&gt;PNP-&gt;SMS');
//			}else if(trData.pathway == '2'){
//				resultTr.find('.pathway').html('BC-&gt;SMS');
//			}else if(trData.pathway == '1'){
//				resultTr.find('.pathway').html('BC');
//			}
//			
//			// Append to Table
//			console.info(resultTr);
//			//resultTable.append(resultTr);
//			resultTr.appendTo($('#resultTbody'));
//		});
//		
//        // set attribute
//        resultTable.attr('name', 'templateTable' + templateCount);
//        
//		
//	}).fail(function(response) {
//		console.info(response);
//		$.FailResponse(response);
//	}).done(function() {
//		$('.LyMain').unblock();
//    });
	
	
	
	// ---- Initialize Page & Load Data ----
    // get List Data
	var loadData = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		$('.resultTr').remove();
		console.info("firstFatch:", firstFatch);
		if(firstFatch){
			firstFatch = false;
			setTotal();
		}
		
		$.ajax({
			type : 'GET',
			url : bcs.bcsContextPath + '/edit/getPNPDetailReport?startDate=' + startDate + '&endDate=' + endDate + '&page=' + page,
            contentType: 'application/json',
		}).success(function(response) {
			console.info("response:", response);
			for(key in response){
				var resultTr = originalTr.clone(true);
				var valueObj = response[key];
				//console.info('valueObj : ', valueObj);
				
				// 0	ORIG_FILE_NAME
				var splits = valueObj[0].split('_');
					// 前方來源系統 PRMSMS
					resultTr.find('.sourceSystem').html(splits[1]);
					// 發送帳號
					resultTr.find('.account').html(splits[2]);
				
				// 1	PROC_FLOW
				if(valueObj[1] == '3'){
					resultTr.find('.pathway').html('BC-&gt;PNP-&gt;SMS');
				}else if(valueObj[1] == '2'){
					resultTr.find('.pathway').html('BC-&gt;SMS');
				}else if(valueObj[1] == '1'){
					resultTr.find('.pathway').html('BC');
				}
				
				// 2	SOURCE
				if(valueObj[2] == '4'){
					resultTr.find('.deliveryPathway').html('UNICA');
				}else if(valueObj[2] == '3'){
					resultTr.find('.deliveryPathway').html('明宣');
				}else if(valueObj[2] == '2'){
					resultTr.find('.deliveryPathway').html('互動');
				}else if(valueObj[2] == '1'){
					resultTr.find('.deliveryPathway').html('三竹');
				}
				
				// 3	MSG
				resultTr.find('.pnpContent').html(valueObj[3]);
				
				// 4	PHONE
				resultTr.find('.customerCellPhoneNumber').html(valueObj[4]);
				
				// 5	PNP_TIME
				resultTr.find('.scheduleDate').html(moment(valueObj[5], 'YYYY-MM-DD HH:mm:ss').format('YYYY-MM-DD'));
				resultTr.find('.scheduleTime').html(moment(valueObj[5], 'YYYY-MM-DD HH:mm:ss').format('HH:mm:ss'));
				
				// 6	DETAIL_SCHEDULE_TIME
				resultTr.find('.deliveryDate').html(moment(valueObj[6], 'YYYY-MM-DD HH:mm:ss').format('YYYY-MM-DD'));
				resultTr.find('.deliveryTime').html(moment(valueObj[6], 'YYYY-MM-DD HH:mm:ss').format('HH:mm:ss'));				
				
				// 7 MODIFY_TIME (useless)
				
				// 8 STATUS
				resultTr.find('.statusCode').html(valueObj[8]);
				
				$('.resultTable').append(resultTr);
			}
		}).fail(function(response) {
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function() {
			$('.LyMain').unblock();
        });
		

	};
	
	var setTotal = function(){
		// get Total
		$('.LyMain').block($.BCS.blockMsgRead);
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getPNPDetailReportTotalPages?startDate=' + startDate + '&endDate=' + endDate
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
	
	// initialize Page
	var initPage = function(){
		// clone & remove
	    originalTr = $('.resultTr').clone(true);
	    $('.resultTr').remove();
		originalTable = $('.resultTable').clone(true);
		
		// initialize date-picker
		startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
		endDate = moment(new Date()).format('YYYY-MM-DD');
		$('#startDate').val(startDate);
		$('#endDate').val(endDate);
	};
	
	initPage();
});