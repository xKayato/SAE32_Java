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
        String sqlOeuvre = "INSERT INTO Oeuvre (nomOeuvre, dateSortie, auteur_studio, photo, actif) VALUES (?, ?, ?, ?, ?)";
        String sqlType = "INSERT INTO Type (nomType) VALUES (?)";
        String sqlTag = "INSERT INTO Tag (nomTag) VALUES (?)";
        String sqlTyper = "INSERT INTO Typer (nomType, idOeuvre) VALUES (?, ?)";
        String sqlTagger = "INSERT INTO Tagger (nomTag, idOeuvre) VALUES (?, ?)";
        String checkType = "SELECT 1 FROM Type WHERE nomType = ?";
        String checkTag = "SELECT 1 FROM Tag WHERE nomTag = ?";
    
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
                try (PreparedStatement pstmtOeuvre = this.connection.prepareStatement(sqlOeuvre, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    pstmtOeuvre.setString(1, jsonObject.optString("nomOeuvre", ""));
                    pstmtOeuvre.setString(2, jsonObject.optString("dateSortie", ""));
                    pstmtOeuvre.setString(3, jsonObject.optString("auteur_studio", ""));
                    pstmtOeuvre.setString(4, jsonObject.optString("photo", ""));
                    pstmtOeuvre.setInt(5, jsonObject.optInt("actif", 0));
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
    
                // Insérer dans Type si non existant
                if (jsonObject.has("type")) {
                    String type = jsonObject.getString("type");
                    if (!recordExists(checkType, type)) {
                        try (PreparedStatement pstmtType = this.connection.prepareStatement(sqlType)) {
                            pstmtType.setString(1, type);
                            pstmtType.executeUpdate();
                        }
                    }
    
                    // Lier Type et Oeuvre
                    try (PreparedStatement pstmtTyper = this.connection.prepareStatement(sqlTyper)) {
                        pstmtTyper.setString(1, type);
                        pstmtTyper.setInt(2, idOeuvre);
                        pstmtTyper.executeUpdate();
                    }
                }
    
                // Insérer dans Tag si non existant
                if (jsonObject.has("tags")) {
                    JSONArray tags = jsonObject.getJSONArray("tags");
                    for (int i = 0; i < tags.length(); i++) {
                        String tag = tags.getString(i);
                        if (!recordExists(checkTag, tag)) {
                            try (PreparedStatement pstmtTag = this.connection.prepareStatement(sqlTag)) {
                                pstmtTag.setString(1, tag);
                                pstmtTag.executeUpdate();
                            }
                        }
    
                        // Lier Tag et Oeuvre
                        try (PreparedStatement pstmtTagger = this.connection.prepareStatement(sqlTagger)) {
                            pstmtTagger.setString(1, tag);
                            pstmtTagger.setInt(2, idOeuvre);
                            pstmtTagger.executeUpdate();
                        }
                    }
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
