$(function() {
    $('.btn_save').click(function() {
        var title = $('#title').val();
        var pccCode = $('#pccCode').val();
        var serialId = $('#serialId').val();
        var sendTimingType = ($('.sendTimingType')[0].checked)?"IMMEDIATE":"SCHEDULE";
        var sendAmountType = ($('.sendAmountType')[0].checked)?"UNIVERSAL":"INDIVIDUAL";
        var amount = ($('.sendAmountType')[0].checked)?($('#amount').val()):0;

        console.info("va1:", sendAmountType);
        
        
//        if (!campaignName || !campaignCode || !sendPoint || !campaignPersonNum) {
//            alert("欄位不可為空");
//            return;
//        }
//
//        var postData = {};
//        postData.title = campaignName;
//        postData.serialId = campaignCode;
//        postData.amount = sendPoint;
//        postData.totalCount = campaignPersonNum;
//        postData.sendType = sendType;
//        postData.status = "IDLE";
//        postData.successfulCount = 0;
//        postData.failedCount = 0;
//        
//        console.info('postData', postData);

        /*
        $.ajax({
            type: "POST",
            url: bcs.bcsContextPath + '/market/createLinePointMain',
            cache: false,
            contentType: 'application/json',
            processData: false,
            data: JSON.stringify(postData)

        }).success(
            function(response) {
                console.info(response);
                alert('儲存成功');
                window.location.replace(bcs.bcsContextPath + '/market/linePointListPage');
            }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
        })
        */
    });
    
	
//	var sendType = 'MANUAL';
//	
//	$(".sendType").click(function(e){
//		sendType = e.currentTarget.value;
//		console.info("selectedSendType:", sendType);
//		console.info("sendTimingType:", $('.sendTimingType')[0].checked);
//	});
});