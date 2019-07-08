/**
 * 
 */



$(function(){
	// Global Variables
	var noticeSwitch = 'true';
	var templateCount = 0;
	var originalTable = {};
	var originalTemplateTr = {};
	var originalTemplateMsgTrTemplate = {};
	
	// Initialize Page
	var initPage = function(){
		// clone & remove
		originalTemplateTr = $('.templateTr').clone(true); // TrTemplate
		$('.templateTr').remove();
		originalTemplateMsgTrTemplate = $('.templateMsgTrTemplate').clone(true);
		originalTable = $('.templateTable').clone(true);
		$('.templateTable').remove();
	};
	
	// load & reload
	var loadDataFunc = function(){
		templateCount = 0;
		// block
		$('.LyMain').block($.BCS.blockMsgRead);
				
		// get noticeSwitch
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getPnpBigSwitch'
		}).success(function(response){			
			noticeSwitch = response['msg'];
			$.each($('input[name="noticeSwitch"]'), function(i, v) {//template的 radio button 設定
				if (v.value == noticeSwitch) {
					v.checked = true;
				}
			});
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
		});
		
		// get all list data
		getListData('全部', '/edit/getPnpAllList');
//		getListData('生效', '/edit/getPNPOnList');
//		getListData('取消', '/edit/getPNPOffList');
		
	};
	
	// get list data
	var getListData = function(name, url){
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + url
		}).success(function(response){
			templateCount++;
			addTab(name);
			var templateMsgTrTemplate = originalTemplateMsgTrTemplate.clone(true);
			
			var content, status;
			for(key in response){
				var templateMsgTr = originalTemplateTr.clone(true);
				
				var valueObj = response[key]; // templateData
				
				//console.info('key templateId: ', key);
				//console.info('valueObj : ', valueObj);
				
				// key templateId
				templateMsgTr.find('.templateMsgId').html(key);
				
				// 0 PRODUCT_SWITCH
				if(valueObj[0] == "true"){
					status = "開<br/>";
				}else{
					status = "關<br/>";
				}
				templateMsgTr.find('.templateMsgStatus').html(status);
				
				// 2 TEMPLATE_TYPE
				var templateType = valueObj[2];
				templateMsgTr.find('.templateMsgType').html(templateType);
				
				// 3 TEMPLATE_IMAGE_ID
				templateMsgTr.find('.templateMsgImgTitle img').remove();
				
				// 4 TEMPLATE_TITLE
				if(valueObj[4] == null){
					content = "無標題<br/>";
				}else{
					content = (valueObj[4] + "<br/>");
				}
				templateMsgTr.find('.templateMsgId').val(key);
				templateMsgTr.find('.templateMsgTitle').html(content);
				templateMsgTr.find('.templateMsgImgTitle a').attr('href', bcs.bcsContextPath + '/edit/pnpCreatePage?templateId=' + key + '&actionType=Edit');				
				
				// 5 CURFEW_START_TIME + 6 CURFEW_END_TIME
				if(valueObj[5] == null || valueObj[5] == "") {
					templateMsgTr.find('.templateMsgCurfewTime').html("無<br/>");
				}else{
					var startTime = valueObj[5].replace(/\.\d+$/, ''); // 刪去毫秒
					var endTime = valueObj[6].replace(/\.\d+$/, ''); // 刪去毫秒
					//console.log("Time: ", startTime, endTime);
					
					templateMsgTr.find('.templateMsgCurfewTime').html(startTime + "~" + endTime + "<br/>");
				}
				
				// 7 MODIFY_TIME
				var createTime = valueObj[7].replace(/\.\d+$/, ''); // 刪去毫秒
				templateMsgTr.find('.templateMsgCreateTime').html(createTime);
				
				// 8 USER_NAME
				templateMsgTr.find('.templateMsgCreateUser').html(valueObj[8]);
				
				// Append to Table
				//$('.templateMsgTrTemplate').append(templateMsgTr);
				//templateTable.find('.templateMsgTrTemplate').append(templateMsgTr);
				templateMsgTrTemplate.append(templateMsgTr);

			}
			
			// clone the board
			templateTable = originalTable.clone(true);
			templateTable.attr('name', 'templateTable' + templateCount);
			templateTable.append(templateMsgTrTemplate);
			
			// append to Tab
			$('#tab'+templateCount).append(templateTable);
			
			$("#tabs").tabs({active: 0});
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			switch(name){
			case '全部':
				getListData('生效', '/edit/getPnpOnList');
				break;
			case '生效':
				getListData('取消', '/edit/getPnpOffList');
				break;
			case '取消':
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
	
	// other Functions
	// to Create Page
	$('#toCreatePage').click(function(){
 		window.location.replace('pnpCreatePage');
	});

	// to Edit Page
	var templateMsgSelectEventFunc = function(){
		var templateId = $(this).attr('templateId');
 		window.location.replace(bcs.bcsContextPath + '/edit/pnpCreatePage?templateId=' + templateId + '&actionType=Edit');
	};
	
	// to Copy Page
	$('.btn_copy').click(function(e) {
		var templateMsgTr = $(this).closest(".templateTable");
		var selectedTemplateId = templateMsgTr.find('.templateMsgId').val();
		window.location.replace(bcs.bcsContextPath + '/edit/pnpCreatePage?templateId=' + selectedTemplateId + '&actionType=Copy');
	});
	
	// do Delete
	$('.btn_detele').click(function(e) {
		var deleteConfirm = confirm("請確認是否刪除");
		if (!deleteConfirm) return; //點擊取消

		var templateMsgTr = $(this).parent().parent();
		var selectedTemplateId = templateMsgTr.find('.templateMsgId').val();
		//console.info("id:", selectedTemplateId);
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/admin/deletePnp/' + selectedTemplateId
		}).success(function(response){
			alert("刪除成功！");
			window.location.replace(bcs.bcsContextPath + '/edit/pnpListPage');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	});
	
	// click NoticeSwitch
	$(".noticeSwitch").click(function(e){
		noticeSwitch = e.currentTarget.value;
		console.info(noticeSwitch);
	});
	
	// alter NoticeSwitch
	$('#btnNoticeSwitch').click(function(){
		var r = confirm("確定要變更帳戶通知大按鈕？");
 		if(!r) return;
 		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/setPnpBigSwitch/' + noticeSwitch
		}).success(function(response){
			alert("變更成功！");
			window.location.replace(bcs.bcsContextPath + '/edit/pnpListPage');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
 		
	});
	
	// main()
	// initialize Page & load Data
	initPage();
	$("#tabs").tabs();
	loadDataFunc();
});