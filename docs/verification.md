# Verification log

This file records commands actually executed and their results. Device-only checks stay pending until evidence exists.

## Verified on 2026-10-29

Environment: Linux x86_64, JDK 17.0.20, Gradle 9.6.0, AGP 9.4.0, Android SDK platform 37.0, build tools 36.0.0. The host required local launchers for the already-installed JDK compiler/linker modules; this did not change project source or declared build versions.

### Repository audit

- Initial `main` commit: `cbe2f6e`.
- Existing content: only `README.md`; no source or build files were overwritten.

### Gradle configuration

```bash
gradle help --no-daemon --no-configuration-cache
```

Result: **BUILD SUCCESSFUL**. AGP and all four modules configured successfully.

### Unit tests and debug APK

```bash
gradle testDebugUnitTest :demo-app:assembleDebug --no-daemon --no-configuration-cache
```

Final result: **BUILD SUCCESSFUL**.

- `FixedStepLoopTest`: 6 tests, 0 skipped, 0 failures, 0 errors.
- `GlCapabilitiesTest`: 3 tests, 0 skipped, 0 failures, 0 errors.
- Total: 9 tests passed.
- APK: `demo-app/build/outputs/apk/debug/demo-app-debug.apk` (approximately 884 KiB).
- `aapt dump badging` confirmed package `dev.gameengine.demo`, minSdk 24, targetSdk/compileSdk 37, launchable `MainActivity`, and required GLES version `0x30001`.

An earlier compile attempt failed on malformed Kotlin newline/regex escapes in renderer and Activity strings. Those sources were corrected and the complete task set was rerun successfully. Host JDK toolchain setup failures were environment issues, not counted as test failures or successes.

### Android lint

```bash
gradle lintDebug --no-daemon --no-configuration-cache
```

Final result: **BUILD SUCCESSFUL**. XML reports for `engine-core`, `engine-renderer`, `engine-android`, and `demo-app` each contain zero `<issue>` entries.

## Not run — requires Android runtime

- GLES 3.1 shader compilation and triangle rendering on a physical device or emulator.
- APK installation and launch.
- Orientation/resize, Activity recreation, pause/resume, surface recreation, and context-loss behavior.
- GPU frame pacing, memory behavior, and resource-leak checks.

No physical-device or emulator claims are made. The next verification step is to install the APK on an authorized GLES 3.1 device, capture the displayed GL version and a screenshot, then exercise background/foreground and surface recreation while inspecting logs.
