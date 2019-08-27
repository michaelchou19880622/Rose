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
	    
	    // initialize time picker
		startDate = moment(new Date()).format('YYYY-MM-DD');
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
            url: bcs.bcsContextPath + '/edit/getBcsLinePointMainList?startDate=' + startDate + '&endDate=' + endDate
        }).success(function(response) {
            console.info("response:", response);
            $.each(response, function(i, o) {
                var templateTr = originalTr.clone(true); //增加一行
                console.info("templateTr:", templateTr);
                
//                templateTr.find('#titleLink').attr('href', bcs.bcsContextPath + '/edit/linePointCreatePage?linePointMainId=' + 
//                		o.id + '&sendGroupId=' + o.linePointSendGroupId + '&msgId=' + o.appendMessageId +'&actionType=Edit');

                templateTr.find('.title').html(o.title);
                
                if (o.modifyTime) {
		              templateTr.find('.modifyTime').html(moment(o.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
		        }else{
		              templateTr.find('.modifyTime').html('-');
		        }
                
                templateTr.find('.modifyUser').html(o.modifyUser);

                templateTr.find('.departmentFullName').html(o.departmentFullName);

                templateTr.find('.pccCode').html(o.pccCode);
                
		        templateTr.find('.serviceName').html(o.sendType);
		        
		        templateTr.find('.campaignCode').html(o.serialId);
		        
		        templateTr.find('.sendedCount').html(o.successfulCount + o.failedCount);
		        
		        templateTr.find('.successfulCount').html(o.successfulCount);
		        
		        templateTr.find('.failedCount').html(o.failedCount);
		       
		        templateTr.find('.totalAmount').html(o.totalAmount);
		        
		        templateTr.find('.status').html(o.status);
		        
		        templateTr.find('.btn_copy').val('查看明細');
                    
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

        var r = confirm("請確認是否執行發送／收回？");
        if (!r) {
        	return;
        }

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
			$('.sumTemplate').remove();
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