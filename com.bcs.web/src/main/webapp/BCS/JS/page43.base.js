$(function(){
	var WINDOW_DEIMENSIONS = "toolbars=no,menubar=no,location=no,scrollbars=yes,resizable=yes,status=yes";

	var recordTrTemplate = {};
	var detailDialog = $('#detailDialog');
	var messageDialog = $('#messageDialog');
	var rewardCardId = $.urlParam("rewardCardId"); //從列表頁導過來的參數;
	var pageIndex = 1;
	var maxPageNum = 0;
	
	var getMaxPageNum = function(cb){
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getRewardCardRecordMaxPage?rewardCardId='+rewardCardId
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
			url : bcs.bcsContextPath + '/edit/getRecordList/'+rewardCardId+'/'+pageIndex
		}).success(function(response){
			$('#pageText').val(pageIndex);
			$('#recordListTable').find('.recordTrTemplate').remove();
			for(var key in response){
				var recordTr = recordTrTemplate.clone(true);
				var valueObj = response[key];
				var pointType ='';
				console.log(valueObj);
				recordTr.find('.cardId').val(valueObj["cardId"]);
				recordTr.find('.mid').html(valueObj["mid"]);
				recordTr.find('.pointGetAmount').html(valueObj["pointGetAmount"]);
				var time = valueObj["pointGetTime"].replace(/\.\d+$/, ''); // 刪去毫秒
				recordTr.find('.pointGetTime').html(time);
				console.log(valueObj["pointType"]);
				if(valueObj["pointType"]==='MANUAL')
					pointType='手動補點';
				else if(valueObj["pointType"]==='AUTOMATIC')
					pointType='使用者掃描';
				else if(valueObj["pointType"]==='SYSTEM')
					pointType='系統回饋';
				
				recordTr.find('.pointType').html(pointType);
				
				$('#recordListTable').append(recordTr);
				
				var startTime = new moment($('#recordListTable').find('.pointGetTime:first').html(), "YYYY-MM-DD");
				var endTime = new moment($('#recordListTable').find('.pointGetTime:last').html(), "YYYY-MM-DD");
				console.info("startTime");
				$('#recoredStartDate').val(startTime.format('YYYY-MM-DD'));
				$('#recordEndDate').val(endTime.format('YYYY-MM-DD'));
				console.info("recoredStartDate",$('#recoredStartDate'));
			}
			
			if($('#recoredStartDate').val()==="" || $('#recordEndDate').val()===""){
				$('#recoredStartDate').val(moment().format('YYYY-MM-DD'));
				$('#recordEndDate').val(moment().format('YYYY-MM-DD'))
			}
		}).fail(function(response){
			console.info(response);
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
			url : bcs.bcsContextPath + '/edit/getRecordList/'+rewardCardId+'/'+startDate+'/'+endDate+'/'+pageIndex
		}).success(function(response){
			console.log(response);
			for(var key in response){
				
				var recordTr = recordTrTemplate.clone(true);
				var valueObj = response[key];
				var pointType='';
				recordTr.find('.cardId').val(valueObj["cardId"]);
				recordTr.find('.mid').html(valueObj["mid"]);
				recordTr.find('.pointGetAmount').html(valueObj["pointGetAmount"]);
				var time = valueObj["pointGetTime"].replace(/\.\d+$/, ''); // 刪去毫秒
				recordTr.find('.pointGetTime').html(time);
				if(valueObj["pointType"]==='MANUAL')
					pointType='手動補點';
				else if(valueObj["pointType"]==='AUTOMATIC')
					pointType='使用者掃描';
				else if(valueObj["pointType"]==='SYSTEM')
					pointType='系統回饋';
				
				recordTr.find('.pointType').html(pointType);
				
				$('#recordListTable').append(recordTr);
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
	
	var initPage = function(){
		recordTrTemplate = $('.recordTrTemplate').clone(true);
		$('.recordTrTemplate').remove();
	}
	
	$('.btn_message').click(function(e) {
		var winnerTr = $(this).closest(".winnerTrTemplate");
		UID = winnerTr.find('.UID').val();
		messageDialog.find('#messageText').val('');
		
		messageDialog.dialog('open');
	});
	
	$('.sendMessage').click(function() {
		if($('#messageDialog').find('#messageText').val() == ""){
			alert('請輸入訊息內容');
			return false;
		}
		
		sendingMsgFunc("SendMsg");
	});
	
	$('.exportPageToExcel').click(function(){
		var startDate = $('#recoredStartDate').val();
		var endDate = $('#recordEndDate').val();
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForRewardRecord?rewardCardId=' + rewardCardId + '&startDate=' + startDate + '&endDate=' + endDate +'&pageIndex='+pageIndex;
		window.open(url,"_blank",  WINDOW_DEIMENSIONS);
	});
	
	$('.exportAllToExcel').click(function(){
		var couponId = $.urlParam("couponId");
		var startDate = $('#recoredStartDate').val();
		var endDate = $('#recordEndDate').val();
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForRewardRecord?rewardCardId=' + rewardCardId + '&startDate=' + startDate+ '&endDate=' + endDate;
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
	
	initPage();
	getMaxPageNum(function(){
		loadDataFunc();
	});
});