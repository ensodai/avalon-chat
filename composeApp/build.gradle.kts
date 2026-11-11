import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.LONG
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

// Shared Gradle property readers
fun gradleString(name: String) = providers.gradleProperty(name).get()
fun gradleInt(name: String) = gradleString(name).toInt()
fun gradleLong(name: String) = gradleString(name).toLong()

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

// Build parameters
val appVersionName = gradleString("app.versionName")
val appVersionCode = gradleLong("app.versionCode")
val appCompileSdk = gradleInt("app.compileSdk")
val appMinSdk = gradleInt("app.minSdk")
val defaultServerBaseUrl = resolveEnvOrProperty("app.server.baseUrl", "APP_SERVER_BASE_URL") ?: "http://10.0.2.2:8080"
val defaultServerSyncWsUrl = resolveEnvOrProperty("app.server.syncWsUrl", "APP_SERVER_SYNC_WS_URL") ?: deriveWsUrl(defaultServerBaseUrl)
val defaultServerLiveCallWsUrl = resolveEnvOrProperty("app.server.liveCallWsUrl", "APP_SERVER_LIVE_CALL_WS_URL") ?: deriveWsUrl(defaultServerBaseUrl)

val experimentalUuidOptIn = "-opt-in=kotlin.uuid.ExperimentalUuidApi"
val appBuildConfigPackage = "com.ensodai.avalon.config"
val roomKspConfigurations = listOf(
    "kspAndroid",
    "kspIosArm64",
    "kspIosSimulatorArm64",
    "kspWasmJs"
)
val verificationCompileTasks = listOf(
    "compileCommonMainKotlinMetadata",
    "compileAndroidMain",
    "compileKotlinIosSimulatorArm64",
    "compileKotlinWasmJs"
)
val forbiddenCommonMainPatterns = listOf(
    Regex("""^\s*import\s+android\..*$""", RegexOption.MULTILINE) to
        "Android import is not allowed in commonMain",
    Regex("""\bandroid\.[A-Z_a-z][A-Za-z0-9_]*\.""") to
        "Android API reference is not allowed in commonMain",
    Regex("""^\s*import\s+androidx\.activity\..*$""", RegexOption.MULTILINE) to
        "androidx.activity import is not allowed in commonMain",
    Regex("""^\s*import\s+androidx\.appcompat\..*$""", RegexOption.MULTILINE) to
        "androidx.appcompat import is not allowed in commonMain",
    Regex("""^\s*import\s+androidx\.fragment\..*$""", RegexOption.MULTILINE) to
        "androidx.fragment import is not allowed in commonMain",
    Regex("""^\s*import\s+androidx\.work\..*$""", RegexOption.MULTILINE) to
        "androidx.work import is not allowed in commonMain"
)

// Build flavor detection for shared runtime config
val requestedTasks = gradle.startParameter.taskNames.map { it.lowercase() }
val isDebugBuild = requestedTasks.none {
    it.contains("release") || it.contains("benchmark") || it.contains("distribution")
}

fun KotlinMultiplatformExtension.configureSharedCompilerOptions() {
    compilerOptions {
        freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        freeCompilerArgs.add("-Xexpect-actual-classes")
        freeCompilerArgs.add(experimentalUuidOptIn)
    }
}

fun KotlinMultiplatformExtension.configureAndroidTarget() {
    android {
        namespace = "com.ensodai.avalon.shared"
        compileSdk = appCompileSdk
        minSdk = appMinSdk
        withJava()
        withHostTest {}
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add(experimentalUuidOptIn)
        }
    }
}

fun KotlinMultiplatformExtension.configureWasmTarget() {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xwasm-kclass-fqn",  // FQN имена классов в wasm
                "-Xir-dump"           // дамп IR
            )
            freeCompilerArgs.add(experimentalUuidOptIn)
        }
    }
}

fun KotlinMultiplatformExtension.configureIosTargets() {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        target.compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add(experimentalUuidOptIn)
                }
            }
        }
    }
}

