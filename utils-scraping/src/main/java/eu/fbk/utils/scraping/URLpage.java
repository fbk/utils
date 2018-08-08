package eu.fbk.utils.scraping;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 * User: aprosio
 */
public class URLpage {

    private URL myURL = null;
    private HashMap<String, String> headers = new HashMap<String, String>();
    private String version = null;
    private URL lastUrl = null;

    public static String giveMeTextInUTF8(byte[] text) throws UnsupportedEncodingException {
        return giveMeTextInUTF8(text, "UTF-8");
    }

    public static String giveMeTextInUTF8(byte[] text, String defaultEncoding) throws UnsupportedEncodingException {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(text, 0, text.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        if (encoding == null) {
            encoding = defaultEncoding;
        }
        return new String(text, 0, text.length, encoding);
    }

    public URL getLastUrl() {
        return lastUrl;
    }

    public String getHost() {
        return myURL.getHost();
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public URLpage(String url) throws MalformedURLException {
        myURL = new URL(url);
    }

    public URLpage(String context, String url) throws MalformedURLException {
        myURL = new URL(new URL(context), url);
    }

    public URLpage(URL context, String url) throws MalformedURLException {
        myURL = new URL(context, url);
    }

    public String getVersion() {
        return version;
    }

    public URL getMyURL() {
        return myURL;
    }

    public String getContent() throws IOException {
        final URLConnection conn = myURL.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(10000);

        for (int i = 0; ; i++) {
            String name = conn.getHeaderFieldKey(i);
            String value = conn.getHeaderField(i);
            if (name == null && value == null) {
                break;
            }
            if (name == null) {
                version = value;
            } else {
                headers.put(name, value);
            }
        }

        InputStream inputStream = null;
        try {
            if (headers.get("Content-Encoding") != null && headers.get("Content-Encoding").equals("gzip")) {
                InputStream stream = conn.getInputStream();
                inputStream = new GZIPInputStream(stream);
            } else {
                inputStream = conn.getInputStream();
            }

            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] tmpBytes = new byte[1024 * 4];
            int readBytes;
            while ((readBytes = inputStream.read(tmpBytes)) > 0) {
                buffer.write(tmpBytes, 0, readBytes);
            }

            lastUrl = conn.getURL();
            return giveMeTextInUTF8(buffer.toByteArray());
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public BufferedReader getContentAsReader() throws IOException {
        return new BufferedReader(new StringReader(getContent()));
    }

}
