var Security;
if (Security && (typeof Security != "object" || Security.NAME))
    throw new Error("Namespace 'Security' already exists");
document.write("<style> @media print { table,tr,td { background-color:transparent; }.noprint { display: none; }}</style>");
// IE11改版 20160506
// IE11改版 20160621_v2.0
Security = {};
Security.NAME = "Security";
//Security.VERSION = "2.0.1";
//Security.CABVERSION = "2.0.0";
Security.VERSION = "3.0.1";
Security.CABVERSION = "3.0.1";
//Security.CODEBASE = "http://intranetweb/abub07/DownLoad/TaiShinSecurity2.CAB#version=2,0,0,0";
Security.HELP_URL = "http://www.ecis.com.tw/images/taishin_01.jpg";//"http://intranetweb/abub07/watermark/help2.htm";
Security.ENABLE_LOAD = true;
Security.DISABLED_CTRL_KEYS = "67,80,88";//Ctrl + C:67 ,Ctrl + P:80,Ctrl + X:88
Security.DISABLED_CTRL = true;
Security.DISABLED_CTRL_DES = "禁止Ctrl + ";

Security.DISABLED_SELECT = true;
Security.DISABLED_DES = "禁止選取頁面!!";

Security.DISABLED_DRAG = true;
Security.DISABLED_DRAG_DES = "禁止拖曳頁面!!";

Security.DISABLED_RIGHT_CLICK = true;
Security.DISABLED_RIGHT_CLICK_DES = "禁止使用滑鼠右鍵!!";

Security.FORCE_CLEARDATA = false;
Security.DISABLED_PRT_SCR = true;

Security.DISABLED_PRT_SCR_DES = "禁止複製!!";


Security.DISABLED_FILE_PRT = true;
Security.DISABLED_FILE_PRT_DES = "禁止使用檔案列印!!";

Security.PAPER_H = 200; //列印高度
Security.PAPER_W = 320; //列印寬度
Security.DocumentHeight = 0;//文件高度
Security.DocumentWidth = 0;//文件寬度

Security.SHOW_WATER_RATE = 1;
Security.SHOW_WATER_MARK = true; //是否顯視浮水印       show_watermark
Security.ACTIVITE_LOG = true; //是否紀錄   LOG
Security.SHOW_WATER_LOGO = "";//預設為銀行LOGO 若為 h 是金控LOGO      TSBWM_LogoPattern
//Security.SHOW_WATER_MARK_URL = "http://watermark/TSBWM/TSBWM.aspx";
//Security.LOG_URL = "http://watermark/TSBWM/TSBPRTSCRLOG.aspx";
Security.SHOW_WATER_MARK_URL = "http://watermark-t/TSBWM/TSBWM.aspx";
Security.LOG_URL = "http://watermark-t/TSBWM/TSBPRTSCRLOG.aspx";
Security.SHOW_WATER_TYPR = "SIG_HTM_IMG";

Security.ControlPanel_Pos = "50%"; //ControlPanel位置

Security.EMPID = "";           //員編---tsbsec_emp_id
Security.EMPNM = "";           //姓名---tsbsec_emp_name
Security.SYSDT = "";           //日期---tsbsec_date_stamp

Security.SERVER_IP = "";        //---var tsbsec_serverip = ""
Security.HTTPURL = "";         //---var tsbsec_httpurl = "" 
Security.CLINET_IP = "";        //---var tsbsec_clientip = ""

Security.PrintLog = "0";
Security.SaveAsLog = "1";
Security.ExportLog = "2";
Security.DownLoadLog = "3";
Security.PrtScrLog = "4";
Security.PrtScrCount = 0;

//@@
//Moved


Security.ProtectPage = function () { }
Security.Common = function () { }
Security.Common.DocumentHeight = function () {
    if (document.documentElement && document.documentElement.clientWidth) {//DOCTYPE
        return parseInt(document.documentElement.scrollHeight);//視窗高
    }
    else {
        return parseInt(document.body.scrollHeight);//視窗高
    }
}
Security.Common.DocumentWidth = function () {
    if (document.documentElement && document.documentElement.clientWidth) {//DOCTYPE
        return parseInt(document.documentElement.scrollWidth);//視窗寬  
    }
    else {
        return parseInt(document.body.scrollWidth);//視窗寬     	
    }
}
Security.HTTP = function () { }
Security.HTTP._factories = [
    function () { return new XMLHttpRequest(); },
    function () { return new ActiveXObject("Msxml2.XMLHTTP"); },
    function () { return new ActiveXObject("Microsoft.XMLHTTP"); }
];
Security.HTTP._factory = null;
Security.HTTP.newRequest = function () {
    if (Security.HTTP._factory != null) return Security.HTTP._factory();
    for (var i = 0; i < Security.HTTP._factories.length; i++) {
        try {
            var factory = Security.HTTP._factories[i];
            var request = factory();
            if (request != null) {
                Security.HTTP._factory = factory;
                return request;
            }
        }
        catch (e) {
            continue;
        }
    }
    Security.HTTP._factory = function () {
        throw new Error("XMLHttpRequest not supported");
    }
    Security.HTTP._factory(); // Throw an error
}

