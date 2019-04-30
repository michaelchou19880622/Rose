$(function(){

	var esnId = $.urlParam("esnId");
	
	// 表單驗證
	var validatorForCreate = $('#formCreateEsn').validate({
		rules : {

			// 發送群組
			'esnName' : {
				required : true,
				maxlength : 50
			},
			'esnMsg' : {
				required : true,
				maxlength : 500
			},
			'file' : {
				required : true
			}
		}
	});
	
	var validatorForEdit = $('#formCreateEsn').validate({
		rules : {

			// 發送群組
			'esnName' : {
				required : true,
				maxlength : 50
			},
			'esnMsg' : {
				required : true,
				maxlength : 500
			}
		}
	});
	
	// 綁訂計算字數函式到輸入框
	var bindCountTextFunctionToInput = function() {
		$('#esnName').keyup(function() {
			var txtLength = $(this).val().length;
			var tr = $(this).closest("tr");
			var inputCount = tr.find(".MdTxtInputCount");
			var countText = inputCount.text();
			inputCount.text(countText.replace(/\d+\//, txtLength + '/'));
		});
		
		$("#esnMsg").keyup(function(e) {
			var txtLength = $(this).val().length;
			var tr = $(this).closest("tr");
			var inputCount = tr.find(".floatRight");
			var countText = inputCount.text();
			inputCount.text(countText.replace(/\d+\//, txtLength + '/'));
		});

	};
	bindCountTextFunctionToInput();
	
	$('#file').on("change", function(ev){
		
		var input = ev.currentTarget;
    	if (input.files && input.files[0]) {
    		var fileName = input.files[0].name;
    		$('.fileTag').text('已上傳 '+ fileName);
    	}	
	});
	
	// 建立電子序號
	$('.btn_save').click(function(){
		saveEsn();
	});
	
	$('.btn_cancel').click(function(){
		
		if (confirm("請確認是否取消")) {
			window.location.replace(bcs.bcsContextPath +'/edit/esnListPage');
		} else {
		    return;
		}
	});
	
	var saveEsn = function() {
		// 檢查表單是否驗證成功
		if(esnId){
			if (!validatorForEdit.form()) {
				return;
			}
		}
		else{
			if (!validatorForCreate.form()) {
				return;
			}
		}
		
		if (!confirm("請確認是否儲存")) {
			return;
		}
		
		$('.LyMain').block($.BCS.blockMsgRead);
		
		setTimeout(function(){
			// 發動ajax將資料傳送至server端
			var formData = new FormData();
			formData.append("esnName", $('#esnName').val());
			formData.append("esnMsg", $('#esnMsg').val());
			if(esnId){
				formData.append("esnId", esnId);
			}
			else{
				formData.append("file", $('#file')[0].files[0]);
			}
			
			$.ajax({
		        url: bcs.bcsContextPath + '/admin/createEsn',
		        type: 'POST',
		        data: formData,
		        async: false,
		        cache: false,
		        contentType: false,
		        processData: false
			}).success(function(response) {
				console.info(response);
				if(esnId){
					alert("電子序號修改成功");
				}
				else{
					alert("電子序號建立成功");
				}
				window.location.replace(bcs.bcsContextPath +'/edit/esnListPage');
			}).fail(function(response) {
				console.info(response);
				$.FailResponse(response);
				$('.LyMain').unblock();
			}).done(function() {
				$('.LyMain').unblock();
			});
		}, 500);
	};
	
	var loadDataFunc = function(){

		if (esnId) {
			
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/edit/getContentEsnMain?esnId=' + esnId
			}).success(function(response){
				console.info(response);
				
				// 標題
				$('#esnName').val(response.esnName);

				// 分享次數
				$('#esnMsg').val(response.esnMsg);

            	// 計算字數
            	$('#esnName').keyup();
            	$('#esnMsg').keyup();
            	
            	// 編輯禁止上傳電子序號
            	$('#file').remove();
            	$('.mdBtnUploadTxt').css('cursor', 'not-allowed');
            	$('.MdBtnUpload').css('background', '#fff');
            	
            	if(response.sendStatus != 'READY'){
            		$('.btn_save').remove();
            	}
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
		}
	};
	
	loadDataFunc();
});