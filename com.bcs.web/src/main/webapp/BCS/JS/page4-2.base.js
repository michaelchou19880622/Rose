/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/msgCreatePage?from=msgListDelayPage');
	});
	$('.DraftBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/msgListDraftPage');
	});
	$('.DelayBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/msgListDelayPage');
	});
	$('.SendedBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/msgListSendedPage');
	});
	$('.ScheduleBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/msgListSchedulePage');
	});

	// 複製按鈕
	var btn_copyFunc = function(){
		var msgId = $(this).attr('msgId');
		console.info('btn_copyFunc msgId:' + msgId);
 		window.location.replace(bcs.bcsContextPath + '/edit/msgCreatePage?msgId=' + msgId + '&actionType=Copy&from=msgListDelayPage');
	};

	// 重設按鈕
	var btn_redeisgnFunc = function(){
		var msgId = $(this).attr('msgId');
		console.info('btn_redeisgnFunc msgId:' + msgId);

		var r = confirm("請確認是否重設, 重設之後會保留在草稿中");
		if (r) {
			
		} else {
		    return;
		}
		
		var postData = {};
		postData.msgId = msgId;
		postData.actionType = 'RedesignMsg';
		
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/edit/redeisgnSendMsg',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			alert("重設成功");
	 		window.location.replace(bcs.bcsContextPath + '/edit/msgCreatePage?msgId=' + msgId + '&actionType=Edit');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};

	// 取得資料列表
	var loadDataFunc = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath +'/edit/getSendMsgList?status=SCHEDULED&sendType=DELAY'
		}).success(function(response){
			$('.dataTemplate').remove();
			$.BCS.ResourceMap = response.ResourceMap;
			
			for(key in response.MsgMain){
				var msgData = templateBody.clone(true);

				var keyObj = JSON.parse(key);
				var valueObj = response.MsgMain[key];
				console.info('keyObj', keyObj);
				console.info('valueObj', valueObj);

				var msgId = keyObj.msgId;

				var msgContent = "-";
				var msgTypeStr = $.BCS.parseSendType(keyObj.sendType) + "<br/>-";
				$.each(valueObj, function(i, o){
					msgTypeStr += $.BCS.parseMsgType(o.msgType) + "-";
					if(o.text){
						msgContent += o.text + "-<br/>";
					}
					else if('STICKER' == o.msgType){
						msgContent += '<img src="' + bcs.bcsResourcePath + '/images/Stickers/' + o.referenceId + '_key.png" alt="Type2" style="cursor: pointer;"><br/>';
					}
					else if('IMAGE' == o.msgType){
						msgContent += '<img src="' + bcs.bcsContextPath + '/getResource/IMAGE/' + o.referenceId + '" alt="Type2" style="cursor: pointer; width:100px"><br/>';
					}
					else if('RICH_MSG' == o.msgType){
						var richMsg = $.BCS.ResourceMap[o.referenceId];
						if(richMsg){
							var imageId = richMsg.richImageId;
							msgContent += '<img src="' + bcs.bcsContextPath + '/getResource/IMAGE/' + imageId + '" alt="Type2" style="cursor: pointer; width:100px"><br/>';
						}
					}
					else if('COUPON' == o.msgType){
						var coupon = $.BCS.ResourceMap[o.referenceId];
						if(coupon){
							var imageId = coupon.couponListImageId;
							msgContent += '<img src="' + bcs.bcsContextPath + '/getResource/IMAGE/' + imageId + '" alt="Type2" style="cursor: pointer; width:100px"><br/>';
						}
					}
					else if('TEMPLATE' == o.msgType){
						var templateMsg = $.BCS.ResourceMap[o.referenceId];
						if(templateMsg){
							var imageId = templateMsg.templateImageId;

							var templateType = templateMsg.templateType;
							if(templateType == 'confirm'){
								templateType = '確認樣板';
							}
							else if(templateType == 'buttons'){
								templateType = '按鈕樣板';
							}
							else if(templateType == 'carousel'){
								templateType = '滑動樣板';
							}
							
							msgContent += '<p>樣板類型 : '+ templateType + '</p>';
							if(templateMsg.templateType != 'confirm'){
								msgContent += '<img src="' + bcs.bcsContextPath + '/getResource/IMAGE/' + imageId + '" alt="Type2" style="width:100px"><br/>';
								msgContent += '<p>標題 : '+ templateMsg.templateTitle + '</p>';
							}else{
								msgContent += '<p>內容  : '+ templateMsg.templateText + '</p>';
							}
						}
					}
				});
				
				if(keyObj.msgTag){
					msgTypeStr += "<br/>類別:" + keyObj.msgTag;
				}

				msgData.find('.msgContent a').attr('href', bcs.bcsContextPath +'/edit/msgCreatePage?msgId=' + msgId + '&actionType=Look&from=msgListDelayPage');
				msgData.find('.msgContent a').html(msgContent);

				msgData.find('.msgType').html(msgTypeStr);
				msgData.find('.scheduleTime').html(keyObj.scheduleTime);

				var groupTitle = response.SendGroup[ keyObj.groupId];
				if(groupTitle){
					msgData.find('.sendGroup').html(groupTitle);
				}
				else{
					msgData.find('.sendGroup').html('-');
				}
				msgData.find('.modifyUser').html(response.AdminUser[keyObj.modifyUser]);

				// 設定複製按鈕事件
				msgData.find('.btn_copy').attr('msgId', msgId);
				msgData.find('.btn_copy').click(btn_copyFunc);

				// 設定重設按鈕事件
				msgData.find('.btn_redeisgn').attr('msgId', msgId);
				msgData.find('.btn_redeisgn').click(btn_redeisgnFunc);
				
				$('#tableBody').append(msgData);
			}
			
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};

	var templateBody = {};
	
	var initTemplate = function(){

		templateBody = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
	}

	initTemplate();
	loadDataFunc();
});