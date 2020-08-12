$(function(){
	var page = 0;
	var paramPage = $.urlParam("page");
    var tracingTrTemplate = {};
	var initTracingTrTemplate = function(){
		tracingTrTemplate = $('.tracingTrTemplate').clone(true);
		$('.tracingTrTemplate').remove();
	};
	initTracingTrTemplate();
	
	$('#dialogTracingListSelect').dialog({
    	autoOpen: false, //初始化不會是open
    	resizable: false, //不可縮放
    	modal: true, //畫面遮罩
    	draggable: false, //不可拖曳
    	minWidth : 1000,
    	position: { my: "top", at: "top", of: window  }
    });
	if(paramPage){
		page = paramPage;
		page--;
	}
	$('.LeftBtn').click(function(){
		if(page > 0){
			page--;
			$.BCS.getTracingLinkList();
		}
	});
	$('.RightBtn').click(function(){
		page++;
		$.BCS.getTracingLinkList();
	});
	$('.query').click(function(){
		page = 0;
		$.BCS.getTracingLinkList();
	});	
	$.BCS.getTracingLinkList = function(){
		var queryFlag = $("#queryFlag").val();
		console.info("queryFlag", queryFlag);
		console.info("page", page);
		var postData = {};
		postData.queryFlag = queryFlag;
		postData.page = page;
		postData.pageSize = 20;
		$('#pageText').html(page+1);
		$.ajax({
	        type: 'POST',
	        url: bcs.bcsContextPath + "/edit/getTracingLinkList",
	        cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			$('.tracingTrTemplate').remove();
			var tracingUrlPre = response.TracingUrlPre;
			var contentLinkTracingList = response.ContentLinkTracingList;
			console.info("contentLinkTracingList", contentLinkTracingList);
			$('#recordNumberText').html(contentLinkTracingList.length);
			$.each(contentLinkTracingList, function(i, o){
				var tracingTr = tracingTrTemplate.clone(true);
				try{
					var contentLinkTracing = o.contentLinkTracing;
					tracingTr.find('.tracingLink a').attr('href', bcs.bcsContextPath +'/edit/tracingGeneratePage?tracingId=' + o.contentLinkTracing.tracingId + '&actionType=Edit');
					tracingTr.find('.tracingLink a').html(tracingUrlPre + o.contentLinkTracing.tracingId);
					tracingTr.find('.tracingTargetTitleForUnbind').html(o.contentLinkUnbind.linkTitle + "<br>" + o.contentLinkUnbind.linkTag);
					tracingTr.find('.tracingTargetTitleForBind').html(o.contentLinkBind.linkTitle + "<br>" + o.contentLinkBind.linkTag);
					tracingTr.find('.tracingTargetTitleForUnmobile').html(o.contentLinkUnmobile.linkTitle + "<br>" + o.contentLinkUnmobile.linkTag);
					tracingTr.find('.tracingTargetForUnbind a').attr('href', o.contentLinkUnbind.linkUrl);
					tracingTr.find('.tracingTargetForUnbind a').html(o.contentLinkUnbind.linkUrl);
					tracingTr.find('.tracingTargetForBind a').attr('href', o.contentLinkBind.linkUrl);
					tracingTr.find('.tracingTargetForBind a').html(o.contentLinkBind.linkUrl);
					tracingTr.find('.tracingTargetForUnmobile a').attr('href', o.contentLinkUnmobile.linkUrl);
					tracingTr.find('.tracingTargetForUnmobile a').html(o.contentLinkUnmobile.linkUrl);
					tracingTr.find('.modifyTime').html(o.contentLinkTracing.modifyTime);
					tracingTr.find('.modifyUser').html(o.contentLinkTracing.modifyUser);
					$('#tracingListTable').append(tracingTr);
				}
				catch(err) {
					console.error(err);
				}
			});
	
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	}
	$.BCS.getTracingLinkList();
});