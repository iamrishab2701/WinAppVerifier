package com.winappverifier.util;

import com.winappverifier.model.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AppChecker {
    private static final Logger logger = LogManager.getLogger(AppChecker.class);

    public static boolean isAppInstalled(AppConfig app) {
        String expectedVersion = extractVersionFromSwId(app.getSw_id());
        String actualVersion = runPowerShellCheck("Registry", PowerShellCommands.getNonMSStoreAppsRegistryEntry(app.getName()));

        if (actualVersion == null) {
            actualVersion = runPowerShellCheck("Appx", PowerShellCommands.getMSStoreAppEntry(app.getName()));
        }

        if (actualVersion == null) {
            actualVersion = runPowerShellCheck("CIM", PowerShellCommands.getCIMResult(app.getName()));
        }

        if (actualVersion == null) {
            logger.info(" - Installed: NO");
            return false;
        }

        logger.info(" - Installed: YES");
        logger.info(" - Expected Version: {}", expectedVersion);
        logger.info(" - Actual Version:   {}", actualVersion);

        if (normalizeVersion(actualVersion).equals(normalizeVersion(expectedVersion))) {
            logger.info(" - Version Match: YES");
        } else {
            logger.info(" - Version Match: NO");
        }

        return true;
    }

    public static String getInstalledVersion(AppConfig app) {
        String version = runPowerShellCheck("Registry", PowerShellCommands.getNonMSStoreAppsRegistryEntry(app.getName()));
        if (version == null) {
            version = runPowerShellCheck("Appx", PowerShellCommands.getMSStoreAppEntry(app.getName()));
        }
        if (version == null) {
            version = runPowerShellCheck("CIM", PowerShellCommands.getCIMResult(app.getName()));
        }
        return version;
    }

    private static String runPowerShellCheck(String label, String command) {
        logger.info("[{}] Executing:", label);
        logger.info("-------------------------------------------------------------------------------------");
        logger.info(command);
        logger.info("-------------------------------------------------------------------------------------");

        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();

            if (version != null && !version.trim().isEmpty()) {
                logger.info("[{}] FOUND", label);
                return version.trim();
            }

            logger.warn("[{}] NOT FOUND", label);
        } catch (Exception e) {
            logger.error("[{}] ERROR: {}", label, e.getMessage());
        }

        return null;
    }

    public static String extractVersionFromSwId(String swId) {
        if (swId == null || !swId.contains("_")) return "";
        String[] parts = swId.split("_");
        return parts[parts.length - 1];
    }

    public static String normalizeVersion(String version) {
        if (version == null) return "";
        return version.trim().replaceAll("\\.0+$", "").toLowerCase();
    }
}