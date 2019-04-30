/**
 *
 */
$(function(){

	var serialTrTemplate = {};
	
	var initSerialTrTemplate = function(){
		
		serialTrTemplate = $('.serialTrTemplate').clone(true);
		$('.serialTrTemplate').remove();
	};
	
	initSerialTrTemplate();

	var loadDataFunc = function(){
		$.ajax({
	        type: 'GET',
	        url: bcs.bcsContextPath + "/edit/getSerialSettingList",
		}).success(function(response){
			$('.serialTrTemplate').remove();
			
			var SerialSettingList = response.SerialSettingList;
			var AdminUser = response.AdminUser;
	
			$.each(SerialSettingList, function(i, o){
				var serialTr = serialTrTemplate.clone(true);
				
				try{
					serialTr.find('.serialFile a').attr('href', bcs.bcsContextPath +'/edit/serialSettingPage?SerialId=' + o.serialId);
					serialTr.find('.serialFile a').html(o.serialTitle);
					
					serialTr.find('.serialCount').html(o.serialCount);

					serialTr.find('.modifyTime').html(o.modifyTime);
					serialTr.find('.modifyUser').html(AdminUser[o.modifyUser]);
					
					$('#serialListTable').append(serialTr);
				}
				catch(err) {
					console.error(err);
				} 
			});
	
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	}
	
	loadDataFunc();
});
