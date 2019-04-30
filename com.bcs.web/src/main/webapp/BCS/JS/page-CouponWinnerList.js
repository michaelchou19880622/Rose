$(function(){
	var WINDOW_DEIMENSIONS = "toolbars=no,menubar=no,location=no,scrollbars=yes,resizable=yes,status=yes";
	var COUPONID = $.urlParam("couponId");
	var recordTrTemplate = {};
	var messageDialog = $('#messageDialog');
	var rewardCardId;
	var pageIndex = 1;
	var maxPageNum = 0;

	
	var getMaxPageNum = function(cb){
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getWinnerMaxPage/'+COUPONID
		}).success(function(response){
			maxPageNum = response;
			$('#pageTotalText').html(maxPageNum);
			cb();
		})
		.fail(function(response){
			$.FailResponse(response);
		}).done(function(){
		});
	}
	
	var loadDataFunc = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getWinnerListByCouponId/'+COUPONID+'/'+pageIndex
		}).success(function(response){
			$('#pageText').val(pageIndex);
			$('#recordListTable').find('.recordTrTemplate').remove();
			for(var key in response){
				var valueObj = response[key];
				
				var winnerDetail = valueObj.winnerDetail;
				var couponCode = valueObj.couponCode;
				var recordTr = recordTrTemplate.clone(true);

				recordTr.find('.uid').html(winnerDetail.uid);
				recordTr.find('.userName').html(winnerDetail.userName);
				recordTr.find('.modifyTime').html(moment(winnerDetail.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
				recordTr.find('.userAddress').html(winnerDetail.userAddress);
				recordTr.find('.userIdCardNumber').html(winnerDetail.userIdCardNumber);
				recordTr.find('.userPhoneNumber').html(winnerDetail.userPhoneNumber);
				recordTr.find('.couponCode').html(couponCode?couponCode:'無');
				$('#recordListTable').append(recordTr);

				var startTime = new moment($('#recordListTable').find('.modifyTime:first').html(), "YYYY-MM-DD");
				var endTime = new moment($('#recordListTable').find('.modifyTime:last').html(), "YYYY-MM-DD");

				$('#recoredStartDate').val(startTime.format('YYYY-MM-DD'));
				$('#recordEndDate').val(endTime.format('YYYY-MM-DD'));

			}
			
			if($('#recoredStartDate').val()==="" || $('#recordEndDate').val()===""){
				$('#recoredStartDate').val(moment().format('YYYY-MM-DD'));
				$('#recordEndDate').val(moment().format('YYYY-MM-DD'))
			}
		}).fail(function(response){
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
		

	};
	
	$('.query').click(function(){
		if(!validateTimeRange()){
			return false;
		}
		var startDate = $('#recoredStartDate').val();
		var endDate = $('#recordEndDate').val();

		$('.LyMain').block($.BCS.blockMsgRead);
		$('#recordListTable').find('.recordTrTemplate').remove();
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getWinnerListByCouponId/'+COUPONID+'/'+startDate+'/'+endDate+'/'+pageIndex
		}).success(function(response){
			for(var key in response){
				var valueObj = response[key];
				
				var winnerDetail = valueObj.winnerDetail;
				var couponCode = valueObj.couponCode;
				var recordTr = recordTrTemplate.clone(true);

				recordTr.find('.uid').html(winnerDetail.uid);
				recordTr.find('.userName').html(winnerDetail.userName);
				recordTr.find('.modifyTime').html(moment(winnerDetail.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
				recordTr.find('.userAddress').html(winnerDetail.userAddress);
				recordTr.find('.userIdCardNumber').html(winnerDetail.userIdCardNumber);
				recordTr.find('.userPhoneNumber').html(winnerDetail.userPhoneNumber);
				recordTr.find('.couponCode').html(couponCode?couponCode:'無');
				$('#recordListTable').append(recordTr);

				var startTime = new moment($('#recordListTable').find('.modifyTime:first').html(), "YYYY-MM-DD");
				var endTime = new moment($('#recordListTable').find('.modifyTime:last').html(), "YYYY-MM-DD");

				$('#recoredStartDate').val(startTime.format('YYYY-MM-DD'));
				$('#recordEndDate').val(endTime.format('YYYY-MM-DD'));
			}
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	});
	
	var validateTimeRange = function(){

		var startDate = moment($('#recoredStartDate').val(), "YYYY-MM-DD");
		var endDate = moment($('#recordEndDate').val(), "YYYY-MM-DD");
		
		if (!startDate.isValid()) {
			alert("請選擇起始日期");
			return false;
		}
		if (!endDate.isValid()) {
			alert("請選擇結束日期");
			return false;
		}
		if (startDate.isAfter(endDate)) {
			alert("起始日不能大於結束日");
			return false;
		}
		
		return true;
	}
	
	$(".datepicker").datepicker({
		'dateFormat' : 'yy-mm-dd'
	});
	
	$('.exportPageToExcel').click(function(){
		var couponId = $.urlParam("couponId");
		var startDate = $('#recoredStartDate').val();
		var endDate = $('#recordEndDate').val();
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForWinnerListByCouponId?couponId=' + couponId + '&startDate=' + startDate + '&endDate=' + endDate+'&pageIndex='+pageIndex;
		window.open(url,"_blank",  WINDOW_DEIMENSIONS);
	});
	
	$('.exportAllToExcel').click(function(){
		var couponId = $.urlParam("couponId");
		var startDate = $('#recoredStartDate').val();
		var endDate = $('#recordEndDate').val();
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForWinnerListByCouponId?couponId=' + couponId + '&startDate=' + startDate + '&endDate=' + endDate;
		window.open(url,"_blank",  WINDOW_DEIMENSIONS);
	});
	
	$('.RightBtn').click(function(){
		if(pageIndex<maxPageNum){
			pageIndex++;
			loadDataFunc();
		}else{
			alert("選取頁數超出範圍");
		}
	});
	
	$('.LeftBtn').click(function(){
		if(pageIndex>1){
			pageIndex--;
			loadDataFunc();
		}else{
			alert("選取頁數超出範圍");
		};
	});
	
	$('#pageText').change(function(){
		if (/[^0-9\.-]/g.test(this.value)) {
            this.value = this.value.replace(/[^0-9\.-]/g, '');
        }
	});
	
	$('#pageText').keypress(function(event) {
		if (event.which === 13){
		    var pageText = this.value;
			if(pageText <= 0 || pageText > maxPageNum){
				console.info("over");
				alert("頁數超過範圍");
				return;
			}else{
				pageIndex = pageText;
				$('.query').trigger('click');
			}

		}
	});
	
	var initPage = function(){
		recordTrTemplate = $('.recordTrTemplate').clone(true);
		$('.recordTrTemplate').remove();
	}
	
	initPage();
	getMaxPageNum(function(){
		loadDataFunc();
	});

});