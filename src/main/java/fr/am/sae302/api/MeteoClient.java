package fr.am.sae302.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MeteoClient {

    public static JsonObject getMeteo(
            double latitude,
            double longitude,
            int heureArrivee
    ) throws Exception {

        String urlStr =
                "https://api.open-meteo.com/v1/forecast?"
                        + "latitude=" + latitude
                        + "&longitude=" + longitude
                        + "&hourly=temperature_2m,precipitation"
                        + "&forecast_days=1";

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();

        JsonObject root = JsonParser.parseString(sb.toString()).getAsJsonObject();

        JsonObject hourly = root.getAsJsonObject("hourly");
        JsonArray temperatures = hourly.getAsJsonArray("temperature_2m");
        JsonArray precipitations = hourly.getAsJsonArray("precipitation");

        // Sécurisation de l'index horaire
        int index = heureArrivee;
        if (index < 0 || index >= temperatures.size()) {
            index = 0;
        }

        double temperature = temperatures.get(index).getAsDouble();
        double precipitation = precipitations.get(index).getAsDouble();

        JsonObject meteo = new JsonObject();
        meteo.addProperty("temperature", temperature);
        meteo.addProperty("precipitation", precipitation);

        return meteo;
    }
}

