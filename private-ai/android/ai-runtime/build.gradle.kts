plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val enableNativeLlm = providers.gradleProperty("enableNativeLlm").orNull == "true"

android {
    namespace = "ai.privateai.airuntime"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (enableNativeLlm) {
            externalNativeBuild {
                cmake {
                    cppFlags += listOf("-std=c++17", "-O3")
                    arguments += listOf(
                        "-DANDROID_STL=c++_shared",
                        "-DLLAMA_BUILD_COMMON=ON",
                        "-DLLAMA_BUILD_TESTS=OFF",
                        "-DLLAMA_BUILD_EXAMPLES=OFF",
                        "-DLLAMA_BUILD_SERVER=OFF",
                    )
                }
            }
            ndk {
                abiFilters += listOf("arm64-v8a", "x86_64")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    if (enableNativeLlm) {
        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt")
                version = "3.22.1"
            }
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    testFixtures {
        enable = true
    }
}

dependencies {
    implementation(project(":shared-domain"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("javax.inject:javax.inject:1")

    testFixturesImplementation(project(":shared-domain"))
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    testImplementation(project(":model-manager"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.mockk:mockk:1.13.13")
}
