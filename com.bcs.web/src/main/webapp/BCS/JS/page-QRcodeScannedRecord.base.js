$(function() {
	initialize();
	
	function initialize() {
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/getQRcodeScannedCount'
		}).success(function(response){
			var exportAPI = bcs.bcsContextPath + '/admin/exportQRcodeScannedRecord';
			
			$('.QrcodeScannedCount').html('<a href="' + exportAPI + '">' + response + '</a>');
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	}
});