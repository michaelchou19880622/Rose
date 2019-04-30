/**
 * 
 */
$(function(){
	var percent = 1000/566;
	var maxWidth = 768;
	var barcode = 241;

	var windowWidth = $(window).width(); 
	console.info('windowWidth', windowWidth);
	if(windowWidth + 0 >  maxWidth){
		windowWidth = maxWidth;
	}
	$('.mainDiv').css('width', windowWidth + 'px');
	
	var windowHeight = $(window).height() ; 
	console.info('windowHeight', windowHeight);
	windowHeight = windowWidth*percent;
	console.info('change windowHeight', windowHeight);
	$('.mainDiv').css('height', windowHeight + 'px');
	
	var trList = $('tr');
	$.each(trList, function(i, o){
		var heightPercent = $(o).attr('heightPercent');
		console.info('heightPercent', heightPercent);
		$(o).css('height', windowHeight * heightPercent / 100);
		$(o).find('a div').css('height', windowHeight * heightPercent / 100);
	});

	$.ajax({
		type : "GET",
		url : 'myIdSub'
	}).success(function(response){
		console.info(response);
		var position =  (windowWidth*.9-barcode)/2;
		if(position < 0){
			position = 0;
		}
		$('#mycardBarcode').css('left',position + 'px');
		
		$('#myIdSub').html(response);
		$('#myIdSub').css('left', (windowWidth/2-10) + 'px');
	}).fail(function(response){
		console.info(response);
 		window.location.replace('index');
	}).done(function(){
	});
});