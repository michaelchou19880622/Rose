$(function(){
	
	$('.getPoint').click(function(){
		
		$.ajax({
			type : "GET",
			url : '../m/createActionUserRewardCardForUse?MID=' + $('.midSelect option:selected').val() 
					+ '&rewardCardId=' + $('.rewardCardSelect option:selected').val() 
					+ '&pointAmount=' + $('.pointSelect option:selected').val()
		}).success(function(response){
			console.info(response);
			alert("集點成功");

		}).fail(function(response){
			console.info(response);
			alert(response)
		}).done(function(){
		});
	});
	
	var midSelectChange = function(){
		console.info("midSelect" + $('.midSelect option:selected').text());
		
		$.ajax({
			type : "GET",
			url : '../m/getLineUserRewardCardList?MID=' + $('.midSelect option:selected').text()
		}).success(function(response){
			console.info(response);
			$('.rewardCardOption').remove();
			
			$.each(response, function(i, o){

				var rewardCardList = $('<option value="" class="rewardCardOption"></option>');

				rewardCardList.val(o.rewardCardId);
				rewardCardList.html(o.rewardCardId);

				$('.rewardCardSelect').append(rewardCardList);
			});

		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};
	
	var loadDataFunc = function(){
		$.ajax({
			type : "GET",
			url : '../m/getLineUserList'
		}).success(function(response){
			console.info(response);
						
			$.each(response, function(i, o){

				var midList = $('<option value="" class="midOption"></option>');

				midList.val(o.mid);
				midList.html(o.mid);

				$('.midSelect').append(midList);
			});

			$('.midSelect').change(midSelectChange);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};
	
	loadDataFunc();
});