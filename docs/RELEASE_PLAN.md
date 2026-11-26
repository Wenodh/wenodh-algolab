# Implementation status and release plan

## Milestone 0 — Native foundation

- [x] Create native Android Gradle project alongside preserved AlgoLab history.
- [x] Establish package ID and API/JVM targets.
- [x] Add minimal Compose home screen and focus presets.
- [x] Persist session start/end timestamps locally.
- [x] Add ongoing foreground notification service.
- [x] Add debug APK GitHub Actions workflow.
- [ ] Verify latest branch build and unit tests after all milestones.
- [ ] Confirm runtime behavior on supported Android versions.

## Milestone 1 — Session reliability

- [x] Add explicit session states: active, completed, interrupted and challenge exit.
- [x] Persist session history locally with SQLiteOpenHelper and versioned migration.
- [x] Restore an active deadline after process death; attempt service recovery after reboot.
- [x] Add completion notification and pure lifecycle/deadline unit tests.
- [ ] Test notification permission denial and OEM battery restrictions.

## Milestone 2 — App restrictions

- [x] Discover launcher apps using a targeted package visibility query (no QUERY_ALL_PACKAGES).
- [x] Persist a user-managed blocked-app list locally.
- [x] Add a foreground-package Accessibility Service with an in-app disclosure and explicit consent step.
- [x] Return Home and post a restriction notification when a selected app is opened during an active focus session.\n- [x] Protect the app itself, default dialer, emergency dialer, default launcher, Settings and core system UI from blocking.
- [x] Persist blocked-app attempts against the active session.
- [x] Refresh Accessibility permission status when the app returns to foreground.
- [ ] Verify service behavior and notification delivery on physical devices/OEMs.
- [ ] Complete Google Play Accessibility API and foreground-service policy review.

## Milestone 3 — Focus integrity and challenge-based exit

- [x] Add math challenges with profile-based difficulty.
- [x] Add short memory-sequence challenge with timed reveal.
- [x] Add reflection challenge requiring a meaningful response.
- [x] Allow challenge dismissal to return to the active session.
- [x] Record successful early exit as CHALLENGE_EXIT, never as a completed session.
- [x] Add unit tests for challenge validation.
- [ ] Device-test challenge flow, accessibility interruptions and lifecycle edge cases.

## Milestone 4 — Focus profiles and system integration

- [x] Add Work, Study, Reading and Wind down profiles.
- [x] Add a user-defined custom profile and duration.
- [x] Add optional DND integration with access checks and restoration of the prior interruption filter.
- [x] Add daily reminder scheduling with WorkManager.
- [x] Recalculate reminders after reboot, system clock and timezone changes.
- [x] Keep reminders as prompts only; never start a focus session automatically.
- [ ] Device-test DND restoration, denied permissions, timezone changes and OEM scheduling delays.

## Milestone 5 — Ambient sound, insights and achievements

- [x] Add locally generated white, pink and brown noise (no bundled copyrighted recordings).
- [x] Tie ambient playback to focus-session service lifecycle and Android audio focus.
- [x] Add today's and rolling seven-day focus summaries.
- [x] Add recent session history with status and blocked-app attempt counts.
- [x] Add durable blocked-app attempt counts and challenge-exit counts.
- [x] Add rule-based achievements with testable unlock thresholds.
- [ ] Device-test audio focus changes, headset/Bluetooth routing, volume and service recovery.

## Milestone 6 — Release hardening

- [ ] Review all changed Kotlin and Gradle files; remove dead code and resolve warnings.
- [ ] Run unit tests and assemble debug APK successfully on the final commit.
- [ ] Verify install, launch, session timer, process death and reboot recovery.
- [ ] Verify blocked-app enforcement and permission-loss UX on multiple Android versions/OEMs.
- [ ] Verify ambient playback, audio focus, DND restoration and schedule reminders.
- [ ] Review privacy disclosures, Accessibility API declaration, special-use FGS declaration and Play policy eligibility.
- [x] Add app icon and versioned release notes.
- [ ] Publish a hosted user-facing privacy policy.
- [ ] Configure release signing outside the repository and produce a signed release build.
- [ ] Create a release tag only after all release gates pass.

## Release criteria

A feature is not complete when its screen exists. It must have persisted behavior, permission/error handling, tests for important state transitions and verification on an Android device or emulator. Debug APKs are for testing only; release signing keys must remain outside the repository. Accessibility-based app restriction is best-effort, can be disabled by the user and is not a secure kiosk/device-owner lock.
