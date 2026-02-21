package fr.am.sae302.model;

public class Coordonnees {

    private String nom;
    private double latitude;
    private double longitude;

    public Coordonnees(String nom, double latitude, double longitude) {
        this.nom = nom;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getNom() {
        return nom;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return nom + " (" + latitude + ", " + longitude + ")";
    }
}
