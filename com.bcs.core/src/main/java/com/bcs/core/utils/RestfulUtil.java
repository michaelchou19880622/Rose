package com.bcs.core.utils;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * @author ???
 */
@Slf4j
public class RestfulUtil {

    private RestTemplate restTemplate;
    private HttpMethod method;
    private String url;
    private String statusCode;
    private HttpEntity<?> httpEntity;
    private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
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
        this.initialize(true);
    }

    public RestfulUtil(HttpMethod method, String url, HttpEntity<?> httpEntity) throws NoSuchAlgorithmException, KeyManagementException {
        this.method = method;
        this.url = url;
        this.httpEntity = httpEntity;

        this.initialize(true);
    }

    public RestfulUtil(HttpMethod method, String url, HttpEntity<?> httpEntity, Boolean useProxy) throws NoSuchAlgorithmException, KeyManagementException {
        this.method = method;
        this.url = url;
        this.httpEntity = httpEntity;

        this.initialize(useProxy);
    }

    //GetterSetter

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHttpEntity(HttpEntity<?> httpEntity) {
        this.httpEntity = httpEntity;
    }

    public String getStatusCode() {
        return statusCode;
    }

    // Method

    public JSONObject execute() {
        try {
            log.info("---------- Start to execute the request ----------");

            log.info("[RestUtil execute] Target url: " + url);
            // log.info("[RestUtil execute] Request body: " + DataUtils.toPrettyJsonUseJackson(httpEntity.getBody()));

            ResponseEntity<String> gatewayResponse = this.restTemplate.exchange(url, method, httpEntity, String.class);

            String responseBody = gatewayResponse.getBody();

            statusCode = gatewayResponse.getStatusCode().toString();

            log.info("[RestUtil execute] Status code: " + statusCode);
            log.info("[RestUtil execute] Response body: " + responseBody);

            if (responseBody != null) {
                int i = responseBody.indexOf("{");
                responseBody = responseBody.substring(i);
            }

            return responseBody == null ? null : new JSONObject(responseBody);
        } catch (HttpClientErrorException e) {
            log.info("[RestUtil execute] Status code: " + e.getStatusCode());
            log.info("[RestUtil execute] Response body: " + e.getResponseBodyAsString());

            log.error(ErrorRecord.recordError(e));
            throw e;
        } catch (HttpServerErrorException e) {
            log.info("[RestUtil execute] Status code: " + e.getStatusCode());
            log.info("[RestUtil execute] Response body: " + e.getResponseBodyAsString());

            log.error(ErrorRecord.recordError(e));
            throw e;
        } catch (Exception e) {
            log.info("[RestUtil execute] Exception: " + e.getMessage());
            log.error(ErrorRecord.recordError(e));

            throw e;
        }
    }

    private void initialize(boolean useProxy) throws NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        /* ---------- Always trust any certificate ---------- */
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, UNQUESTIONING_TRUST_MANAGER, null);
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", csf)
                .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        connectionManager.setMaxTotal(1000);
        connectionManager.setDefaultMaxPerRoute(connectionManager.getMaxTotal());

        httpClientBuilder.setConnectionManager(connectionManager);

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = null;
        String proxyUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_PROXY_URL.toString(), true);

        if (useProxy && StringUtils.isNotBlank(proxyUrl)) {
            log.info("[RestUtil execute] Use proxy and proxy url is: " + proxyUrl);
            clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.setProxy(new HttpHost(proxyUrl, 80, "http")).build());
        } else {
            clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());
        }

        this.restTemplate = new RestTemplate(clientHttpRequestFactory);
    }
}