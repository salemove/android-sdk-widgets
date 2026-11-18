// Integrator test app that integrator-compat/run.sh copies and builds once per profile.
// It is a standalone build, not part of the SDK build.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Only the SDK snapshot published by run.sh comes from mavenLocal, plus a Core SDK
        // version not yet on Maven Central, so a Core release can be checked before publishing
        mavenLocal {
            content {
                includeModule("com.glia", "android-widgets")
                includeModule("com.glia", "android-sdk")
            }
        }
    }
}

rootProject.name = "IntegratorCompat"
include(":app")
