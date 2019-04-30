/**
集點卡預覽、有效期限前提醒、取卡回饋點數 未完成
 * 
 */
$(function(){
	var couponSetPoint = {};
	var actionType = $.urlParam("actionType") || 'Create';
	console.info('actionType', actionType);
	var from = $.urlParam("from") || 'disable';
	console.info('from', from);
	var dateFormat = "YYYY-MM-DD HH:mm:ss";
	$('#optionLabelBlack1').css('color', 'black');
	$('#optionLabelBlack2').css('color', 'black');
	var customizedBackground = false;
	var MsgFrameTemplate = {};
	
	var CURRENT_COUPONCOUNT = 0;
	var COUPONCOUNT = 0;
	var currentCouponList = [];
	
	//選擇優惠券
	var initMsgTemplate = function(){
		console.info("initMsgTemplate");
		var MsgFrames = $('.MsgPlace .MsgFrame');
		
		$.each(MsgFrames, function(i, o){
			var MsgFrame = $(o).clone();
			MsgFrameTemplate[MsgFrame.attr('type')] = MsgFrame;
		});

		$('.MsgPlace .MsgFrame').remove();
		
		console.info('MsgFrameTemplate', MsgFrameTemplate);
	};

	initMsgTemplate();
	
	
	// ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 設定 Dialog Coupon Select 
	// ------------------------------------------------------------------------------------------------------------------------------------------------------
	var couponTrTemplate = {};


	$('#dialogCouponSelect').dialog({
	    autoOpen: false,
	    resizable: false, //不可縮放
	    modal: true, //畫面遮罩
	    draggable: false, //不可拖曳
	    minWidth : 750,
	    position: { my: "top", at: "top", of: window  }
	});

	// 建立 COUPON
	var createMsgFrameCOUPON = function(msgType, settingObj){
		console.info("createMsgFrameCOUPON");
		/**
		 * Coupon MsgFrame Setting
		 */
		if(settingObj){
			var valueObj = $.BCS.ResourceMap[settingObj.referenceId];
			var src = bcs.bcsContextPath + "/getResource/IMAGE/" + valueObj.couponListImageId;
			settingCouponSelectResult(settingObj.referenceId);
		}
		else{
			// Get Image
			var loadImage = $('#couponListTable').attr('loadImage');
			if(!loadImage){
				console.info('couponListTable loadImage');
				var couponImgTitle = $('#couponListTable .couponImgTitle img');
				$.each(couponImgTitle, function(i , o){
					var srcUrl = $(o).attr('srcUrl');
					$(o).attr('src', srcUrl);
				});
				$('#couponListTable').attr('loadImage', 'true');
			}
			
			$('#dialogCouponSelect').dialog('open');
		}
	}
	
	// 設定 Delete Message Content Event
	var deleteMsgContentEvent = function(){
		var couponSeleteId = $(this).closest('[id]').attr('id');
		$(this).closest('.MsgFrame').remove();
		var deletedCouponId = $(this).closest('.MsgFrame').find('.COUPON_ID').val();
		
		if(deletedCouponId){
			console.info('currentCouponList.indexOf(deletedCouponId)',currentCouponList.indexOf(deletedCouponId));
			currentCouponList.splice(findStringInArray(currentCouponList, deletedCouponId), 1);
		}
		console.log(currentCouponList);
		$('#' + couponSeleteId).find('.TypeMsgSolid').show();
		getCouponList();
	};
	

	$.BCS.createMsgFrame = function(msgType, settingObj){
		// Set Each Event
		if(msgType == "COUPON"){
			createMsgFrameCOUPON(msgType, settingObj);
		}
	}
	
	// ------------------------------------------------------------------------------------------------------------------------------------------------------
	// 設定 發送內容 選項 
    // ------------------------------------------------------------------------------------------------------------------------------------------------------
	var setAddMsgContentBtnEvent = function(){
		var MdBtns = $('.MdBtn');

		$.each(MdBtns, function(i, o){
			var inputBtn = $(o).find('input');
			
			inputBtn.click(function(){
				var msgType = inputBtn.attr('msgType');
				var couponSeleteId = $(this).closest('[id]').attr('id');
				sessionStorage.setItem('couponSeleteId', couponSeleteId);
				$.BCS.createMsgFrame(msgType);
			});
		});
	};
	
	// 設定 coupon 選擇
	var couponSelectEventFunc = function(){
		console.info("couponSelectEventFunc");
		var selectedCoupon = $(this);
	    var couponId = selectedCoupon.attr('couponId');
	    var tr = selectedCoupon.closest('tr');
	    var src = tr.find('.couponImgTitle').find('img').attr('src');
	    var couponDescription = tr.find('.couponDescription').text();
	    var couponTitle = tr.find('a').text();
	    
	    settingCouponSelectResult(couponId);
	    currentCouponList.push(couponId);
	    $('#dialogCouponSelect').dialog('close');
	    
	    getCouponList();
	};
	
	var settingCouponSelectResult = function(couponId){

	    var appendBody = MsgFrameTemplate["COUPON"].clone();
	    
	    $.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getContentCoupon?couponId=' + couponId
		}).success(function(response){
			appendBody.css('display', '');
		    
		    appendBody.find('.COUPON_ID').val(couponId);
		    appendBody.find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response.couponListImageId);
		    appendBody.find('.COUPON_DESCRIPTION').text(response.couponDescription);
		    appendBody.find('.mdCMN07HeadTtl01').text('優惠劵：' + response.couponTitle);
		    
		    appendBody.find('.MdBtn03Delete').click(deleteMsgContentEvent);
		    
		    var couponSeleteId = sessionStorage.getItem('couponSeleteId');
		    $('#' + couponSeleteId + ' .MsgPlace').append(appendBody);
		    $('#' + couponSeleteId + ' .TypeMsgSolid').hide();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};
	
	/**
	 * Get Coupon List
	 */
	var getCouponList = function(){
		console.log('getCouponList');
		console.log(currentCouponList);
		$.ajax({
		    type : "GET",
		    url : bcs.bcsContextPath + '/edit/getContentCouponList'
		}).success(function(response){
		    $('.couponTrTemplate').remove();
		    var coupons = response;

		    $.each(coupons, function(index, coupon) {
		        var couponTr = couponTrTemplate.clone(true);
				var rewardCardId = $.urlParam("rewardCardId");
				
				if(coupon.eventReferenceId===null || (coupon.eventReferenceId==rewardCardId && coupon.eventReference==='REWARD_CARD')){
					if(currentCouponList.indexOf(coupon.couponId)===-1){
						 	couponTr.find('.couponId').val(coupon.couponId);
					        couponTr.find('.couponTitle').text(coupon.couponTitle);
					        couponTr.find('.couponDescription').text(coupon.couponDescription);

					        couponTr.find('.couponImgTitle img').attr('couponId', coupon.couponId);
					        couponTr.find('.couponImgTitle img').attr('src', "../getResource/IMAGE/" + coupon.couponListImageId);
					        couponTr.find('.couponImgTitle img').attr('srcUrl', "../getResource/IMAGE/" + coupon.couponListImageId);
					        couponTr.find('.couponImgTitle img').click(couponSelectEventFunc); 
					        
					        couponTr.find('.couponImgTitle a').attr('couponId', coupon.couponId);
					        couponTr.find('.couponImgTitle a').click(couponSelectEventFunc); 
					        	        
					        couponTr.find('.couponCreateTime').text(moment(coupon.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
					        couponTr.find('.couponCreateUser').text(coupon.modifyUser);
					        
					        couponTr.find('.couponRemark').text(coupon.couponRemark?coupon.couponRemark:'無備註');
					        
					        $('#couponListTable').append(couponTr);
					}
			       
		        }
		    });
		}).fail(function(response){
		    console.info(response);
		    $.FailResponse(response);
		}).done(function(){
		});
	}
	
	var initCouponSetPoint = function(){
		console.info("initCouponSetPoint");
		couponSetPoint =  $('.couponSetPoint').clone(true);//取下 couponSet 格式
		$('.couponSetPoint').remove();//刪除 couponSet
	};
	
	initCouponSetPoint();
	
	// 表單驗證
	var validator = $('#formContentRewardCard').validate({
		rules : {
			
			// 集點卡主標題
			'rewardCardMainTitle' : {
				required : true,
				maxlength : 50
			},
			
			// 集點卡副標題
			'rewardCardSubTitle' : {
				required : true,
				maxlength : 50
			},
			
			// 集點卡列表顯示圖片
			'rewardCardListImageInput' : {
				required : '#rewardCardListImageId:blank'
			},
						
			// 使用效期
			'rewardCardStartUsingTime' : {
				required : true,
				dateYYYYMMDD : true
			},
			
			'rewardCardStartUsingTimeHour' : {
				required : true
			},
			
			'rewardCardStartUsingTimeMinute' : {
				required : true
			},
			
			'rewardCardEndUsingTime' : {
				required : true,
				dateYYYYMMDD : true,
				compareDate : {
					compareType : 'after',
					dateFormat : dateFormat,
					getThisDateStringFunction : function() {
						var yearMonthDay = $('#rewardCardEndUsingTime').val();
						var hour = $('#rewardCardEndUsingTimeHour').val();
						var minute = $('#rewardCardEndUsingTimeMinute').val();		
						return yearMonthDay + ' ' + hour + ':' + minute + ':00';
					},
					getAnotherDateStringFunction : function() {
						var yearMonthDay = $('#couponStartUsingTime').val();
						var hour = $('#couponStartUsingTimeHour').val();
						var minute = $('#couponStartUsingTimeMinute').val();		
						return yearMonthDay + ' ' + hour + ':' + minute + ':00';
					},
					thisDateName : '使用效期結束日期',
					anotherDateName : '使用效期開始日期'
				}
			},

			'rewardCardEndUsingTimeHour' : {
				required : true
			},
			
			'rewardCardEndUsingTimeMinute' : {
				required : true
			},
			
			// 領用期間
			'rewardCardStartGetTime' : {
				required : '#rewardCardEndGetTime:filled',
				dateYYYYMMDD : true
			},
			
			'rewardCardEndGetTime' : {
				required : true,
				dateYYYYMMDD : true,
				compareDate : {
					compareType : 'after',
					dateFormat : dateFormat,
					getThisDateStringFunction : function() {
						var yearMonthDay = $('#rewardCardEndGetTime').val();
						var hour = $('#rewardCardEndGetTimeHour').val();
						var minute = $('#rewardCardEndGetTimeMinute').val();		
						return yearMonthDay + ' ' + hour + ':' + minute + ':00';
					},
					getAnotherDateStringFunction : function() {
						var yearMonthDay = $('#rewardCardStartGetTime').val();
						var hour = $('#rewardCardStartGetTimeHour').val();
						var minute = $('#rewardCardStartGetTimeMinute').val();		
						return yearMonthDay + ' ' + hour + ':' + minute + ':00';
					},
					thisDateName : '領取期間結束日期',
					anotherDateName : '領取期間開始日期'
				}
			},
			
			// 使用說明
			'rewardCardUseDescription' : {
				required : true,
				maxlength : 500
			},
			
			// 注意事項
			'rewardCardDescription' : {
				required : true,
				maxlength : 500
			}
			

		}
	});
	
	// 綁訂計算字數函式到輸入框
	var bindCountTextFunctionToInput = function() {		
		$("#rewardCardUseDescription").keyup(function(e) {
			var txtLength = $(this).val().length;
			var tr = $(this).closest("tr");
			var inputCount = tr.find(".MdTxtNotice01");
			var countText = inputCount.text();
			inputCount.text(countText.replace(/\d+\//, txtLength + '/'));
		});
		
		$("#rewardCardDescription").keyup(function(e) {
			var txtLength = $(this).val().length;
			var tr = $(this).closest("tr");
			var inputCount = tr.find(".MdTxtNotice01");
			var countText = inputCount.text();
			inputCount.text(countText.replace(/\d+\//, txtLength + '/'));
		});
	};
	bindCountTextFunctionToInput();
	
	/**
	 * 從欄位取得日期(型態是 Moment.js 的 date wraps)
	 */
	var getMomentByElement = function(elementId) {
		var yearMonthDay = $('#' + elementId).val();
		var hour = $('#' + elementId + 'Hour').val();
		var minute = $('#' + elementId + 'Minute').val();		
		var momentDate = moment(yearMonthDay + ' ' + hour + ':' + minute + ':00', dateFormat);
		return momentDate;
	}
	
	/**
	 * 設定日期時間欄位值
	 */
	var setElementDate = function(elementId, timestamp) {
		if (!timestamp) {
			return;
		}
		
		var momentDate = moment(timestamp);
		$('#' + elementId).val(momentDate.format('YYYY-MM-DD'));
		
		var hour = momentDate.hour();
		$('#' + elementId + 'Hour').val(hour < 10 ? '0' + hour : hour).change();
		
		var minute = momentDate.minute();
		$('#' + elementId + 'Minute').val(minute < 10 ? '0' + minute : minute).change();		
	}
	
	// 下拉選項
	var optionSelectChange_func = function(){
		var selectValue = $(this).find('option:selected').text();
		$(this).closest('.option').find('.optionLabel').html(selectValue);
	};
	$('.optionSelect').change(optionSelectChange_func);
		
	// 日期元件
	$(".datepicker").datepicker({ 'minDate' : 0, 'dateFormat' : 'yy-mm-dd'});

	// 預覽按鍵
	$('.btn_preview').click(function(event) {
		previewFunc();
	});
	
	// 預覽按鍵
	$('.btn_query').click(function(event) {
		previewFunc();
	});
	
	var previewFunc = function(){

		if(actionType == 'Read'){
			var couponId = $.urlParam("couponId");
			previewShowFunc(couponId);
		}
		else{
			var postData = getDataFromUI();
			if(!postData){
				return;
			}

			$('.LyMain').block($.BCS.blockMsgSave);
			$.ajax({
				type : "POST",
				url : bcs.mContextPath + '/createPreviewContentRewardCard',
	            cache: false,
	            contentType: 'application/json',
	            processData: false,
				data : JSON.stringify(postData)
			}).success(function(previewRewardCardId){
				previewShowFunc(previewRewardCardId);
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
				$('.LyMain').unblock();
			}).done(function(){
				$('.LyMain').unblock();
			});
		}
	}
	
	var previewShowFunc = function(previewRewardCardId){
		var config = {};
		config.minHeight = '1000px';
		var previewDialog = $('#previewDialog');
		previewDialog.html('<iframe src="' + bcs.mContextPath + '/userRewardCardPreviewContentPage?referenceId=' + previewRewardCardId + '" allowfullscreen style="width: 100%;height: 850px;"></iframe>');
		$.BCS.newPreviewDialog("集點卡預覽", previewDialog, config);
		
		previewDialog.find('iframe').load(function () {
			$(this).contents().find('.coupon_thumb').css('width', '80px').css('height','80px');
			$(this).contents().find('.coupon_title').css('font-size', '16px');
			
			/* 集點卡的 header */
			$(this).contents().find('.shop_hd.owner').css('margin', '2px 2px 0px 2px');
			$(this).contents().find('.hd_con').css('height', '55px');
			
			/* 集點卡左上角 Logo */
			$(this).contents().find('.hd_con span.thum').css({'width': '35px', 'height': '35px', 'top': '10px'});
			$(this).contents().find('.hd_con span.thum img').css('width', '35px');
			
			/* 集點卡上方的標題、子標題 */
			$(this).contents().find('.hd_con .tit').css('margin', '1px 0 0 10px');
			$(this).contents().find('.hd_con .tit h1').css({'font-size': '15px', 'height': '20px'});
			$(this).contents().find('.hd_con .tit h2').css({'font-size': '11px', 'margin=top': '0'});
			
			/* 集點卡下半部區塊 */
			$(this).contents().find('.stamp_wrap.owner').css('padding', '0px 2px 2px 2px');
			$(this).contents().find('.stamp_con').css('min-height', '148px');
			
			/* 集點卡中蒐集點數的區塊 */
			$(this).contents().find('.bang_size_01').css('padding-top', '24px');
			$(this).contents().find('.bang_size_01 li').css({'width': '78px', 'height': '78px'});
			
			$(this).contents().find('.bang_size_02').css('padding-top', '28px');
			$(this).contents().find('.bang_size_02 li').css({'width': '63px', 'height': '63px'});
			
			$(this).contents().find('.bang_size_03').css('padding-top', '28px');
			$(this).contents().find('.bang_size_03 li').css({'width': '63px', 'height': '63px'});
			
			$(this).contents().find('.bang_size_04').css('padding-top', '40px');
			$(this).contents().find('.bang_size_04 li').css({'width': '43px', 'height': '43px'});
			
			$(this).contents().find('.bang_size_05').css({'padding-top': '40px', 'margin-left': '28px'});
			$(this).contents().find('.bang_size_05 li').css({'width': '43px', 'height': '43px'});
			
			$(this).contents().find('.bang_size_06-25:first-child').css('padding-top', '16px');
			$(this).contents().find('.bang_size_06-25').css('margin-left', '28px');
			$(this).contents().find('.bang_size_06-25 li').css({'width': '43px', 'height': '43px'});
			
			$(this).contents().find('.bang_size_26-30:first-child').css('padding-top', '14px');
			$(this).contents().find('.bang_size_26-30').css('margin-left', '14px');
			$(this).contents().find('.bang_size_26-30 li').css({'width': '23px', 'height': '23px'});
			
			$(this).contents().find('.bang_size_31-50:first-child').css('padding-top', '11px');
			$(this).contents().find('.bang_size_31-50').css('margin-left', '14px');
			$(this).contents().find('.bang_size_31-50 li').css({'width': '23px', 'height': '23px'});
			
			/* 集點卡中使用期限的區塊 */
			$(this).contents().find('.bang_date').css({'font-size': '10px', 'padding-top': '8px'});
			
			/* 集點卡列表區塊 */
			$(this).contents().find('.wrapper').css('border-bottom', '12px solid #9C9E9C');
			$(this).contents().find('.coupon_notes').css('padding', '20px 10px 10px 10px');
			$(this).contents().find('.coupon_title').css({'font-size': '15px', 'line-height': '22px'});
			$(this).contents().find('.coupon_list li').css('padding', '20px 0px 0px 0px');
			
			/* 注意事項區塊 */
			$(this).contents().find('.coupon_notes .item2').css('font-size', '15px');
			$(this).contents().find('.coupon_notes pre').css({'font-size': '13px', 'line-height': '25px', 'padding': '30px 10px 30px 42px'});
		})
		
		previewDialog.dialog('open');
	}
	
	// 上傳圖片
	$('#rewardCardListImageInput').on("change", function(event){

		if(actionType == "Read"){
			location.reload();
			return;
		}
		
		if (!validator.element(this)) {
			return false;
		}
		
		var input = event.currentTarget;
		
    	if (input.files && input.files[0]) {
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
	            	$('#rewardCardListImageId').val(response.resourceId);
	            	$('#rewardCardListImage').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
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
	});
	
	//根據收集點數改變樣式
	$('#requirePoint').change(function(){
		console.info("requirePoint:" + $(this).find('option:selected').text());
		
		stampListRender('stamp_list_render_area');
	});
	
	//切換右邊集點卡款式，並更改左邊樣式圖案
	$('#crePrevBox').find('div').click(function(e){
        templateSetting(e.target);
    });	
	
	function templateSetting(target) {
        var template_num = $(target).attr('data-template-num')||0;
        if (template_num > 0) {
            $('#template_no').val(template_num);
            $('#crePrevBox>li').removeClass('on');
            $(target).parent().addClass('on');
            var changeNum = Number(template_num);
            $('.stamp_con').removeAttr('style');
            $('.stamp_con').attr('class', $('.stamp_con').attr('class').replace(/stamp_con_bg_color_[0-9]{2}/gi, 'stamp_con_bg_color_' + leadingZeros(changeNum, 2)));
            $('#background_image_name').html('');
            
            customizedBackground = false;
        }
    }
	
	function leadingZeros(num, digits) {
        
        var zero = '';
        var StringNum = num.toString();
        if (StringNum.length < digits) {
            for (var i = 0; i < digits - StringNum.length; i++) {
                zero += '0';
            }
        }
        num = num.toString();
        return zero + num;
    }
	
	//樣板主標題隨輸入改變
	$('#rewardCardMainTitle').change(function(){
		$('.rewardCardMainTitle').html($('#rewardCardMainTitle').val());
	});
	//樣板副標題隨輸入改變
	$('#rewardCardSubTitle').change(function(){
		$('.rewardCardSubTitle').html($('#rewardCardSubTitle').val());
	});
	
	// 有效期限
	$(':radio[name="expire_type"]').change(function(event){
		expireMonthSet();
		
		var disabled_type1 = $(this).val() == '1';
		var disabled_type2 = $(this).val() == '2';
		
		$(".optionSelect.expire_type_1").attr("disabled", !disabled_type1);
		$(".optionSelect.expire_type_2").attr("disabled", !disabled_type2);
		
		//改變字體顏色
		if(disabled_type1){
			$(".optionSelect.expire_type_1").closest('label').find('.optionLabel').css('color', 'black');
			$(".optionSelect.expire_type_2").closest('label').find('.optionLabel').css('color', 'gray');
		}else if(disabled_type2){
			$(".optionSelect.expire_type_1").closest('label').find('.optionLabel').css('color', 'gray');
			$(".optionSelect.expire_type_2").closest('label').find('.optionLabel').css('color', 'black');
		}else{
			$(".optionSelect.expire_type_1").closest('label').find('.optionLabel').css('color', 'gray');
			$(".optionSelect.expire_type_2").closest('label').find('.optionLabel').css('color', 'gray');
		}
	});
	
	// 防止不當使用設定
	$(':radio[name="give_limit_type"]').change(function(event){
		var disable = $(this).val() == '2';
		$(".optionSelect.limit_type").attr("disabled", !disable);
		
		var limit_number_disable = $(this).val() == '1';
		$(".optionSelect.limit_number").attr("disabled", !limit_number_disable);
		
		if(disable){
			$(".optionSelect.limit_type").closest('label').find('.optionLabel').css('color', 'black');
		}else{
			$(".optionSelect.limit_type").closest('label').find('.optionLabel').css('color', 'gray');
		}
		
		if(limit_number_disable){
			$(".optionSelect.limit_number").closest('label').find('.optionLabel').css('color', 'black');
		}else{
			$(".optionSelect.limit_number").closest('label').find('.optionLabel').css('color', 'gray');
		}
		
		if($("[name=give_limit_type]:checked").val() == "0"){
			$('#give_limit_branch_type').val(0);
		}else if($("[name=give_limit_type]:checked").val() == "1"){
			$('#give_limit_branch_type').val(24);
		}else{
			$('#give_limit_branch_type').val($('#pointSetTime option:selected').text());
		}
	});
	
	$('#pointSetTime').change(function(){
		$('#give_limit_branch_type').val($('#pointSetTime option:selected').text());
	});
	
	// 取消
	$('.btn_cancel').click(function(){

		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}

		if(from == 'disable'){
			window.location.replace(bcs.bcsContextPath + '/edit/rewardCardListDisablePage');
		}
		else if(from == 'active'){
			window.location.replace(bcs.bcsContextPath + '/edit/rewardCardListPage');
		}
		else{
			window.location.replace(bcs.bcsContextPath + '/edit/rewardCardListPage');
		}
	});
	
	var getDataFromUI = function(){

		if (!validator.form()) {
			return;
		}
		console.info("getDataFromUI");
		var rewardCardId = $.urlParam("rewardCardId");
		
		if(actionType == 'Copy')
			rewardCardId = null;
		
		// 集點卡主標題
		var rewardCardMainTitle = $('#rewardCardMainTitle').val();
		
		// 集點卡副標題
		var rewardCardSubTitle = $('#rewardCardSubTitle').val();
		
		//集點卡樣式
		var rewardCardBackGround = customizedBackground ? $('#rewardCardBackgroundImageId').val()  : $('.stamp_con').attr('class');
		
		//集點卡背景圖片
		var rewardCardListImageId = $('#rewardCardListImageId').val();

		// 所需收集點數
		var requirePoint = $("#requirePoint option:selected").text();
		
		// 集滿點數後發送的優惠卷
		var couponId = $('.COUPON_ID').val();
		var couponTitle = $('.mdCMN07HeadTtl01').text().split("：")[1];
		
		// 使用效期
		var momentRewardCardStartUsingTime = getMomentByElement('rewardCardStartUsingTime');
		var momentRewardCardEndUsingTime = getMomentByElement('rewardCardEndUsingTime');
		
		// 取卡回饋點數
		var bonusPoint = $("#bonus_point option:selected").text();
		
		// 防止不當使用設定
		var limitGetTime = $('#give_limit_branch_type').val();
		var limitGetNumber = '0';
		console.log($('#give_limit_point').val());
		if(limitGetTime == '24'){
			limitGetNumber = $('#give_limit_point').val();
		}
		else if(limitGetTime != '24' && limitGetTime != '0'){
			limitGetNumber = '1';
		}
		console.log('limitGetNumber='+ limitGetNumber);
		
		// 領用期間
		var momentRewardCardStartGetTime = getMomentByElement('rewardCardStartGetTime');
		var momentRewardCardEndGetTime = getMomentByElement('rewardCardEndGetTime');
		
		// 使用事項
		var rewardCardUseDescription = $('#rewardCardUseDescription').val();
		
		// 使用說明
		var rewardCardDescription = $('#rewardCardDescription').val();
		
		var rewardCardFlag = $('.rewardCardFlag').val();
		
		var contentRewardCard = {
			    rewardCardId : rewardCardId,
			    rewardCardMainTitle : rewardCardMainTitle,
			    rewardCardSubTitle : rewardCardSubTitle,
			    rewardCardBackGround : rewardCardBackGround,
			    rewardCardListImageId : rewardCardListImageId,
			    requirePoint : requirePoint,
			    couponId : couponId,
			    couponTitle : couponTitle,
			    rewardCardStartUsingTime : momentRewardCardStartUsingTime.format(dateFormat),
			    rewardCardEndUsingTime : momentRewardCardEndUsingTime.format(dateFormat),
			    bonusPoint : bonusPoint,
			    limitGetTime : limitGetTime,
			    limitGetNumber : limitGetNumber,
			    rewardCardStartGetTime : momentRewardCardStartGetTime.format(dateFormat),
			    rewardCardEndGetTime : momentRewardCardEndGetTime.format(dateFormat),
			    rewardCardUseDescription : rewardCardUseDescription,
			    rewardCardDescription : rewardCardDescription,
			    rewardCardFlag : rewardCardFlag
			  };
		
			  var contentCouponList = [];
			  for(var index =0; index < currentCouponList.length; index++){
				  currentCoupon = currentCouponList[index];
				  couponSetPointTr = $('.couponList').find('#coupon'+(index+1));
				  var requirePoint = couponSetPointTr.find('#couponPointInput').val();
				  contentCouponList.push({
					  couponId : currentCoupon,
					  requirePoint : requirePoint
				  });
			  }
			  
			  var postData = {
			    contentRewardCard : contentRewardCard,
			    contentCouponList : contentCouponList
			  }
			  
			  return postData;
	};
	
	// 儲存按鍵
	$('.btn_save').click(function(){
		console.info("btn_save");
		var postData = getDataFromUI();
		var isSettedRequirePoint = postData.contentRewardCard.requirePoint;
		
		if(actionType == "Read"){
			location.reload();
			return;
		}
		
		if (!validator.form()) {
			return;
		}
		
		//驗證是否每個優惠券都已填寫
		var couponSetPointNum = $('.couponSetPoint').length;
		if($('.MsgFrame').length < couponSetPointNum){
			alert('請選擇優惠券');
			return;
		}
		
		//驗證是否每個優惠券都填寫點數
		var   Regular   =   /^[0-9]*[1-9][0-9]*$/;
		
		for(var index=0;index < couponSetPointNum;index++){
			couponSetPointTr = $('.couponList').find('#coupon'+(index+1));
			var requirePoint = couponSetPointTr.find('#couponPointInput').val();
			
			if(requirePoint==='' || requirePoint===null){
				alert('請輸入優惠券點數');
				return;
			}else if(parseInt(requirePoint) > parseInt(isSettedRequirePoint)){
				console.info('isSettedRequirePoint',isSettedRequirePoint);
				
				alert('優惠券點數不能大於 '+isSettedRequirePoint+' 點');
				return;
			}else if(Regular.test(requirePoint)!==true){
				alert('請設定優惠券點數為正整數');
				return;
			}
		}

		
		if (!confirm(actionType == 'Create' ? '請確認是否建立' : '請確認是否儲存')) {
			return false;
		}

		$('.LyMain').block($.BCS.blockMsgSave);
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/edit/saveContentRewardCard',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			alert( '儲存成功');

			if(from == 'disable'){
				window.location.replace(bcs.bcsContextPath + '/edit/rewardCardListDisablePage');
			}
			else if(from == 'active'){
				window.location.replace(bcs.bcsContextPath + '/edit/rewardCardListPage');
			}
			else if(from == 'api'){
				window.location.replace(bcs.bcsContextPath + '/edit/rewardCardListApiPage');
			}
			else{
				window.location.replace(bcs.bcsContextPath + '/edit/rewardCardListPage');
			}
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	});
	
	var loadDataFunc = function(){
		var rewardCardId = $.urlParam("rewardCardId");
		stampListRender('stamp_list_render_area');
	    
	    couponTrTemplate = $('.couponTrTemplate').clone(true);
	    $('.couponTrTemplate').remove();
	    
		if (rewardCardId) {			
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/edit/getContentRewardCard?rewardCardId=' + rewardCardId
			}).success(function(response){
				console.log(response);
				contentRewardCard = response.contentRewardCard;
				contentCouponList = response.contentCouponList;

				for(var index in contentCouponList){
					contentCoupon = contentCouponList[index];
					var selectedCouponMsgFrame = MsgFrameTemplate["COUPON"].clone();
					var couponTr = generateCouponSetPoint();
					couponTr.find('.TypeMsgSolid').hide();
					
					selectedCouponMsgFrame.css('display', '');

    				selectedCouponMsgFrame.find('.COUPON_ID').val(contentCoupon.couponId);
    				selectedCouponMsgFrame.find('img').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + contentCoupon.couponListImageId);
    				selectedCouponMsgFrame.find('.COUPON_DESCRIPTION').text(contentCoupon.couponDescription);
    				selectedCouponMsgFrame.find('.mdCMN07HeadTtl01').text('優惠劵：' + contentCoupon.couponTitle);
    				couponTr.find('.coupon_probability').val(contentCoupon.probability);
    				couponTr.find('#couponPointInput').val(contentCoupon.requirePoint);
    				selectedCouponMsgFrame.find('.MdBtn03Delete').click(deleteMsgContentEvent);
    				selectedCouponMsgFrame.find('.MdBtn03Delete').css("display", "none");

    				couponTr.find('.MsgPlace').append(selectedCouponMsgFrame);
    				$('.couponList').append(couponTr);
    				
    				currentCouponList.push(contentCoupon.couponId);
				}
				getCouponList();
				

				setAddMsgContentBtnEvent();
				COUPONCOUNT = CURRENT_COUPONCOUNT = contentCouponList.length;
				setActionOperationButtonsVisable();
				//集點卡主副標題
				$('#rewardCardMainTitle').val(contentRewardCard.rewardCardMainTitle).change();
				$('#rewardCardSubTitle').val(contentRewardCard.rewardCardSubTitle).change();
				
				//集點卡樣式
				// $('.stamp_con').attr("class", contentRewardCard.rewardCardBackGround);
				var template_num = contentRewardCard.rewardCardBackGround.split("0")[1];
		        if (template_num > 0 || contentRewardCard.rewardCardBackGround.match(/stamp_con stamp_con_bg_color_0[0-9]/g)) {    // 為既有樣式的底圖
		            $('#template_no').val(template_num);
		            $('#crePrevBox>li').removeClass('on');
		            $('.img_00' + template_num).parent().addClass('on');
		            var changeNum = Number(template_num);
		            $('.stamp_con').attr('class', $('.stamp_con').attr('class').replace(/stamp_con_bg_color_[0-9]{2}/gi, 'stamp_con_bg_color_' + leadingZeros(changeNum, 2)));
		        } else {// 為客製化的底圖
		        	$('#crePrevBox>li').removeClass('on');
		        	$('#rewardCardBackgroundImageId').val(contentRewardCard.rewardCardBackGround);
		        	$('.stamp_con').attr('style', 'background-image: url("' + bcs.bcsContextPath + '/getResource/IMAGE/' + contentRewardCard.rewardCardBackGround + '"); background-repeat: no-repeat; background-size: 100% 100%;');
		        	customizedBackground = true;
		        }
				
				//集點卡列表圖片
				$('#rewardCardListImageId').val(contentRewardCard.rewardCardListImageId);
				$('#rewardCardListImage').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + contentRewardCard.rewardCardListImageId);
				
				//所需收集點數
				$("#requirePoint").val(contentRewardCard.requirePoint).change();

            	//使用效期
            	setElementDate('rewardCardStartUsingTime', contentRewardCard.rewardCardStartUsingTime);
            	setElementDate('rewardCardEndUsingTime', contentRewardCard.rewardCardEndUsingTime);
            	
            	//取卡回饋點數
            	$("#bonus_point").val(contentRewardCard.bonusPoint).change();
            	
            	//防止不當使用設定
            	if(contentRewardCard.limitGetTime == '0'){
            		$('input:radio[name="give_limit_type"]').filter('[value="0"]').attr('checked', true);
            	}else if(contentRewardCard.limitGetTime == '24'){
            		$('input:radio[name="give_limit_type"]').filter('[value="1"]').attr('checked', true);
            		$('#give_limit_point').val(contentRewardCard.limitGetNumber).change();
            		$(".optionSelect.limit_number").prop("disabled", false);
            		$(".optionSelect.limit_number").closest('label').find('.optionLabel').css('color', 'black');
            	}else{
            		$('input:radio[name="give_limit_type"]').filter('[value="2"]').attr('checked', true);
            		$("#pointSetTime").val(contentRewardCard.limitGetTime).change();
            		$(".optionSelect.limit_type").prop("disabled", false);
            		$(".optionSelect.limit_type").closest('label').find('.optionLabel').css('color', 'black');
            	}
            	
            	//領取期間
            	setElementDate('rewardCardStartGetTime', contentRewardCard.rewardCardStartGetTime);
            	setElementDate('rewardCardEndGetTime', contentRewardCard.rewardCardEndGetTime);
            	
            	//使用方法
            	$('#rewardCardUseDescription').text(contentRewardCard.rewardCardUseDescription);
            	//注意事項
            	$('#rewardCardDescription').text(contentRewardCard.rewardCardDescription);
            	
            	// 計算字數
            	$('#couponTitle, #couponSerialNumber, #eventReference, #couponDescription').keyup();
            	$('#couponTitle, #couponSerialNumber, #eventReference, #couponUseDescription').keyup();
            	$('#couponTitle, #couponSerialNumber, #eventReference, #couponRuleDescription').keyup();
			
            	$('.rewardCardFlag').val(contentRewardCard.rewardCardFlag);
				$('.rewardCardFlag').change();
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});

		}else{
			addCoupon();
			getCouponList();
		}
		
		
	};

	//生成集點卡點數圖案
	var stampListRender = function(target) {
	      var totalAmount = parseInt($( "#requirePoint option:selected" ).text());
	      document.getElementById(target).innerHTML = "";
	      var size = 0;
	      var lineDiv = 10;
	      var strHtml = '';
	      var idxSplit = [];
	      var clsName = 'check';
	      
	      if (totalAmount < 6) {
	            size = leadingZeros(totalAmount, 2)+'';
	      } else {
	            if (totalAmount > 5 && totalAmount < 26) {
	                  size = '06-25';
	                  lineDiv = 5;
	            } else if (totalAmount > 25 && totalAmount < 31) {
	                  size = '26-30';
	            } else {
	                  size = '31-50';
	            }
	      }
	      
	      strHtml += '<ul class="bang_size_'+size+'">';
	      for (var startIdx = 1; startIdx <= totalAmount; startIdx++) {
	            if (startIdx == 1) {
	                  strHtml += '<li class="bang" stampIdx="'+startIdx+'"></li>'
	            }else if (startIdx == totalAmount) {
	                  strHtml += '<li class="goal" stampIdx="'+startIdx+'"></li>';
	            } else if (startIdx > 9) {
	                  for (var splitIdx = 0, len = startIdx.toString().length; splitIdx < len; splitIdx += 1) {
	                        idxSplit.push(startIdx.toString().charAt(splitIdx));
	                  }
	                  strHtml += '<li class="number" stampIdx="'+startIdx+'">';
	                  strHtml += '    <div class="n'+idxSplit[0]+'"></div>';
	                  strHtml += '    <div class="n'+idxSplit[1]+'"></div>';
	                  idxSplit.splice(0, 2);
	            }else{
	                  strHtml += '<li class="single number" stampIdx="'+startIdx+'">';
	                  strHtml += '    <div class="n'+startIdx+'"></div>';
	            }
	            strHtml += '</li>';
	            
	            if (lineDiv >= 5 && (startIdx%lineDiv) == 0) {
	                  strHtml += '</ul>';
	                  strHtml += '<ul class="bang_size_'+size+'">';
	            }
	      }
	      strHtml += '</ul>';
	      document.getElementById(target).innerHTML = strHtml;
	}
	
	
	
	/* 上傳自訂集點卡底圖 */
	$('#rewardCardBackgroundInput').on("change", function(event){

		if(actionType == "Read"){
			location.reload();
			return;
		}
		
		if (!validator.element(this)) {
			return false;
		}
		
		var input = event.currentTarget;
		
    	if (input.files && input.files[0]) {
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
	            	alert("集點卡自訂底圖上傳成功!");
	            	$('#crePrevBox>li').removeClass('on');
	            	$('#rewardCardBackgroundImageId').val(response.resourceId);
	            	//$('#rewardCardBackgroundImage').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
	            	$('.stamp_con').attr('style', 'background-image: url("' + bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId + '"); background-repeat: no-repeat; background-size: 100% 100%;');
	            	$('#background_image_name').html('檔名：' + fileName);
	            	customizedBackground = true;
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
	});	
	
	var generateCouponSetPoint = function(){
		var couponSetPointTr = couponSetPoint.clone(true);
		
		var couponTarget = 'coupon' + (COUPONCOUNT+=1);
		
		couponSetPointTr.attr('id',couponTarget);
		
		return couponSetPointTr;
	}
	
	//-----------增加優惠券設定欄位-------------//
	var addCoupon = function(){		
		$('.couponList').append(generateCouponSetPoint());
		setAddMsgContentBtnEvent();
		setActionOperationButtonsVisable();
	};
	$('#addCoupon').click(addCoupon);
	
	var deleteCoupon = function(){
		COUPONCOUNT -= 1;
		var couponSetPointNum = $('.couponSetPoint').length;
		if(currentCouponList.length > 1 || couponSetPointNum> 1){
			var deletedCouponId = $('.couponList .couponSetPoint:last-child').find('.COUPON_ID').val();
			
			if(deletedCouponId){
				console.log(findStringInArray(currentCouponList, deletedCouponId));
				currentCouponList.splice(findStringInArray(currentCouponList, deletedCouponId), 1);
			}
			$('.couponList .couponSetPoint:last-child').remove();
			getCouponList();
			
		}else
			alert("至少要設定一張優惠券");
		
		setActionOperationButtonsVisable();
	};
	$('#deleteCoupon').click(deleteCoupon);
	
	/* 「刪除優惠券」的按鈕的 消失/顯示 控制 */
	var setActionOperationButtonsVisable = function(){
		if(actionType === 'Edit'){
			if(parseInt(COUPONCOUNT) == parseInt(CURRENT_COUPONCOUNT) ){
				$('#deleteCoupon').hide();
			} else {
				$('#deleteCoupon').show();
			}
		} else {
			if(parseInt(COUPONCOUNT) == 1){
				$('#deleteCoupon').hide();
			} else {
				$('#deleteCoupon').show();
			}
		}
	}
	
	loadDataFunc();
	
	function findStringInArray(array, key){
		var result_index = -1;
		
		for(var index in array){
			if(array[index] === key){
				result_index = index;
				return result_index;
			}
		}
		
		return result_index;
	}
});