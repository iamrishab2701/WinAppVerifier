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
        String jsonPath = "apps_config.json";
        List<AppConfig> apps;

        // STEP 1: Load JSON configuration
        try {
            apps = JsonReader.readApps(jsonPath);
        } catch (Exception e) {
            System.err.println("[FATAL] Failed to read or parse app config: " + e.getMessage());
            return;
        }

        List<ReportEntry> reportEntries = new ArrayList<>();
        System.out.println("App Verification Results:");

        for (AppConfig app : apps) {
            System.out.println("\n" + app.getName() + ":");
            ReportEntry entry = new ReportEntry(app.getSw_id(), app.getName());

            try {
                entry.setExpectedVersion(AppChecker.extractVersionFromSwId(app.getSw_id()));

                // STEP 2: App install check
                boolean installed = false;
                try {
                    installed = AppChecker.isAppInstalled(app);
                    System.out.println(" - Installation Status: " + (installed ? "Installed" : "Not Installed"));
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed to check installation: " + e.getMessage());
                }

                entry.setInstalled(installed);

                // STEP 3: Version detection
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

                    // STEP 4: Launch
                    try {
                        boolean launched = AppLauncher.launch(app);
                        entry.setLaunchStatus(launched ? "Running" : "Failed");

                        // STEP 5: Close
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

            // STEP 6: Calculate overall status
            try {
                String overall = "FAILED";
                if (entry.isInstalled()
                        && "Running".equalsIgnoreCase(entry.getLaunchStatus())
                        && "Closed".equalsIgnoreCase(entry.getCloseStatus())) {
                    overall = "PASSED";
                }
                entry.setOverallStatus(overall);
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to calculate overall status for " + app.getName() + ": " + e.getMessage());
                entry.setOverallStatus("FAILED");
            }

            reportEntries.add(entry);
        }

        // STEP 7: Generate HTML report
        try {
            ReportGenerator.writeHtmlReport(reportEntries, "report.html");
            System.out.println("\n✅ HTML report generated: report.html");
        } catch (Exception e) {
            System.err.println("[FATAL] Failed to generate HTML report: " + e.getMessage());
        }
    }
}