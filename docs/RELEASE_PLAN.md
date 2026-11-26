# Implementation status and release plan

## Milestone 0 — Native foundation

- [x] Create native Android Gradle project alongside preserved AlgoLab history.
- [x] Establish package ID and API/JVM targets.
- [x] Add minimal Compose home screen and focus presets.
- [x] Persist session start/end timestamps locally.
- [x] Add ongoing foreground notification service.
- [x] Add debug APK GitHub Actions workflow.
- [ ] Confirm CI build and unit tests succeed; fix any build/runtime issues.

## Milestone 1 — Session reliability

- [x] Add explicit session states: active, completed, interrupted.
- [x] Persist session history locally with SQLiteOpenHelper (Room can be introduced when richer relational queries are needed).
- [x] Restore an active deadline after process death; attempt service recovery after reboot.
- [x] Add completion notification and pure lifecycle/deadline unit tests.
- [ ] Test notification permission denial and OEM battery restrictions.

## Milestone 2 — App restrictions

- [x] Discover launcher apps using a targeted package visibility query (no QUERY_ALL_PACKAGES).
- [x] Persist a user-managed blocked-app list locally.
- [x] Add a foreground-package Accessibility Service with an in-app disclosure and explicit consent step.
- [x] Return Home and post a restriction notification when a selected app is opened during an active focus session.
- [ ] Add permission-loss refresh/status handling across all resume paths and OEM compatibility tests.
- [ ] Add durable blocked-app interruption events to session history.
- [ ] Verify service behavior on an Android device/emulator.
- [ ] Google Play policy review before production distribution.

## Milestone 3 — Challenge-based exit

- [ ] Math, memory and reflection challenges.
- [ ] Successful challenge ends session as interrupted, not completed.
- [ ] Challenge abandonment and repeated-attempt behavior.
- [ ] Ensure emergency/system access is never blocked.

## Milestone 4 — Profiles and system integration

- [ ] Work, Study, Reading, Sleep and custom profiles.
- [ ] Optional DND integration.
- [ ] Smart schedules, reboot restoration and timezone handling.

## Milestone 5 — Insights and finishing

- [ ] Ambient sounds using Media3.
- [ ] Focus analytics and session history.
- [ ] Achievements and milestones.
- [ ] Accessibility, battery, memory and device compatibility testing.
- [ ] Signed release build and store policy review.

## Release criteria

A feature is not complete when its screen exists. It must have persisted behavior, permission/error handling, tests for important state transitions and verification on an Android device or emulator. Debug APKs are for testing only; release signing keys must remain outside the repository.
