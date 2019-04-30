/**
 * 
 */

$(function(){
	$('#turntable_option').click(function(){
 		window.location.replace(bcs.bcsContextPath + '/edit/gameCreatePage/turntable');
	});
	
	$('#scratchCard_option').click(function(){
 		window.location.replace(bcs.bcsContextPath + '/edit/gameCreatePage/scratchCard');
	});
	
	//取消鈕
	$('input[name="cancel"]').click(function() {
		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		
		window.location.replace(bcs.bcsContextPath + '/edit/gameListPage');
	});
});