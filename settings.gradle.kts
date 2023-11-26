rootProject.name = "skybot-lavalink-plugin"
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
    library("sourcemanager", "com.dunctebot", "sourcemanagers").version("1.9.0")

    version("lavalink-api", "4.0.0-beta.5")
    version("lavalink-server", "47201924be7d5a459753fc85f00ca30e49ba3cd1")

}

fun VersionCatalogBuilder.plugins() {
    plugin("lavalink", "dev.arbjerg.lavalink.gradle-plugin").version("1.0.15")
}
