package com.winappverifier.util;

import com.winappverifier.model.ReportEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ReportGenerator {

    public static void writeHtmlReport(List<ReportEntry> entries, String filename) {
        long passed = entries.stream().filter(e -> "PASSED".equalsIgnoreCase(e.getOverallStatus())).count();
        long failed = entries.size() - passed;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {

            writer.write("<html><head><style>");
            writer.write("body { font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9; }");
            writer.write("h2 { color: #333; }");
            writer.write("table { border-collapse: collapse; width: 100%; margin-top: 40px; }");
            writer.write("th, td { border: 1px solid #ccc; padding: 10px; text-align: center; }");
            writer.write("th { background-color: #f2f2f2; }");
            writer.write(".pass { background-color: #d4edda; }");
            writer.write(".fail { background-color: #f8d7da; }");
            writer.write(".warn { background-color: #fff3cd; }");
            writer.write("</style>");
            writer.write("<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>");
            writer.write("</head><body>");

            writer.write("<h2>WinAppVerifier Report</h2>");
            writer.write("<p>Generated: " + LocalDateTime.now() + "</p>");

            // Centered pie chart block
            writer.write("<div style='max-width: 400px; margin: 0 auto;'>");
            writer.write("<canvas id=\"resultChart\" height=\"200\"></canvas>");
            writer.write("</div>");
            writer.write("<script>");
            writer.write("const ctx = document.getElementById('resultChart').getContext('2d');");
            writer.write("new Chart(ctx, { type: 'pie', data: {");
            writer.write("labels: ['Passed', 'Failed'],");
            writer.write("datasets: [{ label: 'Test Summary', data: [" + passed + ", " + failed + "],");
            writer.write("backgroundColor: ['#28a745', '#dc3545'] }] },");
            writer.write("options: { responsive: true, plugins: { legend: { position: 'bottom' } } } });");
            writer.write("</script>");

            // App result table
            writer.write("<table>");
            writer.write("<tr><th>SW ID</th><th>Name</th><th>Expected Version</th><th>Actual Version</th><th>Installed</th><th>Version Match</th><th>Launch Status</th><th>Close Status</th><th>Overall Status</th></tr>");

            for (ReportEntry entry : entries) {
                writer.write("<tr>");
                writer.write("<td>" + entry.getSwId() + "</td>");
                writer.write("<td>" + entry.getName() + "</td>");
                writer.write("<td>" + entry.getExpectedVersion() + "</td>");
                writer.write("<td>" + nullToNA(entry.getActualVersion()) + "</td>");
                writer.write(styleTd(entry.isInstalled(), "YES", "NO"));
                writer.write(styleTd(entry.getVersionMatch(), "YES", "NO"));
                writer.write(styleTd(entry.getLaunchStatus()));
                writer.write(styleTd(entry.getCloseStatus()));
                writer.write(styleTd(entry.getOverallStatus()));
                writer.write("</tr>");
            }

            writer.write("</table>");
            writer.write("</body></html>");

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to generate report: " + e.getMessage());
        }
    }

    private static String styleTd(Boolean value, String trueText, String falseText) {
        if (value == null) {
            return "<td class=\"warn\">N/A</td>";
        }
        return "<td class=\"" + (value ? "pass" : "fail") + "\">" + (value ? trueText : falseText) + "</td>";
    }

    private static String styleTd(String status) {
        if (status == null) return "<td>N/A</td>";
        String cls = "warn";
        if ("Running".equalsIgnoreCase(status) || "Closed".equalsIgnoreCase(status) || "PASSED".equalsIgnoreCase(status)) cls = "pass";
        else if ("Failed".equalsIgnoreCase(status) || "Not Installed".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status) || "Skipped".equalsIgnoreCase(status)) cls = "fail";
        return "<td class=\"" + cls + "\">" + status + "</td>";
    }

    private static String nullToNA(String str) {
        return (str == null || str.isEmpty()) ? "N/A" : str;
    }
}