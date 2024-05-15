package org.acme;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;
// import org.elasticsearch.core.Map;
import org.json.JSONObject;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class CurlRequest1 {

    @Inject
    @ConfigProperty(name = "file.default.keyword")
    static String defaultKeyword;

    // @Inject
    // CsvFileHandler csvFileHandler;

    public static void process(int jsonOutputSize, String keyword) throws IOException, InterruptedException {

        keyword = keyword.replace("/input", "");

        if (keyword == null) {
            keyword = defaultKeyword;
        }

        String url = "http://mtainternal.finobank.keenable.in/" + keyword;
        String outputFile;
        System.out.println("Request URL: " + url);

        for (int i = 0; i < jsonOutputSize; i++) {
            System.out.println("output" + (i + 1) + ".json");
            outputFile = "output" + (i + 1) + ".json";

            Path outputPath = Paths.get("outputResponse.csv");

            if (!Files.exists(outputPath)) {
                Files.createFile(outputPath);
            }
            Path jsonFilePath = Paths.get(outputFile);
            String jsonContent = Files.readString(jsonFilePath).replace("\n", "");

            // HttpClient httpClient = HttpClient.newHttpClient();
            // HttpRequest request = HttpRequest.newBuilder()
            // .header("Content-Type", "application/json")
            // .uri(URI.create(url))
            // .version(HttpClient.Version.HTTP_1_1)
            // .GET()
            // .build();

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonContent)) // Use POST method with request body
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String responseData = response.body();
            // JSONObject jsonResponse = new JSONObject(responseData);
            // // String message = jsonResponse.getString("message");
            // String message;

            // if (jsonResponse.has("message")) {
            //     message = jsonResponse.getString("message");
            // } else {
            //     message = "success";
            // }

            System.out.println("Request URLxxxxxxxxxxxxxx: " + url);
            System.out.println("Request URLxxxxxxxxxxxxxx: " + request.uri());
            System.out.println("Request Bodyxxxxxxxxxxxxxxxxx: " + jsonContent);
            System.out.println("Response Codexxxxxxxxxxxxx: " + response.statusCode());
            System.out.println("Response Bodyxxxxxxxxxxxxx: " + responseData);
            System.out.println("Response Body dirct aaaaaaaaaaaa"+response.body());
            System.out.println("Statusxxxxxxxxxxxxxx: " + analyzeResponse(responseData));
            System.out.println("Endpointxxxxxxxxxxxxxxxx: " + response.uri().getPath());
           

            
           
            String dataLine = "Request Data For: "+processJsonData(jsonContent) +"|";
            // + "Message: " + message + "|"
            dataLine += jsonContent + "|"
                    // + "Message: " + message + "|"
                    + "Response Code: " + response.statusCode() + "|"
                    + "Response Body: " + response.body().replace("\n", "") + "|"
                    + "Endpoint: " + response.uri().getPath() + "|\n";

            Files.writeString(outputPath, dataLine, java.nio.file.StandardOpenOption.APPEND);

        }

    }

    private int jsonOutputSize;

    public void processJsonOutputSize(int size) {
        this.jsonOutputSize = size;
    }

    public static String analyzeResponse(String response) {
        String responseLowerCase = response.toLowerCase();

        if (responseLowerCase.contains("invalid")) {
            return "invalid";
        } else if (responseLowerCase.contains("valid")) {
            return "valid";
        } else if (responseLowerCase.contains("null")) {
            return "null";
        } else {
            return "unknown";
        }
    }

    // public static Map<String, Object> processJsonData(String jsonData) {
    //     JSONObject jsonObject = new JSONObject(jsonData);
    //     return processJsonObject(jsonObject);
    // }

    public static Map<String, Object> processJsonData(String jsonData) {
        JSONObject jsonObject = new JSONObject(jsonData);
        Map<String, Object> keyValuePairs = new HashMap<>();
        boolean hasNullOrPlaceholder = false;
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value == null || "####".equals(value)) {
                hasNullOrPlaceholder = true;
                keyValuePairs.put(key, value);
            } else if (JSONObject.NULL.equals(value)) {
                hasNullOrPlaceholder = true;
                keyValuePairs.put(key, null);
            }
            
        }
        if (!hasNullOrPlaceholder) {
            keyValuePairs.put("All Values", "Correct");
        }

        return keyValuePairs;
    }

    
}
