/**
 *
 */
$(function(){

	//產生優惠劵預覽的內容
	$.BCS.previewMsgRewardCard = function(content, data) {
		var previewDiv = $('<div style="margin-bottom: 10px;"></div>');
		content.append(previewDiv);

		$.ajax({
			type: 'GET',
			url: bcs.bcsContextPath + "/edit/getContentRewardCard?rewardCardId=" + data.rewardCardId,
		}).success(function(response){
			var rewardCard = response;
			console.info('rewardCard', rewardCard);

			var href = bcs.mContextPath + '/index';			
			var rewardCardTitle = $.BCS.escapeHtml(rewardCard.rewardCardMainTitle);
			var rewardCardStartUsingTime = moment(rewardCard.rewardCardStartUsingTime).format('YYYY-MM-DD HH:mm');
			var rewardCardEndUsingTime = moment(rewardCard.rewardCardEndUsingTime).format('YYYY-MM-DD HH:mm');
			var src = bcs.bcsContextPath + "/getResource/IMAGE/" + rewardCard.rewardCardListImageId;
			previewDiv.append('<a href="' + href + '" target="_blank" style="text-decoration: none; display: inline-block;">'
								+ '<div class="previewRewardCardContent">'
									+ '<div class="previewContent" style="width: 240px; font-weight: bold; margin-bottom: 10px;">'
										+ '<img src="' + src + '" style="width: 70px; margin-right: 10px; vertical-align: top;" />'
										+ '<div style="display: inline-block; width: 140px; height: 70px;">' 
											+ '<div style="font-size: 16pt; overflow: hidden; text-overflow: ellipsis; white-space : nowrap; height: 30px;">' + rewardCardTitle + '</div>'
											+ '<div>' + rewardCardStartUsingTime + ' ~<br/>' + rewardCardEndUsingTime + '</div>'
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
			previewDiv.find('.previewRewardCardContent')
				.css({
					"background-color": "#E4E8EB",
					"border-radius": "10px",
					"width": "240px",
					"padding": "10px"
				});
			console.log(previewDiv);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};
});
