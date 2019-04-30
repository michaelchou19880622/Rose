package com.bcs.core.taishin.circle.PNP.plugin;

import javax.net.ssl.*;
import java.security.*;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;

public class RestfulUtil {
	private static Logger logger = Logger.getLogger(RestfulUtil.class);
	
	private HttpMethod method;
	private String url;
	private Boolean useProxy;
	private String statusCode;
	private HttpEntity<?> httpEntity;
	private HttpClientBuilder httpClientBuilder;
	private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[]{
			new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers(){
	                return null;
	            }
	            public void checkClientTrusted( X509Certificate[] certs, String authType ){}
	            public void checkServerTrusted( X509Certificate[] certs, String authType ){}
	        }
		};
	
	public RestfulUtil() throws NoSuchAlgorithmException, KeyManagementException {
		this.useProxy = true;
		
		httpClientBuilder = HttpClientBuilder.create();
		
		/* ---------- Always trust any certificate ---------- */
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init( null, UNQUESTIONING_TRUST_MANAGER, null );
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
		sslContext.init( null, UNQUESTIONING_TRUST_MANAGER, null );
		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
		
		httpClientBuilder.setSSLSocketFactory(csf);
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setHttpEntity(HttpEntity<?> httpEntity) {
		this.httpEntity = httpEntity;
	}
	
	public void setUseProxy(Boolean useProxy) {
		this.useProxy = useProxy;
	}

	public String getStatusCode() {
		return statusCode;
	}
	
	public JSONObject execute(){
		try {
			logger.info("---------- Start to execute the request ----------");
			
			HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = null;
			String proxyUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_PROXY_URL.toString(), true); // Proxy Server 的位置
			
			logger.info("[RestUtil execute] Target url: " + url);
			
			if (useProxy && StringUtils.isNotBlank(proxyUrl)) {
				logger.info("[RestUtil execute] Use proxy and proxy url is: " + proxyUrl);
				clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.setProxy(new HttpHost(proxyUrl, 80, "http")).build());
			} else {
				clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());
			}
			RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
			ResponseEntity<String> gatewayResponse = restTemplate.exchange(url, method, httpEntity, String.class);
			
			String responseBody = gatewayResponse.getBody(); // 取得 response body
			statusCode = gatewayResponse.getStatusCode().toString();
			
			logger.info("[RestUtil execute] Status code: " + statusCode);
			logger.info("[RestUtil execute] Response body: " + responseBody);
			
			if(responseBody!=null){
				int i = responseBody.indexOf("{");
				responseBody = responseBody.substring(i);
			}
			
			return responseBody == null ? null : new JSONObject(responseBody);
		} catch (HttpClientErrorException e) {
			logger.error(ErrorRecord.recordError(e));
			throw e;
		}
	}
}