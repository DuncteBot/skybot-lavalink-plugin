package com.dunctebot.sourcemanagers.elgato.streamdeck;

import com.dunctebot.sourcemanagers.Mp3Track;
import com.sedmelluq.discord.lavaplayer.container.wav.WavAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;

public class StreamDeckAudioTrack extends Mp3Track {
    private final StreamDeckAudioSourceManager manager;

    public StreamDeckAudioTrack(AudioTrackInfo trackInfo, StreamDeckAudioSourceManager manager) {
        super(trackInfo, manager);
        this.manager = manager;
    }

    @Override
    protected SeekableInputStream wrapStream(SeekableInputStream stream) {
        return new ElgatoInputStream(stream);
    }

    @Override
    protected InternalAudioTrack createAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream stream) {
        return new WavAudioTrack(trackInfo, stream);
    }

    @Override
    public String getPlaybackUrl() {
        return this.trackInfo.uri;
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new StreamDeckAudioTrack(this.trackInfo, this.manager);
    }

    @Override
    public StreamDeckAudioSourceManager getSourceManager() {
        return this.manager;
    }
}
