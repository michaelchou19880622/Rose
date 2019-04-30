$(function(){
	var gameId = "";
	var prizeTrTemplate = {};
	
	var loadDataFunc = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		
		gameId = $.urlParam("gameId");    //從列表頁導過來的參數
		
		$('.LyMain').block($.BCS.blockMsgRead);
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getPrizeList/'+gameId
		}).success(function(response){
			var prizeTr;
			for(var i = 0;i<response.length;i++){				
				prizeTr = prizeTrTemplate.clone(true);
				prizeTr.find('.couponPrizeId').val(response[i].couponId);
				prizeTr.find('.name').html(response[i].couponTitle);
				prizeTr.find('.prizeQuantity').text((response[i].couponGetLimitNumber === null || response[i].couponGetLimitNumber === 0) ? '無限制' : response[i].couponGetLimitNumber);
				prizeTr.find('.acceptedCount').text(response[i].couponGetNumber);
				
				$('#prizeListTable').append(prizeTr);
			}
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};
	
	$('.btn_winner_list').click(function(e) {
		var prizeTr = $(this).closest(".prizeTrTemplate");
		var selectedPrizeId = prizeTr.find('.couponPrizeId').val();
		window.location.replace(bcs.bcsContextPath + '/edit/winnerListPage?gameId=' + gameId + '&couponPrizeId=' + selectedPrizeId);
	});
	
	var initPage = function(){
		prizeTrTemplate = $('.prizeTrTemplate').clone(true);
		$('.prizeTrTemplate').remove();
	}
	
	initPage();
	loadDataFunc();
});