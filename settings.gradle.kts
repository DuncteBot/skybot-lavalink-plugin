rootProject.name = "skybot-lavalink-plugin"

include("plugin")
include("source-managers")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            common()
            sourceManager()
            plugins()
        }
    }
}

fun VersionCatalogBuilder.common() {
    version("lavalink-api", "4.0.0-beta.5")
    version("lavalink-server", "47201924be7d5a459753fc85f00ca30e49ba3cd1")
}

fun VersionCatalogBuilder.sourceManager() {
    library("lavaplayer", "dev.arbjerg", "lavaplayer").version("2.0.3")
    library("logger", "org.slf4j", "slf4j-api").version("2.0.7")
    library("commonsIo", "commons-io", "commons-io").version("2.7")
    library("jsoup", "org.jsoup", "jsoup").version("1.15.3")
    library("findbugs", "com.google.code.findbugs", "jsr305").version("3.0.2")
}

fun VersionCatalogBuilder.plugins() {
    plugin("lavalink", "dev.arbjerg.lavalink.gradle-plugin").version("1.0.15")
}
