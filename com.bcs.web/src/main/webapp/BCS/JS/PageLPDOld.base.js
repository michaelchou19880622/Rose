$(function(){
		$('.btn_add').click(function(){
	 		window.location.replace(bcs.bcsContextPath + '/market/linePointReportPage');
		});

		var loadDataFunc = function(){
			// params
			var mainId = $.urlParam("mainId");
			var status = $.urlParam("status");
			console.info("mainId:", mainId);
			console.info("status:", status);
			
			var url = "";
			if(status == "SUCCESS"){
				url = bcs.bcsContextPath + '/market/getSuccessLinePointDetailList/' + mainId
			}else{
				url = bcs.bcsContextPath + '/market/getFailLinePointDetailList/' + mainId
			}
			
			$.ajax({
				type : "GET",
				url : url
			}).success(function(response){
				
				console.log(response);
				$('.dataTemplate').remove();
				console.info(response);
		
				$.each(response, function(i, o){
					var groupData = templateBody.clone(true); //增加一行
					console.info(groupData);
					groupData.find('.uid').html(o.uid);

					if(status == "SUCCESS"){
						groupData.find('.status').html(o.transactionBalance);
					}else{
						groupData.find('.status').html(o.description);
					}
					
					if(o.triggerTime){
						groupData.find('.time').html(moment(o.triggerTime).format('YYYY-MM-DD HH:mm:ss'));
					}else{
						groupData.find('.time').html('-');
					}
					
		
					$('#tableBody').append(groupData);
				});
				
				// set excel link
				var exportUrl = bcs.bcsContextPath + '/edit/exportToExcelForLPPushApiEffectsDetail/' + mainId + '/' + status;	
				$('#btnExcel').attr('href', exportUrl);
				
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
		};
		
		var templateBody = {};
		templateBody = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		
		loadDataFunc();
	});

 