/**
 * 
 */
$(function(){
		
	$('.btn_upload').click(function(){
        $('.LyMain').block($.BCS.blockMsgUpload);
        
		$.ajax({
            type : "POST",
            url : bcs.bcsContextPath +'/admin/refreshInvoice',
            cache: false,
            contentType: 'application/json',
            processData: false,
            data : {}
        }).success(function(response){
            console.info(response);
            alert(JSON.stringify(response));
        }).fail(function(response){
            console.info(response);
            $.FailResponse(response);
            $('.LyMain').unblock();
        }).done(function(){
            $('.LyMain').unblock();
        });
	});

    var loadDataFunc = function(){

    }

    loadDataFunc();
});