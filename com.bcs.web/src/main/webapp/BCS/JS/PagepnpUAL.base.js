/**
 * 
 */

$(function(){
	// ---- Global Variables ----
	// input data
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
	
	// ---- Import Data ----
	// initialize Page
	var initPage = function(){
		// clone & remove
	    originalTr = $('.searchTr').clone(true);
	    $('.searchTr').remove();
		originalTable = $('.searchTable').clone(true);
		$('.searchTable').remove();
		
		// block
		$('.LyMain').block($.BCS.blockMsgRead);
		
		$.ajax({
			type : 'GET',
			url : bcs.bcsContextPath + '/edit/getEmpAccount',
            contentType: 'application/json',
		}).success(function(response) {
			console.info("response:", response);
			$('#account').val(response.account);
			$('#employeeId').val(response.employeeId);
			$('#divisionName').val(response.divisionName);
			$('#departmentName').val(response.departmentName);
			$('#groupName').val(response.groupName);
			$('#pccCode').val(response.pccCode);
			employeeId = response.employeeId;
		}).fail(function(response) {
			console.info(response);
			$.FailResponse(response);
		}).done(function() {
			$('.LyMain').unblock();
        });
	};
	
    // get List Data
	var getListData = function(name, url){
		divisionName = $('#divisionName').val();
		departmentName = $('#departmentName').val();
		groupName = $('#groupName').val();
		pccCode = $('#pccCode').val();
		account = $('#account').val();
		employeeId = $('#employeeId').val();
		
		var postData = {
				divisionName: divisionName,
				departmentName: departmentName,
				groupName: groupName,
				pccCode: pccCode,
				account: account,
				employeeId: employeeId,
				accountType: 'Unica'
		};
		console.info('postData:', postData);
	        
		$.ajax({
			type : 'POST',
			url : bcs.bcsContextPath + url,
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response) {
			templateCount++;
			addTab(name);
			var searchTable = originalTable.clone(true);
			
			console.info("response:", response);
			$.each(response, function(i, trData){
				console.info(trData);
				var searchTr = originalTr.clone(true);

				searchTr.find('.pnpMaintainAccountId').val(trData.id);
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
				
				// Append to Table
				console.info(searchTr);
				searchTable.append(searchTr);
			});
			
            // set attribute
            searchTable.attr('name', 'templateTable' + templateCount);
            
			// append to Tab
			$('#tab'+templateCount).append(searchTable);
			$("#tabs").tabs({active: 0});
			
		}).fail(function(response) {
			console.info(response);
			$.FailResponse(response);
		}).done(function() {
			switch(name){
			case '啟用':
				getListData('停用', '/edit/getPNPMaintainAccountList?status=false');
				break;
			case '停用':	
				$('.LyMain').unblock();
				break;
			}
        });
	};
	
	// add tab
	var addTab = function(name){
        var target;
        
        $("#tabs ul").append(
    		"<li class='tabLi'><a href='#tab" + templateCount + "'>" + name + "</a></li>"
    	);
        
        $("#tabs").append(
            "<div class='tabDiv' id='tab" + templateCount + "'></div>"
        );
        
        $("#tabs").tabs("refresh");
        $("#tabs").tabs({ active: templateCount-1 });
    };

	// delete tab
	var deleteTab = function(){
        $('.tabLi:last').remove();
        $('.tabDiv:last').remove();
        
        $("#tabs").tabs("refresh");
        $("#tabs").tabs({ active: templateCount-1 });
    };
    
    // delete table
	var deleteTemplate = function() {
		templateCount--;
		deleteTab();
	}
	
	// ---- Functions ----
	// do Delete
	$('.btn_delete.search').click(function(){
		var deleteConfirm = confirm("請確認是否刪除");
		if (!deleteConfirm) return; //點擊取消		
		
		var editAndDeleteTr = $(this).parent();
		var pnpMaintainAccountId = editAndDeleteTr.find('.pnpMaintainAccountId').val();
		console.info("id:", pnpMaintainAccountId);
		
		$.ajax({
			type : 'DELETE',
			url : bcs.bcsContextPath + '/edit/deletePNPMaintainAccount?id=' + pnpMaintainAccountId,
		}).success(function(response) {
			console.info(response);
		}).fail(function(response) {
			console.info(response);
			$.FailResponse(response);
		}).done(function() {
			confirm("刪除成功");
			window.location.replace('pnpUnicaAccountListPage');
        });
	});

	// to Edit Page
	$('.btn_edit.search').click(function(){
		var editAndDeleteTr = $(this).parent();
		var pnpMaintainAccountId = editAndDeleteTr.find('.pnpMaintainAccountId').val();
		console.info("id:", pnpMaintainAccountId);
		window.location.replace('pnpUnicaAccountCreatePage?pnpMaintainAccountModelId=' + pnpMaintainAccountId);
	});	


	// do Search
	$('.btn_add.search').click(function(){
		if(templateCount > 0){
			deleteTemplate();
			deleteTemplate();
		}
		//templateCount = 0;
		
		// block
		$('.LyMain').block($.BCS.blockMsgRead);
		// get all list data
		getListData('啟用', '/edit/getPNPMaintainAccountList?status=true');
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
		'&pccCode=' + pccCode + '&account=' + account + '&employeeId=' + employeeId + '&accountType=Unica';
	
		$('.btn_add.download').attr('href', exportUrl);
	});
	
	// to Create Page
	$('.btn_add.create').click(function(){
		window.location.replace('pnpUnicaAccountCreatePage');
	});
	
    
	// ---- Initialize Page & Load Data ----
	initPage();
	$("#tabs").tabs();
	//loadDataFunc();
});