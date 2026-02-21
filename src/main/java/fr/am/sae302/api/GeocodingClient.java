package fr.am.sae302.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GeocodingClient {

    public static JsonObject searchCity(String city) throws Exception {

        // 1) Construire l'URL Open-Meteo Geocoding
        String urlStr = "https://geocoding-api.open-meteo.com/v1/search?name="
                + URLEncoder.encode(city, StandardCharsets.UTF_8)
                + "&count=5"
                + "&format=json";

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        // 2) Lire la réponse JSON brute
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();

        // 3) Parser le JSON
        JsonObject root = JsonParser.parseString(sb.toString()).getAsJsonObject();

        if (!root.has("results")) {
            throw new Exception("Aucun résultat pour la ville : " + city);
        }

        JsonArray results = root.getAsJsonArray("results");
        if (results == null || results.size() == 0) {
            throw new Exception("Ville non trouvée : " + city);
        }

        JsonObject best = null;

        // 4) PRIORITÉ ABSOLUE : France
        for (int i = 0; i < results.size(); i++) {
            JsonObject r = results.get(i).getAsJsonObject();
            if (r.has("country_code") && r.get("country_code").getAsString().equals("FR")) {
                best = r;
                break;
            }
        }

        // 5) PRIORITÉ SECONDAIRE : hors US / PR
        if (best == null) {
            for (int i = 0; i < results.size(); i++) {
                JsonObject r = results.get(i).getAsJsonObject();
                if (!r.has("country_code")) continue;

                String cc = r.get("country_code").getAsString();
                if (!cc.equals("US") && !cc.equals("PR")) {
                    best = r;
                    break;
                }
            }
        }

        // 6) FALLBACK
        if (best == null && results.size() > 0) {
            best = results.get(0).getAsJsonObject();
        }

        if (best == null) {
            throw new Exception("Aucun résultat géographique exploitable pour : " + city);
        }

        // 7) Objet de retour propre
        JsonObject coord = new JsonObject();
        coord.addProperty("name", best.get("name").getAsString());
        coord.addProperty("latitude", best.get("latitude").getAsDouble());
        coord.addProperty("longitude", best.get("longitude").getAsDouble());

        return coord;
    }
}

