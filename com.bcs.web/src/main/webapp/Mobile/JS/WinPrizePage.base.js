/**
 * 
 */

$(function(){
	var gameId = "";			//gameId

	var initPage = function(){
		gameId = $.urlParam("gameId");
		if(!gameId){
			gameId = $('#gameId').val();
		}
		
		prizeTrTemplate = $('.prizeTrTemplate').clone(true);
		$('.prizeTrTemplate').remove();
		
		$.ajax({
            type: 'GET',
            url: '../m/Game/getPrizeDetail/'+gameId,
		}).success(function(response){
//			var prizeTr = prizeTrTemplate.clone(true);
			
//			prizeTr.find('.prizeImage img').attr('src', "../bcs/getCdnResource/IMAGE/" + response['prizeImageId']);
//			prizeTr.find('.prizeName').html(response['prizeName']);
//			prizeTr.find('.prizeContent').html(response['prizeContent']);
			$('.prizeImageShow').find('img').attr('src', "../bcs/getCdnResource/IMAGE/" + response['prizeImageId']);
			
//			$('#prizeListTable').append(prizeTr);
		}).fail(function(response){
			console.info(response);
		}).done(function(){
		});
		
		$.ajax({
            type: 'GET',
            url: '../m/Game/turntable/'+gameId,
		}).success(function(response){
			var gameData = response[gameId];
			$('.headerImage').find('img').attr('src', '../bcs/getCdnResource/IMAGE/' + gameData[2]);
			$('.footerImage').find('img').attr('src', '../bcs/getCdnResource/IMAGE/' + gameData[3]);
		}).fail(function(response){
			console.info(response);
		}).done(function(){
		});
	}
	
	initPage();
});