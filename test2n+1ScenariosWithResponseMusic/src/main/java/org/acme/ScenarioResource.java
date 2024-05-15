package org.acme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.json.JSONObject;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/scenarios")
public class ScenarioResource {

    @Inject 
    ServiceImpl serviceImpl;

    @Inject
    CurlRequest curlRequest;

    @Inject
    CurlRequest1 curlRequest1;

    // // @Inject
    // // @RestClient
    // // ElasticSearchUploaderClient csvRestClient;

    @Inject
    ElasticSearchUploader elasticSearchUploader;
    @Inject 
    DockerComposeRunner dockerComposeRunner;

    @Inject
    @ConfigProperty(name = "file.base.path")
    String baseFilePath;

    @Inject
    @ConfigProperty(name = "file.default.keyword")
    String defaultKeyword;


    String logstashConfigPath = "/usr/share/logstash/pipeline/logstash.conf";
    


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@QueryParam("keyword") String keyword) throws IOException, InterruptedException {
        String dynamicFilePath = constructDynamicFilePath(keyword);
      
        String jsonContent = new String(Files.readAllBytes(Paths.get(dynamicFilePath)));
        JSONObject jsonObject = new JSONObject(jsonContent);
        List<JSONObject> scenarios = serviceImpl.generateScenarios(jsonObject.toString());

        serviceImpl.writeScenariosToJsonFiles(scenarios);
        serviceImpl.writeScenariosToCSV("output.csv", scenarios);

        // curlRequest.process(scenarios.size(),keyword);
        curlRequest1.process(scenarios.size(),keyword);

        // dockerComposeRunner.runDockerComposeUp();

        // elasticSearchUploader.uploadCsvToElasticsearch("output.csv");
        
        // elasticSearchUploader.runLogstash(logstashConfigPath);

        return "Hello from RESTEasy Reactive using file with keyword: " + keyword;
    }

    private String constructDynamicFilePath(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            keyword = defaultKeyword;
        }
        return baseFilePath + keyword + ".json";
    }
}
