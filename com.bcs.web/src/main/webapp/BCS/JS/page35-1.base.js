/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/campaignResponseCreatePage?from=campaignResponseDisablePage');
	});
	$('.ActiveBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/campaignResponsePage');
	});
	$('.DisableBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/campaignResponseDisablePage');
	});
	
	// 刪除按鈕
	var btn_deteleFunc = function(){
		var iMsgId = $(this).attr('iMsgId');
		console.info('btn_deteleFunc iMsgId:' + iMsgId);

		var r = confirm("請確認是否刪除");
		if (r) {
			
		} else {
		    return;
		}
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/admin/deleteInteractiveMsg?iMsgId=' + iMsgId
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
		var iMsgId = $(this).attr('iMsgId');
		console.info('btn_copyFunc iMsgId:' + iMsgId);
 		window.location.replace(bcs.bcsContextPath + '/edit/campaignResponseCreatePage?iMsgId=' + iMsgId + '&actionType=Copy&from=campaignResponseDisablePage');
	};
	
	// 改變狀態按鈕
	var redesignFunc = function(){
		var iMsgId = $(this).attr('iMsgId');
		console.info('redesignFunc iMsgId:' + iMsgId);
		
		$.ajax({
			type : "DELETE",
			url : bcs.bcsContextPath + '/edit/redesignInteractiveMsg?iMsgId=' + iMsgId
		}).success(function(response){
			console.info(response);
			alert("改變成功");
			loadDataFunc();
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
			url : bcs.bcsContextPath +'/edit/getCampaignInteractiveMsgList?type=CAMPAIGN&status=DISABLE'
		}).success(function(response){
			$('.dataTemplate').remove();
			$.BCS.ResourceMap = response.ResourceMap;
			
			for(key in response.MsgMain){
				var msgData = templateBody.clone(true);

				var keyObj = JSON.parse(key);
				var valueObj = response.MsgMain[key];
				console.info('keyObj', keyObj);
				console.info('valueObj', valueObj);
				
				var iMsgId = keyObj.iMsgId;
								
				var msgContent = "";
				var msgTypeStr = "-";
				$.each(valueObj, function(i, o){
					msgTypeStr += $.BCS.parseMsgType(o.msgType) + "-";
					if(o.text){
						msgContent += o.text + "-<br/>";
					}
					else if('STICKER' == o.msgType){
						msgContent += '<img src="' + bcs.bcsResourcePath + '/images/Stickers/' + o.referenceId + '_key.png" alt="Type2" ><br/>';
					}
					else if('IMAGE' == o.msgType){
						msgContent += '<img src="' + bcs.bcsContextPath + '/getResource/IMAGE/' + o.referenceId + '" alt="Type2" style="width:100px"><br/>';
					}
					else if('RICH_MSG' == o.msgType){
						var richMsg = $.BCS.ResourceMap[o.referenceId];
						if(richMsg){
							var imageId = richMsg.richImageId;
							msgContent += '<img src="' + bcs.bcsContextPath + '/getResource/IMAGE/' + imageId + '" alt="Type2" style="width:100px"><br/>';
						}
					}
				});
				
				if(keyObj.msgTag){
					msgTypeStr += "<br/>類別:" + keyObj.msgTag;
				}
				
				var keywordShow = keyObj.mainKeyword;

				// 重新產生 追加關鍵字
				var otherKeywords = $.BCS.ResourceMap['iMsgId-' + iMsgId];
				if(otherKeywords){
					if(otherKeywords.length > 0){
						keywordShow += "<br/><br/>追加 : ";
					}
					
					$.each(otherKeywords, function(i, o){
						var otherKeyword = o.otherKeyword;
						keywordShow += otherKeyword + ", ";
					});
				}

                var campaign = response.CampaignMap['iMsgId-' + iMsgId];
                if(campaign){
                    msgData.find('.campaign').html(campaign.campaignName);
                }


				msgData.find('.mainKeyword a').attr('href', bcs.bcsContextPath +'/edit/campaignResponseCreatePage?iMsgId=' + iMsgId + '&actionType=Edit&from=campaignResponseDisablePage');
				msgData.find('.mainKeyword a').html(keywordShow);
				msgData.find('.userStatus').html($.BCS.parseUserStatus(keyObj.userStatus));
				
				msgData.find('.msgContent').html(msgContent);
				
				msgData.find('.msgType').html(msgTypeStr);
				
				msgData.find('.sendCount a').attr('href', bcs.bcsContextPath +'/edit/keywordAndInteractiveReportPage?iMsgId=' + iMsgId + '&userStatus=' + keyObj.userStatus + '&reportType=CAMPAIGN');
				msgData.find('.sendCount a').html($.BCS.formatNumber(keyObj.sendCount,0));

				msgData.find('.interactiveStatus span').html($.BCS.parseInteractiveStatus(keyObj.interactiveStatus));
				
				msgData.find('.modifyTime').html($.formatTime(new Date(keyObj.modifyTime)));

				msgData.find('.modifyUser').html(response.AdminUser[keyObj.modifyUser]);

				msgData.find('.btn_redeisgn').attr('iMsgId', iMsgId);
				msgData.find('.btn_redeisgn').click(redesignFunc);
				
				msgData.find('.btn_copy').attr('iMsgId', iMsgId);
				msgData.find('.btn_copy').click(btn_copyFunc);

				if (bcs.user.admin) {
					msgData.find('.btn_detele').attr('iMsgId', iMsgId);
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