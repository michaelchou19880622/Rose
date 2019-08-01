/**
 * 
 */

$(function(){
	// ---- Global Variables ----
	// parameters
	var pnpMaintainAccountModelId = null;
	var pnpMaintainAccountActionType = null;
	
	// original Template
	var originalPopTr = {};
	
	// pop data
	var pathway = "";
	var template = "";
	var PNPContent = "";
	
	
	// ---- Import Data ----
	// initialize Page
	var initPage = function(){
		// clone & remove
		originalPopTr = $('.popTr').clone(true);
		$('.popTr').remove();
		
		// add options
		appendOption('pathwayList', 0, 'BC->PNP->SMS');
		appendOption('pathwayList', 1, 'BC->SMS');
		appendOption('pathwayList', 2, 'BC');
		appendOption('templateList', 0, 'TestTemplate');
		
		
		// parameter
		pnpMaintainAccountModelId = $.urlParam("pnpMaintainAccountModelId"); //從列表頁導過來的參數
		
		// Edit Mode
		if(pnpMaintainAccountModelId != null){
			// block
			$('.LyMain').block($.BCS.blockMsgRead);
			
			// change UI
			pnpMaintainAccountActionType = 'Edit';
			$('.CHTtl').html('編輯一般帳號');
			$('.btn_add.add').val('Edit');
			
			// get Data
			$.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + "/pnpAdmin/getPNPMaintainAccount?id=" + pnpMaintainAccountModelId,
    		}).success(function(response){
    			console.info("response:", response);
    			$('#account').val(response.account);
    			$('#accountAttribute').val(response.accountAttribute);
    			$('#sourceSystem').val(response.sourceSystem);
    			$('#employeeId').val(response.employeeId);
    			$('#departmentId').val(response.departmentId);
    			$('#divisionName').val(response.divisionName);
    			$('#departmentName').val(response.departmentName);
    			$('#groupName').val(response.groupName);
    			$('#PccCode').val(response.pccCode);
    			
    			if(response.accountClass == 'O'){
					$('.accountClass')[0].checked = true;
				}else{
					$('.accountClass')[1].checked = true;
				}   			
    			
    			if(response.status = true){
					$('.status')[0].checked = true;
				}else{
					$('.status')[1].checked = true;
				}
    			
    			$('#PNPContent').val(response.pnpContent);
    			
    			// Pop data
    			template = response.template;
    			PNPContent = response.pnpContent;
    			if(response.pathway == '3'){
    				pathway = 'BC-&gt;PNP-&gt;SMS';
    			}else if(response.pathway == '2'){
    				pathway = 'BC-&gt;SMS';
    			}else if(pathway == '1'){
    				pathway = 'BC';
    			}
    			
    			$('.popTr').remove();
    			var popTr = originalPopTr.clone(true);
    			popTr.find('.pathway').html(pathway);
    			popTr.find('.template').html(template);
    			popTr.find('.PNPContent').html(PNPContent);
    			
    			$('.popTbody').append(popTr);
    			
    		}).fail(function(response){
    			console.info(response);
    			$.FailResponse(response);
    		}).done(function(){
    			$('.LyMain').unblock();
    		});
		}else{
			// Create Mode
			pnpMaintainAccountActionType = 'Create';
		}
	};
	
	// add option
	var appendOption = function(listName, value, text){
		var opt = document.createElement('option');
		var list = document.getElementById(listName);
		opt.value = value;
		opt.innerHTML = text;
		list.appendChild(opt);
	};
	
	
	// ---- Functions ----
	// do Add
	$('.btn_add.add').click(function(){
	    $('#dialog-modal').dialog({
	 	   	width: 960,
	        height: 480,
	        modal: true
	    });
    	$('#dialog-modal').show();
	});
	
	// do Confirm
	$('.btn_add.confirm').click(function(){
		postData = {};
				
		postData.account = $('#account').val();
		postData.accountAttribute = $('#accountAttribute').val();
		postData.sourceSystem = $('#sourceSystem').val();
		postData.employeeId = $('#employeeId').val();
		postData.departmentId = $('#departmentId').val();
		postData.divisionName = $('#divisionName').val();
		postData.departmentName = $('#departmentName').val();
		postData.groupName = $('#groupName').val();
		postData.pccCode = $('#PccCode').val();
		postData.accountType = 'Normal';
		
		if($('.accountClass')[0].checked){
			postData.accountClass = 'O';
		}else{
			postData.accountClass = 'M';
		}
		
		if($('.status')[0].checked){
			postData.status = true;
		}else{
			postData.status = false;
		}
		
		if(pathway == 'BC-&gt;PNP-&gt;SMS'){
			postData.pathway = '3';
		}else if(pathway == 'BC-&gt;SMS'){
			postData.pathway = '2';
		}else if(pathway == 'BC'){
			postData.pathway = '1';
		}
		postData.template = template;
		postData.pnpContent = PNPContent;
		
		postData.id = pnpMaintainAccountModelId;
		
		
		$.ajax({
			type : 'POST',
			url : bcs.bcsContextPath + '/pnpAdmin/createPNPMaintainAccount',
			cache : false,
			contentType : 'application/json',
			processData : false,
			data : JSON.stringify(postData)
		}).success(function(response) {
			console.info(response);
			alert('儲存成功');
			window.location.replace('pnpNormalAccountListPage');
		}).fail(function(response) {
			console.info(response);
			var text = response.responseText;
			console.info("text:", text);
			if(text == '帳號、前方來源系統、簡訊內容不可與之前資料重複！'){
				alert('帳號、前方來源系統、簡訊內容不可與之前資料重複！');
			}else{
				$.FailResponse(response);
			}
		})
	});
	
	// do Pop Confirm
	$('#popConfirm').click(function(){
		var list = document.getElementById('pathwayList');
		pathway = list.options[list.selectedIndex].innerHTML;
		template = 'TestTemplate';
		PNPContent = $('#PNPContent')[0].value;
		
		$('.popTr').remove();
		var popTr = originalPopTr.clone(true);
		popTr.find('.pathway').html(pathway);
		popTr.find('.template').html(template);
		popTr.find('.PNPContent').html(PNPContent);
		
		$('.popTbody').append(popTr);
		
    	$('#dialog-modal').dialog("close");
    });
	
	// do Cancel
	$('input[name="cancel"]').click(function() {
		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		window.location.replace(bcs.bcsContextPath + '/pnpAdmin/pnpNormalAccountListPage');
	});
	
