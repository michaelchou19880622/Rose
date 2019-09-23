/**
 *
 */

$(function () {
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

    /* Button Size */
    var btnSize = 0;
    /* Button now index */
    var btnIndex = 0;

    // ---- Import Data ----
    // initialize Page
    var initPage = function () {
        // clone & remove
        originalPopTr = $('.popTr').clone(true);
        $('.popTr').remove();

        // add options
        appendOption('pathwayList', 0, 'BC->PNP->SMS');
        appendOption('pathwayList', 1, 'BC->SMS');
        appendOption('pathwayList', 2, 'BC');
        // appendOption('templateList', 0, 'TestTemplate');


        // parameter
        // 從列表頁導過來的參數
        pnpMaintainAccountModelId = $.urlParam("pnpMaintainAccountModelId");

        // Edit Mode
        if (pnpMaintainAccountModelId != null) {
            // block
            $('.LyMain').block($.BCS.blockMsgRead);

            // change UI
            pnpMaintainAccountActionType = 'Edit';
            $('.CHTtl').html('編輯一般帳號');
            $('.btn_add.add').val('Edit');

            // get Data
            $.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + "/pnpAdmin/getPNPMaintainAccount?id=" + pnpMaintainAccountModelId,
            }).success(function (response) {
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

                if (response.accountClass == 'O') {
                    $('.accountClass')[0].checked = true;
                } else {
                    $('.accountClass')[1].checked = true;
                }

                if (response.status = true) {
                    $('.status')[0].checked = true;
                } else {
                    $('.status')[1].checked = true;
                }

                $('#flexContent').val(response.pnpContent);

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
                console.log('popTr', popTr);
                popTr.find('.pathway').html(pathway);
                popTr.find('.template').html(template);
                popTr.find('.PNPContent').html(PNPContent);

                $('.popTbody').append(popTr);

            }).fail(function (response) {
                console.info(response);
                $.FailResponse(response);
            }).done(function () {
                $('.LyMain').unblock();
            });
        } else {
            // Create Mode
            pnpMaintainAccountActionType = 'Create';
        }
    };

    // add option
    var appendOption = function (listName, value, text) {
        var opt = document.createElement('option');
        var list = document.getElementById(listName);
        opt.value = value;
        opt.innerHTML = text;
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

            $('#account').val(response.account);
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
            // window.location.replace('pnpNormalAccountCreatePage');
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
    $('.btn_add.add').click(function () {
        // block
        $('.LyMain').block($.BCS.blockMsgRead);
        $('.LyMain').unblock();
        $('#dialog-modal').dialog({
            width: 1024,
            height: 768,
            modal: true,
            position: { my: 'center', at: 'center', of: window }
        });
        $('#dialog-modal').show();

        /* Init Create First Button Row */
        addBtn();
    });

    /* SAVE BUTTON */
    // do Confirm
    $('.btn_add.confirm').click(function () {
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
        postData.accountType = 'Normal';
        /* FIXME-----Flex設定相關欄位----- */
        postData.flexTitle = '';
        postData.flexButtonName = '';
        postData.flexButtonUrl = '';
        /* -----Flex設定相關欄位----- */

        if ($('.accountClass')[0].checked) {
            postData.accountClass = 'O';
        } else {
            postData.accountClass = 'M';
        }

        if ($('.status')[0].checked) {
            postData.status = true;
        } else {
            postData.status = false;
        }

        if (pathway == 'BC-&gt;PNP-&gt;SMS') {
            postData.pathway = '3';
        } else if (pathway == 'BC-&gt;SMS') {
            postData.pathway = '2';
        } else if (pathway == 'BC') {
            postData.pathway = '1';
        }
        postData.template = template;
        postData.pnpContent = PNPContent;

        postData.id = pnpMaintainAccountModelId;


        $.ajax({
            type: 'POST',
            url: bcs.bcsContextPath + '/pnpAdmin/createPNPMaintainAccount',
            cache: false,
            contentType: 'application/json',
            processData: false,
            data: JSON.stringify(postData)
        }).success(function (response) {
            console.info(response);
            alert('儲存成功');
            window.location.replace('pnpNormalAccountListPage');
        }).fail(function (response) {
            console.info(response);
            var text = response.responseText;
            console.info("text:", text);
            if (text == '帳號、前方來源系統、簡訊內容不可與之前資料重複！') {
                alert('帳號、前方來源系統、簡訊內容不可與之前資料重複！');
            } else {
                $.FailResponse(response);
            }
        })
    });

    /* 彈出視窗按鈕 */
    // do Pop Confirm
    $('#popConfirm').click(function () {
        var list = document.getElementById('pathwayList');
        /* 已選取的Pathway */
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
    $('input[name="cancel"]').click(function () {
        var r = confirm("請確認是否取消");
        if (r) {
            // confirm true
        } else {
            return;
        }
        window.location.replace(bcs.bcsContextPath + '/pnpAdmin/pnpNormalAccountListPage');
    });

    /* 監聽標題內容即時反映於預覽區 */
    $('#flexTitle').keyup(function(){
        var value = document.getElementById('flexTitle').value;
        if(value === undefined || value === null || value === ''){
            value = '訊息標題';
        }
        document.getElementById('previewTitle').textContent = value;
    });

    /* 監聽訊息內容即時反映於預覽區 */
    $('#flexContent').keyup(function(){
        var value = document.getElementById('flexContent').value;
        if(value === undefined || value === null || value === ''){
            value = 'This is PNP Message Content!!';
        }
        document.getElementById('previewContent').textContent = value;
    });

        /* 監聽訊息內容即時反映於預覽區 */
    $('#flexContent').keyup(function(){
        var value = document.getElementById('flexContent').value;
        if(value === undefined || value === null || value === ''){
            value = 'This is PNP Message Content!!';
        }
        document.getElementById('previewContent').textContent = value;
    });


    $('#addButtonBtn').click(function(){
        addBtn();
    });

    /**
     * Create New Button Row
     */
    function addBtn(){
        console.log('All Btn size: ' + btnSize);
        if(btnSize > 3){
            alert('已達最大按鈕數量!!');
            return;
        }
        btnIndex++;
        /* Button Name */
        var btnNameInput = document.createElement('input');
        btnNameInput.type = 'text';
        btnNameInput.id = 'btnName-' + btnIndex;
        btnNameInput.className = 'input_text';

        btnNameInput.onkeyup = function(){
            var i =  this.id.split('-')[1];
            var value = document.getElementById('btnName-' + i).value;
            if(value === undefined || value === null || value === ''){
                value = '按鈕';
            }
            document.getElementById('previewBtn' + i).textContent = value;
        }

        var btnNameDiv2 = document.createElement('div');
        btnNameDiv2.className = 'param';

        btnNameDiv2.appendChild(btnNameInput);

        var btnNameDiv1 = document.createElement('div');
        btnNameDiv1.className = 'warp40';

        btnNameDiv1.appendChild(btnNameDiv2);

        /* Button Url */
        var btnUrlInput = document.createElement('input');
        btnUrlInput.type = 'text';
        btnUrlInput.id = 'btn-url' + btnIndex;
        btnUrlInput.className = 'input_text2';

        var btnUrlDiv2 = document.createElement('div');
        btnUrlDiv2.className = 'param';

        btnUrlDiv2.appendChild(btnUrlInput);

        var btnUrlDiv1 = document.createElement('div');
        btnUrlDiv1.className = 'warp50';
        btnUrlDiv1.appendChild(btnUrlDiv2);


        /* Remove Button */
        var removeButton = document.createElement('button');
        removeButton.className = 'btn btn-warn';
        removeButton.id = 'removeButtonBtn' + btnIndex;
        removeButton.textContent = '－';
        removeButton.setAttribute("style", "margin-left:20px");
        removeButton.value = btnIndex;
        removeButton.onclick = function(){
            var i = removeButton.value;
            document.getElementById('btnGroup').removeChild(document.getElementById('row' + i));
            document.getElementById('previewBtnGroup').removeChild(document.getElementById('previewButtonRow' + i));
            console.log('Remove Success!! index:' + btnIndex);
            btnSize--;
        }

        var removeButtonBtnDiv = document.createElement('div');
        removeButtonBtnDiv.className = 'warp8';

        removeButtonBtnDiv.appendChild(removeButton);

        /* Row */
        var rowDiv = document.createElement('div');
        rowDiv.id = 'row' + btnIndex;
        rowDiv.appendChild(btnNameDiv1);
        rowDiv.appendChild(btnUrlDiv1);
        rowDiv.appendChild(removeButtonBtnDiv);

        console.log(rowDiv);

        var btnGroup = document.getElementById('btnGroup');
        btnGroup.appendChild(rowDiv);

        //Preview Area
        var previewButton = document.createElement('button');
        previewButton.textContent = 'Button';
        previewButton.className = 'msgBtn';
        previewButton.id = 'previewBtn' + btnIndex;

        var previewButtonDiv = document.createElement('div');
        previewButtonDiv.id = 'previewButtonRow' + btnIndex;
        previewButtonDiv.className = 'msgBtn-area';

        previewButtonDiv.appendChild(previewButton);

        var previewBtnGroup = document.getElementById('previewBtnGroup');
        previewBtnGroup.appendChild(previewButtonDiv);
        btnSize++;
    }


    // ---- Initialize Page & Load Data ----
    initPage();
});
