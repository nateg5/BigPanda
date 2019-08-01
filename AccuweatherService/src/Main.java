import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

public class Main {

    public static final String DEFAULT_FILENAME = "src/locationKeys.txt";

    public static final String ACCUWEATHER_LOCATIONS_URL = "http://dataservice.accuweather.com/locations/v1/";
    public static final String ACCUWEATHER_CURRENT_CONDITIONS_URL = "http://dataservice.accuweather.com/currentconditions/v1/";
    public static final String ACCUWEATHER_API_KEY = "?apikey=Q7FmHNhcT9oLhLirPaixbz5WFmgThFEz";
    public static final String ACCUWEATHER_LOCALIZED_NAME = "LocalizedName";

    public static final String BIGPANDA_URL = "https://api.bigpanda.io/data/v2/alerts";
    public static final String BIGPANDA_AUTHORIZATION = "Bearer 594cc04f9f9553a2e760cf8f4327498d";
    public static final String BIGPANDA_CONTENT_TYPE = "application/json";
    public static final String BIGPANDA_APP_KEY = "ede0997acc403c9c271bb024b79a9045";

    public static void main(String[] args) {
        try {
            String filename = DEFAULT_FILENAME;
            if(args.length > 0) {
                filename = args[0];
            }

            disableCertificateValidation();

            Map<String, String> locationKeyToCityNameMap = buildLocationKeyToCityNameMap(filename);

            for(String locationKey : locationKeyToCityNameMap.keySet()) {
                String response = requestAccuWeatherData(ACCUWEATHER_CURRENT_CONDITIONS_URL + locationKey + ACCUWEATHER_API_KEY);

                sendBigPandaAlert(response, locationKeyToCityNameMap.get(locationKey), locationKey);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void disableCertificateValidation() throws Exception {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

        TrustManager[] certs = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String t) {}
                public void checkServerTrusted(X509Certificate[] certs, String t) {}
            }
        };

        // Create a context that doesn't check certificates.
        SSLContext ssl_ctx = SSLContext.getInstance("TLS");
        ssl_ctx.init(null,                // key manager
                     certs,               // trust manager
                     new SecureRandom()); // random number generator
        HttpsURLConnection.setDefaultSSLSocketFactory(ssl_ctx.getSocketFactory());
    }

    public static Map buildLocationKeyToCityNameMap(String filename) throws Exception {

        Map<String, String> map = new HashMap<>();

        FileReader reader = new FileReader(filename);
        BufferedReader br = new BufferedReader(reader);

        // read line by line
        String locationKey;
        while ((locationKey = br.readLine()) != null) {
            String response = requestAccuWeatherData(ACCUWEATHER_LOCATIONS_URL + locationKey + ACCUWEATHER_API_KEY);

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response, JsonObject.class);
            String cityName = jsonObject.get(ACCUWEATHER_LOCALIZED_NAME).getAsString();

            map.put(locationKey, cityName);
        }

        System.out.println(map.toString());

        return map;
    }

    public static String requestAccuWeatherData(String url) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());

        return response.toString();
    }

    public static String sendBigPandaAlert(String data, String cityName, String locationKey) throws Exception {

        URL obj = new URL(BIGPANDA_URL);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request headers
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", BIGPANDA_AUTHORIZATION);
        con.setRequestProperty("Content-Type", BIGPANDA_CONTENT_TYPE);

        String urlParameters = "{" +
            "\"app_key\":\"" + BIGPANDA_APP_KEY + "\"," +
            "\"status\":\"warning\"," +
            "\"host\":\"" + cityName + "\"," +
            "\"check\":\"Weather Check " + locationKey + "\"," +
            "\"description\":\"" + data.replace("\"", "'") + "\"" +
            "}";

        //send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());

        return response.toString();
    }
}
