# Android permissions and platform constraints

This document is a release gate, not a promise that every restriction can be enforced on every device.

## Current foundation

| Capability | Permission / mechanism | Current state |
|---|---|---|
| Focus notification | `POST_NOTIFICATIONS` on Android 13+ | Runtime request included |
| Ongoing focus service | `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` | Declared; Play eligibility needs review |
| App usage history | `PACKAGE_USAGE_STATS` special access | Not implemented |
| App-switch detection | User-enabled Accessibility Service | Not implemented |
| Do Not Disturb | Notification Policy Access | Not implemented |
| Exact schedule alarms | `SCHEDULE_EXACT_ALARM` where justified/available | Not implemented |

## Foreground service

The first timer implementation declares the Android 14+ `specialUse` foreground-service type with a subtype describing the user-started focus timer. This is not an automatic guarantee of Google Play approval. Before distribution, verify the current Android requirements, demonstrate that a foreground service is necessary for the user-visible session, complete required Play Console declarations, and test Android 14+ behavior.

Do not mislabel a focus timer as `mediaPlayback` or `dataSync`.

## App restrictions

Android does not provide ordinary consumer apps a universal public API to freeze arbitrary third-party applications. The proposed implementation uses a user-enabled Accessibility Service to observe foreground app transitions and display a restriction screen for selected packages. This is best-effort enforcement, not a secure kiosk or device-owner lock.

The service must:
- Explain exactly what it observes and why before the user enables it.
- Request enablement through Android Settings; never attempt to toggle it itself.
- Avoid collecting or transmitting accessibility content.
- Keep emergency calls, system settings and critical system UI accessible.
- Detect when the service is disabled and clearly report that enforcement is inactive.
- Avoid claiming that the user cannot disable or uninstall Wenodh Zen.
- Be reviewed against the current Google Play Accessibility API policy and disclosure requirements before publication.

For stronger managed-device enforcement, investigate Android Enterprise/device-owner provisioning as a separate deployment mode. Do not imply that this is available to a normal consumer install.

## DND and schedules

DND requires the user to grant Notification Policy Access. Schedules should use exact alarms only where the user-facing function genuinely requires precise timing and the applicable permission is granted. Use inexact alarms or WorkManager for deferrable work. Handle reboot, timezone changes, denied permissions and missed schedules explicitly.
