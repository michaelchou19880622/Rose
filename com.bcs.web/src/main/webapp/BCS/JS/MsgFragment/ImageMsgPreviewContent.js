/**
 *
 */
$(function(){

	//產生照片預覽的內容
	$.BCS.previewMsgImage = function(content, data) {
		var previewDiv = $('<div style="margin-bottom: 10px;"><div class="previewImageContent"><div class="previewContent" style="width: 240px;"></div></div><span class="previewTime">' + moment().format("LT") + '</span></div>');
		var src = bcs.bcsContextPath + '/getResource/' + data.resourceType + '/' + data.resourceId;
		previewDiv.find('.previewContent').append('<img src="' + src + '" style="max-width: 290px; max-height: 290px;" />');

		content.append(previewDiv);
	};
});
