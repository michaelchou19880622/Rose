/**
 * 
 */
$(function(){
	
	$('.btn_cancel').click(function(){
		
 		window.location.replace(bcs.bcsContextPath + '/index');
	});
	
	var parseTime = function(timeStr){
		
		var calendar = $.urlParam("calendar");
		if(!calendar){
			calendar = 12;
		}
		
		if(calendar == 12){
			return timeStr + ":00";
		}
		else if(calendar = 11){
			return timeStr + ":00:00";
		}
		else if(calendar = 5){
			return timeStr + " 00:00:00";
		}
	}
	
	var showGraph = function(container, response, multi){

		var width = $.urlParam("width");
		if(!width){
			width = 10;
		}
		
		var values = {};
		var items = [];
		if(multi){

			$.each(response, function(i, obj){
				
				$.each(obj, function(i, o){
					
					var value = values[parseTime(i)];
					
					if(!value){
						values[parseTime(i)] = o;
					}
					else{
						values[parseTime(i)] = o + value;
					}
				});
			});

			$.each(values, function(x, y){

				var data = { x : x, y : y};
				items.push(data);
			});
		}
		else{
			$.each(response, function(i, o){
				var data = { x :parseTime(i), y : o};
				items.push(data);
			});
		}
		
		var start = moment().add(-width, 'hour');
		var end = moment().add(1, 'hour');

		var dataset = new vis.DataSet(items);
		var options = {
			    start: start,
			    end: end,
			    showCurrentTime:false
		};
		var graph2d = new vis.Graph2d(container, dataset, options);
	}

	var loadDataFunc = function(){

		var showSystem = $.urlParam("showSystem");
		var zoom = $.urlParam("zoom");
		if(!zoom){
			zoom = "";
		}
		
		var calendar = $.urlParam("calendar");
		if(!calendar){
			calendar = 12;
		}
		
		if(showSystem){
			$.ajax({
				type : "GET",
				url : bcs.bcsContextPath + '/admin/systemCheck'
			}).success(function(response){
				console.info(response);
				$.each(response, function(i, o){
					$.each(o, function(index, data){
						var systemData = templateBody.clone(true);
						
						systemData.find('.dataTitle').html(index);
						systemData.find('.dataContent').html(data);
						
						$('#tableBody').append(systemData);
					});
				});
			}).fail(function(response){
				console.info(response);
				$.FailResponse(response);
			}).done(function(){
			});
		}
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/catchRecordReceiveList?zoom=' + zoom + "&calendar=" + calendar
		}).success(function(response){
			console.info(response);
			var container = document.getElementById('catchRecordReceiveList');

			showGraph(container, response);
			allData.push(response);
			allReceiveListFunc();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/catchRecordBindedList?zoom=' + zoom + "&calendar=" + calendar
		}).success(function(response){
			console.info(response);
			var container = document.getElementById('catchRecordBindedList');

			showGraph(container, response);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/catchHandleMsgReceiveTimeoutList?zoom=' + zoom + "&calendar=" + calendar
		}).success(function(response){
			console.info(response);
			var container = document.getElementById('catchHandleMsgReceiveTimeoutList');

			showGraph(container, response);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/catchRecordOpAddReceiveList?zoom=' + zoom + "&calendar=" + calendar
		}).success(function(response){
			console.info(response);

			var container = document.getElementById('catchRecordOpAddReceiveList');

			showGraph(container, response);
			allData.push(response);
			allReceiveListFunc();
			
			RecordOp.add = response;
			catchRecordOpReceiveListFunc();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/catchRecordOpBlockedReceiveList?zoom=' + zoom + "&calendar=" + calendar
		}).success(function(response){
			console.info(response);

			var container = document.getElementById('catchRecordOpBlockedReceiveList');

			showGraph(container, response);
			allData.push(response);
			allReceiveListFunc();
			
			RecordOp.block = response;
			catchRecordOpReceiveListFunc();
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/checkSendingMsgHandlerTaskCount'
		}).success(function(response){
			console.info(response);
			
			$('#checkSendingMsgHandlerTaskCount').html(response);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/checkReceivingMsgHandlerTaskCount'
		}).success(function(response){
			console.info(response);
			
			$('#checkReceivingMsgHandlerTaskCount').html(response);
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
		
		$.ajax({
			type : "GET",
			url : bcs.bcsContextPath + '/admin/checkAccessTokenTime'
		}).success(function(response){
			console.info(response);
			
			$('#checkAccessTokenTime').html(JSON.stringify(response));
		}).fail(function(response){
			console.info(response);
			$.FailResponse(response);
		}).done(function(){
		});
	};
	
	var allData = [];
	var allReceiveListFunc = function(){
		if(allData.length == 3){
			var container = document.getElementById('allReceiveList');
			showGraph(container, allData, true);
		}
	};

	var RecordOp = {};
	var catchRecordOpReceiveListFunc = function(){
		if(RecordOp.add && RecordOp.block){
			console.info(RecordOp);
			var container = document.getElementById('catchRecordOpReceiveList');
			showGraphRecordOp(container, RecordOp);
		}
	}
	
	var showGraphRecordOp = function(container, response){

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
        
		var width = 7;
		
		var values = {};
		var items = [];
		
		$.each(response, function(i, obj){
			
			$.each(obj, function(x, y){

				var data = { x : x, y : y, group:i};
				items.push(data);
			});
		});
		
		console.info(items);
		
		var start = moment().add(-width, 'day');
		var end = moment().add(1, 'day');

		var dataset = new vis.DataSet(items);
		var options = {
				legend: {left:{position:"bottom-right"}},
			    start: start,
			    end: end, 
			    graphHeight: '200px',
			    width: '500px',
			    showCurrentTime:false
		};
		var graph2d = new vis.Graph2d(container, dataset, groups, options);
	}
	
	var templateBody = {};
	
	var initTemplate = function(){

		templateBody = $('.dataTemplate').clone(true);
		$('.dataTemplate').remove();
	}
	
	initTemplate();
	loadDataFunc();
});