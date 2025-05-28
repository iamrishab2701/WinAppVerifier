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

---

## 📌 Maintainers

**Created and maintained by:** Rishab Kumar Singh | Rahul Kumar
\[Thoughtworks | QA Engineering]
