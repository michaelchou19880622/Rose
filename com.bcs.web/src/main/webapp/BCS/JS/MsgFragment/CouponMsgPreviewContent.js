/**
 *
 */
$(function(){

	//產生優惠劵預覽的內容
	$.BCS.previewMsgCoupon = function(content, data) {
		var previewDiv = $('<div style="margin-bottom: 10px;"></div>');
		content.append(previewDiv);

		$.ajax({
			type: 'GET',
			url: bcs.bcsContextPath + "/edit/getContentCoupon?couponId=" + data.couponId,
		}).success(function(response){
			var coupon = response;
			console.info('coupon', coupon);

			var href = bcs.mContextPath + '/index';			
			var couponTitle = $.BCS.escapeHtml(coupon.couponTitle);
			var couponStartUsingTime = moment(coupon.couponStartUsingTime).format('YYYY-MM-DD HH:mm');
			var couponEndUsingTime = moment(coupon.couponEndUsingTime).format('YYYY-MM-DD HH:mm');
			var src = bcs.bcsContextPath + "/getResource/IMAGE/" + coupon.couponListImageId;
			previewDiv.append('<a href="' + href + '" target="_blank" style="text-decoration: none; display: inline-block;">'
								+ '<div class="previewCouponContent">'
									+ '<div class="previewContent" style="width: 240px; font-weight: bold; margin-bottom: 10px;">'
										+ '<img src="' + src + '" style="width: 70px; margin-right: 10px; vertical-align: top;" />'
										+ '<div style="display: inline-block; width: 140px; height: 70px;">' 
											+ '<div style="font-size: 16pt; overflow: hidden; text-overflow: ellipsis; white-space : nowrap; height: 30px;">' + couponTitle + '</div>'
											+ '<div>' + couponStartUsingTime + ' ~<br/>' + couponEndUsingTime + '</div>'
										+ '</div>'
									+ '</div>'
									+ '<div style="border-top: solid 1px #D1D0CE;">'
										+ '<div style="float: left;">立即確認</div>'
										+ '<div style="color: #D1D0CE; float: right;">＞</div>'
										+ '<div style="clear: both;"></div>'
									+ '</div>'
								+ '</div>'
							+ '</a>' 
							+ '<br />'
							+ '<span class="previewTime">' + moment().format("LT") + '</span>');
			previewDiv.find('.previewCouponContent')
				.css({
					"background-color": "#E4E8EB",
					"border-radius": "10px",
					"width": "240px",
					"padding": "10px"
				});
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};
});
