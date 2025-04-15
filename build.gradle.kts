plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.mongodb.driver.core)
    implementation(libs.mongodb.driver.sync)
    implementation(libs.bson)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    // JWT and Auth dependencies
    implementation("io.ktor:ktor-server-auth:2.3.9")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.9")
    // BCrypt for password hashing
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("at.favre.lib:bcrypt:0.10.2")
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
