package fr.am.sae302.api;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.am.sae302.config.Config;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;



public class DistanceMatrixClient {


    public static JsonObject appeler(
            String origine,
            String destination,
            String mode,
            String departureTime,
            String transitMode
    ) throws Exception {


        String key = Config.getDistanceMatrixKey();


        String urlStr =
        	    "https://api.distancematrix.ai/maps/api/distancematrix/json"
        	        + "?origins=" + URLEncoder.encode(origine, StandardCharsets.UTF_8)
        	        + "&destinations=" + URLEncoder.encode(destination, StandardCharsets.UTF_8)
        	        + "&mode=" + mode
        	        + "&departure_time=" + departureTime
        	        + "&key=" + key;
        if (mode.equals("transit") && transitMode != null && !transitMode.isEmpty()) {
        	urlStr += "&transit_mode=" + transitMode;
        }
        
        


        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");


        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );


        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();


        JsonObject root = JsonParser.parseString(sb.toString()).getAsJsonObject();
        JsonObject result = new JsonObject();


        JsonArray rows = root.getAsJsonArray("rows");
        if (rows == null || rows.size() == 0) return result;


        JsonObject element =
                rows.get(0).getAsJsonObject()
                        .getAsJsonArray("elements")
                        .get(0).getAsJsonObject();


        if (!element.get("status").getAsString().equals("OK")) return result;


        double distanceKm =
                element.getAsJsonObject("distance")
                        .get("value").getAsDouble() / 1000.0;


        double dureeMinutes =
                element.getAsJsonObject("duration")
                        .get("value").getAsDouble() / 60.0;


        result.addProperty("distanceKm", distanceKm);
        result.addProperty("dureeMinutes", dureeMinutes);


        if (element.has("duration_in_traffic")) {
            double dureeTrafic =
                    element.getAsJsonObject("duration_in_traffic")
                            .get("value").getAsDouble() / 60.0;
            result.addProperty("dureeTraficMinutes", dureeTrafic);
        }


        return result;
    }
}