Security.HTTP.Get = function (url) {
    try {
        var request = Security.HTTP.newRequest();
        request.open("GET", url);
        request.send(null);
    }
    catch (e) {
        if (Security.ACTIVITE_LOG) {
            return "非 近端內部網路,造成 Ajax 安全性問題,無法紀錄log";
        }
        return "";
    }
    return "";
}
Security.HTTP.PostXml = function (url, params) {
    try {
        var request = Security.HTTP.newRequest();
        request.open("POST", url);
        request.setRequestHeader("Content-Type", "text/xml");
        request.send(params);
    }
    catch (e) {
        if (Security.ACTIVITE_LOG) {
            return "非 近端內部網路,造成 Ajax 安全性問題,無法紀錄log";
        }
        return "";
    }
    return "";
}
Security.ProtectPage.defaultPostXml = function (LogType) {
    var currNode;
    var postString = "<METADATA>";
    postString += "<EMPID><![CDATA[]]></EMPID>";
    postString += "<ACTTYPE><![CDATA[]]></ACTTYPE>";
    postString += "<CLIENTIP><![CDATA[]]></CLIENTIP>";
    postString += "<SERVERIP><![CDATA[]]></SERVERIP>";
    postString += "<URL><![CDATA[]]></URL>";
    postString += "<DateStamp><![CDATA[]]></DateStamp>";
    postString += "<Html><![CDATA[]]></Html></METADATA>";
    var xmlDoc = new ActiveXObject("MSXML.DOMDocument");
    xmlDoc.async = false;
    xmlDoc.loadXML(postString);
    xmlDoc.setProperty("SelectionLanguage", "XPath");
    currNode = xmlDoc.selectSingleNode("//METADATA/EMPID");
    currNode.childNodes[0].text = Security.EMPID;
    currNode = xmlDoc.selectSingleNode("//METADATA/ACTTYPE");
    currNode.childNodes[0].text = LogType;
    currNode = xmlDoc.selectSingleNode("//METADATA/CLIENTIP");
    currNode.childNodes[0].text = Security.CLINET_IP;
    currNode = xmlDoc.selectSingleNode("//METADATA/SERVERIP");
    currNode.childNodes[0].text = Security.SERVER_IP;
    currNode = xmlDoc.selectSingleNode("//METADATA/URL");
    currNode.childNodes[0].text = Security.HTTPURL;
    currNode = xmlDoc.selectSingleNode("//METADATA/DateStamp");
    currNode.childNodes[0].text = Security.SYSDT;
    currNode = xmlDoc.selectSingleNode("//METADATA/Html");
    currNode.childNodes[0].text = document.documentElement.innerHTML.replace(/]]>/ig, "");

    return "<?xml version=\"1.0\" encoding=\"" + document.charset + "\" ?>" + xmlDoc.xml;
}
Security.ProtectPage.IniProtectPage = function () {	  	
    var ErrMsg = "";
	//alert(document.readyState);
    if (document.readyState != "complete") return;

    //Added by Patrick
    //alert(document.documentElement.innerHTML);
    if(document.body)
    {
    	  try{
        var oObject = document.createElement("OBJECT");
        oObject.id = "objPrtScr";        
        oObject.classid = "CLSID:9561E076-43B1-43e4-9461-F3794095212C";                
        //oObject.codeBase = Security.CODEBASE;
        document.body.appendChild(oObject);           
      }
       catch (ex) {
       	document.body.appendChild(oObject);
       }	                
    }
    

    try {
        if (document.getElementsByTagName("FRAME").length > 0) return;
    }
    catch (ex) {

    }


    if (!Security.ProtectPage.IsIE()) {
        alert('請使用 IE5 以上版本!!');
        window.location = 'about:blank';
        return;
    }

    if (document.getElementById("ProtectPage") != null) {
        var oNode = null;
        while (!(ProtectPage.XMLDocument.readyState == 1)) break;
        try {
            oNode = ProtectPage.XMLDocument.selectSingleNode("METADATA/CTRL_KEYS");
            if (oNode != null) {
                Security.DISABLED_CTRL_KEYS = oNode.text;
                Security.DISABLED_CTRL = true;
            } else { Security.DISABLED_CTRL = false; }
        } catch (e) {
            oNode = null;
            Security.ProtectPage.DisabledCtrlKeys();
        }
        try {
            oNode = ProtectPage.XMLDocument.selectSingleNode("METADATA/RIGHT_CLICK");
            if (oNode.text == "1") { Security.DISABLED_RIGHT_CLICK = true; }
            else { Security.DISABLED_RIGHT_CLICK = false; }
        } catch (e) {
            oNode = null;
            Security.DISABLED_RIGHT_CLICK = true;
        }
        try {
            oNode = ProtectPage.XMLDocument.selectSingleNode("METADATA/DRAG");
            if (oNode.text == "1") { Security.DISABLED_DRAG = true; }
            else { Security.DISABLED_DRAG = false; }
        } catch (e) {
            oNode = null;
            Security.DISABLED_DRAG = false;
        }
        try {
            oNode = ProtectPage.XMLDocument.selectSingleNode("METADATA/SELECT");
            if (oNode.text == "1") { Security.DISABLED_SELECT = true; }
            else { Security.DISABLED_SELECT = false; }
        } catch (e) {
            oNode = null;
            Security.DISABLED_SELECT = true;
        }
        
     
        try {
            oNode = ProtectPage.XMLDocument.selectSingleNode("METADATA/PRT_SCR");
            if (oNode.text == "1") { Security.DISABLED_PRT_SCR = true; }
            else { Security.DISABLED_PRT_SCR = false; }
        } catch (e) {
            oNode = null;
            Security.DISABLED_PRT_SCR = true;
        }
  
        
        try {
            oNode = ProtectPage.XMLDocument.selectSingleNode("METADATA/FILE_PRT");
            if (oNode.text == "1") {
                Security.DISABLED_FILE_PRT = true;
            } else {
                Security.DISABLED_FILE_PRT = false;
            }
        } catch (e) {
            oNode = null;
            Security.ProtectPage.DisabledFilePrt();
        }
    }

    //=========Adapter================
    if (typeof tsbsec_emp_id == "string") {
        Security.EMPID = (tsbsec_emp_id == "") ? Security.EMPID : tsbsec_emp_id;
    }
    if (typeof tsbsec_emp_name == "string") {
        Security.EMPNM = (tsbsec_emp_name == "") ? Security.EMPNM : tsbsec_emp_name;
    }
    if (typeof tsbsec_date_stamp == "string") {
        Security.SYSDT = (tsbsec_date_stamp == "") ? Security.SYSDT : tsbsec_date_stamp;
    }
    if (typeof tsbsec_httpurl == "string") {
        Security.HTTPURL = (tsbsec_httpurl == "") ? Security.HTTPURL : tsbsec_httpurl;
    }
    if (typeof tsbsec_clientip == "string") {
        Security.CLINET_IP = (tsbsec_clientip == "") ? Security.CLINET_IP : tsbsec_clientip;
    }
    if (typeof tsbsec_serverip == "string") {
        Security.SERVER_IP = (tsbsec_serverip == "") ? Security.SERVER_IP : tsbsec_serverip;
    }
    if (typeof TSBWM_LogoPattern == "string") {
        Security.SHOW_WATER_LOGO = (TSBWM_LogoPattern == "") ? Security.SHOW_WATER_LOGO : TSBWM_LogoPattern;
    }
    if (typeof show_watermark == "boolean") {
        Security.SHOW_WATER_MARK = show_watermark;
    }
    if (typeof force_ScreenProtect == "boolean") {
        Security.FORCE_CLEARDATA = force_ScreenProtect;
    }

    //================================
    ErrMsg = Security.ProtectPage.AddTsbSecurity();
    Security.ProtectPage.DisabledCtrlKeys();
    Security.ProtectPage.DisabledRightClick();
    Security.ProtectPage.DisabledDrag();
    Security.ProtectPage.DisabledSelect();
    Security.ProtectPage.DisabledPrtScr();
    Security.ProtectPage.CleanClipboardData();
    Security.ProtectPage.DisabledFilePrt();
    //@@ Removed
    //Security.ProtectPage.ControlPanel(ErrMsg);
    //@@ Removed
    //Security.ProtectPage.Timer2 = setInterval('Security.ProtectPage.AutoCheck()', 1000);


    if (document.documentElement && document.documentElement.clientWidth) {//DOCTYPE
        //IE6的 select 會異常
        //document.attachEvent("onfocusin", Security.ProtectPage.DisplayHtm);
        //document.attachEvent("onfocusout", Security.ProtectPage.HiddenHtm);
    }
        
    //一定要先執行,才會取的目前文件長寬	
    Security.DocumentHeight = Security.Common.DocumentHeight();
    Security.DocumentWidth = Security.Common.DocumentWidth();
		
    if (Security.SHOW_WATER_MARK) {
		//alert(document.namespaces);
        //document.namespaces.add("v", "urn:schemas-microsoft-com:vml");
        //document.createStyleSheet().addRule("v\\:*", "behavior: url(#default#VML);");
        //document.write("<style>@media print { table,tr,td { background-color:transparent; }}</style>");        
        switch (Security.SHOW_WATER_TYPR) {
            case "HTM_IMG":
                if (!Security.WATER_MARK_H) {
                    Security.WATER_MARK_H = 200; //浮水印高
                    Security.WATER_MARK_W = 320; //浮水印寬
                }
                Security.ProtectPage.MakeWaterMark = Security.ProtectPage.MakeWaterMark_HTM_IMG;
                Security.ProtectPage.AddWaterMark = Security.ProtectPage.AddWaterMarkMulti;
                break;
            case "VML_IMG":
                if (!Security.WATER_MARK_H) {
                    Security.WATER_MARK_H = 200; //浮水印高
                    Security.WATER_MARK_W = 320; //浮水印寬
                }
                Security.ProtectPage.MakeWaterMark = Security.ProtectPage.MakeWaterMark_VML_IMG;
                Security.ProtectPage.AddWaterMark = Security.ProtectPage.AddWaterMarkMulti;
                break;
            case "VML_TEXT":
                if (!Security.WATER_MARK_H) {
                    Security.WATER_MARK_H = 200; //浮水印高
                    Security.WATER_MARK_W = 320; //浮水印寬
                }
                Security.ProtectPage.MakeWaterMark = Security.ProtectPage.MakeWaterMark_VML_TEXT;
                Security.ProtectPage.AddWaterMark = Security.ProtectPage.AddWaterMarkSingle;
                break;
            case "SIG_HTM_IMG":
                if (!Security.WATER_MARK_H) {
                    Security.WATER_MARK_H = 531; //浮水印高
                    Security.WATER_MARK_W = 567; //浮水印寬
                }
                Security.ProtectPage.MakeWaterMark = Security.ProtectPage.MakeWaterMark_SIG_HTM_IMG;
                Security.ProtectPage.AddWaterMark = Security.ProtectPage.AddWaterMarkMulti;
                //Security.ProtectPage.AddWaterMark = Security.ProtectPage.AddWaterMarkSingle;
                break;
            case "SIG_HTM_IMG_PRT":
                if (!Security.WATER_MARK_H) {
                    Security.WATER_MARK_H = Security.Common.DocumentHeight() - 100; //浮水印高
                    Security.WATER_MARK_W = 640; //浮水印寬
                }
                Security.ProtectPage.MakeWaterMark = Security.ProtectPage.MakeWaterMark_SIG_HTM_IMG;
                Security.ProtectPage.AddWaterMark = Security.ProtectPage.AddWaterMarkSingle;
                break;
            case "SIG_HTM_IMG_FLOAT":
                if (!Security.WATER_MARK_H) {
                    Security.WATER_MARK_H = 700; //浮水印高
                    Security.WATER_MARK_W = 640; //浮水印寬
                }
                Security.ProtectPage.MakeWaterMark = Security.ProtectPage.MakeWaterMark_SIG_HTM_IMG;
                Security.ProtectPage.AddWaterMark = Security.ProtectPage.AddWaterMarkSingleFloat;
                break;
            default:
                Security.ProtectPage.MakeWaterMark = Security.ProtectPage.MakeWaterMark_VML_IMG;
                Security.ProtectPage.AddWaterMark = Security.ProtectPage.AddWaterMarkMulti;
                break;
        }

        Security.ProtectPage.ShowWaterMark(); //顯示圖片
        //if(Security.SHOW_WATER_TYPR != "SIG_HTM_IMG_PRT")
        //{
        //一律加上文字顯示模式,避免圖片被拿掉
        //document.body.appendChild(Security.ProtectPage.AddBackGroundColor("black"));
        Security.ProtectPage.MakeWaterMark = Security.ProtectPage.MakeWaterMark_TEXT;
        Security.ProtectPage.ShowWaterMark(); //顯示文字
        //}
        //Security.ProtectPage.ControlPanel();

    }    
    
    //@@ Addd
    Security.ProtectPage.ControlPanel(ErrMsg);
    //@@ Added
    Security.ProtectPage.Timer2 = setInterval('Security.ProtectPage.AutoCheck()', 1000);
}


