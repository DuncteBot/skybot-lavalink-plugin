package com.dunctebot.lavalinkplugin;

import com.dunctebot.sourcemanagers.clypit.ClypitAudioSourceManager;
import com.dunctebot.sourcemanagers.getyarn.GetyarnAudioSourceManager;
import com.dunctebot.sourcemanagers.mixcloud.MixcloudAudioSourceManager;
import com.dunctebot.sourcemanagers.ocremix.OCRemixAudioSourceManager;
import com.dunctebot.sourcemanagers.pornhub.PornHubAudioSourceManager;
import com.dunctebot.sourcemanagers.reddit.RedditAudioSourceManager;
import com.dunctebot.sourcemanagers.speech.SpeechAudioSourceManager;
import com.dunctebot.sourcemanagers.tiktok.TikTokAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DuncteBotInjector implements AudioPlayerManagerConfiguration {
    private final DuncteBotConfig config;
    private final DuncteBotConfig.Sources sourcesConfig;

    public DuncteBotInjector(DuncteBotConfig config, DuncteBotConfig.Sources sourcesConfig) {
        this.config = config;
        this.sourcesConfig = sourcesConfig;
    }

    @Override
    public AudioPlayerManager configure(AudioPlayerManager manager) {
        final Logger logger = LoggerFactory.getLogger(DuncteBotInjector.class);

        // register custom source managers

        if (this.sourcesConfig.isGetyarn()) {
            logger.info("Registering getyarn audio source manager");
            manager.registerSourceManager(new GetyarnAudioSourceManager());
        }

        if (this.sourcesConfig.isClypit()) {
            logger.info("Registering clypit audio source manager");
            manager.registerSourceManager(new ClypitAudioSourceManager());
        }

        if (this.sourcesConfig.isTts()) {
            final String lang = Objects.requireNonNullElse(this.config.getTtsLanguage(), "en-AU");

            logger.info("Registering text to speech audio source manager with language {}", lang);
            manager.registerSourceManager(new SpeechAudioSourceManager(lang));
        }

        if (this.sourcesConfig.isPornhub()) {
            logger.info("Registering PornHub audio source manager");
            manager.registerSourceManager(new PornHubAudioSourceManager());
        }

        if (this.sourcesConfig.isReddit()) {
            logger.info("Registering reddit audio source manager");
            manager.registerSourceManager(new RedditAudioSourceManager());
        }

        if (this.sourcesConfig.isOcremix()) {
            logger.info("Registering OC Remix audio source manager");
            manager.registerSourceManager(new OCRemixAudioSourceManager());
        }

        if (this.sourcesConfig.isTiktok()) {
            logger.info("Registering TikTok audio source manager");
            manager.registerSourceManager(new TikTokAudioSourceManager());
        }

        if (this.sourcesConfig.isMixcloud()) {
            logger.info("Registering Mixcloud audio source manager");
            manager.registerSourceManager(new MixcloudAudioSourceManager());
        }

        if (this.sourcesConfig.isSoundgasm()) {
            logger.info("Registering Soundgasm audio source manager");
            manager.registerSourceManager(new SoundgasmAudioSourceManager());
        }

        return manager;
    }
}
