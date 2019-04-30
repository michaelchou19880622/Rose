/**
 * 
 */
$(function(){

	var loadDataFunc = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getPageVisitReportList'
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
	
			$.each(response, function(i, o){
				var groupData = templateBody.clone(true);
				
				groupData.find('.pageTitle').html(o.pageTitle);
				groupData.find('.pageUrl').html(o.pageUrl);
				
				var pageUrl = encodeURIComponent(o.pageUrl);
				console.info(pageUrl);
				
				groupData.find('.totalCount a').attr('href', bcs.bcsContextPath +'/admin/reportPageVisitDetailPage?pageUrl=' + pageUrl)
				groupData.find('.totalCount a').html($.BCS.formatNumber(o.totalCount,0));
				
				groupData.find('.userCount a').attr('href', bcs.bcsContextPath +'/admin/reportPageVisitDetailPage?pageUrl=' + pageUrl)
				groupData.find('.userCount a').html($.BCS.formatNumber(o.userCount,0));
	
				$('#tableBody').append(groupData);
			});
			
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};

	var templateBody = {};
	templateBody = $('.dataTemplate').clone(true);
	$('.dataTemplate').remove();
	
	loadDataFunc();
});