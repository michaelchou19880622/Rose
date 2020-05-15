/**
 * Pnp analysis report
 */

$(function() {
	// ---- Global Variables ----

	var originalTr;
	var originalTable;
	
	var listSummary;
	
	var totalPageSize = document.getElementById('totalPageSize');
	var currentPageIndex = document.getElementById('currentPageIndex');
	var perPageSize = $(this).find('option:selected').text();
	console.info('perPageSize = ', perPageSize);
	
	var selectedSearchType = $('[name="searchType"]:checked').val();
	console.info('default selectedSearchType = ', selectedSearchType);
	
	var valTotalPageSize = 0;

	// result data
	var hasData = false;
	var firstFetch = true;
	var startDate = null, endDate = null;
	var isCreateTime = true, isOrderTime = false;
	var page = 1, totalPages = 0;
	var pnpStatusMap = {};
	var isChangePage = false;
	
	/* 更新每頁顯示數量下拉選單 */
	var func_optionSelectChanged = function(){
		var selectValue = $(this).find('option:selected').text();
		
		$(this).closest('.optionPageSize').find('.optionLabelPageSize').html(selectValue);
		
		perPageSize = selectValue;
		console.info("perPageSize = ", perPageSize);
		console.info("currentPageIndex.innerText = ", currentPageIndex.innerText);
		
		firstFetch = true;
		
		page = 1;
		
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

	var getPnpStatusMap = function() {
		$.ajax({
			type : 'GET',
			url : bcs.bcsContextPath + '/pnpEmployee/getPnpStatusEnum',
			contentType : 'application/json'
		}).success(function(response) {
			console.log(response);
			if (response !== null) {
				pnpStatusMap = response;
			}
		}).fail(function(response) {
			console.log(response);
		}).done(function(response) {
			console.log('Fetch PnpStatusMap done!!');
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
		if (dataValidate()) {
			cleanList();
			
			page = 1;
			
			startDate = $('#startDate').val();
			console.info('startDate = ', startDate);
			
			endDate = $('#endDate').val();
			console.info('endDate = ', endDate);
			
			loadData();
		}
	});

	$('#exportBtn').click(function() {
		// setExportButtonSource();
		if (hasData) {
			var type = isCreateTime ? 'createTime' : 'orderTime';
			var getUrl = bcs.bcsContextPath + '/pnpEmployee/exportPNPDetailReportExcel' + '?startDate=' + startDate + '&endDate=' + endDate + '&isPageable=false' + '&page=' + page
					+ '&account=' + document.getElementById('accountInput').value + '&pccCode=' + document.getElementById('pccCodeInput').value + '&sourceSystem='
					+ document.getElementById('sourceSystemInput').value + '&phone=' + document.getElementById('phoneNumber').value + '&dateType=' + type;
			console.info('getUrl: ' + getUrl);
			window.location.href = getUrl;
		}

	});
	
	// 發送類型
	$('[name="searchType"]').click(function() {
		selectedSearchType = $('[name="searchType"]:checked').val();
		console.info('selectedSearchType = ', selectedSearchType);
	});

	var dataValidate = function() {
		startDate = $('#startDate').val();
		endDate = $('#endDate').val();
		if (!startDate) {
			alert('請填寫起始日期！');
			return false;
		}
		if (!endDate) {
			alert('請填寫結束日期！');
			return false;
		}
		if (!moment(startDate).add(183, 'days').isAfter(moment(endDate))) {
			alert('起始日期與結束日期之間不可相隔超過6個月！');
			return false;
		}
		if (moment(startDate).isAfter(moment(endDate))) {
			alert('起始日期不可大於結束日期！');
			return false;
		}
		firstFetch = true;
		return true;
	};

	// do Download
	var setExportButtonSource = function() {
		if (hasData) {
			var getUrl = bcs.bcsContextPath + '/pnpEmployee/exportPNPDetailReportExcel' + '?startDate=' + startDate + '&endDate=' + endDate + '&isPageable=false' + '&page=' + page + '&account='
					+ document.getElementById('accountInput').value + '&pccCode=' + document.getElementById('pccCodeInput').value + '&sourceSystem='
					+ document.getElementById('sourceSystemInput').value + '&phone=' + document.getElementById('phoneNumber').value + '&dateType=' + isCreateTime ? 'createTime' : 'orderTime';
			console.info('getUrl', getUrl);

			$('.btn_add.exportToExcel').attr('href', getUrl);
		} else {
			$('.btn_add.exportToExcel').attr('href', '#');
		}
	};

	// ---- Initialize Page & Load Data ----
	// get List Data
	var loadData = function() {
		console.info('firstFetch:', firstFetch);
		
		if (firstFetch || isChangePage) {
			$('.LyMain').block($.BCS.blockMsgRead);
			isChangePage = false;
		}
		
		var getUrl = bcs.bcsContextPath + '/pnpEmployee/getPNPStsRptDetail';
		console.info('getUrl detail', getUrl);

		$.ajax({
			type : 'POST',
			url : getUrl,
			contentType : 'application/json;charset=UTF-8',
			data : JSON.stringify({
				dateType : selectedSearchType,
				startDate : startDate,
				endDate : endDate,
				isPageable : true,
				page : page,
				account : document.getElementById('accountInput').value,
				pccCode : document.getElementById('pccCodeInput').value,
				sourceSystem : null,
				employeeId : null,
				phone : null,
				pageCount : perPageSize
			})
		}).done(function(response) {
//			console.info('response:', response);
//			console.log('response:', JSON.stringify(response));
			
			$('.dataTemplate').remove();
			$('#noDataTxt').remove();
			if (response.length == 0) {
				$('#tableBodySummary').remove();
				$('#tableBody').append('<tr id="noDataTxt"><td colspan="15"><span style="color:red">查無資料</span></td></tr>');
				return false;
			}
			
			var i = 1;
			response.forEach(function(obj) {
				var list = originalTr.clone(true);
				
				list.find('.send_date').html(obj.send_date);
				list.find('.total').html(obj.total);
				list.find('.sms_total').html(obj.sms_total);
				list.find('.sms_ok').html(obj.sms_ok);
				list.find('.sms_no').html(obj.sms_no);
				list.find('.sms_point').html(obj.sms_point);
				list.find('.sms_rate').html(obj.sms_rate);
				list.find('.pnp_total').html(obj.pnp_total);
				list.find('.pnp_ok').html(obj.pnp_ok);
				list.find('.pnp_no').html(obj.pnp_no);
				list.find('.pnp_rate').html(obj.pnp_rate);
				list.find('.bc_total').html(obj.total);
				list.find('.bc_ok').html(obj.bc_ok);
				list.find('.bc_no').html(obj.bc_no);
				list.find('.bc_rate').html(obj.bc_rate);
				$('#tableBody').append(list);
				i++;
			});
			
			hasData = i > 0;
			console.log('Has data : ' + hasData);
			
			if (firstFetch) {
				fetchListCountAndChange();
			} else {
				$('.LyMain').unblock();
			}
			
			// setExportButtonSource();
		}).fail(function(response) {
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		});
	};

	/* 取得分頁總數並變更畫面 */
	var fetchListCountAndChange = function() {
		firstFetch = false;
		
		var getUrl = bcs.bcsContextPath + '/pnpEmployee/getPNPStsRptSummary';
		console.info('getUrl summary = ', getUrl);

		$.ajax({
			type : 'POST',
			url : getUrl,
			contentType : 'application/json;charset=UTF-8',
			data : JSON.stringify({
				dateType : selectedSearchType,
				startDate : startDate,
				endDate : endDate,
				isPageable : false,
				page : page,
				account : document.getElementById('accountInput').value,
				pccCode : document.getElementById('pccCodeInput').value,
				sourceSystem : null,
				employeeId : null,
				phone : null,
				pageCount : perPageSize
			})
		}).done(function(response) {
			console.info('fetchListCountAndChange = ', response);

			$('.dataTemplateSummary').remove();
			
			response.forEach(function(obj) {
				listSummary = originalTrSummary.clone(true);
				listSummary.find('.send_date').html(obj.send_date);
				listSummary.find('.total').html(obj.total);
				listSummary.find('.sms_total').html(obj.sms_total);
				listSummary.find('.sms_ok').html(obj.sms_ok);
				listSummary.find('.sms_no').html(obj.sms_no);
				listSummary.find('.sms_point').html(obj.sms_point);
				listSummary.find('.sms_rate').html(obj.sms_rate);
				listSummary.find('.pnp_total').html(obj.pnp_total);
				listSummary.find('.pnp_ok').html(obj.pnp_ok);
				listSummary.find('.pnp_no').html(obj.pnp_no);
				listSummary.find('.pnp_rate').html(obj.pnp_rate);
				listSummary.find('.bc_total').html(obj.total);
				listSummary.find('.bc_ok').html(obj.bc_ok);
				listSummary.find('.bc_no').html(obj.bc_no);
				listSummary.find('.bc_rate').html(obj.bc_rate);
				
				totalPages = parseInt(Math.ceil(obj.date_count/perPageSize));
				console.info('1-1 totalPages = ', totalPages);
			});

			$('#tableBodySummary').append(listSummary);

			console.info('page = ', page);
			console.info('1-2 totalPages = ', totalPages);

			if (totalPages == 0) {
				currentPageIndex.innerText = '-';
				totalPageSize.innerText = '-';
			} else {
				currentPageIndex.innerText = page;
				totalPageSize.innerText = totalPages;
			}
			
			$('.LyMain').unblock();
			
		}).fail(function(response) {
			console.log(response);
			$.FailResponse(response);
		});
	};

	var cleanList = function() {
		$('.dataTemplate').remove();
		$('.dataTemplateSummary').remove();
		$('.tableBody').remove();
		$('.tableBodySmmary').remove();
		console.log('Result List Remove!!');
	};

	// initialize Page
	var initPage = function() {
		originalTr = $('.dataTemplate').clone(true);
		originalTable = $('#tableBody').clone(true);

		originalTrSummary = $('.dataTemplateSummary').clone(true);
		originalTableSummary = $('#tableBodySummary').clone(true);
		
		cleanList();
		// startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
		startDate = moment(new Date()).format('YYYY-MM-DD');
		endDate = moment(new Date()).format('YYYY-MM-DD');
		$('#startDate').val(startDate);
		$('#endDate').val(endDate);
		// getPnpStatusMap();
		
	};

	initPage();
});
