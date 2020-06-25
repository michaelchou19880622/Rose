/**
 * Pnp Block Single User History Page
 */

$(function() {
	var paramMobile = $.urlParam('mobile');
	console.info('paramMobile = ', paramMobile);
	
	
	// ---- Global Variables ----
	var originalTr;
	var originalTable;
	
	var eleTotalPageSize = document.getElementById('totalPageSize');
	var eleCurrentPageIndex = document.getElementById('currentPageIndex');

	var valPerPageSize = $(this).find('#perPageSizeSelector option:selected').val();
	console.info('init valPerPageSize = ', valPerPageSize);
	
	var valCurrentPageIndex;
	
	var valTotalPageSize = 0;
	
	var valStartDate = "";
	var valEndDate = "";
	var valMobile = paramMobile;
	var valInsertUser = "";
	var valGroupTag = "*";
	var valBlockEnable = 1;
	
	var hasData = false;
	
	/* 更新每頁顯示數量下拉選單 */
	var func_optionSelectChanged = function(){
		var selectValue = $(this).find('option:selected').text();
		
		$(this).closest('.optionPageSize').find('.optionLabelPageSize').html(selectValue);

		valCurrentPageIndex = 1;
		valPerPageSize = selectValue;
		
		console.info("valCurrentPageIndex = ", valCurrentPageIndex);
		console.info("valPerPageSize = ", valPerPageSize);
		
		document.getElementById("searchBtn").click();
	};

	$('.optionSelectPageSize').change(func_optionSelectChanged);

	/* 上/下頁按鈕 */
	var pageBtnHandler = function(condition, actionName) {
		if (condition) {
			valCurrentPageIndex = (actionName === 'next')? ++valCurrentPageIndex : --valCurrentPageIndex;
			console.log('Currency Page Number is ' + valCurrentPageIndex);
			
			currentPageIndex.innerText = valCurrentPageIndex;

			getListCount();
		}
	};
	
	// -------------------Event----------------------
	$('#btn_PreviousPage').click(function() {
		console.info('btn_PreviousPage');
		console.info('valCurrentPageIndex = ', valCurrentPageIndex);
		pageBtnHandler(valCurrentPageIndex > 1, 'back');
	});

	$('#btn_NextPage').click(function() {
		console.info('btn_NextPage');
		console.info('valCurrentPageIndex = ', valCurrentPageIndex);
		console.info('valTotalPageSize = ', valTotalPageSize);
		pageBtnHandler(valCurrentPageIndex < valTotalPageSize, 'next');
	});

	var getListCount = function() {
		
		console.info('valStartDate = ', valStartDate);
		console.info('valEndDate = ', valEndDate);
		console.info('valMobile = ', valMobile);
		console.info('valInsertUser = ', valInsertUser);
		console.info('valGroupTag = ', valGroupTag);
		console.info('valBlockEnable = ', valBlockEnable);

		$('.LyMain').block($.BCS.PnpBlock_dataLoading);
		
		// Get PNP Black List Count
		$.ajax({
			type : 'POST',
			url : bcs.bcsContextPath + '/pnpEmployee/getPnpBlockHistoryCount',
			contentType : 'application/json;charset=UTF-8',
			data : JSON.stringify({
				startDate : valStartDate,
				endDate : valEndDate,
				mobile : valMobile,
				insertUser : valInsertUser,
				groupTag : valGroupTag,
				blockEnable : valBlockEnable
			})
			
		}).done(function(response) {
			console.info('response:', response);
			
			var blockSendHistoryCount = response;
			console.info('blockSendHistoryCount = ', blockSendHistoryCount);

			$('.dataTemplate').remove();
			$('#noDataTxt').remove();
			
			if (blockSendHistoryCount > 0) {
				valCurrentPageIndex = 1;
				valTotalPageSize = Math.ceil(blockSendHistoryCount / valPerPageSize);

				eleCurrentPageIndex.innerText = valCurrentPageIndex;
				eleTotalPageSize.innerText = valTotalPageSize;
				
				document.getElementById("mainFrame").className = "mainFrame alignLeft"; 
				
				loadData();
			} else {
				document.getElementById("mainFrame").className = "mainFrame"; 
				
				$('#tableBody').append('<tr align="center" id="noDataTxt"><td colspan="7"><span style="color:red; text-align: center;">查無資料</span></td></tr>');

				eleCurrentPageIndex.innerText = '-';
				eleTotalPageSize.innerText = '-';

				$('.LyMain').unblock();
			}
		}).fail(function(response) {
			console.info(response);
			$.FailResponse(response);
			
			$('.dataTemplate').remove();
			$('#noDataTxt').remove();
			
			$('.LyMain').unblock();
		});
	};

	$('#exportBtn').click(function() {

		$('.LyMain').block($.BCS.blockMsgRead);
		
		if (!hasData) {
			alert("很抱歉，目前該用戶無資料可匯出！\n請返回排除發送中名單列表頁面重新進行選擇，謝謝。")
			
			$('.LyMain').unblock();
			return;
		}
		
		var getUrl = bcs.bcsContextPath + '/pnpEmployee/exportPNPBlockListReportExcel?'
										+ 'startDate=' + valStartDate 
										+ '&endDate=' + valEndDate 
										+ '&mobile=' + valMobile
										+ '&insertUser=' + valInsertUser 
										+ '&groupTag=' + valGroupTag
										+ '&blockEnable' + valBlockEnable;
		
		getUrl = encodeURI(getUrl);
//		console.info('getUrl = ', getUrl);
		
		window.location.href = getUrl;

		$('.LyMain').unblock();
	});
	
	var loadData = function() {
		
		cleanList();

		console.info('1-1 valCurrentPageIndex = ', valCurrentPageIndex);
		console.info('1-2 valPerPageSize = ', valPerPageSize);
		console.info('1-3 valStartDate = ', valStartDate);
		console.info('1-4 valEndDate = ', valEndDate);
		console.info('1-5 valMobile = ', valMobile);
		console.info('1-6 valInsertUser = ', valInsertUser);
		console.info('1-7 valGroupTag = ', valGroupTag);
		console.info('1-8 valBlockEnable = ', valBlockEnable);
		
		// Get PNP Black List
		$.ajax({
			type : 'POST',
			url : bcs.bcsContextPath + '/pnpEmployee/getPnpExcludeSendingHistoryList',
			contentType : 'application/json;charset=UTF-8',
			data : JSON.stringify({
				page : valCurrentPageIndex,
				pageCount : valPerPageSize,
				startDate : valStartDate,
				endDate : valEndDate,
				mobile : valMobile,
				insertUser : valInsertUser,
				groupTag : valGroupTag,
				blockEnable : valBlockEnable
			})
		}).done(function(response) {
			console.info('response = ', response);
//			console.log('JSON.stringify(response) = ', JSON.stringify(response));
			
			if (response.length == 0) {
				return false;
			}
			
			var i = 0;
			response.forEach(function(obj) {
				var list = originalTr.clone(true);
				
				list.find('.mobileNum').html(obj.mobile);
				list.find('.lineUID').html(obj.uid);
				list.find('.reason').html(obj.modifyReason);
				list.find('.updateTime').html(obj.modifyDateTime);
				
				var blockStatus = (obj.blockEnable == 1)? "排除中" : "取消排除";
				
				list.find('.status').html(blockStatus);
				list.find('.guestLabel').html(obj.groupTag);
				list.find('.modifier').html(obj.insertUser);
				
				$('#tableBody').append(list);
				
				i++;
			});
			
			if (i > 0) {
				hasData = true;
			}
			
			console.info('i = ', i);
			
			$('.LyMain').unblock();
			
		}).fail(function(response) {
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		});
	};
	
	var cleanList = function() {
		$('.dataTemplate').remove();
		$('.tableBody').remove();
	};

	// initialize Page
	var initPage = function() {
		originalTr = $('.dataTemplate').clone(true);
		originalTable = $('#tableBody').clone(true);
		
		cleanList();
	};

	
	initPage();
	getListCount();
});
