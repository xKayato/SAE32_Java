import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;


public class ToJSON {

    private final String csvFile;

    public ToJSON(String csvFile) {
        this.csvFile = csvFile;
        }

        public JSONObject csvToJson() {
        JSONObject json = new JSONObject();
        JSONArray dataArray = new JSONArray();

        try {
            List<String> lines = Files.readAllLines(Paths.get(csvFile));
            if (lines.isEmpty()) {
                System.out.println(Utils.ANSI_YELLOW + "[ERROR] Le fichier CSV est vide." + Utils.ANSI_RESET);
                return json;
            }

            // Avoir les entêtes
            String[] headers = lines.get(0).split(";");
            for (int i = 1; i < lines.size(); i++) {
                String[] data = lines.get(i).split(";");
                if (data.length == headers.length) {
                    // Créer un objet JSON pour chaque ligne
                    JSONObject jsonObject = new JSONObject();
                    for (int j = 0; j < headers.length; j++) {
                        String header = headers[j].trim();
                        String value = data[j].trim();
                        // Ajouter les données dans l'objet JSON
                        jsonObject.put(header, value);
                        
                    }
                    dataArray.put(jsonObject);
                } else {
                    // Ignorer les lignes qui ne correspondent pas au format
                    System.out.println(Utils.ANSI_YELLOW + "[WARNING] Ligne ignorée : " + lines.get(i) + Utils.ANSI_RESET);
                }
            }
            json.put("data", dataArray);
        } catch (IOException e) {
            System.out.println(Utils.ANSI_RED + "[ERROR] Erreur lors de la lecture du fichier : " + e.getMessage() + Utils.ANSI_RESET);
        }

        return json;
    }
}
