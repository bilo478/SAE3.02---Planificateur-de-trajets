package fr.am.sae302.api;

import com.google.gson.JsonObject;

public class TestGeocoding {

    public static void main(String[] args) throws Exception {

        JsonObject coord = GeocodingClient.searchCity("Paris");

        System.out.println("=== RÉSULTATS GÉOCODING ===");
        System.out.println("Ville : " + coord.get("name").getAsString());
        System.out.println("Latitude : " + coord.get("latitude").getAsDouble());
        System.out.println("Longitude : " + coord.get("longitude").getAsDouble());
    }
}
