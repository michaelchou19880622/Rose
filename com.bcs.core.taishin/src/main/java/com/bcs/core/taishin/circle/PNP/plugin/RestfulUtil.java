package com.bcs.core.taishin.circle.PNP.plugin;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Getter
@Setter
@Slf4j(topic = "PnpRecorder")
public class RestfulUtil {

    private HttpMethod method;
    private String url;
    private boolean useProxy;
    private String statusCode;
    private HttpEntity<?> httpEntity;
    private HttpClientBuilder httpClientBuilder;
    private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
    };

    public RestfulUtil() throws NoSuchAlgorithmException, KeyManagementException {
        this.useProxy = true;

        httpClientBuilder = HttpClientBuilder.create();

        /* ---------- Always trust any certificate ---------- */
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, UNQUESTIONING_TRUST_MANAGER, null);
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        httpClientBuilder.setSSLSocketFactory(csf);
    }

    public RestfulUtil(HttpMethod method, String url, HttpEntity<?> httpEntity) throws NoSuchAlgorithmException, KeyManagementException {
        this.method = method;
        this.url = url;
        this.httpEntity = httpEntity;
        this.useProxy = true;

        httpClientBuilder = HttpClientBuilder.create();

        /* ---------- Always trust any certificate ---------- */
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, UNQUESTIONING_TRUST_MANAGER, null);
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        httpClientBuilder.setSSLSocketFactory(csf);
    }

    public JSONObject execute() {
        try {
            log.info("---------- Start to execute the request ----------");

            HttpComponentsClientHttpRequestFactory clientHttpRequestFactory;
            // Proxy Server 的位置
            String proxyUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_PROXY_URL.toString(), true);

            log.info("[RestUtil execute] Target url: " + url);

            if (useProxy && StringUtils.isNotBlank(proxyUrl)) {
                log.info("[RestUtil execute] Use proxy and proxy url is: " + proxyUrl);
                clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.setProxy(new HttpHost(proxyUrl, 80, "http")).build());
            } else {
                clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());
            }
            RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
            ResponseEntity<String> gatewayResponse = restTemplate.exchange(url, method, httpEntity, String.class);

            // 取得 response body
            String responseBody = gatewayResponse.getBody();
            statusCode = gatewayResponse.getStatusCode().toString();

            log.info("[RestUtil execute] Status code: " + statusCode);
            log.info("[RestUtil execute] Response body: " + responseBody);

            if (responseBody != null) {
                int i = responseBody.indexOf('{');
                responseBody = responseBody.substring(i);
            }

            return responseBody == null ? null : new JSONObject(responseBody);
        } catch (HttpClientErrorException e) {
            log.error(ErrorRecord.recordError(e));
            throw e;
        }
    }
}