# Wenodh Zen 0.5.2

## Added

- Safer reboot recovery: the focus timer can resume without attempting to restart ambient media playback from the boot receiver.
- Protect the device's resolved Settings app from being selected for app blocking.
- Ambient audio uses sustained audio focus and recovers from AudioTrack writer errors without crashing the process.
- Add a deliberate two-step emergency exit that bypasses the challenge but records the session as interrupted.

- Work, Study, Reading, Wind down and custom focus profiles.
- Math, memory and reflection challenges for intentional early session exit.
- Local history for blocked-app launch attempts.
- Optional Do Not Disturb mode during focus, restoring the prior interruption filter.
- Daily focus reminder with reboot and timezone rescheduling.
- Locally generated white, pink and brown ambient noise.
- Daily and rolling seven-day focus summaries.
- Rule-based focus achievements.
- Wenodh Zen adaptive launcher icon.

## Privacy and platform behavior

- Focus data and app selections stay in local app storage.
- Android app backup is disabled for the local-only data model.
- Ambient sound is generated locally; no sound files are downloaded.
- App blocking remains best-effort and depends on user-enabled Accessibility access.
- Daily reminders are inexact and never start sessions automatically.

## Verification still required

- Final CI tests and debug APK build on this exact commit.
- Physical-device/OEM tests for Accessibility enforcement, DND, ambient audio, schedules and reboot recovery.
- Google Play policy review for Accessibility API and foreground-service declarations.
- Hosted privacy policy, release signing and production release review.
