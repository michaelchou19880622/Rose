/**
 *
 */

$(function () {
    // ---- Global Variables ----
    // input data
    var employeeRecordId = "";
    var divisionName = "";
    var departmentName = "";
    var groupName = "";
    var pccCode = "";
    var account = "";
    var employeeId = "";

    // result data
    var hasData = false;
    var templateCount = 0;
    var oringinalTr = {};
    var originalTable = {};
    var startDate = null,
        endDate = null;
    var page = 1,
        totalPages = 0;
    var firstFatch = true;

    // ---- Functions ----

    // set Date
    $(".datepicker").datepicker({
        maxDate: 0,
        dateFormat: 'yy-mm-dd',
        changeMonth: true
    });

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
        firstFatch = true;
        return true;
    }

    $('.btn.prev').click(function () {
        if (page > 1) {
            page--;
            loadData();
            // set pageAndTotalPage
            $('#pageAndTotalPages').text(page + '/' + totalPages);
        }
    });

    $('.btn.next').click(function () {
        if (page < totalPages) {
            page++;
            loadData();
            // set pageAndTotalPage
            $('#pageAndTotalPages').text(page + '/' + totalPages);
        }
    });

    // do Search
    $('.btn_add.search').click(function () {
        if (dataValidate()) {
            $('.resultTr').remove();
            startDate = $('#startDate').val();
            endDate = $('#endDate').val();

            // get all list data
            loadData();
        }
    });

    // do Download
    var setExportButtonSource = function () {
        if (hasData) {
            var sourceSystemInput = $('#sourceSystemInput').val();
            var pccCodeInput = $('#pccCodeInput').val();
            var accountInput = $('#accountInput').val();
            var getUrl = bcs.bcsContextPath + '/pnpEmployee/exportPNPDetailReportExcel?startDate=' + startDate + '&endDate=' + endDate +
                '&sourceSystem=' + sourceSystemInput + '&pccCodeInput=' + pccCode + '&account=' + accountInput;
            console.info('getUrl', getUrl);

            $('.btn_add.exportToExcel').attr('href', getUrl);
        } else {
            $('.btn_add.exportToExcel').attr('href', '#');
        }
    }

    var parsePathwayCodeToChinese = function(pathwayCode){
        if (pathwayCode === '3') {
            return 'BC<br/>PNP<br/>SMS';
        } else if (pathwayCode === '2') {
            return 'BC<br/>SMS';
        } else if (pathwayCode === '1') {
            return 'BC';
        }
    }

    // ---- Initialize Page & Load Data ----
    // get List Data
    var loadData = function () {
        $('.LyMain').block($.BCS.blockMsgRead);
        $('.resultTr').remove();
        console.info("firstFatch:", firstFatch);
        if (firstFatch) {
            firstFatch = false;
            setTotal();
        }
        var sourceSystemInput = $('#sourceSystemInput').val();
        var pccCodeInput = $('#pccCodeInput').val();
        var accountInput = $('#accountInput').val();
        var getUrl = bcs.bcsContextPath + '/pnpEmployee/getPNPDetailReport?startDate=' + startDate + '&endDate=' + endDate + '&page=' + page + '&sourceSystem=' + sourceSystemInput +
            '&pccCodeInput=' + pccCode + '&account=' + accountInput;
        console.info('getUrl', getUrl);

        $.ajax({
            type: 'GET',
            url: getUrl,
            contentType: 'application/json',
        }).success(function (response) {
            console.info("response:", response);

            //------------------------------------------------------
            if (response.length === 0) {
                hasData = false;
            } else {
                hasData = true;
            }

            for (var key in response) {
                var resultTr = originalTr.clone(true);
                var valueObj = response[key];
                //console.info('valueObj : ', valueObj);

                // 0	ORIG_FILE_NAME
                var splits = valueObj[0].split('_');
                // 前方來源系統 PRMSMS
                resultTr.find('.sourceSystem').html(splits[1]);
                // 發送帳號
                resultTr.find('.account').html(splits[2]);

                // 1	PROC_FLOW
//                if (valueObj[1] == '3') {
//                    resultTr.find('.pathway').html('BC-&gt;PNP-&gt;SMS');
//                } else if (valueObj[1] == '2') {
//                    resultTr.find('.pathway').html('BC-&gt;SMS');
//                } else if (valueObj[1] == '1') {
//                    resultTr.find('.pathway').html('BC');
//                }

                resultTr.find('.pathway').html(parsePathwayCodeToChinese(valueObj[1]));

                // 2	SOURCE
//                if (valueObj[2] == '4') {
//                    resultTr.find('.deliveryPathway').html('UNICA');
//                } else if (valueObj[2] == '3') {
//                    resultTr.find('.deliveryPathway').html('明宣');
//                } else if (valueObj[2] == '2') {
//                    resultTr.find('.deliveryPathway').html('互動');
//                } else if (valueObj[2] == '1') {
//                    resultTr.find('.deliveryPathway').html('三竹');
//                }

                resultTr.find('.proc_stage').html(valueObj[2]);

                // 3	MSG
                resultTr.find('.pnpContent').html(valueObj[3]);

                // 4	PHONE
                resultTr.find('.customerCellPhoneNumber').html(valueObj[4]);

                // 5	預約時間
//                resultTr.find('.scheduleDate').html(moment(valueObj[5], 'YYYY-MM-DD HH:mm:ss').format('YYYY-MM-DD'));
                // 6
                resultTr.find('.scheduleTime').html(moment(valueObj[5], 'YYYY-MM-DD HH:mm:ss').format('YYYY-MM-DD HH:mm:ss'));

                // 7 bc time
//                resultTr.find('.bcTime').html(valueObj[7]);
                // 8 pnp time
//                resultTr.find('.pnpTime').html(valueObj[8]);


                resultTr.find('.bcStatusCode').html(parseCodeToChinese(valueObj[9]) + '<br/>' + valueObj[7]);
                // 10 pnp_status
                resultTr.find('.pnpStatusCode').html(parseCodeToChinese(valueObj[10]) + '<br/>' + valueObj[8]);
                // 11 sms_status
                resultTr.find('.smsStatusCode').html(parseCodeToChinese(valueObj[11]));

                // 12 PCC_CODE
                resultTr.find('.accountPccCode').html(valueObj[12]);

                $('.resultTable').append(resultTr);
            }

            setExportButtonSource();

        }).fail(function (response) {
            console.info(response);
            $.FailResponse(response);
            $('.LyMain').unblock();
        }).done(function () {
            $('.LyMain').unblock();
        });
    };

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

    var setTotal = function () {
        // get Total
        $('.LyMain').block($.BCS.blockMsgRead);

        var sourceSystemInput = $('#sourceSystemInput').val();
        var pccCodeInput = $('#pccCodeInput').val();
        var accountInput = $('#accountInput').val();
        var getUrl = bcs.bcsContextPath + '/pnpEmployee/getPNPDetailReportTotalPages?startDate=' + startDate + '&endDate=' + endDate + '&sourceSystem=' + sourceSystemInput +
            '&pccCode=' + pccCode + '&account=' + accountInput;
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

    // initialize Page
    var initPage = function () {
        // clone & remove
        originalTr = $('.resultTr').clone(true);
        $('.resultTr').remove();
        originalTable = $('.resultTable').clone(true);

        // initialize date-picker
        startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
        endDate = moment(new Date()).format('YYYY-MM-DD');
        $('#startDate').val(startDate);
        $('#endDate').val(endDate);
    };

    initPage();
});