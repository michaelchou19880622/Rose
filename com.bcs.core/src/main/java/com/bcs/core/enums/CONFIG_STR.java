package com.bcs.core.enums;

public enum CONFIG_STR {
	SYSTEM_START_DATE("system.start.date"),
	SYSTEM_TYPE("bcs.system.type"),
	
	SYSTEM_ID("system.id"),
	SYSTEM_TYPE_IS_API("system.is.api"),
	IS_MAIN_SYSTEM("is.main.system"),
	
	
	SYSTEM_COUPON_USE_TIME("system.coupon.record.useTime"),
	SYSTEM_REWARDCARD_USE_TIME("system.rewardcard.record.useTime"),
	
	SYSTEM_USE_PROXY("system.use.proxy"),
	SYSTEM_CHECK_SIGNATURE("system.check.signature"),
	
	BaseUrlHTTPS("bcs.base.https.url"),
	BaseUrlHTTP("bcs.base.http.url"),
    CdnUrlHTTPS("bcs.cdn.url"),
    
	PageMobile("bcs.base.url.page.mobile"),
	ResourceMobile("bcs.base.url.resource.mobile"),
	
	PageBCS("bcs.base.url.page.bcs"),
	ResourceBCS("bcs.base.url.resource.bcs"),
	
	FilePath("file.path"),
	BillingNoticeExportFilePath("billing.notice.file.path"),
	
	Default("TaishinBank"),
	//Default("HPiCC"),
	
	AutoReply("AutoReply"),
	ManualReply("ManualReply"),
	InManualReplyButNotSendMsg("InManualReplyButNotPush"),
	
	M_PAGE("bcs.m.page"),
	
	TAISHIN_POST_URL_USER_STATUS("taishin.post.url.userStatus"),
	
	LINE_GET_PROFILE_URL("line.get.profile.url"),
	LINE_MESSAGE_REPLY_URL("line.message.reply.url"),
	LINE_MESSAGE_PUSH_URL("line.message.push.url"),
	
	LINE_POST_URL_BC("line.post.url.bc"),

	LINE_GET_URL_BC("line.get.url.bc"),
	LINE_GET_URL_BOT("line.get.url.bot"),
	
	LINE_OAUTH_URL("line.oauth.url"),
	LINE_OAUTH_URL_V2_1("line.oauth.url.v2.1"),
	LINE_OAUTH_URL_ACCESSTOKEN("line.oauth.url.accessToken"),
	LINE_OAUTH_URL_ACCESSTOKEN_V2_1("line.oauth.url.accessToken.v2.1"),
	LINE_OAUTH_VERIFY("line.oauth.verify"),
	LINE_OAUTH_PROFILE("line.oauth.url.profile"),
	LINE_OAUTH_FRIENDSHIP_STATUS("line.oauth.url.friendship.status"),
	LINE_CONVERTING_GET("line.converting.get"),
	LINE_CONVERTING_POST("line.converting.post"),
	LINE_SWITCH_API_SWITCHER_SWITCH("line.switcher.switch"),
	LINE_SWITCH_API_SWITCHER_NOTICE("line.switcher.notice"),
	
	ChannelToken("ChannelToken"),
	ChannelServiceCode("ChannelServiceCode"),
	ChannelRefresh("ChannelRefresh"),
	
	ChannelID("ChannelID"),
	ChannelSecret("ChannelSecret"),
	Channel_MID("Channel_MID"),
	
	ChannelSwitchIconName("ChannelSwitchIconName"),
	ChannelSwitchIconUrl("ChannelSwitchIconUrl"),

	EVENT_SHARE("bcs.event.share"),
	EVENT_SHARE_DELAY("bcs.event.share.delay"),

	SMART_ROBOT_API("smartrobot.api.url"),
	SMART_ROBOT_BOT_API("smartrobot.bot.api.url"),
	
	TRACING_CONFIG_GET_FROM_SESSION("tracing.config.get.from.session"),
	TRACING_CONFIG_USE_SWITCH("tracing.config.use.switch"),
	TRACING_CONFIG_CHECK_MOBILE("tracing.config.check.mobile"),
	
	HASH_PREFIX("hash.prefix"),
	HASH_SUFFIX("hash.suffix"),
	
	RECORD_RECEIVE_AUTORESPONSE_TEXT("record.receive.autoresponse.text"),

	PASSWORD_PREFIX("password.prefix"),
	PASSWORD_SUFFIX("password.suffix"),
	SSO_LOGIN_URL("sso.login.url"),
	
