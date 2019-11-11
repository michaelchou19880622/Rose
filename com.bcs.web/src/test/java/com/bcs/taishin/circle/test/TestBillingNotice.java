package com.bcs.taishin.circle.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.util.SystemOutLogger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.SystemPropertyUtils;

import com.bcs.core.api.test.SpringJUnit4BaseTester;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.RecordReport;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.RecordReportService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpRepositoryCustom;
import com.bcs.core.taishin.circle.PNP.scheduler.LoadFtpPnpDataTask;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpPushMsgService;
import com.bcs.core.taishin.circle.PNP.scheduler.PnpSMSMsgService;
import com.bcs.core.taishin.circle.PNP.service.PnpService;
//import com.bcs.core.taishin.circle.BillingNoticeDataParseService;
//import com.bcs.core.taishin.circle.BillingNoticeFTPService;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.service.BillingNoticeFtpDetail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/spring-base.xml", "classpath:spring/spring-security.xml"})

public class TestBillingNotice {
}
