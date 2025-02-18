import java.util.Scanner;

public class App {

    Scanner input;
    Utils utils;
    database database;

    public App() {
        utils = new Utils();

        database = new database("/var/lib/judgementday/database_oeuvres.db");
        database.connectDatabase();

        System.out.println("\nBienvenue dans l'application de gestion de base de données de JugementDay !\n");
        System.out.println("Que voulez-vous faire ?");
        System.out.println("1. Remplir la base de données à l'aide de table.csv.");
        System.out.println("2. Promouvoir un utilisateur à Administrateur.");
        System.out.println("3. Rétrograder un Administrateur à utilisateur.");
        System.out.println("4. Ajouter un élément.");
        System.out.println("5. Supprimer un élément.");
        System.out.println("6. Réinitialiser la base de données.");
        System.out.println("7. Faire une backup de la base de donées.");
        System.out.println("8. Quitter l'application.");

        input = new Scanner(System.in);
        int choice = input.nextInt();

        switch (choice) {
            case 1:
                utils.fillDatabase(database);
                break;
            case 2:
                System.out.println("Entrez le login de l'utilisateur à promouvoir :");
                Scanner login = new Scanner(System.in);
                utils.promoteUser(login.nextLine(), database);
                break;
            case 3:
                System.out.println("Entrez le login de l'administateur à rétrograder :");
                Scanner admin = new Scanner(System.in);
                utils.retrogradeAdmin(admin.nextLine(), database);
                break;
            case 4:
                utils.addElement(database);
                break;
            case 5:
                utils.deleteElement(database);
                break;
            case 6:
                System.out.println("Êtes-vous sûr de vouloir réinitialiser la base de données ? (y/n)");
                Scanner confirm = new Scanner(System.in);
                String response = confirm.nextLine();
                if (response.equals("y")) {
                    utils.resetDatabase(database);
                } else {
                    System.out.println("Réinitialisation annulée.");
                }
                break;
            case 7:
                utils.backupDatabase(database);
                break;
            case 8:
                System.out.println("Au revoir !");
                System.exit(0);
                break;
            default:
                System.out.println("Choix invalide.");
                break;
        }

    }

    public static void main(String[] args) {
        new App();
    }
}
