package com.bcs.core.web.ui.page.enums;


/**
 * Page Mapping
 * <p>
 * login : Login Page
 */
public enum BcsPageEnum {
    LoginPage("/BCS/Views/login"),
    LoginSSOPage("/BCS/Views/loginSSO"),

    MainPage("/BCS/Views/Page0"),

    SendGroupCreatePage("/BCS/Views/Page1"),
    SendGroupListPage("/BCS/Views/Page2"),

    MsgCreatePage("/BCS/Views/Page3"),
    CdnMsgCreatePage("/BCS/Views/PageCdnMsg"),
    MsgListDraftPage("/BCS/Views/Page4-1"),
    MsgListDelayPage("/BCS/Views/Page4-2"),
    MsgListSendedPage("/BCS/Views/Page4-3"),
    MsgListSchedulePage("/BCS/Views/Page4-4"),

    RichMsgListPage("/BCS/Views/Page5"),
    RichMsgCreatePage("/BCS/Views/Page6"),

    CouponCreatePage("/BCS/Views/Page7"),
    CouponListPage("/BCS/Views/Page8"),
    CouponListDisablePage("/BCS/Views/Page8-1"),
    CouponListApiPage("/BCS/Views/Page8-2"),
    CouponReportPage("/BCS/Views/Page24"),
    CouponSerialNumberPage("/BCS/Views/Page-CouponSerialNumber"),
    CouponWinnerListPage("/BCS/Views/Page-CouponWinnerList"),

    AdminUserCreatePage("/BCS/Views/Page9"),
    AdminUserListPage("/BCS/Views/Page10"),
    AdminUserBoardPage("/BCS/Views/Page44"),

    ReportPage("/BCS/Views/Page11"),
    ReportCreatePage("/BCS/Views/Page16"),
    ReportLinkClickPage("/BCS/Views/Page20"),
    ReportLinkClickDetailPage("/BCS/Views/Page21"),
    ReportPageVisitPage("/BCS/Views/Page22"),
    ReportPageVisitDetailPage("/BCS/Views/Page23"),
    ReportPushApiEffectsPage("/BCS/Views/Page-PushApiEffects"),
    ReportBNEffectsPage("/BCS/Views/PageBNEffects"),
	ReportBNEffectsDetailPage("/BCS/Views/PageBNEffectsDetail"),

    InteractiveResponsePage("/BCS/Views/Page12"),
    InteractiveResponseDisablePage("/BCS/Views/Page12-1"),
    InteractiveResponseCreatePage("/BCS/Views/Page13"),

    KeywordResponsePage("/BCS/Views/Page14"),
    KeywordResponseDisablePage("/BCS/Views/Page14-1"),
    KeywordResponseExpirePage("/BCS/Views/Page14-2"),
    KeywordResponseIneffectiveBtnPage("/BCS/Views/Page14-3"),
    KeywordResponseCreatePage("/BCS/Views/Page15"),

    BlackKeywordResponsePage("/BCS/Views/Page17"),
    BlackKeywordResponseDisablePage("/BCS/Views/Page17-1"),
    BlackKeywordResponseCreatePage("/BCS/Views/Page18"),

    KeywordAndInteractiveReportPage("/BCS/Views/Page19"),

    // Report Page
    SystemReportPage("/BCS/Report/SystemReportPage"),
    OtherPage("/BCS/Report/OtherPage"),
    ConvertingMidToUidPage("/BCS/Report/ConvertingMidToUidPage"),
    UpdateBindedStatusPage("/BCS/Report/UpdateBindedStatusPage"),

    ConfigListPage("/BCS/Report/ConfigListPage"),
    ConfigCreatePage("/BCS/Report/ConfigCreatePage"),

    ConnectionTestPage("/BCS/Report/ConnectionTestPage"),

    // Test Page
    CouponCheckPage("/BCS/Test/CouponCheckPage"),

    // Tracing Generate Page
    TracingGeneratePage("/BCS/Views/Page25"),
    TracingGenerateListPage("/BCS/Views/Page26"),

    SerialSettingPage("/BCS/Views/Page28"),
    SerialSettingListPage("/BCS/Views/Page29"),

    ProductCreatePage("/BCS/Views/Page31"),

    ProductGroupListPage("/BCS/Views/Page32"),
    ProductGroupCreatePage("/BCS/Views/Page33"),

    CampaignListPage("/BCS/Views/Page30"),
    CampaignCreatePage("/BCS/Views/Page34"),

