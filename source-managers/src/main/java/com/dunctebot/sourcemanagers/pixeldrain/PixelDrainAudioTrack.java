package com.dunctebot.sourcemanagers.pixeldrain;

import com.dunctebot.sourcemanagers.AbstractDuncteBotHttpSource;
import com.dunctebot.sourcemanagers.Mp3Track;
import com.sedmelluq.discord.lavaplayer.container.wav.WavAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;

public class PixelDrainAudioTrack extends Mp3Track {
    public PixelDrainAudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager) {
        super(trackInfo, manager);
    }

    @Override
    protected InternalAudioTrack createAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream stream) {
        return new WavAudioTrack(trackInfo, stream);
    }

    @Override
    public String getPlaybackUrl() {
        return String.format(PixeldrainAudioSourceManager.AUDIO_TEMPLATE, this.trackInfo.identifier);
    }
}
