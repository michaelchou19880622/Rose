$(function() {
	// ---- Global Variables ----
	// input data
	var modifyUserInput = "";
	var titleInput = "";
	
	// result data
	var hasData = false;
	var oringinalTr = {};
	var originalTable = {};
	var startDate = null, endDate = null;
	var page = 1, totalPages = 0;
	var firstFatch = true;
	var count = 0 ;
	var linepointMainResponse = {};
	// date module
	$('.datepicker').datepicker({
		 maxDate : 0,
		 dateFormat : 'yy-mm-dd',
		 changeMonth: true
	});
	
	var dataValidate = function(){
		startDate = $('#startDate').val();
		endDate = $('#endDate').val();
		if(!startDate) {
			alert('請填寫起始日期！');
			return false;
		}
		if(!endDate) {
			alert('請填寫結束日期！');
			return false;
		}
		if(!moment(startDate).add(31, 'days').isAfter(moment(endDate))){
			alert('起始日期與結束日期之間不可相隔超過一個月！');
			return false;
		}
		if(moment(startDate).isAfter(moment(endDate))){
			alert('起始日期不可大於結束日期！');
			return false;
		}
		return true;
	}
	
	// ---- Functions ----
    // do Split Page
	$('.btn.prev').click(function(){
		if(page > 1) {
			page--;
			pageList();
			$('#pageAndTotalPages').text(page + '/' + totalPages);
		}
	});
	$('.btn.next').click(function(){
		if(page < totalPages) {
			page++;
			pageList();
			$('#pageAndTotalPages').text(page + '/' + totalPages);
		}
	});
	
	// do Search
	$('.btn_add.search').click(function(){
		if(dataValidate()) {
			// block
			$('.LyMain').block($.BCS.blockMsgRead);
			
			// get time data
			startDate = $('#startDate').val();
			endDate = $('#endDate').val();
			
			// refresh to new list
			$('.resultTr').remove();
			getDataList();
		}
	});
	
	// do Download
	setExportButtonSource = function(){
		console.info('hasData:', hasData);
		if(hasData) {
			var modifyUserInput = $('#modifyUserInput').val();
			var titleInput = $('#titleInput').val();
			var getUrl = bcs.bcsContextPath + '/edit/getLPStatisticsReportExcel?startDate=' + startDate + '&endDate=' + endDate + 
			'&modifyUser=' + modifyUserInput + '&title=' + titleInput;
			console.info('getUrl', getUrl);
			
			$('.btn_add.exportToExcel').attr('href', getUrl);
		} else {
			$('.btn_add.exportToExcel').attr('href', '#');
		}
	}
	
	// ---- Initialize Page ----
    // get Data List
	var getDataList = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		$('.resultTr').remove();
		console.info("firstFatch:", firstFatch);
		if(firstFatch){
			firstFatch = false;
			//setTotal();
		}
		
		var modifyUserInput = $('#modifyUserInput').val();
		var titleInput = $('#titleInput').val();
		var getUrl = bcs.bcsContextPath + '/edit/getLPStatisticsReport?startDate=' + startDate + '&endDate=' + endDate + '&page=' + page + 
			'&modifyUser=' + modifyUserInput + '&title=' + titleInput;
		console.info('getUrl', getUrl);
		
        $.ajax({
			type : 'GET',
			url : getUrl,
            contentType: 'application/json',
        }).success(function(response) {
            console.info("response:", response);
			if(response.length === 0) {
				hasData = false;
			} else {
				hasData = true;
			}
			count = response.length ; //計算一共有幾筆
			if(count % 10 == 0){
				totalPages = count/10;
			}else{
				totalPages = Math.floor(count/10) + 1 ;
			}
			$('#pageAndTotalPages').text(page + '/' + totalPages);
			linepointMainResponse = response;
			pageList();
         
		    setExportButtonSource();
		        
            // Append to Table
           
            
        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
        }).done(function() {
        	$('.LyMain').unblock();
        });		
	};
    
	// get Total Count
//	var setTotal = function(){
//		// block
//		$('.LyMain').block($.BCS.blockMsgRead);
//		
//		// get URL
//		var modifyUserInput = $('#modifyUserInput').val();
//		var titleInput = $('#titleInput').val();
//		var getUrl = bcs.bcsContextPath + '/edit/getLPStatisticsReportTotalPages?startDate=' + startDate + '&endDate=' + endDate + '&page=' + page + 
//		'&modifyUser=' + modifyUserInput + '&title=' + titleInput;
//		console.info('getUrl', getUrl);
//		
//		// get data
//		$.ajax({
//			type : 'GET',
//			url : getUrl
//		}).success(function(response){
//			console.info('msg1: ', response['msg']);
//			totalPages = parseInt(response['msg']);
//			console.info('totalPages1: ', totalPages);
//			// set pageAndTotalPage
//			page = 1;
//			console.info(page + '/' + totalPages);
//			$('#pageAndTotalPages').text(page + '/' + totalPages);
//		}).fail(function(response){
//			console.info(response);
//			$.FailResponse(response);
//			$('.LyMain').unblock();
//		}).done(function(){
//			$('.LyMain').unblock();
//		});
//	}
	var pageList = function(){
		$('.resultTr').remove();
		$.each(linepointMainResponse, function(i, o) {
        	if(Math.floor(i/10)+1 == page){
                var resultTr = originalTr.clone(true); //增加一行
                console.info("resultTr:", resultTr);
                resultTr.find('.title').html(o.title);
                if (o.sendStartTime) {
		              resultTr.find('.modifyTime').html(moment(o.sendStartTime).format('YYYY-MM-DD HH:mm:ss'));
		        }else{
		              resultTr.find('.modifyTime').html('-');
		        }
                resultTr.find('.modifyUser').html(o.modifyUser);
                resultTr.find('.departmentFullName').html(o.departmentFullName);
                resultTr.find('.pccCode').html(o.pccCode);
		        resultTr.find('.serviceName').html(o.sendType);
		        resultTr.find('.campaignCode').html(o.serialId);
		        resultTr.find('.sendedCount').html(o.totalCount);
		        resultTr.find('.successfulCount').html(o.successfulCount);
		        resultTr.find('.failedCount').html(o.failedCount);
		        resultTr.find('.successfulAmount').html(o.successfulAmount);
		        resultTr.find('#toDetail').attr('href', bcs.bcsContextPath + '/edit/linePointStatisticsReportDetailPage?linePointMainId=' + o.id +'&startDate=' + startDate + '&endDate=' + endDate);
		        
		        $('.resultTable').append(resultTr);
        	} 
		});
		
	};
	
	
	// initialize Page
	var initPage = function(){
		// clone & remove
	    originalTr = $('.resultTr').clone(true);
	    $('.resultTr').remove();
	    originalTable = $('.resultTable').clone(true);
	    
	    // initialize time picker
		startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
		endDate = moment(new Date()).format('YYYY-MM-DD');
		$('#startDate').val(startDate);
		$('#endDate').val(endDate);
	};
	
    initPage();
});