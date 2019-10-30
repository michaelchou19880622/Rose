$(function() {
	// ---- Global Variables ----
	// parameter data
	var linePointMainId = null;
	var modifyUser ;
	// result data
	var hasData = true; // always true
	var oringinalTr = {};
	var originalTable = {};
	var page = 1, totalPages = 0;
	var firstFatch = true;
	var linePointMainId = $.urlParam("linePointMainId");
	var linePointDetailCount = 0;
	var linePointDetail = {};
	// ---- Functions ----
    // do Split Page
	$('.btn.prev').click(function(){
		if(page > 1) {
			page--;
			pagelist();
			$('#pageAndTotalPages').text(page + '/' + totalPages);
		}
	});
	$('.btn.next').click(function(){
		if(page < totalPages) {
			page++;
			pagelist();
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
		$('.LyMain').block($.BCS.blockMsgRead);
        $.ajax({
			type : 'POST',
			url : bcs.bcsContextPath + '/edit/findOneLinePointMainByMainId?linePointMainId=' + linePointMainId,
            contentType: 'application/json',
        }).success(function(o) {
            console.info('findOneLinePointMainByMainId response:', o);
            $('#titleText').html('專案名稱：' + o.title);
            $('#serialIdText').html('Campaign：' + o.serialId);
            $('#totalCountText').html('發送總點數：' + o.successfulAmount);
            $('#modifyUserText').html('建立人員：' + o.modifyUser);
            modifyUser = o.modifyUser;
            $('#departmentFullNameText').html('建立人員單位：' + o.departmentFullName);
            $('#pccCodeText').html('PCC：' + o.pccCode);
            getDataList(); // 做完再做表格  不然有時會抓不到modifyUser
        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
        }).done(function() {
        	
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
            linePointDetailCount = response.length;
            linePointDetail = response ;
            
            if(linePointDetailCount % 10 == 0){
				totalPages = linePointDetailCount/10;
			}else{
				totalPages = Math.floor(linePointDetailCount/10) + 1 ;
			}
			$('#pageAndTotalPages').text(page + '/' + totalPages);
			
			pagelist();
           
            
        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
        }).done(function() {
        	$('.LyMain').unblock();
        });		
	};
	
	function pagelist(){
		 $('.resultTr').remove();
		 $.each(linePointDetail, function(i, o) {
			 if(Math.floor(i/10)+1 == page){
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
	             
	             if(o.status=='SUCCESS'){
	             	resultTr.find('.btn_copy').attr('detailId', o.detailId).css("background-color","red");
	                 resultTr.find('.btn_copy').click(btn_cancle);
	             }
	             
	             if(bcs.user.role == 'ROLE_REPORT' || bcs.user.role == 'ROLE_LINE_SEND'){
	             	resultTr.find('.btn_copy').remove();
	             }
             	$('.resultTable').append(resultTr);
			 }
             
             
             // Append to Table
             
		 });
	}
    
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
		
		
		getMainList();
		setExportButtonSource();
		//getDataList();// 这里写sleep之后需要去做的事情
		
	};
	
	var btn_cancle = function(){
		$('.LyMain').block($.BCS.blockMsgRead);
		var detailId = $(this).attr('detailId');

		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/linePointCancelFromDetailId?detailId=' + detailId
		}).success(function(response){
			console.info(response);
			alert("收回成功");
			window.location.replace(bcs.bcsContextPath + '/edit/linePointStatisticsReportDetailPage?linePointMainId=' + linePointMainId);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
			$('.LyMain').unblock();
		});
	};
	
    initPage();
});