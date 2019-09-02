$(function() {
	// ---- Global Variables ----
	// parameter data
	var linePointMainId = null;
	
	// result data
	var hasData = true; // always true
	var oringinalTr = {};
	var originalTable = {};
	var page = 1, totalPages = 0;
	var firstFatch = true;
	
	
	// ---- Functions ----
    // do Split Page
	$('.btn.prev').click(function(){
		if(page > 1) {
			page--;
			getDataList();
			$('#pageAndTotalPages').text(page + '/' + totalPages);
		}
	});
	$('.btn.next').click(function(){
		if(page < totalPages) {
			page++;
			getDataList();
			$('#pageAndTotalPages').text(page + '/' + totalPages);
		}
	});
	
	// do Download
	setExportButtonSource = function(){
		console.info('hasData:', hasData);
		if(hasData) {
			var getUrl = bcs.bcsContextPath + '/edit/getLPStatisticsReportDetailExcel?linePointMainId=' + linePointMainId;
			console.info('getUrl', getUrl);
			
			$('.btn_add.exportToExcel').attr('href', getUrl);
		} else {
			$('.btn_add.exportToExcel').attr('href', '#');
		}
	}
	
	// ---- Initialize Page ----
	// get Main List
	var getMainList = function(){
        $.ajax({
			type : 'POST',
			url : bcs.bcsContextPath + '/edit/findOneLinePointMainByMainId?linePointMainId=' + linePointMainId,
            contentType: 'application/json',
        }).success(function(o) {
            console.info('findOneLinePointMainByMainId response:', o);
            $('#titleText').html('專案名稱：' + o.title);
            $('#serialIdText').html('Campaign：' + o.serialId);
            $('#totalCountText').html('發送總點數：' + o.totalCount);
            $('#modifyUserText').html('建立人員：' + o.modifyUser);
            $('#departmentFullNameText').html('建立人員單位：' + o.departmentFullName);
            $('#pccCodeText').html('PCC：' + o.pccCode);
        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
        }).done(function() {
        	$('.LyMain').unblock();
        });		
	};
	
    // get Data List
	var getDataList = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		$('.resultTr').remove();
		

		
        $.ajax({
			type : 'GET',
			url : bcs.bcsContextPath + '/edit/findAllLinePointDetailByMainId?linePointMainId=' + linePointMainId,
            contentType: 'application/json',
        }).success(function(response) {
            console.info("response:", response);
			
            $.each(response, function(i, o) {
                var responseStatus = "";
                if(o.status=='SUCCESS'){
                	responseStatus = '成功';
                }else if(o.status=='FAIL'){
                	responseStatus = '失敗';
                }else {
                	responseStatus = '等待';
                	return; // == continue
                }
                if(o.detailType == 'CANCEL_API' || o.detailType == 'CANCEL_BCS'){
                	responseStatus = '取消' + responseStatus;
                }
		        
		        
            	var resultTr = originalTr.clone(true); //增加一行
                
                if (o.sendTime) {
		              resultTr.find('.sendTime').html(moment(o.sendTime).format('YYYY-MM-DD HH:mm:ss'));
		        }else{
		              resultTr.find('.sendTime').html('-');
		        }
                
                resultTr.find('.orderKey').html(o.orderKey);
                resultTr.find('.uid').html(o.uid);
                resultTr.find('.custId').html(o.custid);
                resultTr.find('.amount').html(o.amount);
                resultTr.find('.responseStatus').html(responseStatus);

		        
                if (o.status=='FAIL') {
		              resultTr.find('.message').html(o.message);
		        }else{
		              resultTr.find('.message').html('-');
		        }
                
                // Append to Table
                $('.resultTable').append(resultTr);
            });
        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
        }).done(function() {
        	$('.LyMain').unblock();
        });		
	};
    
	// get Total Count
	var setTotal = function(){
		// block
		$('.LyMain').block($.BCS.blockMsgRead);
		
		// get URL
		var getUrl = bcs.bcsContextPath + '/edit/getLPStatisticsReportTotalPages?startDate=' + startDate + '&endDate=' + endDate + '&page=' + page + 
		'&modifyUser=' + modifyUserInput + '&title=' + titleInput;
		console.info('getUrl', getUrl);
		
		// get data
		$.ajax({
			type : 'GET',
			url : getUrl
		}).success(function(response){
			console.info('msg1: ', response['msg']);
			totalPages = parseInt(response['msg']);
			console.info('totalPages1: ', totalPages);
			// set pageAndTotalPage
			page = 1;
			console.info(page + '/' + totalPages);
			$('#pageAndTotalPages').text(page + '/' + totalPages);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	}
	
	// initialize Page
	var initPage = function(){
		// get parameters
		linePointMainId = $.urlParam("linePointMainId");
		
		// clone & remove
	    originalTr = $('.resultTr').clone(true);
	    $('.resultTr').remove();
	    originalTable = $('.resultTable').clone(true);
	    
	    // initialize time picker
		startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
		endDate = moment(new Date()).format('YYYY-MM-DD');
		$('#startDate').val(startDate);
		$('#endDate').val(endDate);
		
		// start execute
		console.info("firstFatch:", firstFatch);
		if(firstFatch){
			firstFatch = false;
			//setTotal();
			getMainList();
			setExportButtonSource();
		}
		getDataList();
	};
	
    initPage();
});