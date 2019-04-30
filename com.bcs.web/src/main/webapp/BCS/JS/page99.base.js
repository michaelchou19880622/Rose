/**
 * 
 */
$(function(){
		
	$('.btn_upload').click(function(){
		$('#uploadPictureFile').click();
	});


	
	$('#uploadPictureFile').on("change", function(ev){

		var input = ev.currentTarget;
    	if (input.files && input.files[0]) {
    		var fileName = input.files[0].name;
    		console.info("fileName : " + fileName);
    		var form_data = new FormData();
    		
            form_data.append("filePart",input.files[0]);
            form_data.append("campaignId",$('#campaignId').val());

    		$('.LyMain').block($.BCS.blockMsgUpload);
    		$.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + '/admin/uploadPicture',
                cache: false,
                contentType: false,
                processData: false,
                data: form_data
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
        } 
	});

    var optionSelectChange_func = function(){
        var selectValue = $(this).find('option:selected').text();
        $(this).closest('.option').find('.optionLabel').html(selectValue);
    };

    var loadDataFunc = function(){

        $.ajax({
            type : "GET",
            async : false,
            url : bcs.bcsContextPath + '/admin/getCampaignList?isActive=true'
        }).success(function(response){
            console.info(response);

            $.each(response, function(i, o){

                var campaign = $('<option value=""></option>');

                campaign.val(o.campaignId);
                campaign.html(o.campaignName);

                $('.campaign').append(campaign);
            });

            $('.campaign').change(optionSelectChange_func);
            
        }).fail(function(response){
            console.info(response);
            $.FailResponse(response);
        }).done(function(){
        });
    }

    loadDataFunc();
});