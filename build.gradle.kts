plugins {
    kotlin("jvm") version "2.1.0"
    application
}

group = "ado"
version = "0.1.0"

repositories {
    mavenCentral()
}

val kotlinLoggingVersion = "6.0.9"
val slf4jVersion = "2.0.13"
val jacksonVersion = "2.17.2"
val cliktVersion = "4.4.0"
val koinVersion = "4.0.0"
val junitVersion = "5.10.2"
val kotestVersion = "5.9.1"
val mockkVersion = "1.13.11"

dependencies {
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")

    implementation("io.insert-koin:koin-core:$koinVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLoggingVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    runtimeOnly("org.slf4j:slf4j-simple:$slf4jVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("cli.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
