/**
 * PNP Maintain Account Config And PNP Flex Template Config Page
 */
$(function () {
    var pnpMaintainAccountModelId = null;
    var originalPathWay = '';
    var originalPnpContent = '';
    var originalTemplate = '';
    var popInstance = null;

    var oriPopup = null;

    var initPage = function () {
        console.log('Init Page!!');
        pnpMaintainAccountModelId = $.urlParam("pnpMaintainAccountModelId");
        console.log('From URL Path Param -> PNP Maintain Account Model Id : ' + pnpMaintainAccountModelId);

        if (pnpMaintainAccountModelId !== null && pnpMaintainAccountModelId !== '') {
            $('.LyMain').block($.BCS.blockMsgRead);
            $('.CHTtl').html('編輯Unica帳號');
            document.getElementById('popupEditPage').value = 'Edit';
            $.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + "/pnpAdmin/getPNPMaintainAccount?id=" + pnpMaintainAccountModelId,
            }).success(function (response) {
                console.log("Success!! Response: " + JSON.stringify(response));
                $('#account').val(response.account);
                $('#accountAttribute').val(response.accountAttribute);
                $('#sourceSystem').val(response.sourceSystem);
                $('#employeeId').val(response.employeeId);
                $('#departmentId').val(response.departmentId);
                $('#divisionName').val(response.divisionName);
                $('#departmentName').val(response.departmentName);
                $('#groupName').val(response.groupName);
                $('#PccCode').val(response.pccCode);
                $('.accountClass')[response.accountClass == 'O' ? 0 : 1].checked = true;
                $('.status')[response.status ? 0 : 1].checked = true;

                /* Backup Original Data */
                originalTemplate = response.template;
                originalPnpContent = response.pnpContent;
                originalPathWay = pathWayCodeToName(response.pathway);

                $('#flexContent').val(originalPnpContent);

                document.getElementById('quickViewPathWay').textContent = originalPathWay;
                document.getElementById('quickViewTemplate').textContent = originalTemplate;
                document.getElementById('quickViewPNPContent').textContent = originalPnpContent;

            }).fail(function (response) {
                console.log('Fail!! Response: ' + response);
                $.FailResponse(response);
            }).done(function () {
                $('.LyMain').unblock();
                loadPopupConfig();
            });
        } else {
            console.log('Id is Null!! To Create Mode!!');
            loadPopupConfig();
        }
    };


    //--------------------- Load Config ------------------------
    var config = {};
    var loadPopupConfig = function () {
        console.log('Load Popup Config!!');
        $.ajax({
                url: bcs.bcsContextPath + '/pnpAdmin/getFlexConfig',
                type: 'GET',
            })
            .done(function (response) {
                console.log("Success!!, Response: " + response);
                config = JSON.parse(JSON.stringify(response));
                console.log(config);
                initPopPage();
            })
            .fail(function (response) {
                console.log("error");
                $.FailResponse(response);
            })
            .always(function () {
                console.log("complete");
            });
    };
    //--------------------- Load Config ------------------------

    //--------------------- Init Pop Page ------------------------
    var initPopPage = function () {
        console.log('Init Popup Page!!');
        popInstance = 'initialed';
        oriPopup = $('#dialog-modal').clone();
        generatePopupSelectList();
        if (pnpMaintainAccountModelId === null || pnpMaintainAccountModelId === '') {
            defaultPopUp();
        } else {
            //load user before setting
            var templateId = document.getElementById('quickViewTemplate').textContent;
            if (templateId !== null && templateId !== '') {
                $.ajax({
                        url: bcs.bcsContextPath + '/pnpAdmin/getFlexConfig/' + templateId,
                        type: 'GET',
                    })
                    .done(function (response) {
                        console.log("Success!!, Response: " + response);
                        console.log(response);
                        console.log(JSON.stringify(response))

                        //                    { "id": 1, "headerBackground": "#FF9791", "headerTextSize": "xl", "headerTextColor": "#000000",
                        //                    "headerTextWeight": "bold", "headerTextStyle": "italic", "headerTextDecoration": "underline",
                        //                    "headerText": "Title", "heroBackground": "#FFF7C4", "heroTextSize": "lg", "heroTextColor": "#FF94A9",
                        //                    "heroTextWeight": "bold", "heroTextStyle": "italic", "heroTextDecoration": "none", "heroText": "Hello Flex!!!",
                        //                    "bodyBackground": "#FFFFFF", "bodyDescTextSize": "xs", "bodyDescTextColor": "#666666", "bodyDescTextWeight": "regular",
                        //                    "bodyDescTextStyle": "normal", "bodyDescTextDecoration": "none",
                        //                    "bodyDescText": "此通知在用戶簽約時所註冊之電話號碼和LINE上所註冊之電話號碼一致時發送。", "buttonText": "Clcik!!",
                        //                    "buttonColor": "#6DFF59", "buttonUrl": "http://www.google.com", "footerLinkText": "收到此訊息的原因是？",
                        //                    "footerLinkUrl": "https://linecorp.com", "modifyTime": "2019-10-16 14:47:24.187", "createTime": "2019-10-16 14:47:24" }


                        //Footer Preview
                        document.getElementById('footerLinkLabel').textContent = response.footerLinkText || config.footer.linkText;
                        document.getElementById('footerLink').href = response.footerLinkUrl || config.footer.linkUrl;

                        //Header Input
                        document.getElementById('flexTitle').value = response.headerText || config.header.text;
                        document.getElementById('headerTitleBackgroundColor').value = response.headerBackground || config.header.background;

                        //Header Preview
                        document.getElementById('previewTitle').textContent = response.headerText || config.header.text;
                        document.getElementById('msgTitle-area').style.background = response.headerBackground || config.header.background;


                        //Hero Input
                        document.getElementById('heroTextColorText').value = response.heroTextColor || config.hero.textColor;
                        document.getElementById('heroBackgroundColorText').value = response.heroBackground || config.hero.background;
                        //Hero Preview
                        document.getElementById('mainMsg').style.background = response.heroBackground || config.hero.background;
                        document.getElementById('previewContent').textContent = response.heroText || config.hero.text;
                        document.getElementById('previewContent').style.color = response.heroTextColor || config.hero.textColor;


                        //Body Preview
                        document.getElementById('bodyDescriptionLabel').style.color = response.bodyDescTextColor || config.body.description.textColor;
                        document.getElementById('bodyDescriptionLabel').style.fontWeight = response.bodyDescTextWeight || config.body.description.textWeight;
                        document.getElementById('bodyDescriptionLabel').style.fontStyle = response.bodyDescTextStyle || config.body.description.textStyle;
                        document.getElementById('bodyDescriptionLabel').style.textDecoration = response.bodyDescTextDecoration || config.body.description.textDecoration;
                        document.getElementById('bodyDescriptionLabel').textContent = response.bodyDescText || config.body.description.text;


                        var buttonLabelArray = response.buttonText.split(',');
                        var buttonColorArray = response.buttonColor.split(',');
                        var buttonUrlArray = response.buttonUrl.split(',');

                        for (var i = 0; i < buttonLabelArray.length; i++) {
                            console.log('Label : ' + buttonLabelArray[i] + ' Color : ' + buttonColorArray[i].replace('#') + ' Url : ' + buttonUrlArray[i])
                            addBtn(buttonLabelArray[i], buttonColorArray[i], buttonUrlArray[i])
                        }

                        styleBtnClick('bold', response.headerTextWeight, 'titleTextBoldBtn')
                        styleBtnClick('italic', response.headerTextStyle, 'titleTextItalicBtn')
                        styleBtnClick('underline', response.headerTextDecoration, 'titleTextUnderLineBtn')

                        styleBtnClick('bold', response.heroTextWeight, 'heroTextBoldBtn')
                        styleBtnClick('italic', response.heroTextStyle, 'heroTextItalicBtn')
                        styleBtnClick('underline', response.heroTextDecoration, 'heroTextUnderLineBtn')
                        triggerFeatureActive();
                    })
                    .fail(function (response) {
                        console.log("error");
                        console.log(response);
//                        $.FailResponse(response);
                        defaultPopUp();
                    })
                    .always(function () {
                        console.log("complete");
                        saveBeforeTemplateJson = generateTemplateJson();
                    });
            } else {
                defaultPopUp();
            }

        }
    };

    var styleBtnClick = function (expect, real, targetElementId) {
        if (expect === real) {
            document.getElementById(targetElementId).click();
        }
    }


    var defaultPopUp = function () {
        // Load Default Config
        document.getElementById('previewTitle').textContent = document.getElementById('flexTitle').value || config.header.text;
        document.getElementById('previewContent').textContent = document.getElementById('flexContent').value || config.hero.text;

        document.getElementById('bodyDescriptionLabel').textContent = config.body.description.text;
        document.getElementById('footerLink').href = config.footer.linkUrl;
        document.getElementById('footerLinkLabel').textContent = config.footer.linkText;

        document.getElementById('msgTitle-area').style.background = config.header.background;
        document.getElementById('mainMsg').style.background = config.hero.background;

        document.getElementById('bodyDescriptionLabel').style.color = config.body.description.textColor;
        document.getElementById('bodyDescriptionLabel').style.fontWeight = config.body.description.textWeight;
        document.getElementById('bodyDescriptionLabel').style.fontStyle = config.body.description.textStyle;
        document.getElementById('bodyDescriptionLabel').style.textDecoration = config.body.description.textDecoration;
        /* Init Create First Button Row */
//        addDefaultButton();
        triggerFeatureActive();
        saveBeforeTemplateJson = generateTemplateJson();
    }

    var triggerFeatureActive = function(){
        var elements = document.getElementsByClassName('color_input_text');
        for(var i = 3; i< elements.length; i++){
            console.log(elements[i]);
            elements[i].disabled = !config.isEnableBodyButtonCustomColor;
        }

        document.getElementById('headerTitleBackgroundColor').disabled = !config.isEnableHeaderCustomBackground
//        document.getElementById('').disabled = config.isEnableHeaderCustomTextColor
        document.getElementById('titleTextUnderLineBtn').disabled = !config.isEnableHeaderCustomTextDecoration
//        document.getElementById('').disabled = config.isEnableHeaderCustomTextSize
        document.getElementById('titleTextItalicBtn').disabled = !config.isEnableHeaderCustomTextStyle
        document.getElementById('titleTextBoldBtn').disabled = !config.isEnableHeaderCustomTextWeight

        document.getElementById('heroBackgroundColorText').disabled = !config.isEnableHeroCustomBackground
        document.getElementById('heroTextColorText').disabled = !config.isEnableHeroCustomTextColor
        document.getElementById('heroTextUnderLineBtn').disabled = !config.isEnableHeroCustomTextDecoration
//        document.getElementById('').disabled = config.isEnableHeroCustomTextSize
        document.getElementById('heroTextItalicBtn').disabled = !config.isEnableHeroCustomTextStyle
        document.getElementById('heroTextBoldBtn').disabled = !config.isEnableHeroCustomTextWeight
    }

    //--------------------- Init Pop Page -----------------------

    //--------------------- Init Pop Select List -----------------------
    var generatePopupSelectList = function () {
        /* Pop Pathway Select Option */
        var appendOption = function (listName, value, text) {
            var opt = document.createElement('option');
            var list = document.getElementById(listName);
            opt.value = value;
            opt.innerHTML = text;
            list.appendChild(opt);
        };

        appendOption('pathwayList', 3, 'BC->PNP->SMS');
        appendOption('pathwayList', 2, 'BC->SMS');
        appendOption('pathwayList', 1, 'BC');

        /* 選取先前選取的 */
        var pathName = document.getElementById('quickViewPathWay').textContent;
        if (pathName === 'BC->PNP->SMS') {
            $('#pathwayList').get(0).selectedIndex = 1;
        } else if (pathName === 'BC->SMS') {
            $('#pathwayList').get(0).selectedIndex = 2;
        } else if (pathName === 'BC') {
            $('#pathwayList').get(0).selectedIndex = 3;
        }
    };
    //--------------------- Init Pop Select List -----------------------


    var pathWayCodeToName = function (pathwayCode) {
        var name = '';
        if ('3' === pathwayCode) {
            name = 'BC->PNP->SMS';
        } else if ('2' === pathwayCode) {
            name = 'BC->SMS';
        } else if ('1' === pathwayCode) {
            name = 'BC';
        }
        return name;
    };

    var pathWayNameToCode = function (pathwayName) {
        var code = '';
        if ('BC->PNP->SMS' === pathwayName) {
            code = '3';
        } else if ('BC->SMS' === pathwayName) {
            code = '2';
        } else if ('BC' === pathwayName) {
            code = '1';
        }
        return code;
    };

    var addDefaultButton = function addDefaultButton() {
        addBtn(
            config.body.button.buttonText,
            config.body.button.buttonColor,
            ''
        );
    }

    //--------------------- Add Button ------------------------
    var btnSize = 0;
    var btnIndex = 0;
    var addBtn = function addBtn(btnText, btnBackColor, btnUrl) {
        console.log('All Btn size: ' + btnSize);
        if (btnSize >= config.buttonMaxQuantity) {
            alert('已達最大按鈕數量!!');
            return;
        }
        btnIndex++;
        /* Button Name */
        var btnNameInput = document.createElement('input');
        btnNameInput.type = 'text';
        btnNameInput.id = 'btnName-' + btnIndex;
        btnNameInput.className = 'input_text';
        btnNameInput.value = btnText || '';
        btnNameInput.maxLength = 10;

        /* 監聽按鈕文字輸入內容即時反映於預覽區 */
        btnNameInput.onkeyup = function () {
            var i = this.id.split('-')[1];
            var value = document.getElementById('btnName-' + i).value;
            if (value === undefined || value === null || value === '') {
                value = config.body.button.buttonText;
            }
            document.getElementById('previewBtn' + i).textContent = value;
        };

        var btnNameDiv2 = document.createElement('div');
        btnNameDiv2.className = 'param';
        btnNameDiv2.appendChild(btnNameInput);

        var btnNameDiv1 = document.createElement('div');
        btnNameDiv1.className = 'warp20';
        btnNameDiv1.appendChild(btnNameDiv2);

        /* Button Url */
        var btnUrlInput = document.createElement('input');
        btnUrlInput.type = 'text';
        btnUrlInput.id = 'btn-url' + btnIndex;
        btnUrlInput.className = 'input_text2';
        btnUrlInput.value = btnUrl;

        var btnUrlDiv2 = document.createElement('div');
        btnUrlDiv2.className = 'param';
        btnUrlDiv2.appendChild(btnUrlInput);

        var btnUrlDiv1 = document.createElement('div');
        btnUrlDiv1.className = 'warp40';
        btnUrlDiv1.appendChild(btnUrlDiv2);


        /* Remove Button */
        var removeButton = document.createElement('button');
        removeButton.className = 'btn btn-warn';
        removeButton.id = 'removeButtonBtn' + btnIndex;
        removeButton.textContent = '－';
        removeButton.value = btnIndex;
        removeButton.setAttribute("style", "margin-left:20px");
        removeButton.onclick = function () {
            var i = removeButton.value;
            document.getElementById('btnGroup').removeChild(document.getElementById('row' + i));
            document.getElementById('previewBtnGroup').removeChild(document.getElementById('previewButtonRow' + i));
            console.log('Remove Success!! index:' + btnIndex);
            btnSize--;
        };

        var colorInput = document.createElement('input');
        colorInput.className = 'color_input_text';
        colorInput.id = 'colorInputBtn' + btnIndex;
        colorInput.placeholder = '按鈕#RRGGBB';
        colorInput.maxLength = 7;
        colorInput.type = 'text';
        colorInput.value = btnBackColor;

        var colorPickerDiv = document.createElement('div');
        colorPickerDiv.id = 'colorPickerDiv' + btnIndex;

        var colorDiv = document.createElement('div');
        colorDiv.className = 'warp10';
        colorDiv.appendChild(colorInput);
        colorDiv.appendChild(colorPickerDiv);




        var removeButtonBtnDiv = document.createElement('div');
        removeButtonBtnDiv.className = 'warp10';

        removeButtonBtnDiv.appendChild(removeButton);

        /* Row */
        var rowDiv = document.createElement('div');
        rowDiv.id = 'row' + btnIndex;
        rowDiv.appendChild(btnNameDiv1);
        rowDiv.appendChild(btnUrlDiv1);
        rowDiv.appendChild(colorDiv);
        rowDiv.appendChild(removeButtonBtnDiv);

        console.log(rowDiv);

        var btnGroup = document.getElementById('btnGroup');
        btnGroup.appendChild(rowDiv);

        //Preview Area
        var previewButton = document.createElement('button');
        previewButton.textContent = btnText || config.body.button.buttonText;
        previewButton.className = 'msgBtn';
        previewButton.style.background = btnBackColor || config.body.button.buttonColor;
        previewButton.id = 'previewBtn' + btnIndex;

        var previewButtonDiv = document.createElement('div');
        previewButtonDiv.id = 'previewButtonRow' + btnIndex;
        previewButtonDiv.className = 'msgBtn-area';

        previewButtonDiv.appendChild(previewButton);

        var previewBtnGroup = document.getElementById('previewBtnGroup');
        previewBtnGroup.appendChild(previewButtonDiv);


        triggerColorPicker(colorInput.id, colorPickerDiv.id, previewButton.id, 'background');

        btnSize++;
    };
    //--------------------- Add Button ------------------------

    //--------------------- Generate Pop Template Json -----------------------
    var generateTemplateJson = function () {
        var templateId = document.getElementById('quickViewTemplate').textContent || null;
        var headerBackground = document.getElementById('headerTitleBackgroundColor').value === '' ? config.header.background : document.getElementById('headerTitleBackgroundColor').value;
        var headerTextSize = 'xl';
        var headerTextColor = '#000000';
        var headerTextWeight = document.getElementById('titleTextBoldBtn').value === 'true' ? 'bold' : 'regular';
        var headerTextStyle = document.getElementById('titleTextItalicBtn').value === 'true' ? 'italic' : 'normal';
        var headerTextDecoration = document.getElementById('titleTextUnderLineBtn').value === 'true' ? 'underline' : 'none';
        var headerText = document.getElementById('previewTitle').textContent === '' ? config.header.text : document.getElementById('previewTitle').textContent;

        var heroBackground = document.getElementById('heroBackgroundColorText').value === '' ? config.hero.background : document.getElementById('heroBackgroundColorText').value;
        var heroTextSize = 'lg';
        var heroTextColor = document.getElementById('heroTextColorText').value === '' ? config.hero.textColor : document.getElementById('heroTextColorText').value;

        var heroTextWeight = document.getElementById('heroTextBoldBtn').value === 'true' ? 'bold' : 'regular';
        var heroTextStyle = document.getElementById('heroTextItalicBtn').value === 'true' ? 'italic' : 'normal';
        var heroTextDecoration = document.getElementById('heroTextUnderLineBtn').value === 'true' ? 'underline' : 'none';
        var heroText = document.getElementById('flexContent').value === '' ? config.hero.text : document.getElementById('flexContent').value;

        var bodyDescTextSize = config.body.description.textSize;
        var bodyDescTextColor = config.body.description.textColor;
        var bodyDescTextWeight = config.body.description.textWeight;
        var bodyDescTextStyle = config.body.description.textStyle;
        var bodyDescTextDecoration = config.body.description.textDecoration;
        var bodyDescText = config.body.description.text;

        var bodyBackground = '#FFFFFF';


        var footerLinkText = config.footer.linkText;
        var footerLinkUrl = config.footer.linkUrl;

        var flexMessageObj = {
            type: 'flex',
            pathwayCode: document.getElementById('pathwayList').options[document.getElementById('pathwayList').selectedIndex].value,
            pathwayName: document.getElementById('pathwayList').options[document.getElementById('pathwayList').selectedIndex].textContent,
            template: templateId,

            header: {
                headerBackground: headerBackground,
                headerTextSize: headerTextSize,
                headerTextColor: headerTextColor,
                headerTextWeight: headerTextWeight,
                headerTextStyle: headerTextStyle,
                headerTextDecoration: headerTextDecoration,
                headerText: headerText
            },
            hero: {
                heroBackground: heroBackground,
                heroTextSize: heroTextSize,
                heroTextColor: heroTextColor,
                heroTextWeight: heroTextWeight,
                heroTextStyle: heroTextStyle,
                heroTextDecoration: heroTextDecoration,
                heroText: heroText
            },

            body: {
                bodyDescTextSize: bodyDescTextSize,
                bodyDescTextColor: bodyDescTextColor,
                bodyDescTextWeight: bodyDescTextWeight,
                bodyDescTextStyle: bodyDescTextStyle,
                bodyDescTextDecoration: bodyDescTextDecoration,
                bodyDescText: bodyDescText,
                bodyBackground: bodyBackground
            },
            footer: {
                footerLinkText: footerLinkText,
                footerLinkUrl: footerLinkUrl
            }
        }

        flexMessageObj.button = generateButtonObject();
        return flexMessageObj;
    };
    //--------------------- Generate Pop Select List -----------------------

    //--------------------- Generate Pop Template Button Json -----------------------
    var generateButtonObject = function () {
        var btnArray = [];
        var rowArray = document.getElementById('btnGroup').children;
        var btnLength = rowArray.length;
        for (var i = 0; i < btnLength; i++) {
            var bodyButtonText = rowArray[i].children[0].children[0].children[0].value;
            var bodyLinkUrl = rowArray[i].children[1].children[0].children[0].value;
            var bodyButtonColor = rowArray[i].children[2].children[0].value;
            var newBtn = {
                id: i,
                label: bodyButtonText,
                uri: bodyLinkUrl,
                color: bodyButtonColor
            };
            btnArray.push(newBtn);
        }
        return btnArray;
    };
    //--------------------- Generate Pop Template Button Json -----------------------

    var validateRequireDate = function(){
        var a1 = document.getElementById('account').value;
        var a2 = document.getElementById('accountAttribute').value;
        var a3 = document.getElementById('sourceSystem').value;
        var a4 = document.getElementById('employeeId').value;
        var a5 = document.getElementById('departmentId').value;
        var a6 = document.getElementById('divisionName').value;
        var a7 = document.getElementById('departmentName').value;
        var a8 = document.getElementById('groupName').value;
        var a9 = document.getElementById('PccCode').value;
        return a1 !== null && a1 !== ''
            && a2 !== null && a2 !== ''
            && a3 !== null && a3 !== ''
            && a4 !== null && a4 !== ''
            && a5 !== null && a5 !== ''
            && a6 !== null && a6 !== ''
            && a7 !== null && a7 !== ''
            && a8 !== null && a8 !== ''
            && a9 !== null && a9 !== '';
    }

    //------------------- Event Listener ----------------------

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
//            $('#account').val(response.account);
            $('#employeeId').val(response.employeeId);
            $('#departmentId').val(response.departmentId);
            $('#divisionName').val(response.divisionName);
            $('#departmentName').val(response.departmentName);
            $('#groupName').val(response.groupName);
            $('#PccCode').val(response.pccCode);
