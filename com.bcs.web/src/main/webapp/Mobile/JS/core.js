$(function(){
	
	$.urlParam = function(name){
		var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
		if(results == null){
			return null;
		}
		else{
			return results[1] || 0;
		}
	};

	Number.prototype.format = function(n, x) {
	    var re = '\\d(?=(\\d{' + (x || 3) + '})+' + (n > 0 ? '\\.' : '$') + ')';
	    return this.toFixed(Math.max(0, ~~n)).replace(new RegExp(re, 'g'), '$&,');
	};
	
	$.formatDate = function(date){
		return $.datepicker.formatDate("yy/mm/dd", date);
	}
});