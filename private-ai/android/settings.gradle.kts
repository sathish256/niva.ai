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
        mavenCentral()
    }
}

rootProject.name = "Niva"

include(":app")
include(":ai-runtime")
include(":model-manager")
include(":storage")
include(":security")
include(":shared-domain")

project(":shared-domain").projectDir = file("../shared/domain")
