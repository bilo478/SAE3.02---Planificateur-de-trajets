package fr.am.sae302.server;

import fr.am.sae302.model.RouteResponse;
import fr.am.sae302.model.Trajet;
import fr.am.sae302.service.RouteService;

import com.google.gson.Gson;

import java.io.*;
import java.net.*;

/**
 * Serveur HTTP de la SAE 3.02.
 *
 * Cette classe implémente un serveur HTTP minimaliste basé sur ServerSocket.
 * Elle expose une route REST permettant de calculer un trajet à partir
 * de paramètres fournis dans l’URL.
 *
 * Le serveur :
 * - écoute sur un port défini
 * - lit les requêtes HTTP entrantes
 * - extrait les paramètres (from, to, mode, departure_time)
 * - appelle le service métier RouteService
 * - renvoie une réponse JSON au client
 *
 * Cette classe ne contient aucune logique métier.
 * Elle joue uniquement le rôle d’intermédiaire entre le client (Swing)
 * et le service applicatif.
 */

public class RouteServer {

    private int port;

    public RouteServer(int port) {
        this.port = port;
    }
    
    /**
     * Démarre le serveur HTTP et écoute les connexions entrantes.
     *
     * Chaque connexion client est traitée dans un thread séparé
     * afin de permettre plusieurs requêtes simultanées.
     *
     * @throws Exception en cas d’erreur réseau
     */

    public void start() throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Serveur HTTP lancé sur le port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> handle(socket)).start();
        }
    }

    /**
     * Traite une requête HTTP entrante.
     *
     * Cette méthode :
     * - lit la requête HTTP brute
     * - extrait l’URL et les paramètres
     * - valide les paramètres obligatoires
     * - appelle le service RouteService
     * - construit et renvoie une réponse JSON
     *
     * @param socket socket client correspondant à la requête
     */

    private void handle(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream()));

            String line = reader.readLine();
            if (line == null) return;

            // Exemple : GET /api/route/plan?from=Paris&to=Lille&mode=driving&departure_time=now HTTP/1.1
            String route = line.split(" ")[1];
            System.out.println("Requête reçue : " + route);
            if (route.startsWith("/api/route/plan")) {

                // Extraction paramètres
                String query = route.contains("?")
                        ? route.substring(route.indexOf("?") + 1)
                        : "";

                String[] params = query.split("&");

                String from = null;
                String to = null;
                String mode = null;
                String departureTime = null;
                String transitMode = null;
                
                for (String p : params) {
                    if (!p.contains("=")) continue;

                    String[] pair = p.split("=");
                    String key = pair[0];
                    String value = URLDecoder.decode(pair[1], "UTF-8");

                    if (key.equals("from")) from = value;
                    else if (key.equals("to")) to = value;
                    else if (key.equals("mode")) mode = value;
                    else if (key.equals("departure_time")) departureTime = value;
                    else if (key.equals("transit_mode")) transitMode = value;
                }
                
                if (departureTime == null) {
                    departureTime = "now";
                }


                // Vérification des champs obligatoires
                if (from == null || to == null || mode == null) {
                    String error = "{\"statut\":\"ERREUR\",\"message\":\"Paramètres manquants\"}";
                    writer.write("HTTP/1.1 400 Bad Request\r\n");
                    writer.write("Content-Type: application/json\r\n");
                    writer.write("Content-Length: " + error.length() + "\r\n");
                    writer.write("\r\n");
                    writer.write(error);
                    writer.flush();
                    socket.close();
                    return;
                }

                // Appel au service métier
                System.out.println("Appel à RouteService...");
                Trajet trajet = RouteService.construireTrajet(from, to, mode, departureTime, transitMode);
                System.out.println("Trajet final :");
                System.out.println("Durée normale : " + trajet.getDureeMinutes() + " min");
                System.out.println("Durée trafic : " + trajet.getDureeTraficMinutes() + " min");
                System.out.println("ETA : " + trajet.getEta());
                System.out.println("RouteService terminé !");
                // Construction réponse (BONUS REST)
                RouteResponse rep = new RouteResponse(trajet, "OK", "Trajet calculé avec succès");
                String json = new Gson().toJson(rep);

                // Envoi HTTP
                writer.write("HTTP/1.1 200 OK\r\n");
                writer.write("Content-Type: application/json\r\n");
                writer.write("Content-Length: " + json.length() + "\r\n");
                writer.write("\r\n");
                writer.write(json);
                writer.flush();
                socket.close();
                return;
            }

            // Sinon : route non reconnue
            writer.write("HTTP/1.1 404 Not Found\r\n\r\n");
            writer.flush();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
