package fr.am.sae302;

import fr.am.sae302.ui.FenetreTrajet;
import fr.am.sae302.server.RouteServer;

public class Application {

    public static void main(String[] args) {

        System.out.println("=== Lancement de l'application SAE 3.02 ===");

        // 1) Lancer l'interface graphique
        new FenetreTrajet();

        // 2) Lancer le serveur HTTP
        new Thread(() -> {
            try { new RouteServer(9090).start(); }
            catch (Exception e) { e.printStackTrace(); }
        }).start();

        System.out.println("Interface lancée. Serveur prêt !");
    }
}
