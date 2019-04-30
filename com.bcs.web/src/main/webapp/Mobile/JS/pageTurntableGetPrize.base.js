$(function (){
	var gameId = "";			//gameId
	var prizeCount = 0;	        //目前獎品數量
	var prizeList = [];
	var prizeTrTemplate = {};
	var obtainedPrizeId;
	var MID;
	
	var initPage = function(){
		gameId = $.urlParam("gameId");
		MID = $.urlParam("MID");
	};

	// 表單驗證
	var validator = $('#formPrize').validate({
		rules : {			
			'userName' : {
				required : {
			        param: true
				}
			},
			'userPhoneNumber' : {
				required : {
			        param: true
				}
			},
			'userEMail' : {
				required : {
			        param: true
				}
			},
		}
	});
	
	$('#acceptPrize').click(function(){
		if (!validator.form()) {
			return;
		}

		var acceptPrizeModel = {};
		
		acceptPrizeModel.userName = $("#userName").val();
		acceptPrizeModel.userPhoneNumber = $("#userPhoneNumber").val();
		acceptPrizeModel.userEMail = $("#userEMail").val();
		
		$.ajax({
			type : 'POST',
			// url : '../m/Game/acceptPrize/'+gameId,
			url : '../m/Game/acceptPrize/'+gameId+'?MID=' + MID,
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(acceptPrizeModel)
		}).success(function(response){
			console.info('response',response);
			
			alert("領取成功!");
		}).fail(function(response){
			console.info(response);
			alert(JSON.stringify(response));
		}).done(function(){
		});
	});
	
	initPage();
});
