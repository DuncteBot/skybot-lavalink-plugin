plugins {
    java
    application
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.github.breadmoirai.github-release") version "2.2.12"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set("org.springframework.boot.loader.JarLauncher")
}

val pluginVersion = Version(1, 4, 2)

group = "com.dunctebot"
version = "$pluginVersion"
val archivesBaseName = "skybot-lavalink-plugin"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://m2.duncte123.dev/releases")
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.dunctebot:sourcemanagers:1.8.3")

    compileOnly("dev.arbjerg.lavalink:plugin-api:3.6.1")

    // for testing
    // runtimeOnly("com.github.freyacodes.lavalink:Lavalink-Server:3ead3be0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            name = "duncte123-m2"
            url = uri("https://m2.duncte123.dev/releases")
            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("duncte123-m2") {
            groupId = "com.dunctebot"
            artifactId = archivesBaseName
            version = "$pluginVersion"

            from(components["java"])
        }
    }
}

// make sure that we can resolve the dependencies
val impl = project.configurations.implementation.get()
impl.isCanBeResolved = true

tasks {
    processResources {
        from("src/main/resources") {
            include("**/dunctebot.properties")
            filter {
                it.replace(
                    "@version@",
                    "$pluginVersion"
                )
            }
        }

        into("build/resources/main")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    jar {
        archiveBaseName.set(archivesBaseName)
    }
    shadowJar {
        archiveBaseName.set(archivesBaseName)
        archiveClassifier.set("")

        configurations = listOf(impl)
    }
    build {
        dependsOn(processResources)
        dependsOn(compileJava)
        dependsOn(shadowJar)
    }

    publish {
        dependsOn(publishToMavenLocal)
    }
    wrapper {
        gradleVersion = "7.3.3"
        distributionType = Wrapper.DistributionType.ALL
    }
}

data class Version(val major: Int, val minor: Int, val patch: Int) {
    override fun toString() = "$major.$minor.$patch"
}

val preRelease = System.getenv("PRERELEASE") == "true"
val verName = "$pluginVersion${if(preRelease) "_${System.getenv("GITHUB_RUN_NUMBER")}" else ""}"

githubRelease {
    token(System.getenv("GITHUB_TOKEN"))
    owner("DuncteBot")
    repo("skybot-lavalink-plugin")
    targetCommitish(System.getenv("RELEASE_TARGET"))
    releaseAssets(tasks.shadowJar.get().outputs.files.toList())
    tagName(verName)
    releaseName(verName)
    overwrite(false)
    prerelease(preRelease)
    body(changelog())
}
