/**
 * 
 */

$(function(){	
	$('.btn_add').click(function(){
 		window.location.replace('templateMsgCreatePage');
	});
	
	var loadDataFunc = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getTemplateMsgList'
		}).success(function(response){
			$('.templateMsgTrTemplate').remove();
			var content;
			
			for(key in response){
				var templateMsgTr = templateMsgTrTemplate.clone(true);
				content = "";
				
				var valueObj = response[key];
				console.info('key templateId: ', key);
				console.info('valueObj : ', valueObj);
				
				var templateType = valueObj[4];
				if(templateType == 'confirm'){
					templateType = '確認樣板';
				}
				else if(templateType == 'buttons'){
					templateType = '按鈕樣板';
				}
				else if(templateType == 'carousel'){
					templateType = '滑動樣板';
				}
				
				if(valueObj[1] == null){
					templateMsgTr.find('.templateMsgImgTitle img').remove();
				}else{
					templateMsgTr.find('.templateMsgImgTitle img').attr('templateId', key);
					templateMsgTr.find('.templateMsgImgTitle img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + valueObj[1]);
					templateMsgTr.find('.templateMsgImgTitle img').click(templateMsgSelectEventFunc);
				}
				
				if(valueObj[2] == null){
					content += "無標題<br/>";
				}else{
					content += ("訊息標題 : " + valueObj[2] + "<br/>");
				}
				
				if(valueObj[0] == " "){
					content += "無內容";
				}else{
					content += ("訊息內容 : '" + valueObj[0] + "'");
				}
				
				templateMsgTr.find('.templateMsgId').val(key);
				templateMsgTr.find('.templateMsgTitle').html(content);
				templateMsgTr.find('.templateMsgImgTitle a').attr('href', bcs.bcsContextPath + '/edit/templateMsgCreatePage?templateId=' + key + '&actionType=Edit');
				templateMsgTr.find('.templateMsgType').html(templateType);
				var time = valueObj[3].replace(/\.\d+$/, ''); // 刪去毫秒
				templateMsgTr.find('.templateMsgCreateTime').html(time);
				templateMsgTr.find('.templateMsgCreateUser').html(valueObj[5]);
				
				$('#templateMsgListTable').append(templateMsgTr);
			}
			
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};
	
	var templateMsgSelectEventFunc = function(){
		var templateId = $(this).attr('templateId');
 		window.location.replace(bcs.bcsContextPath + '/edit/templateMsgCreatePage?templateId=' + templateId + '&actionType=Edit');
	};
	
	$('.btn_detele').click(function(e) {
		var deleteConfirm = confirm("請確認是否刪除");
		if (!deleteConfirm) return; //點擊取消
		
		var templateMsgTr = $(this).closest(".templateMsgTrTemplate");
		var selectedTemplateId = templateMsgTr.find('.templateMsgId').val();
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/admin/deleteTemplateMsg/' + selectedTemplateId
		}).success(function(response){
			alert("刪除成功！");
			loadDataFunc();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	});
	
	$('.btn_copy').click(function(e) {
		var templateMsgTr = $(this).closest(".templateMsgTrTemplate");
		var selectedTemplateId = templateMsgTr.find('.templateMsgId').val();
		window.location.replace(bcs.bcsContextPath + '/edit/templateMsgCreatePage?templateId=' + selectedTemplateId + '&actionType=Copy');
	});
	
	var templateMsgTrTemplate = {};
	var initPage = function(){
		templateMsgTrTemplate = $('.templateMsgTrTemplate').clone(true);
		$('.templateMsgTrTemplate').remove();
	}
	
	initPage();
	loadDataFunc();
});