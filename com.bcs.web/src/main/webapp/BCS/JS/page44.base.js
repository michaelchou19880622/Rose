/**
 * 
 * @returns
 */
$(function(){
	//var num = true ; //只新增一次  身分證字號
	var isTrueID = false;
//	var actionType = ${#authentication.principal.username} || 'adminCreate';
//	alert(actionType);
	var isBIND = 1; // 1 尚未查詢 2 查詢後未綁定 3查詢後已綁定
	
	function CheckID(){
		var userID = $('#userID').val();
		var CheckID = /^[a-zA-Z]{1}[1-2]{1}[0-9]{8}$/.test(userID);

		if(CheckID){
			//身分證字號檢驗
			var local_table = [10,11,12,13,14,15,16,17,34,18,19,20,21,22,35,23,24,25,26,27,28,29,32,30,31,33];
                             /* A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z */
			userID = userID.toUpperCase();  // 小寫轉大寫

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
		isBIND = 1 ;   // 1 尚未查詢 2 查詢後未綁定 3查詢後已綁定
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
					//查詢身分證之後顯示帳號狀態
					var Status = "" ;
					var RegisterDate  = stringToDate(response.ReturnData.RegisterDate);
					var RegisterTime  = response.ReturnData.RegisterTime.substring(0,5);
					var ModifyDate    = null ;
					if(response.ReturnData.Status == "BINDED"){
						Status = "已綁定";
						isBIND = 3 ; // 1 尚未查詢 2 查詢後未綁定 3查詢後已綁定
					}else{
						Status = "解除綁定";
						isBIND = 2 ; // 1 尚未查詢 2 查詢後未綁定 3查詢後已綁定
					}
					
					if(response.ReturnData.ModifyDate != null && response.ReturnData.ModifyDate != ''){
						ModifyDate = stringToDate(response.ReturnData.ModifyDate)+ " " +
									 response.ReturnData.ModifyTime.substring(0,5);
					}else{
						ModifyDate = "N/A"
					}
					
					//$(".showResult").html("LINE UID : <br><br>" + response.ReturnData.Luid ).css("color","blue").css("fontSize", "35px");
					$(".showResult").html(
							"LINE UID :  " + response.ReturnData.Luid + 
							"<br>目前狀態 : " + Status + 
							"<br>綁定時間 : " + RegisterDate + " " + RegisterTime+ 
							"<br>解綁時間 : " + ModifyDate).css("color","blue").css("fontSize", "20px");
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
			if(isBIND == 1){
				alert('請先查詢UID');
			}else if(isBIND == 2){
				alert('此ID目前尚未綁定');
			}else if(isBIND == 3){
				var userId = $('#userID').val();
				$("#dialog").css('display','block').dialog().css('text-align','center');	//秀資訊
				$("#showUserID").html("此步驟無法反悔<br>請確定要解除綁定<br><br>"+ userId);
			}
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
				var Status = "" ;
				var RegisterDate  = stringToDate(response.ReturnData.RegisterDate);
				var RegisterTime  = response.ReturnData.RegisterTime.substring(0,5);
				var ModifyDate    = stringToDate(response.ReturnData.ModifyDate)+ " " +
									response.ReturnData.ModifyTime.substring(0,5);
				if(response.ReturnData.Status == "BINDED"){
					Status = "已綁定";
				}else{
					Status = "解除綁定";
				}
				
				//$(".showResult").html("LINE UID : <br><br>" + response.ReturnData.Luid ).css("color","blue").css("fontSize", "35px");
				$(".showResult").html(
						//"&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp"+userId + " 已解除綁定 <br>"+
						"<br>LINE UID :  " + response.ReturnData.Luid + 
						"<br>目前狀態 : "     + Status + 
						"<br>綁定時間 : "     + RegisterDate + " " + RegisterTime+ 
						"<br>解綁時間 : "     + ModifyDate).css("color","blue").css("fontSize", "20px");
				alert('解除綁定成功');
			}else if(response.ReturnCode == 'E001'){
				//顯示錯誤訊息
				$(".showResult").html(response.ReturnMessage ).css("color","red").css("fontSize", "20px");
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
											  "fontSize": "20px"});
	}
	
	function showblank(){
		$(".CheckID").text("");
	}
	
	function stringToDate(day){
		var startDate = moment(day, "YYYY/MM/DD");
		var dt_to = $.datepicker.formatDate('yy/mm/dd', new Date(startDate));
		return dt_to;
	}
	
});