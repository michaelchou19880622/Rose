/**
 * 
 */
$(function(){
	
	$('.cancel').click(function(){
		$('.popup_wrapper').css('display', 'none');
	});
	
	$('.check').click(function(){
		window.location.reload();
	});

	$('.confirm').click(function(){
//		var actionStore = $(this).attr("actionStore");
//		console.info("actionStore", actionStore);
		
		var couponId = $(this).attr("couponId");
		console.info("couponId", couponId);

		var postData = {
			couponId : couponId
		};

		$('.wrapper').block({ "message" : "處理中...."});
		$.ajax({
			type : "POST",
			url : 'createActionUserCouponForUse',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			var str = "優惠劵使用成功";
			if(response){
				str = response;
			}
			$('.wrapper').unblock();
			$('.popup_text').html(str);
			$('.popup_box').css('margin-top', '80%');
			$('.cancel').css('display', 'none');
			$('.confirm').css('display', 'none');
			$('.check').css('display', '');
			$('.popup_wrapper').css('display', '');
		}).fail(function(response){
			console.info(response);
			var str = "";
			if(response && response.status == 501){
				str = "<br>[" + response.responseText + "]";
			}
			$('.wrapper').unblock();
			$('.popup_text').html('優惠劵使用失敗' + str);
			$('.popup_box').css('margin-top', '80%');
			$('.cancel').css('display', 'none');
			$('.confirm').css('display', 'none');
			$('.check').css('display', '');
			$('.popup_wrapper').css('display', '');
		}).done(function(){
		});
	});
	
	var clickSelect = function(event){
		var actionStore = $(this).attr('class');
		var couponId = $(this).closest('div').attr('couponId');
		console.info("actionStore", actionStore, "couponId", couponId);
		
		$('html, body').animate({
			scrollTop: 0
		}, 600);

		$('.popup_text').html('確認使用此優惠劵？');
		$('.popup_box').css('margin-top', '70%');
		$('.confirm').attr('actionStore', actionStore);
		$('.confirm').attr('couponId', couponId);
		$('.popup_wrapper').css('display', '');
	};
	
	var settingSelectStore = function(){
		var selectStore = $('.select_store').attr('selectStore');
		if(selectStore){
			var list = selectStore.split(",");
			$.each(list, function(i, o){
				try {
					$('.' + o).css('display', '');
				}
				catch(err) {
					console.error(err);
				} 
			});
		}
		else{
			$('.select_store a').css('display', '');
		}
		
		$('.select_store a').click(clickSelect);
	};
	
	settingSelectStore();
});