	API_ORIGINAL_TOKEN("api.original.token"),
	LINE_POINT_API_ORIGINAL_TOKEN("line.point.api.original.token"),
	AES_SECRET_KEY("aes.secret.key"),
	AES_INITIALIZATION_VECTOR("aes.initialization.vector"),
	LINE_POINT_CHANNEL_TOKEN("line.point.channel.token"),
	
	TAISHIN_PROXY_URL("taishin.proxy.url"),
	TAISHIN_LOGIN_URL("taishin.login.url"),
	RICHART_ANNOUNCEMENT_URL("richart.announcement.url"),
	
	GATEWAY_CHANNEL("gateway.channel"),
	GATEWAY_API_URL("gateway.api.url"),
	GATEWAY_API_KEY("gateway.api.key"),
	GATEWAY_API_SECRET("gateway.api.secret"),
	
	LIVECHAT_CHANNEL("livechat.channel"),
	LIVECHAT_START_API_URL("livechat.start.api.url"),
	LIVECHAT_ADD_MESSAGE_API_URL("livechat.add.message.api.url"),
	LIVECHAT_GIVE_UP_API_URL("livechat.giveup.api.url"),
	LIVECHAT_CLOSE_API_URL("livechat.close.api.url"),
	LIVECHAT_CHECK_API_URL("livechat.check.api.url"),
	LIVECHAT_RESET_API_URL("livechat.reset.api.url"),
	LIVECHAT_LEAVE_MESSAGE_API_URL("livechat.leave.message.api.url"),
	
	CHATLOG_GET_MAX_HOUR("chatlog.get.max.hour"),
	LIVECHAT_STATUS_CHECK_CRON("livechat.status.check.cron"),
	
	AUTOREPLY_CHANNEL_NAME("autoreply.channel.name"),
	MANUALREPLY_CHANNEL_NAME("manualreply.channel.name"),
	
	TAISHIN_LOG_API_URL("taishin.log.api.url"),
	TAISHIN_LOG_API_KEY("taishin.log.api.key"),
	
	SRC_USE_STATIC("src.use.static"),
	
	BCS_API_CLUSTER_SEND("rest.api.cluster.send"),
	BCS_API_CLUSTER_SEND_THIS("rest.api.cluster.send.this"),
	
	ADD_LINE_FRIEND_LINK("add.line.friend.link"),
	
	VIP_NIGHT_MGM_ID("vip.night.mgm.id"),
	VIP_NIGHT_MGM_MSG("vip.night.mgm.msg"),
	
	MGM_ACTION_IMG_CDN_URL("mgm.action.img.cdn.url"),
	MGM_SHARE_IMG_CDN_URL("mgm.share.img.cdn.url"),
	MGM_DESCRIPTION_IMG_CDN_URL("mgm.description.img.cdn.url"),
	
	//Billing Notice
    BN_SCHEDULE_TIME("bn.schedule.time"),
    BN_SCHEDULE_UNIT("bn.schedule.unit"),
    BN_FTP_CHANNELIDS("bn.ftp.channelIds"),
    BN_FTP_TYPE("bn.ftp.type"),
    BN_FTP_PROTOCOL("bn.ftp.protocol"),
    BN_FTP_PATH("bn.ftp.path"),
    BN_FTP_HOST("bn.ftp.host"),
    BN_FTP_PORT("bn.ftp.port"),
    BN_FTP_IS64BIT("bn.ftp.is64Bit"),
    BN_FTP_SERVER_HOSTNAME("bn.ftp.serverHostName"),
    BN_FTP_SERVER_HOSTNAME_PORT("bn.ftp.serverHostName.port"),
    BN_FTP_APP_CODE("bn.ftp.APPCode"),
    BN_FTP_RES_CODE("bn.ftp.RESCode"),
    BN_FTP_ACCOUNT("bn.ftp.account"),
    BN_FTP_PASSWORD("bn.ftp.password"),
    BN_FTP_FILE_ENCODING("bn.ftp.fileEncoding"),
    BN_FTP_FILE_EXTENSION("bn.ftp.fileExtension"),
    BN_FTP_DOWNLOAD_SAVEFILEPATH("bn.ftpDownload.saveFilePath"),
    IS_BN_SENDMSG("is.bn.sendMsg"),
	IS_BN_FTPDOWNLOAD("is.bn.ftpDownload"),
	BN_BIGSWITCH("bn.bigswitch"),
	
