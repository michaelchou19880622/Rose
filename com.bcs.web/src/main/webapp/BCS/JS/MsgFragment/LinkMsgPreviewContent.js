/**
 *
 */
$(function(){

	//產生連結訊息預覽的內容
	$.BCS.previewMsgLink = function(content, data) {
		var href = '';
		
		if(data.linkUriParams.lastIndexOf('BcsPage:', 0) == 0){
			href = bcs.mContextPath + '/index';
		} else if (data.linkUriParams.lastIndexOf('http://', 0) == 0 
				|| data.linkUriParams.lastIndexOf('https://', 0) == 0) {
			href = data.linkUriParams;
		}
		
		var textHtml = $.BCS.escapeHtml(data.textParams);
		textHtml = textHtml.replace(/\s/g, '&nbsp;');
		var src = bcs.bcsResourcePath + '/images/linkMsgLogo.jpg';
		var previewDiv = $('<div style="margin-bottom: 10px;">'
							+ '<a href="' + href + '" target="_blank" style="text-decoration: none; display: inline-block;">'
								+ '<div class="previewLinkContent">'
									+ '<div class="previewContent" style="width: 240px; font-weight: bold; margin-bottom: 10px;">'
										+ '<div style="display: inline-block;"><img src="' + src + '" style="margin-right: 10px;" /></div>'
										+ '<div style="display: inline-block; width: 140px; height: 70px; word-break: break-all; overflow: hidden;">' 
											+ textHtml 
										+ '</div>'
									+ '</div>'
									+ '<div style="border-top: solid 1px #D1D0CE;">'
										+ '<div style="float: left;">前往</div>'
										+ '<div style="color: #D1D0CE; float: right;">＞</div>'
										+ '<div style="clear: both;"></div>'
									+ '</div>'
								+ '</div>'
							+ '</a>' 
							+ '<br />'
							+ '<span class="previewTime">' + moment().format("LT") + '</span>'
						+ '</div>');
		previewDiv.find('.previewLinkContent')
			.css({
				"background-color": "#E4E8EB",
				"border-radius": "10px",
				"width": "240px",
				"padding": "10px"
			});
		content.append(previewDiv);
	};
});
