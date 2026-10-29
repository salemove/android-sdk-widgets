plugins {
    id("com.android.application")
}

fun compat(name: String): Provider<String> = providers.gradleProperty("compat.$name")

val kotlinEnabled = compat("kotlin").isPresent

if (kotlinEnabled) {
    apply(plugin = "org.jetbrains.kotlin.android")
}

android {
    namespace = "com.glia.integratorcompat"
    compileSdk = compat("compileSdk").get().toInt()

    defaultConfig {
        applicationId = "com.glia.integratorcompat"
        minSdk = compat("minSdk").get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        val javaTarget = JavaVersion.toVersion(compat("javaTarget").get())
        sourceCompatibility = javaTarget
        targetCompatibility = javaTarget
    }

    // Kotlin sources make the Kotlin compiler load the SDK's metadata, as an integrator's Kotlin code does
    if (kotlinEnabled) {
        sourceSets["main"].java.srcDir("src/kotlin")
    }
}

dependencies {
    implementation("com.glia:android-widgets:${compat("widgetsVersion").get()}")
}
