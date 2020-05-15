/**
 * 
 */
$(function(){
	// 每個richType的連結座標(暫時這樣用)
	var framesTypePointXY = [
 			[{startX : 0, startY : 0, endX : 1040, endY : 1040}], 
 			[{startX : 0, startY : 0, endX : 520, endY : 1040}, {startX : 520, startY : 0, endX : 1040, endY : 1040}], 
 			[{startX : 0, startY : 0, endX : 1040, endY : 520}, {startX : 0, startY : 520, endX : 1040, endY : 1040}], 
 			[{startX : 0, startY : 0, endX : 1040, endY : 346}, {startX : 0, startY : 347, endX :1040, endY : 693},
 			 {startX : 0, startY : 694, endX : 1040, endY : 1040}],
 			[{startX : 0, startY : 0, endX : 520, endY : 520}, {startX : 520, startY : 0, endX : 1040, endY : 520},
 			 {startX : 0, startY : 520, endX : 520, endY : 1040}, {startX : 520, startY : 520, endX : 1040, endY : 1040}],
 			[{startX : 0, startY : 0, endX : 1040, endY : 520}, {startX : 0, startY : 520, endX : 520, endY : 1040},
 			 {startX : 520, startY : 520, endX : 1040, endY : 1040}],
 			[{startX : 0, startY : 0, endX : 1040, endY : 520}, {startX : 0, startY : 520, endX : 1040, endY : 780},
 			 {startX : 0, startY : 780, endX : 1040, endY : 1040}],
 			[{startX : 0, startY : 0, endX : 346, endY : 520}, {startX : 347, startY : 0, endX : 693, endY : 520}, 
 			 {startX : 694, startY : 0, endX : 1040, endY : 520}, {startX : 0, startY : 520, endX : 346, endY : 1040},
 			 {startX : 347, startY : 520, endX : 693, endY : 1040}, {startX : 694, startY : 520, endX : 1040, endY : 1040}]
 	];

	/**
	 * 紀錄 最後按鈕
	 */
	var btnTarget = "";
	
	// 表單驗證
	var validator = $('#ContentValidateForm').validate({
		rules : {
			
			// 訊息標題
			'richMsgTitle' : {
				required : {
			        param: true,
			        depends: function(element) {
						if(btnTarget == "btn_save"){
							return true;
						}
						return false;
			        }
				},
				maxlength : {
			        param: 100
				}
			},
	
			// 主要圖片
			'titleImage' : {
				required : '.imgId:blank'
			}
		}
	});
	
	var actionTypeClick = function(){

		var actionType = $(this).val();
		console.info('actionType', actionType);

		var richMsgUrlPageTr = $(this).closest(".richMsgUrlPageTr");
		
		if(actionType == "sendMessage"){
			richMsgUrlPageTr.find('.sendMessageTd').css('display', '');
			richMsgUrlPageTr.find('.sendMessageTd input').addClass('richMsgUrl');
			richMsgUrlPageTr.find('.webTd').css('display', 'none');
			richMsgUrlPageTr.find('.webTd .MdFRM01Input input').removeClass();
			richMsgUrlPageTr.next().find('.linkInput').css('display', 'none');
		}
		else{
			richMsgUrlPageTr.find('.sendMessageTd').css('display', 'none');
			richMsgUrlPageTr.find('.sendMessageTd .MdFRM01Input input').removeClass();
			richMsgUrlPageTr.find('.webTd').css('display', '');
			richMsgUrlPageTr.find('.webTd .MdFRM01Input input').addClass('richMsgUrl');
			richMsgUrlPageTr.next().find('.linkInput').css('display', '');
		}
	}
	
	var setActionTypeRadioEvent = function(){

		$('.actionType').click(actionTypeClick);
	}
	
	/**
	 * 初始化註記欄位為標籤元件
	 */ 
	var buildLinkTagContentFlag = function(element) {
		return $.BCS.contentFlagComponent(element, 'LINK', {
			placeholder : '請輸入註記'
		});
	};
	
	var richMsgUrlPageTemplate = {};
	var richMsgUrlTxtTemplate = {};
	var urlTrTemplate = {};
	var initTemplate = function(){
		richMsgUrlPageTemplate = $(".richMsgUrlPageTr").clone();
		richMsgUrlTxtTemplate = $(".richMsgUrlTxtTr").clone();
		urlTrTemplate = $(".urlDialogTr").clone();
		
		$(".richMsgUrlPageTr").remove();
		$(".richMsgUrlTxtTr").remove();
		$(".urlDialogTr").remove();
		
		$('#richMsgTitle').val("");
		$('.imgId').val("");
    	$('.mdFRM03Img').find('img').attr('src', "");
		
		console.info('richMsgUrlPageTemplate', richMsgUrlPageTemplate);
		console.info('richMsgUrlTxtTemplate', richMsgUrlTxtTemplate);
		console.info('urlTrTemplate', urlTrTemplate);
		
		var templateFrameType = $("input[name='templateFrameType']");
		templateFrameType[0].checked = true;;
		$("input[name='templateFrameType']:checked").trigger("click");
		
		$('#customizeTypeBtn').hide();
		$('#removeUrl').css({"margin-left": "3px"});
		$('#addUrl').css({"margin-left": "3px"});
		$('#customizeFrameTypeLimit').css({"width": "520px", "border": "1px solid", "position": "relative"});
		$('#savePosition').css({"float": "right", "margin-top": "10px"});
	};
	
	var richId = "";
	var richType = "";
	var actionType = "";
	var getDataByRichId = function() {
		richId = $.urlParam("richId"); //從列表頁導過來的參數
		actionType = $.urlParam("actionType"); //從列表頁導過來的參數
		
		if (richId != null && richId != "") {
			$.ajax({
                type: 'GET',
                url: bcs.bcsContextPath + "/edit/getRichMsg/" + richId,
    		}).success(function(response){
				var valueObj = response[richId];
				console.info('valueObj', valueObj);
				
				richType = valueObj[0];
				$.each($('input[name="templateFrameType"]'), function(i, v) {
					if (v.value == richType) {
						v.checked = true;
					}
				});
				
				$('#richMsgTitle').val(valueObj[1]);
				$('.imgId').val(valueObj[2]);
				$('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + valueObj[2]);

				var urls = [""];
				if(valueObj[3]){
					urls = valueObj[3].split(",");
				}
				var actionTypeList = ["web"];
				if(valueObj[14]){
					actionTypeList = valueObj[14].split(",");
				}
				var linkTitles = [""];
				if(valueObj[4]){
					linkTitles = valueObj[4].split(",");
				}
				
				linkNumbers = urls.length;
				
				changeRichTypeImg(richType); //變更type圖示
				if (richType == "09") {
					$('#customizeTypeBtn').show();
				} else {
					frameTypePointXY = framesTypePointXY[Number(richType) - 1];
				}
				generateRichMsgUrl();

				var richMsgUrlPageTrs = $('.richMsgUrlPageTr');
				for(var i=0; i < linkNumbers; i++) {
					var actionTypes = $(richMsgUrlPageTrs[i]).find('.actionType');
					$.each(actionTypes, function(j, o){
						if($(o).val() == actionTypeList[i]){
							$(o).click();
						}
					});
				}
				
				originalImgHeight = valueObj[10];
				originalImgWidth = valueObj[11];
				
				var richMsgUrls = $('.richMsgUrl');
				var richMsgUrlsTxt = $('.richMsgLinkTxt');
				var richMsgUrlTxtTr = $('.richMsgUrlTxtTr');
				var multiStartX = valueObj[6].split(",");
				var multiStartY = valueObj[7].split(",");
				var multiEndX = valueObj[8].split(",");
				var multiEndY = valueObj[9].split(",");
				var linkIdList = valueObj[12].split(",");
				for (var i=0; i<linkNumbers; i++) {
					if(actionTypeList[i] == "sendMessage"){
						richMsgUrls[i].value = linkIdList[i];
					}
					else{
						richMsgUrls[i].value = urls[i];
					}
					richMsgUrlsTxt[i].value = linkTitles[i];
					
					// 註記標籤元件
					var linkTagContentFlag = richMsgUrlTxtTr.eq(i).data('linkTagContentFlag');
					linkTagContentFlag.findContentFlagList(linkIdList[i]);
					
					if (richType == "09") {
						//設定draggable
						var letter = String.fromCharCode(65 + i);
						setDraggable(letter);
					}
				}
				
				//觸發輸入文字計數
				$('#richMsgTitle').trigger("keyup");
				$('.richMsgLinkTxt').trigger("keyup");
				
				//設定每個draggable的座標與大小
				$.each($('.urlDraggable'), function(i, v) {
					var width = (multiEndX[i] - multiStartX[i]) / 2;
					var heigth = (multiEndY[i] - multiStartY[i]) / 2;
					$(this).css({"width": width, "height": heigth, "top": (multiStartY[i] / 2)+"px", "left": (multiStartX[i] / 2)+"px"})
				});
				
				//設定dialog的圖片大小
				var width = Number(valueObj[11]);
				var height = Number(valueObj[10]);
    			setImgHeightAndWidth(valueObj[2], width, height);
    		}).fail(function(response){
    			console.info(response);
    			$.FailResponse(response);
    		}).done(function(){
    		});
		} else {
			actionType = "Create";
		}
	}
	
	//點擊圖文訊息類別後變更設定連結的圖示
	var linkNumbers = 0; //連結數
	var frameTypePointXY;
	$("input[name='templateFrameType']").click(function(e) {
		var selectedRichType = e.currentTarget.value; //選擇的連結類型
		
		$('#customizeTypeBtn').hide();
		$('.urlDraggable').remove();
		
		if (richType == selectedRichType) {
			getDataByRichId(); //點擊的type與導頁過來的一樣，則取回原先資料
			return;
		}
		
		changeRichTypeImg(selectedRichType);
		
		switch (selectedRichType) {
			case '01':
				linkNumbers = 1;
				break;
			case '02':
				linkNumbers = 2;
				break;
			case '03':
				linkNumbers = 2;
				break;
			case '04':
				linkNumbers = 3;
				break;
			case '05':
				linkNumbers = 4;
				break;
			case '06':
				linkNumbers = 3;
				break;
			case '07':
				linkNumbers = 3;
				break;
			case '08':
				linkNumbers = 6;
				break;
			case '09':
				linkNumbers = 0;
				break;
			default:
				break;
		}
		
		$(".richMsgUrlPageTr").remove();
		$(".richMsgUrlTxtTr").remove();
		if (selectedRichType != "09") {
			frameTypePointXY = framesTypePointXY[Number(selectedRichType) - 1];
			generateRichMsgUrl();
		} else {
			$('#customizeTypeBtn').show();
			$('#addUrl').trigger("click");
		}
	});
	
	var radios = $("input[name='templateFrameType']");
	$.each(radios, function(i, o){
		$(o).closest('.typeMenu').find('img').click(function(){
			$(o).click();
		});
	})
	
	//變更點擊的type圖示
	var changeRichTypeImg = function(richType) {
		var imgHtml = "連結<img src='" +  bcs.bcsResourcePath + "/images/type_richmenu_" + richType + ".png' alt='Type" + Number(richType) + "'>";
		$("#richMsgUrlTh").html(imgHtml);
	}
	
	var totalUrlCount = 0;
	//動態產生輸入url的tr
	var generateRichMsgUrl = function() {
		$("#richMsgUrlTh").prop("rowspan", linkNumbers * 2 + 1);
		
		var validateNameSet = [];
		var appendHtml = "";
		var existUrlNumbers = $('.richMsgUrlPageTr').length; //畫面已存在的連結數
		for (var i=existUrlNumbers; i<linkNumbers; i++) {
			totalUrlCount ++;
			
			var richMsgUrlPage = richMsgUrlPageTemplate;
			var richMsgUrlTxt = richMsgUrlTxtTemplate;
			
			var letter = String.fromCharCode(65 + i);
			richMsgUrlPage.find(".typeSideTxt").html(letter);

			var actionTarget = 'ActionType' + totalUrlCount;
			richMsgUrlPage.find(".actionType").attr('name', actionTarget);

			var nameTarget = 'RichMsg' + totalUrlCount;
			richMsgUrlPage.find('.richMsgUrl').attr('name', nameTarget);
			validateNameSet.push(nameTarget);
			
			appendHtml += '<tr class="richMsgUrlPageTr">' + richMsgUrlPage.html() + '</tr>';
			appendHtml += '<tr class="richMsgUrlTxtTr">' + richMsgUrlTxt.html() + '</tr>';
		}
		
		var jqAppendHtml = $(appendHtml);
		
		// 初始化註記標籤元件並儲存到 tr.richMsgUrlTxtTr
		jqAppendHtml.filter('.richMsgUrlTxtTr').each(function(index, element) {
			var jqElement = $(element);
			jqElement.data('linkTagContentFlag', buildLinkTagContentFlag(jqElement.find('.tagInput')));
		});
		
		$("#richMsgTable").append(jqAppendHtml);
		richMsgUrlTxtKeyupEvent();
		setUrlBtnEvent();
		setActionTypeRadioEvent();
		
		$.each(validateNameSet, function(i, o){

			$('#ContentValidateForm').find('[name="' + o + '"]').rules("add", {
				required : {
			        param: true,
			        depends: function(element) {
						if(btnTarget == "btn_save"){
							return true;
						}
						return false;
			        }
				}
			});
		})
	};
	
	//設定連結文字的input事件
	var richMsgUrlTxtKeyupEvent = function() {
		$('#richMsgTitle').keyup(function() {
			var txtLength = $(this).val().length;
			var richMsgUrlTxtTr = $(this).closest("tr");
			richMsgUrlTxtTr.find(".MdTxtInputCount").html(txtLength + "/100");
		});
		
		$(".richMsgLinkTxt").keyup(function(e) {
			var txtLength = $(this).val().length;
			var richMsgUrlTxtTr = $(this).closest("tr");
			richMsgUrlTxtTr.find(".MdTxtInputCount").html(txtLength + "/100");
		});
	};
	
	var clickedUrlInput;
	var clickedUrlTitle;
	//設定showDialog的按鈕
	var setUrlBtnEvent = function() {
		$(".showDialogBtn").click(function() {
			$('#urlDialog').dialog('open');
			$('#urlSelection').css('display','');
			clickedUrlInput = $(this).closest('tr').find('.richMsgUrl'); //點擊showDialogBtn的input
			clickedUrlTitle = $(this).closest('tr').next().find('.richMsgLinkTxt'); //點擊showDialogBtn的input
		});
	}
	
	//取得所有連結
	var getUrlList = function() {
		$.ajax({
            type: 'GET',
            url: bcs.bcsContextPath + "/edit/getLinkUrlList",
		}).success(function(response){
			var appendHtml = "";
			for (var i in response) {
				var urlTr = urlTrTemplate;
				urlTr.find('.urls').html(response[i].linkTitle);
				urlTr.find('.urls').attr('url', response[i].linkUrl);
				
				appendHtml += '<tr class="urlDialogTr" style="cursor: pointer">' + urlTr.html() + '</tr>';
			}
			
			$('#urlDialogTable').append(appendHtml);
			setUrlDialogTrClickEvent();
			setDialogTableStyle();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	}
	
	//設定Dialog顯示列的點擊事件
	var setUrlDialogTrClickEvent = function() {
		$('.urlDialogTr').click(function(e) {
			$('#urlDialog').dialog('close');
			$('#urlSelection').css('display','none');
			clickedUrlInput[0].value = $(this).find('.urls').attr('url');
			clickedUrlTitle[0].value = $(this).find('.urls').html();
		});
	};
	
	//設定table的css
	var setDialogTableStyle = function() {
		var tableStyle = {
			"margin": "0 auto",
			"font-size": "1.2em",
			"margin-bottom": "15px",
			"width": "100%",
	 		"background": "#fff",
	   		"border-collapse": "collapse",
	   		"border-spacing": "0"
		};
		
		var thStyle = {
			"background": "#8b8b8b",
	 		"color": "#fff",
			"font-weight": "bold",
			"text-align": "center",
		 	"padding": "12px 30px",
		  	"padding-left": "42px"
		};
		
		var tdStyle = {
			"cursor": "pointer",
			"text-align": "center",
		  	"padding": "15px 10px",
		  	"border-bottom": "1px solid #e5e5e5v"
		};
		
		$('#urlDialogTable').css(tableStyle);
		$('#urlDialogTable th').css(thStyle);
		$('#urlDialogTable td').css(tdStyle);
	}
	
	var originalImgHeight = 0;
	var originalImgWidth = 0;
	var reader = new FileReader();
	var img = new Image();
	//上傳圖片
	$("#titleImage").on("change", function(e) {
		var input = e.currentTarget;
    	if (input.files && input.files[0]) {
    		reader.readAsDataURL(input.files[0]);
	    		reader.onload = function(ev){
	            	var txt = ev.target.result;
	                var img = new Image();
	                img.src = txt;
	                img.onload = function() {
		                if (img.width >= 1041 || img.height >= 2081) {
	                		alert("尺寸不正確 " + img.width + "*" + img.height);
	                		return false;
		                } else {
		                	if(input.files[0].size < 1048576) {
		    		    		var fileName = input.files[0].name;
		    		    		console.info("fileName : " + fileName);
		    		    		var form_data = new FormData();
		    		    		
		    		    		form_data.append("filePart",input.files[0]);
		    		
		    		    		$('.LyMain').block($.BCS.blockMsgUpload);
		    		    		$.ajax({
		    		                type: 'POST',
		    		                url: bcs.bcsContextPath + "/edit/createResource?resourceType=IMAGE",
		    		                cache: false,
		    		                contentType: false,
		    		                processData: false,
		    		                data: form_data
		    		    		}).success(function(response){
		    		            	console.info(response);
		    		            	alert("上傳成功!");
		    		            	$('.imgId').val(response.resourceId);
		    		            	$('.mdFRM03Img').find('img').attr('src', bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
		    		            	originalImgWidth = response.resourceWidth;
		    		            	originalImgHeight = response.resourceHeight;
		    		            	setImgHeightAndWidth(response.resourceId, originalImgWidth, originalImgHeight)
		    		    		}).fail(function(response){
		    		    			console.info(response);
		    		    			$.FailResponse(response);
		    		    			$('.LyMain').unblock();
		    		    		}).done(function(){
		    		    			$('.LyMain').unblock();
		    		    		});
		    	    		} else {
		    	    			alert("圖片大小不可大於 1MB！");
		    	    		}
		                }
	                }
	        } 
    	} 
	});
	
	//設定dialog的圖片長與寬
	var setImgHeightAndWidth = function(imgId, width, height) {
		//圖片大小除以2方便在畫面上的顯示
		width = width / 2;
		height = height / 2;
		$('#customizeFrameTypeLimit').css({
			"width": width, 
			"height": height
		});
		
		$('#customizeImg').css({
			"background-image": "url(" + bcs.bcsContextPath + '/getResource/IMAGE/' + imgId + ")", 
			"background-size": "contain", //圖大於div，fit大小
//			"background-size": "cover", //圖小於div，fit大小
			"background-repeat": "no-repeat",
			"opacity": "0.4", //透明度
			"width": width, 
			"height": height
		});
		
		$('.urlDraggable').resizable({
			handles: "all", //所有邊都可以縮放
			maxHeight: height - 2, //有邊框的關係，所以再減2
			maxWidth: 518,
			minHeight: 30,
			minWidth: 30
	    });
		
		$('.heightScale').remove();
		var appendHtml = "";
		for (var i=50; i<=height; i=i+50) {
			appendHtml += "<label class='heightScale' style='position: absolute; left: 520px; top: " + (i-10) + "px'>-" + (i*2) + "</label>";
		}
		appendHtml += "<label class='heightScale' style='position: absolute; left: 520px; top: " + (height-10) + "px'>-" + (height*2) + "</label>";
		$('#customizeFrameTypeLimit').append(appendHtml);
	}
	
	//將座標資訊轉為逗點分隔
	var getMutliPoint = function(pointsArray) {
		var mulitStartX = pointsArray[0].startX;
		var mulitStartY = pointsArray[0].startY;
		var mulitEndX = pointsArray[0].endX;
		var mulitEndY = pointsArray[0].endY;
		for (var i=1; i<pointsArray.length; i++) {
			mulitStartX = mulitStartX + "," + pointsArray[i].startX;
			mulitStartY = mulitStartY + "," + pointsArray[i].startY;
			mulitEndX = mulitEndX + "," + pointsArray[i].endX;
			mulitEndY = mulitEndY + "," + pointsArray[i].endY;
		}
		var multiPoint = [mulitStartX, mulitStartY, mulitEndX, mulitEndY];
		return multiPoint;
	}
	
	//將產生圖片預覽的資料轉換成陣列
	var parseDataToArray = function() {
		var previewRichMsgImageData = [];
		
		previewRichMsgImageData.push($("input[name='templateFrameType']:checked").val());
		previewRichMsgImageData.push($.BCS.escapeHtml($('#richMsgTitle').val()));
		previewRichMsgImageData.push($('.imgId').val());
		
		var richMsgUrls = "";
		$.each($('.richMsgUrlPageTr').find('.richMsgUrl'), function(i ,v) {
			if (richMsgUrls == "") {
				richMsgUrls = $.BCS.escapeHtml($(this).val());
			} else {
				richMsgUrls = richMsgUrls + "," + $.BCS.escapeHtml($(this).val());
			}
		});
		previewRichMsgImageData.push(richMsgUrls);
		
		var richMsgLinkTitles = "";
		$.each($('.richMsgUrlTxtTr').find('.richMsgLinkTxt'), function(i ,v) {
			var title = $(this).val();
			if(!title){
				title = "-";
			}
			
			if (richMsgLinkTitles == "") {
				richMsgLinkTitles = $.BCS.escapeHtml(title);
			} else {
				richMsgLinkTitles = richMsgLinkTitles + "," + $.BCS.escapeHtml(title);
			}
		});
		previewRichMsgImageData.push(richMsgLinkTitles);
		previewRichMsgImageData.push("");
		
		var multiPoint = [];
		//選擇自訂框架
		if ($("input[name='templateFrameType']:checked").val() == "09") {
			var draggablePositions = getUrlDraggablePosition();
			multiPoint = getMutliPoint(draggablePositions)
		} else {
			multiPoint = getMutliPoint(frameTypePointXY)
		}
		previewRichMsgImageData.push(multiPoint[0]);
		previewRichMsgImageData.push(multiPoint[1]);
		previewRichMsgImageData.push(multiPoint[2]);
		previewRichMsgImageData.push(multiPoint[3]);
		
		//圖片高
		previewRichMsgImageData.push(originalImgHeight);
		//圖片寬
		previewRichMsgImageData.push(originalImgWidth);
		
		return previewRichMsgImageData;
	}
	
	var validateInput = function(){
		var richMsgUrls = $('.richMsgUrl');
		for (var i=0; i<linkNumbers; i++) {
			var actionType = $(richMsgUrls[i]).closest('.richMsgUrlPageTr').find('.actionType:checked').val();
			console.info('actionType', actionType);
			
			if(actionType == 'sendMessage'){
				if(!richMsgUrls[i].value){
					alert("必須輸入文字訊息！");
					return false;
				}
			}
			else{
			
				if (!richMsgUrls[i].value.lastIndexOf('http://', 0)==0 
						&& !richMsgUrls[i].value.lastIndexOf('https://', 0)==0
						&& !richMsgUrls[i].value.lastIndexOf('BcsPage:', 0)==0) {
					alert("URL必須包含http或是BcsPage字樣！");
					return false;
				}
			}
		}
		return true;
	}
	
	//圖片預覽
	$('.previewImg').click(function() {
		btnTarget = "btn_query";
		if (!validator.form()) {
			return;
		}
		
		if ($('.imgId').val() == "") {
			alert("請先上傳背景圖像！");
			return;
		}
		
		if(!validateInput()){
			return;
		}
		
		var previewDialog = $('#previewDialog');
		previewDialog.html('<div></div>');
		
		$.BCS.newPreviewDialog("圖文訊息圖片預覽畫面", previewDialog);
		
		var previewImgData = parseDataToArray();
		$.BCS.previewRichMsgImage(previewDialog.find('div'), previewImgData);
		
		previewDialog.dialog('open');
	});
	
	//文字預覽
	$('.previewTxt').click(function() {
		if(!validateInput()){
			return;
		}
		
		var previewDialog = $('#previewDialog');
		previewDialog.html('<div></div>');

		$.BCS.newPreviewDialog("圖文訊息文字預覽畫面", previewDialog);
		
		var previewImgData = parseDataToArray();
		$.BCS.previewRichMsgText($('#previewDialog').find('div'), previewImgData);
		
		previewDialog.dialog('open');
	});
	
	$('#save').click(function() {

		btnTarget = "btn_save";
		if (!validator.form()) {
			return;
		}
		
		if ($("#richMsgTitle").val() == "") {
			alert("請輸入訊息標題！");
			return;
		}
		
		if ($('.imgId').val() == "") {
			alert("請上傳背景圖像！");
			return;
		}
		
		var richMsgUrls = $('.richMsgUrl');
		var validate = true;
		$.each(richMsgUrls, function(i , o){
			if(validate){
				if (!o.value) {
					alert("部份資料尚未輸入！");
					validate = false;
					return
				}

				var actionType = $(o).closest('.richMsgUrlPageTr').find('.actionType:checked').val();
				console.info('actionType', actionType);
				
				if(actionType == 'sendMessage'){
					if(!o.value){
						alert("必須輸入文字訊息！");
						validate = false;
						return
					}
				}
				else{
					if (!o.value.lastIndexOf('http://', 0) == 0 
							&& !o.value.lastIndexOf('https://', 0) == 0
							&& !o.value.lastIndexOf('BcsPage:', 0) == 0) {
						alert("URL必須包含http或是BcsPage字樣！");
						validate = false;
						return
					}
				}
			}
		});
		if(!validate){
			return;
		}
		
		var saveConfirm = confirm("請確認是否儲存");
		if (!saveConfirm) return; //點擊取消
		
		var richMsgImgUrls = [];
		
		var richMsgUrls = $('.richMsgUrlPageTr').find('.richMsgUrl');
		var richMsgUrlTxtTr = $('.richMsgUrlTxtTr');
		var richMsgLinkTitles = richMsgUrlTxtTr.find('.richMsgLinkTxt');
		var richMsgLetters = $('.typeSideTxt');
		
		//選擇自訂框架
		if ($("input[name='templateFrameType']:checked").val() == "09") {
			var draggablePositions = getUrlDraggablePosition();
			for (var i in draggablePositions) {
				if (draggablePositions[i].endX > originalImgWidth || draggablePositions[i].endY > originalImgHeight
						|| draggablePositions[i].startX < 0 || draggablePositions[i].startY < 0) {
					alert("自訂連結區塊超出圖片範圍，請再次確認");
					return;
				}
				
				richMsgImgUrls.push({
					richDetailLetter : richMsgLetters[i].innerText,
					startPointX : draggablePositions[i].startX,
					startPointY : draggablePositions[i].startY,
					endPointX : draggablePositions[i].endX,
					endPointY : draggablePositions[i].endY,
					linkUrl : richMsgUrls[i].value,
					linkTitle : richMsgLinkTitles[i].value,
					linkTagList : richMsgUrlTxtTr.eq(i).data('linkTagContentFlag').getContentFlagList(),
					actionType : $(richMsgUrls[i]).closest('.richMsgUrlPageTr').find('.actionType:checked').val()
				});
			}
		} else {
			$.each(richMsgUrls, function(i, v) {
				richMsgImgUrls.push({
					richDetailLetter : richMsgLetters[i].innerText,
					startPointX : frameTypePointXY[i].startX,
					startPointY : frameTypePointXY[i].startY,
					endPointX : frameTypePointXY[i].endX,
					endPointY : frameTypePointXY[i].endY,
					linkUrl : richMsgUrls[i].value,
					linkTitle : richMsgLinkTitles[i].value,
					linkTagList : richMsgUrlTxtTr.eq(i).data('linkTagContentFlag').getContentFlagList(),
					actionType : $(richMsgUrls[i]).closest('.richMsgUrlPageTr').find('.actionType:checked').val()
				});
			});
		}
		
		postData = {
			richType : $('input[name="templateFrameType"]:checked').val(),
			richTitle : $('#richMsgTitle').val(),
			richImageId : $('.imgId').val(),
			richMsgImgUrls : richMsgImgUrls
		}
		
		$('.LyMain').block($.BCS.blockMsgSave);
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/edit/createRichMsg?actionType=' + actionType + '&richId=' + richId,
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			if (actionType == "Edit") {
				alert("儲存圖文訊息成功！");
			} else {
				alert("建立圖文訊息成功！");
			}
			window.location.replace(bcs.bcsContextPath + '/edit/richMsgListPage');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	});
	
	//取得各個連結區塊的座標
	var getUrlDraggablePosition = function() {
		var draggablePositions = [];
		$.each($(".urlDraggable"), function() {
			//div長寬為原圖的一半520x520，因此儲存座標值是須x2
			var startX = Number($(this).css("left").replace("px", "").replace("auto", "0")) * 2;
			var startY = Number($(this).css("top").replace("px", "").replace("auto", "0")) * 2;
			var height = Number($(this).height()) * 2;
			var width = Number($(this).width()) * 2;
			
			var draggablePosition = {
					startX : startX,
					endX : startX + width,
					startY : startY,
					endY : startY + height
			}
			draggablePositions.push(draggablePosition);
		});
		return draggablePositions;
	}
	
	$('#urlDialog').dialog({
    	autoOpen: false, //初始化不會是open
    	resizable: false, //不可縮放
    	modal: true, //畫面遮罩
    	draggable: false, //不可拖曳
    	minWidth : 500,
    	position: { my: "top", at: "top", of: window  }
    });
	$('#customizeDialog').dialog({
		autoOpen: false, //初始化不會是open
		resizable: false, //不可縮放
		modal: true, //畫面遮罩
		draggable: false, //不可拖曳
		minWidth : 590,
    	position: { my: "top", at: "top", of: window  }
	});

	//增加自訂連結
	$('#addUrl').click(function() {
		linkNumbers++;
		generateRichMsgUrl();
		var letter = String.fromCharCode(64 + linkNumbers);
		setDraggable(letter);
	});
	
	//設定連結區塊
	var setDraggable = function(letter) {
		var urlDraggable = "<div class='urlDraggable' style='position: absolute; left: 0px; top: 0px; width: 50px; height: 50px; border: 1px solid'><p style='position: absolute; top: 50%; left: 50%; margin-top: -16px; margin-left: -7px; font-size:24px'>" + letter + "</p></div>"
		$('#customizeFrameTypeLimit').append(urlDraggable);
		
		$('.urlDraggable').last().draggable({
			containment: "#customizeFrameTypeLimit", 
			scroll: false //不會出現卷軸
		});
		
		var maxHeight = $('#customizeFrameTypeLimit').height() - 2; //有邊框的關係，所以再減2
		$('.urlDraggable').last().resizable({
			handles: "all", //所有邊都可以縮放
			maxHeight: maxHeight,
			maxWidth: 518,
			minHeight: 30,
			minWidth: 30
	    });
	}
	
	//刪除一個自訂連結
	$('#removeUrl').click(function() {
		if (linkNumbers == 1) {
			alert("至少要有一個連結！");
			return;
		}
		linkNumbers--;
		$('.richMsgUrlPageTr').last().remove();
		$('.richMsgUrlTxtTr').last().remove();
		$('.urlDraggable').last().remove();
	});
	
	//顯示連結拖曳的Dialog
	$('#showCustomizeDialog').click(function() {
		
		btnTarget = "showCustomizeDialog";
		if (!validator.form()) {
			return;
		}
		
		if ($('.imgId').val() == "") {
			alert("請上傳背景圖像！");
			return;
		}
		$('#customizeDialog').dialog('open');
	});
	
	//customizeDialog的確認鈕
	$('#savePosition').click(function() {
		var draggablePositions = getUrlDraggablePosition();
		for (var i in draggablePositions) {
			if (draggablePositions[i].endX > originalImgWidth || draggablePositions[i].endY > originalImgHeight
					|| draggablePositions[i].startX < 0 || draggablePositions[i].startY < 0) {
				alert("自訂連結區塊超出圖片範圍，請再次確認");
				return;
			}
		}
		
		$('#customizeDialog').dialog('close');
	});
	
	//取消鈕
	$('input[name="cancel"]').click(function() {

		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		window.location.replace(bcs.bcsContextPath + '/edit/richMsgListPage');
	});
	
	initTemplate();
	getDataByRichId();
	getUrlList();
});