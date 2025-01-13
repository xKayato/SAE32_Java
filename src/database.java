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

    public Connection getConnection() {
        return this.connection;
    }

    public void connectDatabase() {
        String url = "jdbc:sqlite:" + this.file;
        try {
            this.connection = DriverManager.getConnection(url);
            System.out
                    .println(Utils.ANSI_GREEN + "[SUCESS] Connexion à la base de données établie." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la connexion à la base de données : "
                    + e.getMessage() + Utils.ANSI_RESET);
        }
    }

    public void insertIntoDb(JSONObject json) {
        String sqlOeuvre = "INSERT INTO Oeuvre (nomOeuvre, dateSortie, auteur_studio, actif, type) VALUES (?, ?, ?, ?, ?)";
        String sqlType = "INSERT INTO Type (nomType) VALUES (?)";
        String checkType = "SELECT 1 FROM Type WHERE nomType = ?";

        if (this.connection == null) {
            System.out
                    .println(Utils.ANSI_RED + "[ERROR] Connexion à la base de données non établie." + Utils.ANSI_RESET);
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
                    System.out.println(Utils.ANSI_RED + "[ERROR] ID de l'Oeuvre non récupéré." + Utils.ANSI_RESET);
                    continue;
                }

            } catch (SQLException e) {
                System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de l'insertion de cette ligne : "
                        + e.getMessage() + Utils.ANSI_RESET);
            }
        }

        System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Importation terminée." + Utils.ANSI_RESET);
    }

    public void promoteUser(String login){
        String sqlUpdate = "UPDATE User SET acces = 1 WHERE login = ?";
        try (PreparedStatement pstmt = this.connection.prepareStatement(sqlUpdate)) {
            pstmt.setString(1, login);
            pstmt.executeUpdate();
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Utilisateur " + login + " promu en Administrateur." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la promotion de l'utilisateur : " + e.getMessage() + Utils.ANSI_RESET);
            }
        }

    public void resetDatabase() {
        String sql = "BEGIN TRANSACTION; " +
                "DROP TABLE IF EXISTS Avis; " +
                "DROP TABLE IF EXISTS Oeuvre; " +
                "DROP TABLE IF EXISTS Type; " +
                "DROP TABLE IF EXISTS User; " +
                "CREATE TABLE IF NOT EXISTS Avis ( " +
                "idAvis INTEGER, " +
                "texteAvis TEXT, " +
                "note INT, " +
                "date DATE, " +
                "idOeuvre INT, " +
                "login VARCHAR(100), " +
                "PRIMARY KEY(idAvis AUTOINCREMENT), " +
                "FOREIGN KEY(idOeuvre) REFERENCES Oeuvre(idOeuvre) ON DELETE CASCADE, " +
                "FOREIGN KEY(login) REFERENCES User(login) ON DELETE CASCADE); " +
                "CREATE TABLE IF NOT EXISTS Oeuvre ( " +
                "idOeuvre INTEGER, " +
                "nomOeuvre VARCHAR(100), " +
                "dateSortie DATE, " +
                "actif BOOLEAN, " +
                "auteur_studio VARCHAR(100), " +
                "type TEXT, " +
                "PRIMARY KEY(idOeuvre AUTOINCREMENT)); " +
                "CREATE TABLE IF NOT EXISTS Type ( " +
                "nomType VARCHAR(100), " +
                "PRIMARY KEY(nomType)); " +
                "CREATE TABLE IF NOT EXISTS User ( " +
                "login VARCHAR(100), " +
                "mdp VARCHAR(100) NOT NULL, " +
                "acces VARCHAR(20), " +
                "PRIMARY KEY(login)); " +
                "INSERT INTO Type (nomType) VALUES " +
                "('Film'), " +
                "('Manga'), " +
                "('Anime'), " +
                "('Série'), " +
                "('Dessin Anime'), " +
                "('Livre'), " +
                "('Jeu Video'), " +
                "('Film Anime'); " +
                "INSERT INTO User (login, mdp, acces) VALUES " +
                "('admin', '21232f297a57a5a743894a0e4a801fc3', '1'); " +
                "COMMIT;";

        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.executeUpdate();
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Base de données réinitialisée." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la réinitialisation de la base de données : "
                    + e.getMessage() + Utils.ANSI_RESET);
        }
    }

    private boolean recordExists(String query, String value) {
        try (PreparedStatement pstmt = this.connection.prepareStatement(query)) {
            pstmt.setString(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Si une ligne est retournée, l'enregistrement existe
            }
        } catch (SQLException e) {
            System.out.println(
                    Utils.ANSI_RED + "[ERROR] Erreur lors de la vérification : " + e.getMessage() + Utils.ANSI_RESET);
        }
        return false;
    }

    public void addType(String type){
        String sql = "INSERT INTO Type (nomType) VALUES (?)";
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.executeUpdate();
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Type " + type + " ajouté." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de l'ajout du type : " + e.getMessage() + Utils.ANSI_RESET);
        }
    }

    public void deleteType(String type){
        String sql = "DELETE FROM Type WHERE nomType = ?";
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.executeUpdate();
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Type " + type + " supprimé." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la suppression du type : " + e.getMessage() + Utils.ANSI_RESET);
        }
    }

    public void deleteLogin(String login){
        String sql = "DELETE FROM User WHERE login = ?";
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, login);
            pstmt.executeUpdate();
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Utilisateur " + login + " supprimé." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la suppression de l'utilisateur : " + e.getMessage() + Utils.ANSI_RESET);
        }
    }

    public void deleteOeuvre(String Oeuvre){
        String sql = "DELETE FROM Oeuvre WHERE nomOeuvre = ?";
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, Oeuvre);
            pstmt.executeUpdate();
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Oeuvre " + Oeuvre + " supprimée." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la suppression de l'oeuvre : " + e.getMessage() + Utils.ANSI_RESET);
        }
    }

}
