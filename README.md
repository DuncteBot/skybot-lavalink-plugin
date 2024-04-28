# DuncteBot lavalink plugin

Adding support for the following sources:
- Mixcloud
- ocremix.org
- Clyp.it
- Reddit
- getyarn.io
- Text To Speech (if prefixed with `speak:`)
- TikTok (in beta, works on _most_ videos and **will** break all the time)
- PornHub (search by prefixing with `phsearch:`)
- soundgasm

## Lavalink version compatibility

| Lavalink Version | Plugin Version        |
|------------------|-----------------------|
| 3.x.x            | 1.4.x OR 1.5.x \*     |
| 4.x.x            | 1.4.x, 1.5.x OR 1.6.x |

**\* Version 1.5.x will only work on Lavalink v3 if you are running java 17 or newer**

### Latest version old releases

| Notation | Latest Version |
|----------|----------------|
| 1.4.x    | 1.4.2          |
| 1.5.x    | 1.5.1          |

# Lava*player* users
Currently not supported

~~If you need to add the source managers to your lavalink instance as well you can use this library to add them: https://github.com/DuncteBot/skybot-source-managers~~

# Adding to lavalink

Latest version: ![Latest version][VERSION]

Add the following to your lavalink configuration, make sure to replace `VERSION` with the latest version listed above (do not include the `v`).
```yml
lavalink:
    plugins:
        - dependency: "com.dunctebot:skybot-lavalink-plugin:VERSION"
          repository : "https://maven.lavalink.dev/releases" # (optional on lavalink 4)
          snapshot: false # (optional, tells lavalink to use the default snaptshot repository instead)
```

Alternatively you can download the jar from the release on github and place that in your plugins folder

# Configuration
The plugin exposes these configuration options
<br><b>NOTE:</b> this plugins block is a root level object, don't place it where you import the plugin
```yml
plugins:
    dunctebot:
        ttsLanguage: "en-AU" # language of the TTS engine
        sources:
            # true = source enabled, false = source disabled
            getyarn: true # www.getyarn.io
            clypit: true # www.clyp.it
            tts: true # speak:Words to speak
            pornhub: true # should be self-explanatory
            reddit: true # should be self-explanatory
            ocremix: true # www.ocremix.org
            tiktok: true # tiktok.com
            mixcloud: true # mixcloud.com
            soundgasm: true # soundgasm.net
```

## development
You can start the test server by running `./gradlew runLavalink`

[VERSION]: https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.lavalink.dev%2Freleases%2Fcom%2Fdunctebot%2Fskybot-lavalink-plugin%2Fmaven-metadata.xml
