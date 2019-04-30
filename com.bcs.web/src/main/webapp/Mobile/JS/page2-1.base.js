/**
 * 
 */
$(function(){
	$("#btn_line").click(function(){

		var PhoneNum = $('#PhoneNum').val();
		console.info('PhoneNum', PhoneNum);
		
		if(!PhoneNum){
			alert('請輸入行動電話');
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
		
		var BirthdayDay = $('#BirthdayDay').val();
		console.info('BirthdayDay', BirthdayDay);

		if(!BirthdayDay){
			alert('請選擇 生日 - 日');
			return;
		}
		
		$('#formDoBinding').submit();
	});
	
	$('#BirthdayYear').val('1990');
});