# Panel Barcode Tracker

Barcode generation, 100 x 50 mm sticker printing and scan-status tracking for the Aluform panel list.

* `web/Panel_Barcode_Tracker.html` – the standalone browser version (one offline file)
* everything else – the Android app that wraps the same page (`app/src/main/assets/index.html` is a copy of it)

Every push to `main` runs **Actions → Build APK**. The finished APK is published at **Releases → Latest APK** (download `PanelBarcodeTracker.apk` straight to the phone). Builds are signed with a fixed key, so a new APK installs over the old one and keeps your data.

## Android app (WebView wrapper)

The whole app is `app/src/main/assets/index.html` (same file as the browser version).
`MainActivity.java` shows it full-screen inside a WebView and adds:

* **Camera permission** – asked on first launch and whenever the page opens the live scanner.
* **Storage** – Excel / CSV / backup files are written to the phone's **Downloads** folder
  (Android 10+ needs no storage permission; Android 9 and older asks for it).
  Automatic backups go to `Downloads/PanelBarcodeBackups` (newest 3 kept).
* **Excel file picker**, JS alert/confirm dialogs, and **label printing** through Android's print service
  (paper size preset to 100 x 50 mm).
* All data stays on the phone (IndexedDB inside the app).

## Build the APK – option A: GitHub (no software to install)
1. Create a free GitHub repository and upload the *contents* of this folder (keep `.github/`).
2. Open the **Actions** tab → **Build APK** → wait about 3–5 minutes.
3. Open the finished run → **Artifacts** → download `PanelBarcodeTracker-debug-apk`, unzip, copy the `.apk` to the phone.
4. On the phone open the .apk and allow "Install unknown apps" for your file manager/browser when asked.

## Build the APK – option B: Android Studio
1. Install Android Studio, choose **Open** and select this folder, let Gradle sync.
2. **Build → Build APK(s)**. The file is `app/build/outputs/apk/debug/app-debug.apk`.
   (Command line: `./gradlew assembleDebug` with JDK 17 and the Android SDK installed.)

## Updating the app later
Replace `app/src/main/assets/index.html` with the newer HTML file and rebuild.
Data on the phone is kept when you install the new APK over the old one
(keep the same `applicationId`, and build with the same signing key – debug builds from the same
computer/GitHub runner can differ, so download a backup from the Report tab before changing build machine).
