$(function() {
    var originalTr = {};
    var startDate = null, endDate = null;
    
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
	
	// Initialize Page
	var initPage = function(){
		// clone & remove
	    originalTr = $('.templateTr').clone(true);
	    $('.templateTr').remove();
	    
		// initialize date-picker
		startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
		endDate = moment(new Date()).format('YYYY-MM-DD');
		$('#startDate').val(startDate);
		$('#endDate').val(endDate);
	};
	
    var loadDataFunc = function(){
		// block
		$('.LyMain').block($.BCS.blockMsgRead);
		
		// get all list data
		getListData();
    };
	
    // get list data
	var getListData = function(){
		$('.templateTr').remove();
		
        $.ajax({
            type: "GET",
            url: bcs.bcsContextPath + '/edit/findAllBcsLinePointMain?startDate=' + startDate + '&endDate=' + endDate
        }).success(function(response) {
            console.info("response:", response);
            $.each(response, function(i, o) {
                var templateTr = originalTr.clone(true); //增加一行
                console.info("templateTr:", templateTr);
                
                templateTr.find('#titleLink').html(o.title);
                templateTr.find('#titleLink').attr('href', bcs.bcsContextPath + '/edit/linePointCreatePage?linePointMainId=' + 
                		o.id + '&sendGroupId=' + o.linePointSendGroupId + '&msgId=' + o.appendMessageId +'&actionType=Edit');
		        if (o.modifyTime) {
		              templateTr.find('.modifyTime').html(moment(o.modifyTime).format('YYYY-MM-DD'));
		        }else{
		              templateTr.find('.modifyTime').html('-');
		        }
		        templateTr.find('.modifyUser').html(o.modifyUser);
		        templateTr.find('.totalCount').html(o.totalCount);
		        templateTr.find('.totalAmount').html(o.totalAmount);
		        if(o.doAppendMessage){
		        	templateTr.find('.doAppendMessage').html('Y');
		        }else{
		        	templateTr.find('.doAppendMessage').html('N');
		        }
		        if (o.sendTimingTime) {
		              templateTr.find('.sendTimingTime').html(moment(o.sendTimingTime).format('YYYY-MM-DD HH:mm:ss'));
		        }else{
		              templateTr.find('.sendTimingTime').html('立即發送');
		        }
		        if (o.sendStartTime) {
		              templateTr.find('.sendStartTime').html(moment(o.sendStartTime).format('YYYY-MM-DD HH:mm:ss'));
		        }else{
		              templateTr.find('.sendStartTime').html('尚未發送');
		        }
		        templateTr.find('.successfulCount').html(o.successfulCount);
		        templateTr.find('.successfulAmount').html(o.successfulAmount);
		        
		        // get date data
		        var currentTime = moment(new Date()).add(-120, 'seconds');
		        var sendStartTime = moment(o.sendStartTime).format('YYYY-MM-DD HH:mm:ss');
		        console.info('currentTime:', currentTime);
		        console.info('sendStartTime:', sendStartTime);
		        console.info('isAfter:', currentTime.isAfter(sendStartTime));
		        
		        // set status
		        var statusCh = '';
		        if(o.sendTimingType == 'IMMEDIATE'){
		        	if(o.status == 'COMPLETE'){
		        		statusCh = '已發送';
		        	}else{
		        		statusCh = '已設定';
		        	}
		        }else{
		        	if(o.status == 'COMPLETE'){
		        		statusCh = '已發送';
		        	}else{
		        		if(currentTime.isAfter(sendStartTime)){
		        			statusCh = '已逾期';
		        		}else if(o.allowToSend == true){
		        			statusCh = '待發送';
		        		}else{
		        			statusCh = '已設定';
		        		}
		        	}
		        }
		        templateTr.find('.status').html(statusCh);

		        // set button
		        if(o.sendStartTime){
		        	templateTr.find('.btn_copy').val('已發送');
		        }else if(currentTime.isAfter(sendStartTime)){
		        	templateTr.find('.btn_copy').val('過期');
		        }else{
		        	if(o.allowToSend == true){
		        		templateTr.find('.btn_copy').val('取消').css("background-color","red");
		        	}else{
		        		templateTr.find('.btn_copy').val('發送').css("background-color","blue");
		        	}
		        	
		        	templateTr.find('.btn_copy').attr('linePointId', o.id);
                    templateTr.find('.btn_copy').click(btn_sendFunc);
		        }
		        
		        if(bcs.user.role == 'ROLE_LINE_SEND'){
		        	templateTr.find('.btn_copy').attr("disabled",true);
		        }else if (bcs.user.role == 'ROLE_LINE_VERIFY' && bcs.user.account == o.modifyUser){
		        	templateTr.find('.btn_copy').attr("disabled",true);
		        }
		        
//                if (bcs.user.admin) {
//                } else {
//                    templateTr.find('.btn_copy').remove();
//                }
//                
                // Append to Table
                $('.templateTable').append(templateTr);
            });
            
        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
        }).done(function() {
        	$('.LyMain').unblock();
        });		
	};
    
    // other Functions
    // to Create Page
    $('.btn_add').click(function() {
        window.location.replace(bcs.bcsContextPath + '/edit/linePointCreatePage');
    });

    // do Send
    var btn_sendFunc = function() {
        var linePointMainId = $(this).attr('linePointId');
        if(!linePointMainId){
        	return;
        }
        console.info('btn_sendFunc linePointMainId:' + linePointMainId);

        // warning while actionText = Send
        var actionText = $(this).attr('value');
        console.info('actionText:', actionText);
        if(actionText == '發送'){
        	var r = confirm("請再次確認是否要發送？");
            if (!r) {
            	return;
            }
        }
        
        // send
        $.ajax({
            type: "POST",
            url: bcs.bcsContextPath + '/edit/pressSendLinePointMain?linePointMainId=' + linePointMainId 
        }).success(function(response) {
            console.info(response);
            alert("執行成功");
            window.location.replace(bcs.bcsContextPath + '/edit/linePointListPage');
        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
        }).done(function() {
        });
    };
    
	// [查詢] Button
	$('.query').click(function(){
		if(dataValidate()) {
			// block
			$('.LyMain').block($.BCS.blockMsgRead);
			
			// get time data
			$('.dataTemplate').remove();
			startDate = $('#startDate').val();
			endDate = $('#endDate').val();
			
			getListData();
		}
	});
	
	// main()
	// initialize Page & load Data
    initPage();
    loadDataFunc();

});