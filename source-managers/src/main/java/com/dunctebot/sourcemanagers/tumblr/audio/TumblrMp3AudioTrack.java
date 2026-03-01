package com.dunctebot.sourcemanagers.tumblr.audio;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.dunctebot.sourcemanagers.Mp3Track;
import com.dunctebot.sourcemanagers.tumblr.TumblrPostType;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class TumblrMp3AudioTrack extends Mp3Track implements TumblrBasePostAudioTrack {
    public TumblrMp3AudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager) {
        super(trackInfo, manager);
    }

    @Override
    public String getMediaUrl() {
        return "";
    }
}
