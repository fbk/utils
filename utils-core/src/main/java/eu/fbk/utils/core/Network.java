package eu.fbk.utils.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class Network {

    private static final Logger LOGGER = LoggerFactory.getLogger(Network.class);

    public static String downloadPage(String urlPath) throws KeyManagementException, NoSuchAlgorithmException {

        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            public void checkServerTrusted(X509Certificate[] certs, String authType) { }

        } };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;

        try {
            url = new URL(urlPath);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));
            StringBuffer b = new StringBuffer();
            while ((line = br.readLine()) != null) {
                b.append(line).append(System.getProperty("line.separator"));
            }

            return b.toString();
        } catch (MalformedURLException e) {
            LOGGER.warn(e.getMessage());
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                LOGGER.warn(e.getMessage());
            }
        }

        return null;
    }
}
