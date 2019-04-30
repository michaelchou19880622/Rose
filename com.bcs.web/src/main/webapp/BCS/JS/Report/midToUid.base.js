/**
 * 
 */
$(function(){
	
	$('.upload_mid').click(function(){
		$('#upload_mid_btn').click();
	});
	
	$('#upload_mid_btn').on("change", function(ev){

		var input = ev.currentTarget;
    	if (input.files && input.files[0]) {
    		var fileName = input.files[0].name;
    		console.info("fileName : " + fileName);
    		var form_data = new FormData();
    		
    		form_data.append("filePart",input.files[0]);

    		$('.LyMain').block($.BCS.blockMsgUpload);
    		$.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + '/admin/uploadMidToUidTemp',
                cache: false,
                contentType: false,
                processData: false,
                data: form_data
    		}).success(function(response){
            	console.info(response);
            	alert("轉換成功!");
    			if(response.count > 0){
    				var url =  bcs.bcsContextPath + '/admin/downloadMidToUid?fromFile=true&tempId=' + response.tempId;
    				
    				var downloadReport = $('#downloadReport');
    				downloadReport.attr("src", url);
    			}
    			else{
    				alert('查詢結果共 ' + response.count + ' 筆');
    			}
    		}).fail(function(response){
    			console.info(response);
    			$.FailResponse(response);
    			$('.LyMain').unblock();
    		}).done(function(){
    			$('.LyMain').unblock();
    		});
        } 
	});

	$('#downloadReport').load(function () {
        //if the download link return a page
        //load event will be triggered
		$('.LyMain').unblock();
    });
});