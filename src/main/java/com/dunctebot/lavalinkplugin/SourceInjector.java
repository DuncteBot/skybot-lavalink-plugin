package com.dunctebot.lavalinkplugin;

import com.dunctebot.sourcemanagers.clypit.ClypitAudioSourceManager;
import com.dunctebot.sourcemanagers.getyarn.GetyarnAudioSourceManager;
import com.dunctebot.sourcemanagers.pornhub.PornHubAudioSourceManager;
import com.dunctebot.sourcemanagers.reddit.RedditAudioSourceManager;
import com.dunctebot.sourcemanagers.speech.SpeechAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import org.springframework.stereotype.Component;

// TODO: better name
@Component
public class SourceInjector implements AudioPlayerManagerConfiguration {
    @Override
    public AudioPlayerManager configure(AudioPlayerManager manager) {
        // register custom source managers
        manager.registerSourceManager(new GetyarnAudioSourceManager());
        manager.registerSourceManager(new ClypitAudioSourceManager());
        manager.registerSourceManager(new SpeechAudioSourceManager("en-AU"));
        manager.registerSourceManager(new PornHubAudioSourceManager());
        manager.registerSourceManager(new RedditAudioSourceManager());

        return manager;
    }
}
