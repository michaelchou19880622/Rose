/**
 *
 */
$(function(){

	//產生貼圖預覽的內容
	$.BCS.previewMsgSticker = function(content, data) {
		var previewDiv = $('<div style="margin-bottom: 10px;"><div class="previewStickersContent"><div class="previewContent" style="width: 240px;"></div></div><span class="previewTime">' + moment().format("LT") + '</span></div>');
		previewDiv.find('.previewStickersContent')
			.css({
				"background-color": "rgba(177,231,75,1)",
				"border-radius": "10px",
				"width": "240px",
				"padding": "10px"
			});

		var src = bcs.bcsResourcePath + '/images/Stickers/' + data.STKID + '_key.png';
		previewDiv.find('.previewContent').append('<img src="' + src + '" />');

		content.append(previewDiv);
	};
});
