package fr.am.sae302.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modèle représentant un trajet calculé par l’application.
 *
 * Cette classe contient l’ensemble des données métier liées
 * à un trajet :
 * - villes de départ et d’arrivée
 * - mode de transport
 * - distance
 * - durées (normale et trafic)
 * - heure estimée d’arrivée (ETA)
 * - coordonnées géographiques
 * - informations météorologiques
 *
 * Cette classe est utilisée :
 * - par le service métier
 * - par le serveur pour la réponse JSON
 * - par la base de données pour l’historique
 */

public class Trajet {

    private String depart;
    private String arrivee;
    private String mode;

    private double distanceKm;

    // Durées en minutes
    private double dureeMinutes;
    private double dureeTraficMinutes;

    // ETA formatée
    private String eta;
    private String dateCreation;
    
    private double latitudeDepart;
    private double longitudeDepart;
    private double latitudeArrivee;
    private double longitudeArrivee;
    private double temperature;
    private double precipitation;

    /**
     * Construit un trajet à partir des informations principales.
     *
     * @param depart ville de départ
     * @param arrivee ville d’arrivée
     * @param mode mode de transport
     */

    public Trajet(String depart, String arrivee, String mode) {
        this.depart = depart;
        this.arrivee = arrivee;
        this.mode = mode;
    }

    // Getters et setters permettant l’accès et la modification
    // des données du trajet par les différentes couches de l’application


    public String getDepart() { return depart; }
    public String getArrivee() { return arrivee; }
    public String getMode() { return mode; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(double dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public double getDureeTraficMinutes() { return dureeTraficMinutes; }
    public void setDureeTraficMinutes(double dureeTraficMinutes) {
        this.dureeTraficMinutes = dureeTraficMinutes;
    }

    public String getEta() { return eta; }

    public void calculerETA(LocalDateTime departTime) {
        long minutes = Math.round(dureeTraficMinutes > 0
                ? dureeTraficMinutes
                : dureeMinutes);

        LocalDateTime arriveeTime = departTime.plusMinutes(minutes);
        this.eta = arriveeTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public double getLatitudeDepart() { return latitudeDepart; }
    public void setLatitudeDepart(double latitudeDepart) { this.latitudeDepart = latitudeDepart; }

    public double getLongitudeDepart() { return longitudeDepart; }
    public void setLongitudeDepart(double longitudeDepart) { this.longitudeDepart = longitudeDepart; }

    public double getLatitudeArrivee() { return latitudeArrivee; }
    public void setLatitudeArrivee(double latitudeArrivee) { this.latitudeArrivee = latitudeArrivee; }

    public double getLongitudeArrivee() { return longitudeArrivee; }
    public void setLongitudeArrivee(double longitudeArrivee) { this.longitudeArrivee = longitudeArrivee; }
    
    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getPrecipitation() { return precipitation; }
    public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }
}
