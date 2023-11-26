plugins {
    java
    `java-library`
    `maven-publish`
    alias(libs.plugins.lavalink)
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.breadmoirai.github-release") version "2.4.1"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

val pluginVersion = Version(1, 6, 0)

group = "com.dunctebot"
version = "$pluginVersion"
val archivesBaseName = "skybot-lavalink-plugin"
val preRelease = System.getenv("PRERELEASE") == "true"
val verName = "${if (preRelease) "PRE_" else ""}$pluginVersion${if(preRelease) "_${System.getenv("GITHUB_RUN_NUMBER")}" else ""}"


lavalinkPlugin {
    name = "DuncteBot-plugin"
    path = "$group.lavalinkplugin"
    version = verName
    apiVersion = libs.versions.lavalink.api
    serverVersion = gitHash(libs.versions.lavalink.server)
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://m2.duncte123.dev/releases")
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.sourcemanager)
//    implementation(project(":skybot-source-managers"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            name = "lavalink"
            url = uri("https://maven.lavalink.dev/releases")
            credentials {
                username = System.getenv("USERNAME")
                password = System.getenv("PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
//    publications {
//        create<MavenPublication>("lavalink") {
//            groupId = "com.dunctebot"
//            artifactId = archivesBaseName
//            version = "$pluginVersion"
//
//            from(components["java"])
//        }
//    }
}

// make sure that we can resolve the dependencies
val impl = project.configurations.implementation.get()
impl.isCanBeResolved = true

tasks {
//    processResources {
//        from("src/main/resources") {
//            include("**/dunctebot.properties")
//            filter {
//                it.replace(
//                    "@version@",
//                    "$pluginVersion"
//                )
//            }
//        }
//
//        into("build/resources/main")
//        duplicatesStrategy = DuplicatesStrategy.INCLUDE
//    }
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
        gradleVersion = "8.2"
        distributionType = Wrapper.DistributionType.BIN
    }
}
// WHY ARE YOU BROKEN
tasks.githubRelease {
    dependsOn(tasks.jar)
    dependsOn(tasks.shadowJar)
    mustRunAfter(tasks.shadowJar)
}

data class Version(val major: Int, val minor: Int, val patch: Int) {
    override fun toString() = "$major.$minor.$patch"
}

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
