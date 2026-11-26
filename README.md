# Wenodh Zen

**Disconnect. Focus. Be present.**

Wenodh Zen is an Android-first, local-first focus application. This repository is being transitioned from the original Wenodh AlgoLab web project into a native Kotlin + Jetpack Compose Android app. The original project history remains available on `main`.

## Current implementation

- Native Android application module (`com.wenodh.zen`)
- Minimal Compose home screen
- 15, 25, 45 and 60 minute focus presets
- Durable focus deadline stored locally; remaining time is derived from the deadline rather than an in-memory countdown
- Ongoing foreground-service notification while a session is active
- Session state restored from local storage after activity/process recreation
- GitHub Actions debug APK build

## Build

Requirements: JDK 17, Android SDK 35, Gradle 8.9.

```bash
gradle :app:assembleDebug
```

APK output:

```
app/build/outputs/apk/debug/app-debug.apk
```

The GitHub Actions workflow uploads the debug APK as the `wenodh-zen-debug` artifact.

## Product direction

The first release is intentionally functional-first: focus session engine, app restriction enforcement, challenge-based exit, schedules, profiles, DND integration, sounds, analytics and achievements. UI polish is deliberately secondary.

## Important platform limitation

App restriction enforcement is not yet implemented. Android does not expose a general public API for ordinary consumer apps to guarantee suspension of arbitrary apps. The planned Accessibility-based enforcement must be explicitly enabled by the user, disclosed clearly, tested across OEMs and reviewed against current Google Play policy. It must not be described as unbreakable.

The ongoing session service currently uses Android's `specialUse` foreground-service type. Its eligibility and Play distribution requirements must be validated before a production release. See [Android permissions and platform constraints](docs/ANDROID_PERMISSIONS.md).

## Project docs

- [Architecture](docs/ARCHITECTURE.md)
- [Android permissions and platform constraints](docs/ANDROID_PERMISSIONS.md)
- [Implementation status and release plan](docs/RELEASE_PLAN.md)

## License

MIT