fun KotlinDependencyHandler.configureCommonMainDependencies() {
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.windowsizeclass)
    implementation(libs.compose.ui)
    implementation(libs.compose.components.resources)
    implementation(libs.compose.ui.tooling.preview)

    implementation(libs.jetbrains.navigation3.ui)
    implementation(libs.jetbrains.lifecycle.viewmodel.compose)
    implementation(libs.jetbrains.lifecycle.runtime.compose)

    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    implementation(libs.koinCompose)
    implementation(libs.koinComposeViewModel)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.resources)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.sqlite)
    implementation(libs.androidx.sqlite.async)
    implementation(libs.kermit)
    implementation(libs.icons.lucide)
    implementation(libs.coil3.coil.compose)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.zoomable)
    implementation(libs.multiplatform.markdown.renderer)
    implementation(libs.multiplatform.markdown.renderer.m3)
    implementation(libs.filekit.core)
    implementation(libs.filekit.dialogs.compose)
    implementation(projects.shared)
    implementation(libs.kotlinx.rpc.krpc.client)
    implementation(libs.kotlinx.rpc.krpc.ktor.client)
    implementation(libs.kotlinx.rpc.krpc.serialization.json)
}

fun KotlinDependencyHandler.configureAndroidMainDependencies() {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.koinAndroid)
    implementation(libs.coil3.network.okhttp)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.sqlite.bundled)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    implementation(libs.appauth)
    implementation(libs.accompanist.permissions)
    implementation(project.dependencies.platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
}

fun KotlinMultiplatformExtension.configureSourceSets() {
    sourceSets {
        all {
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }
        commonMain {
            dependencies {
                configureCommonMainDependencies()
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }

        androidMain.dependencies {
            configureAndroidMainDependencies()
        }

        val iosMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqlite.bundled)
            }
        }

        val iosArm64Main by getting {
            dependsOn(iosMain)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        val webMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.androidx.sqlite.web)
                implementation(libs.kotlinx.browser)
                implementation(libs.sqldelight.androidx.driver.opfs.wasm.js)
                implementation(libs.coil3.network.ktor3)
                implementation(npm("copy-webpack-plugin", "^11.0.0"))
                implementation(npm("workbox-window", "^7.1.0"))
            }
        }

        val wasmJsMain by getting {
            dependsOn(webMain)
        }
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    id("androidx.baselineprofile.consumer")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.androidx.room3)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.kotlinx.rpc)
}

// Generated shared config for commonMain/androidMain/iosMain/wasmJsMain
buildkonfig {
    packageName = appBuildConfigPackage
    exposeObjectWithName = "AppBuildConfig"

    defaultConfigs {
        buildConfigField(STRING, "VERSION_NAME", appVersionName)
        buildConfigField(LONG, "VERSION_CODE", appVersionCode.toString())
        buildConfigField(BOOLEAN, "IS_DEBUG", isDebugBuild.toString())
        buildConfigField(STRING, "BASE_URL", defaultServerBaseUrl)
        buildConfigField(STRING, "SYNC_WS_URL", defaultServerSyncWsUrl)
        buildConfigField(STRING, "LIVE_CALL_WS_URL", defaultServerLiveCallWsUrl)
        buildConfigField(STRING, "TELEGRAM_CHANNEL", providers.gradleProperty("app.telegram.channelName").getOrElse(""))
        buildConfigField(STRING, "TELEGRAM_BOT", providers.gradleProperty("app.telegram.botUsername").getOrElse(""))
        buildConfigField(STRING, "TERMS_URL", providers.gradleProperty("app.legal.termsUrl").getOrElse(""))
        buildConfigField(STRING, "PRIVACY_URL", providers.gradleProperty("app.legal.privacyUrl").getOrElse(""))
    }
}

