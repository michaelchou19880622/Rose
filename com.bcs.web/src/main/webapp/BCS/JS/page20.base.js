/**
 * 
 */
$(function(){
	
	var page = 0;
	var paramPage = $.urlParam("page");
	if(paramPage){
		page = paramPage;
		page--;
	}
	
	$('.LeftBtn').click(function(){
		if(page > 0){
			page--;
			loadDataFunc("");
		}
	});
	
	$('.RightBtn').click(function(){
		page++;
		loadDataFunc("");
	});

	$('.query').click(function(){
		
		var queryFlag = $("#queryByFlag").val();
		
		loadDataFunc(queryFlag);
	});
	
	var loadDataFunc = function(queryFlag){
		$('.LyMain').block($.BCS.blockMsgRead);
		
		console.info("queryFlag", queryFlag);
		console.info("page", page);

		$('#pageText').html(page+1);
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getLinkUrlReportList?queryFlag=' + queryFlag + '&page=' + page
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
	
			$.each(response, function(i, o){
				var groupData = templateBody.clone(true);
				
				groupData.find('.linkTitle').html(o.linkTitle);
				groupData.find('.linkUrl').html(o.linkUrl);
				
				var linkFlag = moment(o.linkTime).format("YYYY/MM/DD") + "<br/><br/>";
				$.each(o.flags, function(i, o){
					linkFlag += o + "/";
				});
				groupData.find('.linkFlag').html(linkFlag);
				
				var linkUrl = encodeURIComponent(o.linkUrl);
				console.info(linkUrl);
				
				groupData.find('.totalCount a').attr('href', bcs.bcsContextPath +'/admin/reportLinkClickDetailPage?linkUrl=' + linkUrl)
				groupData.find('.totalCount a').html($.BCS.formatNumber(o.totalCount,0));
				
				groupData.find('.userCount a').attr('href', bcs.bcsContextPath +'/admin/reportLinkClickDetailPage?linkUrl=' + linkUrl)
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
	
	loadDataFunc("");
});