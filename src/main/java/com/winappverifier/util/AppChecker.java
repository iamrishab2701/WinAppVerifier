package com.winappverifier.util;

import com.winappverifier.model.AppConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AppChecker {

    public static boolean isAppInstalled(AppConfig app) {
        String expectedVersion = extractVersionFromSwId(app.getSw_id());
        String actualVersion = getVersionFromRegistry(app.getName());

        if (actualVersion == null) {
            actualVersion = getVersionFromAppx(app.getName());
        }

        if (actualVersion == null) {
            actualVersion = getVersionFromCim(app.getName());
        }

        if (actualVersion == null) {
            System.out.println(" - Installed: [NO]");
            return false;
        }

        System.out.println(" - Installed: [YES]");
        System.out.println(" - Expected Version: " + expectedVersion);
        System.out.println(" - Actual Version:   " + actualVersion);

        if (normalizeVersion(actualVersion).equals(normalizeVersion(expectedVersion))) {
            System.out.println(" - Version Match: [YES]");
            return true;
        } else {
            System.out.println(" - Version Match: [NO]");
            return true; // still installed, version mismatch
        }
    }

    private static String getVersionFromRegistry(String name) {
        try {
            String command = String.format(
                    "powershell.exe -Command \"@(" +
                            "Get-ItemProperty 'HKLM:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*'," +
                            "'HKLM:\\Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*'" +
                            ") | Where-Object { $_.DisplayName -like '*%s*' } | Select-Object -ExpandProperty DisplayVersion -First 1\"", name
            );

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();
            return (version != null && !version.trim().isEmpty()) ? version.trim() : null;

        } catch (Exception e) {
            System.err.println("[ERROR] Registry lookup failed for " + name + ": " + e.getMessage());
            return null;
        }
    }

    private static String getVersionFromAppx(String name) {
        try {
            String command = String.format(
                    "powershell.exe -Command \"Get-AppxPackage | Where-Object { $_.Name -like '*%s*' } | Select-Object -ExpandProperty Version -First 1\"", name
            );

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();
            return (version != null && !version.trim().isEmpty()) ? version.trim() : null;

        } catch (Exception e) {
            System.err.println("[ERROR] Appx lookup failed for " + name + ": " + e.getMessage());
            return null;
        }
    }

    private static String getVersionFromCim(String name) {
        try {
            String command = String.format(
                    "powershell.exe -Command \"Get-CimInstance -ClassName Win32_Product | Where-Object { $_.Name -like '*%s*' } | Select-Object -ExpandProperty Version -First 1\"", name
            );

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();
            return (version != null && !version.trim().isEmpty()) ? version.trim() : null;

        } catch (Exception e) {
            System.err.println("[ERROR] CIM lookup failed for " + name + ": " + e.getMessage());
            return null;
        }
    }

    public static String getInstalledVersion(AppConfig app) {
        String version = getVersionFromRegistry(app.getName());
        if (version == null) {
            version = getVersionFromAppx(app.getName());
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
