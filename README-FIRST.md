# MSL Trading Journal V10 Premium — Android alarms + premium UI

## What changed
- Refined premium mobile interface: Samsung One UI-inspired large touch targets and bottom navigation, with iOS-inspired translucency, layered surfaces, rounded panels and restrained gradients.
- Fixed browser alarm messaging so it no longer claims closed-app reliability.
- Added an Android wrapper and native `AlarmManager` scheduling for Asia, London and New York reminders. Android notifications can appear while the journal is not open, and saved alarms are restored after device reboot.
- Existing journal features and local browser storage behavior are retained. AI still uses the configured Cloudflare Worker endpoint.

## Important architecture note
The website/PWA alone cannot guarantee alarms after the browser is suspended or closed. For reliable closed-app alerts, install the Android APK produced by the GitHub Actions workflow. The APK loads the hosted GitHub Pages site and adds native Android alarms.

## Publish website changes
Upload/replace the PWA files at the root of your existing GitHub Pages repository: `index.html`, `styles.css`, `app.js`, `sw.js`, `manifest.webmanifest`, and `assets/icon.svg`. Preserve other existing repository files unless intentionally updating them. The Android bridge is optional in a browser; in the APK, `AndroidAlarm` schedules native reminders.

The app wrapper currently loads:
`https://abiodunonifade37-cmd.github.io/MSL-TRADING-JOURNAL-V/`
If your live repository URL differs, update `JOURNAL_URL` in `android/app/src/main/java/com/msl/tradingjournal/MainActivity.java`.

## Build APK on GitHub
1. Upload the updated website files and the `.github/workflows/android-apk.yml` plus `android/` folder to the repository root.
2. Open GitHub → Actions → **Build MSL Trading Journal Android APK**.
3. Tap **Run workflow** if it has not started automatically.
4. When it finishes, open the workflow run and download artifact **MSL-Trading-Journal-V10-Premium-APK**.
5. Install the APK on Android. On first use, tap **Enable notifications** in journal Settings and allow **Notifications** and **Alarms & reminders**. Keep the app installed; Android may still apply manufacturer battery policies, so set battery usage to Unrestricted if notifications are delayed.

## Alarm behavior
- The APK schedules daily alarms through Android's native alarm service, so reminders do not depend on the browser tab or WebView staying open.
- Exact timing depends on granting Android's Alarms & reminders permission where required. If that permission is denied, Android may deliver reminders less precisely.
- Reboot recovery is included. Force-stop restrictions, notification disabling, battery policies, and uninstalling can still prevent delivery.
- The website-only PWA continues to use browser notifications and cannot guarantee alarms when completely closed.

## Test before relying on it
Set one enabled alarm for 2–3 minutes ahead, save, grant both permissions, leave the app, and verify a notification appears. Then test again after a reboot. Do not assume it is reliable until those tests pass on your specific phone.
