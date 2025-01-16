import java.util.Scanner;
import org.json.JSONObject;

public class Utils {

    ToJSON toJSON;
    // Couleurs pour les messages
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_ORANGE = "\u001B[38;5;208m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public void fillDatabase(database database) {
        System.out.println(ANSI_CYAN + "[INFO] Remplissage de la base de données en cours..." + ANSI_RESET);
        toJSON = new ToJSON("table.csv");
        JSONObject json = toJSON.csvToJson();
        if (json.length() > 0) {
            System.out.println(ANSI_CYAN + "[INFO] Données à importer : " + json.getJSONArray("data") + ANSI_RESET);
            database.insertIntoDb(json);
        } else {
            System.out.println(ANSI_CYAN + "[INFO] Pas de donnée à importer." + ANSI_RESET);
        }
    }

    public void promoteUser(String login, database database) {
        System.out.println(ANSI_CYAN + "[INFO] Promotion de l'utilisateur en cours..." + ANSI_RESET);
        database.promoteUser(login);
    }

    public void resetDatabase(database database) {
        System.out.println(ANSI_CYAN + "[INFO] Réinitialisation de la base de données en cours..." + ANSI_RESET);
        database.resetDatabase();
    }

    public void addElement(database database) {
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("Que voulez-vous ajouter ?");
            System.out.println("1. Ajouter un type");
            System.out.print("Votre choix : ");
            int choice = input.nextInt();
            input.nextLine();

            switch (choice) {
                case 1:
                    addType(database, input);
                    break;
                default:
                    System.out.println("Choix invalide.");
                    break;
            }
        } catch (Exception e) {
            System.out.println(ANSI_RED + "Erreur : Entrée invalide." + ANSI_RESET);
        }
    }

    public void addType(database database, Scanner input) {
        System.out.print("Entrez le nom du type : ");
        String type = input.nextLine();
        database.addType(type);
    }

    public void deleteElement(database database) {
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("Que voulez-vous supprimer ?");
            System.out.println("1. Supprimer un type");
            System.out.println("2. Supprimer une oeuvre");
            System.out.println("3. Supprimer un utilisateur");
            System.out.print("Votre choix : ");
            int choice = input.nextInt();
            input.nextLine();

            switch (choice) {
                case 1:
                    deleteType(database, input);
                    break;
                case 2:
                    deleteOeuvre(database, input);
                    break;
                case 3:
                    deleteUser(database, input);
                    break;
                default:
                    System.out.println("Choix invalide.");
                    break;
            }
        } catch (Exception e) {
            System.out.println(ANSI_RED + "Erreur : Entrée invalide." + ANSI_RESET);
        }
    }

    public void deleteType(database database, Scanner input) {
        System.out.print("Entrez le nom du type à supprimer : ");
        String type = input.nextLine();
        database.deleteType(type);
    }

    public void deleteOeuvre(database database, Scanner input) {
        System.out.print("Entrez le nom de l'oeuvre à supprimer : ");
        String oeuvre = input.nextLine();
        database.deleteOeuvre(oeuvre);
    }

    public void deleteUser(database database, Scanner input) {
        System.out.print("Entrez le login de l'utilisateur à supprimer : ");
        String login = input.nextLine();
        database.deleteLogin(login);
    }

    public void backupDatabase(database database) {
        System.out.println(ANSI_CYAN + "[INFO] Backup de la base de données en cours..." + ANSI_RESET);
        database.backupDatabase();
    }

    public void retrogradeAdmin(String login, database database) {
        System.out.println(ANSI_CYAN + "[INFO] Rétrogradation de l'administrateur en cours..." + ANSI_RESET);
        database.retrogradeAdmin(login);
    }
}
