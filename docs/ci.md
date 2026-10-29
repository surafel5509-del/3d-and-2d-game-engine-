# Continuous integration

Run these checks on JDK 17, Gradle 9.6.0, and Android SDK platform 37.0 with build tools 36.0.0:

```bash
gradle testDebugUnitTest --stacktrace
gradle :demo-app:assembleDebug --stacktrace
gradle lintDebug --stacktrace
```

A GitHub Actions job can use `actions/checkout@v4`, `actions/setup-java@v4` with Temurin 17, `android-actions/setup-android@v3`, and `gradle/actions/setup-gradle@v4` with Gradle 9.6.0. Install the SDK packages before invoking Gradle:

```bash
yes | sdkmanager --licenses >/dev/null
sdkmanager "platforms;android-37.0" "build-tools;36.0.0"
```

The repository integration used for the initial push did not have GitHub's `workflow` permission, so no active workflow file was created. A maintainer with that permission can add `.github/workflows/android.yml` using the steps above. Local host verification results are recorded in [verification.md](verification.md).
