# Wenodh Zen architecture

## Current foundation

- **Platform:** Native Android, Kotlin, Jetpack Compose, Material 3.
- **Package:** `com.wenodh.zen`.
- **Minimum Android:** API 26.
- **Compile/target SDK:** 35.
- **JVM:** 17.
- **State:** Local-first; no account or backend dependency.

## Session timing model

The persisted end timestamp is the source of truth. The UI renders the difference between the current wall clock and that deadline. It does not assume that a coroutine, Activity, or process will remain alive for the duration of a session.

`FocusSessionStore` owns the current session record. `FocusSessionService` owns the ongoing foreground notification and checks the persisted deadline. The UI observes the same store and can reconstruct the current countdown after recreation.

## Planned boundaries

- `core/`: shared platform, persistence, permissions and design primitives.
- `domain/`: session, profile, restriction and schedule models/use cases.
- `data/`: Room/DataStore implementations and repositories.
- `feature/`: Compose screens grouped by user capability.
- `service/`: focus session lifecycle, restriction monitoring and scheduling.

Keep app restriction logic separate from the timer. Losing Accessibility access must never corrupt or silently reset a running focus session.

## Engineering rules

1. Persist state transitions before reporting success to the UI.
2. Derive countdown display from timestamps; never persist a decrementing counter.
3. Keep user-started session state recoverable after process death and reboot.
4. Do not silently elevate permissions or enable DND.
5. Keep emergency/system surfaces usable.
6. A UI control is not a completed feature until its behavior is exercised on-device or in an automated test.
7. Add migrations and tests whenever persisted schemas change.