//		$('.LyMain').block($.BCS.blockMsgRead);
//		
//		$.ajax({
//			type : "GET",
//			url : bcs.bcsContextPath + '/edit/getTemplateMsgList'
//		}).success(function(response){
//			$('.templateMsgTrTemplate').remove();
//			var content;
//			
//			for(key in response){
//				var templateMsgTr = templateMsgTrTemplate.clone(true);
//				content = "";
//				
//				var valueObj = response[key];
//				console.info('key templateId: ', key);
//				console.info('valueObj : ', valueObj);
//				
//				var templateType = valueObj[4];
//				if(templateType == 'confirm'){
//					templateType = '確認樣板';
//				}
//				else if(templateType == 'buttons'){
//					templateType = '按鈕樣板';
//				}
//				else if(templateType == 'carousel'){
//					templateType = '滑動樣板';
//				}
//				
//				if(valueObj[1] == null){
//					templateMsgTr.find('.templateMsgImgTitle img').remove();
//				}else{
//					templateMsgTr.find('.templateMsgImgTitle img').attr('templateId', key);
//					templateMsgTr.find('.templateMsgImgTitle img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + valueObj[1]);
//					templateMsgTr.find('.templateMsgImgTitle img').click(templateMsgSelectEventFunc);
//				}
//				
//				if(valueObj[2] == null){
//					content += "無標題<br/>";
//				}else{
//					content += ("訊息標題 : " + valueObj[2] + "<br/>");
//				}
//				
//				if(valueObj[0] == " "){
//					content += "無內容";
//				}else{
//					content += ("訊息內容 : '" + valueObj[0] + "'");
//				}
//				
//				templateMsgTr.find('.templateMsgId').val(key);
//				templateMsgTr.find('.templateMsgTitle').html(content);
//				templateMsgTr.find('.templateMsgImgTitle a').attr('href', bcs.bcsContextPath + '/edit/templateMsgCreatePage?templateId=' + key + '&actionType=Edit');
//				templateMsgTr.find('.templateMsgType').html(templateType);
//				var time = valueObj[3].replace(/\.\d+$/, ''); // 刪去毫秒
//				templateMsgTr.find('.templateMsgCreateTime').html(time);
//				templateMsgTr.find('.templateMsgCreateUser').html(valueObj[5]);
//				
//				$('#templateMsgListTable').append(templateMsgTr);
//			}
//			
//		}).fail(function(response){
//			console.info(response);
//			$.FailResponse(response);
//			$('.LyMain').unblock();
//		}).done(function(){
//			$('.LyMain').unblock();
//		});
//	};
//	
//	var templateMsgSelectEventFunc = function(){
//		var templateId = $(this).attr('templateId');
// 		window.location.replace(bcs.bcsContextPath + '/edit/templateMsgCreatePage?templateId=' + templateId + '&actionType=Edit');
//	};
//	
//	$('.btn_detele').click(function(e) {
//		var deleteConfirm = confirm("請確認是否刪除");
//		if (!deleteConfirm) return; //點擊取消
//		
//		var templateMsgTr = $(this).closest(".templateMsgTrTemplate");
//		var selectedTemplateId = templateMsgTr.find('.templateMsgId').val();
//		$.ajax({
//			type : "DELETE",
//			url : bcs.bcsContextPath + '/admin/deleteTemplateMsg/' + selectedTemplateId
//		}).success(function(response){
//			alert("刪除成功！");
//			loadDataFunc();
//		}).fail(function(response){
//			console.info(response);
//			$.FailResponse(response);
//		}).done(function(){
//		});
//	});
//	
//
//	var templateMsgTrTemplate = {};
//	var initPage = function(){
//		templateMsgTrTemplate = $('.templateMsgTrTemplate').clone(true);
//		$('.templateMsgTrTemplate').remove();
//	}

	
	// ---- Initialize Page & Load Data ----
	initPage();
//	loadDataFunc();
});