# ADR 0001: Android and GLES baseline

- Status: Accepted for M0/M1
- Date: 2026-10-29

## Decision

Use Kotlin, Android SDK 37, minimum SDK 24, AGP 9.4.0 with built-in Kotlin support, Gradle 9.6.0, JDK 17, `GLSurfaceView`, and GLSL ES 3.10. Do not add an NDK or C++ layer.

## Rationale

This is the current documented AGP/Gradle/JDK compatibility set and avoids the legacy Kotlin Android plugin. `GLSurfaceView` supplies a well-understood EGL/GL-thread lifecycle for the first renderer without introducing a custom EGL host prematurely. GLES 3.1 provides the required shader and compute-capable baseline, although M1 uses only its core rendering subset.

API 24 is the initial application baseline, not a statement that GLES 3.1 itself requires API 24. Device GPU support is still checked at runtime. No optional vendor extension is assumed.

## Consequences

- Devices below API 24 and contexts below GLES 3.1 are unsupported by this demo.
- `GLSurfaceView` requests a GLES 3.x context by major version; `GlCapabilities` must verify 3.1 or newer after creation.
- Emulator tests cannot establish physical-GPU compatibility.
- Native physics or other C++ dependencies require a later measured justification, ABI documentation, and packaging tests.
