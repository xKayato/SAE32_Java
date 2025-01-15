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

    /*
     * Connexion à la base de données SQLite
     */
    public void connectDatabase() {
        String url = "jdbc:sqlite:" + this.file; // URL de la base de données
        try {
            this.connection = DriverManager.getConnection(url); // Connexion à la base de données
            System.out
                    .println(Utils.ANSI_GREEN + "[SUCESS] Connexion à la base de données établie." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la connexion à la base de données : "
                    + e.getMessage() + Utils.ANSI_RESET);
        }
    }

    /*
     * Insère les données d'un fichier JSON dans la base de données
     */
    public void insertIntoDb(JSONObject json) {
        String sqlOeuvre = "INSERT INTO Oeuvre (nomOeuvre, dateSortie, auteur_studio, actif, type) VALUES (?, ?, ?, ?, ?)"; // Requête SQL
        String sqlType = "INSERT INTO Type (nomType) VALUES (?)"; // Requête SQL
        String checkType = "SELECT 1 FROM Type WHERE nomType = ?"; // Requête SQL
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


    /*
     * Promouvoir un utilisateur à Administrateur (acces = 1)
     */
    public void promoteUser(String login) {
        String sqlUpdate = "UPDATE User SET acces = 1 WHERE login = ?";
        try (PreparedStatement pstmt = this.connection.prepareStatement(sqlUpdate)) {
            pstmt.setString(1, login);
            pstmt.executeUpdate();
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Utilisateur " + login + " promu en Administrateur."
                    + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la promotion de l'utilisateur : "
                    + e.getMessage() + Utils.ANSI_RESET);
        }
    }


    /*
     * Réinitialise la base de données en supprimant toutes les tables et en les recréant. Certaines données sont remises
     */
    public void resetDatabase() {
        // Script SQL, chaque commande séparée par un point-virgule
        String[] sqlCommands = {
                "DROP TABLE IF EXISTS Avis",
                "DROP TABLE IF EXISTS Oeuvre",
                "DROP TABLE IF EXISTS Type",
                "DROP TABLE IF EXISTS User",
                "CREATE TABLE IF NOT EXISTS Avis ( " +
                        "idAvis INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "texteAvis TEXT, " +
                        "note INT, " +
                        "date DATE, " +
                        "idOeuvre INT, " +
                        "login VARCHAR(100), " +
                        "FOREIGN KEY(idOeuvre) REFERENCES Oeuvre(idOeuvre) ON DELETE CASCADE, " +
                        "FOREIGN KEY(login) REFERENCES User(login) ON DELETE CASCADE)",
                "CREATE TABLE IF NOT EXISTS Oeuvre ( " +
                        "idOeuvre INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "nomOeuvre VARCHAR(100), " +
                        "dateSortie DATE, " +
                        "actif BOOLEAN, " +
                        "auteur_studio VARCHAR(100), " +
                        "type TEXT)",
                "CREATE TABLE IF NOT EXISTS Type ( " +
                        "nomType VARCHAR(100) PRIMARY KEY)",
                "CREATE TABLE IF NOT EXISTS User ( " +
                        "login VARCHAR(100) PRIMARY KEY, " +
                        "mdp VARCHAR(100) NOT NULL, " +
                        "acces VARCHAR(20))",
                "INSERT INTO Type (nomType) VALUES " +
                        "('Film'), ('Manga'), ('Anime'), ('Série'), " +
                        "('Dessin Anime'), ('Livre'), ('Jeu Video'), ('Film Anime')",
                "INSERT INTO User (login, mdp, acces) VALUES " +
                        "('admin', '21232f297a57a5a743894a0e4a801fc3', '1')"
        };

        try {
            connection.setAutoCommit(false); // Désactiver l'auto-commit pour garantir l'intégrité des transactions

            for (String sql : sqlCommands) {
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.executeUpdate();
                }
            }

            connection.commit(); // Valider les changements
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Base de données réinitialisée." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            try {
                connection.rollback(); // Annuler les changements en cas d'erreur
            } catch (SQLException rollbackEx) {
                System.out.println(
                        Utils.ANSI_RED + "[ERROR] Échec du rollback : " + rollbackEx.getMessage() + Utils.ANSI_RESET);
            }
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la réinitialisation de la base de données : "
                    + e.getMessage() + Utils.ANSI_RESET);
        } finally {
            try {
                connection.setAutoCommit(true); // Réactiver l'auto-commit
            } catch (SQLException ex) {
                System.out.println(Utils.ANSI_RED + "[ERROR] Impossible de réactiver l'auto-commit : " + ex.getMessage()
                        + Utils.ANSI_RESET);
            }
        }
    }

    /*
     * Vérifie si un enregistrement existe dans la base de données (utilisé pour éviter les doublons)
     */
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

    /*
     * Ajoute un type à la base de données
     */
    public void addType(String type) {
        String sql = "INSERT INTO Type (nomType) VALUES (?)"; // Requête SQL
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, type); // Remplace le premier point d'interrogation par le type
            pstmt.executeUpdate(); // Exécute la requête
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Type " + type + " ajouté." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(
                    Utils.ANSI_RED + "[ERROR] Erreur lors de l'ajout du type : " + e.getMessage() + Utils.ANSI_RESET);
        }
    }

    /*
     * Ajoute un utilisateur à la base de données
     */
    public void deleteType(String type) {
        String sql = "DELETE FROM Type WHERE nomType = ?"; // Requête SQL
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, type); // Remplace le premier point d'interrogation par le type
            pstmt.executeUpdate(); // Exécute la requête
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Type " + type + " supprimé." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la suppression du type : " + e.getMessage()
                    + Utils.ANSI_RESET);
        }
    }

    /*
     * Ajoute un utilisateur à la base de données
     */
    public void deleteLogin(String login) {
        String sql = "DELETE FROM User WHERE login = ?"; // Requête SQL
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, login); // Remplace le premier point d'interrogation par le login
            pstmt.executeUpdate(); // Exécute la requête
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Utilisateur " + login + " supprimé." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la suppression de l'utilisateur : "
                    + e.getMessage() + Utils.ANSI_RESET);
        }
    }

    /*
     * Supprime une oeuvre de la base de données
     */
    public void deleteOeuvre(String Oeuvre) {
        String sql = "DELETE FROM Oeuvre WHERE nomOeuvre = ?"; // Requête SQL
        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {
            pstmt.setString(1, Oeuvre); // Remplace le premier point d'interrogation par le nom de l'oeuvre
            pstmt.executeUpdate(); // Exécute la requête
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Oeuvre " + Oeuvre + " supprimée." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la suppression de l'oeuvre : " + e.getMessage()
                    + Utils.ANSI_RESET);
        }
    }

    /*
     * Sauvegarde la base de données dans un fichier backup.db
     */
    public void backupDatabase() {
        String backupFile = "/var/lib/judgementday/backup.db"; // Emplacement de la sauvegarde
        String url = "jdbc:sqlite:" + backupFile; // URL de la sauvegarde
        try (Connection backupConnection = DriverManager.getConnection(url)) {
            connection.createStatement().execute("backup to " + backupFile); // Sauvegarde la base de données (commande SQLite)
            System.out.println(Utils.ANSI_GREEN + "[SUCCESS] Base de données sauvegardée." + Utils.ANSI_RESET);
        } catch (SQLException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la sauvegarde de la base de données : "
                    + e.getMessage() + Utils.ANSI_RESET);
        }
    }

}
