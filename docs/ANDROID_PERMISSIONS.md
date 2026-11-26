# Android permissions and platform constraints

This document is a release gate, not a promise that every restriction can be enforced on every device.

## Current foundation and app restrictions

| Capability | Permission / mechanism | Current state |
|---|---|---|
| Focus notification | `POST_NOTIFICATIONS` on Android 13+ | Runtime request included |
| Ongoing focus service | `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` | Declared; Play eligibility needs review |
| Launcher app discovery | `<queries>` with MAIN + LAUNCHER intent | Implemented; no broad inventory permission |
| App-switch detection | User-enabled Accessibility Service | Implemented; foreground package only |
| DND | Notification Policy Access | Not implemented |
| Exact schedule alarms | `SCHEDULE_EXACT_ALARM` where justified/available | Not implemented |

## App discovery and privacy

Wenodh Zen queries activities that match the Android launcher intent using a targeted `<queries>` declaration. It does not request `QUERY_ALL_PACKAGES`. Only launcher-visible apps are shown in the selection list; some apps without launcher activities may not appear. Do not add broad package visibility unless a policy review establishes that it is necessary and eligible.

The app names and package identifiers selected for blocking are stored locally. No installed-app inventory or usage history is uploaded.

## Accessibility service

The service observes `TYPE_WINDOW_STATE_CHANGED` and `TYPE_WINDOWS_CHANGED` events and uses only the event package name to determine whether a selected app is foregrounded. It does not retrieve window content, screen text, keystrokes or notifications. Enforcement is attempted only while a persisted focus session is active. When a selected app is detected, the service invokes Android's Home global action and posts a notification; the restriction explanation opens only after the user taps that notification. This avoids attempting to launch an Activity from the background, which Android restricts.

Before opening Android Accessibility Settings, the app displays a separate in-app disclosure and requires an affirmative tap. The user can disable the service at any time in Android Settings. Wenodh Zen does not prevent disabling or uninstalling itself.

This is best-effort consumer app restriction, not a secure kiosk or device-owner lock. OEM background behavior, system UI, split-screen, app transitions and Android version differences can affect enforcement. Emergency calls and system settings must remain accessible. If the service is disabled, the app must show that blocking is inactive.

Google Play requires an Accessibility API declaration and prominent disclosure/consent for non-accessibility-tool uses. The service must be reviewed against current Play policy before distribution. Do not set `isAccessibilityTool=true`; Wenodh Zen is not an accessibility tool.

## Foreground service

The timer implementation declares Android's `specialUse` foreground-service type with a subtype describing the user-started focus timer. This is not an automatic guarantee of Google Play approval. Before distribution, verify current Android requirements, demonstrate that a foreground service is necessary for the user-visible session, complete required Play Console declarations, and test Android 14+ behavior.

Do not mislabel a focus timer as `mediaPlayback` or `dataSync`.

## DND and schedules

DND requires the user to grant Notification Policy Access. Schedules should use exact alarms only where the user-facing function genuinely requires precise timing and the applicable permission is granted. Use inexact alarms or WorkManager for deferrable work. Handle reboot, timezone changes, denied permissions and missed schedules explicitly.
