/**
 *
 */

$(function() {
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
  var isCreateTime = true,
      isOrderTime = false;
  var page = 1,
    totalPages = 0;
  var pnpStatusMap = {};

  var pageBtnHandler = function(condition, actionName) {
    if (condition) {
      page = actionName === 'next' ? ++page : --page;
      console.log('Currency Page Number is ' + page);
      loadData();
      $('#pageAndTotalPages').text(page + '/' + totalPages);
    }
  };

  var getPnpStatusMap = function() {
    $.ajax({
      type: 'GET',
      url: bcs.bcsContextPath + '/pnpEmployee/getPnpStatusEnum',
      contentType: 'application/json'
    })
      .success(function(response) {
        console.log(response);
        if (response !== null) {
          pnpStatusMap = response;
        }
      })
      .fail(function(response) {
        console.log(response);
      })
      .done(function(response) {
        console.log('Fetch PnpStatusMap done!!');
      });
  };

  //-------------------Event----------------------
  $('.datepicker').datepicker({
    maxDate: 0,
    dateFormat: 'yy-mm-dd',
    changeMonth: true
  });

  $('#backBtn').click(function() {
    pageBtnHandler(page > 1, 'back');
  });

  $('#nextBtn').click(function() {
    pageBtnHandler(page < totalPages, 'next');
  });

  // do Search
  $('#searchBtn').click(function() {
    if (dataValidate()) {
      cleanList();
      page = 1;
      startDate = $('#startDate').val();
      endDate = $('#endDate').val();
      loadData();
    }
  });

  $('#exportBtn').click(function() {
//    setExportButtonSource();
    if (hasData) {
        var type = isCreateTime ? 'createTime' : 'orderTime';
        var getUrl =
        bcs.bcsContextPath +
        '/pnpEmployee/exportPNPDetailReportExcel' +
        '?startDate=' + startDate +
        '&endDate=' + endDate +
        '&isPageable=false' +
        '&page=' + page +
        '&account=' + document.getElementById('accountInput').value +
        '&pccCode=' + document.getElementById('pccCodeInput').value +
        '&sourceSystem=' + document.getElementById('sourceSystemInput').value +
        '&phone=' + document.getElementById('phoneNumber').value +
        '&dateType=' + type
        ;
        console.info('getUrl: ' + getUrl);
        window.location.href = getUrl;
    }

  });

  $('#isCreateTimeBtn').click(function(){
    if (isOrderTime) {
        var createTimeBtn = document.getElementById('isCreateTimeBtn');
        createTimeBtn.className = 'btn2 btn-style-pressed'
        var orderTimeBtn = document.getElementById('isOrderTimeBtn');
        orderTimeBtn.className = 'btn2 btn-style'
        isCreateTime = true;
        isOrderTime = false;
    }
  })

  $('#isOrderTimeBtn').click(function(){
    if (isCreateTime) {
        var createTimeBtn = document.getElementById('isCreateTimeBtn');
        createTimeBtn.className = 'btn2 btn-style'
        var orderTimeBtn = document.getElementById('isOrderTimeBtn');
        orderTimeBtn.className = 'btn2 btn-style-pressed'
        isCreateTime = false;
        isOrderTime = true;
    }
  })
  //-------------------Event----------------------

  var dataValidate = function() {
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
    if (
      !moment(startDate)
        .add(31, 'days')
        .isAfter(moment(endDate))
    ) {
      alert('起始日期與結束日期之間不可相隔超過一個月！');
      return false;
    }
    if (moment(startDate).isAfter(moment(endDate))) {
      alert('起始日期不可大於結束日期！');
      return false;
    }
    firstFetch = true;
    return true;
  };

  // do Download
  var setExportButtonSource = function() {
    if (hasData) {
      var getUrl =
        bcs.bcsContextPath +
        '/pnpEmployee/exportPNPDetailReportExcel' +
        '?startDate=' +
        startDate +
        '&endDate=' +
        endDate +
        '&isPageable=false' +
        '&page=' +
        page +
        '&account=' +
        document.getElementById('accountInput').value +
        '&pccCode=' +
        document.getElementById('pccCodeInput').value +
        '&sourceSystem=' +
        document.getElementById('sourceSystemInput').value +
        '&phone=' +
        document.getElementById('phoneNumber').value +
        '&dateType=' +
        isCreateTime ? 'createTime' : 'orderTime'
        ;
      console.info('getUrl', getUrl);

      $('.btn_add.exportToExcel').attr('href', getUrl);
    } else {
      $('.btn_add.exportToExcel').attr('href', '#');
    }
  };

  // ---- Initialize Page & Load Data ----
  // get List Data
  var loadData = function() {
    $('.LyMain').block($.BCS.blockMsgRead);
    cleanList();

    console.info('firstFetch:', firstFetch);
    if (firstFetch) {
      firstFetch = false;
      fetchListCountAndChange();
    }
    var getUrl = bcs.bcsContextPath + '/pnpEmployee/getPNPDetailReport';

    console.info('getUrl', getUrl);

    $.ajax({
      type: 'POST',
      url: getUrl,
      contentType: 'application/json',
      data: JSON.stringify({
        dateType : isCreateTime ? 'createTime' : 'orderTime',
        startDate: startDate,
        endDate: endDate,
        isPageable: true,
        page: page,
        account: document.getElementById('accountInput').value,
        pccCode: document.getElementById('pccCodeInput').value,
        sourceSystem: document.getElementById('sourceSystemInput').value,
        employeeId: null,
        phone: document.getElementById('phoneNumber').value
      })
    })
      .success(function(response) {
        console.info('response:', response);
        console.log('response:', JSON.stringify(response));
        var i = 1;
        response.forEach(function(obj) {
          console.log('i = ' + i);
          var list = originalTr.clone(true);

          list.find('.no').html(i);
          list.find('.sourceSystem').html(obj.sourceSystem);
          list.find('.account').html(obj.account);
          list.find('.pathway').html(obj.processFlow);
          list.find('.proc_stage').html(obj.processStage);
          list.find('.pnpContent').html(obj.message);
          list.find('.customerCellPhoneNumber').html(obj.phone);
          var bcStatus = '';
          if (obj.bcHttpStatusCode !== null && obj.bcHttpStatusCode.trim() !== '') {
            bcStatus = obj.bcStatus + ' [' + obj.bcHttpStatusCode + ']';
          } else {
            bcStatus = obj.bcStatus;
          }
          list.find('.bcStatusCode').html(bcStatus);
          var pnpStatus = '';
          if (obj.pnpHttpStatusCode !== null && obj.pnpHttpStatusCode.trim() !== '') {
            pnpStatus = obj.pnpStatus + ' [' + obj.pnpHttpStatusCode + ']';
          } else {
            pnpStatus = obj.pnpStatus;
          }
          list.find('.pnpStatusCode').html(pnpStatus);
          list.find('.smsStatusCode').html(obj.smsStatus);
          list.find('.accountPccCode').html(obj.pccCode);
          if (obj.scheduleTime !== null) {
            list.find('.schedule_time').html(moment(obj.scheduleTime).format('YYYY-MM-DD HH:mm:ss'));
          }
          list.find('.resendBtn').click(function() {
            resendSms(obj.detailId, obj.ftpSource);
          });

          $('#resultTable').append(list);

          var list2 = originalTr2.clone(true);
          if (obj.bcTime !== null) {
            list2.find('.bc_time').html(moment(obj.bcTime).format('YYYY-MM-dd HH:mm:ss'));
          }
          if (obj.pnpTime !== null) {
            list2.find('.pnp_time').html(moment(obj.pnpTime).format('YYYY-MM-dd HH:mm:ss'));
          }
          if (obj.smsTime !== null) {
            list2.find('.sms_time').html(moment(obj.smsTime).format('YYYY-MM-dd HH:mm:ss'));
          }
          // <button id="resendBtn"
          // sec:authorize="hasAnyRole('ROLE_ADMIN','ROLE_PNP_ADMIN','ROLE_MARKET','ROLE_PNP_SEND_LINE_SEND','ROLE_PNP_SEND_LINE_VERIFY')">
          //     再次發送SMS
          // </button>

          $('#resultTable').append(list2);
          i++;
        });

        hasData = i > 0;
        console.log('Has data : ' + hasData);

//        setExportButtonSource();
      })
      .fail(function(response) {
        console.info(response);
        $.FailResponse(response);
        $('.LyMain').unblock();
      })
      .done(function() {
        $('.LyMain').unblock();
      });
  };

  var resendSms = function(id, ftp) {
    switch (ftp) {
      case '明軒':
        ftp = 'MING';
        break;
      case '三竹':
        ftp = 'MITAKE';
        break;
      case 'Unica':
        ftp = 'UNICA';
        break;
      case '互動':
        ftp = 'EVERY8D';
        break;
      default:
        break;
    }
    $('.LyMain').block($.BCS.blockMsgRead);
    var apiUrl =
      bcs.bcsContextPath +
      '/pnpEmployee/resend/sms' +
      '?detailId=' +
      id +
      '&ftpSourceName=' +
      ftp;
    console.log(apiUrl);
    $.ajax({
      type: 'POST',
      url: apiUrl,
      contentType: 'application/x-www-form-urlencoded'
    })
      .success(function(response) {
        console.log(response);
        alert('已設定重發排程!!');
      })
      .fail(function(e) {
        $.FailResponse(e);
        $('.LyMain').unblock();
      })
      .done(function() {
        $('.LyMain').unblock();
      });
  };

  /* 取得分頁總數並變更畫面 */
  var fetchListCountAndChange = function() {
    // get Total
    $('.LyMain').block($.BCS.blockMsgRead);

    var getUrl =
      bcs.bcsContextPath + '/pnpEmployee/getPNPDetailReportTotalPages';

    console.info('getUrl', getUrl);

    $.ajax({
      type: 'POST',
      url: getUrl,
      contentType: 'application/json',
      data: JSON.stringify({
        dateType: isCreateTime ? 'createTime' : 'orderTime',
        startDate: startDate,
        endDate: endDate,
        isPageable: false,
        page: page,
        account: document.getElementById('accountInput').value,
        pccCode: document.getElementById('pccCodeInput').value,
        sourceSystem: document.getElementById('sourceSystemInput').value,
        employeeId: null,
        phone: document.getElementById('phoneNumber').value
      })
    })
      .success(function(response) {
        console.log(response);
        totalPages = parseInt(response);
        console.info('totalPages1: ', totalPages);
        page = 1;
        console.info(page + '/' + totalPages);
        $('#pageAndTotalPages').text(page + '/' + totalPages);
      })
      .fail(function(response) {
        console.log(response);
        $.FailResponse(response);
        $('.LyMain').unblock();
      })
      .done(function() {
        $('.LyMain').unblock();
      });
  };

  var cleanList = function() {
    $('.resultList').remove();
    $('.resultList2').remove();
    $('.resultList-odd').remove();
    $('.resultList2-odd').remove();
    console.log('Result List Remove!!');
  };

  // initialize Page
  var initPage = function() {
    originalTr = $('.resultList').clone(true);
    originalTr2 = $('.resultList2').clone(true);
    originalTrOdd = $('.resultList-odd').clone(true);
    originalTr2Odd = $('.resultList2-odd').clone(true);
    cleanList();
    originalTable = $('#resultTable').clone(true);
    //        startDate = moment(new Date()).add(-7, 'days').format('YYYY-MM-DD');
    startDate = moment(new Date()).format('YYYY-MM-DD');
    endDate = moment(new Date()).format('YYYY-MM-DD');
    $('#startDate').val(startDate);
    $('#endDate').val(endDate);
    getPnpStatusMap();
  };

  initPage();
});