    //Billing Notice
	//PNP
	SYSTEM_PNP_FTP_INFO("system.pnp.ftp.info"),
	PNP_BIGSWITCH("pnp.bigswitch"),
	IS_PNP_FTPDOWNLOAD("pnp.ftpDownload"),
	IS_PNP_SENDMSG("pnp.sendMsg"),
	PNP_FTP_IS64BIT("pnp.ftp.is64Bit"),
	PNP_FTP_TYPE("pnp.ftp.type"),
	PNP_FTP_FILE_EXTENSION("pnp.ftp.fileExtension"),
	PNP_DELIVERY_EXPIRED_TIME_UNIT("pnp.delivery.expired.time.unit"),
	PNP_DELIVERY_EXPIRED_TIME("pnp.delivery.expired.time"),
	
	// DEFAULT PNP
	PNP_FTP_SERVERHOSTNAME_("pnp.ftp.serverHostName."),
	PNP_FTP_SERVERHOSTNAME_PORT_("pnp.ftp.serverHostName.port."),
	PNP_FTP_HOST_("pnp.ftp.host."),
	PNP_FTP_PORT_("pnp.ftp.port."),
	PNP_FTP_USR_("pnp.ftp.usr."),
	PNP_FTP_PASS_("pnp.ftp.pass."),
	PNP_FTP_APPCODE_("pnp.ftp.APPCode."),
	PNP_FTP_RESCODE_("pnp.ftp.RESCode."),
	PNP_FTP_PROTOCOL_("pnp.ftp.protocol."),
	PNP_FTP_DOWNLOAD_TO_LOCAL_PATH_("pnp.ftp.download.to.local.path."),
	PNP_FTP_DOWNLOAD_PATH_("pnp.ftp.download.path."),
	PNP_PROC_FLOW_("pnp.proc.flow."),
	
	PNP_SMS_UPLOAD_PATH_("pnp.sms.upload.path."),
	PNP_SMS_SERVERHOSTNAME_("pnp.sms.serverHostName."),
	PNP_SMS_SERVERHOSTNAME_PORT_("pnp.sms.serverHostName.port."),
	PNP_SMS_HOST_("pnp.sms.host."),
	PNP_SMS_PORT_("pnp.sms.port."),
	PNP_SMS_USR_("pnp.sms.usr."),
	PNP_SMS_PASS_("pnp.sms.pass."),
	PNP_SMS_APPCODE_("pnp.sms.APPCode."),
	PNP_SMS_RESCODE_("pnp.sms.RESCode."),
	PNP_SMS_PROTOCOL_("pnp.sms.protocol."),
	
	
	//三竹
	PNP_FTP_SERVERHOSTNAME_MITAKE("pnp.ftp.serverHostName.mitake"),
	PNP_FTP_SERVERHOSTNAME_PORT_MITAKE("pnp.ftp.serverHostName.port.mitake"),
	PNP_FTP_HOST_MITAKE("pnp.ftp.host.mitake"),
	PNP_FTP_PORT_MITAKE("pnp.ftp.port.mitake"),
	PNP_FTP_USR_MITAKE("pnp.ftp.usr.mitake"),
	PNP_FTP_PASS_MITAKE("pnp.ftp.pass.mitake"),
	PNP_FTP_APPCODE_MITAKE("pnp.ftp.APPCode.mitake"),
	PNP_FTP_RESCODE_MITAKE("pnp.ftp.RESCode.mitake"),
	PNP_FTP_PROTOCOL_MITAKE("pnp.ftp.protocol.mitake"),
	PNP_FTP_DOWNLOAD_TO_LOCAL_PATH_MITAKE("pnp.ftp.download.to.local.path.mitake"),
	PNP_FTP_DOWNLOAD_PATH_MITAKE("pnp.ftp.download.path.mitake"),
	PNP_PROC_FLOW_MITAKE("pnp.proc.flow.mitake"),
	
	PNP_SMS_UPLOAD_PATH_MITAKE("pnp.sms.upload.path.mitake"),
	PNP_SMS_SERVERHOSTNAME_MITAKE("pnp.sms.serverHostName.mitake"),
	PNP_SMS_SERVERHOSTNAME_PORT_MITAKE("pnp.sms.serverHostName.port.mitake"),
	PNP_SMS_HOST_MITAKE("pnp.sms.host.mitake"),
	PNP_SMS_PORT_MITAKE("pnp.sms.port.mitake"),
	PNP_SMS_USR_MITAKE("pnp.sms.usr.mitake"),
	PNP_SMS_PASS_MITAKE("pnp.sms.pass.mitake"),
	PNP_SMS_APPCODE_MITAKE("pnp.sms.APPCode.mitake"),
	PNP_SMS_RESCODE_MITAKE("pnp.sms.RESCode.mitake"),
	PNP_SMS_PROTOCOL_MITAKE("pnp.sms.protocol.mitake"),
	
