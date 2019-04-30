/**
 * 
 */
$(function(){
		
	$('.btn_query').click(function(){
		
		var userId = $('#userId').val();
		if(!userId){
			alert('請輸入UID資料');
			return;
		}

		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/admin/getCouponUserActionList',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : userId
		}).success(function(response){
			console.info(response);
			$('.dataTemplate').remove();

			$.each(response, function(i, o){
				
				var dataBody = templateBody.clone(true);
				dataBody.find('.couponTitle').html(o.mid);
				dataBody.find('.actionTime').html(moment(o.actionTime).format('YYYY-MM-DD HH:mm'));
				dataBody.find('.actionType').html(o.actionType);
				
				dataBody.find('.btn_detele').attr('id', o.id);
				
				$('#tableBody').append(dataBody);
			})
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	});
	
	$('.btn_detele').click(function(){
		var id = $(this).attr('id');
		console.info(id);
		
		if (!confirm("請確認是否刪除？")) return false; //點擊取消

		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/deleteActionUserCoupon?id=' + id
		}).success(function(response){
			alert("刪除成功！");
			$('.btn_query').click();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	});

	var templateBody = {};
	
	var initTemplate = function(){

		templateBody = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
	}

	initTemplate();
});