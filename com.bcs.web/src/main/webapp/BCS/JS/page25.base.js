/**
 *
 */
$(function(){
	
	$.BCS.actionTypeParam = $.urlParam("actionType");

	var msgType = "LINK";
	$.BCS.createMsgFrame(msgType);
	$.BCS.createMsgFrame(msgType);
	$.BCS.createMsgFrame(msgType);
	
	$('.MdCMN06BtnList').remove();
	$('.MdFRM03File').remove();
	$('.MdTxtNotice01').remove();
	
	// 產生追蹤連結
	var generateFunc = function(){
		
		$.BCS.isValidate  = true;
		// 發送內容 設定
		var MsgFrameContents = $.BCS.getMsgFrameContent();
		console.info('MsgFrameContents', MsgFrameContents);
		/**
		 * Validate Error
		 */
		if(!$.BCS.isValidate){
			return;
		}

		var tracingId = $.urlParam("tracingId");
		var confirmStr = "請確認是否產生";
		if(tracingId){
			confirmStr = "請確認是否編輯";
		}

		var r = confirm(confirmStr);
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		// 設定傳送資料
		var postData = {};
		postData.linkData = MsgFrameContents;
		postData.tracingId = tracingId;

		// 傳送資料
		$('.LyMain').block($.BCS.blockMsgSave);
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath +'/edit/tracingGenerate',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			alert('產生追蹤連結成功');
			$('.GeneratePlace .linkInput').val(response);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	}

	$('.btn_save').click(generateFunc);
	$('.btn_add').click(generateFunc);
	
	$('.ShowListBtn').click(function(){

		$('#dialogTracingListSelect').dialog('open');
	});

	// 取得設定資料
	var loadDataFunc = function(){

		var tracingId = $.urlParam("tracingId");

		if(tracingId){
			var getDataUrl = bcs.bcsContextPath +'/edit/getTracingLinkData?tracingId=' + tracingId;
	
			$('.LyMain').block($.BCS.blockMsgRead);
			$.ajax({
				type : "GET",
				url : getDataUrl
			}).success(function(response){
				console.info(response);
				
				var ContentLinkTracing = response.ContentLinkTracing;
				var ContentLink = response.ContentLink;
				var ContentLinkBinded = response.ContentLinkBinded;
				var ContentLinkUnMobile = response.ContentLinkUnMobile;
				var TracingUrlPre = response.TracingUrlPre;
				
				var textParams = $('.textParams');
				$.each(textParams, function(i, o){
					var textParam = $(o);
					if(i == 0){
						textParam.val(ContentLink.linkTitle);
					}
					else if(i == 1 && ContentLinkBinded != null){
						textParam.val(ContentLinkBinded.linkTitle);
					}
					else if(i == 2 && ContentLinkUnMobile != null){
						textParam.val(ContentLinkUnMobile.linkTitle);
					}
				});
				
				var linkInputs = $('.linkInput');
				$.each(linkInputs, function(i, o){
					var linkInput = $(o);
					if(i == 0){
						linkInput.val(ContentLink.linkUrl);
					}
					else if(i == 1 && ContentLinkBinded != null){
						linkInput.val(ContentLinkBinded.linkUrl);
					}
					else if(i == 2 && ContentLinkUnMobile != null){
						linkInput.val(ContentLinkUnMobile.linkUrl);
					}
				});

				$('.GeneratePlace .linkInput').val(TracingUrlPre + tracingId);
				$('.GeneratePlace .linkInput').attr('disabled',true);
				
				$('.CHTtl').html('編輯追蹤網址');

				$('.btn_save').val('編輯追蹤連結');
				$('.btn_add').val('編輯追蹤連結');
				
				var MsgFrames = $('.MsgPlace .MsgFrame');
				$.each(MsgFrames, function(i, o){
					var MsgFrame = $(o);
					if(i == 0){
						var linkTagContentFlag = MsgFrame.data('linkTagContentFlag');
						linkTagContentFlag.findContentFlagList(ContentLink.linkId);
					}
					else if(i == 1 && ContentLinkBinded != null){
						var linkTagContentFlag = MsgFrame.data('linkTagContentFlag');
						linkTagContentFlag.findContentFlagList(ContentLinkBinded.linkId);
					}
					else if(i == 2 && ContentLinkUnMobile != null){
						var linkTagContentFlag = MsgFrame.data('linkTagContentFlag');
						linkTagContentFlag.findContentFlagList(ContentLinkUnMobile.linkId);
					}
				});
				
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
    			$('.LyMain').unblock();
			}).done(function(){
    			$('.LyMain').unblock();
			});
		}

		
		var mdCMN07HeadTtl01s = $('.mdCMN07HeadTtl01');
		$.each(mdCMN07HeadTtl01s, function(i, o){
			var mdCMN07HeadTtl01 = $(o);
			if(i == 0){
				mdCMN07HeadTtl01.html("連結:未綁定");
			}
			else if(i == 1){
				mdCMN07HeadTtl01.html("連結:已綁定");
			}
			else if(i == 2){
				mdCMN07HeadTtl01.html("連結:非手機");
			}
			else if(i == 3){
				mdCMN07HeadTtl01.html("追蹤網址");
			}
		});
	};
	
	loadDataFunc(); 
});
