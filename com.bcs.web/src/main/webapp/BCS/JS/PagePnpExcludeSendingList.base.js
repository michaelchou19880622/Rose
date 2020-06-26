/**
 * Pnp Block List Page
 */

$(function() {
	// ---- Global Variables ----
	var isSearchData = false;

	var originalTr = {};
	var originalTable;
	
	var eleModelBtnOK = document.getElementById("btn_OK");
	var eleModelBtnCancel = document.getElementById('btn_Cancel');
	
	var eleCreateBtn = document.getElementById("createBtn");
	var eleTotalPageSize = document.getElementById('totalPageSize');
	var eleCurrentPageIndex = document.getElementById('currentPageIndex');

	var eleDialogMobileInput = document.getElementById('dialogMobileInput');
	var eleDialogModifyReasonInput = document.getElementById('dialogModifyReasonInput');
	
	var elePnpBlockTagSelector = document.getElementById("pnpBlockTagSelector");
	
	var valPerPageSize = $(this).find('#perPageSizeSelector option:selected').val();
	
	var valCurrentPageIndex;
	
	var valTotalPageSize = 0;
	
	var valStartDate;
	var valEndDate;
	var valMobile;
	var valInsertUser;
	var valGroupTag;
	
	var valMobileInput;
	var valModifyReasonInput;

	var hasData = false;
	
	var startDate = "";
	var endDate = "";
	var mobile = "";
	var insertUser = "";
	var groupTag = "";
	
	var blockSetType;
	
	var isChangePage = false;
	
	/* 彈出視窗 Image Model */
	var model = document.getElementById("myModel");

	/* Defined the popup model for URL */
	var func_showCreatePopupModel = function() {
		model.style.display = "block";

		eleDialogMobileInput.value = "";
		eleDialogMobileInput.readOnly = false;
		
		eleDialogModifyReasonInput.value = "";
		
		blockSetType = 'create';
	};
	
	var func_showCancelPopupModel = function() {
		model.style.display = "block";
		
		paramMobile = $(this).attr('mobile');
		console.info('paramMobile = ', paramMobile);
		
		eleDialogMobileInput.value = paramMobile;
		eleDialogMobileInput.readOnly = true;
		
		eleDialogModifyReasonInput.value = "";
		
		blockSetType = 'cancel';
	};
	
	$(document).on("click", "#createBtn", func_showCreatePopupModel);
	
	$(document).on("click", "#btn_OK", function(event){
		console.info('btn_OK click');

		console.info('blockSetType = ', blockSetType);
		
		valMobileInput = $("#myModel #dialogMobileInput").val().trim();
		console.info('valMobileInput = ', valMobileInput);

		valModifyReasonInput = $('#myModel #dialogModifyReasonInput').val().trim();
		console.info('valModifyReasonInput = ', valModifyReasonInput);
		
		if (!modelDataValidate()) {
			var aaa = alert("手機門號及原因皆為必填欄位，不得為空!\n請再次確認是否已填寫正確?");
			return false;
		}
		
		fun_updateExcludeUser();
	});
	
	$(document).on("click", "#btn_Cancel", function(event){

		eleDialogMobileInput.value = "";
		eleDialogMobileInput.readOnly = false;
		
		eleDialogModifyReasonInput.value = "";
		
		model.style.display = "none";
	});
	
	
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

//			document.getElementById("searchBtn").click();

			loadData();
		}
	};
	
	// -------------------Event----------------------
	$('.datepicker').datepicker({
		maxDate : 0,
		dateFormat : 'yy-mm-dd',
		changeMonth : true
	});

	$('#btn_PreviousPage').click(function() {
//		console.info('btn_PreviousPage');
//		console.info('valCurrentPageIndex = ', valCurrentPageIndex);
		pageBtnHandler(valCurrentPageIndex > 1, 'back');
	});

	$('#btn_NextPage').click(function() {
//		console.info('btn_NextPage');
//		console.info('valCurrentPageIndex = ', valCurrentPageIndex);
//		console.info('valTotalPageSize = ', valTotalPageSize);
		pageBtnHandler(valCurrentPageIndex < valTotalPageSize, 'next');
	});

	// do Search
	$('#searchBtn').click(function() {
		
		valStartDate = $('#startDate').val();
		valEndDate = $('#endDate').val();
		valMobile = $('#mobileInput').val();
		valInsertUser = $('#insertUserInput').val();
		valGroupTag = elePnpBlockTagSelector.options[elePnpBlockTagSelector.selectedIndex].value;
		
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
		
		if (!valGroupTag || valGroupTag == "未設定標籤") {
			valGroupTag = " ";
		}

		console.info('valStartDate = ', valStartDate);
		console.info('valEndDate = ', valEndDate);
		console.info('valMobile = ', valMobile);
		console.info('valInsertUser = ', valInsertUser);
		console.info('valGroupTag = ', valGroupTag);

		if (!dataValidate()) {
			return false;
		}
		
		$('.LyMain').block($.BCS.PnpBlock_dataLoading);
		
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
//			console.info('response:', response);
			
			isSearchData = true;
			
			var blockSendCount = response;
//			console.info('blockSendCount = ', blockSendCount);

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
	});

	$('#exportBtn').click(function() {

		$('.LyMain').block($.BCS.blockMsgRead);
		
//		console.log('Has data : ', hasData);
		
		if (!hasData) {
			if (!isSearchData) {
				alert("很抱歉，您尚未進行資料查詢，無法匯出資料！\n請先進行資料查詢，謝謝。")
			} else {
				alert("目前無資料可匯出！\n請重新進行查詢，謝謝。")
			}
			
			$('.LyMain').unblock();
			return;
		}
		
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
		
		var getUrl = bcs.bcsContextPath + '/pnpEmployee/exportPNPBlockListReportExcel?'
										+ 'startDate=' + valStartDate 
										+ '&endDate=' + valEndDate 
										+ '&mobile=' + valMobile
										+ '&insertUser=' + valInsertUser 
										+ '&groupTag=' + valGroupTag 
										+ '&blockEnable=-1';

		getUrl = encodeURI(getUrl);
//		console.info('getUrl = ', getUrl);
		
		window.location.href = getUrl;

		$('.LyMain').unblock();
	});
	
	var dataValidate = function() {
		if (moment(startDate).isAfter(moment(endDate))) {
			alert('更新時間設定異常 ( 起始時間不可大於結束時間 )');
			return false;
		}

		return true;
	};
	
	var modelDataValidate = function() {
		if (!valMobileInput || valMobileInput == "") {
			return false;
		}
		
		if (!valModifyReasonInput || valModifyReasonInput == "") {
			return false;
		}
		
		return true;
	}

	var loadData = function() {
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
//			console.info('response = ', response);
//			console.log('JSON.stringify(response) = ', JSON.stringify(response));

			$('.dataTemplate').remove();
			
			if (response.length == 0) {
				return false;
			}
			
			var i = 1;
			response.forEach(function(obj) {
				var list = originalTr.clone(true);
				
				list.find('.mobileNum a').attr('href', bcs.bcsContextPath +'/pnpEmployee/pnpExcludeSendingSingleUserHistoryPage?mobile=' + obj.phone);
				list.find('.mobileNum a').html(obj.phone);
				
				list.find('.lineUID').html(obj.uid);
				list.find('.reason').html(obj.modifyReason);
				list.find('.updateTime').html(obj.createTime);
				
				var blockStatus = (obj.blockEnable == 1)? "排除中" : "取消排除";
				
				list.find('.status').html(blockStatus);
				list.find('.guestLabel').html(obj.groupTag);
				list.find('.modifier').html(obj.insertUser);

				/* Setup Button Delete */
				list.find('.btn_delete').attr('mobile', obj.phone);
				list.find('.btn_delete').click(func_showCancelPopupModel);
				
				$('#tableBody').append(list);
				
				i++;
			});
			
			if (i > 0) {
				hasData = true;
			}
			
//			console.info('i = ', i);
			
			$('.LyMain').unblock();
			
		}).fail(function(response) {
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		});
	};
	
	var fun_updateExcludeUser =  function() {
		console.info('mobile = ', valMobileInput);
		console.info('modifyReason = ', valModifyReasonInput);
		console.info('blockSetType = ', blockSetType);
        
        var valBlockEnable = (blockSetType == 'create')? 1 : 0;
		console.info('valBlockEnable = ', valBlockEnable);
		
		var confirmString = (blockSetType == 'create')? '請再次確認建立排除發送名單之電話號碼與原因是否正確?' : '請再次確認取消排除發送名單之電話號碼與原因是否正確?';
		
        var r = confirm(confirmString);
        if (!r) {
        	return;
        }
        
        var currentDateTime = new Date();
        var currentDate = currentDateTime.getFullYear() + '-' + (currentDateTime.getMonth() + 1) + '-'+currentDateTime.getDate();
        var currentTime = currentDateTime.getHours() + ":" + currentDateTime.getMinutes() + ":" + currentDateTime.getSeconds();

		console.info('currentDateTime = ', currentDateTime);
		console.info('currentDate = ', currentDate);
		console.info('currentTime = ', currentTime);
		
		var userAccount = bcs.user.account;
		console.info('userAccount = ', userAccount);
        
		$('.LyMain').block($.BCS.PnpBlock_dataUpdateing);
		$.ajax({
			type : 'POST',
			url : bcs.bcsContextPath + '/pnpEmployee/updPnpBlockSend',
			contentType : 'application/json;charset=UTF-8',
			data : JSON.stringify({
				mobile : valMobileInput,
				blockEnable : valBlockEnable,
				groupTag : "",
				insertUser : userAccount,
				insertDate : currentDate,
				insertTime : currentTime,
				modify_reason : valModifyReasonInput
			})
        }).success(function(response) {
            console.info(response);
            
            var alertString = (blockSetType == 'create')? " 新增至排除發送名單" : " 從排除發送名單中移除";
            alert("已將用戶電話號碼 " + valMobileInput + alertString);
            
    		model.style.display = "none";

    		document.getElementById("searchBtn").click();
            
        	$('.LyMain').unblock();
        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
        	$('.LyMain').unblock();
        }).done(function() {
        	$('.LyMain').unblock();
        });
    }

	var cleanList = function() {
		$('.dataTemplate').remove();
		$('.tableBody').remove();
	};

	// initialize Page
	var initPage = function() {
		originalTr = $('.dataTemplate').clone(true);
		originalTable = $('#tableBody').clone(true);

		cleanList();
		
		loadAndSetBlockTagList();

		document.getElementById("searchBtn").click();
		
		/* 設定預設的開始及結束時間 */
//		startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
//		startDate = moment(new Date()).format('YYYY-MM-DD');
//		endDate = moment(new Date()).format('YYYY-MM-DD');
//		console.info('initPage : startDate = ', startDate);
//		console.info('initPage : endDate = ', endDate);
//		$('#startDate').val(startDate);
//		$('#endDate').val(endDate);
	};

	/* 更新客群標籤下拉選單 */
	var func_optionPnpBlockTagSelectChanged = function(){
		var selectValue = $(this).find('option:selected').text();
		
		$(this).closest('.optionGuestLabel').find('.optionLabelGuestLabel').html(selectValue);
	};
	
	/* 載入PNP排除發送名單客群標籤列表 */
	var loadAndSetBlockTagList = function() {

		$('.LyMain').block($.BCS.PnpBlock_loadTagList);
		
		// Get PNP Block Tag List
		$.ajax({
			type : 'POST',
			url : bcs.bcsContextPath + '/pnpEmployee/qryPNPBlockGTagList',
			contentType : 'application/json;charset=UTF-8',
			data : JSON.stringify({
				inActive : 1
			})
		}).done(function(response) {
//			console.info('response = ', response);
//			console.log('JSON.stringify(response) = ', JSON.stringify(response));
			
			if (response.length == 0) {
				return false;
			}
			
			$.each(response, function(i, o){		
//				console.info('qryPNPBlockGTagList : o = ', JSON.stringify(o));
				
				if (o.groupTag == "" || o.groupTag == null || o.groupTag == " ") {
					return true;
				}
				
				var opt = document.createElement('option');
				opt.innerHTML = o.groupTag;
				
				elePnpBlockTagSelector.appendChild(opt);
			});

			$('.optionSelectGuestLabel').change(func_optionPnpBlockTagSelectChanged);
			
			$('.LyMain').unblock();
			
		}).fail(function(response) {
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		});
	};

	initPage();
});
