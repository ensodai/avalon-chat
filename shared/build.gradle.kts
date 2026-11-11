plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlinx.rpc)
}

kotlin {
    jvmToolchain(21)

    android {
        namespace = "com.ensodai.shared.avalon"
        compileSdk = 37
        minSdk = 24
    }
    
    jvm()
    
    iosArm64()
    iosSimulatorArm64()
    
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
    
    sourceSets {
        all {
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.rpc.krpc.client)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
