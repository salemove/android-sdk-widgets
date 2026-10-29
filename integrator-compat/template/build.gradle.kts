// The toolchain comes from gradle.properties, which run.sh writes per profile
buildscript {
    val agp = providers.gradleProperty("compat.agp").get()
    val kotlin = providers.gradleProperty("compat.kotlin").orNull

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:$agp")
        // Without it the build gets the Kotlin Gradle plugin AGP itself depends on, like a Java-only app
        if (kotlin != null) {
            classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin")
        }
    }
}
