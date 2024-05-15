package org.acme;

import org.json.JSONObject;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@ApplicationScoped
public class ElasticSearchUploader {

    public static void uploadCsvToElasticsearch(String csvFileName) throws IOException, InterruptedException {
        String elasticsearchUrl = "http://localhost:9200/elts";
        deleteIndex(elasticsearchUrl,"");
        // createIndexPattern("http://localhost:9200", "elts");
        

        uploadNewData(csvFileName, elasticsearchUrl);
    }

    

    public static void createIndexPattern(String elasticsearchUrl, String indexPatternName) throws IOException, InterruptedException {
        String kibanaIndexPatternEndpoint = elasticsearchUrl + "/.kibana/_doc/index-pattern-" + indexPatternName;

        String requestBody = "{ \"type\": \"index-pattern\", \"index-pattern\": { \"title\": \"" + indexPatternName + "\", \"timeFieldName\": \"@timestamp\" } }";

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(kibanaIndexPatternEndpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Create Index Pattern Response Code: " + response.statusCode());
        System.out.println("Create Index Pattern Response Body: " + response.body());
    }
    
    

    private static void deleteIndex(String elasticsearchUrl, String indexName) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(elasticsearchUrl + "/" + indexName))
                .DELETE()
                .build();
    
        HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
    
        System.out.println("Delete Response Code: " + deleteResponse.statusCode());
        System.out.println("Delete Response Body: " + deleteResponse.body());
    }

    private static void uploadNewData(String csvFileName, String elasticsearchUrl) throws IOException, InterruptedException {
        elasticsearchUrl += "/_bulk";
        
        Path csvFilePath = Paths.get(csvFileName);
        List<String> jsonLines = Files.readAllLines(csvFilePath);

        StringBuilder bulkRequestBody = new StringBuilder();

        for (String jsonLine : jsonLines) {
            JSONObject jsonDocument = new JSONObject(jsonLine);

            bulkRequestBody.append("{ \"index\" : { \"_index\" : \"elts\" } }\n");
            bulkRequestBody.append(jsonDocument.toString()).append("\n");

            System.out.println("JSON Document: " + jsonDocument.toString());
        }

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(elasticsearchUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bulkRequestBody.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Upload Response Code: " + response.statusCode());
        System.out.println("Upload Response Body: " + response.body());
    }



    public static void runLogstash(String logstashConfigPath) throws IOException, InterruptedException {
        String logstashCommand = "logstash";

        String[] command = {logstashCommand, "-f", logstashConfigPath };

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("Logstash: " + line);
        }

        int exitCode = process.waitFor();

        System.out.println("Logstash process exited with code: " + exitCode);
    }


   
}



    

    
   















// package org.acme;

// import org.eclipse.microprofile.config.inject.ConfigProperty;
// import org.eclipse.microprofile.rest.client.inject.RestClient;
// import org.json.JSONObject;

// import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.inject.Inject;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.util.List;

// @ApplicationScoped
// public class ElasticSearchUploader {

//     @Inject
//     @RestClient
//     ElasticSearchUploaderClient elasticSearchUploaderClient;


//     @Inject
//     @ConfigProperty(name = "elastic.search.url")
//     String elasticSearchUrl;

//     public void uploadCsvToElasticsearch(String csvFileName) throws IOException {
//         Path csvFilePath = Paths.get(csvFileName);
//         List<String> jsonLines = Files.readAllLines(csvFilePath);

//         for (String jsonLine : jsonLines) {
//             JSONObject jsonDocument = new JSONObject(jsonLine);

//             StringBuilder bulkRequestBody = new StringBuilder();
//             bulkRequestBody.append("{ \"index\" : { \"_index\" : \"elts\" } }\n");
//             bulkRequestBody.append(jsonDocument.toString()).append("\n");

//             elasticSearchUploaderClient.uploadCsvToElasticsearch(List.of(bulkRequestBody.toString()));
//             System.out.println("JSON Document: " + jsonDocument.toString());
//         }
//     }
// }

























