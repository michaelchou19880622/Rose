$(function(){
	var WINDOW_DEIMENSIONS = "toolbars=no,menubar=no,location=no,scrollbars=yes,resizable=yes,status=yes";

	var today = new Date();
	var gameId = $.urlParam("gameId");;
	var couponPrizeId = $.urlParam("couponPrizeId");
	var UID = "";
	var winnerTrTemplate = {};
	var detailDialog = $('#detailDialog');
	var messageDialog = $('#messageDialog');
	var pageIndex = 1;
	var maxPageNum = 0;
	
	var getMaxPageNum = function(cb){
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getGameWinnerMaxPage?'+'gameId='+ gameId + 
					((couponPrizeId!==null)?'&couponId='+ couponPrizeId:'')
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
				
		/* 初始化 datepicker */
		$('#winnerListStartDate').datepicker({
			dateFormat : 'yy-mm-dd', 
			changeYear : true,
	        changeMonth : true
		});
		
		$('#winnerListEndDate').datepicker({
			dateFormat : 'yy-mm-dd', 
			changeYear : true,
	        changeMonth : true
		});
		
		console.log(moment(today).format('YYYY-MM-DD'));
		$('#winnerListEndDate').val(moment(today).format('YYYY-MM-DD'));
				
		$('#winnerListEndDate').val(moment(today).format('YYYY-MM-DD'));
		console.info(couponPrizeId);
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getWinnerList/' + gameId + '/' + couponPrizeId+'/'+ pageIndex
		}).success(function(response){
			console.info(response);
			$('#pageText').val(pageIndex);
			$('#winnerListTable').find('.winnerTrTemplate').remove();
			
			if(response.length > 0){
				$('#winnerListStartDate').val(moment(response[0].actionTime).format('YYYY-MM-DD'));
				
				for(var key in response){
					var winnerTr = winnerTrTemplate.clone(true);
					
					winnerTr.find('.name').html(response[key].userMID);
					winnerTr.find('.prizeName').html(response[key].couponTitle);
					winnerTr.find('.modifyTime').html(moment(response[key].actionTime).format('YYYY-MM-DD HH:mm:ss'));
					winnerTr.find('.UID').val(response[key].userMID);
					
					if(response[key].isFillIn)
						winnerTr.find('.winnerId').val(response[key].winnerDetail.winnerListId);
					else {
						winnerTr.find('.winnerId').val(null);
						winnerTr.find('.btn_detail').hide();
					}
	
					$('#winnerListTable').append(winnerTr);
				}
			} else {
				$('#winnerListStartDate').val(moment(today).format('YYYY-MM-DD'));
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
		var startDate = $('#winnerListStartDate').val();
		var endDate = $('#winnerListEndDate').val();
		
		$('.LyMain').block($.BCS.blockMsgRead);
		
		$('#winnerListTable').find('.winnerTrTemplate').remove();
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getWinnerList/' + gameId + '/' + couponPrizeId + '/' + startDate+'/' + endDate+'/'+ pageIndex
		}).success(function(response){
			for(var key in response){
				var winnerTr = winnerTrTemplate.clone(true);
				
				winnerTr.find('.name').html(response[key].userMID);
				winnerTr.find('.prizeName').html(response[key].couponTitle);
				winnerTr.find('.modifyTime').html(moment(response[key].actionTime).format('YYYY-MM-DD HH:mm:ss'));
				winnerTr.find('.UID').val(response[key].userMID);
				
				if(response[key].isFillIn)
					winnerTr.find('.winnerId').val(response[key].winnerDetail.winnerListId);
				else {
					winnerTr.find('.winnerId').val(null);
					winnerTr.find('.btn_detail').hide();
				}

				$('#winnerListTable').append(winnerTr);
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

		var startDate = moment($('#winnerListStartDate').val(), "YYYY-MM-DD");
		var endDate = moment($('#winnerListEndDate').val(), "YYYY-MM-DD");
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
	
	var initPage = function(){
		winnerTrTemplate = $('.winnerTrTemplate').clone(true);
		$('.winnerTrTemplate').remove();
	}
	
	$('.btn_detail').click(function(e) {
		var winnerTr = $(this).closest(".winnerTrTemplate");
		var selectedWinnerId = winnerTr.find('.winnerId').val();
		
		if(selectedWinnerId){
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/edit/getWinnerDetail/' + selectedWinnerId
			}).success(function(response){
				detailDialog.find('.userName').html(response.userName);
				detailDialog.find('.userIdCardNumber').html(response.userIdCardNumber);
				detailDialog.find('.userPhoneNumber').html(response.userPhoneNumber);
				//detailDialog.find('.userEmailAddress').html(response.userEMail);
				detailDialog.find('.userAddress').html(response.userAddress);
				
				detailDialog.dialog('open');
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});		
		} else {
			detailDialog.find('.userName').html('尚未填寫');
			detailDialog.find('.userIdCardNumber').html('尚未填寫');
			detailDialog.find('.userPhoneNumber').html('尚未填寫');
			//detailDialog.find('.userEmailAddress').html(response.userEMail);
			detailDialog.find('.userAddress').html('尚未填寫');
			
			detailDialog.dialog('open');
		}
	});
	
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
	
	var sendingMsgFunc = function(actionType){
		// 設定傳送資料
		var sendMsgModel = {};
		sendMsgModel.actionType = actionType;
		var sendMsgDetails = [];
		var content = {
				'Text' : $('#messageDialog').find('#messageText').val()
		}
		sendMsgDetails.push({
			detailType : "TEXT",
			detailContent : JSON.stringify(content)
		});
		
		sendMsgModel.sendMsgDetails = sendMsgDetails;
		
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath +'/edit/sendToWinner?UID='+UID,
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(sendMsgModel)
		}).success(function(response){
			alert('傳送成功');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};
	
	$('#detailDialog').dialog({
    	autoOpen: false,
		resizable: false, //不可縮放
		modal: true, //畫面遮罩
		draggable: false, //不可拖曳
    	minWidth : 750,
    	position: { my: "top", at: "top", of: window  }
    });
	
	$('#messageDialog').dialog({
    	autoOpen: false,
		resizable: false, //不可縮放
		modal: true, //畫面遮罩
		draggable: false, //不可拖曳
    	minWidth : 750,
    	position: { my: "top", at: "top", of: window  }
    });
	$('.exportPageToExcel').click(function(){
		var startDate = $('#winnerListStartDate').val();
		var endDate = $('#winnerListEndDate').val();
		
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForWinnerList?gameId=' + gameId + '&startDate=' + startDate + '&endDate=' + endDate+'&pageIndex='+pageIndex;
		(couponPrizeId !== null)? url += '&couponPrizeId=' + couponPrizeId:null;
		window.open(url,"_blank",  WINDOW_DEIMENSIONS);
	})
	$('.exportAllToExcel').click(function(){
		var startDate = $('#winnerListStartDate').val();
		var endDate = $('#winnerListEndDate').val();
		
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForWinnerList?gameId=' + gameId  + '&startDate=' + startDate + '&endDate=' + endDate;
		(couponPrizeId !== null)? url += '&couponPrizeId=' + couponPrizeId:null;

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