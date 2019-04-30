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
	
	PageMobile("bcs.base.url.page.mobile"),
	ResourceMobile("bcs.base.url.resource.mobile"),
	
	PageBCS("bcs.base.url.page.bcs"),
	ResourceBCS("bcs.base.url.resource.bcs"),
	
	FilePath("file.path"),
	
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
	AES_SECRET_KEY("aes.secret.key"),
	AES_INITIALIZATION_VECTOR("aes.initialization.vector"),

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
