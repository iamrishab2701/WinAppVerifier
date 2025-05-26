package com.winappverifier.util;

import com.winappverifier.model.AppConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AppLauncher {

    public static boolean launch(AppConfig app) {
        try {
            String command;

            if (app.getPath().trim().toLowerCase().startsWith("start ")) {
                // MS Store or protocol-based
                command = "powershell.exe -Command \"" + app.getPath() + "\"";
            } else {
                // Traditional app
                command = "cmd.exe /c \"" + app.getPath() + "\"";
            }

            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            builder.redirectErrorStream(true);
            builder.start();

            // Wait for app to launch (poll every 2s for up to 30s)
            int retries = 15;
            int intervalMs = 2000;

            for (int i = 0; i < retries; i++) {
                if (isProcessRunning(app.getProcess())) {
                    return true;
                }
                Thread.sleep(intervalMs);
            }

            return false;

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to launch " + app.getName() + ": " + e.getMessage());
            return false;
        }
    }



    public static boolean isProcessRunning(String processName) {
        try {
            Process process = new ProcessBuilder("tasklist").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(processName.toLowerCase())) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to check process: " + e.getMessage());
        }
        return false;
    }

    public static void close(AppConfig app) {
        try {
            new ProcessBuilder("taskkill", "/f", "/im", app.getProcess()).start();
            System.out.println(" - Close Status: [CLOSED]");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to close " + app.getName() + ": " + e.getMessage());
        }
    }
}