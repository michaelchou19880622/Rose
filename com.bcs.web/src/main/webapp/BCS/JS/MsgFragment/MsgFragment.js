/**
 * 
 */
$(function(){
	
    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 設定 發送內容 選項
    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	var MsgFrameTemplate = {};
	
	var initMsgTemplate = function(){
		
		var MsgFrames = $('.MsgPlace .MsgFrame');
		
		$.each(MsgFrames, function(i, o){
			var MsgFrame = $(o).clone();
			MsgFrameTemplate[MsgFrame.attr('type')] = MsgFrame;
		});

		$('.MsgPlace .MsgFrame').remove();
//		console.info('MsgFrameTemplate', MsgFrameTemplate);
	};

	initMsgTemplate();
	
	$.BCS.msgFrameCount = 0;
	
	$.BCS.createMsgFrame = function(msgType, settingObj){
		$.BCS.msgFrameCount++;
		
		// Set Each Event
		if(msgType == "STICKER"){
			/**
			 * Sticker MsgFrame Setting
			 */
			createMsgFrameSTICKER(msgType, settingObj);
		}
		else if(msgType == "IMAGE"){
			/**
			 * Image MsgFrame Setting
			 */
			createMsgFrameIMAGE(msgType, settingObj);
		}
		else if(msgType == "VIDEO"){
			/**
			 * Video MsgFrame Setting
			 */
			createMsgFrameVIDEO(msgType, settingObj);
		}
		else if(msgType == "AUDIO"){
			/**
			 * Audio MsgFrame Setting
			 */
			createMsgFrameAUDIO(msgType, settingObj);
		}
		else if(msgType == "TEXT"){
			createMsgFrameTEXT(msgType, settingObj);
		}
		else if(msgType == "LINK"){
			createMsgFrameLINK(msgType, settingObj);
		}
		else if(msgType == "RICH_MSG"){
			createMsgFrameRICH_MSG(msgType, settingObj);
		}
		else if(msgType == "COUPON"){
			createMsgFrameCOUPON(msgType, settingObj);
		}
		else if(msgType == "REWARDCARD"){
			createMsgFrameREWARDCARD(msgType, settingObj);
		}
		else if(msgType == "TEMPLATE"){
			createMsgFrameTEMPLATE_MSG(msgType, settingObj);
		}
		else{
			var appendBody = MsgFrameTemplate[msgType].clone();
			
			appendBody.css('display', '');
			
			appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);
		
			appendMsg(appendBody);

			if($.BCS.actionTypeParam == "Look"){
				$('.MdBtn03Delete').remove();
			}
		}
	}

	// 建立 STICKER
	var createMsgFrameSTICKER = function(msgType, settingObj){

		/**
		 * Sticker MsgFrame Setting
		 */
		if(settingObj){
		    $('.stickerSelectSolid [data-stkid="' + settingObj.referenceId +'"]').click();
		}
		else{
			// open loadImage
			var loadImage = $('#dialogStickerSelect').attr('loadImage');
			if(!loadImage){
				console.info('dialogStickerSelect loadImage');
				var stickerSelectSolids = $('.stickerSelectSolid li');
				$.each(stickerSelectSolids, function(i , o){
					var datastkid = $(o).attr('data-stkid');
					$(o).html('<img src="' + bcs.bcsResourcePath + '/images/Stickers/' + datastkid + '_key.png" alt="Type2" style="cursor: pointer;">');
				});
				$('#dialogStickerSelect').attr('loadImage', 'true');
			}
			
			$('#dialogStickerSelect').dialog('open');
		}
	}

	// 建立 COUPON
	var createMsgFrameCOUPON = function(msgType, settingObj){
		/**
		 * Coupon MsgFrame Setting
		 */
		if(settingObj){
			var valueObj = $.BCS.ResourceMap[settingObj.referenceId];
			var src = bcs.bcsContextPath + "/getResource/IMAGE/" + valueObj.couponListImageId;
			settingCouponSelectResult(settingObj.referenceId, src, valueObj.couponDescription, valueObj.couponTitle);
		}
		else{
			// Get Image
			var loadImage = $('#couponListTable').attr('loadImage');
			if(!loadImage){
				console.info('couponListTable loadImage');
				var couponImgTitle = $('#couponListTable .couponImgTitle img');
				$.each(couponImgTitle, function(i , o){
					var srcUrl = $(o).attr('srcUrl');
					$(o).attr('src', srcUrl);
				});
				$('#couponListTable').attr('loadImage', 'true');
			}
			
			$('#dialogCouponSelect').dialog('open');
		}
	}
	
	// 建立 REWARDCARD
	var createMsgFrameREWARDCARD = function(msgType, settingObj){
		/**
		 * REWARDCARD MsgFrame Setting
		 */
		
		if(settingObj){
			var valueObj = $.BCS.ResourceMap[settingObj.referenceId];
			var src = bcs.bcsContextPath + "/getResource/IMAGE/" + valueObj.rewardCardListImageId;
			settingRewardCardSelectResult(settingObj.referenceId, src, valueObj.rewardCardDescription, valueObj.rewardCardTitle);
		}
		else{
			// Get Image
			var loadImage = $('#rewardCardListTable').attr('loadImage');
			if(!loadImage){
				console.info('rewardCardListTable loadImage');
				var rewardCardImgTitle = $('#rewardCardListTable .rewardCardImgTitle img');
				$.each(rewardCardImgTitle, function(i , o){
					var srcUrl = $(o).attr('srcUrl');
					$(o).attr('src', srcUrl);
				});
				$('#rewardCardListTable').attr('loadImage', 'true');
			}
			
			$('#dialogRewardCardSelect').dialog('open');
		}
	}
	
	// 建立 RICH_MSG
	var createMsgFrameRICH_MSG = function(msgType, settingObj){

		/**
		 * Sticker MsgFrame Setting
		 */
		if(settingObj){
			var valueObj = $.BCS.ResourceMap[settingObj.referenceId];
			var src = bcs.bcsContextPath + "/getResource/IMAGE/" + valueObj.richImageId;
			settingRichMsgSelectResult(settingObj.referenceId, src);
		}
		else{
			// Get Image
			var loadImage = $('#richMsgListTable').attr('loadImage');
			if(!loadImage){
				console.info('richMsgListTable loadImage');
				var richMsgImgTitle = $('#richMsgListTable .richMsgImgTitle img');
				$.each(richMsgImgTitle, function(i , o){
					var srcUrl = $(o).attr('srcUrl');
					$(o).attr('src', srcUrl);
				});
				$('#richMsgListTable').attr('loadImage', 'true');
			}
			
			$('#dialogRichMsgSelect').dialog('open');
		}
	}
	
	// 建立 TEMPLATE_MSG
	var createMsgFrameTEMPLATE_MSG = function(msgType, settingObj){

		/**
		 * Sticker MsgFrame Setting
		 */
		if(settingObj){
			var valueObj = $.BCS.ResourceMap[settingObj.referenceId];
			var src = bcs.bcsContextPath + "/getResource/IMAGE/" + valueObj.templateImageId;
			
			var title = valueObj.templateTitle;
			if(valueObj.templateType == 'confirm'){
				src = "";
				title = valueObj.templateText;
			}
			settingTemplateMsgSelectResult(valueObj.templateType, title, valueObj.templateId, src);
		}
		else{			
			$('#dialogTemplateMsgSelect').dialog('open');
		}
	}

	// 建立 IMAGE
	var createMsgFrameIMAGE = function(msgType, settingObj){

		var appendBody = MsgFrameTemplate[msgType].clone();

		appendBody.css('display', '');

		appendBody.find('.createResourceImage').css('cursor', 'pointer');
		appendBody.find('.createResourceImage').on("change", function(ev){

			var input = ev.currentTarget;
        	if (input.files && input.files[0]) {
        		if(input.files[0].size < 1048576) {
	        		var fileName = input.files[0].name;
	        		console.info("fileName : " + fileName);
	        		var form_data = new FormData();
	        		
	        		form_data.append("filePart",input.files[0]);
	
	        		$('.LyMain').block($.BCS.blockMsgUpload);
	        		$.ajax({
	                    type: 'POST',
	                    url: bcs.bcsContextPath + '/edit/createResource?resourceType=IMAGE',
	                    cache: false,
	                    contentType: false,
	                    processData: false,
	                    data: form_data
	        		}).success(function(response){
	                	console.info(response);
	                	alert("上傳成功!");
	                	appendBody.find('.resourceId').val(response.resourceId);
	                	appendBody.find('img').attr('src',bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
	        		}).fail(function(response){
	        			console.info(response);
	        			$.FailResponse(response);
	        			$('.LyMain').unblock();
	        		}).done(function(){
	        			$('.LyMain').unblock();
	        		});
        		} else {
        			alert("圖片大小不可大於1MB！");
        		}
            } 
		});
		appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);
		
		// Add Validate TODO
		
		if(settingObj){
			appendBody.find('.resourceId').val(settingObj.referenceId);
        	appendBody.find('img').attr('src',bcs.bcsContextPath + '/getResource/IMAGE/' + settingObj.referenceId);
		}
	
		appendMsg(appendBody);

		if($.BCS.actionTypeParam == "Look"){
			$('.MdBtn03Delete').remove();
			$('.MdBtnUpload').remove();
		}
	}

	// 建立 VIDEO
	var createMsgFrameVIDEO = function(msgType, settingObj){

		var appendBody = MsgFrameTemplate[msgType].clone();

		appendBody.css('display', '');

		appendBody.find('.createResourceVideo').css('cursor', 'pointer');
		appendBody.find('.createResourceVideo').on("change", function(ev){

			var input = ev.currentTarget;
        	if (input.files && input.files[0]) {
        		if(input.files[0].size < 10485760) {
	        		var fileName = input.files[0].name;
	        		console.info("fileName : " + fileName);
	        		var form_data = new FormData();
	        		
	        		form_data.append("filePart",input.files[0]);
	
	        		$('.LyMain').block($.BCS.blockMsgUpload);
	        		$.ajax({
	                    type: 'POST',
	                    url: bcs.bcsContextPath + '/edit/createResource?resourceType=VIDEO',
	                    cache: false,
	                    contentType: false,
	                    processData: false,
	                    data: form_data
	        		}).success(function(response){
	                	console.info(response);
	                	alert("上傳成功!");
	                	appendBody.find('.resourceId').val(response.resourceId);
	                	appendBody.find('img').attr('src',bcs.bcsContextPath + '/getResource/VIDEO/IMAGE/' + response.resourceId);
	        		}).fail(function(response){
	        			console.info(response);
	        			$.FailResponse(response);
	        			$('.LyMain').unblock();
	        		}).done(function(){
	        			$('.LyMain').unblock();
	        		});
        		} else {
        			alert("影片大小不可大於 10MB！");
        		}
            } 
		});
		appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);
		
		// Add Validate TODO
		
		if(settingObj){
			appendBody.find('.resourceId').val(settingObj.referenceId);
        	appendBody.find('img').attr('src',bcs.bcsContextPath + '/getResource/VIDEO/IMAGE/' + settingObj.referenceId);
		}
	
		appendMsg(appendBody);

		if($.BCS.actionTypeParam == "Look"){
			$('.MdBtn03Delete').remove();
			$('.MdBtnUpload').remove();
		}
	}

	// 建立 AUDIO
	var createMsgFrameAUDIO = function(msgType, settingObj){

		var appendBody = MsgFrameTemplate[msgType].clone();

		appendBody.css('display', '');

		appendBody.find('.createResourceAudio').css('cursor', 'pointer');
		appendBody.find('.createResourceAudio').on("change", function(ev){

			var input = ev.currentTarget;
        	if (input.files && input.files[0]) {
        		if(input.files[0].size < 10485760) {
	        		var fileName = input.files[0].name;
	        		console.info("fileName : " + fileName);
	        		var form_data = new FormData();
	        		
	        		form_data.append("filePart",input.files[0]);
	
	        		$('.LyMain').block($.BCS.blockMsgUpload);
	        		$.ajax({
	                    type: 'POST',
	                    url: bcs.bcsContextPath + '/edit/createResource?resourceType=AUDIO',
	                    cache: false,
	                    contentType: false,
	                    processData: false,
	                    data: form_data
	        		}).success(function(response){
	                	console.info(response);
	                	alert("上傳成功!");
	                	appendBody.find('.resourceId').val(response.resourceId);
	                	appendBody.find('.resourceLength').val(response.resourceLength);
	        		}).fail(function(response){
	        			console.info(response);
	        			$.FailResponse(response);
	        			$('.LyMain').unblock();
	        		}).done(function(){
	        			$('.LyMain').unblock();
	        		});
        		} else {
        			alert("音訊大小不可大於 10MB！");
        		}
            } 
		});
		appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);
		
		// Add Validate TODO
		
		if(settingObj){
			appendBody.find('.resourceId').val(settingObj.referenceId);
			appendBody.find('.resourceLength').val("3");
			// TODO appendBody.find('.resourceLength').val(response.resourceLength);
		}
	
		appendMsg(appendBody);

		if($.BCS.actionTypeParam == "Look"){
			$('.MdBtn03Delete').remove();
			$('.MdBtnUpload').remove();
		}
	}

	// 建立 TEXT
	var clickEmoticon = false;
	var createMsgFrameTEXT = function(msgType, settingObj){

		var appendBody = MsgFrameTemplate[msgType].clone();

		appendBody.css('display', '');
		
		if(settingObj){
			appendBody.find('textarea').val(settingObj.text);
		}
		
		appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);

		appendBody.find('.MdBtnEmoticon').click(function(){
			clickEmoticon = this;
			$('#dialogEmoticonSelect').dialog('open');
		});
		
		// Add Validate TODO
	
		appendMsg(appendBody);

		if($.BCS.actionTypeParam == "Look"){
			$('.MdBtn03Delete').remove();
			appendBody.find('textarea').attr('disabled',true);
		}
	}

	// 建立 LINK
	var clickBCSPageLink = false;
	var createMsgFrameLINK = function(msgType, settingObj){

		var appendBody = MsgFrameTemplate[msgType].clone();
		
		appendBody.css('display', '');

		appendBody.find('.createResourceImage').css('cursor', 'pointer');
		appendBody.find('.createResourceImage').on("change", function(ev){

			var input = ev.currentTarget;
        	if (input.files && input.files[0]) {
        		if(input.files[0].size < 1048576) {
	        		var fileName = input.files[0].name;
	        		console.info("fileName : " + fileName);
	        		var form_data = new FormData();
	        		
	        		form_data.append("filePart",input.files[0]);
	
	        		$('.LyMain').block($.BCS.blockMsgUpload);
	        		$.ajax({
	                    type: 'POST',
	                    url: bcs.bcsContextPath + '/edit/createResource?resourceType=IMAGE',
	                    cache: false,
	                    contentType: false,
	                    processData: false,
	                    data: form_data
	        		}).success(function(response){
	                	console.info(response);
	                	alert("上傳成功!");
	                	appendBody.find('.resourceId').val(response.resourceId);
	                	appendBody.find('img').attr('src',bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
	        		}).fail(function(response){
	        			console.info(response);
	        			$.FailResponse(response);
	        			$('.LyMain').unblock();
	        		}).done(function(){
	        			$('.LyMain').unblock();
	        		});
        		} else {
        			alert("圖片大小不可大於 1MB！")
        		}
            } 
		});
		
		// 初始化註記欄位標籤元件
		var linkTagContentFlag = $.BCS.contentFlagComponent(appendBody.find('.linkTag'), 'LINK', {
			placeholder : '請輸入註記'
		});
		appendBody.data('linkTagContentFlag', linkTagContentFlag);
		
		if(settingObj){
			appendBody.find('.textParams').val(settingObj.text);
			
			var content = $.BCS.ResourceMap[settingObj.referenceId];
			if(content){
				appendBody.find('.linkInput').val(content.linkUrl);
				linkTagContentFlag.findContentFlagList(content.linkId);
				
				appendBody.find('.resourceId').val(content.linkPreviewImage);
	        	appendBody.find('img').attr('src',bcs.bcsContextPath + '/getResource/IMAGE/' + content.linkPreviewImage);
			}
		}
		
		appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);
		
		appendBody.find('.showDialogBtn').click(function(){
			clickBCSPageLink = this;
			$('#urlDialog').dialog('open');
		});
		
		// Add Validate TODO
		
		appendMsg(appendBody);

		if($.BCS.actionTypeParam == "Look"){
			$('.MdBtn03Delete').remove();
			appendBody.find('.textParams').attr('disabled',true);
			appendBody.find('.linkInput').attr('disabled',true);			
			linkTagContentFlag.disabled();
			$('.MdBtnUpload').remove();
		}
	}

    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 設定 發送內容 選項 
    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	var setAddMsgContentBtnEvent = function(){
		var MdBtns = $('.MdBtn');

		$.each(MdBtns, function(i, o){
			
			var inputBtn = $(o).find('input');
			var dialogId = $(o).attr('alt-dialog-id');
			
			inputBtn.click(function(){
				var msgType = inputBtn.attr('msgType');
				$.BCS.createMsgFrame(msgType, false, dialogId);
			});
		});
	};
	
	setAddMsgContentBtnEvent();
	
	// 設定 Delete Message Content Event
	var deleteMsgContentEvent = function(){
		$(this).closest('.MsgFrame').remove();
	};

    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 設定 Dialog StickerSelect 
    // ------------------------------------------------------------------------------------------------------------------------------------------------------
    $('#dialogStickerSelect').dialog({
    	autoOpen: false,
		resizable: false, //不可縮放
		modal: true, //畫面遮罩
		draggable: false, //不可拖曳
    	minWidth : 750,
    	position: { my: "top", at: "top", of: window  }
    });
    
	// 設定 stick 選擇
	var stickSelectEventFunc = function(){
		var stkgroupid = $(this).attr('data-stkgroupid');
		var stkid = $(this).attr('data-stkid');
		var stkversion = $(this).attr('data-stkversion');
		var src = bcs.bcsResourcePath + '/images/Stickers/' + stkid + '_key.png';

		var appendBody = MsgFrameTemplate["STICKER"].clone();

		appendBody.css('display', '');
		
		appendBody.find('.STKID').val(stkid);
		appendBody.find('.STKPKGID').val(stkgroupid);
		appendBody.find('.STKVER').val(stkversion);
		appendBody.find('img').attr('src', src);
		
		appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);
	
		appendMsg(appendBody);

		if($.BCS.actionTypeParam == "Look"){
			$('.MdBtn03Delete').remove();
		}
		
		$('#dialogStickerSelect').dialog('close');
	};
	
	// 設定多選項
    $('#stickerSelectTabs').tabs();
    $('.stickerSelectSolid li').click(stickSelectEventFunc);
	$('#stickerSelect').css('display','');

    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 設定 Dialog RichMsg Select 
    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	var richMsgTrTemplate = {};
	
	var initRichMsgTemplate = function(){
		
		richMsgTrTemplate = $('.richMsgTrTemplate').clone(true);
		$('.richMsgTrTemplate').remove();
	};
	
	initRichMsgTemplate();
	
    $('#dialogRichMsgSelect').dialog({
    	autoOpen: false,
		resizable: false, //不可縮放
		modal: true, //畫面遮罩
		draggable: false, //不可拖曳
    	minWidth : 750,
    	position: { my: "top", at: "top", of: window  }
    });

	// 設定 richMsg 選擇
	var richMsgSelectEventFunc = function(){
		var richId = $(this).attr('richId');
		var src = $(this).closest('.richMsgImgTitle').find('img').attr('src');

		settingRichMsgSelectResult(richId, src);
		
		$('#dialogRichMsgSelect').dialog('close');
	};
	
	var settingRichMsgSelectResult = function(richId, src){

		var appendBody = MsgFrameTemplate["RICH_MSG"].clone();

		appendBody.css('display', '');
		
		appendBody.find('.RICH_ID').val(richId);
		appendBody.find('img').attr('src', src);
		
		appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);
	
		appendMsg(appendBody);

		if($.BCS.actionTypeParam == "Look"){
			$('.MdBtn03Delete').remove();
		}
	};

	/**
	 * Get Rich Msg List
	 */
	$.ajax({
		type : "GET",
		url : bcs.bcsContextPath + '/edit/getRichMsgList'
	}).success(function(response){
		$('.richMsgTrTemplate').remove();
		
		for(key in response){
			var richMsgTr = richMsgTrTemplate.clone(true);

			var valueObj = response[key];
//			console.info('valueObj', valueObj);

			richMsgTr.find('.richMsgId').val(key);
			richMsgTr.find('.richMsgTitle').html(valueObj[0]);
			
			richMsgTr.find('.richMsgImgTitle img').attr('richId', key);
			richMsgTr.find('.richMsgImgTitle img').attr('srcUrl', bcs.bcsContextPath + "/getResource/IMAGE/" + valueObj[4]);
			richMsgTr.find('.richMsgImgTitle img').click(richMsgSelectEventFunc); 
			
			richMsgTr.find('.richMsgImgTitle a').attr('richId', key);
			richMsgTr.find('.richMsgImgTitle a').click(richMsgSelectEventFunc); 

			var urls = [""];
			if(valueObj[1]){
				urls = valueObj[1].split(",");
			}
			var titles = [""];
			if(valueObj[5]){
				titles = valueObj[5].split(",");
			}
			var actions = [];
			if(valueObj[7]){
				actions = valueObj[7].split(",");
			}
			var sendMessages = [];
			if(valueObj[8]){
				sendMessages = valueObj[8].split(",");
			}
			var urlHtml = "";
			for (var i=0; i<urls.length; i++) {
				var title = titles[i];
				if(!title){
					title = urls[i];
				}
				var action = actions[i];
				if(!action){
					action = "連結";
					urlHtml += action + "-<a href='" + urls[i] + "' target='_blank'>" + title + "</a><br/>";
				}
				else{
					if("web" == action){
						action = "連結";
						urlHtml += action + "-<a href='" + urls[i] + "' target='_blank'>" + title + "</a><br/>";
					}
					else{
						action = "文字";
						urlHtml += action + "-" + sendMessages[i] + "<br/>";
					}
				}
			}
			richMsgTr.find('.richMsgImgUrl').html(urlHtml);
			
			var time = valueObj[2]; 
			time = time.replace(/\.\d+$/, ''); // 刪去毫秒
			richMsgTr.find('.richMsgCreateTime').html(time);
			richMsgTr.find('.richMsgCreateUser').html(valueObj[3]);

			$('#richMsgListTable').append(richMsgTr);
		}
	}).fail(function(response){
		console.info(response);
		$.FailResponse(response);
	}).done(function(){
	});
	
	// ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 設定 Dialog TemplateMsg Select 
    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	var templateMsgTrTemplate = {};
	
	var initTemplateMsgTemplate = function(){
		
		templateMsgTrTemplate = $('.templateMsgTrTemplate').clone(true);
		$('.templateMsgTrTemplate').remove();
	};
	
	initTemplateMsgTemplate();
	
    $('#dialogTemplateMsgSelect').dialog({
    	autoOpen: false,
		resizable: false, //不可縮放
		modal: true, //畫面遮罩
		draggable: false, //不可拖曳
    	minWidth : 750,
    	position: { my: "top", at: "top", of: window  }
    });
    
 // 設定 templateMsg 選擇
	var templateMsgSelectEventFunc = function(){
		var templateMsgType = $(this).closest('tr').find('.templateMsgType').html();
		console.info("templateMsgType",templateMsgType);
		
		var templateId = $(this).attr('templateId');
		var templateTitle = $(this).closest('.templateMsgImgTitle').find('.templateMsgTitle').html();
		var templateType = templateMsgType;
		
		if(templateMsgType != 'confirm'){
			var src = $(this).closest('.templateMsgImgTitle').find('img').attr('src');
		}else{
			var src = "";
		}
		
		settingTemplateMsgSelectResult(templateType, templateTitle, templateId, src);
		
		$('#dialogTemplateMsgSelect').dialog('close');
	};
	
	var settingTemplateMsgSelectResult = function(templateType, templateTitle, templateId, src){
		var appendBody = MsgFrameTemplate["TEMPLATE"].clone();
		
		appendBody.css('display', '');
		
		if(templateType == 'confirm'){
			templateType = '確認樣板';
		}
		else if(templateType == 'buttons'){
			templateType = '按鈕樣板';
		}
		else if(templateType == 'carousel'){
			templateType = '滑動樣板';
		}
		
		appendBody.find('#templateType').html("類型 : "+templateType);
		appendBody.find('.TEMPLATE_ID').val(templateId);
		appendBody.find('#templateTitle').html(templateTitle);
		appendBody.find('img').attr('src', src);
		
		appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);
		
		appendMsg(appendBody);
		
		if($.BCS.actionTypeParam == "Look"){
			$('.MdBtn03Delete').remove();
		}
	};
	
    /**
	 * Get Template Msg List
	 */
    $.ajax({
		type : "GET",
		url : bcs.bcsContextPath + '/edit/getTemplateMsgList'
	}).success(function(response){
		$('.templateMsgTrTemplate').remove();
		var content;
		
		for(key in response){
			var templateMsgTr = templateMsgTrTemplate.clone(true);
			content = "";
//			console.info('key templateId: ', key);
//			console.info('valueObj : ', valueObj);
			var valueObj = response[key];
			
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
				content += ("訊息內容 : " + valueObj[0]);
			}
			
			templateMsgTr.find('.templateMsgId').val(key);
			templateMsgTr.find('.templateMsgTitle').html(content);
			templateMsgTr.find('.templateMsgImgTitle a').attr('templateId', key);
			templateMsgTr.find('.templateMsgImgTitle a').click(templateMsgSelectEventFunc); 
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

	// ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 設定 Dialog Coupon Select 
	// ------------------------------------------------------------------------------------------------------------------------------------------------------
	var couponTrTemplate = {};

	var initCouponTemplate = function(){
	    
	    couponTrTemplate = $('.couponTrTemplate').clone(true);
	    $('.couponTrTemplate').remove();
	};

	initCouponTemplate();

	$('#dialogCouponSelect').dialog({
	    autoOpen: false,
	    resizable: false, //不可縮放
	    modal: true, //畫面遮罩
	    draggable: false, //不可拖曳
	    minWidth : 750,
	    position: { my: "top", at: "top", of: window  }
	});

	// 設定 coupon 選擇
	var couponSelectEventFunc = function(){
		var selectedCoupon = $(this);
	    var couponId = selectedCoupon.attr('couponId');
	    var tr = selectedCoupon.closest('tr');
	    var src = tr.find('.couponImgTitle').find('img').attr('src');
	    var couponDescription = tr.find('.couponDescription').text();
	    var couponTitle = tr.find('a').text();
	    settingCouponSelectResult(couponId, src, couponDescription, couponTitle);
	    
	    $('#dialogCouponSelect').dialog('close');
	};

	var settingCouponSelectResult = function(couponId, src, couponDescription, couponTitle){
	    var appendBody = MsgFrameTemplate["COUPON"].clone();

	    appendBody.css('display', '');
	    
	    appendBody.find('.COUPON_ID').val(couponId);
	    appendBody.find('img').attr('src', src);
	    appendBody.find('.COUPON_DESCRIPTION').text(couponDescription);
	    appendBody.find('.mdCMN07HeadTtl01').text('優惠劵：' + couponTitle);
	    
	    appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);

	    appendMsg(appendBody);

	    if($.BCS.actionTypeParam == "Look"){
	        $('.MdBtn03Delete').remove();
	    }
	};

	/**
	 * Get Coupon List
	 */
	$.ajax({
	    type : "GET",
	    url : bcs.bcsContextPath + '/edit/getContentCouponList'
	}).success(function(response){
	    $('.couponTrTemplate').remove();
	    var coupons = response;
	    
	    $.each(coupons, function(index, coupon) {
	        var couponTr = couponTrTemplate.clone(true);

	        console.info('coupon', coupon);
	
	        couponTr.find('.couponId').val(coupon.couponId);
	        couponTr.find('.couponTitle').text(coupon.couponTitle);
	        couponTr.find('.couponDescription').text(coupon.couponDescription);
	        
	        couponTr.find('.couponImgTitle img').attr('couponId', coupon.couponId);
	        couponTr.find('.couponImgTitle img').attr('srcUrl', bcs.bcsContextPath + "/getResource/IMAGE/" + coupon.couponListImageId);
	        couponTr.find('.couponImgTitle img').click(couponSelectEventFunc); 
	        
	        couponTr.find('.couponImgTitle a').attr('couponId', coupon.couponId);
	        couponTr.find('.couponImgTitle a').click(couponSelectEventFunc); 
	        	        
	        couponTr.find('.couponCreateTime').text(moment(coupon.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
	        couponTr.find('.couponCreateUser').text(coupon.modifyUser);

	        couponTr.find('.couponRemark').text(coupon.couponRemark?coupon.couponRemark:'無備註');
	        $('#couponListTable').append(couponTr);
	    });
	}).fail(function(response){
	    console.info(response);
	    $.FailResponse(response);
	}).done(function(){
	});
	
	// ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 設定 Dialog RewardCard Select 
	// ------------------------------------------------------------------------------------------------------------------------------------------------------

	var rewardCardTrTemplate = {};

	var initRewardCardTemplate = function(){
	    
		rewardCardTrTemplate = $('.rewardCardTrTemplate').clone(true);
	    $('.rewardCardTrTemplate').remove();
	};
	
	initRewardCardTemplate();
	
	$('#dialogRewardCardSelect').dialog({
	    autoOpen: false,
	    resizable: false, //不可縮放
	    modal: true, //畫面遮罩
	    draggable: false, //不可拖曳
	    minWidth : 750,
	    position: { my: "top", at: "top", of: window  }
	});
	
	// 設定 rewardCard 選擇
	var rewardCardSelectEventFunc = function(){
		var selectedRewardCard = $(this);
	    var rewardCardId = selectedRewardCard.attr('rewardCardId');
	    var tr = selectedRewardCard.closest('tr');
	    var src = tr.find('.rewardCardImgTitle').find('img').attr('src');
	    var rewardCardDescription = tr.find('.rewardCardDescription').text();
	    var rewardCardTitle = tr.find('a').text();
	    settingRewardCardSelectResult(rewardCardId, src, rewardCardDescription, rewardCardTitle);
	    
	    $('#dialogRewardCardSelect').dialog('close');
	};
	
	var settingRewardCardSelectResult = function(rewardCardId, src, rewardCardDescription, rewardCardTitle){
		console.log(MsgFrameTemplate["REWARDCARD"]);
	    var appendBody = MsgFrameTemplate["REWARDCARD"].clone();

	    appendBody.css('display', '');
	    
	    appendBody.find('.REWARDCARD_ID').val(rewardCardId);
	    appendBody.find('img').attr('src', src);
	    appendBody.find('.REWARDCARD_DESCRIPTION').text(rewardCardDescription);
	    appendBody.find('.mdCMN07HeadTtl01').text('集點卡：' + rewardCardTitle);
	    
	    appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);

	    appendMsg(appendBody);

	    if($.BCS.actionTypeParam == "Look"){
	        $('.MdBtn03Delete').remove();
	    }
	};
	
	/**
	 * Get RewardCard List
	 */
	
	$.ajax({
	    type : "GET",
	    url : bcs.bcsContextPath + '/edit/getContentRewardCardList'
	}).success(function(response){
	    $('.rewardCardTrTemplate').remove();
	    var rewardCards = response;
	    
	    $.each(rewardCards, function(index, rewardCard) {
	        var rewardCardTr = rewardCardTrTemplate.clone(true);
	        console.info('rewardCard', rewardCard);
	
	        rewardCardTr.find('.rewardCardId').val(rewardCard.rewardCardId);
	        rewardCardTr.find('.rewardCardTitle').text(rewardCard.rewardCardMainTitle);
	        rewardCardTr.find('.rewardCardDescription').text(rewardCard.rewardCardDescription);
	        
	        rewardCardTr.find('.rewardCardImgTitle img').attr('rewardCardId', rewardCard.rewardCardId);
	        rewardCardTr.find('.rewardCardImgTitle img').attr('srcUrl', bcs.bcsContextPath + "/getResource/IMAGE/" + rewardCard.rewardCardListImageId);
	        rewardCardTr.find('.rewardCardImgTitle img').click(rewardCardSelectEventFunc); 
	        
	        rewardCardTr.find('.rewardCardImgTitle a').attr('rewardCardId', rewardCard.rewardCardId);
	        rewardCardTr.find('.rewardCardImgTitle a').click(rewardCardSelectEventFunc); 
	        	        
	        rewardCardTr.find('.rewardCardCreateTime').text(moment(rewardCard.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
	        rewardCardTr.find('.rewardCardCreateUser').text(rewardCard.modifyUser);
	
	        $('#rewardCardListTable').append(rewardCardTr);
	    });
	}).fail(function(response){
	    console.info(response);
	    $.FailResponse(response);
	}).done(function(){
	});
	
    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 取得 發送內容 從 UI
    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	
	$.BCS.isValidate = true;
	
	$.BCS.getMsgFrameContent = function(id){
		var MsgFrames;
		if (id) {
			MsgFrames = $('#' + id).find('.MsgPlace .MsgFrame');
		} else {
			MsgFrames = $('.MsgPlace .MsgFrame');
		}
		
		var MsgFrameContents = [];
		
		// 取得 發送內容 從 UI
		$.each(MsgFrames, function(i, o){
			var MsgFrame = $(o);
			var msgType = MsgFrame.attr('type');
			
			if(getContentFromFunc[msgType] != null){
				var content = getContentFromFunc[msgType](MsgFrame);
				if(content){
					MsgFrameContents.push(content);
				}
				else{
					$.BCS.isValidate = false;
				}
			}
			else{
				console.error('msgType : ' + msgType + ' Not Implement');
				$.FailAlertStr('錯誤的訊息種類');
				$.BCS.isValidate = false;
			}
		});
		
		return MsgFrameContents;
	};
	
	var getContentFromFunc = {};
	
	// 取得 發送內容 TEXT
	var getContentFromText = function(MsgFrame){
		var content = {};
		content.detailType = "TEXT";
		
		var detailContent = {};
		detailContent.Text = MsgFrame.find('textarea').val();
		console.info('detailContent', detailContent);
		if(!detailContent.Text || detailContent.Text == ""){
			alert('請輸入文字');
			return null;
		}
		
		content.detailContent = JSON.stringify(detailContent);
		
		return content;
	};
	getContentFromFunc.TEXT = getContentFromText;
	
	// 取得 發送內容 STICKER
	var getContentFromSticker = function(MsgFrame){
		var content = {};
		content.detailType = "STICKER";
		
		var detailContent = {};
		detailContent.STKID = MsgFrame.find('.STKID').val();
		detailContent.STKPKGID = MsgFrame.find('.STKPKGID').val();
		detailContent.STKVER = MsgFrame.find('.STKVER').val();
		console.info('detailContent', detailContent);
		
		content.detailContent = JSON.stringify(detailContent);
		
		return content;
	};
	getContentFromFunc.STICKER = getContentFromSticker;

	// 取得 發送內容 IMAGE
	var getContentFromImage = function(MsgFrame){
		var content = {};
		content.detailType = "IMAGE";
		
		var detailContent = {};
		detailContent.resourceType = MsgFrame.find('.resourceType').val();
		detailContent.resourceId = MsgFrame.find('.resourceId').val();
		console.info('detailContent', detailContent);
		if(!detailContent.resourceType || detailContent.resourceType == "" || !detailContent.resourceId || detailContent.resourceId == ""){
			alert('請選擇圖片');
			return null;
		}
		
		content.detailContent = JSON.stringify(detailContent);
		
		return content;
	};
	getContentFromFunc.IMAGE = getContentFromImage;

	// 取得 發送內容 VIDEO
	var getContentFromVideo = function(MsgFrame){
		var content = {};
		content.detailType = "VIDEO";
		
		var detailContent = {};
		detailContent.resourceType = MsgFrame.find('.resourceType').val();
		detailContent.resourceId = MsgFrame.find('.resourceId').val();
		console.info('detailContent', detailContent);
		if(!detailContent.resourceType || detailContent.resourceType == "" || !detailContent.resourceId || detailContent.resourceId == ""){
			alert('請選擇影片');
			return null;
		}
		
		content.detailContent = JSON.stringify(detailContent);
		
		return content;
	};
	getContentFromFunc.VIDEO = getContentFromVideo;

	// 取得 發送內容 AUDIO
	var getContentFromAudio = function(MsgFrame){
		var content = {};
		content.detailType = "AUDIO";
		
		var detailContent = {};
		detailContent.resourceType = MsgFrame.find('.resourceType').val();
		detailContent.resourceId = MsgFrame.find('.resourceId').val();
		detailContent.resourceLength = MsgFrame.find('.resourceLength').val();
		console.info('detailContent', detailContent);
		if(!detailContent.resourceType || detailContent.resourceType == "" || !detailContent.resourceId || detailContent.resourceId == ""){
			alert('請選擇聲音');
			return null;
		}
		
		content.detailContent = JSON.stringify(detailContent);
		
		return content;
	};
	getContentFromFunc.AUDIO = getContentFromAudio;
	
	// 取得 發送內容 BCS_PAGE
	var getContentFromBCS_PAGE = function(MsgFrame){
		var content = {};
		content.detailType = "BCS_PAGE";
		content.detailContent = "{}";
		
		return content;
	};
	getContentFromFunc.BCS_PAGE = getContentFromBCS_PAGE;
	
	// 取得 發送內容 LINK
	var getContentFromLINK = function(MsgFrame){
		var content = {};
		content.detailType = "LINK";
		
		var detailContent = {};
		detailContent.linkPreviewImage = MsgFrame.find('.resourceId').val();
		detailContent.textParams = MsgFrame.find('.textParams').val();
		console.info('textParams', detailContent.textParams);
		if(!detailContent.textParams || detailContent.textParams == ""){
			alert('請輸入連結文字');
			return null;
		}
		
		detailContent.linkUriParams = MsgFrame.find('.linkInput').val();
		console.info('linkUriParams', detailContent.linkUriParams);
		if(!detailContent.linkUriParams || detailContent.linkUriParams == ""){
			alert('請輸入連結');
			return null;
		}
		
		detailContent.linkTagList = MsgFrame.data('linkTagContentFlag').getContentFlagList();

		console.info('detailContent', detailContent);
		
		content.detailContent = JSON.stringify(detailContent);
		
		return content;
	};
	getContentFromFunc.LINK = getContentFromLINK;
	
	// 取得 發送內容 RICH_MSG
	var getContentFromRICH_MSG = function(MsgFrame){
		var content = {};
		content.detailType = "RICH_MSG";
		
		var detailContent = {};
		detailContent.richId = MsgFrame.find('.RICH_ID').val();
		console.info('richId', detailContent.richId);
		
		content.detailContent = JSON.stringify(detailContent);
		
		return content;
	};
	getContentFromFunc.RICH_MSG = getContentFromRICH_MSG;

	// 取得 發送內容 COUPON
	var getContentFromCOUPON = function(MsgFrame){
		var content = {};
		content.detailType = "COUPON";
		
		var detailContent = {};
		detailContent.couponId = MsgFrame.find('.COUPON_ID').val();
		console.info('couponId', detailContent.couponId);
		
		content.detailContent = JSON.stringify(detailContent);
		
		return content;
	};
	getContentFromFunc.COUPON = getContentFromCOUPON;
	
	// 取得 發送內容 REWARDCARD
	var getContentFromREWARDCARD = function(MsgFrame){
		var content = {};
		content.detailType = "REWARDCARD";
		
		var detailContent = {};
		detailContent.rewardCardId = MsgFrame.find('.REWARDCARD_ID').val();
		console.info('rewardCardId', detailContent.rewardCardId);
		
		content.detailContent = JSON.stringify(detailContent);
		
		return content;
	};
	getContentFromFunc.REWARDCARD = getContentFromREWARDCARD;
	
	// 取得 發送內容 TEMPLATE
	var getContentFromTEMPLATE = function(MsgFrame){
		var content = {};
		content.detailType = "TEMPLATE";
		
		var detailContent = {};
		detailContent.templateId = MsgFrame.find('.TEMPLATE_ID').val();
		console.info('templateId', detailContent.templateId);
		
		content.detailContent = JSON.stringify(detailContent);
		
		return content;
	};
	getContentFromFunc.TEMPLATE = getContentFromTEMPLATE;
	
    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 設定 Dialog LinkUrlList Select 
    // ------------------------------------------------------------------------------------------------------------------------------------------------------

	var urlTrTemplate = {};
	
	var initUrlTrTemplate = function(){
		
		urlTrTemplate = $('.urlDialogTr').clone(true);
		$('.urlDialogTr').remove();
	};
	
	initUrlTrTemplate();
	
	$('#urlDialog').dialog({
    	autoOpen: false, //初始化不會是open
    	resizable: false, //不可縮放
    	modal: true, //畫面遮罩
    	draggable: false, //不可拖曳
    	minWidth : 500,
    	position: { my: "top", at: "top", of: window  }
    });

	// 設定 richMsg 選擇
	var linkListSelectEventFunc = function(){
		if(clickBCSPageLink){
			var text = $(this).find('.urls').html();
			$(clickBCSPageLink).closest('.MdFRM04MsgBox').find('.textParams').val(text);
			
			var url = $(this).find('.urls').attr('url');
			$(clickBCSPageLink).closest('.MdBtnEmoticon').find('.linkInput').val(url);
			
		}
		
		$('#urlDialog').dialog('close');
	};
	
	$.ajax({
        type: 'GET',
        url: bcs.bcsContextPath + "/edit/getLinkUrlList",
	}).success(function(response){
		
		for (var i in response) {
			var urlTr = urlTrTemplate.clone(true);
			
			urlTr.find('.urls').html(response[i].linkTitle);
			urlTr.find('.urls').attr('url', response[i].linkUrl);
			
			urlTr.click(linkListSelectEventFunc);
			
			$('#urlDialogTable').append(urlTr);
		}
		
		$('#urlSelection').css('display','');
	}).fail(function(response){
		console.info(response);
		$.FailResponse(response);
	}).done(function(){
	});

    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 設定 Dialog Emoticon Select 
    // ------------------------------------------------------------------------------------------------------------------------------------------------------
    $('#dialogEmoticonSelect').dialog({
    	autoOpen: false,
		resizable: false, //不可縮放
		modal: true, //畫面遮罩
		draggable: false, //不可拖曳
    	minWidth : 750,
    	position: { my: "top", at: "top", of: window  }
    });
    
	// 設定 emoticon 選擇
	var emoticonSelectEventFunc = function(){
		var alt = $(this).find('img').attr('alt');
		
		var textarea = $(clickEmoticon).closest('.MdFRM04MsgBox').find('textarea');
		var text = textarea.val();
		text += "(" + alt + ")";
		textarea.val(text);
		
		$('#dialogEmoticonSelect').dialog('close');
	};
	
	// 設定多選項
    $('#emoticonSelectTabs').tabs();
    $('.emoticonSelectSolid li').click(emoticonSelectEventFunc);
	$('#emoticonSelect').css('display','');

	var appendMsg = function(appendBody) {
		if ($.BCS.currentMsgDialogId) {
			$('#' + $.BCS.currentMsgDialogId).find('.MsgPlace').append(appendBody);
		} else {
			$('.MsgPlace').append(appendBody);
		}
	}
	
});