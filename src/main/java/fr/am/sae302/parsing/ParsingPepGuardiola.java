package fr.am.sae302.parsing;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.InputStream;
import java.io.InputStreamReader;


public class ParsingPepGuardiola {


    public static void main(String[] args) {


        try {
            // 1) Charger le fichier JSON depuis le dossier resources
            InputStream is = ParsingPepGuardiola.class
                    .getClassLoader()
                    .getResourceAsStream("exemples/pep_guardiola.json");


            if (is == null) {
                System.out.println("Fichier JSON introuvable.");
                return;
            }


            // 2) Parser le JSON avec Gson
            JsonObject root = JsonParser
                    .parseReader(new InputStreamReader(is))
                    .getAsJsonObject();


            // 3) Lecture des champs simples
            String prenom = root.get("prenom").getAsString();
            String nom = root.get("nom").getAsString();
            int age = root.get("age").getAsInt();
            String club = root.get("club").getAsString();


            System.out.println("=== INFORMATIONS GÉNÉRALES ===");
            System.out.println("Prénom : " + prenom);
            System.out.println("Nom : " + nom);
            System.out.println("Âge : " + age);
            System.out.println("Club actuel : " + club);


            // 4) Lecture de l'objet imbriqué "palmares"
            JsonObject palmares = root.getAsJsonObject("palmares");


            int ligues = palmares.get("ligues").getAsInt();
            int ldc = palmares.get("ligues_des_champions").getAsInt();
            int coupes = palmares.get("coupes_nationales").getAsInt();


            System.out.println("\n=== PALMARÈS ===");
            System.out.println("Ligues gagnées : " + ligues);
            System.out.println("Ligues des Champions : " + ldc);
            System.out.println("Coupes nationales : " + coupes);


            // 5) Lecture du tableau "clubs_entraines"
            JsonArray clubs = root.getAsJsonArray("clubs_entraines");


            System.out.println("\n=== CLUBS ENTRAÎNÉS ===");
            for (int i = 0; i < clubs.size(); i++) {
                System.out.println("- " + clubs.get(i).getAsString());
            }


            // 6) Lecture du tableau "surnoms"
            JsonArray surnoms = root.getAsJsonArray("surnoms");


            System.out.println("\n=== SURNOMS ===");
            for (int i = 0; i < surnoms.size(); i++) {
                System.out.println("- " + surnoms.get(i).getAsString());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
