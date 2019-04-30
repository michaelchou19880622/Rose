/**
 * 
 */
$(function(){
	//產生文字預覽的內容
	$.BCS.previewRichMsgText = function(content, data) {
		content.html('<div class="previewTxtContent"></div><span class="previewTime"></span>');
		content.find('.previewTime').html(moment(new Date()).format("LT"));
		
		content.find('.previewTxtContent').css({
			"background-color": "rgba(177,231,75,1)",
			"border-radius": "10px",
			"width": "240px",
			"padding": "10px"
		});
		
		content.find('.previewTxtContent').html('<div class="previewContent" style="width: 240px;"></div>')
		var richTitle = data[1];
		if (data[3].indexOf(",") > -1) {
			var richUrls = data[3].split(",");
			var richUrlTxts = data[4].split(",");
		} else {
			var richUrls = [data[3]];
			var richUrlTxts = [data[4]];
		}
		
		var appendHtml = '<p style="font-size: larger; word-break:break-all; ">' + richTitle + '</p>';
		for (var i in richUrls) {
			appendHtml += '<br/><br/>';

			var urlTxt = richUrls[i] ;
			var url = richUrls[i] ;
			if(url.lastIndexOf('BcsPage:', 0) == 0){
				urlTxt = "bcs.index";
				url = bcs.mContextPath + '/index';
			}
				
			appendHtml += '<p style="font-size: larger; word-break:break-all; ">' + richUrlTxts[i] + '</p>' 
						+ '<a href="' + url + '" target="_blank" style="color: #3665dd; font-size: larger; word-break:break-all; ">' + urlTxt + '</a>';
		}
		content.find('.previewContent').append(appendHtml);
	}
	
	//產生圖片預覽的內容
	$.BCS.previewRichMsgImage = function(content, data) {
		content.html('<div class="previewImgContent"></div><span class="previewTime"></span>');
		content.find('.previewTime').html(moment(new Date()).format("LT"));
		
		content.find('.previewImgContent').html('<div class="previewContent"><img id="previewImg" usemap="#imgUrls">' +
						'<map name="imgUrls"></map><div>');
		
		var richImgId = data[2];
		if (data[3].indexOf(",") > -1) {
			var richUrls = data[3].split(",");
			var multiStartX = data[6].split(",");
			var multiStartY = data[7].split(",");
			var multiEndX = data[8].split(",");
			var multiEndY = data[9].split(",");
		} else {
			var richUrls = [data[3]];
			var multiStartX = [data[6]];
			var multiStartY = [data[7]];
			var multiEndX = [data[8]];
			var multiEndY = [data[9]];
		}
		
		var imageHeight = data[10];
		var imageWidth = data[11];
		
		content.find('#previewImg').attr('src', bcs.bcsContextPath + '/getResource/IMAGE/' + richImgId);
		content.find('#previewImg').attr('height', imageHeight / 3.5);
		content.find('#previewImg').attr('width', imageWidth / 3.5);
		
		var appendHtml = "";
		for (var i in richUrls) {

			var url = richUrls[i] ;
			if(url.lastIndexOf('BcsPage:', 0) == 0){
				url = bcs.mContextPath + '/index';
			}
			
			appendHtml += '<area shape="rect" coords="' + (multiStartX[i] / 3) + ', ' + (multiStartY[i] / 3) + ', ' 
				+ (multiEndX[i] / 3) + ', ' + (multiEndY[i] / 3) + '" href="' + url + '" target="_blank" >';
		}
		
		//影像地圖
		content.find('map').append(appendHtml); 
	}
});