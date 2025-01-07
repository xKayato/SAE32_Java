import org.json.JSONObject;

public class App {

    database database;
    ToJSON ToJSON;

    public App() {
        database = new database("databse_oeuvres.db");
        database.connectDatabase();

        ToJSON = new ToJSON("table.csv");

        

        JSONObject json = ToJSON.csvToJson();
        if (json.length() > 0) {
            System.out.println(json);
            database.insertIntoDb(json);
        } else {
            System.out.println("Pas de donnée à importer.");
        }
    }

    public static void main(String[] args) {
        new App();
    }
}
