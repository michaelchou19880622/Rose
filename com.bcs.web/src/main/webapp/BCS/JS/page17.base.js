/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/blackKeywordResponseCreatePage?from=blackKeywordResponsePage');
	});
	$('.ActiveBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/blackKeywordResponsePage');
	});
	$('.DisableBtn').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/blackKeywordResponseDisablePage');
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
 		window.location.replace(bcs.bcsContextPath + '/edit/blackKeywordResponseCreatePage?iMsgId=' + iMsgId + '&actionType=Copy&from=blackKeywordResponsePage');
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
			url : bcs.bcsContextPath +'/edit/getBlackInteractiveMsgList?type=BLACK_KEYWORD&status=ACTIVE'
		}).success(function(response){
			$('.dataTemplate').remove();
			$.BCS.ResourceMap = response.ResourceMap;
			
			for(key in response.MsgMain){
				var msgData = templateBody.clone(true);

				var keyObj = response.MsgMain[key];
				console.info('keyObj', keyObj);
				
				var iMsgId = keyObj.iMsgId;
				
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

				msgData.find('.mainKeyword a').attr('href', bcs.bcsContextPath +'/edit/blackKeywordResponseCreatePage?iMsgId=' + iMsgId + '&actionType=Edit&from=blackKeywordResponsePage');
				msgData.find('.mainKeyword a').html(keywordShow);
				msgData.find('.userStatus').html($.BCS.parseUserStatus(keyObj.userStatus));

				msgData.find('.interactiveStatus span').html($.BCS.parseInteractiveStatus(keyObj.interactiveStatus));
				
				msgData.find('.sendCount a').attr('href', bcs.bcsContextPath +'/edit/keywordAndInteractiveReportPage?iMsgId=' + iMsgId + '&userStatus=' + keyObj.userStatus + '&reportType=BlackKeyword');
				msgData.find('.sendCount a').html($.BCS.formatNumber(keyObj.sendCount,0));
				
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