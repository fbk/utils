package eu.fbk.utils.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Map;


public class Network {

    private static Integer DEFAULT_TIMEOUT = 2000;

    static public String postRequest(String host, int port, Map<String, String> pars) throws IOException {
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(pars);
        String fromServer = null;

        String urlAddress = "http://" + host + ":" + Integer.toString(port) + "/";

        URL serverAddress = new URL(urlAddress);

        StringBuilder sb = new StringBuilder();

        HttpURLConnection connection;

        connection = (HttpURLConnection) serverAddress.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(Integer.parseInt(DEFAULT_TIMEOUT.toString()));
            connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.getOutputStream().write(json.getBytes("UTF-8"));
            connection.connect();

            // read the result from the server
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                while ((fromServer = rd.readLine()) != null) {
                    sb.append(fromServer + '\n');
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

        return sb.toString();
    }

    static public String request(String host, int port, Map<String, String> pars) throws IOException {
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(pars);

        Socket echoSocket = new Socket(host, port);
        PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
//        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        out.println(json);
        String responseLine;
        StringBuffer response = new StringBuffer();
        while ((responseLine = in.readLine()) != null) {
            response.append(responseLine);
            // continue...
        }
        in.close();
        out.close();
        echoSocket.close();

//        System.out.println(json);

        return response.toString();
    }

}
