/**
 * 
 */
$(function(){	
	$.BCS.previewMsgTemplate = function(content, templateData) {

		var html = 
			"<div class='previewTemplate' style='width: 200px;margin: 10px;'>"
			+ "<div style='background-color: white;'>"
				+ "<div style='border-bottom: 1px solid #DCDCDC;'>"
					+ "<div style='margin-bottom: -5px'><img id='previewImg' usemap='#imgUrls' src=''>"
					+ "</div>"
					+ "<div>"
						+ "<div class='previewTitle' style='font-weight: bold; padding: 5px;'></div>"
					+ "</div>"
					+ "<div>"
						+ "<div class='previewContent' style='font-size: small; padding: 5px; color: grey; '></div>"
					+ "</div>"
				+ "</div>"
				+ "<div style='background-color: white;display:flex'>"
					+ "<div class='previewBtn' style='display: none; flex:1;text-align: center; padding: 10px 0px; color: #4682B4;'></div>"
					+ "<div class='previewBtn' style='display: none; flex:1;text-align: center; padding: 10px 0px; color: #4682B4;'></div>"
				+ "</div>"
				+ "<div>"
					+ "<div class='previewBtn' style='display: none; text-align: center; padding: 10px 0px; color: #4682B4;'></div>"
				+ "</div>"
				+ "<div>"
					+ "<div class='previewBtn' style='display: none; text-align: center; padding: 10px 0px; color: #4682B4;'></div>"
				+ "</div>"
				+ "<div>"
					+ "<div class='previewBtn' style='display: none; text-align: center; padding: 10px 0px; color: #4682B4;'></div>"
				+ "</div>"
			+ "</div>"
			+ "<span class='previewTime'>&nbsp</span>"
		+ "</div>";
				
		var data;
		var isFirst = true;
		
		for(var key in templateData){
			content.append(html);
			console.info("key :  ",key,"     templateData :  ",templateData[key]);
			data = templateData[key];//取出template資料
			
			var templateContent = content.find('.previewTemplate:last');
			var templateType = data[1];
			var templateMsgTitle = data[3];
			var templateMsgText = data[4];
			
			if (isFirst) {
				templateContent.find('.previewTime').html(moment(new Date()).format("LT"));
				isFirst = false;
			}

			templateContent.find('.previewTitle').text(templateMsgTitle);
			templateContent.find('.previewContent').text(templateMsgText);

			
			if(data[2] != null){
				var imgId = data[2];
				var imgJq = templateContent.find('#previewImg');
				imgJq.attr('src', bcs.bcsContextPath + '/getResource/IMAGE/' + imgId);

				var width = 0;
				var imgTimeout = function() {
					width = imgJq[0].width;
					if (width > 0) {
						imgJq[0].width = (imgJq[0].width / 3.5);

						templateContent.css('width', imgJq[0].width);
					} else {
						setTimeout(imgTimeout, 100);
					};
				}
				imgTimeout();

			}
			
			actionNumber = (data.length-6)/6;

			if (templateType != 'confirm') {
				templateContent.find('.previewBtn:eq(0)').remove();
			}

			if (templateType == 'carousel') {
				templateContent.css('display', 'inline-block');
			}
			    				
			for(var i=0; i<actionNumber; i++){
				var label = data[7+6*i];
				templateContent.find('.previewBtn:eq(' + i + ')').text(label).show();
			}
		}
	}
});