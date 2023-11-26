/*
 * Copyright 2021 Duncan "duncte123" Sterken
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

plugins {
    `java-library`
//    `maven-publish`
}

project.group = "com.dunctebot"
project.version = "1.9.0"
val archivesBaseName = "sourcemanagers"

dependencies {
    compileOnly(libs.lavaplayer)

    implementation(libs.logger)
    implementation(libs.commonsIo)
    implementation(libs.jsoup)
    implementation(libs.findbugs)

    testImplementation(libs.lavaplayer)
    testImplementation(libs.logger.impl)
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val jar: Jar by tasks
val build: Task by tasks
val clean: Task by tasks
//val publish: Task by tasks

val sourcesJar = task<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets["main"].allJava)
}

build.apply {
//    dependsOn(clean)
    dependsOn(jar)
    dependsOn(sourcesJar)

    jar.mustRunAfter(clean)
    sourcesJar.mustRunAfter(jar)
}

//publishing {
//    repositories {
//        maven {
//            name = "duncte123-m2"
//            url = uri("https://m2.duncte123.dev/releases")
//            credentials {
//                username = System.getenv("DUNCTE_USERNAME")
//                password = System.getenv("DUNCTE_PASSWORD")
//            }
//            authentication {
//                create<BasicAuthentication>("basic")
//            }
//        }
//    }
//    publications {
//        register<MavenPublication>("duncte123-m2") {
//            pom {
//                name.set(archivesBaseName)
//                description.set("Source managers for skybot")
//                url.set("https://github.com/DuncteBot/skybot-source-managers")
//                licenses {
//                    license {
//                        name.set("Apache-2.0")
//                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
//                    }
//                }
//                developers {
//                    developer {
//                        id.set("duncte123")
//                        name.set("Duncan Sterken")
//                        email.set("contact@duncte123.me")
//                    }
//                }
//                scm {
//                    connection.set("scm:git:git://github.com/DuncteBot/skybot-source-managers.git")
//                    developerConnection.set("scm:git:ssh://git@github.com:DuncteBot/skybot-source-managers.git")
//                    url.set("https://github.com/DuncteBot/skybot-source-managers")
//                }
//            }
//
//            from(components["java"])
//
//            artifactId = archivesBaseName
//            groupId = project.group as String
//            version = project.version as String
//
//            artifact(sourcesJar)
//        }
//    }
//}

//publish.apply {
//    dependsOn(build)
//
//    onlyIf {
//        System.getenv("DUNCTE_USERNAME") != null && System.getenv("DUNCTE_PASSWORD") != null
//    }
//}