Security.ProtectPage.IniProtectPage2 = function () {	  
    
}

Security.ProtectPage.DisplayHtm = function () {
    document.body.style.display = "";
}
Security.ProtectPage.HiddenHtm = function () {
    //document.body.style.display = "none";
}
Security.ProtectPage.AutoCheck = function () {	  
	  if(!objPrtScr)
	  {
	      return;
	  }
	  
    Security.ProtectPage.DisabledPrtScr();
    if (parseInt(objPrtScr.PressPrtScr) == 1) {
        Security.PrtScrCount = Security.PrtScrCount + 1;
        if (Security.PrtScrCount == 3) {
            Security.PrtScrCount = 0;
            Security.ProtectPage.PrtScrLog();
        }
        objPrtScr.ClearPressPrtScr();
    }
}
Security.ProtectPage.AddTsbSecurity = function () {
	  return;
}
Security.ProtectPage.EnvErrChk = function (oDiv) {
    if (oDiv.style.backgroundColor != "red") return false;
    oDiv.style.filter = "Alpha(opacity = 100)";
    oDiv.style.top = 0;
    oDiv.style.left = 0;
    oDiv.style.width = "2000px";
    oDiv.style.height = "20000px";
    return true;
}
Security.ProtectPage.ControlPanel = function (ErrMsg) {		
	  if(!objPrtScr)
	  {
	      return;
	  }
	  
    var oDiv = document.createElement("DIV");
    oDiv.id = "TsbSecurityControlPanel";
    oDiv.className = "noprint";

    oDiv.onclick = function () {    	  
    	  var makehtm = "";
        if (oDiv.style.width != "20px") {
            if (Security.ProtectPage.EnvErrChk(oDiv)) return;
            oDiv.style.width = "20px";
            oDiv.style.height = "20px";
            oDiv.style.top = parseInt(document.documentElement.scrollTop) + parseInt(document.all.TsbSecurityControlPanel.style.top);
            oDiv.style.left = "95%";
            oDiv.innerHTML = "";
            oDiv.style.filter = "Alpha(opacity = 30)";
            oDiv.innerHTML = makehtm;
            return;
        }

        oDiv.style.width = "220px";
        oDiv.style.height = "220px";
        //oDiv.style.top = Security.ControlPanel_Pos;
        oDiv.style.top = parseInt(document.documentElement.scrollTop) + parseInt(document.all.TsbSecurityControlPanel.style.top);
        oDiv.style.left = "85%";
        oDiv.style.backgroundColor = "green";
        oDiv.style.filter = "Alpha(opacity = 70)";
        makehtm += "模組版本：" + Security.VERSION + "_v2.0";
        if (Security.SHOW_WATER_MARK_URL.indexOf('watermark-t') != -1) makehtm += " 測試環境";
        makehtm += "</br>CAB版本：";        
        try {        	          	  
            if (objPrtScr.Ver) {
                makehtm += objPrtScr.Ver;
                if (Security.CABVERSION != objPrtScr.Ver) {
                    makehtm += "(最新版本為:" + Security.CABVERSION + ")";
                    oDiv.style.backgroundColor = "yellow";
                }
                if (Security.ProtectPage.Timer && !Security.FORCE_CLEARDATA) {
                    makehtm += "</br><strong>KBHook2.dll不存在!!</strong>";
                    makehtm += "</br><strong>因系統配置不符規定，複製功能已鎖定</strong>";
                    makehtm += "</br><strong>請洽資訊處連管人員(02-5589-3000)</strong>";
                    oDiv.style.backgroundColor = "red";
                }
            } else {
                makehtm += "<strong>TaiShinSecurity3.dll不存在!!" + ErrMsg + "</strong>";
                makehtm += "</br><strong>因系統配置不符規定，複製功能已鎖定</strong>";
                makehtm += "</br><strong>請洽資訊處連管人員(02-5589-3000)</strong>";
                oDiv.style.backgroundColor = "red";
            }
        }
        catch (e) {
            makehtm += "TaiShinSecurity3.dll未安裝!!" + ErrMsg;
            oDiv.style.backgroundColor = "red";
        }
        //makehtm += "</br>CodeBase：" + Security.CODEBASE;

        makehtm += "<hr>列印：";
        if (Security.DISABLED_FILE_PRT) {
            makehtm += "否";
        } else {
            makehtm += "是";
        }
        makehtm += "</br>拖曳：";
        if (Security.DISABLED_DRAG) {
            makehtm += "否";
        } else {
            makehtm += "是";
        }
        makehtm += "</br>選取：";
        if (Security.DISABLED_SELECT) {
            makehtm += "否";
        } else {
            makehtm += "是";
        }
        makehtm += "</br>PrtScr：";
        if (Security.DISABLED_PRT_SCR || Security.FORCE_CLEARDATA) {
            makehtm += "否";
        } else {

            makehtm += "是";

        }
                
        makehtm += "</br><a href='" + Security.HELP_URL + "' target='TsbSecurityHelp'>線上協助</a>";
        oDiv.innerHTML = makehtm;

    }
    oDiv.style.border = "2px groove orange";
    oDiv.style.position = "absolute";
    oDiv.style.fontSize = "10pt";
    oDiv.style.width = "20px";
    oDiv.style.height = "20px";
    oDiv.style.zIndex = 5000;
    oDiv.style.backgroundColor = "blue";
    oDiv.style.filter = "Alpha(opacity = 20)";
    //oDiv.style.top = parseFloat(document.documentElement.clientHeight) - 30;
    //oDiv.style.left = parseFloat(document.documentElement.clientWidth )- 30;
    //oDiv.style.top = "50%";
    oDiv.style.top = Security.ControlPanel_Pos;
    oDiv.style.left = "95%";
    
    document.body.appendChild(oDiv);

    //Mod by Patrick
    //document.body.attachEvent('onscroll', function() { try { document.all.TsbSecurityControlPanel.style.top = parseInt(document.documentElement.scrollTop) + 220; } catch (e) { } });
    if (document.body.addEventListener) {
        document.body.addEventListener('scroll', function () { try { document.all.TsbSecurityControlPanel.style.top = parseInt(document.documentElement.scrollTop) + 220; } catch (e) { } }, false);
    }
    else if (document.body.attachEvent) {
        document.body.attachEvent('onscroll', function () { try { document.all.TsbSecurityControlPanel.style.top = parseInt(document.documentElement.scrollTop) + 220; } catch (e) { } });
    }



    //document.body.attachEvent('onscroll',function(){try{document.all.TsbSecurityControlPanel.style.top = parseInt(document.documentElement.scrollTop) + parseInt(document.all.TsbSecurityControlPanel.style.top);} catch (e) {}});    
    oDiv.onclick();
    oDiv.onclick();
}
Security.ProtectPage.IsIE = function () {
    if (Security.ProtectPage.GetInternetExplorerVersion() != -1) {
        return true;
    } else {
        return false;
    }
}
Security.ProtectPage.VisibilityWaterMark = function (hidden) {
    for (var i = 0; i < document.all.length; i++) {
        if (document.all[i].className == 'waterMark') {
            if (hidden) {
                document.all[i].style.visibility = 'hidden';
            } else {
                document.all[i].style.visibility = '';
            }
        }
    }
}


