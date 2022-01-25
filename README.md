# DuncteBot lavalink plugin

Adding support for the following sources:
- ocremix.org
- spotify (not implemented) (via youtube loading)
- Clyp.it
- Reddit
- getyarn.io
- Text To Speech (if prefixed with `speak:`)
- TikTok (not functional)
- PornHub

# Lavaplayer users
If you need to add the source managers to your lavalink instance as well you can use this library to add them: https://github.com/DuncteBot/skybot-source-managers

# Adding to lavalink

Latest version: ![Latest version][VERSION]

Add the following to your lavalink configuration, make sure to replace `VERSION` with the latest version listed above (do not include the `v`).
```yml
lavalink:
    plugins:
        - dependency: "com.dunctebot:skybot-lavalink-plugin:VERSION"
          repository: "https://m2.duncte123.dev/releases"
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
```

[VERSION]: https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fm2.duncte123.dev%2Freleases%2Fcom%2Fdunctebot%2Fskybot-lavalink-plugin%2Fmaven-metadata.xml