	//三竹
	//互動
	PNP_FTP_SERVERHOSTNAME_EVERY8D("pnp.ftp.serverHostName.every8d"),
	PNP_FTP_SERVERHOSTNAME_PORT_EVERY8D("pnp.ftp.serverHostName.port.every8d"),
	PNP_FTP_HOST_EVERY8D("pnp.ftp.host.every8d"),
	PNP_FTP_PORT_EVERY8D("pnp.ftp.port.every8d"),
	PNP_FTP_USR_EVERY8D("pnp.ftp.usr.every8d"),
	PNP_FTP_PASS_EVERY8D("pnp.ftp.pass.every8d"),
	PNP_FTP_APPCODE_EVERY8D("pnp.ftp.APPCode.every8d"),
	PNP_FTP_RESCODE_EVERY8D("pnp.ftp.RESCode.every8d"),
	PNP_FTP_PROTOCOL_EVERY8D("pnp.ftp.protocol.every8d"),
	PNP_FTP_DOWNLOAD_TO_LOCAL_PATH_EVERY8D("pnp.ftp.download.to.local.path.every8d"),
	PNP_FTP_DOWNLOAD_PATH_EVERY8D("pnp.ftp.download.path.every8d"),
	PNP_PROC_FLOW_EVERY8D("pnp.proc.flow.every8d"),
	
	PNP_SMS_UPLOAD_PATH_EVERY8D("pnp.sms.upload.path.every8d"),
	PNP_SMS_SERVERHOSTNAME_EVERY8D("pnp.sms.serverHostName.every8d"),
	PNP_SMS_SERVERHOSTNAME_PORT_EVERY8D("pnp.sms.serverHostName.port.every8d"),
	PNP_SMS_HOST_EVERY8D("pnp.sms.host.every8d"),
	PNP_SMS_PORT_EVERY8D("pnp.sms.port.every8d"),
	PNP_SMS_USR_EVERY8D("pnp.sms.usr.every8d"),
	PNP_SMS_PASS_EVERY8D("pnp.sms.pass.every8d"),
	PNP_SMS_APPCODE_EVERY8D("pnp.sms.APPCode.every8d"),
	PNP_SMS_RESCODE_EVERY8D("pnp.sms.RESCode.every8d"),
	PNP_SMS_PROTOCOL_EVERY8D("pnp.sms.protocol.every8d"),
	
	
	//互動
	//明宣
	PNP_FTP_SERVERHOSTNAME_MING("pnp.ftp.serverHostName.ming"),
	PNP_FTP_SERVERHOSTNAME_PORT_MING("pnp.ftp.serverHostName.port.ming"),
	PNP_FTP_HOST_MING("pnp.ftp.host.ming"),
	PNP_FTP_PORT_MING("pnp.ftp.port.ming"),
	PNP_FTP_USR_MING("pnp.ftp.usr.ming"),
	PNP_FTP_PASS_MING("pnp.ftp.pass.ming"),
	PNP_FTP_APPCODE_MING("pnp.ftp.APPCode.ming"),
	PNP_FTP_RESCODE_MING("pnp.ftp.RESCode.ming"),
	PNP_FTP_PROTOCOL_MING("pnp.ftp.protocol.ming"),
	PNP_FTP_DOWNLOAD_TO_LOCAL_PATH_MING("pnp.ftp.download.to.local.path.ming"),
	PNP_FTP_DOWNLOAD_PATH_MING("pnp.ftp.download.path.ming"),
	PNP_PROC_FLOW_MING("pnp.proc.flow.ming"),
	
