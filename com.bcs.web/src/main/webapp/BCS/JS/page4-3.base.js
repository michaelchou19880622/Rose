/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/msgCreatePage?from=msgListSendedPage');
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
		var msgSendId = $(this).attr('msgSendId');
		console.info('btn_copyFunc msgSendId:' + msgSendId);
 		window.location.replace(bcs.bcsContextPath + '/edit/msgCreatePage?msgSendId=' + msgSendId + '&actionType=Copy&from=msgListSendedPage');
	};

	// 取得資料列表
	var loadDataFunc = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath +'/edit/getSendedMsgList'
		}).success(function(response){
			$('.dataTemplate').remove();
			$.BCS.ResourceMap = response.ResourceMap;
			
			for(key in response.MsgMain){
				var msgData = templateBody.clone(true);

				var keyObj = JSON.parse(key);
				var valueObj = response.MsgMain[key];
				console.info('keyObj', keyObj);
				console.info('valueObj', valueObj);

				var msgSendId = keyObj.msgSendId;

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
				
				// Status Fail Show
				var failMsg = "";
				var sendCount = keyObj.sendCount;
				if(keyObj.status == "FAIL"){
					msgData.css('color', 'red');
					failMsg = '<br><div>錯誤訊息:<br>' + keyObj.statusNotice + '</div>';
					sendCount = -1;
				}

				msgData.find('.msgContent a').attr('href', bcs.bcsContextPath +'/edit/msgCreatePage?msgSendId=' + msgSendId + '&actionType=Look&from=msgListSendedPage');
				msgData.find('.msgContent a').html(msgContent);

				msgData.find('.msgType').html(msgTypeStr);
				msgData.find('.sendTime').html($.formatTime(new Date(keyObj.sendTime)));

				msgData.find('.sendGroup').html(keyObj.groupTitle + failMsg);
				
				msgData.find('.sendCount a').attr('href', bcs.bcsContextPath +'/edit/exportToExcelForSendedMsg?msgSendId=' + msgSendId);
				msgData.find('.sendCount a').html($.BCS.formatNumber(sendCount,0));
				
				msgData.find('.modifyUser').html(response.AdminUser[keyObj.modifyUser]);

				msgData.find('.btn_copy').attr('msgSendId', msgSendId);
				msgData.find('.btn_copy').click(btn_copyFunc);
				
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