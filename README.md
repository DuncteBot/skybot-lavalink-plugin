# DuncteBot lavalink plugin

Adding support for the following sources:
- Mixcloud
- ocremix.org
- Clyp.it
- Reddit
- getyarn.io
- Text To Speech (if prefixed with `speak:`)
- TikTok (breaking so often it is not worth my time to fix it)
- PornHub (search by prefixing with `phsearch:`)
- soundgasm
- streamDeckAudio files
  - These files are only accepted over HTTP currently
- Pixeldrain.com

## Lavalink version compatibility

| Lavalink Version | Plugin Version                 |
|------------------|--------------------------------|
| 3.x.x            | 1.4.x OR 1.5.x \*              |
| v4 < 4.2         | 1.4.x, 1.5.x OR 1.6.x OR 1.7.0 |
| 4.2+             | 1.7.1                          |

**\* Version 1.5.x will only work on Lavalink v3 if you are running java 17 or newer**

# Lava*player* users
Currently not supported directly. You will need to compile the project manually for that.

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
        tumblr:
          consumerKey: "<YOUR tumblr consumer key>"
          secretKey: "<YOUR tumblr secret key>"
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
            pixeldrain: true # pixeldrain.com
            tumblr: true # tumblr.com, requires an app to be configured with oauth2 support
```

## development
You can start the test server by running `./gradlew runLavalink`

[VERSION]: https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmaven.lavalink.dev%2Freleases%2Fcom%2Fdunctebot%2Fskybot-lavalink-plugin%2Fmaven-metadata.xml
