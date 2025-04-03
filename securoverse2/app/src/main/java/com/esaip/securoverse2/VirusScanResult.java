package com.esaip.securoverse2;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VirusScanResult {
    private final String status;
    private final Map<String, EngineResult> results;

    public VirusScanResult(String status, Map<String, EngineResult> results) {
        this.status = status;
        this.results = results;
    }

    public static VirusScanResult fromJson(JSONObject json) throws JSONException {
        JSONObject attributes = json.getJSONObject("data").getJSONObject("attributes");
        String status = attributes.getString("status");

        Map<String, EngineResult> results = new HashMap<>();
        JSONObject resultsJson = attributes.getJSONObject("results");
        Iterator<String> keys = resultsJson.keys();

        while (keys.hasNext()) {
            String engineName = keys.next();
            JSONObject engineResult = resultsJson.getJSONObject(engineName);
            String category = engineResult.getString("category");
            String result = engineResult.optString("result", "clean");

            results.put(engineName, new EngineResult(category, result));
        }

        return new VirusScanResult(status, results);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Statut : ").append(status).append("\n\n");

        int malicious = 0;
        int suspicious = 0;
        int clean = 0;

        for (Map.Entry<String, EngineResult> entry : results.entrySet()) {
            EngineResult result = entry.getValue();
            switch (result.category.toLowerCase()) {
                case "malicious":
                    malicious++;
                    break;
                case "suspicious":
                    suspicious++;
                    break;
                case "clean":
                    clean++;
                    break;
            }
        }

        sb.append("Résumé :\n");
        sb.append("Malveillant : ").append(malicious).append("\n");
        sb.append("Suspect : ").append(suspicious).append("\n");
        sb.append("Propre : ").append(clean).append("\n");

        return sb.toString();
    }

    public static class EngineResult {
        final String category;
        final String result;

        EngineResult(String category, String result) {
            this.category = category;
            this.result = result;
        }
    }
}