package com.bcs.web.report.controller;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;

@Controller
@RequestMapping("/bcs")
public class BCSConnectionController extends BCSBaseController {
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSConnectionController.class);
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/connectionTestPage")
	public String connectionTestPage(@CurrentUser CustomUser customUser, HttpServletRequest request, HttpServletResponse response) {
		logger.info("connectionTestPage");
		
		return BcsPageEnum.ConnectionTestPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/admin/sendRequest", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> sendRequest(HttpServletRequest request, HttpServletResponse response, @RequestBody String requestBody) throws KeyManagementException {
		logger.info("sendRequest");
		
		ResponseEntity<String> result = null;
		try {
			JSONObject requestObject = new JSONObject(requestBody);
			String targetUrl = requestObject.getString("targetUrl");
			String requestMethod = requestObject.getString("requestMethod");
			Boolean useProxy = requestObject.getBoolean("useProxy");
			
			HttpMethod method = null;
			HttpEntity<String> httpEntity = null;
			HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = null;
			HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
			TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[]{
					new X509TrustManager() {
			            public java.security.cert.X509Certificate[] getAcceptedIssuers(){
			                return null;
			            }
			            public void checkClientTrusted( X509Certificate[] certs, String authType ){}
			            public void checkServerTrusted( X509Certificate[] certs, String authType ){}
			        }
				};
			
			/* ---------- Always trust any certificate ---------- */
			SSLContext sslContext = SSLContext.getInstance("SSL");
			
			sslContext.init( null, UNQUESTIONING_TRUST_MANAGER, null );
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
			
			httpClientBuilder.setSSLSocketFactory(csf);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			
			if(useProxy) {
				String proxyHost = requestObject.getString("proxyHost");
				Integer proxyPort = requestObject.getInt("proxyPort");
				
				clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.setProxy(new HttpHost(proxyHost, proxyPort, "http")).build());
			} else {
				clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());
			}
			
			if(requestMethod.equals("get")) {
				method = HttpMethod.GET;
				httpEntity = new HttpEntity<String>(headers);
			} else if(requestMethod.equals("post")) {
				method = HttpMethod.POST;
				httpEntity = new HttpEntity<String>(requestObject.getString("body"), headers);
			}
			
			RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
			
			result = restTemplate.exchange(targetUrl, method, httpEntity, String.class);
			
			return new ResponseEntity<>(result.getBody(), result.getStatusCode());
		} catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), (result != null) ? result.getStatusCode() : HttpStatus.BAD_REQUEST);
		}
	}
}