Security.ProtectPage.ShowWaterMark = function () {
    Security.ProtectPage.AddWaterMark(0);
}

Security.ProtectPage.ShowWaterMarkByAjax = function (clientHeight) {
    var main_H = Security.Common.DocumentHeight();//視窗高
    var gap_H = 20;
    if ((clientHeight + gap_H) < main_H) {
        Security.ProtectPage.AddWaterMark(clientHeight + gap_H);
    }
}
Security.ProtectPage.AddWaterMarkMulti = function (H) {
		//新增程式碼 by Patrick
    if(!document.body)
    {
			return;             
    }    
        
    var oImg;
    //var main_H = Security.Common.DocumentHeight();//視窗高
    //var main_W = Security.Common.DocumentWidth();//視窗寬
    var main_H = Security.DocumentHeight;//文件高度
    var main_W = Security.DocumentWidth;//文件寬度
    var watermark_H = Security.WATER_MARK_H; //浮水印高
    var watermark_W = Security.WATER_MARK_W; //浮水印寬
    var gap_W = 0;//浮水印間距寬  
    var gap_H = 0;//浮水印間距寬    
    var offset_H = 0;
    //for(var i=0;;i++)
    //{//控制高

		//新增程式碼 by Patrick
    var container_img = document.createElement('div'); 
   
    for (; ;) {
        for (var W = 0; W < main_W;) {//控制寬    
            //if((W + watermark_W + gap_W) > main_W) break;   //想避免列印時會有突出    
            //oImg = Security.ProtectPage.MakeWaterMarkVML();
            oImg = Security.ProtectPage.MakeWaterMark();
            oImg.style.top = H + offset_H + "px";
            oImg.style.left = W + "px";

            //註解掉程式碼 by Patrick
            //document.body.appendChild(oImg);
            
            //改寫後程式碼 by Patrick
            container_img.appendChild(oImg);
            
            W += watermark_W + gap_W;
        }
        H += watermark_H + gap_H;

				//@@                
        if (H > main_H) break;        
        //if (H > 4000) break;
       
        
        
        //if(i>1)
        //{
        //    if(H>(main_H-(watermark_H + gap_H))) break;
        //}
    }  
    
    //新增程式碼 by Patrick
    //一次性加入浮水印到IE DOM，避免重繪導致效能低落或是影響HTML顯示    
    //document.body. = container_img.outerHTML + document.body.innerHTML;  
    document.body.insertBefore(container_img, document.body.childNodes[0]);        
}
Security.ProtectPage.AddWaterMarkSingle = function () {
    //var vml = document.createElement("v:group");
    //vml.setAttribute("id", id);
    //vml.style.width = "600px";
    //vml.style.height = "400px";
    //vml.style.border = "solid 10px lightsteelblue";
    //vml.setAttribute("coordsize", "600 400");


    var oImg;
    oImg = Security.ProtectPage.MakeWaterMark();
    oImg.style.top = "10%";
    //oImg.style.left = "10%"; 
    //vml.appendChild(oImg);
    //document.body.appendChild(vml);

    //Mod by Patrick
    oImg.id = "waterMarkObj";

    if (document.body.addEventListener) {
        document.body.addEventListener('scroll', function () { try { document.all.waterMarkObj.style.top = document.documentElement.scrollTop; document.all.waterMarkObj.style.left = document.documentElement.scrollLeft; } catch (e) { } }, false);
    }
    else if (document.body.attachEvent) {
        document.body.attachEvent('onscroll', function () { try { document.all.waterMarkObj.style.top = document.documentElement.scrollTop; document.all.waterMarkObj.style.left = document.documentElement.scrollLeft; } catch (e) { } });
    }

    document.body.appendChild(oImg);
    //oImg.setAttribute("type","#shapetype1");
}
Security.ProtectPage.AddWaterMarkSingleFloat = function () {

    var oImg;
    oImg = Security.ProtectPage.MakeWaterMark();
    oImg.style.top = "10%";
    oImg.id = "waterMarkObj";
    //document.body.attachEvent('onscroll',function(){try{document.all.waterMarkObj.style.top = document.documentElement.scrollTop;document.all.waterMarkObj.style.left = document.documentElement.scrollLeft;} catch (e) {}});    


    document.body.appendChild(oImg);
}
Security.ProtectPage.MakeWaterMark_SIG_HTM_IMG = function () {
    var oImg = document.createElement("IMG");
    oImg.className = "waterMark";
    oImg.src = Security.SHOW_WATER_MARK_URL + "?e=" + Security.EMPID + "&c=&d=" + Security.SYSDT + "&p=" + Security.SHOW_WATER_LOGO;
    oImg.style.position = "absolute";
    oImg.style.width = Security.WATER_MARK_W;
    oImg.style.height = Security.WATER_MARK_H;
    oImg.style.zIndex = -1000;
    oImg.alt = "台新銀行 " + Security.EMPID + " " + Security.EMPNM + " " + Security.SYSDT;
    //oImg.style.filter = "progid:DXImageTransform.Microsoft.Alpha(opacity = 99)";
    //oImg.style.border = "2px groove orange";
    return oImg;

}
Security.ProtectPage.MakeWaterMark_HTM_IMG = function () {
	//alert('test');
    var oImg = document.createElement("IMG");
    oImg.className = "waterMark";
    oImg.onmouseover = function () {
        var ImgObjs;
        ImgObjs = document.body.getElementsByTagName("IMG");
        for (var i = 0; i < ImgObjs.length; i++) {
            if (ImgObjs[i].className == "waterMark") {
                ImgObjs[i].style.display = "";
            }
        }
        var src = window.event.srcElement;
        src.style.display = "none";
    }
    oImg.src = Security.SHOW_WATER_MARK_URL + "?e=" + Security.EMPID + "&c=&d=" + Security.SYSDT + "&p=" + Security.SHOW_WATER_LOGO;
    oImg.style.border = "2px groove orange";
    oImg.style.position = "absolute";
    oImg.style.width = Security.WATER_MARK_W;
    oImg.style.height = Security.WATER_MARK_H;
    oImg.style.zIndex = 1000;
    oImg.alt = "台新銀行 " + Security.EMPID + " " + Security.EMPNM + " " + Security.SYSDT;
    oImg.style.filter = "progid:DXImageTransform.Microsoft.Matrix(SizingMethod='auto expand',FilterType='nearest neighbor';sizingMethod='auto expand',m11=0.7071067690849304,m12=0.7071067690849304,m21=-0.7071067690849304,m22=0.7071067690849304)progid:DXImageTransform.Microsoft.Alpha(opacity = 20)";
    return oImg;
}

