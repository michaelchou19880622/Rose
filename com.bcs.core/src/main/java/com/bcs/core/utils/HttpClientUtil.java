package com.bcs.core.utils;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class HttpClientUtil {
    /**
     * Logger
     */

    private static Logger logger = Logger.getLogger(HttpClientUtil.class);

    private static final String INIT_FLAG = "INIT_FLAG";

    private static List<CloseableHttpClient> httpClientList = new ArrayList<>();
    private final static int timeout = 1;

    public static CloseableHttpClient getSingleInstance() {
        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(timeout * 20000)
                .setConnectTimeout(timeout * 20000)
                .setConnectionRequestTimeout(timeout * 20000).build();

        HttpClientBuilder builder = HttpClientBuilder.create().setDefaultRequestConfig(config);

        try {
            String proxyUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_PROXY_URL.toString(), true);
            if (StringUtils.isNotBlank(proxyUrl)) {
                HttpHost proxy = new HttpHost(proxyUrl, 80, "http");
                builder.setProxy(proxy);
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        return builder.build();
    }

    public static HttpClient generateClient() throws Exception {
        synchronized (INIT_FLAG) {
            if (httpClientList == null) {
                httpClientList = new ArrayList<>();
            }

            if (httpClientList.isEmpty()) {
                for (int i = 0; i < 10; i++) {
                    httpClientList.add(createClient());
                }
            }
        }

        return randomClient();
    }

    public static void clearData() {
        synchronized (INIT_FLAG) {
            try {
                for (CloseableHttpClient client : httpClientList) {
                    client.close();
                }
                httpClientList.clear();
            } catch (Exception e) {
                logger.error(ErrorRecord.recordError(e));
            }
        }
    }

    private static CloseableHttpClient randomClient() {
        logger.debug("randomClient Size:" + httpClientList.size());

        int index = new Random().nextInt(httpClientList.size());
        return httpClientList.get(index);
    }

    private static CloseableHttpClient createClient() throws Exception {

        SSLContext sslContext = SSLContext.getInstance("SSL");

        // set up a TrustManager that trusts everything
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                System.out.println("getAcceptedIssuers =============" + Calendar.getInstance().getTime().toString());
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
                System.out.println("checkClientTrusted =============" + Calendar.getInstance().getTime().toString());
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
                System.out.println("checkServerTrusted =============" + Calendar.getInstance().getTime().toString());
            }
        }}, new SecureRandom());

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslsf)
                .build();

        // PoolingHttpClientConnectionManager
        try (PoolingHttpClientConnectionManager pm = new PoolingHttpClientConnectionManager(socketFactoryRegistry)) {
            pm.setMaxTotal(1200);
            pm.setDefaultMaxPerRoute(300);

            RequestConfig.Builder builder = RequestConfig.custom();
            builder.setSocketTimeout(timeout * 20000)
                    .setConnectTimeout(timeout * 20000)
                    .setConnectionRequestTimeout(timeout * 20000);

            try {
                String proxyUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_PROXY_URL.toString(), true);
                if (StringUtils.isNotBlank(proxyUrl)) {
                    HttpHost proxy = new HttpHost(proxyUrl, 80, "http");
                    builder.setProxy(proxy);
                }
            } catch (Exception e) {
                logger.error(ErrorRecord.recordError(e));
            }

            RequestConfig defaultRequestConfig = builder.build();

            return HttpClients.custom().setConnectionManager(pm).setDefaultRequestConfig(defaultRequestConfig).build();
        }
    }
}