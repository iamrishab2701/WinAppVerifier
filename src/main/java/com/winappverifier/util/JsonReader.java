package com.winappverifier.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winappverifier.model.AppConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JsonReader {

    public static List<AppConfig> readApps(String filePath) {
        List<AppConfig> apps = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File(filePath));
            JsonNode appsNode = root.get("apps");

            if (appsNode != null && appsNode.isArray()) {
                for (JsonNode node : appsNode) {
                    AppConfig app = mapper.treeToValue(node, AppConfig.class);
                    apps.add(app);
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to read app config: " + e.getMessage());
        }
        return apps;
    }
}