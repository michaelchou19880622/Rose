$(function(){
	$.BCS = {};
	
	/**
	 * 初始化標籤元件
	 * @param elementIdOrElement 元素(輸入框或文字區塊)的 id 或元素(輸入框或文字區塊)
	 * @param contentType 標籤內容類型
	 * @param config 配置(可省略)
	 */
	$.BCS.contentFlagComponent = function(elementIdOrElement, contentType, config) {
		var autocompleteList = [];
		
		var defaultTagEditorConfig = {
				autocomplete : {
					source : autocompleteList
				},
				maxLength : 80,
				forceLowercase: true,
				placeholder: '請輸入標籤...'
		};
		
		var tagEditor = null;
		
		if (typeof elementIdOrElement == 'string') {
			tagEditor = $('#' + elementIdOrElement).tagEditor(
					$.extend(defaultTagEditorConfig, config));
		} else {
			tagEditor = $(elementIdOrElement).tagEditor(
					$.extend(defaultTagEditorConfig, config));
		}
		
		var contentFlagComponent = {
			
			/**
			 * 取得此欄位上的標籤
			 */
			getContentFlagList : function() {
				return tagEditor.tagEditor('getTags')[0].tags;
			},
			
			/**
			 * 移除此欄位上的標籤
			 */
			removeContentFlagList : function() {
			    var tags = tagEditor.tagEditor('getTags')[0].tags;
			    for (i = 0; i < tags.length; i++) { tagEditor.tagEditor('removeTag', tags[i]); }
			},
			
			/**
			 * 查詢標籤並顯示在此欄位上
			 */
			findContentFlagList : function(referenceIdStr) {
				var self = this;
				
				var referenceId = encodeURIComponent(referenceIdStr);
				
				$.ajax({
					type: 'GET',
					url: bcs.bcsContextPath + "/edit/getContentFlagList?referenceId=" + referenceId + "&contentType=" + contentType,
				}).success(function(response){
					console.info('response', response);
					self.removeContentFlagList();
					
					for (var i = 0, size = response.length; i < size; i++) {
						tagEditor.tagEditor('addTag', response[i]);
					}
					
					// 呼叫 disabled 函式來更新"純顯示"的 <ul>
					if (tagEditor.prop('disabled')) {
						self.disabled();
					}
				}).fail(function(response){
					console.info(response);
					$.FailResponse(response);
				}).done(function(){
				});
			},
			
			/**
			 * disabled
			 * 原套件似乎沒提供 disabled 功能，這裡是隱藏原本的 <ul> 並作一份模樣相似的 <ul> 來模擬 disabled
			 */
			disabled : function() {
				tagEditor.prop('disabled', true);
				var tagEditorUl = tagEditor.next('ul.tag-editor');
				tagEditorUl.next('.tagEditorDisabled').remove();
				var disabledTagEditorUl = $('<ul class="tag-editor ui-sortable tagEditorDisabled"><li style="width:1px" class="ui-sortable-handle">&nbsp;</li></ul>');
				
				$.each(this.getContentFlagList(), function(index, contentFlag) {
					var jqLi = $('<li><div class="tag-editor-spacer">&nbsp;,</div><div class="tag-editor-tag" style="cursor: auto;"></div></li>');
					jqLi.find('.tag-editor-tag').text(contentFlag);
					disabledTagEditorUl.append(jqLi);
				});
				
				disabledTagEditorUl.insertAfter(tagEditorUl.hide());
			},
			
			/**
			 * enabled
			 */
			enabled : function() {
				tagEditor.prop('disabled', false);
				var tagEditorUl = tagEditor.next('ul.tag-editor');
				tagEditorUl.find('li:has(.active)').remove();
				tagEditorUl.show().next('.tagEditorDisabled').remove();
			}
		};
		
		// 查詢自動完成標籤列表
		$.ajax({
			type: 'GET',
			url: bcs.bcsContextPath + "/edit/getAutocompleteContentFlagList?contentType=" + contentType,
		}).success(function(response){
			console.info('response', response);
			
			for (var i = 0, size = response.length; i < size; i++) {
				autocompleteList.push(response[i]);
			}
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		return contentFlagComponent;
	};
	
	/**
	 * 跳脫字串中的 html tag
	 * 
	 * @param str
	 * @returns
	 */
	$.BCS.escapeHtml = function(str) {
	    return str
	        .replace(/&/g, "&amp;")
	        .replace(/</g, "&lt;")
	        .replace(/>/g, "&gt;")
	        .replace(/"/g, "&quot;")
	        .replace(/'/g, "&#039;")
	        .replace(/\//g, "&#x2F;")
	};
	
	/**
	 * 將字串中的換行字元取代為 <br />
	 * 
	 * @param str
	 * @returns
	 */
	$.BCS.newLineToBrTag = function(str) {
		return str.replace(/(?:\r\n|\r|\n)/g, '<br/>');
	}
	
	$.urlParam = function(name){
		var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
		if(results == null){
			return null;
		}
		else{
			return results[1] || 0;
		}
	};

	Number.prototype.format = function(n, x) {
	    var re = '\\d(?=(\\d{' + (x || 3) + '})+' + (n > 0 ? '\\.' : '$') + ')';
	    return this.toFixed(Math.max(0, ~~n)).replace(new RegExp(re, 'g'), '$&,');
	};
	
	$.formatDate = function(date){
		return moment(date).format("YYYY/MM/DD");
	}
	
	$.formatTime = function(date){
		var momentDate = moment(date);
		return momentDate.format("YYYY/MM/DD") + "<br/>" + momentDate.hour() + ":" + momentDate.minute() + ":" + momentDate.second();
	}
	
	// Menu Setting
	$('.LySub .subMenu').css('display','none');
	$('.ExSelected').closest('.subMenu').css('display','');
	var menuClickFunc = function(){
		$(this).siblings().css('display','');
	};
	var liLinkEventFunc = function(target){
		target.find('a').first().click(menuClickFunc);
		target.find('a').first().removeAttr('href');
	}
	liLinkEventFunc($('.LySub li').first());
	var LySubLi = $('.LySub li').first().siblings();
	$.each(LySubLi, function(i, o){
		liLinkEventFunc($(o));
	});
	
	// link href Change
	var linkList = $('a');
	$.each(linkList, function(i, o){
		var notChangeHref = $(o).attr('notChangeHref');
		if(!notChangeHref){
			var hrefVal = $(o).attr('href');
			if(hrefVal && hrefVal != ""){
				var paramMarkIndex = hrefVal.indexOf('?');
				
				if (paramMarkIndex != -1) {
					hrefVal = hrefVal.slice(0, paramMarkIndex + 1) + ("&") + hrefVal.slice(paramMarkIndex + 1);
					$(o).attr('href', hrefVal);
				} else {
					$(o).attr('href', hrefVal);
				}
			}
		}
	});
	
	/**
	 * Setting Header Link
	 */
	var headerLinks = $('.MdGHD03Util a');

	$(headerLinks[0]).removeAttr('href');
	
	/**
	 * 
	 */
	$.FailResponse = function(response){
		var str = "";
		if(response && response.status == 501){
			str = "\n\n[" + response.responseText + "]";
		}
		
		alert('錯誤發生 請找開發人員協助' + str);
		$('.LyMain').unblock();
	}
	
	$.FailAlertStr = function(errorStr){
		var str = "";
		if(errorStr){
			str = "\n\n[" + errorStr + "]";
		}
		
		alert('錯誤發生 請找開發人員協助' + str);
	}
	
	$.BCS.blockMsgRead = { "message" : "讀取資料中...."};
	$.BCS.blockMsgUpload = { "message" : "上傳資料中...."};
	$.BCS.blockMsgSave = { "message" : "儲存資料中...."};
	
	$.BCS.formatNumber = function(number, size){

		if(number){
			var fix = size;
			if(size == 0){
				fix = 2;
			}
			var str = number.toFixed(fix).replace(/(\d)(?=(\d{3})+\.)/g, '$1,');
			
			if(size == 0){
				var sub = str.indexOf('.');
				str = str.substring(0, sub);
			}
			
			return str;
		}
		return number;
	}
	
	$(function() {
		$('textarea').each(function(id, obj) {if ($(obj).attr('placeholder') == $(obj).text()) {$(obj).text($(obj).attr('placeholder'))}});
	})
});