import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties
import java.net.Inet4Address
import java.net.NetworkInterface

fun gradleString(name: String) = providers.gradleProperty(name).get()
fun gradleInt(name: String) = gradleString(name).toInt()

fun getLocalIp(): String {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        for (networkInterface in interfaces) {
            if (networkInterface.isLoopback || !networkInterface.isUp) continue
            val name = networkInterface.name.lowercase()
            if (name.contains("docker") || name.contains("tun") || name.contains("tap") ||
                name.contains("virbr") || name.contains("br-") || name.contains("vpn") ||
                name.contains("wireguard")) {
                continue
            }
            for (address in networkInterface.inetAddresses) {
                if (address is Inet4Address) {
                    val ip = address.hostAddress
                    if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                        return ip
                    }
                }
            }
        }
    } catch (e: Exception) {
        // ignore
    }
    return "10.0.2.2" // fallback
}

val appVersionName = gradleString("app.versionName")
val appVersionCode = gradleInt("app.versionCode")
val appDisplayName = gradleString("app.displayName")
val appCompileSdk = gradleInt("app.compileSdk")
val appMinSdk = gradleInt("app.minSdk")
val appTargetSdk = gradleInt("app.targetSdk")
fun resolveEnvOrProperty(propName: String, envName: String): String? {
    val fromEnv = providers.environmentVariable(envName).orNull?.takeIf { it.isNotBlank() }
    if (fromEnv != null) return fromEnv

    val dotEnvFile = rootProject.file(".env")
    if (dotEnvFile.exists()) {
        val props = Properties()
        dotEnvFile.bufferedReader().use { props.load(it) }
        val fromFile = (props.getProperty(envName) ?: props.getProperty(propName))?.trim()?.takeIf { it.isNotBlank() }
        if (fromFile != null) return fromFile
    }

    val fromProp = providers.gradleProperty(propName).orNull?.takeIf { it.isNotBlank() }
    if (fromProp != null) return fromProp

    return null
}

fun deriveWsUrl(httpUrl: String): String {
    val trimmed = httpUrl.trim().trimEnd('/')
    if (trimmed.isEmpty()) return ""
    val wsBase = when {
        trimmed.startsWith("https://", ignoreCase = true) -> "wss://" + trimmed.substring(8)
        trimmed.startsWith("http://", ignoreCase = true) -> "ws://" + trimmed.substring(7)
        trimmed.startsWith("wss://", ignoreCase = true) || trimmed.startsWith("ws://", ignoreCase = true) -> trimmed
        else -> "ws://$trimmed"
    }
    return if (wsBase.endsWith("/")) wsBase else "$wsBase/"
}

val defaultServerBaseUrl = resolveEnvOrProperty("app.server.baseUrl", "APP_SERVER_BASE_URL") ?: "http://10.0.2.2:8080"
val defaultServerSyncWsUrl = resolveEnvOrProperty("app.server.syncWsUrl", "APP_SERVER_SYNC_WS_URL") ?: deriveWsUrl(defaultServerBaseUrl)
val defaultServerLiveCallWsUrl = resolveEnvOrProperty("app.server.liveCallWsUrl", "APP_SERVER_LIVE_CALL_WS_URL") ?: deriveWsUrl(defaultServerBaseUrl)
val composeResourcesSourceDir = layout.projectDirectory.dir(
    "../composeApp/build/generated/assets/copyAndroidMainComposeResourcesToAndroidAssets"
)
val composeResourcesOutputRoot = layout.buildDirectory.dir("generated/composeAppAssets")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(keystorePropertiesFile.inputStream())
}

val releaseStoreFile = keystoreProperties["storeFile"]?.toString()?.let { rootProject.file(it) }
val hasReleaseKeystore = releaseStoreFile != null &&
    releaseStoreFile.exists() &&
    listOf("storePassword", "keyAlias", "keyPassword")
        .all { !keystoreProperties.getProperty(it).isNullOrBlank() }

abstract class SyncComposeResourcesTask : Sync() {
    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty
}

fun registerComposeResourcesSyncTask(
    variantName: String,
) = tasks.register(
    "sync${variantName.replaceFirstChar(Char::uppercaseChar)}ComposeResources",
    SyncComposeResourcesTask::class
) {
    dependsOn(project(":composeApp").tasks.named("copyAndroidMainComposeResourcesToAndroidAssets"))
    from(composeResourcesSourceDir)
    outputDir.convention(composeResourcesOutputRoot.map { it.dir(variantName) })
    into(outputDir)
}

