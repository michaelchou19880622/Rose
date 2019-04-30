/**
 * 
 */
$(function(){
	$('#openPopUpNocard').click(function(event) {
		$('.gridContainer').hide();
		$('#popUpNocard').show();
		return false;
	});
	
	$('.closepopbox').click(function(event) {
		$(this).closest('div.popbox').hide();
		$('.gridContainer').show();
		return false;
	});
	
	$("#btn_line").click(function(){
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
		
		var BirthdayDay = $('#BirthdayDay').val();
		console.info('BirthdayDay', BirthdayDay);

		if(!BirthdayDay){
			alert('請選擇 生日 - 日');
			return;
		}
		
		$('#formDoGetCard').submit();
	});
	
	$('#BirthdayYear').val('1990');
});