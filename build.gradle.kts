plugins {
    java
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("com.github.breadmoirai.github-release") version "2.2.12"
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val pluginVersion = Version(0, 0, 1)

group = "com.dunctebot"
version = "$pluginVersion"
// val archivesBaseName = "dunctebot-lavalink"

repositories {
    mavenCentral()
    maven("https://duncte123.jfrog.io/artifactory/maven")
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
    jcenter() //koe :(
}

dependencies {
    implementation("com.dunctebot:sourcemanagers:1.5.7")

    compileOnly("com.github.freyacodes:Lavalink:2bd6b22")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.dunctebot"
            artifactId = "lavalink"
            version = "$pluginVersion"

            from(components["java"])
        }
    }
}

tasks {
    processResources {
        from("src/main/resources") {
            include("plugin.properties")
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
        archiveBaseName.set("dunctebot-plugin")
    }
    shadowJar {
        archiveBaseName.set("dunctebot-plugin")
        archiveClassifier.set("")
    }
    build {
        dependsOn(processResources)
        dependsOn(compileJava)
        dependsOn(shadowJar)
    }
    publish {
        dependsOn(publishToMavenLocal)
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
    tagName(verName)
    releaseName(verName)
    overwrite(false)
    prerelease(preRelease)
    body(changelog())
}