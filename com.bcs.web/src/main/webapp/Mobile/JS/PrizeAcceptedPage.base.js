/**
 * 
 */

$(function(){
	var gameId = "";			//gameId
	
	var initPage = function(){
		gameId = $.urlParam("gameId");
		
		$.ajax({
            type: 'GET',
            url: '../../m/Game/getPrizeDetail/'+gameId,
		}).success(function(response){
			var gameData = response[gameId];
			console.info("response :  ",response[gameId]);
			
			$('.headerImage').find('img').attr('src', '../bcs/getCdnResource/IMAGE/' + gameData[2]);
			$('.footerImage').find('img').attr('src', '../bcs/getCdnResource/IMAGE/' + gameData[3]);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	}

	initPage();
});