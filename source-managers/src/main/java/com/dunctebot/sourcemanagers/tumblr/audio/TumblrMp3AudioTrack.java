package com.dunctebot.sourcemanagers.tumblr.audio;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.dunctebot.sourcemanagers.Mp3Track;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class TumblrMp3AudioTrack extends Mp3Track implements TumblrBasePostAudioTrack {
    private final String mediaUrl;

    public TumblrMp3AudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager, String mediaUrl) {
        super(trackInfo, manager);

        this.mediaUrl = mediaUrl;
    }

    @Override
    public String getMediaUrl() {
        return this.mediaUrl;
    }

    @Override
    public String getPlaybackUrl() {
        return this.getMediaUrl();
    }
}
