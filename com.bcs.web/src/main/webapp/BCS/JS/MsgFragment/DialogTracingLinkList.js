/**
 * 
 */
$(function(){
    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 設定 Dialog Tracing Link List
    // ------------------------------------------------------------------------------------------------------------------------------------------------------

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
	
	$.BCS.getTracingLinkList = function(){
		$.ajax({
	        type: 'GET',
	        url: bcs.bcsContextPath + "/edit/getTracingLinkList",
		}).success(function(response){
			$('.tracingTrTemplate').remove();
			
			var ContentLinkTracingList = response.ContentLinkTracingList;
			var ContentLinkList = response.ContentLinkList;
			var FlagList = response.FlagList;
			var TracingUrlPre = response.TracingUrlPre;
			var AdminUser = response.AdminUser;
	
			$.each(ContentLinkTracingList, function(i, o){
				var tracingTr = tracingTrTemplate.clone(true);
				
				try{
					tracingTr.find('.tracingLink a').attr('href', bcs.bcsContextPath +'/edit/tracingGeneratePage?tracingId=' + o.tracingId + '&actionType=Edit');
					tracingTr.find('.tracingLink a').html(TracingUrlPre + o.tracingId);

					var linkFlag = "";
					if(FlagList[o.linkId]){

						$.each(FlagList[o.linkId], function(i, o){
							linkFlag += o + "/";
						});
					}
					
					tracingTr.find('.tracingTargetTitle').html(ContentLinkList[o.linkId].linkTitle + "<br/><br/>" + linkFlag);
					
					tracingTr.find('.tracingTarget a').attr('href', ContentLinkList[o.linkId].linkUrl);
					tracingTr.find('.tracingTarget a').html(ContentLinkList[o.linkId].linkUrl);

					tracingTr.find('.modifyTime').html(o.modifyTime);
					tracingTr.find('.modifyUser').html(AdminUser[o.modifyUser]);
					
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