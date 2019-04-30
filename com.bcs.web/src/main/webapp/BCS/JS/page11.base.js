/**
 * 
 */
$(function(){
	// 日期元件
	$(".datepicker").datepicker({
		'maxDate' : 0, //最多只能選至今天
		'dateFormat' : 'yy-mm-dd'
	});
	
	var validateTimeRange = function(){

		var startDate = moment($('#pushReportStartDate').val(), "YYYY-MM-DD");
		var endDate = moment($('#pushReportEndDate').val(), "YYYY-MM-DD");
		if (!startDate.isValid()) {
			alert("請選擇起始日期");
			return false;
		}
		if (!endDate.isValid()) {
			alert("請選擇結束日期");
			return false;
		}
		if (startDate.isAfter(endDate)) {
			alert("起始日不能大於結束日");
			return false;
		}
		
		return true;
	}
	
	$('.query').click(function(){
		if(!validateTimeRange()){
			return false;
		}
		
		loadDataFunc();
	});
	
	$('.create').click(function() {
 		window.location.replace('reportCreatePage');
	});
	
	var setBtnEvent = function() {
		//設定詳細按鈕
		$('.detail').click(function(){
			var reportData = $(this).closest("tr");
			var reportId = reportData.find('.reportId').val();
			
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/edit/getPushReport?reportId=' + reportId + '&actionType=Query'
			}).success(function(response){
				console.log("Report Value:" + response);
				
				//24個欄位資料
				$.each($('#detailDialog').find('.dialogData'), function(i, v) {
					$(this).html(response[i]);
					if (i == 6) {
						$(this).html(response[i] + "%"); //CTR欄位
					}
				});
				
				if(response[17] != null && response[17] != ""){
					$('#dialogDataImg').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + response[17]);
					$('#dialogDataImg').show();
				} else {
					$('#dialogDataImg').hide();
				}
				
				$('#detailDialog').dialog('open');
			}).fail(function(response){
				console.info(response);
			}).done(function(){
			});
		});
		
		//設定新增連結按鈕
		$('.btn_add_link').click(function(){
			var reportData = $(this).closest("tr");
			var richId = reportData.find('.richId').val();
			var reportMsgSendId = reportData.find('.reportMsgSendId').val();
	 		window.location.replace('reportCreatePage?richId=' + richId + '&reportMsgSendId=' + reportMsgSendId + '&actionType=Create');
		});
		
		//設定編輯按鈕
		$('.edit').click(function(){
			var reportData = $(this).closest("tr");
			var reportId = reportData.find('.reportId').val();
	 		window.location.replace('reportCreatePage?reportId=' + reportId + '&actionType=Edit');
		});
		
		//設定刪除按鈕
		$('.btn_detele').click(function(e) {
			if (!confirm("請確認是否刪除？")) return false; //點擊取消
			
			var reportData = $(this).closest("tr");
			var reportId = reportData.find('.reportId').val();
			$.ajax({
				type : "DELETE",
				url : bcs.bcsContextPath + '/admin/deletePushReport?reportId=' + reportId
			}).success(function(response){
				alert("刪除成功！");
				loadDataFunc();
			}).fail(function(response){
				console.info(response);
			}).done(function(){
			});
		});
	}
	
	//替換上週日期至本週日期
	var parseMapDate = function(items) {
		var twoWeekDays = items.length;
		//日期由大至小排序
		for (var i=0; i<twoWeekDays; i++) {
			for (var j=i+1; j<twoWeekDays; j++) {
				var nowItem = Number(items[i].x.replace(/\-/g, ""));
				var nextItem = Number(items[j].x.replace(/\-/g, ""));
				if (nowItem < nextItem) {
					var tmp = items[i]
					items[i] = items[j];
					items[j] = tmp;
				}
			}
		}
		
		var startDate = $('#pushReportStartDate').val();
		var endDate = $('#pushReportEndDate').val();
		var oneWeekdays = parseInt((new Date(endDate) - new Date(startDate)) / 86400000) + 1;
		
		//取代上一週的日期為本週日期，方便比較圖顯示
		for (var i=0; i<twoWeekDays; i++) {
			if (items[i].group == 2) { //上一週
				var pushReportTime = moment(items[i].x, "YYYY-MM-DD");
				var pushReportDate = pushReportTime.add(oneWeekdays, 'day').format("YYYY-MM-DD");
				items[i].x = pushReportDate;
			}
		}
		
		return items;
	}
	
	//產生圖表
	var showGraph = function(container, response, multi){
		var items = [];
		var groupId = 1;
		$.each(response, function(i, map){
			
			$.each(map, function(key, value){
				var data = { 
					x : key,
					y : value.clickRate, 
					group: groupId,
					label: {content: value.clickRate + "%", xOffset: 5, yOffset: 2}
				};
				items.push(data);
			});
			
			groupId++;
		});
		
		items = parseMapDate(items);

		var dataset = new vis.DataSet(items);
		
		//設定group
		var groups = new vis.DataSet();
		groups.add({
			id: 1,
			content: "本週"
		});
		groups.add({
			id: 2,
			content: "上週"
		});
		
		var queryStartDate = moment($('#pushReportStartDate').val(), "YYYY-MM-DD");
		var queryEndDate = moment($('#pushReportEndDate').val(), "YYYY-MM-DD");
		var start = queryStartDate.subtract(6, 'hour');
		var end = queryEndDate.add(1, 'day');
		
		var options = {
			    start: start,
			    end: end,
			    showCurrentTime: false,
			    graphHeight: '300px',
			    timeAxis: {scale: 'day', step: 1},
			    moveable: false, //圖表不可移動
			    legend: {left:{position:"top-right"}}, //顯示分類
		};
		
		var graph2d = new vis.Graph2d(container, dataset, groups, options);
	}
	
	var dataTemplate = {};
	var initTemplate = function(){
		dataTemplate = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
		
		var nowDate = moment(); //取得現在時間
		var lastWeek = moment().dates(nowDate.dates() - 6); //取得前7天(上一週)的時間
		
		$('#pushReportStartDate').val(lastWeek.format('YYYY-MM-DD'));
		$('#pushReportEndDate').val(nowDate.format('YYYY-MM-DD'));
		
		$('#detailDialog').dialog({
	    	autoOpen: false, //初始化不會是open
	    	resizable: false, //不可縮放
	    	modal: true, //畫面遮罩
	    	draggable: false, //不可拖曳
	    	minWidth : 700,
	    	position: { my: "top", at: "top", of: window  }
	    });
	}
	
	//解悉資料庫回傳的值轉換成陣列
	var parseReportListData = function(valueObj) {
		var multiTxt = valueObj[3].split(";");
		var multiUrl = valueObj[4].split(";");
		var multiTrackingCode = valueObj[5].split(";");
		var multiClick = valueObj[6].split(";");
		var multiClickThrough = valueObj[7].split(";");
		var multiVisit = valueObj[8].split(";");
		var multiProuctView = valueObj[9].split(";");
		var multiReportId = valueObj[10].split(";");
		
		var result = [];
		for (var i=0; i<multiTxt.length; i++) { //richMsg連結筆數
			if (i == 0) {
				result[i] = [valueObj[0], valueObj[1], valueObj[2], valueObj[19], valueObj[14], valueObj[15], valueObj[18], valueObj[17], multiUrl[i], multiClick[i], multiReportId[i], valueObj[13]];
			} else {
				result[i] = [multiUrl[i], multiClick[i], multiReportId[i], valueObj[13]];
			}
		}
		
		return result;
	}

	$('#downloadReport').load(function () {
        //if the download link return a page
        //load event will be triggered
		$('.LyMain').unblock();
    });
	
	$('.exportToExcel').click(function(){
		if(!validateTimeRange()){
			return false;
		}
		
		var startDate = $('#pushReportStartDate').val();
		var endDate = $('#pushReportEndDate').val();
		
		var url =  bcs.bcsContextPath + '/edit/exportToExcelForPushReport?startDate=' + startDate + '&endDate=' + endDate;
		
		var downloadReport = $('#downloadReport');
		downloadReport.attr("src", url);
	});
	
	var loadDataFunc = function() {
		var startDate = $('#pushReportStartDate').val();
		var endDate = $('#pushReportEndDate').val();
		
		var n = parseInt((new Date(endDate) - new Date(startDate)) / 86400000);
		$('.MdTxtNotice01').html("顯示以下來源的" + (n+1) + "天資料 " + startDate + "~" + endDate);
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getPushReportList?startDate=' + startDate + '&endDate=' + endDate
		}).success(function(response){
			$('.dataTemplate').remove();
			$('#noDataTxt').remove();
			if (response.length == 0) {
				$('#reportList').append('<tr id="noDataTxt"><td colspan="13"><span style="color:red">查無資料</span></td></tr>');
				return false;
			}
			
			for(i in response){
				var data = dataTemplate.clone(true);
				var multiDataTemplate = data.find('.multiData').clone(true);
				data.find('.multiData').remove();
				
				var valueObj = response[i];
				console.info('valueObj', valueObj);
				
				if (valueObj[4] != null && valueObj[4].search(";") > -1) { //多筆
					var multiData = parseReportListData(valueObj);
					
					var count = valueObj[3].split(";").length;
					data.find('.pushTime').closest('td').attr('rowspan', count);
					data.find('.pushType').closest('td').attr('rowspan', count);
					data.find('.pushImage').closest('td').attr('rowspan', count);
					data.find('.pushNumber').closest('td').attr('rowspan', count);
					data.find('.crt').closest('td').attr('rowspan', count);
					data.find('.ttlClicks').closest('td').attr('rowspan', count);
					data.find('.ttlClicksTh').closest('td').attr('rowspan', count);
					data.find('.ttlVisits').closest('td').attr('rowspan', count);
					data.find('.ttlProducts').closest('td').attr('rowspan', count);
					
					$.each(data.find('.pushReportData'), function(i, v) {
						var name = $(this).attr('name');
						if(name == "clickCount"){

							$(this).find('a').attr('href', bcs.bcsContextPath +'/edit/exportToExcelForClickUrl?reportId=' + multiData[0][10]);
							$(this).find('a').html(multiData[0][i]);
						}
						else{
							$(this).html(multiData[0][i]);
						}
					});

					data.find('.reportMsgSendId').val(multiData[0][11]);
					data.find('.reportId').val(multiData[0][10]);
					data.find('.richId').val(valueObj[12]);
					for (var j=1; j<count; j++) {
						var multiDataTr = multiDataTemplate.clone(true);
						
						$.each(multiDataTr.find('.pushReportData'), function(i, v) {
							var name = $(this).attr('name');
							if(name == "clickCount"){

								$(this).find('a').attr('href', bcs.bcsContextPath +'/edit/exportToExcelForClickUrl?reportId=' + multiData[j][2]);
								$(this).find('a').html(multiData[j][i]);
							}
							else{
								$(this).html(multiData[j][i]);
							}
						});

						multiDataTr.find('.reportId').val(multiData[j][2]);
						multiDataTr.find('.reportMsgSendId').val(multiData[j][3]);
						data.append(multiDataTr);
					}
				} else {
					
					var multiData = [valueObj[0], valueObj[1], valueObj[2], valueObj[19], valueObj[14], valueObj[15], valueObj[18], valueObj[17], valueObj[4], valueObj[6], valueObj[10], valueObj[13]];
					
					$.each(data.find('.pushReportData'), function(i, v) {
						var name = $(this).attr('name');
						if(name == "clickCount"){

							$(this).find('a').attr('href', bcs.bcsContextPath +'/edit/exportToExcelForClickUrl?reportId=' + valueObj[10]);
							$(this).find('a').html(multiData[i]);
						}
						else{
							$(this).html(multiData[i]);
						}
					});
					data.find('.reportMsgSendId').val(valueObj[13]);
					data.find('.reportId').val(valueObj[10]);
					data.find('.richId').val(valueObj[12]);
					if (valueObj[12] == null || valueObj[12] == "" ) {
						data.find('.pushTxt').html(valueObj[3]);
					}
				}
				
				if (valueObj[11] != null && valueObj[11] != "") {
					data.find('.pushImage').attr('src', bcs.bcsContextPath + "/getResource/IMAGE/" + valueObj[11]);
				} else {
					data.find('.pushImage').remove();
				}
				
				$('#reportList').append(data);
			}
			
			setBtnEvent();
		}).fail(function(response){
			console.info(response);
		}).done(function(){
		});
		
		// 取得本週平均點擊率
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getClickRateForWeek?startDate=' + startDate + '&endDate=' + endDate
		}).success(function(response){
			console.log(response);
			var ctrForThisWeek = Math.round(parseFloat(response[0]) * 100) / 100;
			$('#ctrForThisWeek').html(ctrForThisWeek + "%");
			
			var ctrChangePercentage = Math.round((ctrForThisWeek - parseFloat(response[1])) * 100) / 100;
			$('#ctrChangePercentage').html(ctrChangePercentage + "%(自上周)");
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		// 取得本週與上週點擊率比較
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/getClickRateForDay?startDate=' + startDate + '&endDate=' + endDate
		}).success(function(response){
			console.log(response);
			$('#compareClickRate').remove();
			$('#clickSituation').append('<div id="compareClickRate"></div>');
			var compareClickRate = document.getElementById('compareClickRate');
			showGraph(compareClickRate, response, true);
		}).fail(function(response){
			console.info(response);
		}).done(function(){
		});
		
		// 已串聯有效好友數
		var postDataBinded = {};
		postDataBinded.groupId = -2;
		console.info('postDataBinded', postDataBinded);
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/market/getSendGroupConditionResult?startDate=' + startDate + '&endDate=' + endDate,
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postDataBinded)
		}).success(function(response){
			console.info(response);
			record.bindedCount = response;
			showData();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		// 未串聯有效好友數
		var postDataUnbind = {};
		postDataUnbind.groupId = -3;
		console.info('postDataUnbind', postDataUnbind);
		$.ajax({
			type : "POST",
			url : bcs.bcsContextPath + '/market/getSendGroupConditionResult?startDate=' + startDate + '&endDate=' + endDate,
            cache: false,
            contentType: 'application/json',
            processData: false,
			data : JSON.stringify(postDataUnbind)
		}).success(function(response){
			console.info(response);
			record.unbindCount = response;
			showData();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		// 串聯後封鎖好友數
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/edit/countBinded2Block?startDate=' + startDate + '&endDate=' + endDate
		}).success(function(response){
			console.info(response);
			record.binded2Block = response;
			showData();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		loadReceiveOpFunc();
	}
	
	var record = {};
	
	var showData = function(){
		var bindedCount = record.bindedCount;
		var unbindCount = record.unbindCount;
		var binded2Block = record.binded2Block;
		var totalCount = bindedCount + unbindCount;
		
		if(bindedCount || bindedCount == 0){
			$('.bindedCount').html($.BCS.formatNumber(bindedCount, 0));
		}
		if(unbindCount || unbindCount == 0){
			$('.unbindCount').html($.BCS.formatNumber(unbindCount, 0));
		}
		if(binded2Block || binded2Block == 0){
			$('.binded2BlockCount').html($.BCS.formatNumber(binded2Block, 0));
		}

		$('.totalCount').html($.BCS.formatNumber(totalCount, 0));
	}
	
	var loadReceiveOpFunc = function(){
		RecordOp = {};
		$("#catchRecordOpReceiveList").html('');

		var startDate = $('#pushReportStartDate').val();
		var endDate = $('#pushReportEndDate').val();

		// 本週好友新增與封鎖人數
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/countReceiveOpList?startDate=' + startDate + '&endDate=' + endDate + '&opType=follow'
		}).success(function(response){
			console.info(response);
			
			RecordOp.add = response;
			catchRecordOpReceiveListFunc();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/countReceiveOpList?startDate=' + startDate + '&endDate=' + endDate + '&opType=unfollow'
		}).success(function(response){
			console.info(response);
			
			RecordOp.block = response;
			catchRecordOpReceiveListFunc();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	}

	var RecordOp = {};
	var catchRecordOpReceiveListFunc = function(){
		if(RecordOp.add && RecordOp.block){
			console.info(RecordOp);
			var container = document.getElementById('catchRecordOpReceiveList');
			showGraphRecordOp(container, RecordOp);
		}
	}
	
	var showGraphRecordOp = function(container, response){

		var startDate = $('#pushReportStartDate').val();
		var endDate = $('#pushReportEndDate').val();
		
	    var groups = new vis.DataSet();

		        groups.add({
				        id: "add",
				        content: "新增",
				        options: {
				            drawPoints: {
				                style: 'square' // square, circle
				            },
				            shaded: {
				                orientation: 'bottom' // top, bottom
				            }
				        }});
		        
		        groups.add({
			        id: "block",
			        content: "封鎖",
			        options: {
			            drawPoints: {
			                style: 'square' // square, circle
			            },
			            shaded: {
			                orientation: 'bottom' // top, bottom
			            }
			        }});
		
		var values = {};
		var items = [];
		
		$.each(response, function(i, obj){
			
			$.each(obj, function(x, y){

				var data = { x : x, y : y, group:i};
				items.push(data);
			});
		});
		
		var start = moment(startDate).add(-1, 'day');
		var end = moment(endDate).add(1, 'day');

		var dataset = new vis.DataSet(items);
		var options = {
				legend: {left:{position:"top-right"}},
			    start: start,
			    end: end, 
			    graphHeight: '230px',
			    timeAxis: {scale: 'day', step: 1},
			    moveable: false, //圖表不可移動
			    showCurrentTime:false
		};
		var graph2d = new vis.Graph2d(container, dataset, groups, options);
	}
	
	initTemplate();
	loadDataFunc();
});