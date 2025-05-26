# 🧪 WinAppVerifier

**WinAppVerifier** is a Windows-based utility built in Java to verify the presence, version, launchability, and termination of installed applications — including Microsoft Store apps, Winget packages, and vendor-supplied installers.

---

## ✅ Features

* ✔️ Reads **`apps_config.json`** to validate a list of apps via `sw_id`
* ✔️ Supports **MSI, EXE, Intune, MS Store, and Winget** installations
* ✔️ Checks:

    * Installation presence
    * Installed version vs. expected version (from `sw_id`)
    * Successful app launch
    * Clean app termination
* ✔️ Supports hybrid launch:

    * `.exe` path for Win32 apps
    * `start protocol:` for MS Store apps
* ✔️ Generates a visually styled **HTML report**
* ✔️ Built for scale — 1000s of apps in future environments

---

## 🛠️ Requirements

* Java 8 or higher
* Maven (for building/running)
* Windows 10 or 11
* PowerShell (default shell on Windows)

---

## 📦 Input: apps\_config.json

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

## 🚀 Running the Utility

```bash
mvn compile exec:java
```

### Output

* CLI logs for each app
* An HTML report named `report.html` generated in the root directory

---

## 📊 Output: report.html

This file includes a styled table showing:

* `sw_id`, `name`
* Expected vs actual version
* Installation status
* Launch status
* Close status
* Color-coded result for easy stakeholder review

---

## 🔄 Launch Types

| Type            | Path Format          |
| --------------- | -------------------- |
| Traditional app | "C:\Path\To\app.exe" |
| MS Store app    | "start ms-teams:"    |

---

## 🛑 Close Logic

Uses:

```bash
taskkill /f /im <process>
```

> Ensure the `process` field in the config matches the real `.exe` from Task Manager or `Get-Process`.

---

## 🔍 Version Detection Order

1. Registry (`Get-ItemProperty`) — fast
2. MS Store (`Get-AppxPackage`)
3. Fallback (`Get-CimInstance`) — slower, used only if others fail

---

## 📈 Scaling Notes

* Designed for high volume (10,000+ apps)
* Easily parallelizable in the future
* Just expand `apps_config.json` to add more apps

---

## 📌 Maintainers

Created and maintained by: **Rishab Kumar Singh**
\[Thoughtworks | QA Engineering]

---

## 🧭 Roadmap

* CLI arguments for input/output paths
* PDF report export
* Parallel execution
* Summary stats in report
* Retry/resume for failed validations
