/**
 * 
 */
$(function(){	
	// ---- Global Variables ----
	var generalActionTr = {};    	//action的列
	var generalTemplateTable = {};  //templateTable
	var templateId = "";			//templateId
	var actionType = "";			//紀錄是編輯或新增
	var trCount = 0;				//產生的Tr數量
	var templateCount = 0;			//目前template數量
	var actionNumber = 0;			//每個template的action數
	var templateType = "";			// templateType Radio
	
	// initialize
	var initPage = function(){
		generalActionTr = $('.actionTr').clone(true); //取下action格式
		$('.actionTr').remove();//刪除action
		generalTemplateTable = $('.templateTable').clone(true);
		$('.templateTable').remove();
		templateId = $.urlParam("templateId"); //從列表頁導過來的參數
		actionType = $.urlParam("actionType"); //從列表頁導過來的參數
		
		//從列表進來
		if(templateId != null && templateId != ""){
			$.ajax({
                type: 'GET',
                url: bcs.bcsContextPath + "/edit/getBillingNotice/" + templateId,
    		}).success(function(response){
				// templateCount初始化
    			templateCount = 0;
				
    			var templateData;
    			var templateTable;
    			var actionTr;
    			var actionType;
    			var actionTypes;
    			
    			for(var key in response){
    				templateCount++;
    				addTab();
    				console.info("key :  ", key, "     response :  ", response[key]);
    				console.info(response[key].length);
    				templateData = response[key]; //取出template資料
    				
    				templateTable = generateTemplateTable();
    				
        			// 0	PRODUCT_SWITCH
    				if(templateCount <= 1){
	    				if(templateData[0] == 'true' || templateData[0] == true){
	    					templateTable.find('input[name="templateSwitch"]')[0].checked = true;
	    				}else {
	    					console.info(templateTable.find('input[name="templateSwitch"]')[1]);
	    					templateTable.find('input[name="templateSwitch"]')[1].checked = true;
	    				}
    				}

    				// 1	ALT_TEXT
    				if(templateCount <= 1){
    					templateTable.find('#altText').val(templateData[1]);
    				}
    				
    				// 2	TEMPLATE_TYPE
        			templateType = templateData[2]; // template是什麼類型
        			//console.info('templateType: ', templateType);
    				$.each($('input[name="templateType"]'), function(i, v) {//template的 radio button 設定
    					if (v.value == templateType) {
    						v.checked = true;
    					}
    				});    				
    				
    				// 3	TEMPLATE_IMAGE_ID
    				if(templateData[3] != null){
    					templateTable.find('.imgId').val(templateData[3]);
    					templateTable.find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + templateData[3]);
        			}
    				
    				// 4	TEMPLATE_TITLE
    				templateTable.find('#templateMsgTitle').val(templateData[4]);
    				
    				// 5	CURFEW_START_TIME + 6 CURFEW_END_TIME
    				if(templateCount <= 1){
	    				console.info("start:", templateData[5]);
	    				console.info("end:", templateData[6]);
	    				if(templateData[5] == null || templateData[5] == ""){
	    					templateTable.find('input[name=curfewSwitch]')[0].checked = true;
	    					templateTable.find('#curfewView').hide();
	    				}else{
	    					templateTable.find('input[name=curfewSwitch]')[1].checked = true;
	    					console.info(templateTable.find('.curfewView'));
	    					templateTable.find('.time-picker')[0].value = templateData[5];
	        				templateTable.find('.time-picker')[1].value = templateData[6];
	    				}
    				}
    				
    				// 7	TEMPLATE_TEXT
    				if(templateCount > 1){
    					templateTable.find('#contentText').val(templateData[7]);
    				}
    				
    				
    				// Hide Tr
    				if(templateCount <= 1){
    					//templateTable.find('.titleTr').css('display','none');
    					templateTable.find('.contentTextTr').css('display','none');
    				}
    				if(templateCount > 1){
    					templateTable.find('.templateSwitchTr').css('display','none');
    					templateTable.find('.curfewSwitchTr').css('display','none');
    					templateTable.find('.altTextTr').css('display','none');
    				}
    				
    				// 8-13 ACTIONS
    				actionNumber = (templateData.length-8)/6;
    				console.info('actionNumber:', actionNumber);
    				
    				templateTable.find(".actionTh").prop("rowspan", actionNumber * 3 + 3);
    	    				
    				for(var i=0; i<actionNumber; i++){
    					actionTr = generateActionTr(i);
    					    					
    					actionType1 = templateData[8+6*i];
    					
    					actionTypes = actionTr.find('.actionType');
    					
    					actionTr.find("input[name='label']").val(templateData[9+6*i]);
    					switch(actionType1){
							case 'postback':
								actionTr.find("input[name='data']").val(templateData[10+6*i]);
								break;
							case 'message':
								actionTr.find("input[name='text']").val(templateData[11+6*i]);
								break;
							case 'uri':
								actionTr.find("input[name='text']").val(templateData[12+6*i]);
								break;
    					}
    					
    					templateTable.append(actionTr);
    					$.each(actionTypes, function(i, o) {
    						if ($(o).val() == actionType1) {
    							console.info("click   ",actionType1);
    							$(o).click();
    						}
    					});
    				}
    				
    				$('#tab'+templateCount).append(templateTable);
    				$("#tabs").tabs({active: 0});
    			}

    			setTemplateOperationButtonsVisable();
    			setActionOperationButtonsVisable();
    			initButtonsVisable(templateType);
    			
    		}).fail(function(response){
    			console.info(response);
    			$.FailResponse(response);
    		}).done(function(){
    		});
			
		//從menu進來
			
		}else{
			// 生成模式
			actionType = "Create";
			
			// templateTypes0 check
			var templateTypes = $("input[name='templateType']");
			templateTypes[0].checked = true;
			$("input[name='templateType']:checked").trigger("click");
			
			// templateSwitch0 check
			var templateSwitches = $("input[name='templateSwitch']");
			templateSwitches[0].checked = true;
			$("input[name='templateSwitch']:checked").trigger("click");			
			
			// curfewSwitch0 check
			var curfewSwitches = $("input[name='curfewSwitch']");
			curfewSwitches[1].checked = true;
			$("input[name='curfewSwitch']:checked").trigger("click");
			
			initButtonsVisable(templateType);
		}
	}
	
	// select templateType
	$("input[name='templateType']").click(function(e){
		templateType = e.currentTarget.value;
		templateCount=0;
		actionNumber=0;
		$('.tabLi').remove();
		$('.tabDiv').remove();
		addTemplate();
		addAction();
		
		// templateSwitch0 check
		var templateSwitches = $("input[name='templateSwitch']");
		templateSwitches[0].checked = true;
		$("input[name='templateSwitch']:checked").trigger("click");			
		
		// curfewSwitch0 check
		var curfewSwitches = $("input[name='curfewSwitch']");
		curfewSwitches[1].checked = true;
		$("input[name='curfewSwitch']:checked").trigger("click");
		
		initButtonsVisable(templateType);
	});
	
	//---------radios綁圖片---------
	var radios = $("input[name='templateType']");
	$.each(radios, function(i, o){
		$(o).closest('.typeMenu').find('img').click(function(){
			$(o).click();
		});
	})
	
	//----------選擇action類型-----------
	$(".actionType").click(function(e){
		var selectedActionType = e.currentTarget.value;
		
		var actionTr = $(this).closest(".actionTr");
		switch(selectedActionType){
			case 'uri' :
				actionTr.next().next().css('display','none');
				actionTr.next().css('display','');
				actionTr.next().find('p:first').html('連結<span style="color: red">*</span>');
				break;
			case 'message' :
				actionTr.next().next().css('display','none');
				actionTr.next().css('display','');
				actionTr.next().find('p:first').html('文字訊息<span style="color: red">*</span>');
				//actionTr.next().find(input).attr("placeholder","限300個字");
				break;
			case 'postback' :
				actionTr.next().next().css('display','');
				actionTr.next().css('display','none');
				break;
		}
	});
	
	//----------選擇curfewSwitch類型-----------
	$(".curfewSwitch").click(function(e){
		var selectedCurfewSwitch = e.currentTarget.value;
		
		//var curfewView = $("#curfewView");
		switch(selectedCurfewSwitch){
			case 'off' :
				$("#curfewView").hide();
				break;
			case 'on' :
				$("#curfewView").show();
				break;
		}
	});
	
	//-------增加樣板----------
	var addTemplate = function() {
		templateCount++;
		addTab();
		var templateTable = generateTemplateTable();
		var actionTr;
		
		templateTable.find(".actionTh").prop("rowspan", actionNumber * 3 + 3);

		if(templateCount<=1){
			//templateTable.find('.titleTr').css('display','none');
			templateTable.find('.contentTextTr').css('display','none');
		}
		
		if(templateCount>1){
			templateTable.find('.templateSwitchTr').css('display','none');
			templateTable.find('.curfewSwitchTr').css('display','none');
			templateTable.find('.altTextTr').css('display','none');
		}
		
		for(var i=0; i<actionNumber; i++){
			actionTr = generateActionTr(i);
			templateTable.append(actionTr);
		}
		
		setTemplateOperationButtonsVisable();
		$('#tab'+templateCount).append(templateTable);
	}
	
	$('#addTable').click(addTemplate);
	
	//-------刪除樣板----------
	var deleteTemplate = function() {
		templateCount--;
		deleteTab();
		
		setTemplateOperationButtonsVisable();
	}
	
	$('#deleteTable').click(deleteTemplate);
	
	//-----------動態產生templateTable-----------------------
	var generateTemplateTable = function(){
		var templateTable = generalTemplateTable.clone(true);
		var templateTableTarget = 'templateTable' + templateCount;
		
		templateTable.attr('name', templateTableTarget);
		
		return templateTable;
	}
	
	
	
	//-------所有樣板增加動作----------
	var addAction = function() {
		var templateTables = $('.templateTable');
		var actionTr;
		
		for(var i=0;i<templateCount;i++){
			actionTr = generateActionTr(actionNumber);
			$(templateTables[i]).find(".actionTh").prop("rowspan", actionNumber * 3 + 3);
			$(templateTables[i]).append(actionTr);
		}

		actionNumber++;
		setActionOperationButtonsVisable();
	}
	
	$('#addAction').click(addAction);
	
	//-------所有樣板刪除動作-------------
	deleteAction = function() {
		actionNumber--;
		
		$('.templateTable .actionTr:last-child').remove();
		$('.templateTable .actionTr:last-child').remove();
		$('.templateTable .actionTr:last-child').remove();

		setActionOperationButtonsVisable();
	}
	
	$('#deleteAction').click(deleteAction);
	
	//-----------動態產生actionTr-----------
	var generateActionTr = function(actionNumber){
		var actionTr = generalActionTr.clone(true);
		trCount++;
		
		var actionTarget = 'ActionType' + trCount;
		var actionType = actionTr.find(".actionType");
		actionType.attr('name', actionTarget);
		actionType[0].checked = true;
		actionTr.find(".actionType:checked").trigger("click");
		var letter = String.fromCharCode(65 + actionNumber);
		actionTr.find(".typeSideTxt").html(letter);
		
		return actionTr;
	}
	
	// add Tab
	var addTab = function(){
        var target;
        
        $("#tabs ul").append(
    		"<li class='tabLi'><a href='#tab" + templateCount + "'>Column" + templateCount + "</a></li>"
    	);
        
        $("#tabs").append(
            "<div class='tabDiv' id='tab" + templateCount + "'></div>"
        );
        
        $("#tabs").tabs("refresh");
        $("#tabs").tabs({ active: templateCount-1 });
    };
    
    // delete Tab
	var deleteTab = function(){
        $('.tabLi:last').remove();
        $('.tabDiv:last').remove();
        
        $("#tabs").tabs("refresh");
        $("#tabs").tabs({ active: templateCount-1 });
    };
	
	//設定button的Visable
	var setActionOperationButtonsVisable = function(){
		if(templateType == "carousel"){
			if(actionNumber == 3){
				$('#addAction').hide();
				$('#deleteAction').show();
			}
			else if(actionNumber == 1){
				$('#addAction').show();
				$('#deleteAction').hide();
			}else{
				$('#addAction').show();
				$('#deleteAction').show();
			}
		}else{
			if(actionNumber == 4){
				$('#addAction').hide();
				$('#deleteAction').show();
			}
			else if(actionNumber == 1){
				$('#addAction').show();
				$('#deleteAction').hide();
			}else{
				$('#addAction').show();
				$('#deleteAction').show();
			}
		}
		
	}
	
	var setTemplateOperationButtonsVisable = function(){
		if(templateCount == 5){
			$('#addTable').hide();
			$('#deleteTable').show();
		}
		else if(templateCount == 1){
			$('#addTable').show();
			$('#deleteTable').hide();
		}else{
			$('#addTable').show();
			$('#deleteTable').show();
		}
	}
	
	var initButtonsVisable = function(templateType){
		// button -> hide
		switch(templateType){
			case 'buttons' :
				$('#addTable').hide();
				$('#deleteTable').hide();
				break;
		}
	}
	
	//取得template的資料
	var getTemplateTableInformation = function(templateTable){
		var actionTypeTds = templateTable.find('.actionTypeTd');
		var actions = [];
		var templateData = {};
		
		for(var i=0; i<actionNumber; i++){
			actions.push({
					actionLetter : $(actionTypeTds[i]).find('.typeSideTxt').text(),
					actionType : $(actionTypeTds[i]).find('.actionType:checked').val(),
					actionLabel : $(actionTypeTds[i]).closest('.actionTr').find("input[name='label']").val(),
					actionData : $(actionTypeTds[i]).closest('.actionTr').next().next().find("input[name='data']").val(),
					actionText : $(actionTypeTds[i]).closest('.actionTr').next().find("input[name='text']").val()
			});
		}
		
		templateData.templateTitle = templateTable.find('#templateMsgTitle').val();
		
		templateData.curfewStartTime = templateTable.find('#curfewStartTime').val();
		templateData.curfewEndTime = templateTable.find('#curfewEndTime').val();
		var curfewSwitches = templateTable.find('.curfewSwitch');
		if(curfewSwitches[0].checked){
			templateData.curfewStartTime = "";
			templateData.curfewEndTime = "";			
		}
		
		// templateSwitches
		var templateSwitches = templateTable.find('.templateSwitch');
		//console.log(templateSwitches[0].checked); //on == true
		//console.log(templateSwitches[1].checked);
		templateData.templateSwitch = templateSwitches[0].checked;
		
		templateData.altText = templateTable.find('#altText').val();
		
		templateData.templateText = templateTable.find('#contentText').val();
		console.info("templateData.templateText = ", templateData.templateText);
		
		templateData.templateType = templateType;
		templateData.templateImageId = templateTable.find('.imgId').val();
		templateData.templateActions = actions;
		
		console.log(templateData);
		
		return templateData;
	}	
	
	
	// ---- Functions ----
	// do Save
	$('#save').click(function(){
		var templateTables = $('.templateTable');
		var actions = [];
		var templates=[];
		var templateLevel;
			
		if(!checkTemplateTableValid(templateTables)){
			return;
		}
		
		for(var i=0; i<templateCount;i++){
			templateData = getTemplateTableInformation($(templateTables[i]));
			
			if(i>0){
				templateData.templateLevel = "COLUMN";
			}else{
				templateData.templateLevel = "MAIN";
			}

			templateData.templateLetter = i;
			
			templates.push(templateData);
		}
		
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/edit/createBillingNotice?actionType=' + actionType + '&templateId=' + templateId,
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(templates)
		}).success(function(response){
			console.info('response',response);
			
			if (actionType == "Edit") {
				alert("儲存樣板訊息成功！");
			} else {
				alert("建立樣板訊息成功！");
			}
			
			window.location.replace(bcs.bcsContextPath + '/edit/billingNoticeListPage');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	});
	
	// validate Time
	var checkTimeValid = function(timeString){
		console.log("timeString:", timeString);
		var aDate = moment(timeString, 'HH:mm:ss', true);
		console.log("Valid:", aDate.isValid());
		return aDate.isValid();
	}
	
	// validate Table
	var checkTemplateTableValid = function(templateTables){
		var templateTable;
		var actionTypeTds;
		var actionType;
		var imageIsEmpty;
		var titleIsEmpty;
		
		templateTable = $(templateTables[0]);
		
		var curfewSwitches = templateTable.find('.curfewSwitch');
		if(!curfewSwitches[0].checked){
			var curfewStartTime = templateTable.find('#curfewStartTime').val();
			var curfewEndTime = templateTable.find('#curfewEndTime').val();
			
			console.log(curfewStartTime);
			console.log(curfewEndTime);
			
			if(!checkTimeValid(curfewStartTime)){
				alert("宵禁起始時間格式錯誤！");
				return false;
			}
			if(!checkTimeValid(curfewEndTime)){
				alert("宵禁起始時間格式錯誤！");
				return false;
			}			
			if(curfewStartTime ==  curfewEndTime){
				alert("宵禁起始時間與宵禁結束時間不可相同！");
				return false;
			}
		}
		
		for(var i=0; i<templateCount; i++){
			templateTable = $(templateTables[i]);
			actionTypeTds = templateTable.find('.actionTypeTd');
			
			if(i == 0){
				imageIsEmpty = (templateTable.find('.imgId').val() == "");
			}else if(templateTable.find('.imgId').val() == "" ^ imageIsEmpty){
				alert("必須所有column都有圖片或都沒有!");
				return false;
			}

			// skip title check
//			if(i == 0){
//				titleIsEmpty = (templateTable.find('#templateMsgTitle').val() == "");
//			}else if(templateTable.find('#templateMsgTitle').val() == "" ^ titleIsEmpty){
//				alert("必須所有column都有標題或都沒有!");
//				return false;
//			}

			if (i == 0) { // Column 1
				var altTextMsg = templateTable.find('#altText').val();
				console.info("Column" + (i + 1) + '非手機顯示訊息 = ' + altTextMsg);
				
				if (altTextMsg == "") {
					alert("Column" + (i + 1) + "必須輸入非手機顯示訊息！");
					return false;
				}
			}
			else { // Column 2, Column3, ...., Column5
				var contextTextMsg = templateTable.find('#contentText').val();
				console.info("Column" + (i + 1) + "訊息內容 = " + contextTextMsg);

				if (contextTextMsg == "" && i >= 1) {
					alert("Column" + (i + 1) + "訊息內容不能為空，請確認是否已正確填寫?");
					return false;
				}

				if (contextTextMsg.length > 60) {
					alert("Column" + (i + 1) + "訊息內容不能超過60個字，請重新輸入。");
					return false;
				}
			}
//				
//			var altTextMsg = templateTable.find('#altText').val();
//			console.info('altTextMsg = ', altTextMsg);
//			
//			if (altTextMsg == "" && i == 0) {
//				alert("Column" + (i + 1) + "必須輸入非手機顯示訊息！");
//				return false;
//			}
			
			for(var j=0; j<actionNumber; j++){
				actionType = $(actionTypeTds[j]).find('.actionType:checked').val();
				if($(actionTypeTds[j]).closest('.actionTr').find("input[name='label']").val() == ""){
					alert("Column"+ (i+1) + "的動作" + $(actionTypeTds[j]).find('.typeSideTxt').text() +"必須輸入按鈕文字！");
					return false;
				}
				switch(actionType){
					case 'uri' :
						if($(actionTypeTds[j]).closest('.actionTr').next().find("input[name='text']").val() == ""){
							alert("Column"+ (i+1) + "的動作" + $(actionTypeTds[j]).find('.typeSideTxt').text() +"必須輸入連結！");
							return false;
						}
						if (!$(actionTypeTds[j]).closest('.actionTr').next().find("input[name='text']").val().lastIndexOf('http://', 0)==0 
								&& !$(actionTypeTds[j]).closest('.actionTr').next().find("input[name='text']").val().lastIndexOf('https://', 0)==0
								&& !$(actionTypeTds[j]).closest('.actionTr').next().find("input[name='text']").val().lastIndexOf('BcsPage:', 0)==0) {
							alert("連結必須包含http或是BcsPage字樣！");
							return false;
						}
						break;
					case 'message' :
						if($(actionTypeTds[j]).closest('.actionTr').next().find("input[name='text']").val() == ""){
							alert("Column"+ (i+1) + "的動作" + $(actionTypeTds[j]).find('.typeSideTxt').text() +"必須輸入文字訊息！");
							return false;
						}
						break;
					case 'postback' :
						if($(actionTypeTds[j]).closest('.actionTr').next().next().find("input[name='data']").val() == ""){
							alert("Column"+ (i+1) + "的動作" + $(actionTypeTds[j]).find('.typeSideTxt').text() +"必須輸入隱藏訊息！");
							return false;
						}
						break;
				}
			}
		}
		return true;
	}
	
	// do Cancel
	$('input[name="cancel"]').click(function() {
		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		window.location.replace(bcs.bcsContextPath + '/edit/billingNoticeListPage');
	});
	
	// do Upload Image
	$("#titleImage").on("change", function(e) {
		var input = e.currentTarget;
    	if (input.files && input.files[0]) {
    		if(input.files[0].size < 1048576) {
	    		var fileName = input.files[0].name;
	    		var form_data = new FormData();
	    		
	    		form_data.append("filePart",input.files[0]);
	
	    		$('.LyMain').block($.BCS.blockMsgUpload);
	    		$.ajax({
	                type: 'POST',
	                url: bcs.bcsContextPath + "/edit/createResource?resourceType=IMAGE",
	                cache: false,
	                contentType: false,
	                processData: false,
	                data: form_data
	    		}).success(function(response){
	            	console.info(response);
	            	alert("上傳成功!");
	            	$(input).closest('.MdFRM03File').find('.imgId').val(response.resourceId);
	            	$(input).closest('.MdFRM03File').find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
	    		}).fail(function(response){
	    			console.info(response);
	    			$.FailResponse(response);
	    			$('.LyMain').unblock();
	    		}).done(function(){
	    			$('.LyMain').unblock();
	    		});
    		} else {
    			alert("圖片大小不可大於 1MB！");
    		}
        } 
	});
	
	// ---- Initialize & LoadDataFunction ----
	$("#tabs").tabs();
	initPage();
});