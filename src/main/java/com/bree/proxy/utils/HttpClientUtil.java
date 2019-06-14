package com.bree.proxy.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import com.bree.proxy.model.ProxyInfo;
/**
 * httpclient发送http和https请求
 *
 * @author breeze
 */
public class HttpClientUtil {

	 /**
   	 * 发送http或https请求  --DefaultHttpClient方式
   	 * @param httpclient
   	 * @param httprequest
   	 * @return
   	 */
	public static String invoke(DefaultHttpClient httpclient, HttpUriRequest httprequest) throws Exception {
		HttpResponse response = sendRequest(httpclient, httprequest);
		String body = paseResponse(response);
		return body;
	}
	/**
   	 * 解析http或https请求的返回结果
   	 * @param response
   	 * @return
   	 */
	public static String paseResponse(HttpResponse response) throws Exception {
		HttpEntity entity = response.getEntity();
		String charset = EntityUtils.getContentCharSet(entity);
		String body = null;
		body = EntityUtils.toString(entity);
		return body;
	}
	 /**
   	 * 执行http或https请求 --DefaultHttpClient方式
   	 * @param httpclient
   	 * @param httprequest
   	 * @return
   	 */
	public static HttpResponse sendRequest(DefaultHttpClient httpclient, HttpUriRequest httrequest) throws Exception {
		HttpResponse response = null;
		response = httpclient.execute(httrequest);
		return response;
	}
	 /**
   	 * 获取发送http请求的httpclient  --DefaultHttpClient方式
   	 * @param proxy
   	 * @return
   	 */
	public static DefaultHttpClient getHttpClient(ProxyInfo proxy) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials(new AuthScope(proxy.getIp(), proxy.getPort()),
				new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
		HttpHost httpHostProxy = new HttpHost(proxy.getIp(), proxy.getPort());
		httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, httpHostProxy);
		return httpClient;
	}
	 /**
   	 * 获取发送https请求的httpclient  --DefaultHttpClient方式
   	 * @param proxy
   	 * @param timeout 毫秒值
   	 * @return
   	 */
	public static DefaultHttpClient getHttpsClient(ProxyInfo proxy,int timeout) throws Exception {
		SSLSocketFactory sf = new SSLSocketFactory(new TrustStrategy() {
			// 信任所有
			@Override
			public boolean isTrusted(java.security.cert.X509Certificate[] x509Certificates, String s)
					throws java.security.cert.CertificateException {
				return true;
			}
		});
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		// 连接池设置
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, sf));
		PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
		cm.setMaxTotal(200); // 连接池里的最大连接数
		cm.setDefaultMaxPerRoute(20); // 每个路由的默认最大连接数
		// 其它设置
		DefaultHttpClient httpClient = new DefaultHttpClient(cm);
		CookieStore cookieStore = httpClient.getCookieStore();
		// 添加语言cookie
		BasicClientCookie2 langCookie = new BasicClientCookie2("LangKey", "cs");
		langCookie.setVersion(0);
		langCookie.setDomain(proxy.getIp());
		langCookie.setPath("/");
		cookieStore.addCookie(langCookie);
		HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				if (executionCount >= 3) {
					// 如果超过最大重试次数，那么就不要继续了
					return false;
				}
				if (exception instanceof NoHttpResponseException) {
					// 如果服务器丢掉了连接，那么就重试
					return true;
				}
				if (exception instanceof SSLHandshakeException) {
					// 不要重试SSL握手异常
					return false;
				}
				HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
				boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
				if (idempotent) {
					// 如果请求被认为是幂等的，那么就重试
					return true;
				}
				return false;
			}
		};
		httpClient.setHttpRequestRetryHandler(myRetryHandler);
		// 设置读取超时时间
		httpClient.getParams().setIntParameter("http.socket.timeout", timeout);
		// 设置连接超时超时
		httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, timeout);
		// 添加代理
		if (null != proxy) {
			if (proxy.getUsed() == 1) {
				String ip = proxy.getIp();// 代理ip
				int port = proxy.getPort();// 代理端口
				String proxyUserName = proxy.getUsername();// 代理账号用户名
				String proxyPassword = proxy.getPassword();// 代理账号密码
				if (!(ip == null || port <= 0)) {
					// 访问的目标站点，端口和协议
					httpClient.getCredentialsProvider().setCredentials(new AuthScope(ip, port),
							new UsernamePasswordCredentials(proxyUserName, proxyPassword));
					// 代理的设置
					HttpHost proxyHost = new HttpHost(ip, port);
					httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
				}
			}
		}
		return httpClient;
	}
	 /**
   	 * 获取发送https请求的httpclient  --CloseableHttpClient方式
   	 * @param proxy
   	 * @param timeout 毫秒值
   	 * @return
   	 */
	public static CloseableHttpClient createSSLClientDefault(String ip,int timeout){
	    try {
	        CookieStore cookieStore = new BasicCookieStore();
	        // 添加语言cookie
	        BasicClientCookie2 langCookie = new BasicClientCookie2("LangKey", "cs");
	        langCookie.setVersion(0);
	        langCookie.setDomain(ip);
	        langCookie.setPath("/");
	        cookieStore.addCookie(langCookie);
	        RequestConfig config = null;
	        if (config == null) {
	            config = RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build();
	        }
	        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
	            //信任所有
	            @Override
	            public boolean isTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws java.security.cert.CertificateException {
	                return true;
	            }
	        }).build();
	        
	        
	        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);	
	       
	        return HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultCookieStore(cookieStore).setDefaultRequestConfig(config).setRetryHandler((new HttpRequestRetryHandler() {
	            @Override
	            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
	                if (executionCount >= 3) {
	                    // 如果超过最大重试次数，那么就不要继续了
	                    return false;
	                }
	                if (exception instanceof NoHttpResponseException) {
	                    // 如果服务器丢掉了连接，那么就重试
	                    return true;
	                }
	                if (exception instanceof SSLHandshakeException) {
	                    // 不要重试SSL握手异常
	                    return false;
	                }
	                HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
	                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
	                if (idempotent) {
	                    // 如果请求被认为是幂等的，那么就重试
	                    return true;
	                }
	                return false;
	            }
	        })).build();
	    } catch (KeyManagementException e) {
	        e.printStackTrace();
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    } catch (KeyStoreException e) {
	        e.printStackTrace();
	    }
	    return  HttpClients.createDefault();
	}
	
	/**
   	 * 发送https或http请求  --CloseableHttpClient方式
   	 * @param proxy 代理服务器的配置
   	 * @param requestUrl 请求的目标地址
   	 * @return
   	 */
	public static String closeableHttpClientRequest(ProxyInfo proxy,String requestUrl) throws Exception{
		String result = null;
		// 1.获取HttpClient
		CloseableHttpClient closeableHttpClient = HttpClientUtil.createSSLClientDefault(proxy.getIp(), 5000);

		//2.设置代理服务器
		HttpClientContext context = null;
		RequestConfig config = null;
		if(proxy != null && proxy.getUsed() == 1){
			HttpHost proxyPost = new HttpHost(proxy.getIp(), proxy.getPort(), "http");  
			 
			BasicScheme proxyAuth = new BasicScheme();
			proxyAuth.processChallenge(new BasicHeader(AUTH.PROXY_AUTH, "BASIC realm=default"));
			BasicAuthCache authCache = new BasicAuthCache();
			authCache.put(proxyPost, proxyAuth);
			 
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
			        new AuthScope(proxyPost),
			        new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
			 
			context = HttpClientContext.create();
			context.setAuthCache(authCache);
			context.setCredentialsProvider(credsProvider);
			
			config = RequestConfig.custom().setProxy(proxyPost).build();  
		}
		
		// 3.发送请求
		HttpGet httpGet = new HttpGet(requestUrl);
		httpGet.setConfig(config);

		// 创建参数队列
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		// 参数名为pid，值是2
		formparams.add(new BasicNameValuePair("pid", "1"));
		
		//4.处理响应
		CloseableHttpResponse response = closeableHttpClient.execute(httpGet, context);
		HttpEntity httpEntity = response.getEntity();
		if (httpEntity != null) {
			result = EntityUtils.toString(httpEntity, "UTF-8");
			
		}
		// 释放资源
		closeableHttpClient.close();
		return result;
	}

}
