package com.winappverifier.util;

public class PowerShellCommands {

    public static String getNonMSStoreAppsRegistryEntry(String appName) {
        return String.format(
                "powershell.exe -Command \"@(" +
                        "Get-ItemProperty 'HKLM:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*'," +
                        "'HKLM:\\Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\*'" +
                        ") | Where-Object { $_.DisplayName -like '*%s*' } | " +
                        "Select-Object -ExpandProperty DisplayVersion -First 1\"", appName
        );
    }

    public static String getMSStoreAppEntry(String appName) {
        return String.format(
                "powershell.exe -Command \"Get-AppxPackage | Where-Object { $_.Name -like '*%s*' } | " +
                        "Select-Object -ExpandProperty Version -First 1\"", appName
        );
    }

    public static String getCIMResult(String appName) {
        return String.format(
                "powershell.exe -Command \"Get-CimInstance -ClassName Win32_Product | " +
                        "Where-Object { $_.Name -like '*%s*' } | " +
                        "Select-Object -ExpandProperty Version -First 1\"", appName
        );
    }
}