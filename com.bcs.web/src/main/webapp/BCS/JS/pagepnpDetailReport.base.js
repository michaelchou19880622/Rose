/**
 *
 */

$(function () {
    // ---- Global Variables ----

    var originalTr;
    var originalTr2;
    var originalTrOdd;
    var originalTr2Odd;
    var originalTable;

    // result data
    var hasData = false;
    var firstFetch = true;
    var startDate = null,
        endDate = null;
    var page = 1,
        totalPages = 0;


    var pageBtnHandler = function(condition, actionName){
        if(condition){
            page = actionName === 'next' ? ++page : --page;
            console.log('Currency Page Number is ' + page);
            loadData();
            $('#pageAndTotalPages').text(page + '/' + totalPages);
        }
    }

    //-------------------Event----------------------
    $(".datepicker").datepicker({
        maxDate: 0,
        dateFormat: 'yy-mm-dd',
        changeMonth: true
    });

    $('#backBtn').click(function () {
        pageBtnHandler(page > 1, 'back');
    });


    $('#nextBtn').click(function () {
        pageBtnHandler(page < totalPages, 'next');
    });





    // do Search
    $('#searchBtn').click(function () {
        if (dataValidate()) {
            cleanList();
            startDate = $('#startDate').val();
            endDate = $('#endDate').val();
            loadData();
        }
    });

    $('#exportBtn').click(function(){
        setExportButtonSource();
    });

    //-------------------Event----------------------


    var dataValidate = function () {
        startDate = $('#startDate').val();
        endDate = $('#endDate').val();
        if (!startDate) {
            alert('請填寫起始日期！');
            return false;
        }
        if (!endDate) {
            alert('請填寫結束日期！');
            return false;
        }
        if (!moment(startDate).add(31, 'days').isAfter(moment(endDate))) {
            alert('起始日期與結束日期之間不可相隔超過一個月！');
            return false;
        }
        if (moment(startDate).isAfter(moment(endDate))) {
            alert('起始日期不可大於結束日期！');
            return false;
        }
        firstFetch = true;
        return true;
    }


    // do Download
    var setExportButtonSource = function () {
        if (hasData) {
            var getUrl = bcs.bcsContextPath + '/pnpEmployee/exportPNPDetailReportExcel?'
                + 'startDate=' + startDate
                + '&endDate=' + endDate
                + '&sourceSystem=' + document.getElementById('sourceSystemInput').value
                + '&pccCodeInput=' + document.getElementById('pccCodeInput').value
                + '&account=' + document.getElementById('accountInput').value
                + '&phoneNumber=' + document.getElementById('phoneNumber').value;
            console.info('getUrl', getUrl);

            $('.btn_add.exportToExcel').attr('href', getUrl);
        } else {
            $('.btn_add.exportToExcel').attr('href', '#');
        }
    }



    // ---- Initialize Page & Load Data ----
    // get List Data
    var loadData = function () {
        $('.LyMain').block($.BCS.blockMsgRead);
        cleanList();

        console.info("firstFetch:", firstFetch);
        if (firstFetch) {
            firstFetch = false;
            fetchListCountAndChange();
        }
        var getUrl = bcs.bcsContextPath + '/pnpEmployee/getPNPDetailReport?'
            + 'startDate=' + startDate
            + '&endDate=' + endDate
            + '&page=' + page
            + '&sourceSystem=' + document.getElementById('sourceSystemInput').value
            + '&pccCodeInput=' + document.getElementById('pccCodeInput').value
            + '&account=' + document.getElementById('accountInput').value
            + '&phoneNumber=' + document.getElementById('phoneNumber').value

        console.info('getUrl', getUrl);

        $.ajax({
            type: 'GET',
            url: getUrl,
            contentType: 'application/json',
        }).success(function (response) {
            console.info("response:", response);
            console.log("response:", JSON.stringify(response));
            var i = 1;
            for (var key in response) {
                console.log('i = ' + i);
                var list = i % 2 == 0 ? originalTrOdd.clone(true) : originalTr.clone(true);
                var valueObj = response[key];

                var splits = valueObj[0].split('_');
                list.find('.no').html(i);
                list.find('.sourceSystem').html(splits[1]);
                list.find('.account').html(splits[2]);
                list.find('.pathway').html(converterPathWayCodeToName(valueObj[1]));
                list.find('.proc_stage').html(valueObj[2]);
                list.find('.pnpContent').html(valueObj[3]);
                list.find('.customerCellPhoneNumber').html(valueObj[4]);
                list.find('.schedule_time').html(moment(valueObj[5], 'YYYY-MM-DD HH:mm:ss').format('YYYY-MM-DD HH:mm:ss'));
                list.find('.bcStatusCode').html(parseCodeToChinese(valueObj[9]));
                list.find('.pnpStatusCode').html(parseCodeToChinese(valueObj[10]));
                list.find('.smsStatusCode').html(parseCodeToChinese(valueObj[11]));
                list.find('.accountPccCode').html(valueObj[12]);

                $('#resultTable').append(list);

                var list2 = i % 2 == 0 ? originalTr2Odd.clone(true) : originalTr2.clone(true);
                list2.find('.bc_time').html(valueObj[7]);
                list2.find('.pnp_time').html(valueObj[8]);
                list2.find('.sms_time').html('');

                $('#resultTable').append(list2);
                i++;
            }

            hasData = i > 0;
            console.log('Has data : ' + hasData);

            setExportButtonSource();

        }).fail(function (response) {
            console.info(response);
            $.FailResponse(response);
            $('.LyMain').unblock();
        }).done(function () {
            $('.LyMain').unblock();
        });
    };

    var converterPathWayCodeToName = function(pathwayCode){
        if (pathwayCode === '3') {
            return 'BC<br/>PNP<br/>SMS';
        } else if (pathwayCode === '2') {
            return 'BC<br/>SMS';
        } else if (pathwayCode === '1') {
            return 'BC';
        }
    }



    var parseCodeToChinese = function(status){
        switch (status) {
            case "DRAFT":
                return "正在存進資料庫";
            case "WAIT":
                return "等待進入處理程序";
            case "SCHEDULED":
                return "等待預約發送";
            case "BC_PROCESS":
                return "進行BC發送處理中";
            case "BC_SENDING":
                return "BC發送中";
            case "BC_COMPLETE":
                return "BC處理程序完成";
            case "BC_FAIL":
                return "BC發送失敗";
            case "BC_FAIL_PNP_PROCESS":
                return "轉發PNP";
            case "BC_FAIL_SMS_PROCESS":
                return "轉發SMS";
            case "PNP_SENDING":
                return "PNP發送中";
            case "CHECK_DELIVERY":
                return "已發送，等待回應";
            case "PNP_COMPLETE":
                return "PNP處理程序完成";
            case "PNP_FAIL_SMS_PROCESS":
                return "轉發SMS";
            case "SMS_SENDING":
                return "SMS發送中";
            case "SMS_CHECK_DELIVERY":
                return "SMS已發送，等待回應";
            case "SMS_COMPLETE":
                return "SMS處理程序完成";
            case "SMS_FAIL":
                return "SMS發送失敗";


            case "PROCESS":
                return "發送處理進行中";
            case "FINISH":
                return "發送處理完成";
            case "SENDING":
                return "發送中";
            case "DELETE":
                return "已刪除";
            case "COMPLETE":
                return "處理程序完成";
            default:
                return status;
        }
    }

    /* 取得分頁總數並變更畫面 */
    var fetchListCountAndChange = function () {
        // get Total
        $('.LyMain').block($.BCS.blockMsgRead);

        var getUrl = bcs.bcsContextPath + '/pnpEmployee/getPNPDetailReportTotalPages?'
            + 'startDate=' + startDate
            + '&endDate=' + endDate
            + '&sourceSystem=' + document.getElementById('sourceSystemInput').value
            + '&pccCode=' + document.getElementById('pccCodeInput').value
            + '&account=' + document.getElementById('accountInput').value
            + '&phoneNumber=' + document.getElementById('phoneNumber').value;

        console.info('getUrl', getUrl);

        $.ajax({
            type: 'GET',
            url: getUrl
        }).success(function (response) {
            console.info('msg1: ', response['msg']);
            totalPages = parseInt(response['msg']);
            console.info('totalPages1: ', totalPages);
            // set pageAndTotalPage
            page = 1;
            console.info(page + '/' + totalPages);
            $('#pageAndTotalPages').text(page + '/' + totalPages);
        }).fail(function (response) {
            console.info(response);
            $.FailResponse(response);
            $('.LyMain').unblock();
        }).done(function () {
            $('.LyMain').unblock();
        });
    }

    var cleanList = function(){
        $('.resultList').remove();
        $('.resultList2').remove();
        $('.resultList-odd').remove();
        $('.resultList2-odd').remove();
        console.log('Result List Remove!!')
    }

    // initialize Page
    var initPage = function () {
        originalTr = $('.resultList').clone(true);
        originalTr2 = $('.resultList2').clone(true);
        originalTrOdd = $('.resultList-odd').clone(true);
        originalTr2Odd = $('.resultList2-odd').clone(true);
        cleanList();
        originalTable = $('#resultTable').clone(true);
        startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
        endDate = moment(new Date()).format('YYYY-MM-DD');
        $('#startDate').val(startDate);
        $('#endDate').val(endDate);
    };

    initPage();
});