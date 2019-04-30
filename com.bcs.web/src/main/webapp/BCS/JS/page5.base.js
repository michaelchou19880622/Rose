/**
 * 
 */
$(function(){
	$('.btn_add').click(function(){
 		window.location.replace('richMsgCreatePage');
	});

	var loadDataFunc = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getRichMsgList'
		}).success(function(response){
			$('.richMsgTrTemplate').remove();
			
			for(key in response){
				var richMsgTr = richMsgTrTemplate.clone(true);

				var valueObj = response[key];
				console.info('valueObj', valueObj);

				richMsgTr.find('.richMsgId').val(key);
				richMsgTr.find('.richMsgTitle').html(valueObj[0]);
				richMsgTr.find('.richMsgImgTitle img').attr('richId', key);
				richMsgTr.find('.richMsgImgTitle img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + valueObj[4]);
				richMsgTr.find('.richMsgImgTitle img').click(richMsgSelectEventFunc);
				richMsgTr.find('.richMsgImgTitle a').attr('href', bcs.bcsContextPath + '/edit/richMsgCreatePage?richId=' + key + '&actionType=Edit');
				var urls = [];
				if(valueObj[1]){
					urls = valueObj[1].split(",");
				}
				var titles = [];
				if(valueObj[5]){
					titles = valueObj[5].split(",");
				}
				var actions = [];
				if(valueObj[7]){
					actions = valueObj[7].split(",");
				}
				var sendMessages = [];
				if(valueObj[8]){
					sendMessages = valueObj[8].split(",");
				}
				var urlHtml = "";
				if(urls != null && urls.length > 0){
					for (var i=0; i<urls.length; i++) {
						var title = titles[i];
						if(!title){
							title = urls[i];
						}
						var action = actions[i];
						if(!action){
							action = "連結";
							urlHtml += action + "-<a href='" + urls[i] + "' target='_blank'>" + title + "</a><br/>";
						}
						else{
							if("web" == action){
								action = "連結";
								urlHtml += action + "-<a href='" + urls[i] + "' target='_blank'>" + title + "</a><br/>";
							}
							else{
								action = "文字";
								urlHtml += action + "-" + sendMessages[i] + "<br/>";
							}
						}
					}
				}
				else{
					var action = "文字";
					urlHtml += action + "-" + sendMessages[0] + "<br/>";
				}
				richMsgTr.find('.richMsgImgUrl').html(urlHtml);
				
				var time = valueObj[2].replace(/\.\d+$/, ''); // 刪去毫秒
				richMsgTr.find('.richMsgCreateTime').html(time);
				richMsgTr.find('.richMsgCreateUser').html(valueObj[3]);

				$('#richMsgListTable').append(richMsgTr);
			}
			
			setDeleteBtnEvent();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};
	
	var richMsgSelectEventFunc = function(){
		var richId = $(this).attr('richId');
 		window.location.replace(bcs.bcsContextPath + '/edit/richMsgCreatePage?richId=' + richId + '&actionType=Edit');
	};
	
	var setDeleteBtnEvent = function() {
		$('.btn_detele').click(function(e) {
			var deleteConfirm = confirm("請確認是否刪除");
			if (!deleteConfirm) return; //點擊取消
			
			var richMsgTr = $(this).closest(".richMsgTrTemplate");
			var selectedRichId = richMsgTr.find('.richMsgId').val();
			$.ajax({
				type : "DELETE",
				url : bcs.bcsContextPath + '/admin/deleteRichMsg/' + selectedRichId
			}).success(function(response){
				alert("刪除成功！");
				loadDataFunc();
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
		});
		
		$('.btn_copy').click(function(e) {
			var richMsgTr = $(this).closest(".richMsgTrTemplate");
			var selectedRichId = richMsgTr.find('.richMsgId').val();
			window.location.replace(bcs.bcsContextPath + '/edit/richMsgCreatePage?richId=' + selectedRichId + '&actionType=Copy');
		});
	}

	var richMsgTrTemplate = {};
	var initTemplate = function(){
		richMsgTrTemplate = $('.richMsgTrTemplate').clone(true);
		$('.richMsgTrTemplate').remove();
	}

	initTemplate();
	loadDataFunc();
});