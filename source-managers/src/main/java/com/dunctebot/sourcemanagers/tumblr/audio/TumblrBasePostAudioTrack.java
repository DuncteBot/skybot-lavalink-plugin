package com.dunctebot.sourcemanagers.tumblr.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public interface TumblrBasePostAudioTrack extends AudioTrack {
    String getMediaUrl();
}