//            $('#accountAttribute').val('批次');
        }).fail(function (response) {
            console.info(response);
            $.FailResponse(response);
        }).done(function () {
            console.info('done!!');
        });
    });

    /* Cancel */
    $('input[name="cancel"]').click(function () {
        var r = confirm("請確認是否取消");
        if (r) {
            console.log('Confirm True!!');
        } else {
            return;
        }
        window.location.replace(bcs.bcsContextPath + '/pnpAdmin/pnpUnicaAccountListPage');
    });

    $('#popupEditPage').click(function () {
        $('.LyMain').block($.BCS.blockMsgRead);
        $('.LyMain').unblock();
        $('#dialog-modal').dialog({
            width: 1433,
            height: 768,
            modal: true,
            position: { my: 'center', at: 'center', of: window }
        });
        $('#dialog-modal').show();

        if (popInstance === null) {
            loadPopupConfig();
        }
    });

    var saveBeforeTemplateJson;

    /* 主頁面儲存按鈕 */
    $('#saveAccountModelBtn').click(function () {
        var validIsPass = validateRequireDate();
        if(validIsPass) {
            if (saveBeforeTemplateJson === undefined || saveBeforeTemplateJson === null || saveBeforeTemplateJson === '' || saveBeforeTemplateJson === JSON.stringify({})) {
                loadPopupConfig();
            }
            console.log('saveBeforeTemplateJson:' + JSON.stringify(saveBeforeTemplateJson));

            var maintainAccountData = {};
            maintainAccountData.account = $('#account').val();
            maintainAccountData.accountAttribute = $('#accountAttribute').val();
            maintainAccountData.sourceSystem = $('#sourceSystem').val();
            maintainAccountData.employeeId = $('#employeeId').val();
            maintainAccountData.departmentId = $('#departmentId').val();
            maintainAccountData.divisionName = $('#divisionName').val();
            maintainAccountData.departmentName = $('#departmentName').val();
            maintainAccountData.groupName = $('#groupName').val();
            maintainAccountData.pccCode = $('#PccCode').val();
            maintainAccountData.accountType = 'Unica';
            maintainAccountData.accountClass = $('.accountClass')[0].checked ? 'O' : 'M';
            maintainAccountData.status = $('.status')[0].checked;
            maintainAccountData.template = document.getElementById('quickViewTemplate').textContent;
            maintainAccountData.pnpContent = document.getElementById('quickViewPNPContent').textContent;
            maintainAccountData.id = pnpMaintainAccountModelId;

            var quickViewPathWay = document.getElementById('quickViewPathWay').textContent;
            var quickViewPathWayCode = pathWayNameToCode(quickViewPathWay);
            maintainAccountData.pathway = quickViewPathWayCode;

            var sendData = {
                maintainAccount: JSON.stringify(maintainAccountData),
                flexTemplate: JSON.stringify(saveBeforeTemplateJson)
            };

            console.log(JSON.stringify(sendData, null, 4));

            $.ajax({
                type: 'POST',
                url: bcs.bcsContextPath + '/pnpAdmin/createPNPMaintainAccount',
                cache: false,
                contentType: 'application/json',
                processData: false,
                data: JSON.stringify(sendData)
            }).success(function (response) {
                console.info(response);
                alert('儲存成功');
                window.location.replace('pnpUnicaAccountListPage');
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
        }
    });
    //------------------- Event Listener ----------------------



    //------------------- POP Event Listener ----------------------
    var visible = false;
    $('#showJson').click(function () {
        var templateJson = generateTemplateJson();
        document.getElementById('console').textContent = JSON.stringify(templateJson, null, 4);
        visible = !visible;
        document.getElementById('showJson').textContent = visible ? '隱藏JSON' : '顯示JSON';
        document.getElementById('console').style.display = visible ? 'inline-block' : 'none';
    });

    /* 監聽標題內容即時反映於預覽區 */
    $('#flexTitle').keyup(function () {
        var value = document.getElementById('flexTitle').value;
        if (value === undefined || value === null || value === '') {
            value = config.header.text;
        }
        document.getElementById('previewTitle').textContent = value;
    });

    /* 監聽訊息內容即時反映於預覽區 */
    $('#flexContent').keyup(function () {
        var value = document.getElementById('flexContent').value;
        if (value === undefined || value === null || value === '') {
            value = config.hero.text;
        }
        document.getElementById('previewContent').textContent = value;
    });

    /* Add Button */
    $('#addButtonBtn').click(function () {
        addDefaultButton();
    });

    $('#saveBtn').click(function (event) {
        validButton();
        saveBeforeTemplateJson = generateTemplateJson();
        console.log(JSON.stringify(saveBeforeTemplateJson));
        document.getElementById('quickViewPathWay').textContent = saveBeforeTemplateJson.pathwayName;
        document.getElementById('quickViewTemplate').textContent = saveBeforeTemplateJson.template;
        document.getElementById('quickViewPNPContent').textContent = saveBeforeTemplateJson.hero.heroText;
        $('#dialog-modal').dialog("close");
    });

    var validButton = function(){
        var btnGroup = document.getElementById('btnGroup');
        var btnCount = btnGroup.childElementCount;
        console.log('Button Count : ' + btnCount);
        if (btnCount > 0) {
            for (var i = 0; i < btnCount; i++) {
                var name = btnGroup.children[row].children[1].children[0].children[0].value;
                var url = btnGroup.children[row].children[2].children[0].children[0].value;
                var color = btnGroup.children[row].children[2].children[0].value
                console.log('Name: ' + name + ',Url: ' + url + ',Color: ' + color);
                if (name.trim() === '' || url.trim() === '' || color === '') {
                    btnGroup.removeChild(btnGroup.children[row]);
                }
            }
        }
    }


    //---------------------Text Button Press--------------------------
    var triggerBtnPressed = function (btnElementId, btnStyleName, previewElementId, styleName, styleValue) {
        $('#' + btnElementId).click(function () {
            var isPressed = document.getElementById(btnElementId).className.indexOf('btn-style-pressed');
            if (isPressed > 0) {
                document.getElementById(btnElementId).className = 'btn btn-style ' + btnStyleName;
                document.getElementById(btnElementId).value = null;
                document.getElementById(previewElementId).style[styleName] = '';
            } else {
                document.getElementById(btnElementId).className = 'btn btn-style-pressed ' + btnStyleName;
                document.getElementById(btnElementId).value = 'true';
                document.getElementById(previewElementId).style[styleName] = styleValue;
            }
        });
    };

    /* 監聽字體樣式選取即時反映於預覽區 */
    triggerBtnPressed('titleTextBoldBtn', 'btn-bold', 'previewTitle', 'fontWeight', 'bold');
    triggerBtnPressed('titleTextItalicBtn', 'btn-italic', 'previewTitle', 'fontStyle', 'italic');
    triggerBtnPressed('titleTextUnderLineBtn', 'btn-underline', 'previewTitle', 'textDecoration', 'underline');

    triggerBtnPressed('heroTextBoldBtn', 'btn-bold', 'previewContent', 'fontWeight', 'bold');
    triggerBtnPressed('heroTextItalicBtn', 'btn-italic', 'previewContent', 'fontStyle', 'italic');
    triggerBtnPressed('heroTextUnderLineBtn', 'btn-underline', 'previewContent', 'textDecoration', 'underline');
    //---------------------Text Button Press--------------------------

    //---------------------Color Picker--------------------------
    var triggerColorPicker = function (elementId, pickerId, previewElementId, styleName) {
        // var colorPicker;
        // var colorPickerCnt = 0;
        // $('#' + elementId).focus(function () {
        //     if (colorPickerCnt <= 0) {
        //         console.log(JSON.stringify(iro));
        //         colorPicker = iro.ColorPicker('#' + pickerId, {
        //             width: 200,
        //             color: 'rgb(255, 0, 0)',
        //             borderWidth: 0,
        //             borderColor: '#000',
        //             padding: 10
        //         });
        //         colorPicker.on(["color:init", "color:change"], function (color) {
        //             document.getElementById(elementId).value = color.hexString.toUpperCase();
        //             document.getElementById(previewElementId).style[styleName] = color.hexString.toUpperCase();
        //         });
        //     }
        //     document.getElementById(pickerId).style.display = 'block';
        //     colorPickerCnt++;
        // });

//        $('#' + elementId).blur(function () {
//            document.getElementById(pickerId).style.display = 'none';
//        });

        $('#' + elementId).keyup(function () {
            var value = document.getElementById(elementId).value;
            if (value === null || value === undefined || value === '') {
                var defValue;
                if (styleName === 'background') {
                    if (elementId === 'headerTitleBackgroundColor') {
                        defValue = config.header.background;
                    } else {
                        defValue = config.hero.background;
                    }
                    document.getElementById(previewElementId).style[styleName] = defValue;
                }
                if (styleName === 'color') {
                    defValue = '#000000';
                    document.getElementById(previewElementId).style[styleName] = defValue;
                }
                //Comment
                // colorPicker.color.hexString = defValue;
            } else {
                var hexPattern = /^#[0-9a-fA-F]{6}$/;
                if (hexPattern.test(value)) {
                    // colorPicker.color.hexString = value;
                    if (styleName === 'background') {
                        document.getElementById(previewElementId).style[styleName] = value;
                    }
                    if (styleName === 'color') {
                        document.getElementById(previewElementId).style[styleName] = value;
                    }
                }
            }
        });
    };

    /* 監聽顏色選取即時反映於預覽區 */
    triggerColorPicker('headerTitleBackgroundColor', 'colorPicker1', 'msgTitle-area', 'background');
    triggerColorPicker('heroTextColorText', 'colorPicker2', 'previewContent', 'color');
    triggerColorPicker('heroBackgroundColorText', 'colorPicker3', 'mainMsg', 'background');
    //---------------------Color Picker--------------------------
    //------------------- Popup Event Listener ----------------------


    // ---- Initialize Page & Load Data ----
    initPage();
});