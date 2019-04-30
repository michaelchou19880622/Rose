/**
 * 
 */
$(function(){
	history.pushState(null, "", location.href.split("?")[0]);    // 將網址後面的 MID 資訊拿掉

	$('.cancel').click(function(){
		$('.popup_wrapper').css('display', 'none');
	});
	
	$('.check').click(function(){
		window.location.reload();
	});

	$('.confirm').click(function(){
		var idnum = $(this).attr("idnum");
		console.info("idnum", idnum);
		
		var rewardCardId = $(this).attr("rewardCardId");
		console.info('rewardCardId:',rewardCardId);
		
		var postData = {
				idnum : idnum,
				rewardCardId : rewardCardId
		};

		$('.wrapper').block({ "message" : "處理中...."});
		
		$.ajax({
			type : "POST",
			url : 'createActionUserRewardCardForGetApi',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info("集點卡領取成功");
			/*
			20190121 功能未開放故comment out
			*/
//	 		window.location.assign('userRewardCardContentPage?referenceId=' + response);
		}).fail(function(response){
			console.info(response);
			var str = "";
			if(response && response.status == 501){
				str = "<br>[" + response.responseText + "]";
			}
			$('.wrapper').unblock();
			$('.popup_text').html('集點卡領取失敗' + str);
			$('.cancel').css('display', 'none');
			$('.confirm').css('display', 'none');
			$('.check').css('display', '');
			$('.popup_wrapper').css('display', '');
		}).done(function(){
		});
	});
	
	var rewardCardTitleFunc = function(event){		
		var show = $(this).attr("show");
		console.info("show", show);
		
		var rewardCardId = $(this).attr("rewardCardId");
		console.info("rewardCardId", rewardCardId);
		
		var str = '是否領取此數位集點卡';
		
		if(show == ""){
			$('.popup_text').html(str);
			$('.confirm').attr('rewardCardId', rewardCardId);
			$('.popup_wrapper').css('display', '');
		}
		else{

			var postData = {
					rewardCardId : rewardCardId
			};

			$('.wrapper').block({ "message" : "處理中...."});
			
			$.ajax({
				type : "POST",
				url : 'createActionUserRewardCardForGetApi',
	            cache: false,
	            contentType: 'application/json',
	            processData: false,
				data : JSON.stringify(postData)
			}).success(function(response){
				console.info("集點卡領取成功");
				/*
				20190121 功能未開放故comment out
				*/
//		 		window.location.assign('userRewardCardContentPage?referenceId=' + response);
			}).fail(function(response){
				console.info(response);
				var str = "";
				if(response && response.status == 501){
					str = "<br>[" + response.responseText + "]";
				}
				$('.wrapper').unblock();
				$('.popup_text').html('集點卡領取失敗' + str);
				$('.cancel').css('display', 'none');
				$('.confirm').css('display', 'none');
				$('.check').css('display', '');
				$('.popup_wrapper').css('display', '');
			}).done(function(){
			});
		}
	};
	
	var loadDataFunc = function(){

		$('.wrapper').block({ "message" : "處理中...."});
		$.ajax({
			type : "GET",
			url : 'getMyRewardCardList'
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
	
			$.each(response, function(i, rewardCardData){
				
				var rewardCardBody = templateBody.clone(true);
				console.info(rewardCardData);
				var rewardCardMainTitle = rewardCardData.rewardCardMainTitle;
				
				rewardCardMainTitle += "<br>" + rewardCardData.rewardCardSubTitle;
				rewardCardMainTitle += "<br>" + rewardCardData.status;
				
				rewardCardBody.find('.rewardCard_title').html(rewardCardMainTitle);

				rewardCardBody.find('.linkBody').attr("show", rewardCardData.status);
				rewardCardBody.find('.linkBody').attr("idnum", rewardCardData.idnum);
				rewardCardBody.find('.linkBody').attr("rewardCardId", rewardCardData.rewardCardId);

				rewardCardBody.find('.linkBody').click(rewardCardTitleFunc);
				
				var img = rewardCardData.rewardCardListImageId;
				if(img != null && img!= ""){
					rewardCardBody.find('.rewardCard_thumb').attr('src', img);
				}

				$('.rewardCard_list').append(rewardCardBody);
			});
			
		}).fail(function(response){
			console.info(response);
			var str = "";
			if(response && response.status == 501){
				str = "<br>[" + response.responseText + "]";
			}
			$('.wrapper').unblock();
			$('.popup_text').html('集點卡錯誤失敗' + str);
			$('.cancel').css('display', 'none');
			$('.confirm').css('display', 'none');
			$('.check').css('display', '');
			$('.popup_wrapper').css('display', '');
		}).done(function(){
			$('.wrapper').unblock();
		});
	};

	var templateBody = {};
	templateBody = $('.dataTemplate').clone(true);
	$('.dataTemplate').remove();
	
	var alertNoCardMsg = function(){
		
		var noCard = $('#noCard').val();
		
		if(noCard.length > 0){
			alert(noCard);
		}
	}
	alertNoCardMsg();
	loadDataFunc();
	
});