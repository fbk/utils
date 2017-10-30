package eu.fbk.utils.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;


public class Network {

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
