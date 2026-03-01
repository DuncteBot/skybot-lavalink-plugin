package com.dunctebot.sourcemanagers.tumblr.audio;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.dunctebot.sourcemanagers.MpegTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class TumblrMpegAudioTrack extends MpegTrack implements TumblrBasePostAudioTrack {
    private final String mediaUrl;

    public TumblrMpegAudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager, String mediaUrl) {
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
