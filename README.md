# DuncteBot lavalink plugin

Adding support for the following sources:
- ocremix.org (wip)
- spotify (wip) (youtube loading)
- Clyp.it
- Reddit
- getyarn.io
- Text To Speech (if prefixed with `speak:`)
- TikTok (not functional)
- PornHub

# Adding to lavalink

Latest version: ![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/DuncteBot/lavalink?include_prereleases)

Add the following to your lavalink configuration, make sure to replace `VERSION` with the latest version listed above
```yml
plugins:
  - dependency: "com.dunctebot:lavalink:VERSION"
    repository: "https://jitpack.io"
```

# Configuration
The plugin exposes these configuration options
```yml
lavalink:
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