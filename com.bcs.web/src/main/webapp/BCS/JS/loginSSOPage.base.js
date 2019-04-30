/**
 * 
 */
$(function(){
	var id;
	var roles;
	
	var loadDataFunc = function(){
		id = $.urlParam("id");
		roles = $.urlParam("roles");
		
		if(roles == null){
			roles = "";
		}
		
		$.ajax({
			type : "GET",
			url : "../m/loginSSO/getusername?id="+id + "&roles=" + roles
		}).success(function(response){
//			alert(response.account);
			if(response){
				$('#id').val(response.account);
	//			alert(response.password);
				$('#passwd').val(response.password);
			}
			$('#login').click();
		}).fail(function(response){
			console.info(response);
			var json = response.responseJSON;
			alert('錯誤發生 請找開發人員協助\n\n[' + json.msg + "]");
			window.location.replace(json.loginurl);
		}).done(function(){
		});
	};
	
	loadDataFunc();
});