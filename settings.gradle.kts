pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven ( url = "https://jitpack.io" )
        maven ( url = "https://maven.google.com" )
    }
}

rootProject.name = "ChattingApp"
include(":app")
 