# Wenodh Zen 0.5.0

## Added

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
