plugins {
    java
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

group = "com.dunctebot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://duncte123.jfrog.io/artifactory/maven")
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
    jcenter() //koe :(
}

dependencies {
    implementation("com.dunctebot:sourcemanagers:1.5.6")
    implementation("com.github.freyacodes:Lavalink:2bd6b22")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}