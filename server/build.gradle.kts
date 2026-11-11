import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.koin.compiler)
    id("application")
    alias(libs.plugins.kotlinx.rpc)
}

group = "org.example"
version = "4.1.4"

dependencies {
    val ktorVersion = "3.5.0"
    val ktorAuthVersion = "3.5.0"
    val websockets = "3.5.0"
    val exposedVersion = "1.3.0"
    val hikariVersion = "7.0.2"
    val postgresqlVersion = "42.7.11"
    val googleApiClient = "2.8.1"
    val javaJwt = "4.5.0"
    val logbackClassic = "1.5.34"
    val telegram = "10.0.0"
    val flywaydb = "12.8.1"
    val koinVersion = "4.2.1"
    val geminiAPI = "1.57.0"
    val kotlinRetry = "2.0.2"
    val rateLimit = "3.3.2"
    val kotlinLogger = "7.0.3"
    val caffeine = "3.2.3"
    val geoip2 = "5.0.2"
    val lettuceVersion = "6.3.0.RELEASE"
    val kotlinxRpc = "0.10.3"
    val dotenvKotlin = "6.5.1"
    val kotlinxDatetime = "0.8.0"
    val kotlinxSerialization = "1.11.0"
    val jacksonDatabind = "2.15.2"
    val grpcCore = "1.28.0"
    val kloggingJvm = "0.9.4"
    val googleAuthLibraryOauth2Http = "1.41.0"
    val gson = "2.10.1"
    val googleHttpClientGson = "2.0.3"
    val geminiTTS = "2.93.0"
    val twelveMonkeysVersion = "3.13.1"

    implementation(projects.shared)

    implementation("com.google.cloud:google-cloud-texttospeech:$geminiTTS")
    implementation("com.twelvemonkeys.imageio:imageio-webp:$twelveMonkeysVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-server:$kotlinxRpc")
    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-ktor-server:$kotlinxRpc")
    implementation("org.jetbrains.kotlinx:kotlinx-rpc-krpc-serialization-json:$kotlinxRpc")

    implementation("io.lettuce:lettuce-core:$lettuceVersion")
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeine")

    implementation("io.github.cdimascio:dotenv-kotlin:$dotenvKotlin")
    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLogger")
    implementation("com.google.genai:google-genai:$geminiAPI")
    implementation("com.michael-bull.kotlin-retry:kotlin-retry:$kotlinRetry")

    implementation("org.flywaydb:flyway-core:$flywaydb")
    implementation("org.flywaydb:flyway-database-postgresql:${flywaydb}")
    implementation("io.ktor:ktor-server-call-logging:${ktorVersion}")

    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:$telegram")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$websockets")
    implementation("io.ktor:ktor-client-core:${ktorVersion}")
    implementation("io.ktor:ktor-client-cio:${ktorVersion}")
    implementation("io.ktor:ktor-client-content-negotiation:${ktorVersion}")
    implementation("io.ktor:ktor-server-cors:${ktorVersion}")
    implementation("io.ktor:ktor-server-default-headers:${ktorVersion}")

    implementation("io.insert-koin:koin-ktor:${koinVersion}")
    implementation("io.insert-koin:koin-logger-slf4j:${koinVersion}")
    implementation("io.insert-koin:koin-annotations:${koinVersion}")

    implementation("ch.qos.logback:logback-classic:$logbackClassic")
    implementation("org.slf4j:slf4j-api:2.0.17")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:${exposedVersion}")

    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")

    implementation("io.ktor:ktor-server-auth:$ktorAuthVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorAuthVersion")
    implementation("com.auth0:java-jwt:$javaJwt")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetime")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerialization")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabind")
    implementation("io.grpc:grpc-core:$grpcCore")
    implementation("io.klogging:klogging-jvm:$kloggingJvm")

    implementation("io.ktor:ktor-server-rate-limit-jvm:$rateLimit")
    implementation("io.ktor:ktor-server-compression:${ktorVersion}")
    implementation("io.ktor:ktor-server-resources:${ktorVersion}")
    implementation("com.google.auth:google-auth-library-oauth2-http:$googleAuthLibraryOauth2Http")
    implementation("com.google.code.gson:gson:$gson")
    constraints {
        implementation("com.google.code.gson:gson:$gson") {
            because("Google Auth library requires strict JSON parsing in 2.11.0, causing crashes")
        }
    }

    implementation("com.google.http-client:google-http-client-gson:$googleHttpClientGson")
    implementation("com.google.api-client:google-api-client:${googleApiClient}")
    implementation("com.google.firebase:firebase-admin:9.4.1")

    // GeoIP
    implementation("com.maxmind.geoip2:geoip2:$geoip2")
    implementation("io.ktor:ktor-server-forwarded-header:${ktorVersion}")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.ktor:ktor-server-test-host:${ktorVersion}")
    testImplementation("io.insert-koin:koin-test:${koinVersion}")
    testImplementation("io.insert-koin:koin-test-junit5:${koinVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

koinCompiler {
    userLogs = true
    debugLogs = false
    unsafeDslChecks = false
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

ktor {
    fatJar {
        archiveFileName.set("flow-backend.jar")
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveFileName.set("flow-backend.jar")
    mergeServiceFiles()
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

tasks.test {
    useJUnitPlatform()
}
