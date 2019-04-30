$(function(){

	// 日期元件
	$(".datepicker").datepicker({
		'maxDate' : 0, //最多只能選至今天
		'dateFormat' : 'yy-mm-dd'
	});
	
	var validateTimeRange = function() {
		var startDate = moment($('#startDate').val(), "YYYY-MM-DD");
		var endDate = moment($('#endDate').val(), "YYYY-MM-DD");
		if (!startDate.isValid()) {
			alert("請選擇起始日期");
			return false;
		}
		if (!endDate.isValid()) {
			alert("請選擇結束日期");
			return false;
		}
		if (startDate.isAfter(endDate)) {
			alert("起始日不能大於結束日");
			return false;
		}
		
		return true;
	}
	
	// 前往電子序號建立頁面
	$('.btn_add').click(function(){
 		window.location.replace(bcs.bcsContextPath +'/edit/esnCreatePage');
	});
	
	$('.btn_save').click(function(){
		
		if(!validateTimeRange()){
			return false;
		}
		
		var startDate = $('#startDate').val();
		var endDate = $('#endDate').val();
		
		loadDataFunc(startDate, endDate);
	});
	
	// 刪除按鈕
//	var btn_delFunc = function(){
//		var esnId = $(this).attr('esnId');
//		console.info('btn_delFunc esnId:' + esnId);
//
//		var r = confirm("請確認是否刪除");
//		if (r) {
//			
//		} else {
//		    return;
//		}
//		$('.LyMain').block($.BCS.blockMsgRead);
//		
//		$.ajax({
//			type : "DELETE",
//			url : bcs.bcsContextPath + '/admin/deleteEsn?esnId=' + esnId
//		}).success(function(response){
//			console.info(response);
//			alert("刪除成功");
//			loadDataFunc();
//		}).fail(function(response){
//			console.info(response);
//			$.FailResponse(response);
//			$('.LyMain').unblock();
//		}).done(function(){
//			$('.LyMain').unblock();
//		});
//		
//	};

	// 發送電子序號
	$('.btn_css').on('click', function(){
		
		var thisElement = $(this);
		
		if(confirm('是否發送電子序號?')){

    		var form_data = new FormData();

    		form_data.append("esnId", thisElement.attr('esnId'));

    		$('.LyMain').block($.BCS.blockMsgUpload);
    		$.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + '/edit/sendEsnMsg',
                cache: false,
                contentType: false,
                processData: false,
                data: form_data
    		}).success(function(response){
            	console.info(response);
            	thisElement.off('click');
            	thisElement.closest('tr').find('.finishCount').html('發送中(待統計)');
            	thisElement.closest('tr').find('.lastCount').html('發送中(待統計)');
            	thisElement.closest('tr').find('.btn_css').css('background', '#d3d3d3');
            	alert("電子序號發送中"); 	
    		}).fail(function(response){
    			console.info(response);
    			$.FailResponse(response);
    			$('.LyMain').unblock();
    		}).done(function(){
    			$('.LyMain').unblock();
    		});
		}
	});
	
	var loadDataFunc = function(startDate, endDate){

		var getUrl = bcs.bcsContextPath + '/edit/getEsnList';
		
		if(startDate && endDate){
			getUrl = bcs.bcsContextPath + '/edit/getEsnList?startDate='+ startDate +'&endDate='+ endDate;
		}
		
		$('.LyMain').block($.BCS.blockMsgRead);
		
		$.ajax({
			type : "GET",
			url : getUrl
		}).success(function(response){
			$('.dataTemplate').remove();
			if (response){

				for(key in response) {
					var groupData = templateBody.clone(true);
					
					groupData.find('.esnName a').html(response[key].esnName);
					
					groupData.find('.esnMsg').html(response[key].esnMsg);
						
					groupData.find('.modifyUser').html(moment(response[key].modifyTime).format("YYYY/MM/DD HH:mm:ss") +'<br/>'+ response[key].modifyUser);
					
					if(response[key].status == 'ACTIVE'){
						groupData.find('.esnName a').attr('href', bcs.bcsContextPath +'/edit/esnCreatePage?esnId=' + response[key].esnId);
						groupData.find('.totalCount').html(response[key].totalCount);
						
						if(response[key].sendStatus == 'READY'){
							groupData.find('.finishCount a').html(response[key].finishCount);
							groupData.find('.finishCount a').attr('href', bcs.bcsContextPath +'/edit/exportToExcelForEsnFinish?esnId=' + response[key].esnId);
							groupData.find('.lastCount').html(response[key].totalCount - response[key].finishCount);
							groupData.find('.btn_css').attr('esnId', response[key].esnId);
						}
						else if(response[key].sendStatus == 'FINISH'){
							groupData.find('.finishCount a').html(response[key].finishCount);
							groupData.find('.finishCount a').attr('href', bcs.bcsContextPath +'/edit/exportToExcelForEsnFinish?esnId=' + response[key].esnId);
							groupData.find('.lastCount').html(response[key].totalCount - response[key].finishCount);
							groupData.find('.btn_css').css('background', '#d3d3d3');
							groupData.find('.btn_css').off('click');
						}
						else{
							groupData.find('.finishCount').html('發送中');
							groupData.find('.lastCount').html('發送中');
							groupData.find('.btn_css').css('background', '#d3d3d3');
							groupData.find('.btn_css').off('click');
						}
					}
					else{
						groupData.find('.totalCount').html('建立中');
						groupData.find('.finishCount').html('建立中');
						groupData.find('.lastCount').html('建立中');
						groupData.find('.btn_css').css('background', '#d3d3d3');
						groupData.find('.btn_css').off('click');
					}

					$('#tableBody').append(groupData);
				}
			}
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
			$('.LyMain').unblock();
		}).done(function(){
			$('.LyMain').unblock();
		});
	};

	var templateBody = {};
	templateBody = $('.dataTemplate').clone(true);
	$('.dataTemplate').remove();
	
	loadDataFunc();
});