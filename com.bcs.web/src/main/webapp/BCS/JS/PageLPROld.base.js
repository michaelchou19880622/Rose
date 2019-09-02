$(function(){
		$('#btnCreate').click(function(){
	 		window.location.replace(bcs.bcsContextPath + '/market/linePointPushPage');
		});
		
		var loadDataFunc = function(){
			
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/market/getAllLinePointMainList'
			}).success(function(response){
				
				console.log(response);
				$('.dataTemplate').remove();
				console.info(response);
		
				$.each(response, function(i, o){
					var groupData = templateBody.clone(true); //增加一行
					console.info(groupData);
					groupData.find('.serialId').html(o.serialId);
					groupData.find('.title').html(o.title);
					if(o.modifyTime){
						groupData.find('.modifyTime').html(moment(o.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
					}else{
						groupData.find('.modifyTime').html('-');
					}
					
					groupData.find('.amount').html(o.amount);
					groupData.find('.totalCount').html(o.totalCount);
				 	groupData.find('.status').html(o.status);
				 	groupData.find('.sendType').html(o.sendType);
				 	groupData.find('.modifyUser').html(o.modifyUser);
				 	
				 	var link1 = bcs.bcsContextPath + '/market/linePointDetailPage?mainId=' + o.id + '&status=SUCCESS';
	 				groupData.find('.successfulCount').html('<a>' + o.successfulCount + '</a>').find('a').attr('href', link1);
				 	var link2 = bcs.bcsContextPath + '/market/linePointDetailPage?mainId=' + o.id + '&status=FAIL';
	 				groupData.find('.failedCount').html('<a>' + o.failedCount + '</a>').find('a').attr('href', link2);
	 				var link3 = bcs.bcsContextPath + '/market/linePointSchedulePage?mainId=' + o.id;
	 				groupData.find('.schedule').html('<a>' + '查看' + '</a>').find('a').attr('href', link3);
					$('#tableBody').append(groupData);
				});
				
				// set excel link
				var exportUrl = bcs.bcsContextPath + '/edit/exportToExcelForLPPushApiEffects';	
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

 