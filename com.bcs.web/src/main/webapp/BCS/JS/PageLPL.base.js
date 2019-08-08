$(function() {
	console.info("222");
	var templateCount = 0;
    var originalTr = {};
    var originalTable = {};
    var searchText = "";
	// Initialize Page
	var initPage = function(){
		// clone & remove
	    originalTr = $('.templateTr').clone(true);
	    $('.templateTr').remove();
		originalTable = $('.templateTable').clone(true);
		$('.templateTable').remove();
	};
	
    var loadDataFunc = function() {
    	//searchText = $.urlParam('searchText');
    	if($.urlParam('searchText')){
    		console.info("searchText", $.urlParam('searchText'));
    		searchText = $.urlParam('searchText');
    	}
		templateCount = 0;
		// block
		$('.LyMain').block($.BCS.blockMsgRead);
		
		// get all list data
		getListData('全部', '/market/getAllLinePointMainList');
    };
	
    // get list data
	var getListData = function(name, url){
		if(searchText != null && searchText != ""){
			url += 'Search/' + searchText;
			console.info("url:", url);
		}
		
        $.ajax({
            type: "GET",
            url: bcs.bcsContextPath + url
        }).success(function(response) {
			templateCount++;
			addTab(name);
			var templateTable = originalTable.clone(true);
			
            console.info("response:", response);
            $.each(response, function(i, o) {
                var templateTr = originalTr.clone(true); //增加一行
                console.info("templateTr:", templateTr);
                // templateTr.find('.campaignCode a').attr('href', bcs.bcsContextPath + '/getAllLinePointMainList?campaignCode=' + o.campaignCode);
                templateTr.find('.campaignCode').html(o.serialId);
                templateTr.find('.campaignName').html(o.title);
                if (o.modifyTime) {
                    templateTr.find('.modifyTime').html(moment(o.modifyTime).format('YYYY-MM-DD HH:mm:ss'));
                } else {
                    templateTr.find('.modifyTime').html('-');
                }

                templateTr.find('.campaignName').html(o.title);
                templateTr.find('.sendPoint').html(o.amount);
                templateTr.find('.campaignPersonNum').html(o.totalCount);
                templateTr.find('.status').html(o.status);
                templateTr.find('.sendType').html(o.sendType);
                templateTr.find('.setUpUser').html(o.modifyUser);

                if (bcs.user.admin) {
                    templateTr.find('.btn_detele').attr('id', o.id);
                    templateTr.find('.btn_detele').click(btn_deteleFunc);
                } else {
                    templateTr.find('.btn_detele').remove();
                }
                
                // Append to Table
                templateTable.append(templateTr);
            });
            
            // set attribute
            templateTable.attr('name', 'templateTable' + templateCount);
            
			// append to Tab
			$('#tab'+templateCount).append(templateTable);
			$("#tabs").tabs({active: 0});

        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
        }).done(function() {
			switch(name){
			case '全部':
				getListData('人工發送', '/market/getManualLinePointMainList');
				break;
			case '人工發送':	
				getListData('自動發送', '/market/getAutoLinePointMainList');
				break;
			case '自動發送':
				$('.LyMain').unblock();
				break;
			}
        });		
	};
	
	// add tab
	var addTab = function(name){
        var target;
        
        $("#tabs ul").append(
    		"<li class='tabLi'><a href='#tab" + templateCount + "'>" + name + "</a></li>"
    	);
        
        $("#tabs").append(
            "<div class='tabDiv' id='tab" + templateCount + "'></div>"
        );
        
        $("#tabs").tabs("refresh");
        $("#tabs").tabs({ active: templateCount-1 });
    };
    
    // other Functions
    // to Create Page
    $('.btn_add').click(function() {
        window.location.replace(bcs.bcsContextPath + '/market/linePointCreatePage');
    });

    $('.btn_save').click(function() {
    	var searchText = $('#searchText').val();
        window.location.replace(bcs.bcsContextPath + '/market/linePointListPage?searchText=' + searchText);
    });
    
    // do Delete
    var btn_deteleFunc = function() {
        var campaignId = $(this).attr('id');

        console.info('btn_deteleFunc campaignId:' + campaignId);

        var r = confirm("請確認是否刪除");
        if (r) {

        } else {
            return;
        }

        $.ajax({
            type: "DELETE",
            url: bcs.bcsContextPath + '/admin/deleteLinePointMain?campaignId=' + campaignId + '&listType=LineCampaignList'
        }).success(function(response) {
            console.info(response);
            alert("刪除成功");
            window.location.replace(bcs.bcsContextPath + '/market/linePointListPage');
        }).fail(function(response) {
            console.info(response);
            $.FailResponse(response);
        }).done(function() {});
    };
    
	// main()
	// initialize Page & load Data
    initPage();
    $("#tabs").tabs();
    loadDataFunc();

});