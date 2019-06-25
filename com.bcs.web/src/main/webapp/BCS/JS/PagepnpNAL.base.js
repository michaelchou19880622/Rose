/**
 * 
 */

$(function(){
	var hasData = false;
	var divisionName = "";
	var departmentName = "";
	var groupName = "";
	var pccCode = "";
	var account = "";
	var employeeId = "";
	
	var oringinalTr = {};
	
	var initPage = function(){
		oringinalTr = $('.searchTr').clone(true);
	};
	
	$('.btn_add.create').click(function(){
		window.location.replace('pnpNormalAccountCreatePage');
	});
		
	$('.btn_add.search').click(function(){
		divisionName = $('#divisionName').val();
		departmentName = $('#departmentName').val();
		groupName = $('#groupName').val();
		pccCode = $('#pccCode').val();
		account = $('#account').val();
		employeeId = $('#employeeId').val();
		
//		if (!divisionName||!departmentName||!groupName||!pccCode||!account||!employeeId){
//			alert("必填欄位不可為空");
//			return;
//		}
		
		var postData = {
				divisionName: divisionName,
				departmentName: departmentName,
				groupName: groupName,
				pccCode: pccCode,
				account: account,
				employeeId: employeeId,
				accountType: 'Normal'
		};
		console.info('postData:', postData);
		
		$.ajax({
			type : 'POST',
			url : bcs.bcsContextPath + '/edit/getPNPMaintainAccountList',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response) {
				console.info(response);
				$('.searchTr').remove();
				
				$.each(response, function(i, trData){
					console.info(trData);
					var searchTr = oringinalTr.clone(true);

					searchTr.find('.account').html(trData.account);
					searchTr.find('.accountAttribute').html(trData.accountAttribute);
					searchTr.find('.accountClass').html(trData.accountClass);
					searchTr.find('.departmentId').html(trData.departmentId);
					searchTr.find('.divisionName').html(trData.divisionName);
					searchTr.find('.departmentName').html(trData.departmentName);
					searchTr.find('.groupName').html(trData.groupName);
					searchTr.find('.employeeId').html(trData.employeeId);
					searchTr.find('.id').html(trData.id);
					searchTr.find('.pccCode').html(trData.pccCode);
					searchTr.find('.pnpContent').html(trData.pnpContent);
					searchTr.find('.sourceSystem').html(trData.sourceSystem);
					searchTr.find('.status').html(trData.status);
					searchTr.find('.template').html(trData.template);
					
					//searchTr.find('.pathway').html(trData.pathway);
					if(trData.pathway == '3'){
						searchTr.find('.pathway').html('BC-&gt;PNP-&gt;SMS');
					}else if(trData.pathway == '2'){
						searchTr.find('.pathway').html('BC-&gt;SMS');
					}else if(trData.pathway == '1'){
						searchTr.find('.pathway').html('BC');
					}
					
					console.info(searchTr);
					$('.searchTbody').append(searchTr);
				});
		}).fail(function(response) {
			console.info(response);
			$.FailResponse(response);
		})
	});

	$('.btn_add.download').click(function(){
		divisionName = $('#divisionName').val();
		departmentName = $('#departmentName').val();
		groupName = $('#groupName').val();
		pccCode = $('#pccCode').val();
		account = $('#account').val();
		employeeId = $('#employeeId').val();
		
//		if (!divisionName||!departmentName||!groupName||!pccCode||!account||!employeeId){
//			alert("必填欄位不可為空");
//			return;
//		}
		
		var exportUrl = '../edit/exportToExcelForPNPMaintainAccount?' + 
		'divisionName='+ divisionName + '&departmentName='+ departmentName + '&groupName='+ groupName + 
		'&pccCode=' + pccCode + '&account=' + account + '&employeeId=' + employeeId + '&accountType=Normal';
	
		$('.btn_add.download').attr('href', exportUrl);
	});
	
	initPage();
	//loadDataFunc();
});