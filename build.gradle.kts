plugins {
    java
    application
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.1"
    id("com.github.breadmoirai.github-release") version "2.2.12"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set("org.springframework.boot.loader.JarLauncher")
}

val pluginVersion = Version(1, 1, 1)

group = "com.dunctebot"
version = "$pluginVersion"
val archivesBaseName = "skybot-lavalink-plugin"

repositories {
    mavenCentral()
    maven("https://duncte123.jfrog.io/artifactory/maven")
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
    jcenter() //koe :(
}

dependencies {
    implementation("com.dunctebot:sourcemanagers:1.5.9")

    compileOnly("dev.arbjerg.lavalink:plugin-api:0.8.1")

    // for testing
    runtimeOnly("com.github.freyacodes.lavalink:Lavalink-Server:feature~plugins-SNAPSHOT")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
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
        gradleVersion = "7.1"
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
    repo("lavalink")
    targetCommitish(System.getenv("RELEASE_TARGET"))
    releaseAssets(tasks.shadowJar.get().outputs.files.toList())
    tagName(verName)
    releaseName(verName)
    overwrite(false)
    prerelease(preRelease)
    body(changelog())
}