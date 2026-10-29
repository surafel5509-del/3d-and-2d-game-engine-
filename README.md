# Android 2D + 3D Game Engine

A modular Android game-engine foundation written in Kotlin with an OpenGL ES 3.1 renderer. The repository is being built in verified increments; it is **not yet a complete 2D/3D engine**.

## Current vertical slice

M0/M1 provides a four-module Gradle project and a demo app that:

- requests an OpenGL ES 3.x surface and verifies the actual context is GLES 3.1 or newer;
- shows a readable fallback instead of starting rendering on unsupported devices;
- compiles and links GLSL ES 3.10 shaders with diagnostic logs;
- creates VAO/VBO/EBO resources and draws an indexed, vertex-colored rotating triangle;
- updates rotation through a bounded deterministic 60 Hz fixed-step loop with interpolated rendering;
- handles resize, pause/resume, surface recreation, and GL-thread cleanup ownership;
- unit-tests fixed-step timing and GLES capability parsing.

No device rendering claim is made until an instrumentation or manual device run is recorded in [docs/verification.md](docs/verification.md).

## Module graph

```text
engine-core
    ↑
engine-renderer
    ↑
engine-android
    ↑
  demo-app
```

- `engine-core`: renderer-independent timing and simulation primitives.
- `engine-renderer`: GL capabilities, shader/program ownership, indexed mesh buffers, and the M1 scene.
- `engine-android`: `GLSurfaceView` lifecycle bridge, runtime capability reporting, and error fallback callbacks.
- `demo-app`: runnable Android activity and unsupported-device UI.

Later milestones add separate scene, ECS, asset, input, animation, physics, audio, UI, and debug modules only when each has a compiling vertical slice.

## Requirements

| Component | Version |
| --- | --- |
| JDK | 17 |
| Gradle | 9.6.0 |
| Android Gradle plugin | 9.4.0 (built-in Kotlin support) |
| Compile / target SDK | 37 |
| Minimum SDK | 24 |
| Device graphics | OpenGL ES 3.1 or newer |
| NDK | Not used in M0/M1 |

AGP 9.4's built-in Kotlin support is intentional: no legacy `org.jetbrains.kotlin.android` plugin is applied.

## Build and test

Install Android SDK platform 37 and the SDK build tools required by AGP. Set `ANDROID_HOME` or create `local.properties` with `sdk.dir=/path/to/android-sdk`. From the repository root:

```bash
gradle testDebugUnitTest
gradle :demo-app:assembleDebug
```

To install on a connected, authorized device:

```bash
gradle :demo-app:installDebug
adb shell am start -n dev.gameengine.demo/.MainActivity
```

For Android Studio, use a release compatible with AGP 9.4, open the repository root, allow Gradle to sync, and run the `demo-app` configuration on a GLES 3.1 device.

## Repository map

```text
.
├── build.gradle.kts
├── settings.gradle.kts
├── engine-core/
│   └── src/{main,test}/...
├── engine-renderer/
│   └── src/main/
│       ├── assets/shaders/triangle.{vert,frag}
│       └── java/dev/gameengine/renderer/...
├── engine-android/
│   └── src/main/java/dev/gameengine/android/...
├── demo-app/
│   └── src/main/...
├── docs/
│   ├── architecture.md
│   ├── ci.md
│   ├── verification.md
│   └── adr/0001-android-gles-baseline.md
```

## Feature status

| Area | Status |
| --- | --- |
| Multi-module Android build | Implemented for M0/M1 |
| GLES 3.1 detection and fallback | Implemented; device verification pending |
| Clear screen and indexed triangle | Implemented; device verification pending |
| Shader compilation diagnostics | Implemented; device verification pending |
| Fixed-step timing | Implemented and host-unit-tested |
| Math, cameras, transforms | M2 — not implemented |
| Sprites, textures, 2D batching, touch input | M3 — not implemented |
| ECS, scenes, serialization, async assets | M4 — not implemented |
| Lit 3D meshes and imported models | M5 — not implemented |
| Physics, animation, audio, particles | M6 — not implemented |
| Shadows, post-processing, profiling | M7 — not implemented |
| Platformer and 3D exploration samples | M8 — not implemented |

## Troubleshooting

- **“Unsupported graphics device”**: inspect the version and GPU shown by the fallback. A GLES 3.0 or 2.0 context is intentionally rejected.
- **Shader compile/link failure**: the fallback contains the driver log. Validate that the deployed assets still begin with `#version 310 es` and test on the affected GPU.
- **SDK not found**: configure `ANDROID_HOME` or `local.properties`; never commit `local.properties`.
- **Black screen after surface recreation**: capture `EngineSurfaceView` and `MainActivity` logs, then record the device/GPU in `docs/verification.md` before changing lifecycle ownership.
- **Emulator limitations**: host GPU translation and timing differ from physical devices. Emulator success is not evidence of physical-device shader or frame-pacing correctness.

## Next milestones

1. **M2**: allocation-conscious vector/matrix math, cameras, hierarchical transforms, generalized mesh and material APIs.
2. **M3**: texture loading off the GL thread, sprite batching, touch input, and a playable 2D scene.
3. **M4**: ECS, scene ownership, events, serialization, and asynchronous asset handles.
4. **M5–M8**: lit 3D rendering, replaceable physics, animation/audio/particles, profiling, and two complete samples.

See [architecture decisions](docs/architecture.md), [CI instructions](docs/ci.md), and [exact verification evidence](docs/verification.md).
