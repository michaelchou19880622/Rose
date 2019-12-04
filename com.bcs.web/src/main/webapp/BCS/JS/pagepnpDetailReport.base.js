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
      startDate = $('#startDate').val();
      endDate = $('#endDate').val();
      loadData();
    }
  });

  $('#exportBtn').click(function() {
    setExportButtonSource();
  });

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
        var getUrl = bcs.bcsContextPath + '/pnpEmployee/exportPNPDetailReportExcel';
        console.info('getUrl', getUrl);
        $.ajax({
          type: 'POST',
          url: getUrl,
          contentType: 'application/json',
          data: JSON.stringify({
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
        }).success(function(){
            console.log('export success');
        }).fail(function(e){
            console.log(e);
        })
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
          var list =
            i % 2 == 0 ? originalTrOdd.clone(true) : originalTr.clone(true);

          list.find('.no').html(i);
          list.find('.sourceSystem').html(obj.sourceSystem);
          list.find('.account').html(obj.account);
          list.find('.pathway').html(obj.processFlow);
          list.find('.proc_stage').html(obj.processStage);
          list.find('.pnpContent').html(obj.message);
          list.find('.customerCellPhoneNumber').html(obj.phone);
          list.find('.bcStatusCode').html(obj.bcStatus + (obj.bcHttpStatusCode === null ? '' : obj.bcHttpStatusCode));
          list.find('.pnpStatusCode').html(obj.pnpStatus + (obj.pnpHttpStatusCode === null ? '' : obj.pnpHttpStatusCode));
          list.find('.smsStatusCode').html(obj.smsStatus);
          list.find('.accountPccCode').html(obj.pccCode);
          list
            .find('.schedule_time')
            .html(
              moment(obj.scheduleTime, 'YYYY-MM-DD HH:mm:ss').format(
                'YYYY-MM-DD HH:mm:ss'
              )
            );

          $('#resultTable').append(list);

          var list2 =
            i % 2 == 0 ? originalTr2Odd.clone(true) : originalTr2.clone(true);
          list2.find('.bc_time').html(obj.bcTime);
          list2.find('.pnp_time').html(obj.pnpTime);
          list2.find('.sms_time').html(obj.smsTime);

          $('#resultTable').append(list2);
          i++;
        });

        hasData = i > 0;
        console.log('Has data : ' + hasData);

        setExportButtonSource();
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

  var converterPathWayCodeToName = function(pathwayCode) {
    if (pathwayCode === '3') {
      return 'BC<br/>PNP<br/>SMS';
    } else if (pathwayCode === '2') {
      return 'BC<br/>SMS';
    } else if (pathwayCode === '1') {
      return 'BC';
    }
  };

  var parseCodeToChinese = function(status) {
    return pnpStatusMap[status];
  };

  /* 取得分頁總數並變更畫面 */
  var fetchListCountAndChange = function() {
    // get Total
    $('.LyMain').block($.BCS.blockMsgRead);

    var getUrl =
      bcs.bcsContextPath +
      '/pnpEmployee/getPNPDetailReportTotalPages?' +
      'startDate=' +
      startDate +
      '&endDate=' +
      endDate +
      '&sourceSystem=' +
      document.getElementById('sourceSystemInput').value +
      '&pccCode=' +
      document.getElementById('pccCodeInput').value +
      '&account=' +
      document.getElementById('accountInput').value +
      '&phoneNumber=' +
      document.getElementById('phoneNumber').value;

    console.info('getUrl', getUrl);

    $.ajax({
      type: 'GET',
      url: getUrl
    })
      .success(function(response) {
        console.info('msg1: ', response['msg']);
        totalPages = parseInt(response['msg']);
        console.info('totalPages1: ', totalPages);
        // set pageAndTotalPage
        page = 1;
        console.info(page + '/' + totalPages);
        $('#pageAndTotalPages').text(page + '/' + totalPages);
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
