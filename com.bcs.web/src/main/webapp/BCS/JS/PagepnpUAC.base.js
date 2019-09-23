/**
 *
 */

$(function(){
	// ---- Global Variables ----
	// parameters
	var pnpMaintainAccountModelId = null;
	var pnpMaintainAccountActionType = null;

	// original Template
	var originalPopTr = {};

	// pop data
	var pathway = "";
	var template = "";
	var PNPContent = "";

	var pathWayIndex = "";
	// ---- Import Data ----
	// initialize Page
	var initPage = function(){
		// clone & remove
		originalPopTr = $('.popTr').clone(true);
		$('.popTr').remove();

		// add options
		appendOption('pathwayList', 0, 'BC->PNP->SMS');
		appendOption('pathwayList', 1, 'BC->SMS');
		appendOption('pathwayList', 2, 'BC');
		appendOption('templateList', 0, 'TestTemplate');


		// parameter
		pnpMaintainAccountModelId = $.urlParam("pnpMaintainAccountModelId"); //從列表頁導過來的參數

		// Edit Mode
		if(pnpMaintainAccountModelId != null){
			// block
			$('.LyMain').block($.BCS.blockMsgRead);

			// change UI
			pnpMaintainAccountActionType = 'Edit';
			$('.CHTtl').html('編輯Unica帳號');
			$('.btn_add.add').val('Edit');

			// get Data
			$.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + "/pnpAdmin/getPNPMaintainAccount?id=" + pnpMaintainAccountModelId,
    		}).success(function(response){
    			console.info("response:", response);
    			$('#account').val(response.account);
    			$('#accountAttribute').val(response.accountAttribute);
    			$('#sourceSystem').val(response.sourceSystem);
    			$('#employeeId').val(response.employeeId);
    			$('#departmentId').val(response.departmentId);
    			$('#divisionName').val(response.divisionName);
    			$('#departmentName').val(response.departmentName);
    			$('#groupName').val(response.groupName);
    			$('#PccCode').val(response.pccCode);

    			if(response.accountClass == 'O'){
					$('.accountClass')[0].checked = true;
				}else{
					$('.accountClass')[1].checked = true;
				}

    			if(response.status){
					$('.status')[0].checked = true;
				}else{
					$('.status')[1].checked = true;
				}

    			$('#PNPContent').val(response.pnpContent);

    			// Pop data
    			template = response.template;
    			PNPContent = response.pnpContent;
                if (response.pathway == '3') {
                    pathway = 'BC-&gt;PNP-&gt;SMS';
                    $('#pathwayList').get(0).selectedIndex=1;
                } else if (response.pathway == '2') {
                    pathway = 'BC-&gt;SMS';
                    $('#pathwayList').get(0).selectedIndex=2;
                } else if (pathway == '1') {
                    pathway = 'BC';
                    $('#pathwayList').get(0).selectedIndex=3;
                }

                $('#templateList').get(0).selectedIndex=1;


    			$('.popTr').remove();
    			var popTr = originalPopTr.clone(true);
    			popTr.find('.pathway').html(pathway);
    			popTr.find('.template').html(template);
    			popTr.find('.PNPContent').html(PNPContent);

    			$('.popTbody').append(popTr);

    		}).fail(function(response){
    			console.info(response);
    			$.FailResponse(response);
    		}).done(function(){
    			$('.LyMain').unblock();
    		});
		}else{
			// Create Mode
			pnpMaintainAccountActionType = 'Create';
		}
	};

	// add option
	var appendOption = function(listName, value, text){
		var opt = document.createElement('option');
		opt.value = value;
        opt.innerHTML = text;
		var list = document.getElementById(listName);
		list.appendChild(opt);
	};


	// ---- Functions ----
	/* 員工代碼查詢按鈕 */
        $('#searchBtn').click(function () {
            console.log('Search Button Click!!');
            var id = $('#employeeId').val();
            console.info("id:", id);

            $.ajax({
                type: 'GET',
                url: bcs.bcsContextPath + '/pnpAdmin/getEmpAccountInfo?id=' + id,
                contentType: 'application/json',
            }).success(function (response) {
                console.info("response:", response);

//                $('#account').val(response.account);
                $('#employeeId').val(response.employeeId);
                $('#departmentId').val(response.departmentId);
                $('#divisionName').val(response.divisionName);
                $('#departmentName').val(response.departmentName);
                $('#groupName').val(response.groupName);
                $('#PccCode').val(response.pccCode);
                $('#accountAttribute').val('批次');
            }).fail(function (response) {
                console.info(response);
                $.FailResponse(response);
//                window.location.replace('pnpNormalAccountCreatePage');
            }).done(function () {
                // $('.LyMain').unblock();
                // $('#dialog-modal').dialog({
                //     width: 1024,
                //     height: 768,
                //     modal: true
                // });
                // $('#dialog-modal').show();
            });
        });


	// do Add
	$('.btn_add.add').click(function(){
        // block
        $('.LyMain').block($.BCS.blockMsgRead);
        $('.LyMain').unblock();
        $('#dialog-modal').dialog({
            width: 960,
            height: 480,
            modal: true
        });
        $('#dialog-modal').show();
	});

	// do Confirm
	$('.btn_add.confirm').click(function(){
		postData = {};

		postData.account = $('#account').val();
		postData.accountAttribute = $('#accountAttribute').val();
		postData.sourceSystem = $('#sourceSystem').val();
		postData.employeeId = $('#employeeId').val();
		postData.departmentId = $('#departmentId').val();
		postData.divisionName = $('#divisionName').val();
		postData.departmentName = $('#departmentName').val();
		postData.groupName = $('#groupName').val();
		postData.pccCode = $('#PccCode').val();
		postData.accountType = 'Unica';

		if($('.accountClass')[0].checked){
			postData.accountClass = 'O';
		}else{
			postData.accountClass = 'M';
		}

		if($('.status')[0].checked){
			postData.status = true;
		}else{
			postData.status = false;
		}

		if(pathway == 'BC-&gt;PNP-&gt;SMS'){
			postData.pathway = '3';
		}else if(pathway == 'BC-&gt;SMS'){
			postData.pathway = '2';
		}else if(pathway == 'BC'){
			postData.pathway = '1';
		}
		postData.template = template;
		postData.pnpContent = PNPContent;

		postData.id = pnpMaintainAccountModelId;


		$.ajax({
			type : 'POST',
			url : bcs.bcsContextPath + '/pnpAdmin/createPNPMaintainAccount',
			cache : false,
			contentType : 'application/json',
			processData : false,
			data : JSON.stringify(postData)
		}).success(function(response) {
			console.info(response);
			alert('儲存成功');
			window.location.replace('pnpUnicaAccountListPage');
		}).fail(function(response) {
			console.info(response);
			var text = response.responseText;
			console.info("text:", text);
			if(text == '帳號、前方來源系統、簡訊內容不可與之前資料重複！'){
				alert('帳號、前方來源系統、簡訊內容不可與之前資料重複！');
			}else{
				$.FailResponse(response);
			}
		})
	});

	// do Pop Confirm
	$('#popConfirm').click(function(){
		var list = document.getElementById('pathwayList');
		pathway = list.options[list.selectedIndex].innerHTML;
		template = 'TestTemplate';
		PNPContent = $('#PNPContent')[0].value;

		$('.popTr').remove();
		var popTr = originalPopTr.clone(true);
		popTr.find('.pathway').html(pathway);
		popTr.find('.template').html(template);
		popTr.find('.PNPContent').html(PNPContent);

		$('.popTbody').append(popTr);

    	$('#dialog-modal').dialog("close");
    });

	// do Cancel
	$('input[name="cancel"]').click(function() {
		var r = confirm("請確認是否取消");
		if (r) {
			// confirm true
		} else {
		    return;
		}
		window.location.replace(bcs.bcsContextPath + '/pnpAdmin/pnpUnicaAccountListPage');
	});
	initPage();
});