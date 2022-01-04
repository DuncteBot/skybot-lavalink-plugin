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

# Adding to lavalink

Latest version: ![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/DuncteBot/skybot-lavalink-plugin)

Add the following to your lavalink configuration, make sure to replace `VERSION` with the latest version listed above
```yml
lavalink:
    plugins:
        - dependency: "com.dunctebot:skybot-lavalink-plugin:VERSION"
          repository: "https://jitpack.io"
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
            tts: true # tts:Words to speak
            pornhub: true # should be self-explanatory
            reddit: true # should be self-explanatory
            ocremix: true # www.ocremix.org
```