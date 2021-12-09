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

Add the following to your lavalink configuration, make sure to replace `VERSION` with the latest version
```yml
plugins:
  - dependency: "com.github.dunctebot:lavalink:VERSION"
    repository: "https://jitpack.io"
```