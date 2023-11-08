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

    version("lavalink-api", "4.0.0-beta.5")
    version("lavalink-server", "0f59a5a981af0dfa13cb9f51145e077e8dd89e13")

}

fun VersionCatalogBuilder.plugins() {
    plugin("lavalink", "dev.arbjerg.lavalink.gradle-plugin").version("1.0.15")
}
