/**
 * 
 */
$(function(){	
	//全域變數
	var generalActionTr = {};    	//action的列
	var generalTemplateTable = {};  //templateTable
	var templateId = "";			//templateId
	var templateType = "";			//templateType
	var actionType = "";			//紀錄是編輯或新增
	var trCount = 0;			//產生的Tr數量
	var actionNumber = 0;			//每個template的action數
	var templateCount = 0;		//目前template數量
	
	//-------初始化頁面----------
	var initPage = function(){
		generalActionTr = $('.actionTr').clone(true);//取下action格式
		$('.actionTr').remove();//刪除action
		generalTemplateTable = $('.templateTable').clone(true);
		$('.templateTable').remove();
		templateId = $.urlParam("templateId"); //從列表頁導過來的參數
		actionType = $.urlParam("actionType"); //從列表頁導過來的參數
		
		//從列表進來
		if(templateId != null && templateId != ""){
			$.ajax({
                type: 'GET',
                url: bcs.bcsContextPath + "/edit/getTemplateMsg/" + templateId,
    		}).success(function(response){
    			templateCount = 0;
    			templateType = (response[templateId])[1];//template是什麼類型
    			console.info('templateType    ',templateType);
				$.each($('input[name="templateType"]'), function(i, v) {//template的rdiobutton設定
					if (v.value == templateType) {
						v.checked = true;
					}
				});
    			
    			var templateData;
    			var templateTable;
    			var actionTr;
    			var actionType;
    			var actionTypes;
    			
    			for(var key in response){
    				templateCount++;
    				addTab();
    				console.info("key :  ",key,"     response :  ",response[key]);
    				templateData = response[key];//取出template資料
    				
    				templateTable = generateTemplateTable();

    				templateTable.find('#altText').val(templateData[0]);
    				templateTable.find('#templateMsgTitle').val(templateData[3]);
    				templateTable.find('#templateMsgText').val(templateData[4]);
    				
    				if(templateData[2] != null){
    					templateTable.find('.imgId').val(templateData[2]);
    					templateTable.find('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + templateData[2]);
        			}
    				
    				actionNumber = (templateData.length-6)/6;
    				templateTable.find(".actionTh").prop("rowspan", actionNumber * 3 + 3);
    				    				
    				for(var i=0; i<actionNumber; i++){
    					actionTr = generateActionTr(i);
    					    					
    					actionType = templateData[6+6*i];
    					actionTypes = actionTr.find('.actionType');
    					
    					actionTr.find("input[name='label']").val(templateData[7+6*i]);
    					switch(actionType){
						case 'postback':
							actionTr.find("input[name='data']").val(templateData[8+6*i]);
							break;
						case 'message':
							actionTr.find("input[name='text']").val(templateData[9+6*i]);
							break;
						case 'uri':
							actionTr.find("input[name='text']").val(templateData[10+6*i]);
							break;
    					}
    					
    					templateTable.append(actionTr);
    					$.each(actionTypes, function(i, o) {
    						if ($(o).val() == actionType) {
    							console.info("click   ",actionType);
    							$(o).click();
    						}
    					});
    				}
    				
    				$('#tab'+templateCount).append(templateTable);
    				
    				if(templateType == 'confirm'){
        				$('#addTable').hide();
        				$('#addAction').hide();
        				$('#deleteAction').hide();
        				$('#deleteTable').hide();
        				$('.templateTable').find('.imageTr').hide();
        				$('.templateTable').find('.titleTr').hide();
        			}
    			}
    			
    			$("#tabs").tabs({active: 0});
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
			var templateTypes = $("input[name='templateType']");
			templateTypes[0].checked = true;
			$("input[name='templateType']:checked").trigger("click");
			actionType = "Create";
		}
	}
	
	//------選擇template-----------
	$("input[name='templateType']").click(function(e){
		templateType = e.currentTarget.value;
		templateCount=0;
		actionNumber=0;
		$('.tabLi').remove();
		$('.tabDiv').remove();
		addTemplate();
		addAction();
		
		if(templateType == "confirm"){
			addAction();
			$('.templateTable').find('.imageTr').hide();
			$('.templateTable').find('.titleTr').hide();
		}
		
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
	
	//-------增加樣板----------
	var addTemplate = function() {
		templateCount++;
		addTab();
		var templateTable = generateTemplateTable();
		var actionTr;
		
		templateTable.find(".actionTh").prop("rowspan", actionNumber * 3 + 3);
		
		if(templateCount>1){
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
	
	//-----------動態產生templateTable-----------------------
	var generateTemplateTable = function(){
		var templateTable = generalTemplateTable.clone(true);
		var templateTableTarget = 'templateTable' + templateCount;
		templateTable.attr('name', templateTableTarget);
		
		return templateTable;
	}
	
	$('#deleteTable').click(deleteTemplate);
	
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
	
	//動態增加tab
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
    
    //刪除tab
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
		switch(templateType){
			case 'buttons' :
				$('#addTable').hide();
				$('#deleteTable').hide();
				break;
			case 'confirm' :
				$('#addTable').hide();
				$('#deleteTable').hide();
				$('#addAction').hide();
				$('#deleteAction').hide();
				break;
		}
	}
	
	//---------儲存template
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
			url : bcs.bcsContextPath + '/edit/createTemplateMsg?actionType=' + actionType + '&templateId=' + templateId,
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
			
			window.location.replace(bcs.bcsContextPath + '/edit/templateMsgListPage');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	});
	
	//檢查是否有不合法資料
	var checkTemplateTableValid = function(templateTables){
		var templateTable;
		var actionTypeTds;
		var actionType;
		var imageIsEmpty;
		var titleIsEmpty;
		
		for(var i=0; i<templateCount; i++){
			templateTable = $(templateTables[i]);
			actionTypeTds = templateTable.find('.actionTypeTd');
			
			if(i == 0){
				imageIsEmpty = (templateTable.find('.imgId').val() == "");
			}else if(templateTable.find('.imgId').val() == "" ^ imageIsEmpty){
				alert("必須所有column都有圖片或都沒有!");
				return false;
			}
			
			if(i == 0){
				titleIsEmpty = (templateTable.find('#templateMsgTitle').val() == "");
			}else if(templateTable.find('#templateMsgTitle').val() == "" ^ titleIsEmpty){
				alert("必須所有column都有標題或都沒有!");
				return false;
			}
			
			if(templateTable.find('#altText').val() == "" && i==0){
				alert("Column"+ (i+1) + "必須輸入非手機顯示訊息！");
				return false;
			}
			
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
		
		templateData.altText = templateTable.find('#altText').val();
		templateData.templateType = templateType;
		templateData.templateImageId = templateTable.find('.imgId').val();
		templateData.templateTitle = templateTable.find('#templateMsgTitle').val();
		templateData.templateText = templateTable.find('#templateMsgText').val();
		templateData.templateActions = actions;
		
		
		return templateData;
	}	
	
	//取消鈕
	$('input[name="cancel"]').click(function() {
		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		window.location.replace(bcs.bcsContextPath + '/edit/templateMsgListPage');
	});
	
	//--------上傳圖片------------------
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
	
	$("#tabs").tabs();
	
	initPage();
});