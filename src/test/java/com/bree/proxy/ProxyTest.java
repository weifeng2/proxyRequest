package com.bree.proxy;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.bree.proxy.model.ProxyInfo;
import com.bree.proxy.utils.HttpClientUtil;

public class ProxyTest {

	public static void main(String[] args) {
		try {
			//proxyHttpTest();
			// proxyHttpsTest();
			closeableHttpClientTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 测试发送https或http请求  ---CloseableHttpClient方式
	 * 
	 * @return
	 */
	public static void closeableHttpClientTest() throws Exception{
		//String proxyHost = "112.85.130.233";
		//int proxyPort = 9999;
		String proxyHost = "39.106.71.9";
		int proxyPort = 3762;
		
		//String proxyHost = "127.0.0.1";
		//int proxyPort = 8888;
		
		
		String userName = "proxytest";
		String password = "tyy2019";
		//String url = "https://fpcy.jiangsu.chinatax.gov.cn:80/WebQuery/yzmQuery?callback=jQuery110209213418538287719_1559285746349&fpdm=3200184130&fphm=02670770&r=0.3771476808141081&v=V1.0.07_001&nowtime=1559809971767&area=3200&publickey=F5E7A0387DBAD5E6EA113225455A975A&_=1559285746358";
		String url = "https://fpcy.dlntax.gov.cn/WebQuery/yzmQuery?callback=jQuery110209213418538287719_1559285746349&fpdm=2102184130&fphm=02670770&r=0.00982363574892764&v=V1.0.07_001&nowtime=1559800014984&area=2102&publickey=4D40DF1E0BDE8E4E23B1ADDAB06FB2A4&_=1559285746355";
		//String url = "http://47.94.37.219:5000/myip.do";

		ProxyInfo proxy = new ProxyInfo(1, proxyHost, proxyPort, userName, password);
		String  result = HttpClientUtil.closeableHttpClientRequest(proxy,url);
		System.out.println(result);
	}
	

	/**
	 * 测试发送http请求 ---DefaultHttpClient方式
	 * 
	 * @return
	 */
	public static void proxyHttpTest() throws Exception {

		String proxyHost = "39.106.71.9";
		int proxyPort = 3762;
		String userName = "proxytest";
		String password = "tyy2019";
		ProxyInfo proxy = new ProxyInfo(1, proxyHost, proxyPort, userName, password);
		DefaultHttpClient httpClient = HttpClientUtil.getHttpClient(proxy);

		HttpUriRequest request = new HttpGet("http://47.94.37.219:5000/myip.do");

		HttpResponse reponse = HttpClientUtil.sendRequest(httpClient, request);
		String result = HttpClientUtil.paseResponse(reponse);
		System.out.println(result);
	}

	/**
	 * 测试发送https请求  ---DefaultHttpClient方式
	 * 
	 * @return
	 */
	public static void proxyHttpsTest() throws Exception {

		String proxyHost = "39.106.71.9";
		int proxyPort = 3762;
		String userName = "proxytest";
		String password = "tyy2019";

		ProxyInfo proxy = new ProxyInfo(1, proxyHost, proxyPort, userName, password);

		DefaultHttpClient httpClient = HttpClientUtil.getHttpsClient(proxy, 5000);
		//HttpUriRequest request = new
		//HttpGet("https://fpcy.dlntax.gov.cn/WebQuery/yzmQuery?callback=jQuery110209213418538287719_1559285746349&fpdm=2102184130&fphm=02670770&r=0.00982363574892764&v=V1.0.07_001&nowtime=1559800014984&area=2102&publickey=4D40DF1E0BDE8E4E23B1ADDAB06FB2A4&_=1559285746355");
		HttpUriRequest request = new HttpGet(
				"https://fpcy.jiangsu.chinatax.gov.cn:80/WebQuery/yzmQuery?callback=jQuery110209213418538287719_1559285746349&fpdm=3200184130&fphm=02670770&r=0.3771476808141081&v=V1.0.07_001&nowtime=1559809971767&area=3200&publickey=F5E7A0387DBAD5E6EA113225455A975A&_=1559285746358");

		HttpResponse reponse = HttpClientUtil.sendRequest(httpClient, request);
		String result = HttpClientUtil.paseResponse(reponse);
		System.out.println(result);
	}
}
