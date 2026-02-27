plugins {
    java
    alias(libs.plugins.lavalink) apply false
    id("com.gradleup.shadow") version "9.0.0" apply false
    id("com.github.breadmoirai.github-release") version "2.4.1" apply false
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}



allprojects {
    group = "com.dunctebot"

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://m2.duncte123.dev/releases")
        maven("https://m2.dv8tion.net/releases")
        maven("https://maven.lavalink.dev/releases")
        maven("https://maven.lavalink.dev/snapshots")
        maven("https://jitpack.io")
    }

    tasks.withType<Wrapper> {
        gradleVersion = "8.11"
        distributionType = Wrapper.DistributionType.BIN
    }
}

subprojects {
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