// Resource and code generation tools
compose.resources {
    packageOfResClass = "com.ensodai.avalon.generated.resources"
    generateResClass = always
    publicResClass = true
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

composeCompiler {
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose_stability_config.txt"))
}

// Kotlin Multiplatform targets and source sets
kotlin {
    configureAndroidTarget()
    configureWasmTarget()
    configureIosTargets()
    configureSharedCompilerOptions()
    configureSourceSets()
}

// Code generation dependencies
dependencies {
    roomKspConfigurations.forEach { configuration ->
        add(configuration, libs.androidx.room.compiler)
    }
}

koinCompiler {
    userLogs = true
    strictSafety = true
}

// Task wiring and verification
tasks.matching { it.name == "prepareAndroidMainArtProfile" }.configureEach {
    dependsOn(tasks.named("generateBuildKonfig"))
}

tasks.matching { it.name == "extractAndroidMainAnnotations" }.configureEach {
    dependsOn(tasks.named("kspAndroidMain"))
}

tasks.named<Kotlin2JsCompile>("compileKotlinWasmJs").configure {
    // Work around KT-82395: KSP/Room generates top-level declarations and crashes Wasm IC.
    incremental = false
    javaClass.methods
        .firstOrNull { it.name == "setIncrementalJsKlib\$kotlin_gradle_plugin_common" }
        ?.invoke(this, false)
}

tasks.matching { it.name == "copyAndroidMainComposeResourcesToAndroidAssets" }.configureEach {
    val outputDirectory = javaClass.methods
        .firstOrNull { method -> method.name == "getOutputDirectory" }
        ?.invoke(this) as? DirectoryProperty

    outputDirectory?.convention(
        layout.buildDirectory.dir("generated/assets/$name")
    )
}

baselineProfile {
    variants {
        create("androidMain") {
            from(project(":benchmark"))
        }
    }
}

val forbidAndroidImportsInCommonMain by tasks.registering {
    group = "verification"
    description = "Fails when Android-only imports or references are used from commonMain."

    val commonMainDir = layout.projectDirectory.dir("src/commonMain/kotlin")
    inputs.dir(commonMainDir)

    doLast {
        val violations = fileTree(commonMainDir) {
            include("**/*.kt")
        }.files.flatMap { file ->
            val text = file.readText()
            val lines = text.lines()

            forbiddenCommonMainPatterns.flatMap { (regex, message) ->
                regex.findAll(text).map { match ->
                    val lineNumber = text.substring(0, match.range.first).count { it == '\n' } + 1
                    val lineText = lines.getOrNull(lineNumber - 1)?.trim().orEmpty()
                    Triple(file, lineNumber, "$message: $lineText")
                }.toList()
            }
        }.sortedBy { (file, lineNumber, _) ->
            "${file.absolutePath}:$lineNumber"
        }

        if (violations.isNotEmpty()) {
            violations.forEach { (file, lineNumber, message) ->
                logger.error("e: file://${file.absolutePath}:$lineNumber:1 $message")
            }

            throw GradleException(
                "Forbidden Android-specific APIs found in commonMain. " +
                    "See file:line errors above. Move Android code to androidMain or behind expect/actual."
            )
        }
    }
}

val verifyMultiplatform by tasks.registering {
    group = "verification"
    description = "Runs core multiplatform compilation checks for common, Android, iOS simulator, and Wasm."

    dependsOn(
        forbidAndroidImportsInCommonMain,
        *verificationCompileTasks.toTypedArray()
    )
}

tasks.named("check").configure {
    dependsOn(forbidAndroidImportsInCommonMain, verifyMultiplatform)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.uuid.ExperimentalUuidApi")
    }
}

// PWA Workbox injection task
val injectWorkboxManifest by tasks.registering(Exec::class) {
    group = "build"
    description = "Injects Workbox manifest into the Service Worker template for Wasm."

    val wasmBrowserDistribution = tasks.named("wasmJsBrowserDistribution")
    dependsOn(wasmBrowserDistribution)

    workingDir = projectDir
    commandLine("npx", "--yes", "workbox-cli", "injectManifest", "workbox-config.js")
    isIgnoreExitValue = true
}

// Ensure the manifest is injected after distribution is generated
tasks.named("wasmJsBrowserDistribution").configure {
    finalizedBy(injectWorkboxManifest)
}

