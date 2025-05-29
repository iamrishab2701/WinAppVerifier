package com.winappverifier;

import com.winappverifier.model.AppConfig;
import com.winappverifier.model.ReportEntry;
import com.winappverifier.util.AppChecker;
import com.winappverifier.util.AppLauncher;
import com.winappverifier.util.JsonReader;
import com.winappverifier.util.ReportGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    private static final Logger logger = LogManager.getLogger(AppLauncher.class);
    public static void main(String[] args) {
        int threadCount = parseThreadCount(args);
        List<AppConfig> apps = loadAppConfig("apps_config.json");

        if (apps.isEmpty()) {
            logger.error("[FATAL] No apps to process. Exiting.");
            return;
        }

        logger.info("App Verification Results:");
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<ReportEntry>> futures = new ArrayList<>();

        for (AppConfig app : apps) {
            futures.add(executor.submit(() -> {
                logger.info("\n" + app.getName() + ":");
                return processApp(app);
            }));
        }

        executor.shutdown();

        List<ReportEntry> reportEntries = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                reportEntries.add(futures.get(i).get());
            } catch (Exception e) {
                logger.error("[ERROR] Failed to get result for app: " + apps.get(i).getName() + " - " + e.getMessage());
                ReportEntry fallback = new ReportEntry(apps.get(i).getSw_id(), apps.get(i).getName());
                fallback.setInstalled(false);
                fallback.setLaunchStatus("Failed");
                fallback.setCloseStatus("Skipped");
                fallback.setOverallStatus("FAILED");
                reportEntries.add(fallback);
            }
        }

        generateReport(reportEntries);
    }

    private static int parseThreadCount(String[] args) {
        int defaultThreads = 4;
        for (int i = 0; i < args.length; i++) {
            if ("--threads".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    return Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    logger.warn("[WARN] Invalid thread count. Using default: " + defaultThreads);
                }
            }
        }
        return defaultThreads;
    }

    private static List<AppConfig> loadAppConfig(String path) {
        try {
            return JsonReader.readApps(path);
        } catch (Exception e) {
            logger.error("[FATAL] Failed to read or parse app config: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static ReportEntry processApp(AppConfig app) {
        String logPrefix = "[THREAD-" + Thread.currentThread().getId() + "] [" + app.getName() + "]";
        ReportEntry entry = new ReportEntry(app.getSw_id(), app.getName());

        try {
            entry.setExpectedVersion(AppChecker.extractVersionFromSwId(app.getSw_id()));

            boolean installed = false;
            try {
                installed = AppChecker.isAppInstalled(app);
                logger.info(logPrefix + " - Installation Status: " + (installed ? "Installed" : "Not Installed"));
            } catch (Exception e) {
                logger.error(logPrefix + " [ERROR] Failed to check installation: " + e.getMessage());
            }

            entry.setInstalled(installed);

            if (installed) {
                try {
                    entry.setActualVersion(AppChecker.getInstalledVersion(app));
                } catch (Exception e) {
                    logger.error(logPrefix + " [ERROR] Failed to retrieve actual version: " + e.getMessage());
                    entry.setActualVersion(null);
                }

                try {
                    boolean versionMatch = AppChecker.normalizeVersion(entry.getExpectedVersion())
                            .equals(AppChecker.normalizeVersion(entry.getActualVersion()));
                    entry.setVersionMatch(versionMatch);
                } catch (Exception e) {
                    logger.error(logPrefix + " [ERROR] Failed to compare version: " + e.getMessage());
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
                            logger.error(logPrefix + " [ERROR] Failed to close app: " + e.getMessage());
                            entry.setCloseStatus("Failed");
                        }
                    } else {
                        entry.setCloseStatus("Skipped");
                    }

                } catch (Exception e) {
                    logger.error(logPrefix + " [ERROR] Failed to launch app: " + e.getMessage());
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
            logger.error(logPrefix + " [ERROR] Failed to process app: " + e.getMessage());
            entry.setInstalled(false);
            entry.setActualVersion(null);
            entry.setVersionMatch(null);
            entry.setLaunchStatus("Failed");
            entry.setCloseStatus("Skipped");
        }

        try {
            entry.setOverallStatus(calculateOverallStatus(entry));
        } catch (Exception e) {
            logger.error(logPrefix + " [ERROR] Failed to calculate overall status: " + e.getMessage());
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
            logger.info("\n✅ HTML report generated: report.html");
        } catch (Exception e) {
            logger.error("[FATAL] Failed to generate HTML report: " + e.getMessage());
        }
    }
}