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
        String jsonPath = "apps_config.json";  // unified config file

        List<AppConfig> apps = JsonReader.readApps(jsonPath);
        List<ReportEntry> reportEntries = new ArrayList<>();

        System.out.println("App Verification Results:");
        for (AppConfig app : apps) {
            System.out.println("\n" + app.getName() + ":");

            boolean installed = AppChecker.isAppInstalled(app);
            System.out.println(" - Installation Status: " + (installed ? "Installed" : "Not Installed"));

            ReportEntry entry = new ReportEntry(app.getSw_id(), app.getName());
            entry.setInstalled(installed);
            entry.setExpectedVersion(AppChecker.extractVersionFromSwId(app.getSw_id()));
            entry.setActualVersion(AppChecker.getInstalledVersion(app));

            if (installed) {
                boolean versionMatch = AppChecker.normalizeVersion(entry.getExpectedVersion())
                        .equals(AppChecker.normalizeVersion(entry.getActualVersion()));
                entry.setVersionMatch(versionMatch);

                boolean launched = AppLauncher.launch(app);
                entry.setLaunchStatus(launched ? "Running" : "Failed");

                if (launched) {
                    AppLauncher.close(app);
                    entry.setCloseStatus("Closed");
                } else {
                    entry.setCloseStatus("Skipped");
                }
            } else {
                entry.setVersionMatch(null);
                entry.setLaunchStatus("Skipped");
                entry.setCloseStatus("Skipped");
            }

            reportEntries.add(entry);
        }

        // ✅ Generate HTML report
        ReportGenerator.writeHtmlReport(reportEntries, "report.html");
        System.out.println("\n✅ HTML report generated: report.html");
    }
}
