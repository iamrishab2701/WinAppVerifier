package com.winappverifier.util;

import com.winappverifier.model.AppConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AppChecker {

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
            System.out.println(" - Installed: NO");
            return false;
        }

        System.out.println(" - Installed: YES");
        System.out.println(" - Expected Version: " + expectedVersion);
        System.out.println(" - Actual Version:   " + actualVersion);

        if (normalizeVersion(actualVersion).equals(normalizeVersion(expectedVersion))) {
            System.out.println(" - Version Match: YES");
        } else {
            System.out.println(" - Version Match: NO");
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

            System.out.println("[Registry] Executing:");
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println(command);
            System.out.println("-------------------------------------------------------------------------------------");

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();

            if (version != null && !version.trim().isEmpty()) {
                System.out.println("[Registry] FOUND");
                return version.trim();
            }

            System.out.println("[Registry] NOT FOUND");
            return null;

        } catch (Exception e) {
            System.err.println("[Registry] ERROR: " + e.getMessage());
            return null;
        }
    }

    private static String checkMSStoreAppInstallation(String name) {
        try {
            String command = String.format(
                    "powershell.exe -Command \"Get-AppxPackage | Where-Object { $_.Name -like '*%s*' } | " +
                            "Select-Object -ExpandProperty Version -First 1\"", name
            );

            System.out.println("[Appx] Executing:");
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println(command);
            System.out.println("-------------------------------------------------------------------------------------");

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();

            if (version != null && !version.trim().isEmpty()) {
                System.out.println("[Appx] FOUND");
                return version.trim();
            }

            System.out.println("[Appx] NOT FOUND");
            return null;

        } catch (Exception e) {
            System.err.println("[Appx] ERROR: " + e.getMessage());
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

            System.out.println("[CIM] Executing:");
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println(command);
            System.out.println("-------------------------------------------------------------------------------------");

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();

            if (version != null && !version.trim().isEmpty()) {
                System.out.println("[CIM] FOUND");
                return version.trim();
            }

            System.out.println("[CIM] NOT FOUND");
            return null;

        } catch (Exception e) {
            System.err.println("[CIM] ERROR: " + e.getMessage());
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