# Wenodh Zen Privacy Policy

**Effective date:** 26 November 2026

Wenodh Zen is designed to work locally on your Android device. This document describes the current app implementation and must be reviewed before public distribution.

## Information stored on your device

Wenodh Zen stores the following information in its private app storage:

- Focus session start/end times, planned duration and session outcome.
- Names and package identifiers of apps you choose to block.
- Counts and package identifiers for blocked-app launch attempts during a focus session.
- Your focus profiles, selected ambient sound, DND preference and daily reminder time.

This information is used only to provide focus timers, app restrictions, history, statistics and achievements.

## Accessibility service

If you choose to enable Wenodh Zen's Accessibility Service, Android sends foreground-window/app transition events to the service. The service reads the event's package name to check whether the foreground app is on your blocked list while a focus session is active.

The service does not retrieve screen/window content, read text, capture keystrokes, inspect notifications or transmit Accessibility events off the device. You can disable the service at any time in Android Accessibility settings. App restriction is best-effort and is not a device lock.

## Audio

Ambient white, pink and brown noise is generated locally by the app. No ambient recordings are downloaded or streamed. Android audio focus is requested while the sound is active.

## Permissions and system access

- Notifications are used for the ongoing focus timer, completion, blocked-app and scheduled reminder notifications.
- Foreground services keep a user-started timer running and, when selected, maintain ambient playback.
- Notification Policy Access is optional. If granted and enabled, the app temporarily applies Priority Do Not Disturb during a focus session and attempts to restore the prior interruption filter afterwards.
- WorkManager schedules inexact daily reminder notifications. A reminder never starts a focus session automatically.

You can revoke optional access in Android Settings.

## Collection, sharing and advertising

The current app does not require an account, does not include an analytics SDK or advertising SDK, and does not send focus history, app selections or Accessibility events to a server. The current app has no backend or cloud synchronization.

The Android manifest disables app backup for this local-only data model. App data remains in private app storage until you clear the app's data or uninstall it.

## Children's privacy

Wenodh Zen is a general-purpose productivity utility and does not knowingly collect personal information from children.

## Changes

This policy may be updated when the app's behavior changes. The policy version and effective date will be updated with releases.

## Contact and support

For questions or support, use the repository issue tracker:
https://github.com/Wenodh/wenodh-algolab/issues

## Distribution note

Before Google Play distribution, publish this policy at a stable public URL and ensure the Play Console Data safety form, Accessibility API declaration, foreground-service declarations and in-app disclosures match the shipped binary.
