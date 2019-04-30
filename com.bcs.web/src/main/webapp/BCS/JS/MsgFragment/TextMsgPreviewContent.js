/**
 *
 */
$(function(){
	
	//產生文字預覽的內容
	$.BCS.previewMsgText = function(content, data) {
		var textHtml = $.BCS.escapeHtml(data.Text);
		textHtml = $.BCS.newLineToBrTag(textHtml);
		textHtml = textHtml.replace(/\s/g, '&nbsp;');
		
		var previewDiv = $('<div style="margin-bottom: 10px;"><div class="previewTxtContent"><div class="previewContent" style="width: 240px; word-break: break-all;"></div></div><span class="previewTime">' + moment().format("LT") + '</span></div>');
		previewDiv.find('.previewTxtContent')
			.css({
				"background-color": "rgba(177,231,75,1)",
				"border-radius": "10px",
				"width": "240px",
				"padding": "10px"
			});
		previewDiv.find('.previewContent').html(textHtml);

		content.append(previewDiv);
	};
});
