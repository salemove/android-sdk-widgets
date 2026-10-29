plugins {
    id("com.android.application")
}

fun compat(name: String): Provider<String> = providers.gradleProperty("compat.$name")

val kotlinEnabled = compat("kotlin").isPresent
val composeEnabled = compat("compose").orNull.toBoolean()

if (kotlinEnabled) {
    apply(plugin = "org.jetbrains.kotlin.android")
}
if (composeEnabled) {
    apply(plugin = "org.jetbrains.kotlin.plugin.compose")
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

    buildFeatures {
        compose = composeEnabled
    }

    // Kotlin and Compose sources make the Kotlin compiler load the SDK's metadata, as an integrator's code does
    sourceSets["main"].apply {
        if (kotlinEnabled) java.srcDir("src/kotlin")
        if (composeEnabled) java.srcDir("src/compose")
    }
}

dependencies {
    implementation("com.glia:android-widgets:${compat("widgetsVersion").get()}")

    if (composeEnabled) {
        implementation(platform("androidx.compose:compose-bom:${compat("composeBom").get()}"))
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.material3:material3")
        implementation("androidx.activity:activity-compose:${compat("activityCompose").get()}")
    }
}
