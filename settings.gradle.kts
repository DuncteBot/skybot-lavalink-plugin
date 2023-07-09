rootProject.name = "lavalink-plugin"
//include(":skybot-source-managers")
//project(":skybot-source-managers").projectDir = File("../skybot-source-managers")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            common()
            plugins()
        }
    }
}

fun VersionCatalogBuilder.common() {
    library("sourcemanager", "com.dunctebot", "sourcemanagers").version("1.8.4")

    version("lavalink-api", "4.0.0-beta.1")
    version("lavalink-server", "1c0795bf156fe6559c9c0aed0412bcd8f323a3e0")

}

fun VersionCatalogBuilder.plugins() {
    plugin("lavalink", "dev.arbjerg.lavalink.gradle-plugin").version("1.0.7")
}
