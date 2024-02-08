rootProject.name = "skybot-lavalink-plugin-base"

include("plugin")
project(":plugin").name = "skybot-lavalink-plugin"
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
    version("lavalink-api", "4.0.3")
    version("lavalink-server", "4.0.3")
}

fun VersionCatalogBuilder.sourceManager() {
    version("slf4j-version", "2.0.9")

    library("lavaplayer", "dev.arbjerg", "lavaplayer").version("2.0.3")
    library("logger", "org.slf4j", "slf4j-api").versionRef("slf4j-version")
    library("logger-impl", "org.slf4j", "slf4j-simple").versionRef("slf4j-version")
    library("commonsIo", "commons-io", "commons-io").version("2.7")
    library("jsoup", "org.jsoup", "jsoup").version("1.15.3")
    library("findbugs", "com.google.code.findbugs", "jsr305").version("3.0.2")
}

fun VersionCatalogBuilder.plugins() {
    plugin("lavalink", "dev.arbjerg.lavalink.gradle-plugin").version("1.0.15")
}
