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
	var startDate = null, endDate = null;
	
	// result
	var hasData = false;
	var templateCount = 0;
	var oringinalTr = {};
	var originalTable = {};

	
	// ---- Functions ----
	
	// do Search
	$('.btn_add.search').click(function(){
		// block
		$('.LyMain').block($.BCS.blockMsgRead);
		// get all list data
		getListData('/edit/getPNPMaintainAccountList?status=true');
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
	
	
	// ---- Initialize Page & Load Data ----
	// initialize Page
	var initPage = function(){
		// clone & remove
	    originalTr = $('.searchTr').clone(true);
	    $('.searchTr').remove();
		originalTable = $('.searchTable').clone(true);
		
		// set initialized date
		startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
		endDate = moment(new Date()).format('YYYY-MM-DD');
		$('#startDate').val(startDate);
		$('#endDate').val(endDate);
		
		// get Employee Account Information
//		$('.LyMain').block($.BCS.blockMsgRead);
//		$.ajax({
//			type : 'GET',
//			url : bcs.bcsContextPath + '/edit/getEmpAccount',
//            contentType: 'application/json',
//		}).success(function(response) {
//			console.info("response:", response);
//			$('#accountTb').val(response.account);
//			$('#pccCodeTb').val(response.pccCode);
//			employeeId = response.employeeId;
//		}).fail(function(response) {
//			console.info(response);
//			$.FailResponse(response);
//		}).done(function() {
//			$('.LyMain').unblock();
//        });
		$('.LyMain').unblock();
	};

    // get List Data
	var getListData = function(url){
		console.info("s:", startDate);
		console.info("e:", endDate);
		$.ajax({
			type : 'GET',
			url : bcs.bcsContextPath + '/edit/getPNPDetailReport?startDate=' + startDate + '&endDate=' + endDate,
            contentType: 'application/json',
		}).success(function(response) {
			console.info("response:", response);
			for(key in response){
				var searchTr = originalTr.clone(true);
				var valueObj = response[key];
				//console.info('valueObj : ', valueObj);
				
				//0	ORIG_FILE_NAME
				var splits = valueObj[0].split('_');
				 // 前方來源系統 PRMSMS		splits[1]
				 searchTr.find('.sourceSystem').html(splits[1]);
				 // 發送帳號				splits[2]
				 searchTr.find('.account').html(splits[2]);
				
				//1	PROC_FLOW
				if(valueObj[1] == '3'){
					searchTr.find('.pathway').html('BC-&gt;PNP-&gt;SMS');
				}else if(valueObj[1] == '2'){
					searchTr.find('.pathway').html('BC-&gt;SMS');
				}else if(valueObj[1] == '1'){
					searchTr.find('.pathway').html('BC');
				}
				
				//2	SOURCE
				if(valueObj[2] == '4'){
					searchTr.find('.deliveryPathway').html('UNICA');
				}else if(valueObj[2] == '3'){
					searchTr.find('.deliveryPathway').html('明宣');
				}else if(valueObj[2] == '2'){
					searchTr.find('.deliveryPathway').html('互動');
				}else if(valueObj[2] == '1'){
					searchTr.find('.deliveryPathway').html('三竹');
				}
				
				//3	MSG
				searchTr.find('.pnpContent').html(valueObj[3]);
				
				//4	PHONE
				searchTr.find('.customerCellPhoneNumber').html(valueObj[4]);
				
				//5	PNP_TIME
				searchTr.find('.scheduleDate').html(moment(valueObj[5], 'YYYY-MM-DD HH:mm:ss').format('YYYY-MM-DD'));
				searchTr.find('.scheduleTime').html(moment(valueObj[5], 'YYYY-MM-DD HH:mm:ss').format('HH:mm:ss'));
				
				//6	PNP_DELIVERY_TIME
				searchTr.find('.deliveryDate').html(moment(valueObj[6], 'YYYY-MM-DD HH:mm:ss').format('YYYY-MM-DD'));
				searchTr.find('.deliveryTime').html(moment(valueObj[6], 'YYYY-MM-DD HH:mm:ss').format('HH:mm:ss'));				
				
				$('.searchTable').append(searchTr);
			}
		}).fail(function(response) {
			console.info(response);
			$.FailResponse(response);
		}).done(function() {
			$('.LyMain').unblock();
        });
		
//		divisionName = $('#divisionName').val();
//		departmentName = $('#departmentName').val();
//		groupName = $('#groupName').val();
//		pccCode = $('#pccCode').val();
//		account = $('#account').val();
//		employeeId = $('#employeeId').val();
//		
//		var postData = {
//				divisionName: divisionName,
//				departmentName: departmentName,
//				groupName: groupName,
//				pccCode: pccCode,
//				account: account,
//				employeeId: employeeId,
//				accountType: 'Normal'
//		};
//		console.info('postData:', postData);
//	        
//		$.ajax({
//			type : 'POST',
//			url : bcs.bcsContextPath + url,
//            cache: false,
//            contentType: 'application/json',
//            processData: false,
//			data : JSON.stringify(postData)
//		}).success(function(response) {
//			var searchTable = originalTable.clone(true);
//			
//			console.info("response:", response);
//			$.each(response, function(i, trData){
//				console.info(trData);
//				var searchTr = originalTr.clone(true);
//
//				searchTr.find('.pnpMaintainAccountId').val(trData.id);
//				searchTr.find('.account').html(trData.account);
//				searchTr.find('.accountAttribute').html(trData.accountAttribute);
//				searchTr.find('.accountClass').html(trData.accountClass);
//				searchTr.find('.departmentId').html(trData.departmentId);
//				searchTr.find('.divisionName').html(trData.divisionName);
//				searchTr.find('.departmentName').html(trData.departmentName);
//				searchTr.find('.groupName').html(trData.groupName);
//				searchTr.find('.employeeId').html(trData.employeeId);
//				searchTr.find('.id').html(trData.id);
//				searchTr.find('.pccCode').html(trData.pccCode);
//				searchTr.find('.pnpContent').html(trData.pnpContent);
//				searchTr.find('.sourceSystem').html(trData.sourceSystem);
//				searchTr.find('.status').html(trData.status);
//				searchTr.find('.template').html(trData.template);
//				
//				//searchTr.find('.pathway').html(trData.pathway);
//				if(trData.pathway == '3'){
//					searchTr.find('.pathway').html('BC-&gt;PNP-&gt;SMS');
//				}else if(trData.pathway == '2'){
//					searchTr.find('.pathway').html('BC-&gt;SMS');
//				}else if(trData.pathway == '1'){
//					searchTr.find('.pathway').html('BC');
//				}
//				
//				// Append to Table
//				console.info(searchTr);
//				//searchTable.append(searchTr);
//				searchTr.appendTo($('#searchTbody'));
//			});
//			
//            // set attribute
//            searchTable.attr('name', 'templateTable' + templateCount);
//            
//			
//		}).fail(function(response) {
//			console.info(response);
//			$.FailResponse(response);
//		}).done(function() {
//			$('.LyMain').unblock();
//        });
	};
	
	initPage();
	//loadDataFunc();
});