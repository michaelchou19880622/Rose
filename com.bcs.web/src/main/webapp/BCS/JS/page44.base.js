/**
 * 
 * @returns
 */
$(function(){
	//var num = true ; //只新增一次  身分證字號
	var isTrueID = false;
	
//	var actionType = ${#authentication.principal.username} || 'adminCreate';
//	alert(actionType);


	function CheckID(){
		var userID = $('#userID').val();
		var CheckID = /^[A-Z]{1}[1-2]{1}[0-9]{8}$/.test(userID);
		if(CheckID){
			//身分證字號檢驗
			var local_table = [10,11,12,13,14,15,16,17,34,18,19,20,21,22,35,23,24,25,26,27,28,29,32,30,31,33];
                             /* A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z */

			var local_digit = local_table[userID.charCodeAt(0)-'A'.charCodeAt(0)];
			
		    var checksum = 0;

		    checksum += Math.floor(local_digit / 10);
		    checksum += (local_digit % 10) * 9;


		    for(var i=1, p=8; i <= 8; i++, p--){
		    	checksum += parseInt(userID.charAt(i)) * p;
		    }
		    checksum += parseInt(userID.charAt(9));
		    if((checksum % 10) != 0){
		    	isTrueID = false ;
		    }else{
		    	isTrueID = true ;
		    }
		}else{
			isTrueID = false ;
		}
	};
	
	$('#userID').blur(function(){
		CheckID();
		$(".showResult").html("");
		if(isTrueID){
			showblank();
		}else{
			showerror();
		}
	});
	
	
	$('#InquireUID').click(function(){
		
		if(!isTrueID){
			showerror();
		}else{
			showblank();
			var userId = $('#userID').val();
			
			$.ajax({
				
				type : "POST",
				url : bcs.bcsContextPath + '/tsmb/inquireUserLineUrl?userId=' + userId ,
	            cache: false,
	            contentType: 'application/json',
	            processData: false,
			}).success(function(response){
				console.info(response);
				response = JSON.parse(response);      //string to Json
				if(response.ReturnCode == 'S001'){
					//顯示UID
					$(".showResult").html("LINE UID : <br><br>" + response.ReturnData.Luid ).css("color","blue").css("fontSize", "35px");
				}else if(response.ReturnCode == 'E001'){
					//顯示錯誤訊息
					$(".showResult").html(response.ReturnMessage ).css("color","red").css("fontSize", "35px");
				}

			}).fail(function(response){
				console.info(response);
				$(".showResult").html('查無此ID').css("color","red").css("fontSize", "35px");
			}).done(function(){
			});
		
		}
	});
	
	//彈跳視窗 再次確認是否解除
	$('#Unbind').click(function(){
		if(!isTrueID){
			showerror();
		}else{
			showblank();
			var userId = $('#userID').val();
			$("#dialog").css('display','block').dialog().css('text-align','center');	//秀資訊
			$("#showUserID").html("此步驟無法反悔<br>請確定要解除綁定<br><br>"+ userId);
		}
	});
	//彈跳視窗 取消解除
	$('#cancel').click(function(){
		 $("#dialog").dialog("close");
	});
	
	//彈跳視窗 確定解除
	$('#determine').click(function(){
		$("#dialog").dialog("close");
		var userId = $('#userID').val();
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/tsmb/unbindUserLineUrl?userId=' + userId,
            cache: false,
            contentType: 'application/json',
            processData: false,
		}).success(function(response){
			console.info(response);
			response = JSON.parse(response);      //string to Json
			if(response.ReturnCode == 'S001'){
				//顯示UID
				$(".showResult").html("身分證字號 : "+ userId + "   " +response.ReturnMessage +"<br><br> LINE UID : <br><br>" + response.ReturnData.Luid ).css("color","blue").css("fontSize", "35px");
			}else if(response.ReturnCode == 'E001'){
				//顯示錯誤訊息
				$(".showResult").html(response.ReturnMessage ).css("color","red").css("fontSize", "35px");
			}

		}).fail(function(response){
			console.info(response);
			$(".showResult").html('查無此ID' ).css("color","red").css("fontSize", "35px");
			//$.FailResponse(response);
		}).done(function(){
		});
	});
	
	function showerror(){
		$(".CheckID").text("身分證格式錯誤").css({ "color":"red",
											  "fontSize": "10px"});
	}
	
	function showblank(){
		$(".CheckID").text("");
	}
	
});