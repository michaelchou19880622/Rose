/**
 * 
 */
$(function(){
	var actionType = $.urlParam("actionType") || 'Create';
	console.info('actionType', actionType);
	var from = $.urlParam("from") || 'disable';
	console.info('from', from);
	var couponCodeListData = null;
	var dateFormat = "YYYY-MM-DD HH:mm:ss";
	var defaultCouponCodeListNumber = 0 ;
	var couponCodeListNumber = 0;
	// 表單驗證
	var validator = $('#formContentCoupon').validate({
		rules : {
			
			// 優惠券標題
			'couponTitle' : {
				required : true,
				maxlength : 50
			},
			
			// 列表縮圖
			'couponListImageInput' : {
				required : '#couponListImageId:blank'
			},
			
			// 主要圖片
			'couponImageInput' : {
				required : '#couponImageId:blank'
			},
						
			// 使用效期
			'couponStartUsingTime' : {
				required : true,
				dateYYYYMMDD : true
			},
			
			'couponStartUsingTimeHour' : {
				required : true
			},
			
			'couponStartUsingTimeMinute' : {
				required : true
			},
			
			'couponEndUsingTime' : {
				required : true,
				dateYYYYMMDD : true,
				compareDate : {
					compareType : 'after',
					dateFormat : dateFormat,
					getThisDateStringFunction : function() {
						var yearMonthDay = $('#couponEndUsingTime').val();
						var hour = $('#couponEndUsingTimeHour').val();
						var minute = $('#couponEndUsingTimeMinute').val();		
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
			
			'couponEndUsingTimeHour' : {
				required : true
			},
			
			'couponEndUsingTimeMinute' : {
				required : true
			},
			
//			// 優惠券序號
			'showCouponSerialNumber' : {
				required : true
			},
			
			'isCouponCode' : {
				required : true
			},
			
			'couponSerialNumber' : {
				required : ':radio[name="isCouponCode"][value="2"]:checked',
				maxlength : 50
			}, 
			
			// 可使用次數
			'couponUsingLimit' : {
				required : true
			},

			// 領用期間
			'couponStartGetTime' : {
//				required : '#couponEndGetTime:filled',//無作用?
				required : true,
				dateYYYYMMDD : true
			},
			
			'couponEndGetTime' : {
//				required : '#couponStartGetTime:filled',//無作用?
				required : true,
				dateYYYYMMDD : true,
				compareDate : {
					compareType : 'after',
					dateFormat : dateFormat,
					getThisDateStringFunction : function() {
						var yearMonthDay = $('#couponEndGetTime').val();
						var hour = $('#couponEndGetTimeHour').val();
						var minute = $('#couponEndGetTimeMinute').val();		
						return yearMonthDay + ' ' + hour + ':' + minute + ':00';
					},
					getAnotherDateStringFunction : function() {
						var yearMonthDay = $('#couponStartGetTime').val();
						var hour = $('#couponStartGetTimeHour').val();
						var minute = $('#couponStartGetTimeMinute').val();		
						return yearMonthDay + ' ' + hour + ':' + minute + ':00';
					},
					thisDateName : '領取期間結束日期',
					anotherDateName : '領取期間開始日期'
				}
			},
			
			// 領用次數限制
			'couponGetLimitNumber' : {
				required : function(){
					var checkIsCouponCode = $(':radio[name="isCouponCode"]:checked').val();
					if(checkIsCouponCode==='1'){
						return true;
					}
					return false;
				} ,
				min:1,
				digits : true,
				maxlength : 10
			},
			
			// 優惠劵事件設定
			'eventReference' : {
				maxlength : 50
			}
		},
		messages:{
			'couponGetLimitNumber':'數量不得小於 0'
		}
	});
	
	var couponTypeContentFlag = $.BCS.contentFlagComponent('couponType', 'COUPON');
	
	// 綁訂計算字數函式到輸入框
	var bindCountTextFunctionToInput = function() {
		$('#couponTitle, #couponSerialNumber, #eventReference').keyup(function() {
			var txtLength = $(this).val().length;
			var tr = $(this).closest("tr");
			var inputCount = tr.find(".MdTxtInputCount");
			var countText = inputCount.text();
			inputCount.text(countText.replace(/\d+\//, txtLength + '/'));
		});
		
		$("#couponDescription").keyup(function(e) {
			var txtLength = $(this).val().length;
			var tr = $(this).closest("tr");
			var inputCount = tr.find(".MdTxtNotice01");
			var countText = inputCount.text();
			inputCount.text(countText.replace(/\d+\//, txtLength + '/'));
		});
		
		$("#couponUseDescription").keyup(function(e) {
			var txtLength = $(this).val().length;
			var tr = $(this).closest("tr");
			var inputCount = tr.find(".MdTxtNotice01");
			var countText = inputCount.text();
			inputCount.text(countText.replace(/\d+\//, txtLength + '/'));
		});
		
		$("#couponRuleDescription").keyup(function(e) {
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
			var postData = getDataFromUI({isJson:true});
			console.info('postData', postData);
			if(!postData){
				return;
			}

			$('.LyMain').block($.BCS.blockMsgSave);
			$.ajax({
				type : "POST",
				url : bcs.mContextPath + '/createPreviewContentCoupon',
	            cache: false,
	            contentType: 'application/json',
	            processData: false,
				data : JSON.stringify(postData)
			}).success(function(response){
				console.info(response);
				previewShowFunc(response);
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
				$('.LyMain').unblock();
			}).done(function(){
				$('.LyMain').unblock();
			});
		}
	}
	
	var previewShowFunc = function(couponId){
		
		var config = {};
		config.minHeight = '1000px';
		
		var previewDialog = $('#previewDialog');
		previewDialog.html('<iframe src="' + bcs.mContextPath + '/userCouponPreviewContentPage?referenceId=' + couponId + '" allowfullscreen style="width: 100%;height: 1000px;"></iframe>');
		
		$.BCS.newPreviewDialog("優惠券預覽", previewDialog, config);
		
		previewDialog.find('iframe').load(function () {
			$(this).contents().find('body').css('font-size', '.45em');
			
			$(this).contents().find('.used_alert').css('line-height', '20px');
			$(this).contents().find('.used_alert').css('padding', '10px 0');
			
			$(this).contents().find('.coupon_copywriter .title').css('line-height', '1em');
			$(this).contents().find('.coupon_copywriter pre').css('line-height', '1em');
			
			$(this).contents().find('.coupon_copywriter .redeem li').css('margin-top', '10px');

			$(this).contents().find('.use_coupon').css('background-size', '320px 200%');
			
			$(this).contents().find('.coupon_copywriter .redeem .sn').css('background-size', '300px 100%');
			$(this).contents().find('.coupon_copywriter .redeem .sn').css('line-height', '3em');
			$(this).contents().find('.coupon_copywriter .redeem .sn').css('height', '100%');
			
			$(this).contents().find('.coupon_copywriter .redeem .date').css('background-size', '300px 100%');
			$(this).contents().find('.coupon_copywriter .redeem .date').css('line-height', '1em');
			$(this).contents().find('.coupon_copywriter .redeem .date').css('height', '100%');

			$(this).contents().find('.coupon_copywriter').css('padding-bottom', '10px');

			$(this).contents().find('.use_coupon').css('line-height', '2em');
			$(this).contents().find('.use_coupon').css('height', '2em');

			$(this).contents().find('.coupon_notes .item1').css('font-size', '16px');
			$(this).contents().find('.coupon_notes pre').css('font-size', '14px');
			$(this).contents().find('.coupon_notes pre').css('line-height', '1em');
			$(this).contents().find('.coupon_notes pre').css('padding', '10px 10px 10px 42px');

			$(this).contents().find('.coupon_notes .item2 ').css('font-size', '16px');

			$(this).contents().find('.coupon_notes_alert').css('font-size', '14px');
			$(this).contents().find('.coupon_notes_alert').css('line-height', '1em');
        });
		
		previewDialog.dialog('open');
	}
	
	// 上傳圖片
	$('#couponListImageInput').on("change", function(event){

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
	            	$('#couponListImageId').val(response.resourceId);
	            	$('#couponListImage').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
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
	
	// 上傳圖片
	$('#couponImageInput').on("change", function(event){

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
	            	$('#couponImageId').val(response.resourceId);
	            	$('#couponImage').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.resourceId);
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
	
	//上傳電子序號
	$('.btn_upload_CSV').click(function(){
		$('#uploadCouponCodeCSVFile').click();
	});
	
	$('#uploadCouponCodeCSVFile').on("change", function(ev){
		uploadCouponCsvFile(ev);
	})
	
	var uploadCouponCsvFile = function(ev){
		var input = ev.currentTarget;
		
    	if (input.files && input.files[0]) {
    		var fileName = input.files[0].name;
    		$('#couponCodeUploadFile').text(fileName);
    		couponCodeListData=input.files[0];
    		var form_data = new FormData();
    		form_data.append("couponCodeListData",couponCodeListData);
			countNumberForCouponCodeListFile(form_data,function(number){
				couponCodeListNumber = defaultCouponCodeListNumber+number;
				$("#couponGetLimitNumber").val(couponCodeListNumber);
			});
    	}
	}
	
	//checkFileNumberApi
	var countNumberForCouponCodeListFile = function(form_data,cb){
		$('.LyMain').block($.BCS.blockMsgUpload);
        $.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/edit/countNumberForCouponCodeListFile',
            cache: false,
            contentType: false,
            processData: false,
            data : form_data
		}).success(function(number){
			cb(number);
		}).fail(function(response){
			$('.LyMain').unblock();	
			$.FailResponse(response);
		}).done(function(){
			$('.LyMain').unblock();	
		});
	}

	// 優惠劵序號
	$(':radio[name="isCouponCode"]').change(function(event){
		var isCouponCode = $(this).val();
		
    	var div = $("#couponGetLimitNumber")
		.prop('readonly',isCouponCode==='1')
		.closest('div');
    	
    	
    	
    	if(isCouponCode==='1')
    		div.addClass('ExDisabled');
    	else
    		div.removeClass('ExDisabled');

    	switch(isCouponCode){
			case '0':
				$('#couponCodeUploadFile').hide();
				$(".couponCodeListUpload").hide();
				
				$(".couponSerialNumber").hide();
				
				$("#couponGetLimitNumber").val('');
				// 再驗證一次優惠劵輸入框，使其更新驗證狀態
				validator.element('#couponSerialNumber');
				break;
			case '1':	
				$('#couponCodeUploadFile').show();
				$(".couponCodeListUpload").show();
				
				//getCouponCodeListNumber
				var couponId = $.urlParam("couponId");
				console.log($("#couponGetLimitNumber").val());

				$("#couponGetLimitNumber").val(couponCodeListNumber);
				
				$(".couponSerialNumber").hide();
				break;
			case '2':
				$('#couponCodeUploadFile').hide();
				$(".couponCodeListUpload").hide();
				
				$("#couponGetLimitNumber").val('');
				
				$(".couponSerialNumber").show();
				break;
		}
	})

	// 取消
	$('.btn_cancel').click(function(){

		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}

		if(from == 'disable'){
			window.location.replace(bcs.bcsContextPath + '/edit/couponListDisablePage');
		}
		else if(from == 'active'){
			window.location.replace(bcs.bcsContextPath + '/edit/couponListPage');
		}
		else if(from == 'api'){
			window.location.replace(bcs.bcsContextPath + '/edit/couponListApiPage');
		}
		else{
			window.location.replace(bcs.bcsContextPath + '/edit/couponListPage');
		}
	});
	
	var getDataFromUI = function(o){
		var postData = new FormData();
		
		if (!validator.form()) {
			return;
		}
		
		var couponId = $.urlParam("couponId");
		console.info('couponId', couponId);
		
		if(actionType == 'Copy'){
			couponId = null;
		}
		
		// 優惠券標題
		var couponTitle = $('#couponTitle').val();

		// 列表縮圖
		var couponListImageId = $('#couponListImageId').val();
		
		// 主要圖片
		var couponImageId = $('#couponImageId').val();
				
		// 使用效期
		var momentCouponStartUsingTime = getMomentByElement('couponStartUsingTime');
		var momentCouponEndUsingTime = getMomentByElement('couponEndUsingTime');

		//是否設定電子序號
		
		var checkIsCouponCode = $(':radio[name="isCouponCode"]:checked').val();
		var isCouponCode = false;
		var couponSerialNumber = null;
		switch(checkIsCouponCode){
			case '1':
				isCouponCode=true;
				postData.append("couponCodeListData",couponCodeListData);
				break;
			case '2':
				couponSerialNumber=$('#couponSerialNumber').val();
				break;
		}
		
		var isFillIn = ($(':radio[name="isFillIn"]:checked').val()=='0')?false :true;
		
		// 可使用次數
		var couponUsingLimit = $(':radio[name="couponUsingLimit"]:checked').val();
		
		// 宣傳文字
		var couponDescription = $('#couponDescription').val();
		
		// 使用方法
		var couponUseDescription = $('#couponUseDescription').val();
		
		// 注意事項
		var couponRuleDescription = $('#couponRuleDescription').val();
		
		// 備註
		var couponRemark = $('#couponRemark').val();
		
		// 領用期間
		var momentCouponStartGetTime = getMomentByElement('couponStartGetTime');
		var momentCouponEndGetTime = getMomentByElement('couponEndGetTime');
		
		// 領用次數限制
		var couponGetLimitNumber = $('#couponGetLimitNumber').val();
		
		// 優惠劵事件設定
		var eventReference = $('#eventReference').val();

		// 設定其他條件
		var couponFlag = $('.couponFlag').val();
		console.info('couponFlag', couponFlag);
		
		var contentCouponModel = {
			flagValueList : couponTypeContentFlag.getContentFlagList(), // 優惠券類型
			contentCoupon : {
				couponId : couponId,
				couponTitle : couponTitle,
				couponListImageId : couponListImageId,
				couponImageId : couponImageId,
				couponStartUsingTime : momentCouponStartUsingTime.format(dateFormat),
				couponEndUsingTime : momentCouponEndUsingTime.format(dateFormat),
				couponSerialNumber : couponSerialNumber,
				isCouponCode : isCouponCode,
				isFillIn : isFillIn,
				couponUsingLimit : couponUsingLimit,
				couponDescription : couponDescription,
				couponUseDescription : couponUseDescription,
				couponRuleDescription : couponRuleDescription,
				couponStartGetTime : (momentCouponStartGetTime.isValid() ? momentCouponStartGetTime.format(dateFormat) : null),
				couponEndGetTime : (momentCouponEndGetTime.isValid() ? momentCouponEndGetTime.format(dateFormat) : null),
				couponGetLimitNumber : couponGetLimitNumber,
				couponFlag : couponFlag,
				eventReference: eventReference,
				couponRemark : couponRemark
			}
		};

		postData.append("contentCoupon",new Blob([JSON.stringify(contentCouponModel)],{type: "application/json"}));
		
		if(o.isJson===true)
			return contentCouponModel;
		else
			return postData;
	};
	
	// 儲存按鍵
	$('.btn_save').click(function(){

		if(actionType == "Read"){
			location.reload();
			return;
		}
		
		if (!validator.form()) {
			return;
		}
		
		var postData = getDataFromUI({isJson:false});

		if (!confirm(actionType == 'Create' ? '請確認是否建立' : '請確認是否儲存')) {
			return false;
		}

		$('.LyMain').block($.BCS.blockMsgSave);

		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/edit/saveContentCoupon',
            cache: false,
            contentType: false,
            processData: false,
            data : postData
		}).success(function(response){
			console.info(response);
			alert( '儲存成功');

			if(from == 'disable'){
				window.location.replace(bcs.bcsContextPath + '/edit/couponListDisablePage');
			}
			else if(from == 'active'){
				window.location.replace(bcs.bcsContextPath + '/edit/couponListPage');
			}
			else if(from == 'api'){
				window.location.replace(bcs.bcsContextPath + '/edit/couponListApiPage');
			}
			else{
				window.location.replace(bcs.bcsContextPath + '/edit/couponListPage');
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
		var couponId = $.urlParam("couponId");
		
		if (couponId) {
        	couponTypeContentFlag.findContentFlagList(couponId);
			
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/edit/getContentCoupon?couponId=' + couponId
			}).success(function(response){
				console.log(response);
				$('#couponTitle').val(response.couponTitle);
				
				$('#couponListImageId').val(response.couponListImageId);
				if(response.couponListImageId){
					if (response.couponListImageId.lastIndexOf('http://', 0) == 0 
							|| response.couponListImageId.lastIndexOf('https://', 0) == 0) {
						$('#couponListImage').attr("src", response.couponListImageId);
					}
					else{
						$('#couponListImage').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.couponListImageId);
					}
				}
				
				$('#couponImageId').val(response.couponImageId);
				if(response.couponImageId){
					if (response.couponImageId.lastIndexOf('http://', 0) == 0 
							|| response.couponImageId.lastIndexOf('https://', 0) == 0) {
						$('#couponImage').attr("src", response.couponImageId);
					}
					else{
		            	$('#couponImage').attr("src", bcs.bcsContextPath + '/getResource/IMAGE/' + response.couponImageId);
					}
				}
            	$('#couponType').val(response.couponType);
            	
            	setElementDate('couponStartUsingTime', response.couponStartUsingTime);
            	setElementDate('couponEndUsingTime', response.couponEndUsingTime);
            	
            	var isCouponCode = response.isCouponCode ? '1' : (response.couponSerialNumber ? '2' : '0');
            	
            	$(':radio[name="isCouponCode"]')
            		.val([isCouponCode])
            		.filter(':checked')
            		.change();
            	
            	
            	$(':radio[name="isFillIn"]')
            		.val([response.isFillIn ? '1' : '0'])
            		.filter(':checked')
            		.change();
            	
            	
            	if(actionType==='Copy'){
            		console.info('isCouponCode',isCouponCode);
            		$('#couponGetLimitNumber').val( isCouponCode == '1' ? 0 : null);
				}else{
	            	$('#couponSerialNumber').val(response.couponSerialNumber);

	            	getCouponCodeListNumber(response.couponId,function(number){
	            		defaultCouponCodeListNumber = number;
	            		couponCodeListNumber = defaultCouponCodeListNumber;
	            		var div = $("#couponGetLimitNumber").closest('div');
	            		
	            		if(isCouponCode==='1'){
	            			var div = $("#couponGetLimitNumber")
	            			.prop('readonly',true)
	            			.val(couponCodeListNumber)
	            			.closest('div');
	            			
	            			div.addClass('ExDisabled');
	            			$('#couponGetLimitNumber').val(couponCodeListNumber);
	            		}else{
	            			$('#couponGetLimitNumber').val(response.couponGetLimitNumber);
	                		div.removeClass('ExDisabled');
	            		}
	            	});
				}



            	
            	
				$(':radio[name="couponUsingLimit"]').val([response.couponUsingLimit]);
            	$('#couponDescription').val(response.couponDescription);
            	$('#couponUseDescription').val(response.couponUseDescription);
            	$('#couponRuleDescription').val(response.couponRuleDescription);
            	$('#couponRemark').val(response.couponRemark);
            	
            	setElementDate('couponStartGetTime', response.couponStartGetTime);
            	setElementDate('couponEndGetTime', response.couponEndGetTime);
            	

            	$('#eventReference').val(response.eventReference);

            	$('.couponFlag').val(response.couponFlag);
				$('.couponFlag').change();
            	
            	// 計算字數
            	$('#couponTitle, #couponSerialNumber, #eventReference, #couponDescription').keyup();
            	$('#couponTitle, #couponSerialNumber, #eventReference, #couponUseDescription').keyup();
            	$('#couponTitle, #couponSerialNumber, #eventReference, #couponRuleDescription').keyup();
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});

			if(actionType == "Read"){
				$('.btn_save').remove();
				$('#couponTitle').attr('disabled',true);
				$('.MdBtnUpload').remove();
				$('#couponType').attr('disabled',true);
				$('#couponStartUsingTime').attr('disabled',true);
				$('#couponStartUsingTimeHour').attr('disabled',true);
				$('#couponStartUsingTimeMinute').attr('disabled',true);
				$('#couponEndUsingTime').attr('disabled',true);
				$('#couponEndUsingTimeHour').attr('disabled',true);
				$('#couponEndUsingTimeMinute').attr('disabled',true);
//				$('.showCouponSerialNumber').attr('disabled',true);
				$('#couponSerialNumber').attr('disabled',true);
				$('.couponUsingLimit').attr('disabled',true);
				$('#couponDescription').attr('disabled',true);
				$('#couponUseDescription').attr('disabled',true);
				$('#couponRuleDescription').attr('disabled',true);
				$('#couponStartGetTime').attr('disabled',true);
				$('#couponStartGetTimeHour').attr('disabled',true);
				$('#couponStartGetTimeMinute').attr('disabled',true);
				$('#couponEndGetTime').attr('disabled',true);
				$('#couponEndGetTimeHour').attr('disabled',true);
				$('#couponEndGetTimeMinute').attr('disabled',true);
				$('#couponGetLimitNumber').attr('disabled',true);
				$('#eventReference').attr('disabled',true);
				$('#couponFlag').attr('disabled',true);
			}
		}
	};

	var getCouponCodeListNumber = function(couponId,cb){
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getCouponCodeListNumber?couponId='+couponId,
            cache: false,
            contentType: 'application/json',
            processData: false
		}).success(function(number){
			cb(number);
		}).fail(function(response){
			$('.LyMain').unblock();	
			$.FailResponse(response);
		}).done(function(){
			$('.LyMain').unblock();	
		});
	}

	var optionSelectChange_func = function(){
		var selectValue = $(this).find('option:selected').text();
		$(this).closest('.option').find('.optionLabel').html(selectValue);
	};

	$('.couponFlag').change(optionSelectChange_func);
	
	loadDataFunc();
});