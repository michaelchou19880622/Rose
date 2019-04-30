/**
 * 
 */
$(function(){
	var percent = 1000/622;
	var maxWidth = 768;

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
});