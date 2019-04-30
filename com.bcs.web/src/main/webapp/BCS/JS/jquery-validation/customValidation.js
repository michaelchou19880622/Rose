/**
 * 自訂 jQuery Validation Plugin 的驗證規則
 */

/**
 * 驗證日期格式需符合 YYYY-MM-DD
 */
$.validator.addMethod("dateYYYYMMDD", function(value, element, params) {
	return this.optional(element) || /^\d{4}\-\d{2}\-\d{2}$/.test(value);
}, $.validator.format("日期格式不正確，需符合 YYYY-MM-DD"));

/**
 * 比較日期大小
 * 
 * 範例：
	'demoThisDate' : {
		required : true,
		dateYYYYMMDD : true,
		compareDate : {
			compareType : 'after', // 可接受：same、after、sameOrAfter、before、sameOrBefore
			dateFormat : 'YYYY-MM-DD', // 必須是 Moment.js 使用的日期格式
			getThisDateStringFunction : function() { // 取得本欄位的值
				return $('#demoThisDate').val();
			},
			getAnotherDateStringFunction : function() { // 取得另一個欄位的值
				return $('#demoAnotherDate').val();
			},
			thisDateName : 'demo結束日期', // 本欄位的顯示名稱，給錯誤訊息使用
			anotherDateName : 'demo開始日期' // 另一個欄位的顯示名稱，給錯誤訊息使用
		}
	},
 */
$.validator.addMethod("compareDate", function(value, element, params) {	
	var compareType = params.compareType;
	var dateFormat = params.dateFormat;
	var anotherDateString = params.getAnotherDateStringFunction();
	var momentAnotherDate = moment(anotherDateString, dateFormat);
	var thisDateString = params.getThisDateStringFunction();
	var momentThisDate = moment(thisDateString, dateFormat);
		
	// 若有一個日期格式不對，就不驗證
	if (!momentThisDate.isValid() || !momentAnotherDate.isValid()) {
		return true;
	}
	
	var result = false;
	
	switch (compareType) {
	case 'same':
		result = momentThisDate.isSame(momentAnotherDate);
		break;
	case 'after':
		result = momentThisDate.isAfter(momentAnotherDate);
		break;
	case 'sameOrAfter':
		result = momentThisDate.isSameOrAfter(momentAnotherDate);
		break;
	case 'before':
		result = momentThisDate.isBefore(momentAnotherDate);
		break;
	case 'sameOrBefore':
		result = momentThisDate.isSameOrBefore(momentAnotherDate);
		break;
	default:
		break;
	}
	
	return this.optional(element) || result;
}, function(params, element) {
	var errorMessage = '';
	var compareType = params.compareType;
	
	switch (compareType) {
	case 'same':
		errorMessage = jQuery.validator.format('{0} 必須等於 {1}', params.thisDateName, params.anotherDateName);
		break;
	case 'after':
		errorMessage = jQuery.validator.format('{0} 必須大於 {1}', params.thisDateName, params.anotherDateName);
		break;
	case 'sameOrAfter':
		errorMessage = jQuery.validator.format('{0} 必須大於或等於 {1}', params.thisDateName, params.anotherDateName);
		break;
	case 'before':
		errorMessage = jQuery.validator.format('{0} 必須小於 {1}', params.thisDateName, params.anotherDateName);
		break;
	case 'sameOrBefore':
		errorMessage = jQuery.validator.format('{0} 必須小於或等於 {1}', params.thisDateName, params.anotherDateName);
		break;
	default:
		break;
	}
	
	return errorMessage;
});

/**
 * 驗證日期格式需符合 YYYY-MM-DD
 */
$.validator.addMethod("minNumeric", function(value, element, params) {
	return value > params;
}, $.validator.format("需大於{0}"));