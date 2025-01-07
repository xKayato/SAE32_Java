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
                System.out.println("Le fichier CSV est vide.");
                return json;
            }

            String[] headers = lines.get(0).split(";");
            for (int i = 1; i < lines.size(); i++) {
                String[] data = lines.get(i).split(";");
                if (data.length == headers.length) {
                    JSONObject jsonObject = new JSONObject();
                    for (int j = 0; j < headers.length; j++) {
                        String header = headers[j].trim();
                        String value = data[j].trim();

                        // Convert "tags" column into JSONArray
                        if (header.equalsIgnoreCase("tags")) {
                            JSONArray tagsArray = new JSONArray();
                            for (String tag : value.split(",")) {
                                tagsArray.put(tag.trim());
                            }
                            jsonObject.put(header, tagsArray);
                        } else {
                            jsonObject.put(header, value);
                        }
                    }
                    dataArray.put(jsonObject);
                } else {
                    System.out.println("Ligne ignorée : " + lines.get(i));
                }
            }
            json.put("data", dataArray);
        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture du fichier : " + e.getMessage());
        }

        return json;
    }
}