Security.ProtectPage.MakeWaterMark_VML_IMG = function () {
    var oImg = document.createElement("v:image");
    oImg.className = "waterMark";
    oImg.onmouseover = function () {
        var ImgObjs;
        ImgObjs = document.body.getElementsByTagName("image");
        for (var i = 0; i < ImgObjs.length; i++) {
            if (ImgObjs[i].className == "waterMark") {
                ImgObjs[i].style.display = "";
            }
        }
        var src = window.event.srcElement;
        src.style.display = "none";
    }
    oImg.style.position = "absolute";
    oImg.style.zIndex = 1000;
    oImg.src = Security.SHOW_WATER_MARK_URL + "?e=" + Security.EMPID + "&c=&d=" + Security.SYSDT + "&p=" + Security.SHOW_WATER_LOGO;
    oImg.style.width = Security.WATER_MARK_W;
    oImg.style.height = Security.WATER_MARK_H;
    oImg.alt = "台新銀行 " + Security.EMPID + " " + Security.EMPNM + " " + Security.SYSDT;
    oImg.style.fontSize = "20pt";
    oImg.style.rotation = "-45";
    oImg.style.filter = "Alpha(opacity = 20)";
    return oImg;
}
Security.ProtectPage.MakeWaterMark_VML_TEXT = function () {

    var oSapTit = document.createElement("v:shape");
    oSapTit.className = "waterMark";
    oSapTit.setAttribute("type", "#shapetype1");
    oSapTit.filled = "f";
    oSapTit.strokecolor = "red";

    oSapTit.style.position = "absolute";
    oSapTit.style.zIndex = 1000;
    oSapTit.style.width = Security.WATER_MARK_W;
    oSapTit.style.height = Security.WATER_MARK_H;
    oSapTit.style.rotation = "-45";
    oSapTit.style.border = "solid 1px lightsteelblue";
    var oTxtTit = document.createElement("v:textpath");
    oTxtTit.trim = "t";
    oTxtTit.fitpath = "t";
    oTxtTit.type = "shapetype1";
    oTxtTit.string = "台新銀行";
    oTxtTit.style.cssText = "font-family:標楷體;font-size:50pt;v-text-reverse:t;v-text-kern:t;";

    oSapTit.appendChild(oTxtTit);

    return oSapTit;
}
Security.ProtectPage.MakeWaterMark_TEXT = function () {
    var oSpan = document.createElement("SPAN");
    oSpan.className = "waterMark";
    oSpan.style.position = "absolute";
    //oSpan.style.width = Security.WATER_MARK_W * Security.SHOW_WATER_RATE;
    //oSpan.style.height = Security.WATER_MARK_H * Security.SHOW_WATER_RATE;
    oSpan.style.width = Security.WATER_MARK_W;
    oSpan.style.height = Security.WATER_MARK_H;
    oSpan.style.zoom = Security.SHOW_WATER_RATE; //避免圖片比例過小造成文字顯示超過圖片
    oSpan.style.zIndex = -1001;
    oSpan.style.verticalAlign = "text-bottom";
    oSpan.style.fontSize = "58pt";
    oSpan.style.color = "darkgray";
    oSpan.style.fontFamily = "標楷體";
    //oSpan.innerHTML = "<table style='width:100%;height:100%;'><tr><td><h1>台新銀行</h1></td></tr><tr><td><h1>" + Security.EMPID + "</h1></td></tr><tr><td><h1>" + Security.SYSDT + "</h1></td></tr></table>";
    oSpan.innerHTML = "&nbsp;&nbsp;</br><h1 style='font-size:16pt;'>台新銀行</h1></br><h1 style='font-size:16pt;'>" + Security.EMPID + "</h1></br><h1 style='font-size:16pt;'>" + Security.SYSDT + "</h1>";
    return oSpan;
}
Security.ProtectPage.AddBackGroundColor = function (color) {
    var oDiv = document.createElement("DIV");
    oDiv.className = "waterMark";
    oDiv.style.position = "absolute";
    oDiv.style.top = 0;
    oDiv.style.left = 0;
    oDiv.style.width = Security.DocumentWidth;
    oDiv.style.height = Security.DocumentHeight;
    oDiv.style.zIndex = -1002;
    oDiv.style.backgroundColor = color;
    return oDiv;
}
Security.ProtectPage.DisabledSaveAs = function () {
    //var iframe = document.createElement("iframe");
    //    iframe.id = "SaveAS";
    //    iframe.style.height = 0;
    //    document.body.appendChild(iframe);
    //    SaveAS.document.write("<noScript><iframe src = '*.htm'></iframe></noScript>");
    return "<noScript><iframe src = '*.htm'></iframe></noScript>";

}
Security.ProtectPage.PrintLog = function () {
    //return Security.HTTP.Get(Security.LOG_URL + "?e=" + Security.EMPID + "&t=0&d=" + Security.SYSDT + "&u=" + Security.HTTPURL + "&c=" + Security.CLINET_IP +"&s=" + Security.SERVER_IP);
    return Security.HTTP.PostXml(Security.LOG_URL, Security.ProtectPage.defaultPostXml(Security.PrintLog));
}
Security.ProtectPage.SaveAsLog = function () {
    //Security.HTTP.Get(Security.LOG_URL + "?e=" + Security.EMPID + "&t=1&d=" + Security.SYSDT + "&u=" + Security.HTTPURL + "&c=" + Security.CLINET_IP +"&s=" + Security.SERVER_IP);
    return Security.HTTP.PostXml(Security.LOG_URL, Security.ProtectPage.defaultPostXml(Security.SaveAsLog));

}
Security.ProtectPage.ExportLog = function () {
    //Security.HTTP.Get(Security.LOG_URL + "?e=" + Security.EMPID + "&t=2&d=" + Security.SYSDT + "&u=" + Security.HTTPURL + "&c=" + Security.CLINET_IP +"&s=" + Security.SERVER_IP);
    return Security.HTTP.PostXml(Security.LOG_URL, Security.ProtectPage.defaultPostXml(Security.ExportLog));
}
Security.ProtectPage.DownLoadLog = function () {
    //Security.HTTP.Get(Security.LOG_URL + "?e=" + Security.EMPID + "&t=3&d=" + Security.SYSDT + "&u=" + Security.HTTPURL + "&c=" + Security.CLINET_IP +"&s=" + Security.SERVER_IP);
    return Security.HTTP.PostXml(Security.LOG_URL, Security.ProtectPage.defaultPostXml(Security.DownLoadLog));
}
Security.ProtectPage.PrtScrLog = function () {
	if (Security.ACTIVITE_LOG) {
    return Security.HTTP.PostXml(Security.LOG_URL, Security.ProtectPage.defaultPostXml(Security.PrtScrLog));
  }
}
Security.ProtectPage.DisabledCtrlKeys = function () {

    //IE 11 using addEventListerner Mod by Patrick
    //document.attachEvent('onkeydown', Security.ProtectPage.CtrlKeys);
    if (window.addEventListener) {
        document.addEventListener('keydown', Security.ProtectPage.CtrlKeys);
    }
    else {
        document.attachEvent('onkeydown', Security.ProtectPage.CtrlKeys);
    }
}
Security.ProtectPage.CtrlKeys = function () {
    if (!Security.DISABLED_CTRL) return true;
    if (event.ctrlKey) { var arrKeys = Security.DISABLED_CTRL_KEYS.split(','); for (var i = 0; i < arrKeys.length; i++) { if (event.keyCode == arrKeys[i]) { alert(Security.DISABLED_CTRL_DES + String.fromCharCode(arrKeys[i])); return false; } } }
}
Security.ProtectPage.DisabledSelect = function () {
    //IE 11 using addEventListerner Mod By Patrick
    //document.attachEvent('onselectstart', Security.ProtectPage.Select);
    if (window.addEventListener) {
        document.addEventListener('selectstart', Security.ProtectPage.Select);
    }
    else {
        document.attachEvent('onselectstart', Security.ProtectPage.Select);
    }

}
Security.ProtectPage.Select = function () {
    if (!Security.DISABLED_SELECT) return true;
    alert(Security.DISABLED_DES); return false;
}
Security.ProtectPage.DisabledDrag = function () {
    //IE 11 using addEventListerner Mod By Patrick
    //document.attachEvent('ondragstart',Security.ProtectPage.Drag);
    if (window.addEventListener) {
        document.addEventListener('dragstart', Security.ProtectPage.Drag);
    }
    else {
        document.attachEvent('ondragstart', Security.ProtectPage.Drag);
    }
}
Security.ProtectPage.Drag = function () {
    if (!Security.DISABLED_DRAG) return true;
    alert(Security.DISABLED_DRAG_DES); return false;
}

