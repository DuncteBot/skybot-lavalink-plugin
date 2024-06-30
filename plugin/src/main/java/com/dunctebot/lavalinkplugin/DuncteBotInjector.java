package com.dunctebot.lavalinkplugin;

import com.dunctebot.sourcemanagers.clypit.ClypitAudioSourceManager;
import com.dunctebot.sourcemanagers.elgato.streamdeck.StreamDeckAudioSourceManager;
import com.dunctebot.sourcemanagers.getyarn.GetyarnAudioSourceManager;
import com.dunctebot.sourcemanagers.mixcloud.MixcloudAudioSourceManager;
import com.dunctebot.sourcemanagers.ocremix.OCRemixAudioSourceManager;
import com.dunctebot.sourcemanagers.pixeldrain.PixeldrainAudioSourceManager;
import com.dunctebot.sourcemanagers.pornhub.PornHubAudioSourceManager;
import com.dunctebot.sourcemanagers.reddit.RedditAudioSourceManager;
import com.dunctebot.sourcemanagers.soundgasm.SoundGasmAudioSourceManager;
import com.dunctebot.sourcemanagers.speech.SpeechAudioSourceManager;
import com.dunctebot.sourcemanagers.tiktok.TikTokAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

import static java.util.Map.entry;

@Service
public class DuncteBotInjector implements AudioPlayerManagerConfiguration {
    private final Logger logger = LoggerFactory.getLogger(DuncteBotInjector.class);
    private final Map<String, SourceManagerInfo> sourceManagers;

    public DuncteBotInjector(DuncteBotConfig config, DuncteBotConfig.Sources sourcesConfig) {

        this.sourceManagers = Map.ofEntries(
                entry("yarn", new SourceManagerInfo(sourcesConfig::isGetyarn, GetyarnAudioSourceManager::new)),
                entry("clypit", new SourceManagerInfo(sourcesConfig::isClypit, ClypitAudioSourceManager::new)),
                entry("PornHub", new SourceManagerInfo(sourcesConfig::isPornhub, PornHubAudioSourceManager::new)),
                entry("Reddit", new SourceManagerInfo(sourcesConfig::isReddit, RedditAudioSourceManager::new)),
                entry("OC Remix", new SourceManagerInfo(sourcesConfig::isOcremix, OCRemixAudioSourceManager::new)),
                entry("TikTok", new SourceManagerInfo(sourcesConfig::isTiktok, TikTokAudioSourceManager::new)),
                entry("Mixcloud", new SourceManagerInfo(sourcesConfig::isMixcloud, MixcloudAudioSourceManager::new)),
                entry("Soundgasm", new SourceManagerInfo(sourcesConfig::isSoundgasm, SoundGasmAudioSourceManager::new)),
                entry("Elgato (.streamDeckAudio)", new SourceManagerInfo(sourcesConfig::isElgato, StreamDeckAudioSourceManager::new)),
                entry("pixeldrain", new SourceManagerInfo(sourcesConfig::isPixeldrain, PixeldrainAudioSourceManager::new)),
                entry("Text To Speech", new SourceManagerInfo(sourcesConfig::isTts, () -> {
                    final String lang = Objects.requireNonNullElse(config.getTtsLanguage(), "en-AU");

                    logger.info("TTS language is: {}", lang);

                    return new SpeechAudioSourceManager(lang);
                }))
        );
    }

    @NotNull
    @Override
    public AudioPlayerManager configure(@NotNull AudioPlayerManager manager) {

        // register custom source managers
        for (var entry : this.sourceManagers.entrySet()) {
            final var info = entry.getValue();

            if (info.configSupplier().getAsBoolean()) {
                logger.info("Registering {} audio source manager", entry.getKey());
                manager.registerSourceManager(info.supplier().get());
            } else {
                logger.info("{} audio source manager is disabled, skipping it", entry.getKey());
            }
        }

        return manager;
    }
}
