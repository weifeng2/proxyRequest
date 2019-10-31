package com.bree.proxy.utils;
 
    /**
    * Created with IntelliJ IDEA.
    * User: victor
    * Date: 13-10-11
    * Time: 下午3:09
    * To change this template use File | Settings | File Templates.
    */
 
    import java.io.File;
 
    import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
 
    import org.apache.http.HttpEntity;
 
    import org.apache.http.client.methods.CloseableHttpResponse;
 
    import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLContexts;
 
    import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
 
    import org.apache.http.impl.client.CloseableHttpClient;
 
    import org.apache.http.impl.client.HttpClients;
 
    import org.apache.http.util.EntityUtils;
 
 
 
    /**
    * 代码展示了如果使用ssl context创建安全socket连接
    */
 
    public class HttpClientLoadCert {
 
        public final static void main(String[] args) throws Exception {
 
            KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
 
            //加载证书文件
 
            FileInputStream instream = new FileInputStream(new File("e://my.store"));
 
            try {
 
                trustStore.load(instream, "123456".toCharArray());
 
            } finally {
 
                instream.close();
 
            }
 
            SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore).build();
 
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
 
                    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            
            
 
            CloseableHttpClient httpclient = HttpClients.custom()
 
                    .setSSLSocketFactory(sslsf)
 
                    .build();
 
            try
 
            {
 
                //访问支付宝
                //遇到证书解析报错时增加下面部分代码
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
            		     
            	HttpPost httppost = new HttpPost("https://fpdk.ningxia.chinatax.gov.cn/login.do");
 
                System.out.println("executing request" + httppost.getRequestLine());
 
                CloseableHttpResponse response = httpclient.execute(httppost);
 
                try {
 
                    HttpEntity entity = response.getEntity();
 
                    System.out.println("----------------------------------------");
 
                    System.out.println(response.getStatusLine());
 
                    if (entity != null) {
 
                        System.out.println(EntityUtils.toString(entity));
 
                    }
 
                }catch(Exception e){
                	e.printStackTrace();
                } finally {
 
                    response.close();
 
                }
 
            }catch(Exception e){
            	e.printStackTrace();
            } finally {
 
                httpclient.close();
 
            }
 
        }
 
    }