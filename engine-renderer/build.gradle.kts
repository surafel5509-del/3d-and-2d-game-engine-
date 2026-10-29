plugins {
    id("com.android.library")
}

android {
    namespace = "dev.gameengine.renderer"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":engine-core"))
    testImplementation("junit:junit:4.13.2")
}
