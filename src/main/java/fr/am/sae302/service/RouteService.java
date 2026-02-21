package fr.am.sae302.service;

import com.google.gson.JsonObject;

import fr.am.sae302.api.DistanceMatrixClient;
import fr.am.sae302.api.GeocodingClient;
import fr.am.sae302.api.MeteoClient;
import fr.am.sae302.database.TrajetDAO;
import fr.am.sae302.model.Trajet;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Service métier principal de la SAE 3.02.
 *
 * Cette classe centralise toute la logique applicative liée
 * au calcul d’un trajet. Elle est volontairement séparée :
 *
 * - de l’interface graphique (Swing)
 * - du serveur HTTP (REST)
 * - de la base de données (DAO)
 *
 * Elle est responsable de :
 * - l’appel aux API externes (géocodage, DistanceMatrix, météo)
 * - l’extraction et le parsing des données JSON
 * - le calcul des durées, de la distance et de l’ETA
 * - la création et l’enrichissement de l’objet Trajet
 * - la sauvegarde du trajet en base de données
 *
 * Aucune logique métier ne doit se trouver ailleurs que dans cette classe.
 */
public class RouteService {

    /**
     * Construit un trajet complet à partir des paramètres fournis
     * par le serveur HTTP.
     */
    public static Trajet construireTrajet(
            String depart,
            String arrivee,
            String mode,
            String departureTime,
            String transitMode
    ) throws Exception {

        // Création du trajet
        Trajet t = new Trajet(depart, arrivee, mode);

        // 1) Géocodage départ
        JsonObject coordDep = GeocodingClient.searchCity(depart);
        t.setLatitudeDepart(coordDep.get("latitude").getAsDouble());
        t.setLongitudeDepart(coordDep.get("longitude").getAsDouble());

        // 2) Géocodage arrivée
        JsonObject coordArr = GeocodingClient.searchCity(arrivee);
        t.setLatitudeArrivee(coordArr.get("latitude").getAsDouble());
        t.setLongitudeArrivee(coordArr.get("longitude").getAsDouble());

        // 3) DistanceMatrix
        JsonObject dm = DistanceMatrixClient.appeler(
                depart,
                arrivee,
                mode,
                departureTime,
                transitMode
        );

        if (dm.has("distanceKm") && dm.has("dureeMinutes")) {
            t.setDistanceKm(dm.get("distanceKm").getAsDouble());
            t.setDureeMinutes(dm.get("dureeMinutes").getAsDouble());

            if (dm.has("dureeTraficMinutes")) {
                t.setDureeTraficMinutes(dm.get("dureeTraficMinutes").getAsDouble());
            } else {
                t.setDureeTraficMinutes(t.getDureeMinutes());
            }
        } else {
            t.setDistanceKm(0);
            t.setDureeMinutes(0);
            t.setDureeTraficMinutes(0);
        }

        // 4) Calcul de l'heure de départ réelle
        LocalDateTime departTime;

        if ("now".equals(departureTime)) {
            departTime = LocalDateTime.now();
        } else {
            long timestamp = Long.parseLong(departureTime);
            departTime = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timestamp),
                    ZoneId.systemDefault()
            );
        }

        // Calcul ETA à partir de la vraie heure de départ
        t.calculerETA(departTime);

        // 5) Heure d’arrivée estimée (pour météo)
        double dureeEffective =
                t.getDureeTraficMinutes() > 0
                        ? t.getDureeTraficMinutes()
                        : t.getDureeMinutes();

        LocalDateTime heureArrivee = departTime.plusMinutes(Math.round(dureeEffective));
        int heureArriveeH = heureArrivee.getHour();

        // 6) Appel météo à l’arrivée
        JsonObject meteo = MeteoClient.getMeteo(
                t.getLatitudeArrivee(),
                t.getLongitudeArrivee(),
                heureArriveeH
        );

        t.setTemperature(meteo.get("temperature").getAsDouble());
        t.setPrecipitation(meteo.get("precipitation").getAsDouble());

        // 7) Sauvegarde en base
        TrajetDAO.sauvegarder(t);

        return t;
    }
}

