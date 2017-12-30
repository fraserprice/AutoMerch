package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class HttpGet {
    public static String getRequest(String url) {
        try {
            StringBuilder sb = new StringBuilder();
            URL endpointUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) endpointUrl.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
