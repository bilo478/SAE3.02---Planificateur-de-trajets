package fr.am.sae302.api;

import com.google.gson.JsonObject;

public class TestAPI {

    public static void main(String[] args) throws Exception {

        JsonObject result = DistanceMatrixClient.appeler(
                "Paris",
                "Lille",
                "driving",
                "now",
                null
        );

        System.out.println("=== RÉSULTAT DISTANCE MATRIX ===");
        System.out.println("Distance (km) : " + result.get("distanceKm"));
        System.out.println("Durée (min)   : " + result.get("dureeMinutes"));

        if (result.has("dureeTraficMinutes")) {
            System.out.println("Durée trafic  : " + result.get("dureeTraficMinutes"));
        }
    }
}

