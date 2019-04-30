/**
 * 
 */


var isMobile = false; //initiate as false
// device detection
if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|ipad|iris|kindle|Android|Silk|lge |maemo|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(navigator.userAgent) 
    || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(navigator.userAgent.substr(0,4))) isMobile = true;

if(!isMobile){
	window.location.replace('shareNotMobilePage');
}

$(function(){
	var percent = 3300/750;
	var maxWidth = 750;

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
	
	$('#eventShareTrigger').click(function(){

		$('.mainDiv').block({ "message" : "處理中...."});
		$.ajax({
			type : "POST",
			url : 'eventShareTrigger?eventId=EventShare1',
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : ""
		}).success(function(response){
			console.info(response);
			console.info("用LINE傳送成功");
			$('.mainDiv').unblock();
	 		window.location.assign('line://msg/text/?%e8%b7%9fIKEA%e5%ae%98%e6%96%b9%e5%b8%b3%e8%99%9f%e7%95%b6%e5%a5%bd%e5%8f%8b%ef%bc%8c%e5%af%a6%e5%9c%a8%e5%a4%aa%e7%aa%a9%e5%bf%83%ef%bc%8c%e4%b8%b2%e8%81%af%e5%ae%9c%e5%ae%b6%e5%8d%a1%e5%be%8c%e9%82%84%e6%9c%89%e5%b0%88%e5%b1%ac%e5%84%aa%e6%83%a0%e3%80%81%e7%8d%a8%e5%ae%b6%e6%8e%a8%e8%96%a6%ef%bc%8c%e7%8f%be%e5%9c%a8%e5%8d%87%e7%b4%9a%e9%82%84%e6%9c%89%e6%a9%9f%e6%9c%83%e7%8d%b2%e5%be%97%e7%8f%be%e6%8a%98%e5%84%aa%e6%83%a0%e5%92%8c%e8%b3%bc%e7%89%a9%e9%87%91%0d%0ahttp%3a%2f%2fbit.ly%2fikea_bc');
		}).fail(function(response){
			console.info(response);
			console.info("用LINE傳送失敗");
			$('.mainDiv').unblock();
			window.location.replace('index?event=SHARE_CARD');
		}).done(function(){
		});
	})
});