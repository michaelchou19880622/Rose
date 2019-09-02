/**
 * 
 */

$(function(){
	var gameId = "";			//gameId
	var isBinding = "";			//isBinding
	var checkboxArray = [
		'Hair',
		'Oral',
		'House',
		'Female',
		'Male',
		'Baby'
	];
	
	$('#checkboxImage').click(function(event) {
		$(this).hide();
		$('#checkboxCheckedImage').show();
	});
	
	$('#checkboxCheckedImage').click(function(event) {
		$(this).hide();
		$('#checkboxImage').show();
	});
	
	var shareMsg = "";
	
	var initPage = function(){
		gameId = $.urlParam("gameId");
		if(!gameId){
			gameId = $('#gameId').val();
		}
		isBinding = $('#isBinding').val();
		if(isBinding == "true"){
			$('.notBindingShow').hide();
		}
		
		$.ajax({
            type: 'GET',
            url: '../m/Game/getPrizeDetail/'+gameId,
		}).success(function(response){
			$('.prizeImageShow').find('img').attr('src', "../bcs/getCdnResource/IMAGE/" + response['prizeImageId']);
		}).fail(function(response){
			console.info(response);
		}).done(function(){
		});
		
		$.ajax({
            type: 'GET',
            url: '../m/Game/turntable/'+gameId,
		}).success(function(response){
			$('.headerImage').find('img').attr('src', '../bcs/getCdnResource/IMAGE/' + response.headerImageId);
			$('.footerImage').find('img').attr('src', '../bcs/getCdnResource/IMAGE/' + response.footerImageId);
			if(response.shareImageId && response.shareImageId != ""){
				$('.shareImage').find('img').attr('src', '../bcs/getCdnResource/IMAGE/' + response.shareImageId);
			}
			if(response.shareSmallImageId && response.shareSmallImageId != ""){
				$('.shareSmallImage').find('img').attr('src', '../bcs/getCdnResource/IMAGE/' + response.shareSmallImageId);
			}
			shareMsg = response.shareMsg;
		}).fail(function(response){
			console.info(response);
		}).done(function(){
		});
	}
	
	$('#goWinPrizePage').click(function(){
		var acceptPrizeModel = {};
		
		if(!checkValid()){
			return;
		}
		acceptPrizeModel.userName = $("#userName").val();
		acceptPrizeModel.address = $("#address").val();
		acceptPrizeModel.numOfChildren = $("#numOfChildren").val();

		acceptPrizeModel.gender = $('[name="Gender"]:checked').val();
		acceptPrizeModel.birthdayYear = $('#BirthdayYear').val();
		acceptPrizeModel.birthdayMonth = $('#BirthdayMonth').val();
		acceptPrizeModel.phoneNum = $('#PhoneNum').val();
		
		acceptPrizeModel.preferredProducts = [];

		checkboxArray.forEach(function(value, id){
			if ($('#checkboxChecked' + value).is(':visible')) {
				acceptPrizeModel.preferredProducts.push(value);
			}
		});
		
		$.ajax({
			type : 'POST',
			url : '../m/Game/acceptPrize/'+gameId,
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(acceptPrizeModel)
		}).success(function(response){
			console.info('response',response);
			
			if(response == 'success'){
				//window.location = 'm/turntableIndexPage?gameId=' + gameId;
			}else{
				alert("您已領取過獎品!");
			}
			
			location.reload();
		}).fail(function(response){
			console.info(response);
			location.reload();
		}).done(function(){
		});
	});
	
	$('.shareBtn').click(function(){

		$.ajax({
			type : "POST",
			url : 'gameShareTrigger?gameId=' + gameId,
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : ""
		}).success(function(response){
			console.info(response);
			console.info("用LINE傳送成功");
			var url = encodeURIComponent(shareMsg);
			console.info(url);
			window.location.assign('line://msg/text/?' + url);
		}).fail(function(response){
			console.info(response);
			console.info("用LINE傳送失敗");
			location.reload();
		}).done(function(){
		});
	});

	//初始checkbox check 事件
	checkboxArray.forEach(function(value, id){
		$('#checkbox' + value).click(function() {
			$(this).hide();
			$('#checkboxChecked' + value).show();
		});

		$('#checkboxChecked' + value).click(function() {
			$(this).hide();
			$('#checkbox' + value).show();
		});
	});
	
	//檢查是否有不合法資料
	var checkValid = function(){
		if($('#userName').val() == ""){
			alert("請輸入姓名!");
			return false;
		}
		
		if($('#address').val() == ""){
			alert("請輸入地址!");
			return false;
		}
		
		if(isBinding == "false"){
			
			var Gender = $('[name="Gender"]:checked').val();
			console.info('Gender', Gender);
			if(!Gender){
				alert('請選擇 性別');
				return;
			}

			var BirthdayYear = $('#BirthdayYear').val();
			console.info('BirthdayYear', BirthdayYear);
			
			if(!BirthdayYear){
				alert('請選擇 生日 - 年');
				return;
			}
	
			var BirthdayMonth = $('#BirthdayMonth').val();
			console.info('BirthdayMonth', BirthdayMonth);
	
			if(!BirthdayMonth){
				alert('請選擇 生日 - 月');
				return;
			}
			
			var PhoneNum = $('#PhoneNum').val();
			console.info('PhoneNum', PhoneNum);
			
			if(!PhoneNum){
				alert('請輸入行動電話');
				return;
			}
			
			if(PhoneNum.length != 10){
				alert('請輸入正確的行動電話');
				return;
			}
		}
	
		if ($('#checkboxCheckedImage').is(':hidden')) {
			alert('請閱讀並同意服務條款');
			return;
		}
		
		return true;
	}
	
	initPage();
});