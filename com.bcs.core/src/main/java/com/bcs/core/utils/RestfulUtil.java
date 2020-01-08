package com.bcs.core.utils;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;

public class RestfulUtil {
	private static Logger logger = Logger.getLogger(RestfulUtil.class);
	
	private RestTemplate restTemplate;
	private HttpMethod method;
	private String url;
	private String statusCode;
	private HttpEntity<?> httpEntity;
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
		Boolean useProxy = true;
		
		this.initialize(useProxy);
	}
	
	public RestfulUtil(HttpMethod method, String url, HttpEntity<?> httpEntity) throws NoSuchAlgorithmException, KeyManagementException {
		this.method = method;
		this.url = url;
		this.httpEntity = httpEntity;
		Boolean useProxy = true;
		
		this.initialize(useProxy);
	}
	
	public RestfulUtil(HttpMethod method, String url, HttpEntity<?> httpEntity, Boolean useProxy) throws NoSuchAlgorithmException, KeyManagementException {
		this.method = method;
		this.url = url;
		this.httpEntity = httpEntity;
		
		this.initialize(useProxy);
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
	
	public String getStatusCode() {
		return statusCode;
	}
	
	public JSONObject execute() throws Exception {
		try {
			logger.info("---------- Start to execute the request ----------");
			
			logger.info("[RestUtil execute] Target url: " + url);
			logger.info("[RestUtil execute] Request body: " + DataUtils.toPrettyJsonUseJackson(httpEntity.getBody()));

			ResponseEntity<String> gatewayResponse = this.restTemplate.exchange(url, method, httpEntity, String.class);
			
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
			logger.info("[RestUtil execute] Status code: " + e.getStatusCode());
			logger.info("[RestUtil execute] Response body: " + e.getResponseBodyAsString());
			
			logger.error(ErrorRecord.recordError(e));
			throw e;
		} catch (HttpServerErrorException e) {
			logger.info("[RestUtil execute] Status code: " + e.getStatusCode());
			logger.info("[RestUtil execute] Response body: " + e.getResponseBodyAsString());
			
			logger.error(ErrorRecord.recordError(e));
			throw e;
		} catch (Exception e) {
			logger.info("[RestUtil execute] Exception: " + e.getMessage());
			logger.error(ErrorRecord.recordError(e));
			
			throw e;
		}
	}
	
	private void initialize(Boolean useProxy) throws NoSuchAlgorithmException, KeyManagementException {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		
		/* ---------- Always trust any certificate ---------- */
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init( null, UNQUESTIONING_TRUST_MANAGER, null );
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
		String proxyUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_PROXY_URL.toString(), true); // Proxy Server 的位置
		
		if (useProxy && StringUtils.isNotBlank(proxyUrl)) {
			logger.info("[RestUtil execute] Use proxy and proxy url is: " + proxyUrl);
			clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.setProxy(new HttpHost(proxyUrl, 80, "http")).build());
		} else {
			clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());
		}
		
		this.restTemplate = new RestTemplate(clientHttpRequestFactory);
	}
}