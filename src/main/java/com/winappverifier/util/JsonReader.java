package com.winappverifier.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winappverifier.model.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JsonReader {
    private static final Logger logger = LogManager.getLogger(AppLauncher.class);
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
            logger.error("[ERROR] Failed to read app config: " + e.getMessage());
        }
        return apps;
    }
}