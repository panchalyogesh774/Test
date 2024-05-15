package org.acme;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.StandardOpenOption;

public class CsvFileHandler {

    private static final String DELIMITER = ",";
    private static final String CSV_HEADER_PREFIX = "Request Data For";
    private static Map<String, String> dynamicHeaders = new LinkedHashMap<>();

    public static String formatDataLine(String jsonContent, HttpResponse<String> response) {
        String requestData = "";
        String responseData = response.body();

        if (responseData.contains(":")) {
            String[] responseParts = responseData.split(":");
            if (responseParts.length > 1) {
                String[] requestParams = responseParts[1].replaceAll("[^a-zA-Z0-9,]", "").trim().split(",");
                for (String param : requestParams) {
                    dynamicHeaders.put(param, ""); // Initialize with empty values
                    requestData += param + DELIMITER;
                }
            }
        }

        // Populate values from the input JSON
        Map<String, String> inputValues = extractInputValues(jsonContent);
        requestData += String.join(DELIMITER, inputValues.values()) + DELIMITER;

        String dataLine = CSV_HEADER_PREFIX + ":" + String.join(DELIMITER, dynamicHeaders.keySet()) + DELIMITER;

        // Add placeholders for columns
        dataLine += response.statusCode() + DELIMITER
                + response.body().replace("\n", "");

        dynamicHeaders.clear(); // Clear headers for the next request

        return dataLine;
    }

    public static void saveToCsv(String dataLine, Path outputPath) throws IOException {
        if (!Files.exists(outputPath)) {
            Files.createFile(outputPath);
            Files.writeString(outputPath, dataLine + "\n", StandardOpenOption.APPEND);
        } else {
            Files.writeString(outputPath, dataLine + "\n", StandardOpenOption.APPEND);
        }
    }

    private static Map<String, String> extractInputValues(String jsonContent) {
        // Extract values from the input JSON
        Map<String, String> inputValues = new LinkedHashMap<>();
        JSONObject jsonObject = new JSONObject(jsonContent);
        for (String header : dynamicHeaders.keySet()) {
            inputValues.put(header, jsonObject.optString(header, ""));
        }
        return inputValues;
    }
}
