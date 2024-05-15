package org.acme;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceImpl {


    public List<JSONObject> generateScenarios(String jsonString) {
        List<JSONObject> scenarios = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(jsonString);
        boolean flag = false;
    
        scenarios.add(jsonObject);
    
        for (String arrayName : getArrayNames(jsonObject)) {
            if (jsonObject.has(arrayName) && jsonObject.get(arrayName) instanceof JSONArray) {
                flag = true;
                JSONArray jsonArray = jsonObject.getJSONArray(arrayName);
                for (int i = 0; i < jsonArray.length(); i++) {
                    scenarios.addAll(generateParameterScenarios1(jsonObject, arrayName, i));
                }
            }
        }
    
        System.out.println(flag + "11111");
    
        if (flag) {
            return scenarios;
        } else {
            return generateScenarios2(jsonString);
        }
    }

    private List<String> getArrayNames(JSONObject jsonObject) {
        List<String> arrayNames = new ArrayList<>();
        for (String key : jsonObject.keySet()) {
            if (jsonObject.get(key) instanceof JSONArray) {
                arrayNames.add(key);
            }
        }
        
        return arrayNames;
    }

    public List<JSONObject> generateParameterScenarios1(JSONObject baseJsonObject, String arrayName, int arrayIndex) {
        List<JSONObject> scenariosList = new ArrayList<>();

        int numBaseParameters = baseJsonObject.length();
        for (int i = 0; i < numBaseParameters; i++) {
            String paramName = baseJsonObject.names().getString(i);

            JSONObject nullScenario = new JSONObject(baseJsonObject.toString());
            nullScenario.put(paramName, JSONObject.NULL);
            scenariosList.add(nullScenario);

            JSONObject invalidScenario = new JSONObject(baseJsonObject.toString());
            invalidScenario.put(paramName, "####");
            scenariosList.add(invalidScenario);
        }

        if (baseJsonObject.has(arrayName) && baseJsonObject.get(arrayName) instanceof JSONArray) {
            JSONArray array = baseJsonObject.getJSONArray(arrayName);
            if (array.length() > arrayIndex) {
                JSONObject arrayElement = array.getJSONObject(arrayIndex);
                for (int i = 0; i < arrayElement.length(); i++) {
                    String paramName = arrayElement.names().getString(i);

                    JSONObject nullScenario = new JSONObject(baseJsonObject.toString());
                    nullScenario.getJSONArray(arrayName).getJSONObject(arrayIndex).put(paramName, JSONObject.NULL);
                    scenariosList.add(nullScenario);

                    JSONObject invalidScenario = new JSONObject(baseJsonObject.toString());
                    invalidScenario.getJSONArray(arrayName).getJSONObject(arrayIndex).put(paramName, "####");
                    scenariosList.add(invalidScenario);
                }
            }
        }

        return scenariosList;
    }

    public List<JSONObject> generateScenarios2(String jsonString) {
        List<JSONObject> scenarios = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(jsonString);
        scenarios.add(jsonObject);
        scenarios.addAll(generateParameterScenarios2(jsonObject));
        return scenarios;
    }

    public List<JSONObject> generateParameterScenarios2(JSONObject baseJsonObject) {
        List<JSONObject> scenariosList = new ArrayList<>();
    
        for (String paramName : baseJsonObject.keySet()) {
            Object paramValue = baseJsonObject.get(paramName);
    
            if (paramValue instanceof JSONObject) {
                
                JSONObject nestedObject = (JSONObject) paramValue;

                for (JSONObject nestedScenario : generateParameterScenarios2(nestedObject)) {
                    
                    JSONObject mergedScenario = new JSONObject(baseJsonObject.toString());
                    mergedScenario.put(paramName, nestedScenario);
                    scenariosList.add(mergedScenario);
                }
            } else {
                JSONObject nullScenario = new JSONObject(baseJsonObject.toString());
                nullScenario.put(paramName, JSONObject.NULL);
                scenariosList.add(nullScenario);
    
                JSONObject invalidScenario = new JSONObject(baseJsonObject.toString());
                invalidScenario.put(paramName, "####");
                scenariosList.add(invalidScenario);
            }
        }
    
        return scenariosList;
    }

    public void writeScenariosToJsonFiles(List<JSONObject> scenarios) {
        for (int i = 0; i < scenarios.size(); i++) {
            writeScenarioJsonToFile("output" + (i + 1) + ".json", scenarios.get(i));
        }
    }

    public void writeScenarioJsonToFile(String fileName, JSONObject scenario) {
        try (PrintWriter fileWriter = new PrintWriter(new FileWriter(fileName))) {
            fileWriter.println(scenario.toString(2));
            System.out.println("Saved " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeScenariosToCSV(String fileName, List<JSONObject> scenarios) {
        try (PrintWriter csvWriter = new PrintWriter(new FileWriter(fileName),true)) {
            for (JSONObject scenario : scenarios) {
                csvWriter.println(scenario.toString());
                csvWriter.flush();
            }
            System.out.println("Saved " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
