package com.winappverifier;

import com.winappverifier.model.AppConfig;
import com.winappverifier.model.ReportEntry;
import com.winappverifier.util.AppChecker;
import com.winappverifier.util.AppLauncher;
import com.winappverifier.util.JsonReader;
import com.winappverifier.util.ReportGenerator;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<AppConfig> apps = loadAppConfig("apps_config.json");
        List<ReportEntry> reportEntries = new ArrayList<>();

        System.out.println("App Verification Results:");

        for (AppConfig app : apps) {
            System.out.println("\n" + app.getName() + ":");
            ReportEntry entry = processApp(app);
            reportEntries.add(entry);
        }

        generateReport(reportEntries);
    }

    private static List<AppConfig> loadAppConfig(String path) {
        try {
            return JsonReader.readApps(path);
        } catch (Exception e) {
            System.err.println("[FATAL] Failed to read or parse app config: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static ReportEntry processApp(AppConfig app) {
        ReportEntry entry = new ReportEntry(app.getSw_id(), app.getName());

        try {
            entry.setExpectedVersion(AppChecker.extractVersionFromSwId(app.getSw_id()));

            boolean installed = false;
            try {
                installed = AppChecker.isAppInstalled(app);
                System.out.println(" - Installation Status: " + (installed ? "Installed" : "Not Installed"));
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to check installation: " + e.getMessage());
            }

            entry.setInstalled(installed);

            if (installed) {
                try {
                    entry.setActualVersion(AppChecker.getInstalledVersion(app));
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed to retrieve actual version: " + e.getMessage());
                    entry.setActualVersion(null);
                }

                try {
                    boolean versionMatch = AppChecker.normalizeVersion(entry.getExpectedVersion())
                            .equals(AppChecker.normalizeVersion(entry.getActualVersion()));
                    entry.setVersionMatch(versionMatch);
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed to compare version: " + e.getMessage());
                    entry.setVersionMatch(null);
                }

                try {
                    boolean launched = AppLauncher.launch(app);
                    entry.setLaunchStatus(launched ? "Running" : "Failed");

                    if (launched) {
                        try {
                            AppLauncher.close(app);
                            entry.setCloseStatus("Closed");
                        } catch (Exception e) {
                            System.err.println("[ERROR] Failed to close app: " + e.getMessage());
                            entry.setCloseStatus("Failed");
                        }
                    } else {
                        entry.setCloseStatus("Skipped");
                    }

                } catch (Exception e) {
                    System.err.println("[ERROR] Failed to launch app: " + e.getMessage());
                    entry.setLaunchStatus("Failed");
                    entry.setCloseStatus("Skipped");
                }

            } else {
                entry.setActualVersion(null);
                entry.setVersionMatch(null);
                entry.setLaunchStatus("Skipped");
                entry.setCloseStatus("Skipped");
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to process app: " + app.getName() + " - " + e.getMessage());
            entry.setInstalled(false);
            entry.setActualVersion(null);
            entry.setVersionMatch(null);
            entry.setLaunchStatus("Failed");
            entry.setCloseStatus("Skipped");
        }

        try {
            entry.setOverallStatus(calculateOverallStatus(entry));
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to calculate overall status for " + app.getName() + ": " + e.getMessage());
            entry.setOverallStatus("FAILED");
        }

        return entry;
    }

    private static String calculateOverallStatus(ReportEntry entry) {
        if (entry.isInstalled()
                && "Running".equalsIgnoreCase(entry.getLaunchStatus())
                && "Closed".equalsIgnoreCase(entry.getCloseStatus())) {
            return "PASSED";
        }
        return "FAILED";
    }

    private static void generateReport(List<ReportEntry> reportEntries) {
        try {
            ReportGenerator.writeHtmlReport(reportEntries, "report.html");
            System.out.println("\n✅ HTML report generated: report.html");
        } catch (Exception e) {
            System.err.println("[FATAL] Failed to generate HTML report: " + e.getMessage());
        }
    }
}