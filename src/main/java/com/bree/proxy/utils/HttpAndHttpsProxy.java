package com.bree.proxy.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.Proxy.Type;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;

import com.bree.proxy.model.ProxyInfo;

public class HttpAndHttpsProxy {

	public static void main(String[] args) {

		// String url =
		// "https://fpcy.jiangsu.chinatax.gov.cn:80/WebQuery/yzmQuery?callback=jQuery110209213418538287719_1559285746349&fpdm=3200184130&fphm=02670770&r=0.3771476808141081&v=V1.0.07_001&nowtime=1559809971767&area=3200&publickey=F5E7A0387DBAD5E6EA113225455A975A&_=1559285746358";
		 String url =
		 "https://fpcy.dlntax.gov.cn/WebQuery/yzmQuery?callback=jQuery110209213418538287719_1559285746349&fpdm=2102184130&fphm=02670770&r=0.00982363574892764&v=V1.0.07_001&nowtime=1559800014984&area=2102&publickey=4D40DF1E0BDE8E4E23B1ADDAB06FB2A4&_=1559285746355";
		//String url = "http://47.94.37.219:5000/myip.do";
		String params = "";

		String proxyHost = "39.106.71.9";
	    int proxyPort = 3762;
		
		//String proxyHost = "112.85.130.167";
		//int proxyPort = 9999;
		
		//String proxyHost = "192.168.144.225";
		//int proxyPort = 3128;
		
		String userName = "proxytest";
		String password = "tyy2019";
		
		//String userName = "airoot";
		//String password = "123456";

		ProxyInfo proxy = new ProxyInfo(1, proxyHost, proxyPort, userName, password);

		//String getResult = HttpAndHttpsProxy.httpProxy(url, params, proxy);
		String getResult = HttpAndHttpsProxy.httpsProxy(url, params, proxy);
		System.out.println(getResult);
	}

	public static String httpsProxy(String url, String param, ProxyInfo proxyInfo) {
		HttpsURLConnection httpsConn = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		BufferedReader reader = null;
				
		try {
			URL urlClient = new URL(url);
			System.out.println("请求的URL========：" + urlClient);

			SSLContext sc = SSLContext.getInstance("SSL");
			// 指定信任https
			sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
			// 创建代理虽然是https也是Type.HTTP
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyInfo.getIp(), proxyInfo.getPort()));
	
			httpsConn = (HttpsURLConnection) urlClient.openConnection(proxy);
			
			// 设置代理
			String nameAndPass = proxyInfo.getUsername() +":"+ proxyInfo.getPassword();
			String encoding = new String(Base64.encodeBase64(nameAndPass.getBytes()));
			httpsConn.setRequestProperty("Proxy-Authorization","Basic " + encoding);
			Authenticator.setDefault(new MyAuthenticator(proxyInfo.getUsername(), proxyInfo.getPassword()));
						
			// 设置通用的请求属性
			
			httpsConn.setRequestMethod("GET");  

			httpsConn.setRequestProperty("accept", "*/*");
			httpsConn.setRequestProperty("connection", "Keep-Alive");
			httpsConn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			httpsConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			
			// 发送POST请求必须设置如下两行
			//httpsConn.setDoOutput(true);
			//httpsConn.setDoInput(true);
						
			httpsConn.setSSLSocketFactory(sc.getSocketFactory());
			httpsConn.setHostnameVerifier(new TrustAnyHostnameVerifier());
			
			String sss = httpsConn.getRequestProperty("Proxy-Authorization");
			Map map = httpsConn.getRequestProperties();
			System.out.println(sss);
			System.out.println(map);
			
			httpsConn.connect();
			
			
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(httpsConn.getOutputStream());
			// 发送请求参数
			out.print(param);
			//out.write("Proxy-Authorization: Basic cHJveHl0ZXN0OnR5eTIwMTk=");
			// flush输出流的缓冲
			out.flush();
			
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(httpsConn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			// 断开连接
			httpsConn.disconnect();
			System.out.println("====result====" + result);
			System.out.println("返回结果：" + httpsConn.getResponseMessage());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (out != null) {
				out.close();
			}
		}

		return result;
	}

	public static String httpProxy(String url, String param, ProxyInfo proxyInfo) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url;
			if (param != null && !("".equals(param)))
				urlNameString = url + "?" + param;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			HttpURLConnection connection = null;
			if (proxyInfo != null && proxyInfo.getUsed() == 1) {// 使用代理模式
				@SuppressWarnings("static-access")
				Proxy proxy = new Proxy(Proxy.Type.DIRECT.HTTP, new InetSocketAddress(proxyInfo.getIp(), proxyInfo.getPort()));
				Authenticator.setDefault(new MyAuthenticator(proxyInfo.getUsername(), proxyInfo.getPassword()));
				connection = (HttpURLConnection) realUrl.openConnection(proxy);
			} else {
				connection = (HttpURLConnection) realUrl.openConnection();
			}

			// 设置通用的请求属性
			for (Map.Entry<String, String> entry : setProperty().entrySet()) {
				connection.setRequestProperty(entry.getKey(), entry.getValue());
			}
			
			String nameAndPass = proxyInfo.getUsername() +":"+ proxyInfo.getPassword();
			String encoding = new String(Base64.encodeBase64(nameAndPass.getBytes()));
			
			connection.setRequestProperty("Proxy-Authorization","Basic " + encoding);
			// 建立连接
			connection.connect();
			// 定义 BufferedReader输入流来读取URL的响应
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK
					|| connection.getResponseCode() == HttpURLConnection.HTTP_CREATED
					|| connection.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED) {
				in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			} else {
				in = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
			}
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			connection.disconnect();
		} catch (Exception e) {
			System.out.println("发送GET请求出现异常！");
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}
	
	// 设置请求头属性
			public static Map<String, String> setProperty() {
				HashMap<String, String> pMap = new HashMap<>();
				// pMap.put("Accept-Encoding", "gzip"); //请求定义gzip,响应也是压缩包
				pMap.put("accept", "*/*");
				pMap.put("connection", "Keep-Alive");
				pMap.put("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
				pMap.put("Content-Type", "application/x-www-form-urlencoded");
				return pMap;
			}
	
	public static SSLContext MyX509TrustManagerUtils() {

		TrustManager[] tm = { new HttpAndHttpsProxy().new MyX509TrustManager() };
		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(null, tm, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ctx;
	}
	
	/*
	 * HTTPS忽略证书验证,防止高版本jdk因证书算法不符合约束条件,使用继承X509ExtendedTrustManager的方式
	 */
	class MyX509TrustManager extends X509ExtendedTrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2) throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

	}
	
	static class MyAuthenticator extends Authenticator {
	    private String user = "";
	    private String password = "";
	  
	    public MyAuthenticator(String user, String password) {
	      this.user = user;
	      this.password = password;
	    }
	  
	    protected PasswordAuthentication getPasswordAuthentication() {
	      return new PasswordAuthentication(user, password.toCharArray());
	    }
	  }

	private static class TrustAnyTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}

	private static class TrustAnyHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

}