Security.ProtectPage.DisabledRightClick = function () {		
	  
    //IE 11 using addEventListerner Mod by Patrick
    //document.attachEvent('oncontextmenu',Security.ProtectPage.RightClick);
    if (window.addEventListener) {
        document.addEventListener('contextmenu', Security.ProtectPage.RightClick);
    }
    else {  
        document.attachEvent('oncontextmenu', Security.ProtectPage.RightClick);
       }
    }
    
    Security.ProtectPage.RightClick = function (e) {    	   
        if (!Security.DISABLED_RIGHT_CLICK) 
        { 
        	 return true;
        }
        else
       	{	
        alert(Security.DISABLED_RIGHT_CLICK_DES); 
        event.preventDefault ? event.preventDefault() : event.returnValue = false;
        return false;
       }
     }  


    Security.ProtectPage.DisabledPrtScr = function () {        	      
	      if(!objPrtScr)
	      {
	          return;
	      }
        
        if (Security.DISABLED_PRT_SCR) {
            try {
                objPrtScr.Disabled();                
                //Security.ProtectPage.Timer = setInterval('Security.ProtectPage.PrtScr()', 3000);
                //alert("objPrtScr.Disabled");
            }
            catch (e) {
                Security.ProtectPage.Timer = setInterval('Security.ProtectPage.PrtScr()', 3000);
                //alert("因系統配置不符規定，複製功能已鎖定，請聯繫連管排除問題!!");
            }
        } else {
            try {
                objPrtScr.Enabled();                               
                clearInterval(Security.ProtectPage.Timer);
                //alert("objPrtScr.Enabled");
            }
            catch (e) {
                Security.ProtectPage.Timer = setInterval('Security.ProtectPage.PrtScr()', 3000);
                //alert("因系統配置不符規定，複製功能已鎖定，請聯繫連管排除問題!!");
            }
        }
    }
    Security.ProtectPage.CleanClipboardData = function () {
        if (Security.FORCE_CLEARDATA) {
            try {
                Security.ProtectPage.Timer = setInterval("window.clipboardData.setData('text',Security.DISABLED_PRT_SCR_DES)", 3000);
            } catch (e) {
            }
        }
    }
    Security.ProtectPage.PrtScr = function () {
        if (Security.DISABLED_PRT_SCR) {
            try {
                window.clipboardData.setData('text', Security.DISABLED_PRT_SCR_DES);
                //window.clipboardData.clearData('Image');
            } catch (e) {
            }
        }
        return false;
    }
    Security.ProtectPage.DisabledFilePrt = function () {
        //Mod by Patrick
        //window.attachEvent('onbeforeprint',Security.ProtectPage.BeforeFilePrt);
        //window.attachEvent('onafterprint',Security.ProtectPage.AfterFilePrt);
        if (window.addEventListener) {
            window.addEventListener('beforeprint', Security.ProtectPage.BeforeFilePrt, false);
        }
        else if (window.attachEvent) {
            window.attachEvent('onbeforeprint', Security.ProtectPage.BeforeFilePrt);
        }

        //Mod by Patrick
        if (window.addEventListener) {
            window.addEventListener('afterprint', Security.ProtectPage.AfterFilePrt, false);
        }
        else if (window.attachEvent) {
            window.attachEvent('onafterprint', Security.ProtectPage.AfterFilePrt);
        }

    }
    Security.ProtectPage.BeforeFilePrt = function () {
        var ErrMsg = Security.ProtectPage.PrintLog();

        if (ErrMsg != "") {
            ErrMsg += ":無法列印,請聯絡AP負責人!!"
            alert(ErrMsg);
            Security.DISABLED_FILE_PRT = true;
        }
        if (Security.DISABLED_FILE_PRT) {
            alert(Security.DISABLED_FILE_PRT_DES);
            for (var i = 0; i < document.all.length; i++) {
                if (document.all[i].style.visibility != 'hidden') {
                    document.all[i].style.visibility = 'hidden';
                    document.all[i].id = document.all[i].id + '_BefPrtDoc';
                }
            }
        } else {
            for (var i = 0; i < document.all.length; i++) {
                if (document.all[i].className == 'waterMark') {

                    switch (Security.SHOW_WATER_TYPR) {
                        case "SIG_HTM_IMG":
                        case "SIG_HTM_IMG_PRT":
                        case "SIG_HTM_IMG_FLOAT":
                            break;
                        default:
                            document.all[i].style.zIndex = -1000;
                            break;
                    }
                }
            }
        }
    }

    Security.ProtectPage.AfterFilePrt = function () {
        if (Security.DISABLED_FILE_PRT) {
            for (var i = 0; i < document.all.length; i++) {
                if (document.all[i].id.indexOf('_BefPrtDoc') != -1) {
                    document.all[i].style.visibility = '';
                    document.all[i].id = document.all[i].id.replace('_BefPrtDoc', '');
                }
            }
        } else {
            for (var i = 0; i < document.all.length; i++) {
                if (document.all[i].className == 'waterMark') {
                    switch (Security.SHOW_WATER_TYPR) {
                        case "SIG_HTM_IMG":
                        case "SIG_HTM_IMG_PRT":
                        case "SIG_HTM_IMG_FLOAT":
                            break;
                        default:
                            document.all[i].style.zIndex = 1000;
                            break;
                    }
                }
            }
        }
    }
    Security.ProtectPage.GetInternetExplorerVersion = function () {
        var rv = -1;
        //Marked by Patrick
        //if (navigator.appName == 'Microsoft Internet Explorer'){
        var ua = navigator.userAgent;
        //Marked by Patrick     
        // var re  = new RegExp("(MSIE ([0-9]{1,}[\.0-9]{0,}))");
        var re = new RegExp("(MSIE ([0-9]{1,}[\.0-9]{0,}))|(rv:11\.0)");
        if (re.exec(ua) != null)
            rv = parseFloat(RegExp.$1);
        //Marked by Patrick
        //} 

        return rv;
    }
	
	/**
	 * detect IE
	 * returns version of IE or false, if browser is not Internet Explorer
	 */
	function detectIE() {
	  var ua = window.navigator.userAgent;

	  // Test values; Uncomment to check result …

	  // IE 10
	  // ua = 'Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0)';
	  
	  // IE 11
	  // ua = 'Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko';
	  
	  // IE 12 / Spartan
	  // ua = 'Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36 Edge/12.0';
	  
	  // Edge (IE 12+)
	  // ua = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586';

	  var msie = ua.indexOf('MSIE ');
	  if (msie > 0) {
		// IE 10 or older => return version number
		return parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
	  }

	  var trident = ua.indexOf('Trident/');
	  if (trident > 0) {
		// IE 11 => return version number
		var rv = ua.indexOf('rv:');
		return parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
	  }

	  var edge = ua.indexOf('Edge/');
	  if (edge > 0) {
		// Edge (IE 12+) => return version number
		return parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10);
	  }

	  // other browser
	  return 0;
	}	
	
    window.onbeforeunload = function (e) {
		if (typeof objPrtScr != "undefined")
		{
			objPrtScr.Enabled();
		}
    }
	
	function doLoad() {
		alert( "The load event is executing" );
	}
        
    Security.ProtectPage.prototype = {}
    //================Adapter============================================
    var tsbsec_ctl;
    if (tsbsec_ctl && (typeof tsbsec_ctl != "object" || tsbsec_ctl.NAME))
        throw new Error("Namespace 'tsbsec_ctl' already exists");
    var tsbsec_ctl = {};
    tsbsec_ctl.NAME = "tsbsec_ctl";

    //log紀錄    
    tsbsec_ctl.print_doc = function () { Security.DISABLED_FILE_PRT = false; window.print(); Security.DISABLED_FILE_PRT = true; }
    tsbsec_ctl.doc_save_log = function () { Security.ProtectPage.SaveAsLog(); }
    tsbsec_ctl.doc_export_log = function () { Security.ProtectPage.ExportLog(); }
    tsbsec_ctl.doc_download_log = function () { Security.ProtectPage.DownLoadLog(); }

    tsbsec_ctl.lockprint = function () { Security.DISABLED_FILE_PRT = true; }
    tsbsec_ctl.unlockprint = function () { Security.DISABLED_FILE_PRT = false; }

    tsbsec_ctl.ScreenProtectPage = function (iLevel) {
        Security.DISABLED_CTRL_KEYS = "";
        if (iLevel != 0 && !(iLevel % 2 < 1)) {//Enabled Copy        
            Security.DISABLED_PRT_SCR = false;
        }
        else {//Disabled Copy
            Security.DISABLED_CTRL_KEYS = '67,88'; //禁止 用 CTRL + C 和 CTRL + X 複製
        }

        if (iLevel != 0 && !(iLevel % 4 < 2)) {//Enabled Select
            Security.DISABLED_SELECT = false;
        }
        else {//Disabled Select
            Security.DISABLED_SELECT = true;
        }

        if (iLevel != 0 && !(iLevel % 8 < 4)) {//Enabled mouse right key
            Security.DISABLED_RIGHT_CLICK = false;
        }
        else {//Disabled mouse right key
            Security.DISABLED_RIGHT_CLICK = true;
        }

        if (iLevel != 0 && !(iLevel % 16 < 8)) {//Enabled DRAG
            Security.DISABLED_DRAG = false;
        }
        else {//Disabled DRAG
            Security.DISABLED_DRAG = true;
        }

    }

    //=====Start Point===================================================   	
	var ie_version = detectIE();
    if (Security.ENABLE_LOAD) {
		//alert("ie_version="+ie_version);
		if(ie_version == 11)
		{			
			//document.addEventListener('load', doLoad, false);
			window.addEventListener('load', Security.ProtectPage.IniProtectPage, false);
		}
		else
		{
			if (typeof window.addEventListener != "undefined") {
				document.addEventListener('readystatechange', Security.ProtectPage.IniProtectPage, false);
			}
			else if (typeof window.attachEvent != "undefined") {           		     		    
				document.attachEvent('onreadystatechange', Security.ProtectPage.IniProtectPage);
			}
			else
			{
				windows.onload = Security.ProtectPage.IniProtectPage;
			}
		}
	}
//=================================================================== 