	PNP_SMS_UPLOAD_PATH_MING("pnp.sms.upload.path.ming"),
	PNP_SMS_SERVERHOSTNAME_MING("pnp.sms.serverHostName.ming"),
	PNP_SMS_SERVERHOSTNAME_PORT_MING("pnp.sms.serverHostName.port.ming"),
	PNP_SMS_HOST_MING("pnp.sms.host.ming"),
	PNP_SMS_PORT_MING("pnp.sms.port.ming"),
	PNP_SMS_USR_MING("pnp.sms.usr.ming"),
	PNP_SMS_PASS_MING("pnp.sms.pass.ming"),
	PNP_SMS_APPCODE_MING("pnp.sms.APPCode.ming"),
	PNP_SMS_RESCODE_MING("pnp.sms.RESCode.ming"),
	PNP_SMS_PROTOCOL_MING("pnp.sms.protocol.ming"),
	//明宣
	//UNICA
	PNP_FTP_SERVERHOSTNAME_UNICA("pnp.ftp.serverHostName.unica"),
	PNP_FTP_SERVERHOSTNAME_PORT_UNICA("pnp.ftp.serverHostName.port.unica"),
	PNP_FTP_HOST_UNICA("pnp.ftp.host.unica"),
	PNP_FTP_PORT_UNICA("pnp.ftp.port.unica"),
	PNP_FTP_USR_UNICA("pnp.ftp.usr.unica"),
	PNP_FTP_PASS_UNICA("pnp.ftp.pass.unica"),
	PNP_FTP_APPCODE_UNICA("pnp.ftp.APPCode.unica"),
	PNP_FTP_RESCODE_UNICA("pnp.ftp.RESCode.unica"),
	PNP_FTP_PROTOCOL_UNICA("pnp.ftp.protocol.unica"),
	PNP_FTP_DOWNLOAD_TO_LOCAL_PATH_UNICA("pnp.ftp.download.to.local.path.unica"),
	PNP_FTP_DOWNLOAD_PATH_UNICA("pnp.ftp.download.path.unica"),
	PNP_PROC_FLOW_UNICA("pnp.proc.flow.unica"),
	
	PNP_SMS_UPLOAD_PATH_UNICA("pnp.sms.upload.path.unica"),
	PNP_SMS_SERVERHOSTNAME_UNICA("pnp.sms.serverHostName.unica"),
	PNP_SMS_SERVERHOSTNAME_PORT_UNICA("pnp.sms.serverHostName.port.unica"),
	PNP_SMS_HOST_UNICA("pnp.sms.host.unica"),
	PNP_SMS_PORT_UNICA("pnp.sms.port.unica"),
	PNP_SMS_USR_UNICA("pnp.sms.usr.unica"),
	PNP_SMS_PASS_UNICA("pnp.sms.pass.unica"),
	PNP_SMS_APPCODE_UNICA("pnp.sms.APPCode.unica"),
	PNP_SMS_RESCODE_UNICA("pnp.sms.RESCode.unica"),
	PNP_SMS_PROTOCOL_UNICA("pnp.sms.protocol.unica"),
	
	//PNP白名單
	PNP_WHITELIST_VALIDATE("pnp.whitelist.validate"),
	PNP_WHITELIST_ACCOUNT_PCCCODE_MITAKE(""),
	PNP_WHITELIST_ACCOUNT_PCCCODE_EVERY8D(""),
	PNP_WHITELIST_ACCOUNT_PCCCODE_MING(""),
	PNP_WHITELIST_ACCOUNT_PCCCODE_UNICA(""),
	PNP_SCHEDULE_TIME("pnp.schedule.time"),
	PNP_SCHEDULE_UNIT("pnp.schedule.unit"),
	PNP_READLINES_ENCODE("pnp.readLines.encode"),
	LINE_PNP_PUSH_VERIFIED("line.pnp.push.verified"),
	
	// Line Point
	LINE_POINT_API_CLIENT_ID("line.point.api.client.id"),
	LINE_POINT_MESSAGE_PUSH_URL("line.point.push.url"),
	LINE_POINT_MESSAGE_CANCEL_URL("line.point.cancel.url"),
	
//	// Oracle Database
	ORACLE_DATASOURCE_URL("oracle.datasource.url"),
	ORACLE_DATASOURCE_USERNAME("oracle.datasource.username"),
	ORACLE_DATASOURCE_PASSWORD("oracle.datasource.password"),
	ORACLE_DATASOURCE_DRIVER_NAME("oracle.datasource.driver.name"),
	ORACLE_SCHEMA_HR("oracle.schema.hr"),
	ORACLE_SCHEMA_CMM("oracle.schema.cmm"),
	
	//PNP監控
//	PNP_PROC_FLOW("pnp.proc.flow"),
	
	//PNP
	;

    private final String str;
    
    CONFIG_STR(String str) {
        this.str = str;
    }
	/**
	 * @return the str
	 */
	public String toString() {
		return str;
	}

}
