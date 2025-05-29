package com.winappverifier.util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.winappverifier.model.AppConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AppChecker {
    private static final Logger logger = LogManager.getLogger(AppChecker.class);

    public static boolean isAppInstalled(AppConfig app) {
        String expectedVersion = extractVersionFromSwId(app.getSw_id());
        String actualVersion = null;

        // 1. Fastest: Registry-based check
        actualVersion = checkNonMSStoreAppInstallation(app.getName());

        // 2. MS Store/UWP
        if (actualVersion == null) {
            actualVersion = checkMSStoreAppInstallation(app.getName());
        }

        // 3. Last resort (slow): WMI
        if (actualVersion == null) {
            actualVersion = getVersionFromCim(app.getName());
        }

        // Final outcome
        if (actualVersion == null) {
            logger.info(" - Installed: NO");
            return false;
        }

        logger.info(" - Installed: YES");
        logger.info(" - Expected Version: " + expectedVersion);
        logger.info(" - Actual Version:   " + actualVersion);

        if (normalizeVersion(actualVersion).equals(normalizeVersion(expectedVersion))) {
            logger.info(" - Version Match: YES");
        } else {
            logger.info(" - Version Match: NO");
        }

        return true;
    }

    private static String checkNonMSStoreAppInstallation(String name) {
        try {
            String command = String.format(
                    "powershell.exe -Command \"@(" +
                            "Get-ItemProperty 'HKLM:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*'," +
                            "'HKLM:\\Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*'" +
                            ") | Where-Object { $_.DisplayName -like '*%s*' } | " +
                            "Select-Object -ExpandProperty DisplayVersion -First 1\"", name
            );

            logger.info("[Registry] Executing:");
            logger.info("-------------------------------------------------------------------------------------");
            logger.info(command);
            logger.info("-------------------------------------------------------------------------------------");

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();

            if (version != null && !version.trim().isEmpty()) {
                logger.info("[Registry] FOUND");
                return version.trim();
            }

            logger.error("[Registry] NOT FOUND");
            return null;

        } catch (Exception e) {
            logger.error("[Registry] ERROR: " + e.getMessage());
            return null;
        }
    }

    private static String checkMSStoreAppInstallation(String name) {
        try {
            String command = String.format(
                    "powershell.exe -Command \"Get-AppxPackage | Where-Object { $_.Name -like '*%s*' } | " +
                            "Select-Object -ExpandProperty Version -First 1\"", name
            );

            logger.info("[Appx] Executing:");
            logger.info("-------------------------------------------------------------------------------------");
            logger.info(command);
            logger.info("-------------------------------------------------------------------------------------");

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();

            if (version != null && !version.trim().isEmpty()) {
                logger.info("[Appx] FOUND");
                return version.trim();
            }

            logger.error("[Appx] NOT FOUND");
            return null;

        } catch (Exception e) {
            logger.info("[Appx] ERROR: " + e.getMessage());
            return null;
        }
    }

    private static String getVersionFromCim(String name) {
        try {
            String command = String.format(
                    "powershell.exe -Command \"Get-CimInstance -ClassName Win32_Product | " +
                            "Where-Object { $_.Name -like '*%s*' } | " +
                            "Select-Object -ExpandProperty Version -First 1\"", name
            );

            logger.info("[CIM] Executing:");
            logger.info("-------------------------------------------------------------------------------------");
            logger.info(command);
            logger.info("-------------------------------------------------------------------------------------");

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();

            if (version != null && !version.trim().isEmpty()) {
                logger.info("[CIM] FOUND");
                return version.trim();
            }

            logger.error("[CIM] NOT FOUND");
            return null;

        } catch (Exception e) {
            logger.error("[CIM] ERROR: " + e.getMessage());
            return null;
        }
    }

    public static String getInstalledVersion(AppConfig app) {
        String version = checkNonMSStoreAppInstallation(app.getName());
        if (version == null) {
            version = checkMSStoreAppInstallation(app.getName());
        }
        if (version == null) {
            version = getVersionFromCim(app.getName());
        }
        return version;
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