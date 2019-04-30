/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/msgCreatePage?from=msgListDraftPage');
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
	
	// 刪除按鈕
	var btn_deteleFunc = function(){
		var msgId = $(this).attr('msgId');
		console.info('btn_deteleFunc msgId:' + msgId);

		var r = confirm("請確認是否刪除");
		if (r) {
			
		} else {
		    return;
		}
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/admin/deleteSendMsg?msgId=' + msgId
		}).success(function(response){
			console.info(response);
			alert("刪除成功");
			loadDataFunc();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};
	
	// 複製按鈕
	var btn_copyFunc = function(){
		var msgId = $(this).attr('msgId');
		console.info('btn_copyFunc msgId:' + msgId);
 		window.location.replace(bcs.bcsContextPath + '/edit/msgCreatePage?msgId=' + msgId + '&actionType=Copy&from=msgListDraftPage');
	};

	// 取得資料列表
	var loadDataFunc = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath +'/edit/getSendMsgList?status=DRAFT'
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

				msgData.find('.msgContent a').attr('href', bcs.bcsContextPath +'/edit/msgCreatePage?msgId=' + msgId + '&actionType=Edit&from=msgListDraftPage');
				msgData.find('.msgContent a').html(msgContent);

				msgData.find('.msgType').html(msgTypeStr);
				msgData.find('.modifyTime').html($.formatTime(new Date(keyObj.modifyTime)));
				
				var groupTitle = response.SendGroup[ keyObj.groupId];
				if(groupTitle){
					msgData.find('.sendGroup').html(groupTitle);
				}
				else{
					msgData.find('.sendGroup').html('-');
				}
				msgData.find('.modifyUser').html(response.AdminUser[keyObj.modifyUser]);

				msgData.find('.btn_copy').attr('msgId', msgId);
				msgData.find('.btn_copy').click(btn_copyFunc);

				if (bcs.user.admin) {
					msgData.find('.btn_detele').attr('msgId', msgId);
					msgData.find('.btn_detele').click(btn_deteleFunc);
				} else {
					msgData.find('.btn_detele').remove();
				}

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