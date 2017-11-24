package eu.fbk.utils.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class Network {

    private static final Logger LOGGER = LoggerFactory.getLogger(Network.class);

    private static final String USER_AGENT = "FBK Utils";

    public static String encodeMap(Map<String, String> pars) {
        StringBuffer buffer = new StringBuffer();
        for (String key : pars.keySet()) {
            String value = pars.get(key);

            if (buffer.length() > 0) {
                buffer.append("&");
            }

            // Should the key be encoded?
            buffer.append(key);

            buffer.append("=");
            try {
                buffer.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return buffer.toString();
    }

    public static String postRequest(String urlPath, Map<String, String> pars) throws IOException {
        return postRequest(urlPath, encodeMap(pars));
    }

    public static String postRequest(String urlPath, String pars) throws IOException {
        URL obj = new URL(urlPath);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(pars);
        wr.flush();
        wr.close();

//        int responseCode = con.getResponseCode();
//        System.out.println("\nSending 'POST' request to URL : " + urlPath);
//        System.out.println("Post parameters : " + parameters);
//        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

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

    public static void main(String[] args) {
        String url = "https://translate.yandex.net/api/v1.5/tr.json/translate";
        Map<String, String> map = new HashMap<>();
        map.put("key", "trnsl.1.1.20171010T091318Z.1e87b765f05f625b.099f046a438c4dd1efac8bbbd3a6f9f7d80fc866");
        map.put("lang", "it-en");
        map.put("text", "Il cane dorme sul tappeto.");
        map.put("options", "4");

        try {
            postRequest(url, map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
