package com.bree.proxy.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ssl.SSLContexts;

import com.bree.proxy.model.ProxyInfo;

public class HttpsURLConnectionLoadCert {

	public static void main(String[] args) {
     try {
		 Method add = sun.security.ec.CurveDB.class.getDeclaredMethod("add", String.class,String.class,
		 int.class, String.class,String.class, String.class,String.class, 
		 String.class,String.class,int.class,Pattern.class);
		 add.setAccessible(true);
		 Pattern localPattern = Pattern.compile(",|\\[|\\]");
		 String p="FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF";
		 String a="FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC";
		 String b="28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93";
		 String n="FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123";
		 String gx="32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7";
		 String gy="BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0";
		 add.invoke(null,"SM2", "1.2.156.10197.1.301", 1, p, a, b, n, gx, gy, 1, localPattern);
	} catch (Exception e) {
		e.printStackTrace();
	}
		String url = "https://fpdk.guangxi.chinatax.gov.cn/login.do?";
		String params = "";
		
		String getResult = HttpsURLConnectionLoadCert.httpsProxy(url, params);
		System.out.println(getResult);
	}

	public static String httpsProxy(String url, String param) {
		HttpsURLConnection httpsConn = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		BufferedReader reader = null;
				
		try {
			URL urlClient = new URL(url);
			System.out.println("请求的URL========：" + urlClient);
			
			KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
			 
            //加载证书文件
 
            FileInputStream instream = new FileInputStream(new File("e://my.store"));
 
            try {
 
                trustStore.load(instream, "123456".toCharArray());
 
            } finally {
 
                instream.close();
 
            }
			SSLContext sc = SSLContexts.custom().loadTrustMaterial(trustStore).build();
			// 指定信任https
			//sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());	
			httpsConn = (HttpsURLConnection) urlClient.openConnection();
									
			// 设置通用的请求属性
			httpsConn.setRequestMethod("POST");  
			httpsConn.setRequestProperty("accept", "*/*");
			httpsConn.setRequestProperty("connection", "Keep-Alive");
			httpsConn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			httpsConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			
			// 发送POST请求必须设置如下两行
			httpsConn.setDoOutput(true);
			httpsConn.setDoInput(true);
						
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

		TrustManager[] tm = { new HttpsURLConnectionLoadCert().new MyX509TrustManager() };
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
