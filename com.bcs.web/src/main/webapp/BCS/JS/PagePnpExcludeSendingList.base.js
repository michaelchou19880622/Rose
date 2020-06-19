/**
 * Pnp analysis report
 */

$(function() {
	// ---- Global Variables ----
	
	var isSearchData = false;

	var originalTr;
	var originalTable;
	
	var listSummary;
	
	var eleCreateBtn = document.getElementById("createBtn");
	var eleTotalPageSize = document.getElementById('totalPageSize');
	var eleCurrentPageIndex = document.getElementById('currentPageIndex');
	
	var valPerPageSize = $(this).find('#perPageSizeSelector option:selected').val();
	
	var valCurrentPageIndex;
	
	var eleGroupTag = $(this).find('#guestLabelSelector option:selected');
	
	var valSelectedSearchType = $('[name="searchType"]:checked').val();
//	console.info('default selectedSearchType = ', selectedSearchType);
	
	var valTotalPageSize = 0;
	
	var valStartDate;
	var valEndDate;
	var valMobile;
	var valInsertUser;
	var valGroupTag;

	// result data
	var hasData = false;
	var firstFetch = true;
	
	var startDate = "";
	var endDate = "";
	var mobile = "";
	var insertUser = "";
	var groupTag = "";
	
	var isCreateTime = true, isOrderTime = false;
	
	var page = 1, totalPages = 0;
	var pnpStatusMap = {};
	var isChangePage = false;
	var totalDateCount = 0;
	
	function pad(number, length) {
		   
	    var str = '' + number;
	    while (str.length < length) {
	        str = '0' + str;
	    }
	   
	    return str;
	}


	/* 彈出視窗 Image Model */
	var model = document.getElementById("myModel");

	/* When the user clicks anywhere outside of the model, close the model */
	window.onclick = function(event) {
		if (event.target == model) {
			model.style.display = "none";
		}
	}
	
	window.addEventListener('click', function(e) {   
		if (event.target == model && document.getElementById('myModel').contains(e.target)){
			model.style.display = "none";
		}
	});

	/* When the user click 'ESC', close the model */
	$(document).keyup(function(e) {
		// Some browsers support 'which'(IE) others support 'keyCode' (Chrome...etc)
		var keycode = (e.keyCode ? e.keyCode : e.which);
		
		if (keycode == 27) {
			if (model.style.display === "block") {
				model.style.display = "none";
			}
		}
	});
	
	/* Defined the popup model for URL */
	var func_showCreateCancelPopupModel = function() {
		model.style.display = "block";
	};
	

	$('#createBtn').click(func_showCreateCancelPopupModel);
	
	
	/* 更新每頁顯示數量下拉選單 */
	var func_optionSelectChanged = function(){
		var selectValue = $(this).find('option:selected').text();
		
		$(this).closest('.optionPageSize').find('.optionLabelPageSize').html(selectValue);

		page = 1;
		perPageSize = selectValue;
		console.info("perPageSize = ", perPageSize);
		console.info("currentPageIndex.innerText = ", currentPageIndex.innerText);
		
		firstFetch = true;
		
		loadData();
	};

	$('.optionSelectPageSize').change(func_optionSelectChanged);

	/* 上/下頁按鈕 */
	var pageBtnHandler = function(condition, actionName) {
		if (condition) {
			page = actionName === 'next' ? ++page : --page;
			console.log('Currency Page Number is ' + page);
			
			currentPageIndex.innerText = page;
			
			isChangePage = true;
			
			loadData();
		}
	};
	
	var setupGuestLabelMap = function() {
		$.ajax({
			type : 'GET',
			url : bcs.bcsContextPath + '/pnpEmployee/getGuestLabelMap',
			contentType : 'application/json'
		}).success(function(response) {
			console.log(response);
			if (response !== null) {
				pnpStatusMap = response;
			}
		}).fail(function(response) {
			console.log(response);
		}).done(function(response) {
			console.log('getGuestLabelMap done!!');
		});
	};

	// -------------------Event----------------------
	$('.datepicker').datepicker({
		maxDate : 0,
		dateFormat : 'yy-mm-dd',
		changeMonth : true
	});

	$('#btn_PreviousPage').click(function() {
		pageBtnHandler(page > 1, 'back');
	});

	$('#btn_NextPage').click(function() {
		pageBtnHandler(page < totalPages, 'next');
	});

	// do Search
	$('#searchBtn').click(function() {
		
		valStartDate = $('#startDate').val();
		valEndDate = $('#endDate').val();
		valMobile = $('#mobileInput').val();
		valInsertUser = $('#insertUserInput').val();
		valGroupTag = eleGroupTag.val();
		
		if (!valStartDate || valStartDate == 'YYYY-MM-DD') {
			valStartDate = "";
		}
	
		if (!valEndDate || valEndDate == 'YYYY-MM-DD') {
			valEndDate = "";
		}
		
		if (valStartDate == "" && valEndDate != "") {
			valStartDate = valEndDate;
		}
		else if (valEndDate == "" && valStartDate != "") {
			valEndDate = valStartDate;
		}
		
		if (!valGroupTag || valGroupTag == '請選擇') {
			valGroupTag = "";
		}

		console.info('valStartDate = ', valStartDate);
		console.info('valEndDate = ', valEndDate);

		$('.LyMain').block($.BCS.blockMsgRead);
		
		// Get PNP Black List Count
		$.ajax({
			type : 'POST',
			url : bcs.bcsContextPath + '/pnpEmployee/getPnpBlockSendCount',
			contentType : 'application/json;charset=UTF-8',
			data : JSON.stringify({
				startDate : valStartDate,
				endDate : valEndDate,
				mobile : valMobile,
				insertUser : valInsertUser,
				groupTag : valGroupTag
			})
			
		}).done(function(response) {
			console.info('response:', response);
			
			var blockSendCount = response;
			console.info('blockSendCount = ', blockSendCount);

			$('.dataTemplate').remove();
			$('#noDataTxt').remove();
			
			if (blockSendCount > 0) {
				valCurrentPageIndex = 1;
				valTotalPageSize = Math.ceil(blockSendCount / valPerPageSize);

				eleCurrentPageIndex.innerText = valCurrentPageIndex;
				eleTotalPageSize.innerText = valTotalPageSize;
				
				document.getElementById("mainFrame").className = "mainFrame alignLeft"; 
				
				loadData();
			} else {
				document.getElementById("mainFrame").className = "mainFrame"; 
				
				$('#tableBody').append('<tr align="center" id="noDataTxt"><td colspan="8"><span style="color:red; text-align: center;">查無資料</span></td></tr>');

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
		
		
		
//		$.ajax({
//			type : 'POST',
//			url : bcs.bcsContextPath + '/pnpEmployee/getPnpExcludeSendingList',
//			contentType : 'application/json;charset=UTF-8',
//			data : JSON.stringify({
//				page : page,
//				pageCount : 10,
//				startDate : "",
//				endDate : "",
//				mobile : "",
//				insertUser : "",
//				groupTag : ""
//			})
//			
//		}).done(function(response) {
//			console.info('response:', response);
////			console.log('response:', JSON.stringify(response));
//			
//			$('.dataTemplate').remove();
//			$('#noDataTxt').remove();
//			
//			if (response.length == 0) {
//				$('#dataTemplateSummary').remove();
//				$('#tableBody').append('<tr id="noDataTxt"><td colspan="8"><span style="color:red">查無資料</span></td></tr>');
//				currentPageIndex.innerText = '-';
//				totalPageSize.innerText = '-';
//				$('.LyMain').unblock();
//				return false;
//			}
//			
//			var i = 1;
//			response.forEach(function(obj) {
//				var list = originalTr.clone(true);
//				
//				list.find('.mobileNum').html(obj.phone);
//				list.find('.lineUID').html(obj.uid);
//				list.find('.reason').html(obj.modifyReason);
//				list.find('.updateTime').html(obj.modifyTime);
//				list.find('.status').html(obj.blockEnable);
//				list.find('.guestLabel').html(obj.groupTag);
//				list.find('.modifier').html(obj.insertUser);
//				
//				$('#tableBody').append(list);
//				
//				i++;
//			});
//			
//			console.info('i = ', i);
//			
//		}).fail(function(response) {
//			console.info(response);
//			$.FailResponse(response);
//			$('.LyMain').unblock();
//		});
		
		// For Test
//		for (testIdx = 0; testIdx < 5; testIdx++) {
//			var list = originalTr.clone(true);
//			
//			list.find('.mobileNum').html('0912' + pad(Math.floor(Math.random() * 999999), 6));
//			list.find('.lineUID').html('Utesttestlineuid000000' + pad(Math.floor(Math.random() * 999999), 6));
//			list.find('.reason').html('第 ' + pad(Math.floor(Math.random() * 999999), 6) + ' 號奧客...');
//			list.find('.updateTime').html('2020-05-27 13:55:40');
//			list.find('.status').html('排除中');
//			list.find('.guestLabel').html('24');
//			list.find('.modifier').html('1000' + pad(Math.floor(Math.random() * 999), 3));
//			$('#tableBody').append(list);
//		};
		
		
//		isSearchData = true;
//		
//		if (dataValidate()) {
//			cleanList();
//			
//			page = 1;
//			
//			startDate = $('#startDate').val();
////			console.info('startDate = ', startDate);
//			
//			endDate = $('#endDate').val();
////			console.info('endDate = ', endDate);
//			
//			loadData();
//		}
	});

	$('#exportBtn').click(function() {

		$('.LyMain').block($.BCS.blockMsgRead);
		
		console.log('Has data : ', hasData);
		
		if (!hasData) {
			
			if (!isSearchData) {
				alert("很抱歉，您尚未進行資料查詢，無法匯出資料！\n請先進行資料查詢，謝謝。")
			} else {
				alert("目前無資料可匯出！\n請重新進行查詢，謝謝。")
			}
			
			$('.LyMain').unblock();
			return;
		}
		
		var type = isCreateTime ? 'createTime' : 'orderTime';
		
		var getUrl = bcs.bcsContextPath + '/pnpEmployee/exportPNPStsReportExcel?'
										+ 'dateType=' + selectedSearchType 
										+ '&startDate=' + startDate 
										+ '&endDate=' + endDate 
										+ '&isPageable=false' 
										+ '&page=1'
										+ '&account=' + document.getElementById('accountInput').value 
										+ '&pccCode=' + document.getElementById('pccCodeInput').value 
										+ '&sourceSystem=' + ''
										+ '&employeeId=' + ''
										+ '&phone=' + ''
										+ '&pageCount=' + totalDateCount;
		
		getUrl = encodeURI(getUrl);
//		console.info('getUrl: ' + getUrl);
		
		window.location.href = getUrl;

		$('.LyMain').unblock();
	});
	
//	var dataValidate = function() {
//		startDate = $('#startDate').val();
//		endDate = $('#endDate').val();
//		
//		console.info('startDate = ', startDate);
//		console.info('endDate = ', endDate);
//		
//		if (!startDate || startDate == 'YYYY-MM-DD') {
//			alert('請填寫起始日期！');
//			return false;
//		}
//		
//		if (!endDate || endDate == 'YYYY-MM-DD') {
//			alert('請填寫結束日期！');
//			return false;
//		}
//		
//		if (!moment(startDate).add(184, 'days').isAfter(moment(endDate))) {
//			alert('起始日期與結束日期之間不可相隔超過6個月！');
//			return false;
//		}
//		
//		if (moment(startDate).isAfter(moment(endDate))) {
//			alert('起始日期不可大於結束日期！');
//			return false;
//		}
//		
//		firstFetch = true;
//		return true;
//	};

	var setExportButtonSource = function() {
		console.log('Has data : ', hasData);
		
		if (hasData) {
			var getUrl = bcs.bcsContextPath + '/pnpEmployee/exportPNPDetailReportExcel' 
											+ '?startDate=' + startDate 
											+ '&endDate=' + endDate 
											+ '&isPageable=false' 
											+ '&page=' + page 
											+ '&account=' + document.getElementById('accountInput').value 
											+ '&pccCode=' + document.getElementById('pccCodeInput').value 
											+ '&sourceSystem=' + document.getElementById('sourceSystemInput').value 
											+ '&phone=' + document.getElementById('phoneNumber').value 
											+ '&dateType=' + ((isCreateTime) ? 'createTime' : 'orderTime');
			console.info('getUrl', getUrl);

			$('.btn_add.exportToExcel').attr('href', getUrl);
		} else {
			$('.btn_add.exportToExcel').attr('href', '#');
		}
	};

	var loadData = function() {

		console.info('valStartDate = ', valStartDate);
		console.info('valEndDate = ', valEndDate);
		
		// Get PNP Black List
		$.ajax({
			type : 'POST',
			url : bcs.bcsContextPath + '/pnpEmployee/getPnpExcludeSendingList',
			contentType : 'application/json;charset=UTF-8',
			data : JSON.stringify({
				page : valCurrentPageIndex,
				pageCount : valPerPageSize,
				startDate : valStartDate,
				endDate : valEndDate,
				mobile : valMobile,
				insertUser : valInsertUser,
				groupTag : valGroupTag
			})
		}).done(function(response) {
			console.info('response = ', response);
			console.log('JSON.stringify(response) = ', JSON.stringify(response));
			
			if (response.length == 0) {
				return false;
			}
			
			var i = 1;
			response.forEach(function(obj) {
				var list = originalTr.clone(true);
				
				list.find('.mobileNum').html(obj.phone);
				list.find('.lineUID').html(obj.uid);
				list.find('.reason').html(obj.modifyReason);
				list.find('.updateTime').html(obj.createTime);
				list.find('.status').html(obj.blockEnable);
				list.find('.guestLabel').html(obj.groupTag);
				list.find('.modifier').html(obj.insertUser);
				
				$('#tableBody').append(list);
				
				i++;
			});
			
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
		$('.dataTemplateSummary').remove();
		$('.tableBody').remove();
		$('.tableBodySmmary').remove();
//		console.log('Result List Remove!!');
	};

	// initialize Page
	var initPage = function() {
		originalTr = $('.dataTemplate').clone(true);
		originalTable = $('#tableBody').clone(true);

		originalTrSummary = $('.dataTemplateSummary').clone(true);
		originalTableSummary = $('#tableBodySummary').clone(true);
		
		cleanList();
		
		$('#startDate').val('YYYY-MM-DD');
		$('#endDate').val('YYYY-MM-DD');
		
//		startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
//		startDate = moment(new Date()).format('YYYY-MM-DD');
//		endDate = moment(new Date()).format('YYYY-MM-DD');
//		console.info('initPage : startDate = ', startDate);
//		console.info('initPage : endDate = ', endDate);
//		$('#startDate').val(startDate);
//		$('#endDate').val(endDate);
		
	};

	initPage();
});
