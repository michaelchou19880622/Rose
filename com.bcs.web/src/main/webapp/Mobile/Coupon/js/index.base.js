/**
 * 
 */
$(function(){
	console.log(location);
	
	$('.cancel').click(function(){
		$('.popup_wrapper').css('display', 'none');
	});
	
	$('.check').click(function(){
		var noCoupon = $('#noCoupon').val();
		if(noCoupon.length!=0){
			console.log('noCoupon'+noCoupon);
			$('#noCoupon').val("");
			$('.popup_wrapper').css('display', 'none');
		}else
			window.location.reload();
	});

	$('.confirm').click(function(){
		var idnum = $(this).attr("idnum");
		console.info("idnum", idnum);
		
		var couponId = $(this).attr("couponId");
		console.info("couponId", couponId);

		var postData = {
				idnum : idnum,
				couponId : couponId
		};

		$('.wrapper').block({ "message" : "處理中...."});
		$.ajax({
			type : "POST",
			url : 'createActionUserCouponForGetApi',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			console.info("優惠劵領取成功");
			/*
			20190121 功能未開放故comment out
			*/
//			window.location.assign('userCouponContentPage?referenceId=' + response);
		}).fail(function(response){
			console.info(response);
			var str = "";
			if(response && response.status == 501){
				str = "<br>[" + response.responseText + "]";
			}
			$('.wrapper').unblock();
			$('.popup_text').html('優惠劵領取失敗' + str);
			$('.cancel').css('display', 'none');
			$('.confirm').css('display', 'none');
			$('.check').css('display', '');
			$('.popup_wrapper').css('display', '');
		}).done(function(){
		});
	});
	
	var couponTitleFunc = function(event){
		var idnum = $(this).attr("idnum");
		console.info("idnum", idnum);
		
		var show = $(this).attr("show");
		console.info("show", show);
		
		var couponId = $(this).attr("couponId");
		console.info("couponId", couponId);
		
		var str = '是否領取此數位優惠券<br>';
		
		if(show == ""){
			$('html, body').animate({
				scrollTop: 0
			}, 600);
			
			$('.popup_text').html(str);
			$('.popup_box').css('margin-top', '70%');
			$('.confirm').attr('idnum', idnum);
			$('.confirm').attr('couponId', couponId);
			$('.popup_wrapper').css('display', '');
		}
		else{

			var postData = {
					idnum : idnum,
					couponId : couponId
			};

			$('.wrapper').block({ "message" : "處理中...."});
			$.ajax({
				type : "POST",
				url : 'createActionUserCouponForGetApi',
	            cache: false,
	            contentType: 'application/json',
	            processData: false,
				data : JSON.stringify(postData)
			}).success(function(response){
				console.info(response);
				console.info("優惠劵領取成功");
				if(typeof response === "string"){				
					/*
					 20190121 功能未開放故comment out
					*/
//					window.location.assign('userCouponContentPage?referenceId=' + response);
				}
			}).fail(function(response){
				console.info(response);
				var str = "";
				if(response && response.status == 501){
					str = "<br>[" + response.responseText + "]";
				}
				$('.wrapper').unblock();
				$('.popup_text').html('優惠劵領取失敗' + str);
				$('.cancel').css('display', 'none');
				$('.confirm').css('display', 'none');
				$('.check').css('display', '');
				$('.popup_wrapper').css('display', '');
			}).done(function(){
			});
		}
	};
	
	var myCoupon = {};
	
	var loadDataFunc = function(){

		$('.wrapper').block({ "message" : "處理中...."});
		$.ajax({
			type : "GET",
			url : 'getMyCouponList'
		}).success(function(response){
			$('.dataTemplate').remove();
			console.info(response);
	
			$.each(response, function(i, couponData){
				
				var couponBody = templateBody.clone(true);
				
				var couponTitle = couponData.couponTitle;
				
				couponTitle+= "<br>" + couponData.status;
				
				couponBody.find('.coupon_title').html(couponTitle);

				couponBody.find('.linkBody').attr("show", couponData.status);
				couponBody.find('.linkBody').attr("idnum", couponData.idnum);
				couponBody.find('.linkBody').attr("couponId", couponData.couponId);

				couponBody.find('.linkBody').click(couponTitleFunc);
				
				var img = couponData.couponListImageId;
				if(img != null && img!= ""){
					couponBody.find('.coupon_thumb').attr('src', img);
				}

				$('.coupon_list').append(couponBody);
			});
			
		}).fail(function(response){
			console.info(response);
			var str = "";
			if(response && response.status == 501){
				str = "<br>[" + response.responseText + "]";
			}
			$('.wrapper').unblock();
			$('.popup_text').html('優惠劵錯誤失敗' + str);
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
	var alertNoCouponMsg = function(){
		
		var noCoupon = $('#noCoupon').val();
		
		if(noCoupon.length > 0){
			$('.wrapper').unblock();
			$('.popup_text').html(noCoupon);
			$('.cancel').css('display', 'none');
			$('.confirm').css('display', 'none');
			$('.check').css('display', '');
			$('.popup_wrapper').css('display', '');
		}
	}
	alertNoCouponMsg();
	loadDataFunc();
});