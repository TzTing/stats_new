package com.bright.common.security;

import org.jasig.cas.client.ssl.HttpURLConnectionFactory;
import org.jasig.cas.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * @author: Tz
 * @Date: 2023/05/30 18:24
 */
public class CommonUtilsExt {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

    static {
        disableSslVerification();
    }

    private static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
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
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static String getResponseFromServer(URL constructedUrl, HttpURLConnectionFactory factory, String encoding) {

        HttpURLConnection conn = null;
        InputStreamReader in = null;

        try {

            // 原来的写法。
            //conn = factory.buildHttpURLConnection(constructedUrl.openConnection());
            // 新的写法
            conn = (HttpURLConnection) constructedUrl.openConnection();
            if (CommonUtils.isEmpty(encoding)) {
                in = new InputStreamReader(conn.getInputStream());
            } else {
                in = new InputStreamReader(conn.getInputStream(), encoding);
            }

            StringBuilder builder = new StringBuilder(255);

            int byteRead;
            while ((byteRead = in.read()) != -1) {
                builder.append((char) byteRead);
            }
            String var7 = builder.toString();
            return var7;
        } catch (RuntimeException var13) {
            throw var13;
        } catch (SSLException var14) {
            LOGGER.error("SSL error getting response from host: {} : Error Message: {}", new Object[]{constructedUrl.getHost(), var14.getMessage(), var14});
            throw new RuntimeException(var14);
        } catch (IOException var15) {
            LOGGER.error("Error getting response from host: [{}] with path: [{}] and protocol: [{}] Error Message: {}", new Object[]{constructedUrl.getHost(), constructedUrl.getPath(), constructedUrl.getProtocol(), var15.getMessage(), var15});
            throw new RuntimeException(var15);
        } finally {
            CommonUtils.closeQuietly(in);
            if (conn != null) {
                conn.disconnect();
            }

        }
    }

}

