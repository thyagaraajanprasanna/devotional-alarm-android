# Quiet Alarm — Android starter project

Working title only; the app name can be changed later.

## Current prototype scope
- Native Kotlin Android starter project.
- No `INTERNET` permission, ad SDK, analytics SDK, account, or cloud dependency.
- User chooses an audio file through Android's system picker; no broad storage permission.
- Schedules a daily exact alarm and displays notification actions.
- Keeps alarm dismissal separate from the music service's stop action.
- Uses private app preferences and non-exported receivers/service.

## Important: prototype, not production-ready
This is a starter scaffold and has **not been compiled or device-tested** in this environment. Before relying on it for waking up, it needs Android Studio build fixes, testing on the target Android version/device, and improvements:
- Proper media session / MediaStyle controls and robust action semantics.
- A playlist queue, not just one selected audio file.
- Alarm/snooze state machine and careful handling of dismiss vs. stop music.
- Permission/notification-denied handling and Android 13+ foreground-service behavior.
- URI persistence edge cases and reboot/exact-alarm permission edge cases.
- Testing under Doze, battery optimization, reboot, force-stop, and vendor battery management.
- Optional encryption only if sensitive data warrants it; ordinary alarm settings are stored in private app preferences.

## Build
1. Install Android Studio with Android SDK 35.
2. Open this folder as a project.
3. Let Gradle sync, then build/run on a physical Android device.
4. Test with a short local audio file first.

## Permissions rationale
- `SCHEDULE_EXACT_ALARM`: Android special access on recent Android versions for precise scheduled alarms.
- `POST_NOTIFICATIONS`: required on Android 13+ to show alarm/playback notifications.
- `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: needed for continued background playback.
- `RECEIVE_BOOT_COMPLETED`: allows rescheduling alarms after reboot.
No Internet, broad storage, contacts, location, camera, or microphone permissions are declared.

Android lock-screen behavior is controlled partly by device settings. The app does not request full-screen intent permission and does not launch a full-screen alarm UI.
