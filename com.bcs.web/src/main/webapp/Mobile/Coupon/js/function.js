$(document).ready(function() {
    
    var section_h = $(".coupon_alert").height();
    var obj_h = $(".coupon_alert > strong").height();
    
    var pos_top = Math.ceil((section_h - obj_h)/2)-60;
    
    $(".coupon_alert > strong").css("top",pos_top);
});