    CampaignResponsePage("/BCS/Views/Page35"),
    CampaignResponseDisablePage("/BCS/Views/Page35-1"),
    CampaignResponseCreatePage("/BCS/Views/Page36"),

    CampaignUserListPage("/BCS/Views/Page37"),

    LineUserUploadPage("/BCS/Views/Page98"),
    // Upload Picture Page
    UploadPicturePage("/BCS/Views/Page99"),
    // 測試發票批次同步
    RefreshInvoicePage("/BCS/Views/Page97"),

    //輸出ExcelTestPage
    ExcelExportTestPage("/BCS/Views/Page100"),

    //建立通路群組page
    BusinessGroupCreatePage("/BCS/Views/Page38"),
    BusinessGroupListPage("/BCS/Views/Page39"),

    //建立通路page
    BusinessCreatePage("/BCS/Views/Page40"),

    //RewardCard
    RewardCardCreatePage("/BCS/Views/Page41"),
    RewardCardListPage("/BCS/Views/Page42"),
    RewardCardPointRecordPage("/BCS/Views/Page43"),
    RewardCardDisableListPage("/BCS/Views/Page42-1"),
    RewardCardQRCodePage("/BCS/Views/Page42-QR"),

    //Template Page
    TemplateMsgCreatePage("/BCS/Views/PageT1"),
    TemplateMsgListPage("/BCS/Views/PageT2"),

    // Billing Notice Page
    BillingNoticeCreatePage("/BCS/Views/PageBN1"),
    BillingNoticeListPage("/BCS/Views/PageBN2"),

    // Line Point Page
    LinePointListPage("/BCS/Views/PageLPL"),
    LinePointCreatePage("/BCS/Views/PageLPC"),
    LinePointStatisticsReportPage("/BCS/Views/PageLPStatisticsReport"),
    LinePointStatisticsReportDetailPage("/BCS/Views/PageLPStatisticsReportDetail"),
//	LinePointSendPage("/BCS/Views/PageLPS"),
//	LinePointSendOldPage("/BCS/Views/PageLPSOld"),
//	LinePointReportPage("/BCS/Views/PageLPR"),
//	LinePointDetailPage("/BCS/Views/PageLPD"),

    // PNP Template
    PnpCreatePage("/BCS/Views/PagePNP1"),
    PnpListPage("/BCS/Views/PagePNP2"),

    // PNP Maintain
    PNPNormalAccountListPage("/BCS/Views/PagepnpNAL"),
    PNPNormalAccountCreatePage("/BCS/Views/PagepnpNAC"),
    PNPUnicaAccountListPage("/BCS/Views/PagepnpUAL"),
    PNPUnicaAccountCreatePage("/BCS/Views/PagepnpUAC"),

    // PNP Report
    PNP_DETAIL_REPORT_PAGE("/BCS/Views/PagepnpDetailReport"),
    PNP_ANALYSIS_REPORT_PAGE("/BCS/Views/PagepnpAnalysisReport"),
    
    // PNP BlackList
    PNP_EXCLUDE_SENDING_LIST_PAGE("/BCS/Views/PagePnpExcludeSendingList"),
    PNP_EXCLUDE_SENDING_LIST_HISTORY_PAGE("/BCS/Views/PagePnpExcludeSendingListHistory"),

    //Game
    GameCreatePage("/BCS/Views/PageG1"),
    GameListPage("/BCS/Views/PageG2"),
    TurntableCreatePage("/BCS/Views/PageG3"),
    WinnerListPage("/BCS/Views/PageG4"),
    PrizeListPage("/BCS/Views/PageG5"),
    ScratchCardCreatePage("/BCS/Views/PageSC1"),

    CampaignVIPNightIndexPage("/BCS/Campaigns/VIPNight/IndexPage"),

    // MGM
    ShareCampaignCreatePage("/BCS/Views/PageMGM1"),
    ShareCampaignListPage("/BCS/Views/PageMGM2"),
    ShareCampaignListDisablePage("/BCS/Views/PageMGM3"),
    ShareCampaignReportPage("/BCS/Views/PageMGM4"),

    // 電子序號
    EsnCreatePage("/BCS/Views/PageESN1"),
    EsnListPage("/BCS/Views/PageESN2"),

    //貴賓之夜
    QRcodeScannedRecordPage("/BCS/Views/Page-QRcodeScannedRecord"),
    ;

    private final String str;

    BcsPageEnum(String str) {
        this.str = str;
    }

    /**
     * @return the str
     */
    public String toString() {
        return str;
    }

}
