$(function() {
    var originalTr = {};
    var startDate = null, endDate = null;
    
	$('.datepicker').datepicker({
		 maxDate : 0,
		 dateFormat : 'yy-mm-dd',
		 changeMonth: true
	});
	
	$('.query').click(function(){
		if(dataValidate()) {
			$('.dataTemplate').remove();
			$('.sumTemplate').remove();
			startDate = $('#startDate').val();
			endDate = $('#endDate').val();
			
			getListData();
		}
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
            url: bcs.bcsContextPath + '/edit/getBcsLinePointMainList?startDate=' + startDate + '&endDate=' + endDate
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
		        templateTr.find('.status').html(o.status);
		        
		        var currentTime = moment();
		        var sendStartTime = moment(o.sendStartTime).format('YYYY-MM-DD HH:mm:ss');
		        console.info('currentTime:', currentTime);
		        console.info('sendStartTime:', sendStartTime);
		        console.info('isAfter:', currentTime.isAfter(sendStartTime));
		        if(o.sendStartTime){
		        	templateTr.find('.btn_copy').val('已發送');
		        } else if(currentTime.isAfter(sendStartTime)){
		        	templateTr.find('.btn_copy').val('已過期');
		        }else{
		        	if(o.allowToSend == true){
		        		templateTr.find('.btn_copy').val('收回');
		        	}else{
		        		templateTr.find('.btn_copy').val('發送');
		        	}
		        	
		        	templateTr.find('.btn_copy').attr('linePointId', o.id);
                    templateTr.find('.btn_copy').click(btn_sendFunc);
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
        console.info('btn_sendFunc linePointMainId:' + linePointMainId);

//        var r = confirm("請確認是否發送/收回？");
//        if (!r) {
//        	return;
//        }

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
    
	// main()
	// initialize Page & load Data
    initPage();
    loadDataFunc();

});