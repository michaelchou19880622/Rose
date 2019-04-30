/**
 * 
 */
$(function(){
	
	// 表單驗證
	var validator = $('#formCampaign').validate({
		rules : {
			
			// 商品名稱
			'campaignName' : {
				required : {
			        param: true
				},
				maxlength : 100
			},
			'startTime': {
		      required: true
		    },
			'endTime': {
		      required: true
		    },
			'price': {
		      number: true,
		      required: true,
		      minNumeric: 0
		    },
			'groupId': {
		      required: true
		    },
			'gameId': {
		      required: true
		    }
		}
	});
		
	$('.btn_save').click(function(){
		if (!validator.form()) {
			return;
		}
	
		var campaignName = $('#campaignName').val();
		console.info('campaignName', campaignName);
		var startTime = getTimeValue("startTime");
		var endTime = getTimeValue("endTime");
		var price = $('#price').val();
		var groupId = $('#groupId').val();
		var gameId = $('#gameId').val();
		var isActive = $('#isActive').val();
		var campaignId = $.urlParam("campaignId");
		console.info('campaignId', campaignId);
		var actionType = $.urlParam("actionType");
		console.info('actionType', actionType);

		if (new Date(startTime).getTime() > new Date(endTime).getTime()) {
			alert("起始日不能大於結束日");
			return false;
		}
		
		var postData = {};
		if (campaignId) {
			postData.campaignId = campaignId;
		}
		postData.campaignName = campaignName;
		postData.startTime = startTime;
		postData.endTime = endTime;
		postData.price = price;
		postData.groupId = groupId;
		postData.isActive = isActive;
		postData.gameId = gameId;
		
		console.info('postData', postData);

		/**
		 * Do Confirm Check
		 */
		var confirmStr = "請確認是否建立";
		if(campaignId && actionType == 'Edit'){
			confirmStr = "請確認是否儲存";
		}

		var r = confirm(confirmStr);
		if (r) {
			// confirm true
		} else {
		    return;
		}

		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/admin/createCampaign',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postData)
		}).success(function(response){
			console.info(response);
			alert( '儲存成功');
 			window.location.replace(bcs.bcsContextPath + '/admin/campaignListPage');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	});
	
	$('.btn_cancel').click(function(){
		
		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		var groupId = $.urlParam("groupId");
 		window.location.replace(bcs.bcsContextPath + '/admin/campaignListPage?isActive=true');
	});
	
	var optionSelectChange_func = function(){
		var selectValue = $(this).find('option:selected').text();
		$(this).closest('.option').find('.optionLabel').html(selectValue);
	};

	var getTimeValue = function(id) {
		var time = '';

		var datepickerVal = $('#' + id + ' .datepicker').val();
		console.info('datepickerVal', datepickerVal);
		if(!datepickerVal){
			return null;
		}

		var selectHour = $('#' + id + ' .selectHour').val();
		console.info('selectHour', selectHour);

		var selectMinuteOne = $('#' + id + ' .selectMinuteOne').val();
		console.info('selectMinuteOne', selectMinuteOne);

		var selectMinuteTwo = $('#' + id + ' .selectMinuteTwo').val();
		console.info('selectMinuteTwo', selectMinuteTwo);

		time = datepickerVal + " " + selectHour + ":" + selectMinuteOne + selectMinuteTwo + ":00";
		console.info('time', time);

		return time;
	}

	var setTimeValue = function(id, milisec) {
		if(milisec){
			var d = new Date(milisec);
			var yyyyMMdd =  [d.getFullYear(),
	          padLeft(d.getMonth() + 1, 2, '0'),
	          padLeft(d.getDate(), 2, '0')
	         ].join('-')

			console.info('d', d);

			$('#' + id + ' .datepicker').val(yyyyMMdd);

			$('#' + id + ' .selectHour').val(padLeft(d.getHours(), 2, '0'));
			$('#' + id + ' .selectHour').change();

			var min = padLeft(d.getMinutes(), 2, '0');
			$('#' + id + ' .selectMinuteOne').val(min.substr(0, 1));
			$('#' + id + ' .selectMinuteOne').change();

			$('#' + id + ' .selectMinuteTwo').val(min.substr(1, 2));
			$('#' + id + ' .selectMinuteTwo').change();
		}
	}

	var padLeft = function(v, len, char) {
		var str = v.toString();
		for (var i = str.length; i < len; i++) {
			str = char + str;
		}
		return str;
	}

	var loadDataFunc = function(){
		var campaignId = $.urlParam("campaignId");
		var campaign;
		if(campaignId){
			$.ajax({
				type : "GET",
				async : false,
				url : bcs.bcsContextPath + '/admin/getCampaign?campaignId=' + campaignId
			}).success(function(response){
				console.info(response);
				campaign = response;

				$('#campaignName').val(campaign.campaignName);
				setTimeValue('startTime', campaign.startTime);
				setTimeValue('endTime', campaign.endTime);
				$('#price').val(campaign.price);
				$('#isActive').val(campaign.isActive);
				
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
			
			var actionType = $.urlParam("actionType");
			
			if(actionType == "Edit"){
				$('.CHTtl').html('編輯商品');
			}
		}

		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/getGameNameList'
		}).success(function(response){
			console.info(response);

			$.each(response, function(i, o){

				var contentGame = $('<option value=""></option>');

				contentGame.val(i);
				contentGame.html(o);

				$('.contentGame').append(contentGame);
			});

			$('.contentGame').change(optionSelectChange_func);
			$('#gameId').val(campaign.gameId).change();
					
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});

		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/getProductGroupNameList'
		}).success(function(response){
			console.info(response);

			$.each(response, function(i, o){

				var productGroup = $('<option value=""></option>');

				productGroup.val(i);
				productGroup.html(o);

				$('.productGroup').append(productGroup);
			});

			$('.productGroup').change(optionSelectChange_func);
			$('#groupId').val(campaign.groupId).change();

		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
			
		
	};

	$( ".datepicker" ).datepicker({ 'minDate' : 0, 'dateFormat' : 'yy-mm-dd'});
	
	$('.groupId').change(optionSelectChange_func);
	$('.selectMonth').change(optionSelectChange_func);
	$('.selectWeek').change(optionSelectChange_func);
	$('.selectHour').change(optionSelectChange_func);
	$('.serialSetting').change(optionSelectChange_func);
	$('.selectMinuteOne').change(optionSelectChange_func);
	$('.selectMinuteTwo').change(optionSelectChange_func);

	loadDataFunc();
});