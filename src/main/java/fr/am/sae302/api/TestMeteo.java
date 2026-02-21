package fr.am.sae302.api;


import com.google.gson.JsonObject;


/**
 * Classe de test pour l’API météo Open-Meteo.
 *
 * Permet de tester indépendamment :
 * - l’appel HTTP
 * - le parsing JSON
 * - la récupération de la météo à une heure donnée
 */
public class TestMeteo {


    public static void main(String[] args) {


        try {
            // Coordonnées de Paris
            double latitude = 48.85341;
            double longitude = 2.3488;


            // Heure cible (ex: 12 = midi)
            int hourIndex = 12;


            JsonObject meteo = MeteoClient.getMeteo(latitude, longitude, hourIndex);


            double temperature = meteo.get("temperature").getAsDouble();
            double precipitation = meteo.get("precipitation").getAsDouble();


            System.out.println("=== TEST API METEO ===");
            System.out.println("Latitude  : " + latitude);
            System.out.println("Longitude : " + longitude);
            System.out.println("Heure cible (index) : " + hourIndex);
            System.out.println("Température : " + temperature + " °C");
            System.out.println("Précipitations : " + precipitation + " mm");


        } catch (Exception e) {
            System.err.println("Erreur lors du test météo :");
            e.printStackTrace();
        }
    }
}

