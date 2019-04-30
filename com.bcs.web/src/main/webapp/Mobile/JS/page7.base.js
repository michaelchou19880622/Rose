/**
 * 
 */
$(function(){	
	var percent = 1000/446;
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

	var bottonPercent = 300/750;
	$('.TermsOfBusiness').css('margin-bottom', windowWidth*bottonPercent);
	
	$('#checkboxImage').click(function(event) {
		$(this).hide();
		$('#checkboxCheckedImage').show();
	});
	
	$('#checkboxCheckedImage').click(function(event) {
		$(this).hide();
		$('#checkboxImage').show();
	});
	
	// [下一步]按鍵，點擊前若未勾選 "我已詳閱..." checkbox 就顯示提醒訊息
	$('#goUserBindingPage').click(function(event) {

		var Gender = $('[name="Gender"]:checked').val();
		console.info('Gender', Gender);
		if(!Gender){
			alert('請選擇 性別');
			return;
		}
		
		var BirthdayYear = $('#BirthdayYear').val();
		console.info('BirthdayYear', BirthdayYear);
		
		if(!BirthdayYear){
			alert('請選擇 生日 - 年');
			return;
		}

		var BirthdayMonth = $('#BirthdayMonth').val();
		console.info('BirthdayMonth', BirthdayMonth);

		if(!BirthdayMonth){
			alert('請選擇 生日 - 月');
			return;
		}
		
		var PhoneNum = $('#PhoneNum').val();
		console.info('PhoneNum', PhoneNum);
		
		if(!PhoneNum){
			alert('請輸入行動電話');
			return;
		}
		
		if(PhoneNum.length != 10){
			alert('請輸入正確的行動電話');
			return;
		}

		if ($('#checkboxCheckedImage').is(':hidden')) {
			alert('請閱讀並同意服務條款');
			return;
		}
		
		$('#formDoBinding').submit();
		return false;
	});
});