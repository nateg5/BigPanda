import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import sun.org.mozilla.javascript.internal.json.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("Hello World!");

            String url = "http://dataservice.accuweather.com/currentconditions/v1/75098?details=true&apikey=Q7FmHNhcT9oLhLirPaixbz5WFmgThFEz";

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            //con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());

            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(response.toString(), JsonArray.class);
            System.out.println("jsonArray = " + jsonArray.toString());
            System.out.println("jsonArray.size = " + jsonArray.size());
            JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
            System.out.println("jsonObject = " + jsonObject.toString());
            for(Map.Entry entry : jsonObject.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }


        } catch(Exception e) {
            System.out.println(e);
        }
    }
}
