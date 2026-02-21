package fr.am.sae302.ui;

import fr.am.sae302.database.TrajetDAO;
import fr.am.sae302.model.Trajet;

import javax.swing.*;
import java.awt.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Fenêtre principale de l’interface graphique de la SAE 3.02.
 *
 * Cette classe représente l’interface utilisateur Swing permettant :
 * - de saisir une ville de départ et une ville d’arrivée
 * - de choisir un mode de transport
 * - de choisir une date et une heure de départ (immédiat ou différé)
 * - de lancer une requête HTTP vers le serveur REST
 * - d’afficher le résultat du trajet (distance, durée, ETA, météo)
 * - de consulter l’historique des trajets enregistrés en base de données
 *
 * Cette classe ne contient aucune logique métier.
 */
public class FenetreTrajet extends JFrame {

    private JTextField fromField;
    private JTextField toField;

    private JComboBox<String> modeBox;
    private JComboBox<String> transitBox;

    private JTextArea resultArea;
    private JButton historyButton;

    // === Gestion date / heure ===
    private JRadioButton nowRadio;
    private JRadioButton laterRadio;

    private JTextField dateField;    // dd-MM-yyyy
    private JTextField hourField;    // HH
    private JTextField minuteField;  // mm

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public FenetreTrajet() {

        super("SAE 3.02 – Planificateur de Trajet");

        setSize(550, 560);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ====================
        // FORMULAIRE
        // ====================
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));

        formPanel.add(new JLabel("Ville de départ :"));
        fromField = new JTextField();
        formPanel.add(fromField);

        formPanel.add(new JLabel("Ville d'arrivée :"));
        toField = new JTextField();
        formPanel.add(toField);

        // === Date de départ ===
        formPanel.add(new JLabel("Date de départ (JJ-MM-AAAA) :"));
        dateField = new JTextField(LocalDate.now().format(DATE_FORMAT));
        dateField.setEnabled(false);
        formPanel.add(dateField);

        // === Heure de départ ===
        formPanel.add(new JLabel("Heure de départ :"));

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        nowRadio = new JRadioButton("Maintenant", true);
        laterRadio = new JRadioButton("Différée");

        ButtonGroup group = new ButtonGroup();
        group.add(nowRadio);
        group.add(laterRadio);

        hourField = new JTextField("12", 2);
        minuteField = new JTextField("00", 2);

        hourField.setEnabled(false);
        minuteField.setEnabled(false);

        timePanel.add(nowRadio);
        timePanel.add(laterRadio);
        timePanel.add(new JLabel("HH"));
        timePanel.add(hourField);
        timePanel.add(new JLabel("mm"));
        timePanel.add(minuteField);

        formPanel.add(timePanel);

        // === Mode transport ===
        formPanel.add(new JLabel("Mode de transport :"));
        modeBox = new JComboBox<>(new String[]{
                "Voiture",
                "Marche",
                "Vélo",
                "Transport en commun"
        });
        formPanel.add(modeBox);

        // === Transit ===
        formPanel.add(new JLabel("Type transport en commun :"));
        transitBox = new JComboBox<>(new String[]{"Bus", "Train"});
        transitBox.setVisible(false);
        formPanel.add(transitBox);

        add(formPanel, BorderLayout.NORTH);

        // ====================
        // BOUTONS
        // ====================
        JPanel buttonPanel = new JPanel();

        JButton calculButton = new JButton("Calculer le trajet");
        historyButton = new JButton("Historique");
        JButton clearButton = new JButton("Vider l'historique");

        buttonPanel.add(calculButton);
        buttonPanel.add(historyButton);
        buttonPanel.add(clearButton);

        add(buttonPanel, BorderLayout.CENTER);

        // ====================
        // RESULTATS
        // ====================
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setPreferredSize(new Dimension(520, 220));

        add(scrollPane, BorderLayout.SOUTH);

        // ====================
        // LOGIQUE UI
        // ====================
        modeBox.addActionListener(e ->
                transitBox.setVisible("Transport en commun".equals(modeBox.getSelectedItem()))
        );

        nowRadio.addActionListener(e -> {
            dateField.setEnabled(false);
            hourField.setEnabled(false);
            minuteField.setEnabled(false);
        });

        laterRadio.addActionListener(e -> {
            dateField.setEnabled(true);
            hourField.setEnabled(true);
            minuteField.setEnabled(true);
        });

        // ====================
        // CALCUL TRAJET
        // ====================
        calculButton.addActionListener(e -> {

            try {
                String depart = fromField.getText().trim();
                String arrivee = toField.getText().trim();

                if (depart.isEmpty() || arrivee.isEmpty()) {
                    resultArea.setText("Veuillez saisir une ville de départ et d'arrivée.");
                    return;
                }

                String modeApi = convertModeToApi((String) modeBox.getSelectedItem());

                // === departure_time ===
                String departureTime;
                if (nowRadio.isSelected()) {
                    departureTime = "now";
                } else {
                    LocalDate date = LocalDate.parse(dateField.getText().trim(), DATE_FORMAT);
                    int hour = Integer.parseInt(hourField.getText().trim());
                    int minute = Integer.parseInt(minuteField.getText().trim());

                    LocalDateTime ldt = date.atTime(hour, minute);
                    long timestamp = ldt.atZone(ZoneId.systemDefault()).toEpochSecond();
                    departureTime = String.valueOf(timestamp);
                }

                String urlStr =
                        "http://localhost:9090/api/route/plan"
                                + "?from=" + depart
                                + "&to=" + arrivee
                                + "&mode=" + modeApi
                                + "&departure_time=" + departureTime;

                if ("transit".equals(modeApi)) {
                    urlStr += "&transit_mode=" + transitBox.getSelectedItem().toString().toLowerCase();
                }

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JsonObject trajet = JsonParser.parseString(sb.toString())
                        .getAsJsonObject()
                        .getAsJsonObject("trajet");

                resultArea.setText(
                        "=== RÉSULTAT DU TRAJET ===\n\n"
                                + "Trajet : " + trajet.get("depart").getAsString()
                                + " → " + trajet.get("arrivee").getAsString() + "\n"
                                + "Mode : " + convertModeToDisplay(trajet.get("mode").getAsString()) + "\n\n"
                                + "Distance : " + String.format("%.1f", trajet.get("distanceKm").getAsDouble()) + " km\n"
                                + "Durée : " + minutesToHeures(trajet.get("dureeMinutes").getAsDouble()) + "\n"
                                + "ETA : " + trajet.get("eta").getAsString() + "\n\n"
                                + "Température : " + trajet.get("temperature").getAsDouble() + " °C\n"
                                + "Précipitations : " + trajet.get("precipitation").getAsDouble() + " mm\n"
                );

            } catch (Exception ex) {
                resultArea.setText("Erreur : " + ex.getMessage());
            }
        });

        setVisible(true);
    }

    private String minutesToHeures(double minutes) {
        int m = (int) Math.round(minutes);
        return (m / 60) + " h " + (m % 60) + " min";
    }

    private String convertModeToApi(String fr) {
        return switch (fr) {
            case "Voiture" -> "driving";
            case "Marche" -> "walking";
            case "Vélo" -> "bicycling";
            case "Transport en commun" -> "transit";
            default -> "driving";
        };
    }

    private String convertModeToDisplay(String api) {
        return switch (api) {
            case "driving" -> "Voiture";
            case "walking" -> "Marche à pied";
            case "bicycling" -> "Vélo";
            case "transit" -> "Transport en commun";
            default -> api;
        };
    }

    public static void main(String[] args) {
        new FenetreTrajet();
    }
}
