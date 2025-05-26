package com.winappverifier.util;

import com.winappverifier.model.ReportEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ReportGenerator {

    public static void writeHtmlReport(List<ReportEntry> entries, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {

            writer.write("<html><head><style>");
            writer.write("body { font-family: Arial; }");
            writer.write("table { border-collapse: collapse; width: 100%; }");
            writer.write("th, td { border: 1px solid #ddd; padding: 8px; text-align: center; }");
            writer.write("th { background-color: #f2f2f2; }");
            writer.write(".pass { background-color: #d4edda; }");
            writer.write(".fail { background-color: #f8d7da; }");
            writer.write(".warn { background-color: #fff3cd; }");
            writer.write("</style></head><body>");
            writer.write("<h2>WinAppVerifier Report</h2>");
            writer.write("<p>Generated: " + LocalDateTime.now() + "</p>");
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
                writer.write(styleTd(entry.getOverallStatus())); // NEW

                writer.write("</tr>");
            }

            writer.write("</table></body></html>");

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