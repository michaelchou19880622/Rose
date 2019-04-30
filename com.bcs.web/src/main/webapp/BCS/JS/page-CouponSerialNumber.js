$(function(){
	var COUPONID = $.urlParam("couponId");
	var recordTrTemplate = {};
	var messageDialog = $('#messageDialog');
	var rewardCardId;
	var pageIndex = 1;
	var maxPageNum = 100;
	
	var getMaxPageNum = function(cb){
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getCouponSerialMaxPage/'+COUPONID
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
		console.info("loadDataFunc");
		$('.LyMain').block($.BCS.blockMsgRead);
		$('#recordListTable').find('.recordTrTemplate').remove();
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getCouponCodeList/'+COUPONID+'/'+ pageIndex
		}).success(function(response){
			$('#pageText').val(pageIndex);

			for(var key in response){
				var valueObj = response[key];
				var recordTr = recordTrTemplate.clone(true);

				recordTr.find('.couponSerialNumber').html(valueObj["couponCode"]);
				recordTr.find('.isUsed').html(valueObj["status"] === 'IS_USED'?'已使用':'未使用');
				var time = valueObj["modifyTime"].replace(/\.\d+$/, ''); // 刪去毫秒
				recordTr.find('.modifyTime').html(time);
				recordTr.find('.modifyUser').html(valueObj["modifyUser"]);
				$('#recordListTable').append(recordTr);
				
				var startTime = new moment($('#recordListTable').find('.modifyTime:first').html(), "YYYY-MM-DD");
				var endTime = new moment($('#recordListTable').find('.modifyTime:last').html(), "YYYY-MM-DD");
				
				$('#recoredStartDate').val(startTime.format('YYYY-MM-DD'));
				$('#recordEndDate').val(endTime.format('YYYY-MM-DD'));
			}
		}).fail(function(response){
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};
	
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

	$('#pageText').keypress(function(event) {
		if (event.which === 13){
			console.log("keypress");
		    var pageText = this.value;
			if(pageText <= 0 || pageText > maxPageNum){
				alert("頁數超過範圍");
				return;
			}
			pageIndex = pageText;
		    loadDataFunc();
		}
	});
	
	var initPage = function(){
		recordTrTemplate = $('.recordTrTemplate').clone(true);
		$('.recordTrTemplate').remove();
	}
	
	initPage();
	getMaxPageNum(function(){
		loadDataFunc()
	});
});