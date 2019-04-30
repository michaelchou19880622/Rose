/**
 * 
 */
$(function(){
	$.BCS.newPreviewDialog = function(title, dialogDiv, config) {
		var defaultConfig = {
			title: title,
	    	autoOpen: false, //初始化不會是open
	    	resizable: false, //不可縮放
	    	modal: true, //畫面遮罩
	    	draggable: false, //不可拖曳
	    	width : 395,
	    	minHeight : 500,
	    	position: { my: "top", at: "top", of: window  }
	    };
		config = $.extend(defaultConfig, config);
		dialogDiv.dialog(config);
		
		dialogDiv.css({
			"background-image": "url(/bcs/BCS/images/previewTxtBackground.jpg)",
			"background-size": "contain", //圖大於div，fit大小
		});
	}
});