val composeResourcesSyncTasks = mutableMapOf<String, TaskProvider<SyncComposeResourcesTask>>()

fun composeResourcesSyncTaskFor(buildType: String): TaskProvider<SyncComposeResourcesTask> =
    composeResourcesSyncTasks.getOrPut(buildType) {
        registerComposeResourcesSyncTask(buildType)
    }

configure<ApplicationExtension> {
    namespace = "com.ensodai.avalon"
    compileSdk = appCompileSdk

    defaultConfig {
        applicationId = "com.ensodai.avalon"
        minSdk = appMinSdk
        targetSdk = appTargetSdk
        versionCode = appVersionCode
        versionName = appVersionName
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appAuthRedirectScheme"] =
            "com.googleusercontent.apps.placeholder"
    }

    signingConfigs {
        create("release") {
            storeFile = keystoreProperties["storeFile"]?.toString()?.let { rootProject.file(it) }
            storePassword = keystoreProperties["storePassword"]?.toString()
            keyAlias = keystoreProperties["keyAlias"]?.toString()
            keyPassword = keystoreProperties["keyPassword"]?.toString()
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName(if (hasReleaseKeystore) "release" else "debug")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            versionNameSuffix = "-release"
            manifestPlaceholders["appName"] = appDisplayName
            buildConfigField("String", "BASE_URL", "\"$defaultServerBaseUrl\"")
            buildConfigField("String", "SYNC_WS_URL", "\"$defaultServerSyncWsUrl\"")
            buildConfigField("String", "LIVE_CALL_WS_URL", "\"$defaultServerLiveCallWsUrl\"")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "../composeApp/proguard-rules.pro"
            )
        }
        debug {
            signingConfig = signingConfigs.getByName(if (hasReleaseKeystore) "release" else "debug")
            versionNameSuffix = "-debug"
            manifestPlaceholders["appName"] = appDisplayName
            buildConfigField("String", "BASE_URL", "\"$defaultServerBaseUrl\"")
            buildConfigField("String", "SYNC_WS_URL", "\"$defaultServerSyncWsUrl\"")
            buildConfigField("String", "LIVE_CALL_WS_URL", "\"$defaultServerLiveCallWsUrl\"")
        }
        create("debugLocal") {
            initWith(buildTypes.getByName("debug"))
            matchingFallbacks += listOf("debug")
            versionNameSuffix = "-debugLocal"
            manifestPlaceholders["appName"] = appDisplayName
            // Используем динамический физический IP-адрес хоста для обхода VPN и коллизий маршрутизации эмулятора
            val localIp = getLocalIp()
            buildConfigField("String", "BASE_URL", "\"http://$localIp:8080\"")
            buildConfigField("String", "SYNC_WS_URL", "\"ws://$localIp:8080\"")
            buildConfigField("String", "LIVE_CALL_WS_URL", "\"ws://$localIp:8080\"")
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName(if (hasReleaseKeystore) "release" else "debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "BASE_URL", "\"$defaultServerBaseUrl\"")
            buildConfigField("String", "SYNC_WS_URL", "\"$defaultServerSyncWsUrl\"")
            buildConfigField("String", "LIVE_CALL_WS_URL", "\"$defaultServerLiveCallWsUrl\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "../composeApp/proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

androidComponents {
    onVariants { variant ->
        val buildType = variant.buildType ?: return@onVariants
        val syncTask = composeResourcesSyncTaskFor(buildType)
        variant.sources.assets?.addGeneratedSourceDirectory(
            syncTask,
            SyncComposeResourcesTask::outputDir
        )
    }
}

dependencies {
    baselineProfile(project(":benchmark"))

    implementation(project(":composeApp"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.koinAndroid)
    implementation(libs.coil3.coil.compose)
    implementation(libs.compose.components.resources)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
}

baselineProfile {
    saveInSrc = true
    automaticGenerationDuringBuild = false
    dexLayoutOptimization = true
}

tasks.register<Exec>("runDebugLocalSetup") {
    group = "help"
    description = "Sets up adb reverse for all active emulators to work with local backend on port 8080."
    commandLine("bash", "-c", "adb devices | grep emulator | cut -f1 | xargs -I {} adb -s {} reverse tcp:8080 tcp:8080")
}
