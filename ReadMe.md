# 🧪 WinAppVerifier

**WinAppVerifier** is a Windows-based utility built in Java to verify the presence, version, launchability, and termination of installed applications — including Microsoft Store apps, Winget packages, and vendor-supplied installers.

---

## ✅ Features

* ✔️ Reads `apps_config.json` to validate a list of apps via `sw_id`
* ✔️ Supports **MSI, EXE, Intune, MS Store, and Winget** installations
* ✔️ Performs:

  * Installation presence check
  * Version match (from `sw_id`)
  * App launch and graceful close
* ✔️ Hybrid launch support:

  * `.exe` path for Win32 apps
  * `start protocol:` for MS Store apps
* ✔️ Parallel execution using `--threads` CLI flag
* ✔️ Generates an interactive **HTML report with summary chart**
* ✔️ Designed for scale — ready for thousands of apps
* ✔️ Log4j2 based logging.
  

---

## 🛠️ Requirements

* Java 8 or higher
* Maven (for build and run)
* Windows 10 or 11
* PowerShell available on system path

---

## 📦 Input: `apps_config.json`

```json
{
  "apps": [
    {
      "sw_id": "7zip.7zip_Igor Pavlov_24.09",
      "name": "7-Zip",
      "path": "C:\\Program Files\\7-Zip\\7zFM.exe",
      "process": "7zFM.exe"
    },
    {
      "sw_id": "XP8BT8DW290MPQ_Microsoft Corporation_1.25.06502",
      "name": "MSTeams",
      "path": "start ms-teams:",
      "process": "ms-teams.exe"
    }
  ]
}
```

---


## 🔎 PowerShell Commands Used for App Detection
The utility uses three different PowerShell-based techniques to determine whether an application is installed and what version is currently available. These commands are executed sequentially in order of performance and reliability:

| Detection Method     | PowerShell Command                         | Use Case                                                                 |
|----------------------|---------------------------------------------|--------------------------------------------------------------------------|
| Registry (Fastest)   | `Get-ItemProperty` from Uninstall keys      | Detects Win32 apps installed via MSI or EXE setups that write to registry |
| MS Store Apps        | `Get-AppxPackage`                           | Specifically used for apps installed via Microsoft Store (UWP)           |
| CIM / WMI (Fallback) | `Get-CimInstance -ClassName Win32_Product`  | Slow but exhaustive; used only if above two fail. Detects even legacy installer traces |

### Examples:
* Registry Example:
```sh
Get-ItemProperty 'HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall\*' |
Where-Object { $_.DisplayName -like '*AppName*' } |
Select-Object -ExpandProperty DisplayVersion -First 1
```

*MS Store Example:
```sh
Get-AppxPackage |
Where-Object { $_.Name -like '*AppName*' } |
Select-Object -ExpandProperty Version -First 1
```

*CIM Example
```sh
Get-CimInstance -ClassName Win32_Product |
Where-Object { $_.Name -like '*AppName*' } |
Select-Object -ExpandProperty Version -First 1
```

🧠 Note: The registry method is fastest and safest. CIM/WMI should be used sparingly as it’s known to be slow and may even trigger application repairs on some systems.

---

## 🚀 How to Run

### 1. Using Maven

```sh
mvn compile exec:java "-Dexec.args=--threads 4"
```

Defaults to 4 threads if not specified.

### 2. Using Jar

```sh
mvn clean package
java -jar target/WinAppVerifier-1.0-SNAPSHOT.jar --threads 4
```

---

## 📊 Output: `report.html`

This file includes:

* 📋 App-wise results table
* 📊 Summary pie chart (Passed vs Failed apps)
* 🟢 Color-coded statuses
* 🔍 Overall result per app

All output is written to `report.html` in the project root.

---

## 🔄 Launch Types

| Type            | Path Format               |
| --------------- | ------------------------- |
| Traditional app | `"C:\\Path\\To\\app.exe"` |
| MS Store app    | `"start ms-teams:"`       |

---

## 🛑 Close Logic

Uses:

```sh
taskkill /f /im <process>
```

Ensure the `process` field in the config matches the real executable name shown in Task Manager.

---

## 🔍 Version Detection Priority

1. Registry via `Get-ItemProperty`
2. MS Store via `Get-AppxPackage`
3. WMI fallback via `Get-CimInstance` (last resort)

---

## ⚡ Parallel Execution

* Controlled via `--threads <N>` flag
* Guaranteed no skips or duplicates
* Order of apps in config is preserved in the report
* Fully thread-safe and isolated

---

## 📈 Scaling Notes

* Validated for high-volume testing (10,000+ apps)
* Parallel execution ensures fast turnaround
* Easily extend config JSON to add apps

## 📈 Future Scope
* Feasibility for other OS
* Docker based execution for a limited number of apps for cloud based automated execution.

---

## 📌 Maintainers

**Created and maintained by:** [Rishab Kumar Singh](https://www.linkedin.com/in/iamrishab2701/) | [Rahul Kumar](https://www.linkedin.com/in/whoisrahul/)
\[Thoughtworks | QA Engineering]
