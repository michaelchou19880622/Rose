/**
 * 
 */
$(function(){
	const COUPON_STATUS = {
			STATUS_GOTTEN: {
				statusCode : "STATUS_GOTTEN",
				status : "已領取此優惠券"
			},
			STATUS_GOTTEN_SAME_POINT:{
				statusCode :"STATUS_GOTTEN_SAME_POINT",
				status : "已領取同點數之優惠券，無法領取"
			},
			STATUS_CANNOT_GET:{
				statusCode :"STATUS_CANNOT_GET",
				status : "點數不足，無法領取"
			} ,
			STATUS_CAN_GET: {
				statusCode : "STATUS_CAN_GET",
				status :"可以領取"
			},
			STATUS_USED: {
				statusCode : "STATUS_USED",
				status :"已使用此優惠券"
			},
			STATUS_CANNOT_GET_AMOUNT_ZERO: {
				statusCode : "STATUS_CANNOT_GET_AMOUNT_ZERO",
				status :"數量不足，無法領取"
			}
	};	
	
	function leadingZeros(num, digits) {
        
        var zero = '';
        var StringNum = num.toString();
        if (StringNum.length < digits) {
            for (var i = 0; i < digits - StringNum.length; i++) {
                zero += '0';
            }
        }
        num = num.toString();
        return zero + num;
    }
	
	//生成集點卡點數圖案
	var stampListRender = function(target) {
		  console.log("stampListRender");
	      var totalAmount = parseInt($('.requirePoint').val());
	      var havePoint = parseInt($('.havePoint').val()) > totalAmount ? totalAmount : parseInt($('.havePoint').val());
	      document.getElementById(target).innerHTML = "";
	      var size = 0;
	      var lineDiv = 10;
	      var strHtml = '';
	      var idxSplit = [];
	      var clsName = 'check';
	      
	      if (totalAmount < 6) {
	            size = leadingZeros(totalAmount, 2)+'';
	      } else {
	            if (totalAmount > 5 && totalAmount < 26) {
	                  size = '06-25';
	                  lineDiv = 5;
	            } else if (totalAmount > 25 && totalAmount < 31) {
	                  size = '26-30';
	            } else {
	                  size = '31-50';
	            }
	      }
	      
	      //產生點數圖案
	      strHtml += '<ul class="bang_size_'+size+'">';
	      	//已蓋章的點數圖案
	      for (var startIdx = 1; startIdx <= havePoint; startIdx++) {
	            if (startIdx == 1) {
	                  strHtml += '<li class="bang" id="bang" stampIdx="'+startIdx+'"></li>'
	            }else if (startIdx == totalAmount) {
	            	  if(havePoint != totalAmount){
	            		  strHtml += '<li class="goal" stampIdx="'+startIdx+'"></li>';
	            	  }else
	            		  strHtml += '<li class="bang" id="bang" stampIdx="'+startIdx+'"></li>'
	            } else if (startIdx > 9) {
	                  for (var splitIdx = 0, len = startIdx.toString().length; splitIdx < len; splitIdx += 1) {
	                        idxSplit.push(startIdx.toString().charAt(splitIdx));
	                  }//class number stamp
	                  strHtml += '<li class="number stamp" stampIdx="'+startIdx+'">';
	                  strHtml += '    <div class="n'+idxSplit[0]+'"></div>';
	                  strHtml += '    <div class="n'+idxSplit[1]+'"></div>';
	                  idxSplit.splice(0, 2);
	            }else{//class single number stamp
	                  strHtml += '<li class="single number stamp" stampIdx="'+startIdx+'">';
	                  strHtml += '    <div class="n'+startIdx+'"></div>';
	            }
	            strHtml += '</li>';
	            
	            if (lineDiv >= 5 && (startIdx%lineDiv) == 0) {
	                  strHtml += '</ul>';
	                  strHtml += '<ul class="bang_size_'+size+'">';
	            }
	      }
	      	//未蓋章的點數圖案
	      for (var startIdx = havePoint + 1; startIdx <= totalAmount; startIdx++) {
	            if (startIdx == 1) {
	                  strHtml += '<li class="bang" id="bang" stampIdx="'+startIdx+'"></li>'
	            }else if (startIdx == totalAmount) {
	                  strHtml += '<li class="goal" stampIdx="'+startIdx+'"></li>';
	            } else if (startIdx > 9) {
	                  for (var splitIdx = 0, len = startIdx.toString().length; splitIdx < len; splitIdx += 1) {
	                        idxSplit.push(startIdx.toString().charAt(splitIdx));
	                  }
	                  strHtml += '<li class="number" stampIdx="'+startIdx+'">';
	                  strHtml += '    <div class="n'+idxSplit[0]+'"></div>';
	                  strHtml += '    <div class="n'+idxSplit[1]+'"></div>';
	                  idxSplit.splice(0, 2);
	            }else{
	                  strHtml += '<li class="single number" stampIdx="'+startIdx+'">';
	                  strHtml += '    <div class="n'+startIdx+'"></div>';
	            }
	            strHtml += '</li>';
	            
	            if (lineDiv >= 5 && (startIdx%lineDiv) == 0) {
	                  strHtml += '</ul>';
	                  strHtml += '<ul class="bang_size_'+size+'">';
	            }
	      }
	      strHtml += '</ul>';
	      document.getElementById(target).innerHTML = strHtml;
	      if(havePoint == 0){
	    	  $('#bang').attr("class", "bangZero");
	      }
	}
	
	var alertRewardCardResult = function(){
		var rewardCardResult = $('#rewardCardResult').val();
		if(rewardCardResult.length > 0){
			alert(rewardCardResult);
			$.get('../m/RewardCard/removeRewardCardResultAttr', function(response) {
			});
		}
	}
	
	var couponTitleFunc = function(event){
		var couponId = $(this).attr("couponId");
		console.info("couponId", couponId);
		var str = '是否領取此數位優惠券';
		var status = $(this).attr("status");
		if(status === COUPON_STATUS.STATUS_CAN_GET.statusCode){
			$('html, body').animate({
				scrollTop: 0
			}, 600);
			
			$('.popup_text').html(str);
			$('.popup_box').css('margin-top', '70%');
			$('.confirm').attr('couponId', couponId);
			$('.popup_wrapper').css('display', '');
		}else{
			createActionUserCouponForGetApi(couponId);
		}
	};
	
	var loadDataFunc = function(){
		var rewardCardId = $.urlParam("referenceId");
		
		var couponList = CONTENTCOUPONS;// CONTENTCOUPONS 由 html 的  modelAttribute 獲取
		
		if(couponList!=null){
			couponList.sort(function(x,y){return x.requirePoint - y.requirePoint;});
			console.info("couponList",couponList);
			$.each(couponList, function(i, couponData){
				var couponBody = templateBody.clone(true);
				var couponTitle="";
				couponTitle+= "可獲得點數："+ couponData.requirePoint;
				couponTitle+= "<br>" + couponData.couponTitle;
				couponTitle+= "<br>" + COUPON_STATUS[couponData.status].status;
				couponBody.find('.coupon_title').html(couponTitle);
				couponBody.find('.linkBody').attr("couponId", couponData.couponId);
				couponBody.find('.linkBody').attr("status", couponData.status);
				
				if(couponData.status == COUPON_STATUS.STATUS_GOTTEN.statusCode || 
						couponData.status == COUPON_STATUS.STATUS_CAN_GET.statusCode ||
						couponData.status == COUPON_STATUS.STATUS_USED.statusCode)
					couponBody.find('.linkBody').click(couponTitleFunc);
				else{
					console.log(couponBody.find('.linkBody'));
					var chid = couponBody.find('.linkBody').children().clone(true);
					couponBody.find('.linkBody').remove();
					couponBody.append(chid);
				}	
				var img = couponData.couponListImageId;
				if(img != null && img!= ""){
					couponBody.find('.coupon_thumb').attr('src', img);
				}

				$('.coupon_list').append(couponBody);
			});
		}
	};
	
	$('.cancel').click(function(){
		$('.popup_wrapper').css('display', 'none');
	});
	
	$('.confirm').click(function(){
		var couponId = $(this).attr("couponId");
		createActionUserCouponForGetApi(couponId);
	})
	
	$('.check').click(function(){
		window.location.reload();
	});
	
	var createActionUserCouponForGetApi= function(couponId){
		var postData = {
				couponId : couponId
		};
		$('.wrapper').block({ "message" : "處理中...."});
		$.ajax({
			type : "POST",
			url : '../m/RewardCard/createActionUserCouponForGetApi',
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
//				window.location.assign('userCouponContentPage?referenceId=' + response);  
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
	
	var templateBody = {};
	templateBody = $('.dataTemplate').clone(true);
	$('.dataTemplate').remove();
	
	loadDataFunc();
	
	stampListRender('stamp_list_render_area');
	alertRewardCardResult();
	
	
});