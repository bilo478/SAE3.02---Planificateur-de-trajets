package fr.am.sae302.database;


import fr.am.sae302.model.Trajet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class TrajetDAO {


    private static final String INSERT_SQL =
        "INSERT INTO historique_trajets " +
        "(depart, arrivee, mode, distance_km, duree_min, temperature, precipitation) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";


    public static void sauvegarder(Trajet t) {


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {


            ps.setString(1, t.getDepart());
            ps.setString(2, t.getArrivee());
            ps.setString(3, t.getMode());
            ps.setDouble(4, t.getDistanceKm());
            ps.setDouble(5, t.getDureeMinutes());
            ps.setDouble(6, t.getTemperature());
            ps.setDouble(7, t.getPrecipitation());


            ps.executeUpdate();


            System.out.println("Trajet sauvegardé en base");


        } catch (Exception e) {
            System.err.println("Erreur sauvegarde trajet");
            e.printStackTrace();
        }
    }
    public static List<Trajet> getHistorique() throws Exception {
        List<Trajet> list = new ArrayList<>();


        Connection conn = DatabaseManager.getConnection();
        String sql = "SELECT * FROM historique_trajets ORDER BY date_requete DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();


        while (rs.next()) {
        	Trajet t = new Trajet(
        		    rs.getString("depart"),
        		    rs.getString("arrivee"),
        		    rs.getString("mode")
        		);

        		t.setDistanceKm(rs.getDouble("distance_km"));
        		t.setDureeMinutes(rs.getDouble("duree_min"));
        		t.setTemperature(rs.getDouble("temperature"));
        		t.setPrecipitation(rs.getDouble("precipitation"));

        		LocalDateTime ldt = rs.getTimestamp("date_requete").toLocalDateTime();
        		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        		t.setDateCreation(ldt.format(fmt));



            list.add(t);
        }


        rs.close();
        ps.close();
        conn.close();


        return list;
    }
    public static void viderHistorique() throws Exception {


        String sql = "DELETE FROM historique_trajets";


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {


            ps.executeUpdate();
        }
    }

}



