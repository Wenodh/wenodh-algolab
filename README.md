# Wenodh Zen

**Disconnect. Focus. Be present.**

Wenodh Zen is a native Android, local-first focus application built with Kotlin and Jetpack Compose. The original Wenodh AlgoLab web project and its history remain untouched on `main`; active Android work is isolated on `feature/wenodh-zen-foundation`.

## Current features

- Focus sessions with a deadline-backed timer and ongoing notification.
- Session recovery after process recreation and best-effort reboot recovery.
- Work, Study, Reading, Wind down and custom focus profiles.
- User-selected blocked-app list with Accessibility-based foreground app detection.
- Math, memory and reflection challenges for intentional early exit.
- Locally recorded session history and blocked-app attempts.
- Optional Do Not Disturb integration with prior filter restoration.
- Daily focus reminders scheduled with WorkManager; reminders never auto-start sessions.
- Locally generated white, pink and brown noise, tied to the focus service and Android audio focus.
- Daily and rolling seven-day summaries plus rule-based achievements.
- No account, backend, analytics SDK or cloud sync.

## Build

Requirements: JDK 17, Android SDK 35, Gradle 8.9.

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

APK output:

```
app/build/outputs/apk/debug/app-debug.apk
```

GitHub Actions runs unit tests before assembling the debug APK and uploads the APK as the `wenodh-zen-debug` artifact.

## Privacy and platform constraints

App selections, session history and focus statistics are stored locally. Ambient sounds are generated on-device; no recordings are downloaded. The app does not need an account or network connection for its focus features.

App restriction uses a user-enabled Accessibility Service to observe foreground package names only. It does not read screen text, keystrokes or notifications. This is best-effort enforcement, not a secure device lock; Android settings, emergency access and the ability to disable the service remain available. Google Play policy eligibility and required Accessibility API disclosures must be reviewed before distribution.

The timer uses Android's `specialUse` foreground-service type. Its eligibility and Play Console declaration must be reviewed before release. See [Android permissions and platform constraints](docs/ANDROID_PERMISSIONS.md).

## Project docs

- [Architecture](docs/ARCHITECTURE.md)
- [Android permissions and platform constraints](docs/ANDROID_PERMISSIONS.md)
- [Implementation status and release plan](docs/RELEASE_PLAN.md)\n- [Privacy policy draft](docs/PRIVACY_POLICY.md)

## Release status

Functional milestones are implemented on the feature branch. Final CI, physical-device/OEM testing, privacy/policy review and signed release preparation remain release gates. Debug APKs are for testing only; signing keys must never be committed.

## License

MIT
