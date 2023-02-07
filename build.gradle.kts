import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "com.github.breadmoirai"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
}

dependencies {
    // https://github.com/kotlin/kotlinx.coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")

    // https://kotlinlang.org/docs/reflection.html#jvm-dependency
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.0")

    // https://github.com/DV8FromTheWorld/JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.2")
    // https://github.com/MinnDevelopment/jda-ktx
    implementation("com.github.minndevelopment:jda-ktx:17eb77a")

    // https://docs.kweb.io/book/gettingstarted.html
    implementation("io.kweb:kweb-core:1.3.6")

    // https://github.com/lightbend/config
    implementation("com.typesafe:config:1.4.2")

    // https://github.com/broo2s/typedmap
    implementation("me.broot.typedmap:typedmap-core:1.0.0")

    // https://github.com/square/moshi
    implementation("com.squareup.moshi:moshi:1.14.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")

    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.4.5")

    // https://github.com/InsertKoinIO/koin
    implementation("io.insert-koin:koin-core:3.3.2")

    // https://arrow-kt.io/docs/core/#Gradle-kotlin
    implementation("io.arrow-kt:arrow-core:1.1.2")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application.mainClass.set("com.github.breadmoirai.discordtabletop.MainKt")
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    languageVersion = "1.8"
}