import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

public class database {

    private Connection connection;
    private String file;

    public database(String file) {
        this.file = file;
    }

    public void connectDatabase() {
        String url = "jdbc:sqlite:" + this.file;
        try {
            this.connection = DriverManager.getConnection(url);
            System.out.println("Connexion à la base de données établie.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la connexion à la base de données : " + e.getMessage());
        }
    }

    public void insertIntoDb(JSONObject json) {
        String sqlOeuvre = "INSERT INTO Oeuvre (nomOeuvre, dateSortie, auteur_studio, actif, type) VALUES (?, ?, ?, ?, ?)";
        String sqlType = "INSERT INTO Type (nomType) VALUES (?)";
        String checkType = "SELECT 1 FROM Type WHERE nomType = ?";

        if (this.connection == null) {
            System.out.println("Erreur : connexion à la base de données non établie.");
            return;
        }

        JSONArray data = json.getJSONArray("data");

        for (Object obj : data) {
            JSONObject jsonObject = (JSONObject) obj;

            try {
                // Insérer dans Oeuvre
                int idOeuvre = -1;
                try (PreparedStatement pstmtOeuvre = this.connection.prepareStatement(sqlOeuvre,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {

                    // Insérer dans Type si non existant
                    if (jsonObject.has("type")) {
                        String type = jsonObject.getString("type");
                        if (!recordExists(checkType, type)) {
                            try (PreparedStatement pstmtType = this.connection.prepareStatement(sqlType)) {
                                pstmtType.setString(1, type);
                                pstmtType.executeUpdate();
                            }
                        }
                    }

                    pstmtOeuvre.setString(1, jsonObject.optString("nomOeuvre", ""));
                    pstmtOeuvre.setString(2, jsonObject.optString("dateSortie", ""));
                    pstmtOeuvre.setString(3, jsonObject.optString("auteur_studio", ""));
                    pstmtOeuvre.setInt(4, jsonObject.optInt("actif", 0));
                    pstmtOeuvre.setString(5, jsonObject.optString("type", ""));
                    pstmtOeuvre.executeUpdate();

                    // Récupérer l'ID auto-incrémenté
                    try (ResultSet generatedKeys = pstmtOeuvre.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            idOeuvre = generatedKeys.getInt(1);
                        }
                    }
                }

                if (idOeuvre == -1) {
                    System.out.println("Erreur : ID de l'Oeuvre non récupéré.");
                    continue;
                }

            } catch (SQLException e) {
                System.out.println("Erreur lors de l'insertion de cette ligne : " + e.getMessage());
            }
        }
    }

    private boolean recordExists(String query, String value) {
        try (PreparedStatement pstmt = this.connection.prepareStatement(query)) {
            pstmt.setString(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Si une ligne est retournée, l'enregistrement existe
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la vérification : " + e.getMessage());
        }
        return false;
    }

}
