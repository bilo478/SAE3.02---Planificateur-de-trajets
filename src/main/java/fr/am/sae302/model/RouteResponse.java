package fr.am.sae302.model;


public class RouteResponse {


    private String statut;
    private String message;
    private Trajet trajet;


    public RouteResponse(Trajet trajet, String statut, String message) {
        this.trajet = trajet;
        this.statut = statut;
        this.message = message;
    }


    public String getStatut() {
        return statut;
    }


    public String getMessage() {
        return message;
    }


    public Trajet getTrajet() {
        return trajet;
    }
}
