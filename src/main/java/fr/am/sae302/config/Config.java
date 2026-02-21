package fr.am.sae302.config;

public class Config {

    private static final String DM_API_KEY = "YOUR_API_KEY";

    public static String getDistanceMatrixKey() {
        return DM_API_KEY;
    }
}

